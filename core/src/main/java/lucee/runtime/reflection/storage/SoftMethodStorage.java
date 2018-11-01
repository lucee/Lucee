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

import static org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength.SOFT;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.map.ReferenceMap;

import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;

/**
 * Method Storage Class
 */
public final class SoftMethodStorage {
    private Map<Class, Map<Key, Array>> map = new ReferenceMap<Class, Map<Key, Array>>(SOFT, SOFT);

    /**
     * returns a methods matching given criteria or null if method doesn't exist
     * 
     * @param clazz clazz to get methods from
     * @param methodName Name of the Method to get
     * @param count wished count of arguments
     * @return matching Methods as Array
     */
    public Method[] getMethods(Class clazz, Collection.Key methodName, int count) {
	Map<Key, Array> methodsMap = map.get(clazz);
	if (methodsMap == null) methodsMap = store(clazz);

	Array methods = methodsMap.get(methodName);
	if (methods == null) return null;

	Object o = methods.get(count + 1, null);
	if (o == null) return null;
	return (Method[]) o;
    }

    /**
     * store a class with his methods
     * 
     * @param clazz
     * @return returns stored struct
     */
    private Map<Key, Array> store(Class clazz) {
	Method[] methods = clazz.getMethods();
	Map<Key, Array> methodsMap = new ConcurrentHashMap<Key, Array>();
	for (int i = 0; i < methods.length; i++) {
	    storeMethod(methods[i], methodsMap);

	}
	map.put(clazz, methodsMap);
	return methodsMap;
    }

    /**
     * stores a single method
     * 
     * @param method
     * @param methodsMap
     */
    private void storeMethod(Method method, Map<Key, Array> methodsMap) {
	Key methodName = KeyImpl.init(method.getName());

	Array methodArgs;
	synchronized (methodsMap) {
	    methodArgs = methodsMap.get(methodName);
	    if (methodArgs == null) {
		methodArgs = new ArrayImpl();
		methodsMap.put(methodName, methodArgs);
	    }
	}

	storeArgs(method, methodArgs);
	// Modifier.isStatic(method.getModifiers());
    }

    /**
     * stores arguments of a method
     * 
     * @param method
     * @param methodArgs
     */
    private void storeArgs(Method method, Array methodArgs) {

	Class[] pmt = method.getParameterTypes();
	Method[] args;
	synchronized (methodArgs) {
	    Object o = methodArgs.get(pmt.length + 1, null);
	    if (o == null) {
		args = new Method[1];
		methodArgs.setEL(pmt.length + 1, args);
	    }
	    else {
		Method[] ms = (Method[]) o;
		args = new Method[ms.length + 1];
		for (int i = 0; i < ms.length; i++) {
		    args[i] = ms[i];
		}
		methodArgs.setEL(pmt.length + 1, args);
	    }
	}
	args[args.length - 1] = method;
    }
}