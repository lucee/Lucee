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

import java.lang.reflect.Field;
import java.util.WeakHashMap;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * Method Storage Class
 */
public final class WeakFieldStorage {
	private WeakHashMap map = new WeakHashMap();

	/**
	 * returns all fields matching given criteria or null if field does exist
	 * 
	 * @param clazz clazz to get field from
	 * @param fieldname Name of the Field to get
	 * @return matching Fields as Array
	 */
	public Field[] getFields(Class clazz, String fieldname) {
		Struct fieldMap;
		Object o;
		synchronized (map) {
			o = map.get(clazz);
			if (o == null) {
				fieldMap = store(clazz);
			}
			else fieldMap = (Struct) o;
		}
		o = fieldMap.get(fieldname, null);
		if (o == null) return null;
		return (Field[]) o;

	}

	/**
	 * store a class with his methods
	 * 
	 * @param clazz
	 * @return returns stored Struct
	 */
	private StructImpl store(Class clazz) {
		Field[] fieldsArr = clazz.getFields();
		StructImpl fieldsMap = new StructImpl();
		for (int i = 0; i < fieldsArr.length; i++) {
			storeField(fieldsArr[i], fieldsMap);
		}
		map.put(clazz, fieldsMap);
		return fieldsMap;
	}

	/**
	 * stores a single method
	 * 
	 * @param field
	 * @param fieldsMap
	 */
	private void storeField(Field field, StructImpl fieldsMap) {
		String fieldName = field.getName();
		Object o = fieldsMap.get(fieldName, null);
		Field[] args;
		if (o == null) {
			args = new Field[1];
			fieldsMap.setEL(fieldName, args);
		}
		else {
			Field[] fs = (Field[]) o;
			args = new Field[fs.length + 1];
			for (int i = 0; i < fs.length; i++) {
				fs[i].setAccessible(true);
				args[i] = fs[i];
			}
			fieldsMap.setEL(fieldName, args);
		}
		args[args.length - 1] = field;
	}

}