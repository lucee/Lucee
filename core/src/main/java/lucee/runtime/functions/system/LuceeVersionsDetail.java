
package lucee.runtime.functions.system;

import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.maven.GitHubReleaseProvider;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.MavenUpdateProvider.Repository;
import lucee.runtime.config.maven.Version;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class LuceeVersionsDetail extends BIF {

	private static final long serialVersionUID = -4525694087027654154L;

	public static Struct call(PageContext pc, String version) throws PageException {
		Version v = Version.parseVersion(version, null);

		// releases: Maven Central first, artifacts missing there (lco, light, zero) from GitHub Releases
		if (v != null && !"SNAPSHOT".equalsIgnoreCase(v.getQualifier())) {
			try {
				MavenUpdateProvider central = new MavenUpdateProvider(MavenUpdateProvider.getDefaultRepositorySnapshots(), MavenUpdateProvider.getDefaultRepositoryReleases(),
						new Repository[0], MavenUpdateProvider.DEFAULT_GROUP, MavenUpdateProvider.DEFAULT_ARTIFACT);
				Map<String, Object> data = central.detail(v, "jar", false);
				if (data != null) {
					GitHubReleaseProvider.fillMissing(v, data);
					return toStruct(data);
				}
			}
			catch (Exception e) {
				LogUtil.log(Log.LEVEL_WARN, "LuceeVersionsDetail", "Failed to read version [" + version + "] from Maven Central: " + e.getMessage());
			}

			// not on Maven Central, use the GitHub Release
			Map<String, Object> data = GitHubReleaseProvider.detail(v);
			if (data.containsKey("jar") || data.containsKey("lco")) return toStruct(data);
		}

		try {
			return LuceeVersionsDetailMvn.call(pc, version);
		}
		catch (Exception e) {
			return LuceeVersionsDetailS3.call(pc, version);
		}
	}

	private static Struct toStruct(Map<String, Object> data) throws PageException {
		Struct sct = new StructImpl();
		for (Entry<String, Object> e: data.entrySet()) {
			sct.set(e.getKey(), e.getValue());
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));

		throw new FunctionException(pc, "LuceeVersionsDetail", 1, 1, args.length);
	}
}
