package lucee.runtime.config;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.res.Resource;
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
import lucee.runtime.debug.DebuggerPool;
import lucee.runtime.exp.PageException;
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

	public TagHandlerPool getTagHandlerPool() {
		return tagHandlerPool;
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
}
