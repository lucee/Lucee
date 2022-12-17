package lucee.runtime.functions.system;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.type.scope.ScopeContext;
import lucee.runtime.type.scope.ApplicationImpl;

public final class InspectTemplates extends BIF implements Function {

	private static final long serialVersionUID = -2777306151061026079L;

	public static boolean call(PageContext pc) throws PageException{
		reset(pc, null);
		return true;
	}

	public static void reset(PageContext pc, Config c) throws PageException {
		ConfigWebPro config;
		ConfigWebPro web;
		CFMLFactoryImpl factory;

		pc = ThreadLocalPageContext.get(pc);
		if (c == null) config = (ConfigWebPro) ThreadLocalPageContext.getConfig(pc);
		else config = (ConfigWebPro) c;

		ConfigServer cs = config.getConfigServer("server");
		ConfigWeb[] webs = cs.getConfigWebs();
		CFMLEngineFactory.getInstance();
		CFMLEngineImpl engine = (CFMLEngineImpl) cs.getCFMLEngine();

		// reset Applications for each Web Context
		for (int i = 0; i < webs.length; i++) {
			web = (ConfigWebPro) webs[i];
			factory = (CFMLFactoryImpl) web.getFactory();
			ScopeContext sc = factory.getScopeContext();
			reset(web, sc);
		}

		// reset server config
		reset(config, config.getMappings());
		reset(config, config.getCustomTagMappings());
		reset(config, config.getComponentMappings());
		reset(config, config.getFunctionMappings());
		reset(config, config.getServerFunctionMappings());
		reset(config, config.getTagMappings());
		reset(config, config.getServerTagMappings());
	}

	public static void reset(Config config, ScopeContext sc){
		ApplicationContext ac;
		Struct all = sc.getAllApplicationScopes();
		Iterator<Entry<Key,Object>> it = all.entryIterator();
		Entry<Key, Object> e;
		// reset each Application in this Web Context
		while (it.hasNext()) {
			e = it.next();
			ac = (ApplicationContext) e.getValue();
			if (ac != null) {
				reset(config, ac.getMappings());
				reset(config, ac.getComponentMappings());
				reset(config, ac.getCustomTagMappings());
			}
		}
		// reset Web Context
		reset(config, config.getMappings());
		reset(config, config.getCustomTagMappings());
		reset(config, config.getComponentMappings());
		//reset(config, config.getFunctionMappings());
		//reset(config, config.getTagMappings());
	}

	public static void reset(Config config, Collection<Mapping> mappings) {
		if (mappings == null) return;
		Iterator<Mapping> it = mappings.iterator();
		while (it.hasNext()) {
			reset(config, it.next());
		}
	}

	public static void reset(Config config, Mapping[] mappings) {
		if (mappings == null) return;
		for (int i = 0; i < mappings.length; i++) {
			reset(config, mappings[i]);
		}
	}

	public static void reset(Config config, Mapping mapping) {
		if (mapping == null) return;
		(((MappingImpl) mapping)).resetPages(null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "InspectTemplates", 0, 0, args.length);
	}
}