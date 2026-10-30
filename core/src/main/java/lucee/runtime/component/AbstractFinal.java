/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.runtime.Component;
import lucee.runtime.Interface;
import lucee.runtime.InterfaceImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;

public final class AbstractFinal {

	private final Map<String, InterfaceImpl> interfaces = new HashMap<>();
	private Map<Collection.Key, UDFB> absUDFs = new HashMap<Collection.Key, UDFB>();
	private final Map<Collection.Key, UDF> finUDFs = new HashMap<Collection.Key, UDF>();
	// the interface that provides the function in absUDFs (LDEV-1835), no entry for functions from abstract components
	private final Map<Collection.Key, InterfaceImpl> owners = new HashMap<>();
	// default functions provided by more than one unrelated interface, key -> names of the interfaces (LDEV-1835)
	private final Map<Collection.Key, List<String>> ambiguous = new HashMap<>();

	public void add(List<InterfaceImpl> interfaces) {
		// add all interfaces to a flat structure
		Iterator<InterfaceImpl> it = interfaces.iterator();
		Iterator<UDF> iit;
		InterfaceImpl inter;
		UDF udf;
		while (it.hasNext()) {
			inter = it.next();
			List<InterfaceImpl> parents = inter._getExtends();

			// first add the parents, so children can overwrite functions with same name
			if (!ArrayUtil.isEmpty(parents)) add(parents);

			// UDFs
			iit = inter.getUDFIt();
			while (iit.hasNext()) {
				udf = iit.next();
				add(udf, inter);
			}

			this.interfaces.put(inter.getPageSource().getDisplayPath(), inter); // this is add to a map to ensure we have every interface only once

		}

	}

	public void add(Collection.Key key, UDF udf) throws ApplicationException {
		if (Component.MODIFIER_ABSTRACT == udf.getModifier()) absUDFs.put(key, new UDFB(udf));
		if (Component.MODIFIER_FINAL == udf.getModifier()) {
			if (finUDFs.containsKey(key)) {
				UDF existing = finUDFs.get(key);
				throw new ApplicationException("the function [" + key + "] from component [" + udf.getSource()
						+ "] tries to override a final method with the same name from component [" + existing.getSource() + "]");
			}
			finUDFs.put(key, udf);
		}
	}

	private void add(UDF udf, InterfaceImpl inter) {
		Collection.Key key = KeyImpl.init(udf.getFunctionName());
		boolean isDefault = udf.getModifier() == ComponentUtil.MODIFIER_DEFAULT;

		UDFB existing = absUDFs.get(key);
		if (existing != null) {
			InterfaceImpl existingOwner = owners.get(key);
			// a function declared abstract in an abstract component is not replaced by a default function
			if (existingOwner == null) {
				if (isDefault) return;
			}
			// the same interface reached again (e.g. via a different parent), nothing to do
			else if (samePage(existingOwner, inter)) return;
			else {
				// the existing function comes from a more specific interface, so that one wins
				if (isSubInterface(existingOwner, inter)) return;
				// unrelated interfaces
				if (!isSubInterface(inter, existingOwner)) {
					boolean existingIsDefault = existing.udf.getModifier() == ComponentUtil.MODIFIER_DEFAULT;
					if (isDefault) {
						// an abstract function always wins over a default function
						if (!existingIsDefault) return;
						// 2 default functions, the component has to decide
						List<String> names = ambiguous.get(key);
						if (names == null) {
							names = new ArrayList<>();
							names.add(existingOwner.getPageSource().getDisplayPath());
							ambiguous.put(key, names);
						}
						String name = inter.getPageSource().getDisplayPath();
						if (!names.contains(name)) names.add(name);
						return;
					}
				}
			}
		}
		// the more specific interface decides again
		ambiguous.remove(key);
		absUDFs.put(key, new UDFB(udf));
		owners.put(key, inter);
	}

	private static boolean samePage(InterfaceImpl a, InterfaceImpl b) {
		return a.getPageSource().getDisplayPath().equals(b.getPageSource().getDisplayPath());
	}

	/**
	 * is "sub" the same as or an interface that extends (directly or indirectly) "sup"
	 */
	private static boolean isSubInterface(InterfaceImpl sub, InterfaceImpl sup) {
		if (samePage(sub, sup)) return true;
		List<InterfaceImpl> parents = sub._getExtends();
		if (parents == null) return false;
		for (InterfaceImpl parent: parents) {
			if (isSubInterface(parent, sup)) return true;
		}
		return false;
	}

	/**
	 * @return the names of the interfaces if more than one unrelated interface provides a default
	 *         function with that name, otherwise null
	 */
	public List<String> getAmbiguousDefault(Collection.Key key) {
		return ambiguous.get(key);
	}

	/*
	 * public long lastUpdate() { if(lastUpdate==0 && !interfaces.isEmpty()){ long temp;
	 * Iterator<InterfaceImpl> it = interfaces.values().iterator(); while(it.hasNext()){
	 * temp=ComponentUtil.getCompileTime(null,it.next().getPageSource(),0); if(temp>lastUpdate)
	 * lastUpdate=temp; } } return lastUpdate; }
	 */

	public boolean hasAbstractUDFs() {
		return !absUDFs.isEmpty();
	}

	public boolean hasFinalUDFs() {
		return !finUDFs.isEmpty();
	}

	public boolean hasUDFs() {
		return !finUDFs.isEmpty() || !absUDFs.isEmpty();
	}

	public Iterator<InterfaceImpl> getInterfaceIt() {
		return interfaces.values().iterator();
	}

	public Interface[] getInterfaces() {
		return interfaces.values().toArray(new Interface[interfaces.size()]);
	}

	/*
	 * public Map<Collection.Key,UDF> getAbstractUDFs() { Map<Key, UDF> tmp = absUDFs; absUDFs=new
	 * HashMap<Collection.Key,UDF>(); return tmp; }
	 */

	public Map<Collection.Key, UDFB> getAbstractUDFBs() {
		return absUDFs;
	}

	public Map<Collection.Key, UDF> getFinalUDFs() {
		return finUDFs;
	}

	public boolean hasInterfaces() {
		return !interfaces.isEmpty();
	}

	public static class UDFB {

		public boolean used = false;
		public final UDF udf;

		public UDFB(UDF udf) {
			this.udf = udf;
		}

	}
}