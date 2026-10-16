package lucee.runtime.functions.mvn;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.mvn.POM;
import lucee.runtime.mvn.POMReader;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.KeyConstants;

public final class MavenImport implements Function {

	private static final long serialVersionUID = 2847516392084751563L;

	public static Query call(PageContext pc, String pomPath) throws PageException {
		return call(pc, pomPath, false);
	}

	public static Query call(PageContext pc, String pomPath, boolean includeTransitive) throws PageException {
		if (StringUtil.isEmpty(pomPath, true)) throw new FunctionException(pc, "MavenImport", 1, "pomPath", "argument is empty");

		Resource pomFile = ResourceUtil.toResourceExisting(pc, pomPath);
		if (!pomFile.isFile()) throw new FunctionException(pc, "MavenImport", 1, "pomPath", "file does not exist [" + pomPath + "]");

		POMReader reader;
		try {
			// user-supplied pom — bypass the cache so subsequent edits are picked up
			reader = POMReader.parse(pomFile);
		}
		catch (IOException | SAXException e) {
			throw Caster.toPageException(e);
		}

		List<POMReader.Dependency> declared = reader.getDependencies();
		Resource mvnDir = ((ConfigPro) pc.getConfig()).getMavenDir();
		Log log = LogUtil.getLog(pc.getConfig(), "mvn", "application");

		Query qry = new QueryImpl(new Key[] {
				KeyConstants._groupId, KeyConstants._artifactId, KeyConstants._version,
				KeyConstants._scope, KeyConstants._optional, KeyConstants._checksum,
				KeyConstants._url, KeyConstants._path
		}, 0, "dependencies");

		try {
			for (POMReader.Dependency d: declared) {
				if (StringUtil.isEmpty(d.groupId, true) || StringUtil.isEmpty(d.artifactId, true) || StringUtil.isEmpty(d.version, true)) continue;
				POM pom = POM.getInstance(mvnDir, d.groupId.trim(), d.artifactId.trim(), d.version.trim(), log);
				addRow(qry, pom, d.scope, d.optional);
				if (includeTransitive) {
					for (POM child: pom.getAllDependencies(true)) {
						addRow(qry, child, null, null);
					}
				}
			}
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return qry;
	}

	private static void addRow(Query qry, POM pom, String declaredScope, String declaredOptional) throws PageException, IOException {
		if (!"jar".equals(pom.getPackaging())) return;

		String scope = pom.getScopeAsString();
		if (StringUtil.isEmpty(scope, true)) scope = StringUtil.isEmpty(declaredScope, true) ? "compile" : declaredScope.trim();

		String strOptional = pom.getOptionalAsString();
		if (StringUtil.isEmpty(strOptional, true)) strOptional = declaredOptional;
		Boolean optional = StringUtil.isEmpty(strOptional, true) ? Boolean.FALSE : Caster.toBoolean(strOptional, Boolean.FALSE);

		// download failures propagate — silently recording an empty path would lie about the import
		Resource jar = pom.getArtifact("jar");

		String checksum = pom.getChecksum();
		if (StringUtil.isEmpty(checksum, true) && jar != null && jar.isFile()) {
			checksum = lucee.runtime.mvn.MavenUtil.createChecksum(jar, "md5");
		}
		if (checksum == null) checksum = "";

		String url = pom.getArtifactAsURL("jar", null, false).toExternalForm();

		int row = qry.addRow();
		qry.setAt(KeyConstants._groupId, row, pom.getGroupId());
		qry.setAt(KeyConstants._artifactId, row, pom.getArtifactId());
		qry.setAt(KeyConstants._version, row, pom.getVersion());
		qry.setAt(KeyConstants._scope, row, scope);
		qry.setAt(KeyConstants._optional, row, optional);
		qry.setAt(KeyConstants._checksum, row, checksum);
		qry.setAt(KeyConstants._url, row, url);
		qry.setAt(KeyConstants._path, row, jar != null ? jar.toString() : "");
	}
}
