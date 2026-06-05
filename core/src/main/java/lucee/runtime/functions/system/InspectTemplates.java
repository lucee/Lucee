package lucee.runtime.functions.system;

import java.util.Collection;
import java.util.Iterator;

import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;

public final class InspectTemplates extends BIF implements Function {

	private static final long serialVersionUID = -2777306151061026079L;

	public static boolean call(PageContext pc) {
		reset(pc.getConfig());
		return true;
	}

	public static void reset(Config c) {
		if (c == null) c = ThreadLocalPageContext.getConfigServer();

		if (c instanceof ConfigServer) {
			for (ConfigWeb cw: ((ConfigServer) c).getConfigWebs()) {
				reset(cw);
			}
			return;
		}

		ConfigWebPro config = (ConfigWebPro) c;

		// clear cached Application.cfc mapping-path resolutions so the next request re-validates
		config.clearResolvedMappingPaths();

		// application
		reset(config, config.getApplicationMappings());

		// config
		reset(config, config.getMappings());
		reset(config, config.getCustomTagMappings());
		reset(config, config.getComponentMappings());
		reset(config, config.getFunctionMappings());
		reset(config, config.getTagMappings());
	}

	public static void reset(Config config, Collection<Mapping> mappings) {
		if (mappings == null) return;
		Iterator<Mapping> it = mappings.iterator();
		while (it.hasNext()) {
			reset(it.next());
		}
	}

	public static void reset(Config config, Mapping[] mappings) {
		if (mappings == null) return;
		for (int i = 0; i < mappings.length; i++) {
			reset(mappings[i]);
		}
	}

	public static void reset(Mapping mapping) {
		if (mapping == null) return;
		(((MappingImpl) mapping)).resetPages(null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		else throw new FunctionException(pc, "InspectTemplates", 0, 0, args.length);
	}
}