package lucee.runtime.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.JavaSettings;
import lucee.runtime.listener.JavaSettingsImpl;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.MavenCoordinate;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

/**
 * LDEV-5281 while warmup, the maven dependencies a component declares in the attribute javasettings
 * are resolved and downloaded when the component is compiled, so they are available when the server
 * is started for real. Only literal values are handled. Outside of warmup nothing is resolved at
 * compile time. A coordinate that is invalid or can not be downloaded fails the warmup.
 */
public final class WarmupJavaSettings {

	private static final List<String> failures = Collections.synchronizedList(new ArrayList<String>());

	/**
	 * @return true if the server is started in warmup mode (lucee.enable.warmup or lucee.build)
	 */
	public static boolean isWarmup() {
		Boolean build = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.enable.warmup", ""), null);
		if (build == null) build = Caster.toBoolean(SystemUtil.getSystemPropOrEnvVar("lucee.build", ""), null);
		return Boolean.TRUE.equals(build);
	}

	/**
	 * Resolves and downloads the maven dependencies of the given javasettings JSON. The failure is
	 * remembered, so the warmup is failed even if the exception does not stop it.
	 * 
	 * @param json the javasettings JSON of a component
	 * @param source description of the template the javasettings belongs to
	 * @throws IOException if a coordinate is invalid or an artifact can not be resolved
	 */
	public static void resolve(String json, String source) throws IOException {
		try {
			_resolve(json);
		}
		catch (Exception e) {
			String msg = "javasettings of [" + source + "]: " + e.getMessage();
			failures.add(msg);
			IOException ioe = new IOException(msg);
			ExceptionUtil.initCauseEL(ioe, e);
			throw ioe;
		}
	}

	private static void _resolve(String json) throws Exception {
		json = json.trim();
		// a path to a json file is only known at runtime
		if (StringUtil.endsWithIgnoreCase(json, ".json")) return;

		Struct data = Caster.toStruct(new JSONExpressionInterpreter().interpret(null, json));
		Object obj = data.get(KeyConstants._maven, null);
		if (obj == null) obj = data.get(KeyConstants._mvn, null);
		if (obj == null) return;

		Array arr = Caster.toArray(obj, null);
		List<MavenCoordinate> coordinates = new ArrayList<>();
		if (arr != null) {
			Iterator<Object> it = arr.valueIterator();
			while (it.hasNext()) {
				coordinates.add(MavenUtil.toCoordinate(it.next()));
			}
		}
		else {
			Struct sct = Caster.toStruct(obj, null);
			if (sct == null) throw new IOException("the value of [maven] must be an array of maven coordinates");
			coordinates.add(MavenUtil.toCoordinate(sct));
		}
		if (coordinates.isEmpty()) return;

		ConfigServer cs = ThreadLocalPageContext.getConfigServer();
		if (cs == null) throw new IOException("the maven dependencies can not be resolved, the server configuration is not available");

		MavenCoordinate[] arrCoordinates = coordinates.toArray(new MavenCoordinate[coordinates.size()]);
		String list = "[" + MavenUtil.toString(arrCoordinates) + "]";
		JavaSettings js = JavaSettingsImpl.getInstance(cs, arrCoordinates);
		Collection<Resource> jars;
		try {
			jars = ((JavaSettingsImpl) js).getAllResources();
		}
		catch (IOException e) {
			throw new IOException("failed to resolve the maven dependencies " + list + ": " + e.getMessage(), e);
		}
		for (Resource jar: jars) {
			if (!jar.isFile()) throw new IOException("the artifact [" + jar + "] of the maven dependencies " + list + " does not exist");
		}
	}

	public static boolean hasFailures() {
		return !failures.isEmpty();
	}

	public static List<String> getFailures() {
		synchronized (failures) {
			return new ArrayList<>(failures);
		}
	}
}
