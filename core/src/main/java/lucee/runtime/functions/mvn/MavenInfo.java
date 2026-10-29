package lucee.runtime.functions.mvn;

import lucee.runtime.type.query.QueryReturnType;
import java.io.IOException;
import java.util.List;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.POM;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class MavenInfo implements Function {

	private static final long serialVersionUID = -3515451010781896377L;

	public static Query call(PageContext pc, String groupId, String artifactId) throws PageException {
		return call(pc, groupId, artifactId, null, null, true, false);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version) throws PageException {
		return call(pc, groupId, artifactId, version, null, true, false);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes) throws PageException {
		return call(pc, groupId, artifactId, version, arrScopes, true, false);
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes, boolean includeOptional) throws PageException {
		return call(pc, groupId, artifactId, version, arrScopes, includeOptional, false);
	}

	public static Object call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes, boolean includeOptional, boolean validate, String returntype)
			throws PageException {
		return call(pc, groupId, artifactId, version, arrScopes, includeOptional, validate, returntype, null);
	}

	public static Object call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes, boolean includeOptional, boolean validate, String returntype,
			String columnKey) throws PageException {
		// validate the returntype before the (possibly slow) lookup
		int rt = QueryReturnType.parse(returntype, "MavenInfo");
		return QueryReturnType.convert(call(pc, groupId, artifactId, version, arrScopes, includeOptional, validate), rt, QueryReturnType.toColumnKey(columnKey));
	}

	public static Query call(PageContext pc, String groupId, String artifactId, String version, Array arrScopes, boolean includeOptional, boolean validate) throws PageException {

		// validate groupId
		if (!StringUtil.isEmpty(groupId, true)) groupId = groupId.trim();
		else throw new FunctionException(pc, "MavenInfo", 1, "groupId", "argument is empty");

		// validate artifactId
		if (!StringUtil.isEmpty(artifactId, true)) artifactId = artifactId.trim();
		else throw new FunctionException(pc, "MavenInfo", 2, "artifactId", "argument is empty");

		// validate version
		if (StringUtil.isEmpty(version, true)) version = null;
		else version = version.trim();

		// validate scope
		int scopes;
		if (arrScopes != null && arrScopes.size() > 0) {
			try {
				scopes = MavenUtil.toScopes(ListUtil.toStringArray(arrScopes));
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
		else scopes = POM.SCOPES_FOR_RUNTIME;

		// validate returntype

		/*
		 * boolean asQuery = true; if (StringUtil.isEmpty(returntype, true)) asQuery = true; else if
		 * ("tree".equalsIgnoreCase(returntype.trim())) asQuery = false; else if
		 * ("query".equalsIgnoreCase(returntype.trim())) asQuery = true; else throw new
		 * FunctionException(pc, "MavenInfo", 5, "returntype", "valid values are [query,tree]");
		 */

		Log log = LogUtil.getLog(pc.getConfig(), "mvn", "application");
		POM root = POM.getInstance(((ConfigPro) pc.getConfig()).getMavenDir(), groupId, artifactId, version, scopes, log);

		try {
			// as Query
			// if (asQuery) {

			List<POM> deps = root.getAllDependencies(includeOptional);
			Query qry = new QueryImpl(new Key[] { KeyConstants._groupId, KeyConstants._artifactId, KeyConstants._version, KeyConstants._scope, KeyConstants._optional,
					KeyConstants._checksum, KeyConstants._url, KeyConstants._path }, 0, "dependencies");
			addRow(qry, root, false, validate, includeOptional);
			for (POM pom: deps) {
				addRow(qry, pom, true, validate, includeOptional); // depencies are "compile" by default
			}
			return qry;

			// }
			// FUTURE add tree
			// as tree
			/*
			 * List<TreeNode<POM>> tree = root.getAllDependenciesAsTrees(); for (TreeNode<POM> node: tree) {
			 * node. }
			 */

		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private static void addRow(Query qry, POM pom, boolean dependency, boolean validate, boolean includeOptional) throws PageException, IOException {

		if (!pom.getPackaging().equals("jar")) return;

		// scope
		String scope = pom.getScopeAsString();
		if (StringUtil.isEmpty(scope)) {
			scope = dependency ? "compile" : "";
		}

		// optional
		String strOptional = pom.getOptionalAsString();
		Boolean optional;
		if (StringUtil.isEmpty(strOptional)) {
			optional = dependency ? Boolean.FALSE : null;
		}
		else {
			optional = Caster.toBoolean(strOptional);
		}
		if (!includeOptional && Boolean.TRUE.equals(optional)) return;

		// local resource
		Resource jar = null;
		try {
			jar = pom.getArtifact("jar");
		}
		catch (IOException ioe) {

		}
		// checksum
		String checksum = pom.getChecksum();
		if (StringUtil.isEmpty(checksum)) {
			checksum = jar != null && jar.isFile() ? MavenUtil.createChecksum(jar, "md5") : "";
		}

		int row = qry.addRow();
		qry.setAt(KeyConstants._groupId, row, pom.getGroupId());
		qry.setAt(KeyConstants._artifactId, row, pom.getArtifactId());
		qry.setAt(KeyConstants._version, row, pom.getVersion());
		qry.setAt(KeyConstants._scope, row, scope);
		qry.setAt(KeyConstants._optional, row, optional);
		qry.setAt(KeyConstants._checksum, row, checksum);
		qry.setAt(KeyConstants._url, row, pom.getArtifactAsURL("jar", null, validate).toExternalForm());
		qry.setAt(KeyConstants._path, row, jar != null ? jar.toString() : "");
	}
}
