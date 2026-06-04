package lucee.runtime.config;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lucee.commons.digest.HashUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lock.KeyLock;
import lucee.commons.lock.KeyLockImpl;
import lucee.runtime.CIPage;
import lucee.runtime.ComponentImpl;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cache.tag.CacheHandlerCollections;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.config.gateway.GatewayMap;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.gateway.GatewayEngineImpl;
import lucee.runtime.lock.LockManager;
import lucee.runtime.lock.LockManagerImpl;
import lucee.runtime.net.amf.AMFEngine;
import lucee.runtime.net.amf.AMFEngineDummy;
import lucee.runtime.net.rpc.DummyWSHandler;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.net.rpc.ref.WSHandlerReflector;
import lucee.runtime.op.Caster;
import lucee.runtime.search.SearchEngine;
import lucee.runtime.tag.TagHandlerPool;
import lucee.runtime.writer.CFMLWriter;
import lucee.runtime.writer.CFMLWriterImpl;
import lucee.runtime.writer.CFMLWriterWS;
import lucee.runtime.writer.CFMLWriterWSPref;

public final class ConfigWebHelper {

	private final ConfigServerImpl cs;
	private ConfigWebPro cw;
	private final TagHandlerPool tagHandlerPool;
	private DebuggerPool debuggerPool;
	private KeyLock<String> contextLock = new KeyLockImpl<String>();
	private CacheHandlerCollections cacheHandlerCollections;
	private Map<String, SoftReference<Mapping>> applicationMappings = new ConcurrentHashMap<String, SoftReference<Mapping>>();
	// Resolution cache for Application.cfc mapping paths. Skips source.exists() syscall on hits.
	// Engages for NEVER and AUTO modes. Invalidated by: inspectTemplates() (full clear),
	// application start (per-app clear), AUTO ticker (negative entries re-validated),
	// admin inspect-mode change (full clear via ConfigWebImpl.resetInspectTemplate override).
	private final Map<String, ResolvedMapping> resolvedMappingPaths = new ConcurrentHashMap<String, ResolvedMapping>();
	// Transition-log state — survives cache invalidation so we only log when matched flag flips.
	private final Map<String, Boolean> lastLoggedMatched = new ConcurrentHashMap<String, Boolean>();
	private CIPage baseComponentPageCFML;
	private ComponentImpl baseComponenInstanceExeConstr;
	private ComponentImpl baseComponenInstanceNonExeConstr;
	private final CFMLCompilerImpl compiler = new CFMLCompilerImpl();
	private WSHandler wsHandler;
	private GatewayEngineImpl gatewayEngine;
	private SearchEngine searchEngine;
	private ClassDefinition<SearchEngine> searchEngineCD;
	private static final LockManager lockManager = LockManagerImpl.getInstance(false);
	private AMFEngine amfEngine;
	protected IdentificationWeb id;

	public ConfigWebHelper(ConfigServerImpl cs, ConfigWebPro cw) {
		this.cs = cs;
		this.cw = cw;
		tagHandlerPool = new TagHandlerPool(cw);

	}

	public boolean hasIndividualSecurityManager(ConfigWebPro cwp) {
		return cs.hasIndividualSecurityManager(cwp.getIdentification().getId());
	}

	public void reset() {
		tagHandlerPool.reset();
		contextLock = new KeyLockImpl<String>();
		baseComponentPageCFML = null;
	}

	public void setIdentification(IdentificationWeb id) {
		this.id = id;
	}

	public IdentificationWeb getIdentification() {
		return id;
	}

	public void setAMFEngine(AMFEngine engine) {
		amfEngine = engine;
	}

	public AMFEngine getAMFEngine() {
		if (amfEngine == null) return AMFEngineDummy.getInstance();
		return amfEngine;
	}

	public String getLabel() {
		String hash = cw.getHash();
		String label = hash;
		Map<String, String> labels = cs.getLabels();
		if (labels != null) {
			String l = labels.get(hash);
			if (!StringUtil.isEmpty(l)) {
				label = l;
			}
		}
		return label;
	}

	public LockManager getLockManager() {
		return lockManager;
	}

	public SearchEngine getSearchEngine(PageContext pc) throws PageException {
		if (searchEngine == null || searchEngineCD != cw.getSearchEngineClassDefinition()) {
			searchEngineCD = cw.getSearchEngineClassDefinition();
			try {
				Object o = ClassUtil.loadInstance(cw.getSearchEngineClassDefinition().getClazz());
				if (o instanceof SearchEngine) searchEngine = (SearchEngine) o;
				else throw new ApplicationException("class [" + o.getClass().getName() + "] does not implement the interface SearchEngine");

				searchEngine.init(cw, ConfigUtil.getFile(cw.getConfigDir(), ConfigUtil.translateOldPath(cw.getSearchEngineDirectory()), "search", cw.getConfigDir(),
						FileUtil.TYPE_DIR, ResourceUtil.LEVEL_GRAND_PARENT_FILE, cw));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return searchEngine;
	}

	public TagHandlerPool getTagHandlerPool() {
		return tagHandlerPool;
	}

	public GatewayEngineImpl getGatewayEngineImpl(GatewayMap entries) throws PageException {
		// already here
		if (gatewayEngine != null && ThreadLocalPageContext.insideGateway()) return gatewayEngine;

		try {
			ThreadLocalPageContext.insideGateway(true);
			// new engine
			if (gatewayEngine == null) {
				gatewayEngine = new GatewayEngineImpl(cw);
				if (entries != null) {
					try {
						gatewayEngine.addEntries(cw, entries);
					}
					catch (Exception e) {
						throw Caster.toPageException(e);
					}
				}
			}
			// update engine
			else if (entries != null && !entries.getId().equals(gatewayEngine.id())) {
				try {
					gatewayEngine.addEntries(cw, entries);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
		}
		finally {
			ThreadLocalPageContext.insideGateway(false);
		}
		return gatewayEngine;
	}

	public WSHandler getWSHandler() throws PageException {
		if (wsHandler == null) {
			ClassDefinition cd = cw instanceof ConfigImpl ? ((ConfigImpl) cw).getWSHandlerClassDefinition() : null;
			if (isEmpty(cd)) cd = cs.getWSHandlerClassDefinition();
			try {
				if (isEmpty(cd)) {
				wsHandler = new DummyWSHandler();
				return wsHandler;
			}
				Object obj = ClassUtil.newInstance(cd.getClazz());
				if (obj instanceof WSHandler) wsHandler = (WSHandler) obj;
				else wsHandler = new WSHandlerReflector(obj);
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
		}
		return wsHandler;
	}

	public CFMLWriter getCFMLWriter(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) {
		if (cw.getCFMLWriterType() == ConfigPro.CFML_WRITER_WS) return new CFMLWriterWS(pc, req, rsp, -1, false, cw.closeConnection(), cw.isShowVersion(), cw.contentLength());
		else if (cw.getCFMLWriterType() == ConfigPro.CFML_WRITER_REFULAR)
			return new CFMLWriterImpl(pc, req, rsp, -1, false, cw.closeConnection(), cw.isShowVersion(), cw.contentLength());
		else return new CFMLWriterWSPref(pc, req, rsp, -1, false, cw.closeConnection(), cw.isShowVersion(), cw.contentLength());

	}

	public DebuggerPool getDebuggerPool() {
		if (debuggerPool == null) {
			Resource dir = cw.getConfigDir().getRealResource("debugger");
			dir.mkdirs();
			debuggerPool = new DebuggerPool(dir);
		}
		return debuggerPool;
	}

	public KeyLock<String> getContextLock() {
		return contextLock;
	}

	public CacheHandlerCollection getCacheHandlerCollection(int type, CacheHandlerCollection defaultValue) {
		if (cacheHandlerCollections == null) cacheHandlerCollections = new CacheHandlerCollections(cw);
		switch (type) {
		case Config.CACHE_TYPE_FILE:
			return cacheHandlerCollections.file;
		case Config.CACHE_TYPE_FUNCTION:
			return cacheHandlerCollections.function;
		case Config.CACHE_TYPE_HTTP:
			return cacheHandlerCollections.http;
		case Config.CACHE_TYPE_INCLUDE:
			return cacheHandlerCollections.include;
		case Config.CACHE_TYPE_QUERY:
			return cacheHandlerCollections.query;
		case Config.CACHE_TYPE_RESOURCE:
			return cacheHandlerCollections.resource;
		case Config.CACHE_TYPE_WEBSERVICE:
			return cacheHandlerCollections.webservice;
		// case Config.CACHE_TYPE_OBJECT: return cacheHandlerCollections.object;
		// case Config.CACHE_TYPE_TEMPLATE: return cacheHandlerCollections.template;
		}

		return defaultValue;
	}

	public void releaseCacheHandlers(PageContext pc) {
		if (cacheHandlerCollections == null) return;
		cacheHandlerCollections.releaseCacheHandlers(pc);
	}

	public CIPage getBaseComponentPage(PageContext pc) {

		CIPage base = baseComponentPageCFML;
		if (base == null) {
			try {
				PageSource ps = cw.getBaseComponentPageSource(pc, false);
				if (ps == null) return null;
				base = (CIPage) ps.loadPage(pc, false);
			}
			catch (PageException pe) {
				PageSource ps = cw.getBaseComponentPageSource(pc, true);
				if (ps == null) return null;
				try {
					base = (CIPage) ps.loadPage(pc, false);
				}
				catch (PageException e) {
					LogUtil.log("component", e);
				}
			}
			if (base != null) {
				baseComponentPageCFML = base;
			}
		}
		return base;
	}

	public ComponentImpl getBaseComponentInstance(PageContext pc, ComponentPageImpl exclude, boolean executeConstr) throws PageException {
		ComponentImpl base = executeConstr ? baseComponenInstanceExeConstr : baseComponenInstanceNonExeConstr;
		CIPage p = getBaseComponentPage(pc);
		if (base == null) {
			if (p != null) {
				if (exclude.getPageSource().equals(p.getPageSource())) return null;

				base = ComponentLoader.loadComponent(pc, p, "Component", false, false, true, executeConstr);
				if (executeConstr) baseComponenInstanceExeConstr = base;
				else baseComponenInstanceNonExeConstr = base;
			}
		}
		if (base == null || exclude.getPageSource().equals(p.getPageSource())) return null;
		return base._duplicate(false, false);
	}

	public void resetBaseComponentPage() {
		baseComponentPageCFML = null;
		baseComponenInstanceExeConstr = null;
		baseComponenInstanceNonExeConstr = null;
	}

	public Mapping[] getApplicationMappings() {
		List<Mapping> list = new ArrayList<>();
		Iterator<SoftReference<Mapping>> it = applicationMappings.values().iterator();
		SoftReference<Mapping> sr;
		while (it.hasNext()) {
			sr = it.next();
			if (sr != null) list.add(sr.get());
		}
		return list.toArray(new Mapping[list.size()]);
	}

	public Mapping getApplicationMapping(String type, String virtual, String physical, String archive, boolean physicalFirst, boolean ignoreVirtual,
			boolean checkPhysicalFromWebroot, boolean checkArchiveFromWebroot) {
		String key = type + ":" + virtual.toLowerCase() + ":" + (physical == null ? "" : physical.toLowerCase()) + ":" + (archive == null ? "" : archive.toLowerCase()) + ":"
				+ physicalFirst;
		key = Long.toString(HashUtil.create64BitHash(key), Character.MAX_RADIX);

		SoftReference<Mapping> t = applicationMappings.get(key);
		Mapping m = t == null ? null : t.get();

		if (m == null) {
			m = new MappingImpl(cw, virtual, physical, archive, Config.INSPECT_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, ConfigPro.INSPECT_INTERVAL_UNDEFINED, physicalFirst,
					false, false, false, true, ignoreVirtual, null, -1, -1, checkPhysicalFromWebroot, checkArchiveFromWebroot);
			applicationMappings.put(key, new SoftReference<Mapping>(m));
		}
		else m.check();

		return m;
	}

	public boolean isApplicationMapping(Mapping mapping) {
		Iterator<SoftReference<Mapping>> it = applicationMappings.values().iterator();
		SoftReference<Mapping> sr;
		while (it.hasNext()) {
			sr = it.next();
			if (sr != null && mapping.equals(sr.get())) return true;
		}
		return false;
	}

	public ResolvedMapping resolveApplicationMappingPath(Resource source, String rawPath) {
		String key = source.getAbsolutePath().toLowerCase() + "|" + rawPath;
		ResolvedMapping cached = resolvedMappingPaths.get(key);
		if (cached != null) return cached;

		Resource resolved = source.getParentResource().getRealResource(rawPath);
		boolean matched = resolved.exists();
		String resolvedPath = matched ? resolved.getAbsolutePath() : rawPath;
		ResolvedMapping rm = new ResolvedMapping(resolvedPath, matched, source, rawPath);
		resolvedMappingPaths.put(key, rm);
		logTransitionIfChanged(key, rawPath, matched, resolvedPath);
		return rm;
	}

	public void clearResolvedMappingPaths() {
		resolvedMappingPaths.clear();
	}

	// Walk negative entries and re-syscall. If a path now exists, update in place — the ticker
	// already paid the syscall, no point making the next request pay it too. Positive entries
	// are left alone (deletions surface as downstream 404s).
	public void revalidateNegativeMappingPaths() {
		for (Map.Entry<String, ResolvedMapping> entry: resolvedMappingPaths.entrySet()) {
			ResolvedMapping cached = entry.getValue();
			if (cached.matched) continue;
			Resource resolved = cached.source.getParentResource().getRealResource(cached.rawPath);
			if (resolved.exists()) {
				String abs = resolved.getAbsolutePath();
				ResolvedMapping fresh = new ResolvedMapping(abs, true, cached.source, cached.rawPath);
				resolvedMappingPaths.put(entry.getKey(), fresh);
				logTransitionIfChanged(entry.getKey(), cached.rawPath, true, abs);
			}
		}
	}

	private void logTransitionIfChanged(String key, String rawPath, boolean matched, String resolvedPath) {
		Boolean previous = lastLoggedMatched.put(key, matched);
		if (previous == null) {
			LogUtil.log(cw, Log.LEVEL_DEBUG, "mapping",
					"resolved [" + rawPath + "] -> " + (matched ? resolvedPath : "fallback (not found)"));
		}
		else if (previous != matched) {
			if (matched) LogUtil.log(cw, Log.LEVEL_INFO, "mapping", "now resolves [" + rawPath + "] -> " + resolvedPath);
			else LogUtil.log(cw, Log.LEVEL_WARN, "mapping", "no longer resolves [" + rawPath + "], falling back");
		}
	}

	public static final class ResolvedMapping {
		public final String path;
		public final boolean matched;
		public final Resource source;
		public final String rawPath;

		public ResolvedMapping(String path, boolean matched, Resource source, String rawPath) {
			this.path = path;
			this.matched = matched;
			this.source = source;
			this.rawPath = rawPath;
		}
	}

	public CFMLCompilerImpl getCompiler() {
		return compiler;
	}

	boolean isEmpty(ClassDefinition cd) {
		return cd == null || StringUtil.isEmpty(cd.getClassName());
	}
}
