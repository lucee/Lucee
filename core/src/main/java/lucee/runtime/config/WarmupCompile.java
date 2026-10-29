package lucee.runtime.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.CFMLFactory;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ThreadUtil;

/**
 * LDEV-3422 opt-in compile of all templates of the mappings during warmup
 * (LUCEE_ENABLE_WARMUP_COMPILE=true), so the class files already exist when the server is started
 * for real.
 */
public final class WarmupCompile {

	public static final String SETTING = "lucee.enable.warmup.compile";

	private static final String LOG_TYPE = "warmup-compile";
	private static final ResourceFilter FILTER_TEMPLATES = new OrResourceFilter(
			new ResourceFilter[] { new DirectoryResourceFilter(), new ExtensionResourceFilter(Constants.getExtensions()) });

	private static Boolean enabled;

	public static boolean isEnabled() {
		if (enabled == null) enabled = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar(SETTING, ""), Boolean.FALSE);
		return enabled.booleanValue();
	}

	public static final class Result {
		private int compiled;
		private int skippedMappings;
		private final List<String> failures = new ArrayList<>();

		public int getCompiled() {
			return compiled;
		}

		public int getFailed() {
			return failures.size();
		}

		public int getSkippedMappings() {
			return skippedMappings;
		}

		public List<String> getFailures() {
			return failures;
		}

		public String toSummary() {
			return "compiled " + compiled + " templates, " + failures.size() + " failed, skipped " + skippedMappings + " archive only mappings";
		}
	}

	/**
	 * compiles all templates of the mappings, component mappings and custom tag mappings of the given
	 * web context, template that fail to compile are logged and counted, they do not stop the compile
	 */
	public static Result compile(final ConfigWebPro config) {
		final Result result = new Result();
		// own thread, so the PageContext is not bound to the thread that is starting the context
		Thread t = new Thread("Lucee-warmup-compile") {
			@Override
			public void run() {
				compileAll(config, result);
			}
		};
		t.start();
		try {
			t.join();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			String msg = "interrupted while waiting for the compile of the templates";
			LogUtil.log(config, "application", LOG_TYPE + ": " + msg, e, Log.LEVEL_ERROR);
			synchronized (result) {
				result.failures.add(msg);
			}
		}
		synchronized (result) {
			return result;
		}
	}

	private static void compileAll(ConfigWebPro config, Result result) {
		PageContext pc = null;
		try {
			pc = ThreadUtil.createPageContext(config, "localhost", "/", "", null, true, Long.MAX_VALUE);
			Set<String> done = new HashSet<>();
			compile(pc, config, config.getMappings(), done, result);
			compile(pc, config, config.getComponentMappings(), done, result);
			compile(pc, config, config.getCustomTagMappings(), done, result);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			String msg = "failed to compile the templates";
			LogUtil.log(config, "application", LOG_TYPE + ": " + msg, t, Log.LEVEL_ERROR);
			synchronized (result) {
				result.failures.add(msg + ": " + t.getMessage());
			}
		}
		finally {
			if (pc != null) {
				CFMLFactory f = pc.getConfig().getFactory();
				f.releaseLuceePageContext(pc, true);
			}
		}
	}

	private static void compile(PageContext pc, ConfigWebPro config, Mapping[] mappings, Set<String> done, Result result) {
		if (mappings == null) return;
		for (Mapping mapping: mappings) {
			if (!mapping.hasPhysical()) {
				// archive only, already compiled
				synchronized (result) {
					result.skippedMappings++;
				}
				continue;
			}
			Resource dir = mapping.getPhysical();
			if (!done.add(mapping.getVirtualLowerCaseWithSlash() + "|" + dir.getAbsolutePath())) continue;
			compile(pc, config, mapping, dir, "", result);
		}
	}

	private static void compile(PageContext pc, ConfigWebPro config, Mapping mapping, Resource res, String path, Result result) {
		if (!ResourceUtil.exists(res)) return;
		if (res.isDirectory()) {
			Resource[] children = res.listResources(FILTER_TEMPLATES);
			if (children != null) {
				for (Resource child: children) {
					compile(pc, config, mapping, child, path + '/' + child.getName(), result);
				}
			}
			return;
		}
		if (!res.isFile()) return;

		PageSource ps = mapping.getPageSource(path);
		try {
			ps.loadPage(pc, false);
			synchronized (result) {
				result.compiled++;
			}
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			String template = ps.getDisplayPath();
			LogUtil.log(config, "application", LOG_TYPE + ": failed to compile [" + template + "]", t, Log.LEVEL_ERROR);
			synchronized (result) {
				result.failures.add(template + ": " + t.getMessage());
			}
		}
	}

	private WarmupCompile() {
	}
}
