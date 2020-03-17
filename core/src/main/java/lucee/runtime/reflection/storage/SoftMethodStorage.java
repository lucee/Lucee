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

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;

/**
 * Method Storage Class
 */
public final class SoftMethodStorage {
	private final ConcurrentHashMap<String, Object> tokens = new ConcurrentHashMap<String, Object>();

	private Map<Class, SoftReference<Map<Key, Map<Integer, Method[]>>>> map = new ConcurrentHashMap<Class, SoftReference<Map<Key, Map<Integer, Method[]>>>>();

	/**
	 * returns a methods matching given criteria or null if method doesn't exist
	 * 
	 * @param clazz clazz to get methods from
	 * @param methodName Name of the Method to get
	 * @param count wished count of arguments
	 * @return matching Methods as Array
	 */
	public Method[] getMethods(Class clazz, Collection.Key methodName, int count) {
		SoftReference<Map<Key, Map<Integer, Method[]>>> tmp = map.get(clazz);
		Map<Key, Map<Integer, Method[]>> methodsMap = tmp == null ? null : tmp.get();
		if (methodsMap == null) methodsMap = store(clazz);

		Map<Integer, Method[]> methods = methodsMap.get(methodName);
		if (methods == null) return null;

		return methods.get(count + 1);
	}

	/**
	 * store a class with his methods
	 * 
	 * @param clazz
	 * @return returns stored struct
	 */
	private Map<Key, Map<Integer, Method[]>> store(Class clazz) {
		synchronized (getToken(clazz)) {
			Method[] methods = clazz.getMethods();
			Map<Key, Map<Integer, Method[]>> methodsMap = new ConcurrentHashMap<Key, Map<Integer, Method[]>>();
			for (int i = 0; i < methods.length; i++) {
				storeMethod(methods[i], methodsMap);
			}
			map.put(clazz, new SoftReference<Map<Key, Map<Integer, Method[]>>>(methodsMap));
			return methodsMap;
		}
	}

	private Object getToken(Class clazz) {
		Object newLock = new Object();
		Object lock = tokens.putIfAbsent(clazz.getName(), newLock);
		if (lock == null) {
			lock = newLock;
		}
		return lock;
	}

	/**
	 * stores a single method
	 * 
	 * @param method
	 * @param methodsMap
	 */
	private void storeMethod(Method method, Map<Key, Map<Integer, Method[]>> methodsMap) {
		Key methodName = KeyImpl.init(method.getName());

		Map<Integer, Method[]> methodArgs = methodsMap.get(methodName);
		if (methodArgs == null) {
			methodArgs = new ConcurrentHashMap<Integer, Method[]>();
			methodsMap.put(methodName, methodArgs);
		}
		storeArgs(method, methodArgs);
	}

	/**
	 * stores arguments of a method
	 * 
	 * @param method
	 * @param methodArgs
	 */
	private void storeArgs(Method method, Map<Integer, Method[]> methodArgs) {

		Class[] pmt = method.getParameterTypes();
		Method[] args;
		Method[] ms = methodArgs.get(pmt.length + 1);
		if (ms == null) {
			args = new Method[1];
			methodArgs.put(pmt.length + 1, args);
		}
		else {
			args = new Method[ms.length + 1];
			for (int i = 0; i < ms.length; i++) {
				args[i] = ms[i];
			}
			methodArgs.put(pmt.length + 1, args);
		}
		args[args.length - 1] = method;
	}
}