package lucee.runtime.config;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lock.KeyLock;
import lucee.commons.lock.KeyLockImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CIPage;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.cache.tag.CacheHandlerCollection;
import lucee.runtime.cache.tag.CacheHandlerCollections;
import lucee.runtime.compiler.CFMLCompilerImpl;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.debug.DebuggerPool;
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

public class ConfigWebHelper {

	private final ConfigServerImpl cs;
	private ConfigWebPro cw;
	private final TagHandlerPool tagHandlerPool;
	private DebuggerPool debuggerPool;
	private KeyLock<String> contextLock = new KeyLockImpl<String>();
	private CacheHandlerCollections cacheHandlerCollections;
	private Map<String, SoftReference<Mapping>> applicationMappings = new ConcurrentHashMap<String, SoftReference<Mapping>>();
	private CIPage baseComponentPageCFML;
	private CIPage baseComponentPageLucee;
	private final CFMLCompilerImpl compiler = new CFMLCompilerImpl();
	private WSHandler wsHandler;
	private GatewayEngineImpl gatewayEngine;
	private Map<String, Mapping> serverTagMappings;
	private Map<String, Mapping> serverFunctionMappings;
	private SearchEngine searchEngine;
	private static final LockManager lockManager = LockManagerImpl.getInstance(false);
	private AMFEngine amfEngine;
	protected IdentificationWeb id;

	public ConfigWebHelper(ConfigServerImpl cs, ConfigWebPro cw) {
		this.cs = cs;
		this.cw = cw;
		tagHandlerPool = new TagHandlerPool(cw);

	}

	public short getPasswordSource() {
		return cs.hasCustomDefaultPassword() ? ConfigWebImpl.PASSWORD_ORIGIN_DEFAULT : ConfigWebImpl.PASSWORD_ORIGIN_SERVER;
	}

	public boolean hasIndividualSecurityManager(ConfigWebPro cwp) {
		return cs.hasIndividualSecurityManager(cwp.getIdentification().getId());
	}

	public void reset() {
		tagHandlerPool.reset();
		contextLock = new KeyLockImpl<String>();
		baseComponentPageCFML = null;
		baseComponentPageLucee = null;
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
		if (searchEngine == null) {
			try {
				Object o = ClassUtil.loadInstance(cw.getSearchEngineClassDefinition().getClazz());
				if (o instanceof SearchEngine) searchEngine = (SearchEngine) o;
				else throw new ApplicationException("class [" + o.getClass().getName() + "] does not implement the interface SearchEngine");

				searchEngine.init(cw, ConfigWebUtil.getFile(cw.getConfigDir(), ConfigWebUtil.translateOldPath(cw.getSearchEngineDirectory()), "search", cw.getConfigDir(),
						FileUtil.TYPE_DIR, cw));
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

	public Collection<Mapping> getServerTagMappings() {
		if (serverTagMappings == null) {
			Iterator<Entry<String, Mapping>> it = cs.tagMappings.entrySet().iterator();// .cloneReadOnly(this);
			Entry<String, Mapping> e;
			serverTagMappings = new ConcurrentHashMap<String, Mapping>();
			while (it.hasNext()) {
				e = it.next();
				serverTagMappings.put(e.getKey(), ((MappingImpl) e.getValue()).cloneReadOnly(cw));
			}
		}
		return serverTagMappings.values();
	}

	public Mapping getServerTagMapping(String mappingName) {
		getServerTagMappings(); // necessary to make sure it exists
		return serverTagMappings.get(mappingName);
	}

	public Collection<Mapping> getServerFunctionMappings() {
		if (serverFunctionMappings == null) {
			Iterator<Entry<String, Mapping>> it = cs.functionMappings.entrySet().iterator();
			Entry<String, Mapping> e;
			serverFunctionMappings = new ConcurrentHashMap<String, Mapping>();
			while (it.hasNext()) {
				e = it.next();
				serverFunctionMappings.put(e.getKey(), ((MappingImpl) e.getValue()).cloneReadOnly(cw));
			}
		}
		return serverFunctionMappings.values();
	}

	public Mapping getServerFunctionMapping(String mappingName) {
		getServerFunctionMappings();// call this to make sure it exists
		return serverFunctionMappings.get(mappingName);
	}

	public void resetServerFunctionMappings() {
		serverFunctionMappings = null;
	}

	public GatewayEngineImpl getGatewayEngineImpl() {
		if (gatewayEngine == null) {
			gatewayEngine = new GatewayEngineImpl(cw);
		}
		return gatewayEngine;
	}

	public WSHandler getWSHandler() throws PageException {
		if (wsHandler == null) {
			ClassDefinition cd = cw instanceof ConfigImpl ? ((ConfigImpl) cw).getWSHandlerClassDefinition() : null;
			if (isEmpty(cd)) cd = cs.getWSHandlerClassDefinition();
			try {
				if (isEmpty(cd)) return new DummyWSHandler();
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

	public CIPage getBaseComponentPage(int dialect, PageContext pc) throws PageException {
		// CFML
		if (dialect == CFMLEngine.DIALECT_CFML) {
			if (baseComponentPageCFML == null) {
				baseComponentPageCFML = (CIPage) cw.getBaseComponentPageSource(dialect, pc).loadPage(pc, false);
			}
			return baseComponentPageCFML;
		}
		// Lucee
		if (baseComponentPageLucee == null) {
			baseComponentPageLucee = (CIPage) cw.getBaseComponentPageSource(dialect, pc).loadPage(pc, false);
		}
		return baseComponentPageLucee;
	}

	public void resetBaseComponentPage() {
		baseComponentPageCFML = null;
		baseComponentPageLucee = null;
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
			m = new MappingImpl(cw, virtual, physical, archive, Config.INSPECT_UNDEFINED, physicalFirst, false, false, false, true, ignoreVirtual, null, -1, -1,
					checkPhysicalFromWebroot, checkArchiveFromWebroot);
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

	public CFMLCompilerImpl getCompiler() {
		return compiler;
	}

	boolean isEmpty(ClassDefinition cd) {
		return cd == null || StringUtil.isEmpty(cd.getClassName());
	}
}
