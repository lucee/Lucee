/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 */
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.osgi.framework.Bundle;

public class BundleInfo implements Function {

	private static final long serialVersionUID = 3928190461638362170L;

	public static Struct call(PageContext pc , Object obj) throws PageException {
		if(obj==null) throw new FunctionException(pc, "bundleInfo", 1, "object", "value is null");
		
		Class<?> clazz;
		if(obj instanceof JavaObject) clazz=((JavaObject)obj).getClazz();
		else if(obj instanceof ObjectWrap) clazz=((ObjectWrap)obj).getEmbededObject().getClass();
		else clazz=obj.getClass();
		
		ClassLoader cl = clazz.getClassLoader();
		if(cl instanceof BundleClassLoader) {
			BundleClassLoader bcl=(BundleClassLoader) cl;
			Bundle b=bcl.getBundle();
			Struct sct=new StructImpl();
			sct.setEL(KeyConstants._id, b.getBundleId());
			sct.setEL(KeyConstants._name, b.getSymbolicName());
			sct.setEL("location", b.getLocation());
			sct.setEL(KeyConstants._version, b.getVersion().toString());
			sct.setEL(KeyConstants._state, OSGiUtil.toState(b.getState(), null));
			return sct;
		}
		throw new ApplicationException(obj+"given object is not from a OSGi bundle");
	}
}