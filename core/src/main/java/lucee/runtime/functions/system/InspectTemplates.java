package lucee.runtime.functions.system;

import java.util.Collection;
import java.util.Iterator;

import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSourcePool;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.listener.ApplicationContext;

public final class InspectTemplates extends BIF implements Function {

	private static final long serialVersionUID = -2777306151061026079L;

	public static boolean call(PageContext pc) {
		reset(pc, null);
		return true;
	}

	public static void reset(PageContext pc, Config c) {
		ConfigWebImpl config;
		pc = ThreadLocalPageContext.get(pc);
		if (c == null) config = (ConfigWebImpl) ThreadLocalPageContext.getConfig(pc);
		else config = (ConfigWebImpl) c;

		// application context
		if (pc != null) {
			ApplicationContext ac = pc.getApplicationContext();
			if (ac != null) {
				reset(config, ac.getMappings());
				reset(config, ac.getComponentMappings());
				reset(config, ac.getCustomTagMappings());
			}
		}

		// config
		reset(config, config.getMappings());
		reset(config, config.getCustomTagMappings());
		reset(config, config.getComponentMappings());
		reset(config, config.getFunctionMappings());
		reset(config, config.getServerFunctionMappings());
		reset(config, config.getTagMappings());
		reset(config, config.getServerTagMappings());
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
		PageSourcePool pool = ((MappingImpl) mapping).getPageSourcePool();
		pool.resetPages(null);

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "InspectTemplates", 0, 0, args.length);
	}
}