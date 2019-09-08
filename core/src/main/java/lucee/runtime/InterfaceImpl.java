/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.component.MetaDataSoftReference;
import lucee.runtime.component.MetadataUtil;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFImpl;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

/**
 * 
 * MUST add handling for new attributes (style, namespace, serviceportname, porttypename, wsdlfile,
 * bindingname, and output)
 */
public class InterfaceImpl implements Interface {

	private static final long serialVersionUID = -2488865504508636253L;

	private static final InterfaceImpl[] EMPTY = new InterfaceImpl[] {};

	private PageSource pageSource;
	private String strExtend;
	private String hint;
	private String dspName;
	private String callPath;
	private boolean realPath;
	private Map meta;
	boolean initialized;

	private List<InterfaceImpl> extend;

	private final Map<Collection.Key, UDF> udfs = new HashMap<Collection.Key, UDF>();
	// private Map<Collection.Key,UDF> interfacesUDFs=null;

	public InterfaceImpl(PageContext pc, InterfacePageImpl page, String strExtend, String hint, String dspName, String callPath, boolean realPath, Map meta) throws PageException {
		// print.ds("Interface::Constructor:"+page.getPageSource().getDisplayPath());

		pc = ThreadLocalPageContext.get(pc);
		this.pageSource = page.getPageSource();
		this.strExtend = strExtend;
		this.hint = hint;
		this.dspName = dspName;
		this.callPath = callPath;
		this.realPath = realPath;
		this.meta = meta;

		// load extends
		if (!StringUtil.isEmpty(strExtend, true)) this.extend = loadInterfaces(pc, pageSource, strExtend);
	}

	public static List<InterfaceImpl> loadInterfaces(PageContext pc, PageSource loadingLocation, String listExtends) throws PageException {
		List<InterfaceImpl> extend = new ArrayList<InterfaceImpl>();
		Iterator<String> it = lucee.runtime.type.util.ListUtil.toListRemoveEmpty(listExtends, ',').iterator();
		InterfaceImpl inter;
		String str;

		while (it.hasNext()) {
			str = it.next().trim();
			if (str.isEmpty()) continue;
			inter = ComponentLoader.searchInterface(pc, loadingLocation, str);
			extend.add(inter);
		}
		return extend;
	}

	@Override
	public boolean instanceOf(String type) {
		if (realPath) {
			if (type.equalsIgnoreCase(callPath)) return true;
			if (type.equalsIgnoreCase(pageSource.getComponentName())) return true;
			if (type.equalsIgnoreCase(_getName())) return true;
		}
		else {
			if (type.equalsIgnoreCase(callPath)) return true;
			if (type.equalsIgnoreCase(_getName())) return true;
		}

		// extends
		if (extend == null || extend.isEmpty()) return false; // no kids

		Iterator<InterfaceImpl> it = extend.iterator();
		while (it.hasNext()) {
			if (it.next().instanceOf(type)) return true;
		}
		return false;
	}

	/**
	 * @return the callPath
	 */
	@Override
	public String getCallPath() {
		return callPath;
	}

	private String _getName() { // MUST nicht so toll
		if (callPath == null) return "";
		return lucee.runtime.type.util.ListUtil.last(callPath, "./", true);
	}

	@Override
	public void registerUDF(Collection.Key key, UDF udf) throws ApplicationException {
		if (udf.getModifier() == Component.MODIFIER_FINAL)
			throw new ApplicationException("the final function [" + key + "] is not allowed within the interface [" + getPageSource().getDisplayPath() + "]");
		udfs.put(key, udf);
	}

	@Override
	public void registerUDF(Collection.Key key, UDFProperties props) throws ApplicationException {
		registerUDF(key, new UDFImpl(props));
	}

	public void regJavaFunction(Collection.Key key, String className) throws ClassException, ClassNotFoundException, IOException, ApplicationException {
		registerUDF(key, (UDF) ClassUtil.loadInstance(getPageSource().getMapping().getPhysicalClass(className)));
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable table = new DumpTable("interface", "#99cc99", "#ffffff", "#000000");
		table.setTitle("Interface " + callPath + "" + (" " + StringUtil.escapeHTML(dspName)));
		table.setComment("Interface can not directly invoked as an object");
		// if(top.properties.extend.length()>0)table.appendRow(1,new SimpleDumpData("Extends"),new
		// SimpleDumpData(top.properties.extend));
		// if(top.properties.hint.trim().length()>0)table.appendRow(1,new SimpleDumpData("Hint"),new
		// SimpleDumpData(top.properties.hint));

		// table.appendRow(1,new SimpleDumpData(""),_toDumpData(top,pageContext,maxlevel,access));
		return table;
	}

	/*
	 * *
	 * 
	 * @return the page / public InterfacePage getPage() { return page; }
	 */

	@Override
	public PageSource getPageSource() {
		return pageSource;
	}

	@Override
	public Interface[] getExtends() {
		return extend == null ? EMPTY : extend.toArray(new InterfaceImpl[extend.size()]);
	}

	public List<InterfaceImpl> _getExtends() {
		return extend;
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return _getMetaData(pc, this, false);
	}

	@Override
	public Struct getMetaData(PageContext pc, boolean ignoreCache) throws PageException {
		return _getMetaData(pc, this, ignoreCache);
	}

	private static Struct _getMetaData(PageContext pc, InterfaceImpl icfc, boolean ignoreCache) throws PageException {
		Page page = MetadataUtil.getPageWhenMetaDataStillValid(pc, icfc, ignoreCache);
		if (page != null && page.metaData != null && page.metaData.get() != null) return page.metaData.get();

		long creationTime = System.currentTimeMillis();

		Struct sct = new StructImpl();
		ArrayImpl arr = new ArrayImpl();
		{
			Iterator<UDF> it = icfc.udfs.values().iterator();
			while (it.hasNext()) {
				arr.append(it.next().getMetaData(pc));
			}
		}

		if (icfc.meta != null) {
			Iterator it = icfc.meta.entrySet().iterator();
			Map.Entry entry;
			while (it.hasNext()) {
				entry = (Entry) it.next();
				sct.setEL(KeyImpl.toKey(entry.getKey()), entry.getValue());
			}
		}

		if (!StringUtil.isEmpty(icfc.hint, true)) sct.set(KeyConstants._hint, icfc.hint);
		if (!StringUtil.isEmpty(icfc.dspName, true)) sct.set(KeyConstants._displayname, icfc.dspName);
		// init(pc,icfc);
		if (!ArrayUtil.isEmpty(icfc.extend)) {
			Set<String> _set = lucee.runtime.type.util.ListUtil.listToSet(icfc.strExtend, ',', true);
			Struct ex = new StructImpl();
			sct.set(KeyConstants._extends, ex);
			Iterator<InterfaceImpl> it = icfc.extend.iterator();
			InterfaceImpl inter;
			while (it.hasNext()) {
				inter = it.next();
				if (!_set.contains(inter.getCallPath())) continue;
				ex.setEL(KeyImpl.init(inter.getCallPath()), _getMetaData(pc, inter, true));
			}

		}

		if (arr.size() != 0) sct.set(KeyConstants._functions, arr);
		PageSource ps = icfc.pageSource;
		sct.set(KeyConstants._name, ps.getComponentName());
		sct.set(KeyConstants._fullname, ps.getComponentName());

		sct.set(KeyConstants._path, ps.getDisplayPath());
		sct.set(KeyConstants._type, "interface");

		page.metaData = new MetaDataSoftReference<Struct>(sct, creationTime);
		return sct;
	}

	@Override
	public Variables beforeStaticConstructor(PageContext pc) {
		return null;
	}

	@Override
	public void afterStaticConstructor(PageContext pc, Variables var) {

	}

	public Iterator<UDF> getUDFIt() {
		return udfs.values().iterator();
	}

}