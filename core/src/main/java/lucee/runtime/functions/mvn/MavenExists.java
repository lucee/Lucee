package lucee.runtime.functions.mvn;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.mvn.POM;

public final class MavenExists implements Function {

	private static final long serialVersionUID = 4782196348512073921L;

	public static boolean call(PageContext pc, String coord) throws PageException {
		if (StringUtil.isEmpty(coord, true)) throw new FunctionException(pc, "MavenExists", 1, "groupId", "argument is empty");
		if (coord.indexOf(':') < 0) {
			throw new FunctionException(pc, "MavenExists", 1, "groupId",
					"single-argument form must be a gradle-style coord [groupId:artifactId] or [groupId:artifactId:version], got [" + coord + "]");
		}
		GAVSO gavso = MavenUtil.toGAVSO(coord, null);
		if (gavso == null) throw new FunctionException(pc, "MavenExists", 1, "groupId", "cannot parse [" + coord + "] as a gradle-style coord");
		return call(pc, gavso.g, gavso.a, gavso.v);
	}

	public static boolean call(PageContext pc, String groupId, String artifactId) throws PageException {
		return call(pc, groupId, artifactId, null);
	}

	public static boolean call(PageContext pc, String groupId, String artifactId, String version) throws PageException {
		if (StringUtil.isEmpty(groupId, true)) throw new FunctionException(pc, "MavenExists", 1, "groupId", "argument is empty");
		if (StringUtil.isEmpty(artifactId, true)) throw new FunctionException(pc, "MavenExists", 2, "artifactId", "argument is empty");

		Resource mvnDir = ((ConfigPro) pc.getConfig()).getMavenDir();
		String g = groupId.trim();
		String a = artifactId.trim();

		if (StringUtil.isEmpty(version, true)) {
			Resource artifactDir = POM.localArtifactDir(mvnDir, g, a);
			if (!artifactDir.isDirectory()) return false;
			Resource[] children = artifactDir.listResources();
			if (children == null) return false;
			for (Resource child: children) {
				if (child.isDirectory() && hasJar(mvnDir, g, a, child.getName())) return true;
			}
			return false;
		}

		return hasJar(mvnDir, g, a, version.trim());
	}

	private static boolean hasJar(Resource mvnDir, String g, String a, String v) {
		return POM.localDir(mvnDir, g, a, v).getRealResource(a + "-" + v + ".jar").isFile();
	}
}
