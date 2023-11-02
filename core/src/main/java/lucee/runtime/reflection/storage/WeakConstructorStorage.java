/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.reflection.storage;

import java.lang.reflect.Constructor;
import java.util.WeakHashMap;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

/**
 * Constructor Storage Class
 */
public final class WeakConstructorStorage {
	private WeakHashMap<Class, Array> map = new WeakHashMap<Class, Array>();

	/**
	 * returns a constructor matching given criteria or null if Constructor doesn't exist
	 * 
	 * @param clazz Class to get Constructor for
	 * @param count count of arguments for the constructor
	 * @return returns the constructors
	 */
	public Constructor[] getConstructors(Class clazz, int count) {
		Array con;
		Object o;
		synchronized (map) {
			o = map.get(clazz);
			if (o == null) {
				con = store(clazz);
			}
			else con = (Array) o;
		}
		o = con.get(count + 1, null);
		if (o == null) return null;
		return (Constructor[]) o;
	}

	/**
	 * stores the constructors for a Class
	 * 
	 * @param clazz
	 * @return stored structure
	 */
	private Array store(Class clazz) {
		Constructor[] conArr = clazz.getConstructors();
		Array args = new ArrayImpl();
		for (int i = 0; i < conArr.length; i++) {
			storeArgs(conArr[i], args);
		}
		map.put(clazz, args);
		return args;

	}

	/**
	 * seperate and store the different arguments of one constructor
	 * 
	 * @param constructor
	 * @param conArgs
	 */
	private void storeArgs(Constructor constructor, Array conArgs) {
		Class[] pmt = constructor.getParameterTypes();
		Object o = conArgs.get(pmt.length + 1, null);
		Constructor[] args;
		if (o == null) {
			args = new Constructor[1];
			conArgs.setEL(pmt.length + 1, args);
		}
		else {
			Constructor[] cs = (Constructor[]) o;
			args = new Constructor[cs.length + 1];
			for (int i = 0; i < cs.length; i++) {
				args[i] = cs[i];
			}
			conArgs.setEL(pmt.length + 1, args);
		}
		args[args.length - 1] = constructor;

	}
}