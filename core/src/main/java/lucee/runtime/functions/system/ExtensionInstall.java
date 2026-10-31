package lucee.runtime.functions.system;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigUtil;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.ResetFilter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.mvn.MavenUtil.MavenCoordinate;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * LDEV-1585: install an extension at runtime, so a script can declare the extensions it needs.
 * 
 * extensionInstall( extension [, version [, password]] )
 * 
 * extension is a Maven coordinate in gradle short form (group:artifact or group:artifact:version).
 * The extension id (UUID) is still supported for extensions that are not available on Maven.
 */
public final class ExtensionInstall extends BIF implements Function {

	private static final long serialVersionUID = 4431970156843309641L;

	public static Struct call(PageContext pc, String extension) throws PageException {
		return call(pc, extension, null, null);
	}

	public static Struct call(PageContext pc, String extension, String version) throws PageException {
		return call(pc, extension, version, null);
	}

	public static Struct call(PageContext pc, String extension, String version, String password) throws PageException {
		if (StringUtil.isEmpty(extension, true)) throw new FunctionException(pc, "ExtensionInstall", 1, "extension", "argument is empty");
		extension = extension.trim();
		if ("latest".equalsIgnoreCase(version)) version = null;
		if (!StringUtil.isEmpty(version, true)) version = version.trim();
		else version = null;

		// same access rules as the admin tag, installing an extension changes the server configuration
		ConfigServerImpl cs = ConfigUtil.getConfigServerImpl(pc.getConfig());
		if (StringUtil.isEmpty(password)) password = SystemUtil.getSystemPropOrEnvVar("lucee.server.admin.password", null);
		ConfigUtil.checkGeneralWriteAccess(cs, StringUtil.isEmpty(password) ? null : cs.isPasswordEqual(password));

		// maven coordinate (primary form)
		ExtensionDefintion ed;
		MavenCoordinate coordinate = null;
		if (extension.indexOf(':') != -1) {
			try {
				coordinate = MavenUtil.toCoordinate(extension);
			}
			catch (PageException pe) {
				throw new FunctionException(pc, "ExtensionInstall", 1, "extension", pe.getMessage());
			}
			// the version defined with the coordinate must not contradict the version argument
			if (!StringUtil.isEmpty(coordinate.version, true)) {
				if (version != null && !version.equals(coordinate.version.trim())) {
					throw new FunctionException(pc, "ExtensionInstall", 2, "version",
							"the version [" + version + "] conflicts with the version [" + coordinate.version + "] defined in the coordinate [" + extension + "]");
				}
				version = coordinate.version.trim();
			}
			coordinate = new MavenCoordinate(coordinate.groupId, coordinate.artifactId, version);

			// no version defined, so any installed version is good enough
			if (version == null) {
				RHExtension installed = findInstalled(cs, coordinate, null);
				if (installed != null) return toStruct(installed, true);
			}
			ed = RHExtension.toExtensionDefinition(coordinate.toGAV());
		}
		// legacy, extension id
		else {
			if (!Decision.isUUId(extension)) {
				throw new FunctionException(pc, "ExtensionInstall", 1, "extension",
						"invalid value [" + extension + "], the value must be a Maven coordinate in the form [groupId:artifactId] or [groupId:artifactId:version] (or the id (UUID) of an extension that is not available on Maven)");
			}
			if (version == null) {
				RHExtension installed = findInstalled(cs, null, extension);
				if (installed != null) return toStruct(installed, true);
				ed = RHExtension.toExtensionDefinition(extension);
			}
			else ed = new ExtensionDefintion(extension, version);
		}
		if (ed == null) throw new FunctionException(pc, "ExtensionInstall", 1, "extension", "cannot resolve the extension [" + extension + "]");

		Log log = ThreadLocalPageContext.getLog(pc, "application");
		ResetFilter filter = new ResetFilter();
		RefBooleanImpl installDone = new RefBooleanImpl(true);
		RHExtension rhe;
		try {
			rhe = DeployHandler.deployExtension(cs, ed, filter, log, true, false, true, installDone);
		}
		finally {
			filter.resetThrowPageException(cs);
		}
		if (rhe == null) throw new FunctionException(pc, "ExtensionInstall", 1, "extension", "failed to install the extension [" + extension + "]");
		return toStruct(rhe, !installDone.toBooleanValue());
	}

	private static RHExtension findInstalled(ConfigPro config, MavenCoordinate coordinate, String id) {
		for (RHExtension ext: config.getRHExtensions()) {
			if (coordinate != null) {
				if (ext.hasGAV() && coordinate.groupId.equalsIgnoreCase(ext.getGroupId()) && coordinate.artifactId.equalsIgnoreCase(ext.getArtifactId())
						&& (StringUtil.isEmpty(coordinate.version, true) || coordinate.version.trim().equalsIgnoreCase(ext.getVersion()))) {
					return ext;
				}
			}
			else if (ext.getId().equalsIgnoreCase(id)) return ext;
		}
		return null;
	}

	private static Struct toStruct(RHExtension ext, boolean alreadyInstalled) throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._id, ext.getId());
		sct.set(KeyConstants._name, ext.getMetadata().getName());
		sct.set(KeyConstants._version, ext.getVersion());
		sct.set(KeyConstants._maven, ext.hasGAV() ? new MavenCoordinate(ext.getGroupId(), ext.getArtifactId(), ext.getVersion()).toGAV() : "");
		sct.set("alreadyInstalled", alreadyInstalled);
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]));
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]));
		throw new FunctionException(pc, "ExtensionInstall", 1, 3, args.length);
	}
}
