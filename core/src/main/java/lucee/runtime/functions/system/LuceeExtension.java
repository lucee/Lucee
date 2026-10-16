package lucee.runtime.functions.system;

import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.res.Resource;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.maven.ExtensionProvider;
import lucee.runtime.config.maven.MavenUpdateProvider;
import lucee.runtime.config.maven.Version;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.extension.ExtensionMetadata;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.VersionRange;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class LuceeExtension extends BIF {

	private static final long serialVersionUID = -6051430362930149903L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {

		try {
			// list extensions
			if (args.length < 2) {
				ExtensionProvider ep = new ExtensionProvider(args.length == 0 ? MavenUpdateProvider.DEFAULT_GROUP : Caster.toString(args[0]).trim());
				Array arr = new ArrayImpl();
				for (String artifact: ep.list()) {
					arr.appendEL(artifact);
				}
				return arr;
			}
			// list versions of an extension
			else if (args.length == 2) {
				ExtensionProvider ep = new ExtensionProvider(Caster.toString(args[0]).trim());
				Array arr = new ArrayImpl();
				for (Version version: ep.list(Caster.toString(args[1]).trim())) {
					arr.appendEL(version.toString());
				}
				return arr;
			}
			// detail to a specific extension
			else if (args.length == 3 || args.length == 4) {
				ExtensionProvider ep = new ExtensionProvider(Caster.toString(args[0]).trim());
				String artifactId = Caster.toString(args[1]).trim();
				Version version = Version.parseVersion(Caster.toString(args[2]).trim());

				// detail
				Struct sct = new StructImpl();
				sct.set(KeyConstants._version, version.toString());
				Map<String, Object> data = ep.detail(artifactId, version);
				for (Entry<String, Object> e: data.entrySet()) {
					sct.set(Caster.toKey(e.getKey()), e.getValue());
				}

				boolean download = args.length == 4 ? Caster.toBooleanValue(args[3]) : false;

				// local resource
				if (download) {
					Resource local = ep.getResource((ConfigPro) pc.getConfig(), artifactId, version);
					sct.set(KeyConstants._local, local.getAbsolutePath());

					RHExtension ext = RHExtension.getInstance(pc.getConfig(), local);
					ExtensionMetadata em = ext.getMetadata();
					Struct meta = new StructImpl();
					meta.set(KeyConstants._id, em._getId());
					meta.set(KeyConstants._name, em.getName());
					meta.set(KeyConstants._description, em.getDescription());
					meta.set(KeyConstants._image, em.getImage());
					if (em.getBuiltDate() != null) {
						meta.set("buildDate", em.getBuiltDate());
					}

					// min core
					VersionRange minCore = em.getMinCoreVersion();
					if (minCore != null) {
						meta.set("MinCoreVersion", minCore.toString());
					}
					// min loader
					double minLoader = em.getMinLoaderVersion();
					if (minLoader > 0D) {
						meta.set("MinCoreVersion", minLoader);
					}
					sct.set(KeyConstants._metadata, meta);
				}

				return sct;
			}
			else {
				throw new FunctionException(pc, "LuceeExtension", 0, 4, args.length);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}