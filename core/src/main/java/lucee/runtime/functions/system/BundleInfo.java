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

import java.util.Iterator;
import java.util.List;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.java.JavaObject;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.OSGiUtil.PackageQuery;
import lucee.runtime.osgi.OSGiUtil.VersionDefinition;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class BundleInfo implements Function {

	private static final long serialVersionUID = 3928190461638362170L;

	public static Struct call(PageContext pc, Object obj) throws PageException {
		if (obj == null) throw new FunctionException(pc, "bundleInfo", 1, "object", "value is null");

		Class<?> clazz;
		if (obj instanceof JavaObject) clazz = ((JavaObject) obj).getClazz();
		else if (obj instanceof ObjectWrap) clazz = ((ObjectWrap) obj).getEmbededObject().getClass();
		else clazz = obj.getClass();

		ClassLoader cl = clazz.getClassLoader();
		if (cl instanceof BundleClassLoader) {
			BundleClassLoader bcl = (BundleClassLoader) cl;
			Bundle b = bcl.getBundle();
			Struct sct = new StructImpl();
			sct.setEL(KeyConstants._id, b.getBundleId());
			sct.setEL(KeyConstants._name, b.getSymbolicName());
			sct.setEL(KeyConstants._location, b.getLocation());
			sct.setEL(KeyConstants._version, b.getVersion().toString());
			sct.setEL(KeyConstants._state, OSGiUtil.toState(b.getState(), null));
			try {
				sct.setEL("requiredBundles", toArray1(OSGiUtil.getRequiredBundles(b)));
				sct.setEL("requiredPackages", toArray2(OSGiUtil.getRequiredPackages(b)));
			}
			catch (BundleException be) {
				throw Caster.toPageException(be);
			}
			return sct;
		}
		throw new ApplicationException("object [" + clazz + "] is not from an OSGi bundle");
	}

	private static Array toArray1(List<BundleDefinition> list) {
		Struct sct;
		Array arr = new ArrayImpl();
		Iterator<BundleDefinition> it = list.iterator();
		BundleDefinition bd;
		VersionDefinition vd;
		while (it.hasNext()) {
			bd = it.next();
			sct = new StructImpl();
			sct.setEL(KeyConstants._bundleName, bd.getName());
			vd = bd.getVersionDefiniton();
			if (vd != null) {
				sct.setEL(KeyConstants._bundleVersion, vd.getVersionAsString());
				sct.setEL("operator", vd.getOpAsString());
			}
			arr.appendEL(sct);
		}
		return arr;
	}

	private static Array toArray2(List<PackageQuery> list) {
		Struct sct, _sct;
		Array arr = new ArrayImpl(), _arr;
		Iterator<PackageQuery> it = list.iterator();
		PackageQuery pd;
		Iterator<VersionDefinition> _it;
		VersionDefinition vd;
		while (it.hasNext()) {
			pd = it.next();
			sct = new StructImpl();
			sct.setEL(KeyConstants._package, pd.getName());
			sct.setEL("versions", _arr = new ArrayImpl());

			_it = pd.getVersionDefinitons().iterator();
			while (_it.hasNext()) {
				vd = _it.next();
				_sct = new StructImpl();
				_sct.setEL(KeyConstants._bundleVersion, vd.getVersion().toString());
				_sct.setEL("operator", vd.getOpAsString());
				_arr.appendEL(_sct);
			}
			arr.appendEL(sct);
		}
		return arr;
	}
}