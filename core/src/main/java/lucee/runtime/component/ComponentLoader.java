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
package lucee.runtime.component;

import javax.servlet.jsp.tagext.BodyContent;

import lucee.print;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CIObject;
import lucee.runtime.CIPage;
import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.InterfaceImpl;
import lucee.runtime.InterfacePageImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.Constants;
import lucee.runtime.debug.DebugEntryTemplate;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.writer.BodyContentUtil;

public class ComponentLoader {

	private static final short RETURN_TYPE_PAGE = 1;
	private static final short RETURN_TYPE_INTERFACE = 2;
	private static final short RETURN_TYPE_COMPONENT = 3;

	private static final ResourceFilter DIR_OR_EXT = new OrResourceFilter(
			new ResourceFilter[] { DirectoryResourceFilter.FILTER, new ExtensionResourceFilter(Constants.getComponentExtensions()) });
	private static final ImportDefintion[] EMPTY_ID = new ImportDefintion[0];

	/**
	 * 
	 * @param pc
	 * @param loadingLocation
	 * @param rawPath
	 * @param searchLocal
	 * @param searchRoot
	 * @param isExtendedComponent if set to true this is a base component loaded because another
	 *            component has defined this component via extends
	 * @return
	 * @throws PageException
	 */
	public static ComponentImpl searchComponent(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot, boolean isExtendedComponent)
			throws PageException {
		return (ComponentImpl) _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, true, RETURN_TYPE_COMPONENT, isExtendedComponent);
	}

	public static ComponentImpl searchComponent(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot,
			final boolean isExtendedComponent, boolean executeConstr) throws PageException {
		return (ComponentImpl) _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, RETURN_TYPE_COMPONENT, isExtendedComponent);
	}

	public static InterfaceImpl searchInterface(PageContext pc, PageSource loadingLocation, String rawPath, boolean executeConstr) throws PageException {
		return (InterfaceImpl) _search(pc, loadingLocation, rawPath, Boolean.TRUE, Boolean.TRUE, executeConstr, RETURN_TYPE_INTERFACE, false);
	}

	public static InterfaceImpl searchInterface(PageContext pc, PageSource loadingLocation, String rawPath) throws PageException {
		return (InterfaceImpl) _search(pc, loadingLocation, rawPath, Boolean.TRUE, Boolean.TRUE, true, RETURN_TYPE_INTERFACE, false);
	}

	public static Page searchPage(PageContext pc, PageSource child, String rawPath, Boolean searchLocal, Boolean searchRoot) throws PageException {
		return (Page) _search(pc, child, rawPath, searchLocal, searchRoot, false, RETURN_TYPE_PAGE, false);
	}

	private static Object _search(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot, boolean executeConstr, short returnType,
			final boolean isExtendedComponent) throws PageException {
		PageSource currPS = pc.getCurrentPageSource(null);

		ImportDefintion[] importDefintions = null;
		if (currPS != null) {
			Page currP;
			Component cfc = pc.getActiveComponent();
			if (cfc instanceof ComponentImpl && currPS.equals(cfc.getPageSource())) {
				importDefintions = ((ComponentImpl) cfc)._getImportDefintions();
			}
			else if ((currP = currPS.loadPage(pc, false, null)) != null) {
				importDefintions = currP.getImportDefintions();
			}
		}

		int dialect = currPS == null ? pc.getCurrentTemplateDialect() : currPS.getDialect();
		// first try for the current dialect
		Object obj = _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions, dialect, isExtendedComponent);
		// then we try the opposite dialect
		if (obj == null && ((ConfigPro) pc.getConfig()).allowLuceeDialect()) { // only when the lucee dialect is enabled we have to check the opposite
			obj = _search(pc, loadingLocation, rawPath, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions,
					dialect == CFMLEngine.DIALECT_CFML ? CFMLEngine.DIALECT_LUCEE : CFMLEngine.DIALECT_CFML, isExtendedComponent);
		}

		if (obj == null)
			throw new ExpressionException("invalid " + toStringType(returnType, dialect) + " definition, can't find " + toStringType(returnType, dialect) + " [" + rawPath + "]");
		return obj;
	}

	private static Object _search(PageContext pc, PageSource loadingLocation, String rawPath, Boolean searchLocal, Boolean searchRoot, boolean executeConstr, short returnType,
			PageSource currPS, ImportDefintion[] importDefintions, int dialect, final boolean isExtendedComponent) throws PageException {
		ConfigPro config = (ConfigPro) pc.getConfig();

		if (dialect == CFMLEngine.DIALECT_LUCEE && !config.allowLuceeDialect()) PageContextImpl.notSupported();

		boolean doCache = config.useComponentPathCache();
		String sub = null;
		if (returnType != RETURN_TYPE_PAGE && rawPath.indexOf('$') != -1) {
			int d = rawPath.lastIndexOf('$');
			int s = rawPath.lastIndexOf('.');
			if (d > s) {
				sub = rawPath.substring(d + 1);
				rawPath = rawPath.substring(0, d);
			}
		}

		// app-String appName=pc.getApplicationContext().getName();
		rawPath = rawPath.trim().replace('\\', '/');
		String path = (rawPath.indexOf("./") == -1) ? rawPath.replace('.', '/') : rawPath;

		boolean isRealPath = !StringUtil.startsWith(path, '/');
		// PageSource currPS = pc.getCurrentPageSource();
		// Page currP=currPS.loadPage(pc,false);
		PageSource ps = null;
		CIPage page = null;

		// MUSTMUST improve to handle different extensions
		String pathWithCFC = path.concat("." + (dialect == CFMLEngine.DIALECT_CFML ? Constants.getCFMLComponentExtension() : Constants.getLuceeComponentExtension()));

		// no cache for per application pathes
		Mapping[] acm = pc.getApplicationContext().getComponentMappings();
		if (!ArrayUtil.isEmpty(acm)) {
			Mapping m;
			for (int y = 0; y < acm.length; y++) {
				m = acm[y];
				ps = m.getPageSource(pathWithCFC);
				page = toCIPage(ps.loadPageThrowTemplateException(pc, false, (Page) null));
				if (page != null) {

					return returnType == RETURN_TYPE_PAGE ? page : load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
				}
			}
		}

		if (searchLocal == null) searchLocal = Caster.toBoolean(rawPath.indexOf('.') == -1 ? true : config.getComponentLocalSearch());
		if (searchRoot == null) searchRoot = Caster.toBoolean(config.getComponentRootSearch());

		// CACHE
		// check local in cache
		String localCacheName = null;
		if (searchLocal && isRealPath && currPS != null) {
			localCacheName = currPS.getDisplayPath().replace('\\', '/');
			localCacheName = localCacheName.substring(0, localCacheName.lastIndexOf('/') + 1).concat(pathWithCFC);
			if (doCache) {
				page = config.getCachedPage(pc, localCacheName);
				if (page != null)
					return returnType == RETURN_TYPE_PAGE ? page : load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
			}
		}

		// check import cache
		if (doCache && isRealPath) {
			ImportDefintion impDef = config.getComponentDefaultImport();
			ImportDefintion[] impDefs = importDefintions == null ? EMPTY_ID : importDefintions;
			int i = -1;
			do {

				if (impDef.isWildcard() || impDef.getName().equalsIgnoreCase(path)) {
					page = config.getCachedPage(pc, "import:" + impDef.getPackageAsPath() + pathWithCFC);
					if (page != null) return returnType == RETURN_TYPE_PAGE ? page
							: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
				}
				impDef = ++i < impDefs.length ? impDefs[i] : null;
			}
			while (impDef != null);
		}

		if (doCache) {
			// check global in cache
			page = config.getCachedPage(pc, pathWithCFC);
			if (page != null)
				return returnType == RETURN_TYPE_PAGE ? page : load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
		}

		// SEARCH
		// search from local
		if (searchLocal && isRealPath) {
			// check realpath
			PageSource[] arr = ((PageContextImpl) pc).getRelativePageSources(pathWithCFC);
			page = toCIPage(PageSourceImpl.loadPage(pc, arr, null));
			if (page != null) {
				if (doCache) config.putCachedPageSource(localCacheName, page.getPageSource());
				return returnType == RETURN_TYPE_PAGE ? page : load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
			}
		}

		// search with imports
		ApplicationContext ac = pc.getApplicationContext();
		// Mapping[] ccMappings = config.getComponentMappings();
		// Mapping[] acMappings = ac != null ? ac.getComponentMappings() : null;
		Mapping[][] compMappings = new Mapping[][] { (ac != null ? ac.getComponentMappings() : null), config.getComponentMappings() };

		if (isRealPath) {
			ImportDefintion impDef = config.getComponentDefaultImport();
			ImportDefintion[] impDefs = importDefintions == null ? EMPTY_ID : importDefintions;
			PageSource[] arr;

			int i = -1;
			do {

				if (impDef.isWildcard() || impDef.getName().equalsIgnoreCase(path)) {

					// search from local first
					if (searchLocal) {
						arr = ((PageContextImpl) pc).getRelativePageSources(impDef.getPackageAsPath() + pathWithCFC);
						page = toCIPage(PageSourceImpl.loadPage(pc, arr, null));
						if (page != null) {
							if (doCache) config.putCachedPageSource("import:" + impDef.getPackageAsPath() + pathWithCFC, page.getPageSource());
							return returnType == RETURN_TYPE_PAGE ? page
									: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
						}
					}

					// search mappings and webroot
					page = toCIPage(PageSourceImpl.loadPage(pc, ((PageContextImpl) pc).getPageSources("/" + impDef.getPackageAsPath() + pathWithCFC), null));
					if (page != null) {
						String key = impDef.getPackageAsPath() + pathWithCFC;
						if (doCache && !((MappingImpl) page.getPageSource().getMapping()).isAppMapping()) config.putCachedPageSource("import:" + key, page.getPageSource());
						return returnType == RETURN_TYPE_PAGE ? page
								: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
					}

					// search application component mappings
					for (int z = 0; z < compMappings.length; z++) {
						Mapping[] mappings = compMappings[z];
						if (mappings != null) {
							Mapping m;
							for (int y = 0; y < mappings.length; y++) {
								m = mappings[y];
								ps = m.getPageSource(impDef.getPackageAsPath() + pathWithCFC);
								page = toCIPage(ps.loadPageThrowTemplateException(pc, false, (Page) null));
								if (page != null) {
									if (doCache && z > 0) config.putCachedPageSource("import:" + impDef.getPackageAsPath() + pathWithCFC, page.getPageSource());
									return returnType == RETURN_TYPE_PAGE ? page
											: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
								}
							}
						}
					}

				}
				impDef = ++i < impDefs.length ? impDefs[i] : null;
			}
			while (impDef != null);

		}

		String p;
		if (isRealPath) p = '/' + pathWithCFC;
		else p = pathWithCFC;

		// search mappings and webroot
		page = toCIPage(PageSourceImpl.loadPage(pc, ((PageContextImpl) pc).getPageSources(p), null));
		if (page != null) {
			String key = pathWithCFC;
			if (doCache && !((MappingImpl) page.getPageSource().getMapping()).isAppMapping()) config.putCachedPageSource(key, page.getPageSource());
			return returnType == RETURN_TYPE_PAGE ? page : load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
		}

		// search component mappings
		for (int y = 0; y < compMappings.length; y++) {
			Mapping[] mappings = compMappings[y];
			if (mappings != null) {
				Mapping m;
				for (int i = 0; i < mappings.length; i++) {
					m = mappings[i];
					ps = m.getPageSource(p);
					page = toCIPage(ps.loadPageThrowTemplateException(pc, false, (Page) null));

					// recursive search
					if (page == null && config.doComponentDeepSearch() && path.indexOf('/') == -1) {
						ps = MappingUtil.searchMappingRecursive(m, pathWithCFC, true);
						if (ps != null) {
							page = toCIPage(ps.loadPageThrowTemplateException(pc, false, (Page) null));
							if (page != null) doCache = false;// do not cache this, it could be ambigous
						}
					}

					if (page != null) {
						if (doCache && y > 0) config.putCachedPageSource(pathWithCFC, page.getPageSource());
						return returnType == RETURN_TYPE_PAGE ? page
								: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
					}
				}
			}
		}

		// search relative to active component (this get not cached because the cache get ambigous if we do)
		if (searchLocal && isRealPath) {
			if (loadingLocation == null) {
				Component c = pc.getActiveComponent();
				if (c != null) loadingLocation = c.getPageSource();
			}

			if (loadingLocation != null) {
				ps = loadingLocation.getRealPage(pathWithCFC);
				if (ps != null) {
					page = toCIPage(ps.loadPageThrowTemplateException(pc, false, (Page) null));

					if (page != null) {
						return returnType == RETURN_TYPE_PAGE ? page
								: load(pc, page, trim(path.replace('/', '.')), sub, isRealPath, returnType, isExtendedComponent, executeConstr);
					}
				}
			}
		}
		// translate cfide. to org.lucee.cfml
		if (StringUtil.startsWithIgnoreCase(rawPath, "cfide.")) {
			String rpm = Constants.DEFAULT_PACKAGE + "." + rawPath.substring(6);
			try {
				return _search(pc, loadingLocation, rpm, searchLocal, searchRoot, executeConstr, returnType, currPS, importDefintions, dialect, false);
			}
			catch (ExpressionException ee) {
				return null;
				// throw new ExpressionException("invalid "+toStringType(returnType)+" definition, can't find
				// "+rawPath+" or "+rpm);
			}
		}
		return null;
		// throw new ExpressionException("invalid "+toStringType(returnType)+" definition, can't find
		// "+toStringType(returnType)+" ["+rawPath+"]");
	}

	private static String toStringType(short returnType, int dialect) {
		if (RETURN_TYPE_COMPONENT == returnType) return dialect == CFMLEngine.DIALECT_LUCEE ? "class" : "component";
		if (RETURN_TYPE_INTERFACE == returnType) return "interface";
		return "component/interface";
	}

	private static String trim(String str) {
		if (StringUtil.startsWith(str, '.')) str = str.substring(1);
		return str;
	}

	public static ComponentImpl loadComponent(PageContext pc, PageSource ps, String callPath, boolean isRealPath, boolean silent) throws PageException {
		return _loadComponent(pc, toCIPage(ps.loadPage(pc, false), callPath), callPath, isRealPath, false, true);
	}

	public static ComponentImpl loadComponent(PageContext pc, PageSource ps, String callPath, boolean isRealPath, boolean silent, boolean executeConstr) throws PageException {
		return _loadComponent(pc, toCIPage(ps.loadPage(pc, false), callPath), callPath, isRealPath, false, executeConstr);
	}

	// do not change, method is used in flex extension
	public static ComponentImpl loadComponent(PageContext pc, Page page, String callPath, boolean isRealPath, boolean silent, boolean isExtendedComponent, boolean executeConstr)
			throws PageException {
		CIPage cip = toCIPage(page, callPath);
		if (silent) {
			// TODO is there a more direct way
			BodyContent bc = pc.pushBody();
			try {
				return _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr);
			}
			finally {
				BodyContentUtil.clearAndPop(pc, bc);
			}
		}
		return _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr);
	}

	private static CIObject load(PageContext pc, Page page, String callPath, String sub, boolean isRealPath, short returnType, final boolean isExtendedComponent,
			boolean executeConstr) throws PageException {
		CIPage cip = toCIPage(page, callPath);
		if (sub != null) {
			cip = loadSub(cip, sub);
			// page=page.loadSub(sub);
		}
		if (cip instanceof ComponentPageImpl) {
			if (returnType != RETURN_TYPE_COMPONENT)
				throw new ApplicationException("the component [" + cip.getPageSource().getComponentName() + "] cannot be used as an interface.");

			return _loadComponent(pc, cip, callPath, isRealPath, isExtendedComponent, executeConstr);
		}

		if (returnType != RETURN_TYPE_INTERFACE) throw new ApplicationException("the interface [" + cip.getPageSource().getComponentName() + "] cannot be used as a component.");

		return loadInterface(pc, cip, cip.getPageSource(), callPath, isRealPath);
	}

	private static CIPage loadSub(CIPage page, String sub) throws ApplicationException {
		print.e("class:" + page.getClass().getName());
		print.e("class:" + page.getPageSource().getClassName());
		print.e("sub:" + sub);
		// TODO find a better way to create that class name
		String subClassName = lucee.transformer.bytecode.Page.createSubClass(page.getPageSource().getClassName(), sub, page.getPageSource().getDialect());

		// subClassName:sub.test_cfc$cf$1$sub1
		// - sub.test_cfc$sub1$cf

		print.e("subClassName:" + subClassName);
		CIPage[] subs = page.getSubPages();
		for (int i = 0; i < subs.length; i++) {
			print.e("- " + subs[i].getClass().getName());

			// subClassName:sub.test_cfc$cf$1$sub1
			// - sub.test_cfc$sub1$cf

			if (subs[i].getClass().getName().equals(subClassName)) return subs[i];
		}
		throw new ApplicationException("There is no Sub component [" + sub + "] in [" + page.getPageSource().getDisplayPath() + "]");

	}

	public static Page loadPage(PageContext pc, PageSource ps, boolean forceReload) throws PageException {
		if (pc.getConfig().debug() && ((ConfigPro) pc.getConfig()).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
			DebugEntryTemplate debugEntry = pc.getDebugger().getEntry(pc, ps);
			pc.addPageSource(ps, true);

			long currTime = pc.getExecutionTime();
			long exeTime = 0;
			long time = System.currentTimeMillis();
			try {
				debugEntry.updateFileLoadTime((int) (System.currentTimeMillis() - time));
				exeTime = System.currentTimeMillis();
				return ps.loadPage(pc, forceReload);
			}
			finally {
				long diff = ((System.currentTimeMillis() - exeTime) - (pc.getExecutionTime() - currTime));
				pc.setExecutionTime(pc.getExecutionTime() + (System.currentTimeMillis() - time));
				debugEntry.updateExeTime(diff);
				pc.removeLastPageSource(true);
			}
		}
		// no debug
		pc.addPageSource(ps, true);
		try {
			return ps.loadPage(pc, forceReload);
		}
		finally {
			pc.removeLastPageSource(true);
		}
	}

	private static ComponentImpl _loadComponent(PageContext pc, CIPage page, String callPath, boolean isRealPath, final boolean isExtendedComponent, boolean executeConstr)
			throws PageException {
		ComponentImpl rtn = null;
		if (pc.getConfig().debug() && ((ConfigPro) pc.getConfig()).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
			DebugEntryTemplate debugEntry = pc.getDebugger().getEntry(pc, page.getPageSource());
			pc.addPageSource(page.getPageSource(), true);

			long currTime = pc.getExecutionTime();
			long exeTime = 0;
			long time = System.nanoTime();
			try {
				debugEntry.updateFileLoadTime((int) (System.nanoTime() - time));
				exeTime = System.nanoTime();
				rtn = initComponent(pc, page, callPath, isRealPath, isExtendedComponent, executeConstr);

			}
			finally {
				if (rtn != null) rtn.setLoaded(true);
				long diff = ((System.nanoTime() - exeTime) - (pc.getExecutionTime() - currTime));
				pc.setExecutionTime(pc.getExecutionTime() + (System.nanoTime() - time));
				debugEntry.updateExeTime(diff);
				pc.removeLastPageSource(true);
			}
		}
		// no debug
		else {
			pc.addPageSource(page.getPageSource(), true);
			try {
				rtn = initComponent(pc, page, callPath, isRealPath, isExtendedComponent, executeConstr);
			}
			finally {
				if (rtn != null) rtn.setLoaded(true);
				pc.removeLastPageSource(true);
			}
		}

		return rtn;
	}

	public static InterfaceImpl loadInterface(PageContext pc, Page page, PageSource ps, String callPath, boolean isRealPath) throws PageException {
		InterfaceImpl rtn = null;
		if (pc.getConfig().debug() && ((ConfigPro) pc.getConfig()).hasDebugOptions(ConfigPro.DEBUG_TEMPLATE)) {
			DebugEntryTemplate debugEntry = pc.getDebugger().getEntry(pc, ps);
			pc.addPageSource(ps, true);

			long currTime = pc.getExecutionTime();
			long exeTime = 0;
			long time = System.nanoTime();
			try {
				debugEntry.updateFileLoadTime((int) (System.nanoTime() - time));
				exeTime = System.nanoTime();
				if (page == null) page = ps.loadPage(pc, false);
				rtn = initInterface(pc, page, callPath, isRealPath);
			}
			finally {
				long diff = ((System.nanoTime() - exeTime) - (pc.getExecutionTime() - currTime));
				pc.setExecutionTime(pc.getExecutionTime() + (System.nanoTime() - time));
				debugEntry.updateExeTime(diff);
				pc.removeLastPageSource(true);
			}
		}
		// no debug
		else {
			pc.addPageSource(ps, true);
			try {
				if (page == null) page = ps.loadPage(pc, false);
				rtn = initInterface(pc, page, callPath, isRealPath);
			}
			finally {
				pc.removeLastPageSource(true);
			}
		}
		return rtn;
	}

	private static InterfaceImpl initInterface(PageContext pc, Page page, String callPath, boolean isRealPath) throws PageException {
		if (!(page instanceof InterfacePageImpl)) throw new ApplicationException("invalid interface definition [" + callPath + "]");
		InterfacePageImpl ip = (InterfacePageImpl) page;
		InterfaceImpl i = ip.newInstance(pc, callPath, isRealPath);
		return i;
	}

	private static ComponentImpl initComponent(PageContext pc, CIPage page, String callPath, boolean isRealPath, final boolean isExtendedComponent, boolean executeConstr)
			throws PageException {
		// is not a component, then it has to be an interface
		if (!(page instanceof ComponentPageImpl)) throw new ApplicationException("you cannot instantiate the interface [" + page.getPageSource().getDisplayPath()
				+ "] as a component (" + page.getClass().getName() + "" + (page instanceof InterfacePageImpl) + ")");

		ComponentPageImpl cp = (ComponentPageImpl) page;
		ComponentImpl c = cp.newInstance(pc, callPath, isRealPath, isExtendedComponent, executeConstr);

		// abstract/final check
		if (!isExtendedComponent) {
			if (c.getModifier() == Component.MODIFIER_ABSTRACT) throw new ApplicationException(
					"you cannot instantiate an abstract component [" + page.getPageSource().getDisplayPath() + "], this component can only be extended by other components");
		}
		else if (c.getModifier() == Component.MODIFIER_FINAL) throw new ApplicationException("you cannot extend a final component [" + page.getPageSource().getDisplayPath() + "]");

		c.setInitalized(true);
		return c;

	}

	private static CIPage toCIPage(Page p, String callPath) throws PageException {
		if (p instanceof CIPage) return (CIPage) p;

		if (p != null) throw new ApplicationException("invalid component definition [" + callPath + "] in template [" + p.getPageSource().getDisplayPath() + "]");

		throw new ApplicationException("invalid component definition [" + callPath + "] ");
	}

	private static CIPage toCIPage(Page p) {
		if (p instanceof CIPage) return (CIPage) p;
		return null;
	}
}