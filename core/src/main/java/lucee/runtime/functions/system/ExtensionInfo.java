package lucee.runtime.functions.system;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.extension.ExtensionMetadata;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.mvn.MavenUtil;
import lucee.runtime.mvn.MavenUtil.GAVSO;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.BundleInfo;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class ExtensionInfo extends BIF implements Function {

	private static final long serialVersionUID = 2627423175121799118L;

	public static Struct call(PageContext pc, String id) throws PageException {
		if (StringUtil.isEmpty(id, true)) return new StructImpl();
		return getInfo(id.trim(), ((ConfigWebPro) pc.getConfig()).getRHExtensions());
	}

	private static Struct getInfo(String id, RHExtension[] extensions) throws PageException {
		Struct sct = new StructImpl();

		// is id a gav?
		GAVSO gav = MavenUtil.toGAVSO(id, null);

		for (RHExtension ext: extensions) {
			if ((gav != null && ext.hasGAV() && new GAVSO(ext.getGroupId(), ext.getArtifactId(), ext.getVersion()).equals(gav))
					|| (ext.getId().equalsIgnoreCase(id) || ext.getMetadata().getSymbolicName().equalsIgnoreCase(id))) {

				ExtensionMetadata md = ext.getMetadata();
				String ver = ext.getVersion().toString();
				String sName = md.getSymbolicName();
				sct.set(KeyConstants._id, ext.getId());
				sct.set(KeyConstants._symbolicName, sName);
				sct.set(KeyConstants._name, md.getName());
				sct.set(KeyConstants._image, md.getImage());
				sct.set(KeyConstants._description, md.getDescription());
				sct.set(KeyConstants._version, ver == null ? null : ver);
				sct.set(KeyConstants._trial, md.isTrial());
				sct.set(KeyConstants._releaseType, RHExtension.toReleaseType(md.getReleaseType(), "all"));
				try {
					sct.set(KeyConstants._flds, Caster.toArray(md.getFlds()));
					sct.set(KeyConstants._tlds, Caster.toArray(md.getTlds()));
					sct.set(KeyConstants._functions, Caster.toArray(md.getFunctions()));
					sct.set(KeyConstants._archives, Caster.toArray(md.getArchives()));
					sct.set(KeyConstants._tags, Caster.toArray(md.getTags()));
					sct.set(KeyConstants._contexts, Caster.toArray(md.getContexts()));
					sct.set(KeyConstants._webcontexts, Caster.toArray(md.getWebContexts()));
					sct.set(KeyConstants._config, Caster.toArray(md.getConfigs()));
					sct.set(KeyConstants._eventGateways, Caster.toArray(md.getEventGateways()));
					sct.set(KeyConstants._categories, Caster.toArray(md.getCategories()));
					sct.set(KeyConstants._applications, Caster.toArray(md.getApplications()));
					sct.set(KeyConstants._components, Caster.toArray(md.getComponents()));
					sct.set(KeyConstants._plugins, Caster.toArray(md.getPlugins()));
					sct.set(KeyConstants._startBundles, Caster.toBoolean(md.isStartBundles()));

					BundleInfo[] bfs = md.getBundles();
					Query qryBundles = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._version }, bfs == null ? 0 : bfs.length, "bundles");
					if (bfs != null) {
						for (int i = 0; i < bfs.length; i++) {
							qryBundles.setAt(KeyConstants._name, i + 1, bfs[i].getSymbolicName());
							if (bfs[i].getVersion() != null) qryBundles.setAt(KeyConstants._version, i + 1, bfs[i].getVersionAsString());
						}
					}
					sct.set("Bundles", qryBundles);
				}
				catch (Exception e) {
					throw Caster.toPageException(e);
				}
			}
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		else throw new FunctionException(pc, "ExtensionExists", 1, 1, args.length);
	}
}