
package lucee.runtime.functions.system;

import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.framework.Version;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class MavenListVersions extends BIF {

	private static final long serialVersionUID = -353400384202349094L;
	private static final int TYPE_ALL = 0;
	private static final int TYPE_SNAPSHOT = 1;
	private static final int TYPE_RELEASE = 2;

	public static Array call(PageContext pc, String type) throws PageException {
		// validate type
		int t = TYPE_ALL;
		boolean latest = false;
		if (!StringUtil.isEmpty(type, true)) {
			type = type.trim().toLowerCase();
			if ("all".equals(type)) t = TYPE_ALL;
			else if ("snapshot".equals(type)) t = TYPE_SNAPSHOT;
			else if ("release".equals(type)) t = TYPE_RELEASE;
			else if ("latest".equals(type)) {
				latest = true;
				t = TYPE_ALL;
			}
			else if ("latest:release".equals(type)) {
				latest = true;
				t = TYPE_RELEASE;
			}
			else if ("latest:snapshot".equals(type)) {
				latest = true;
				t = TYPE_SNAPSHOT;
			}
			else throw new FunctionException(pc, "MavenListVersions", 1, "type",
					"type name [" + type + "] is invalid, valid types names are [all,snapshot,relase,latest,latest:release,latest:snapshot]");
		}
		MavenUpdateProvider mup = new MavenUpdateProvider();
		try {
			String key;
			// just the latest of every cycle
			if (latest) {
				Map<String, Version> map = new LinkedHashMap<>();
				Version existing;
				for (Version v: mup.list()) {
					key = new StringBuilder().append(v.getMajor()).append('.').append(v.getMinor()).append('.').append(v.getMicro()).toString();
					if (t == TYPE_ALL || (t == TYPE_SNAPSHOT && v.getQualifier().endsWith("-SNAPSHOT")) || (t == TYPE_RELEASE && !v.getQualifier().endsWith("-SNAPSHOT"))) {
						existing = map.get(key);
						if (existing == null || OSGiUtil.compare(existing, v) < 0) {
							map.put(key, v);
						}
					}
				}
				Array arr = new ArrayImpl();
				for (Version v: map.values()) {
					arr.append(v.toString());
				}
				return arr;
			}
			// all
			Array arr = new ArrayImpl();
			for (Version v: mup.list()) {
				if (t == TYPE_ALL || (t == TYPE_SNAPSHOT && v.getQualifier().endsWith("-SNAPSHOT")) || (t == TYPE_RELEASE && !v.getQualifier().endsWith("-SNAPSHOT"))) {
					arr.append(v.toString());
				}
			}
			return arr;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 0) return call(pc, null);

		throw new FunctionException(pc, "MavenListVersions", 0, 1, args.length);
	}
}