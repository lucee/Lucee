package lucee.runtime.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.DirectoryResourceFilter;
import lucee.commons.io.res.filter.OrResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.WildcardPatternFilter;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.other.CreateUniqueId;

public class ExportImportHandler {
	public static final short TYPE_CONFIGURATION = 1;
	public static final short TYPE_EXTENSION = 2;
	public static final short TYPE_CORE = 4;
	public static final short TYPE_FILES = 8;
	public static final short TYPE_ALL = TYPE_CONFIGURATION + TYPE_EXTENSION + TYPE_CORE + TYPE_FILES;

	public static void export(PageContext pc, ConfigServer cs, short types, String target, boolean addOptionalArtifacts, String regularMappingFilter, String componentMappingFilter,
			String customtagMappingFilter) throws IOException, PageException, ConverterException {

		Resource tmp = SystemUtil.getTempDirectory();
		Resource dir;
		// we need a new directory
		do {
			dir = tmp.getRealResource(CreateUniqueId.invoke());
		}
		while (dir.isDirectory());

		dir.createDirectory(true);

		Resource configFile = dir.getRealResource("config.json");
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<String, Resource> artifacts;
			artifacts = new HashMap<String, Resource>();
			map.put("artifacts", artifacts);

			// server
			map.put("server", _export((ConfigPro) cs, types, dir.getRealResource("server"), "/server", addOptionalArtifacts, regularMappingFilter, componentMappingFilter,
					customtagMappingFilter));

			// webs
			Resource websDir = dir.getRealResource("webs");
			List<Map<String, Object>> webs = new ArrayList<Map<String, Object>>();
			map.put("webs", webs);
			String id;
			for (ConfigWeb cw: cs.getConfigWebs()) {
				id = cw.getIdentification().getId();
				webs.add(_export((ConfigPro) cw, types, websDir.getRealResource(id), "/webs/" + id, addOptionalArtifacts, regularMappingFilter, componentMappingFilter,
						customtagMappingFilter));
			}

			// store config
			IOUtil.copy(toIS(JSONConverter.serialize(pc, map)), configFile, true);

			// zip everything
			CompressUtil.compress(CompressUtil.FORMAT_ZIP, dir, ResourceUtil.toResourceNotExisting(pc, target), false, -1);
		}
		finally {
			dir.delete();
		}
	}

	private static InputStream toIS(CharSequence cs) {
		return new ByteArrayInputStream(cs.toString().getBytes());
	}

	private static Map<String, Object> _export(ConfigPro config, short types, Resource dir, String pathAppendix, boolean addOptionalArtifacts, String regularMappingFilter,
			String componentMappingFilter, String customtagMappingFilter) throws IOException {
		Map<String, Object> map = new HashMap<String, Object>();

		// Core
		if ((types & TYPE_CONFIGURATION) > 0) {}
		// Extension
		if ((types & TYPE_EXTENSION) > 0) {
			Resource extDir = dir.getRealResource("extensions");
			extDir.mkdirs();
			List<Object> extensions = new ArrayList<Object>();
			map.put("extensions", extensions);
			Map<String, String> m;
			for (RHExtension ext: config.getRHExtensions()) {
				m = new HashMap<String, String>();
				extensions.add(m);
				m.put("id", ext.getId());
				m.put("version", ext.getVersion());
				if (dir != null) {
					m.put("artifact", pathAppendix + "/extensions/" + ext.getExtensionFile().getName());
					if (addOptionalArtifacts) IOUtil.copy(ext.getExtensionFile(), extDir.getRealResource(ext.getExtensionFile().getName()));
				}
			}
		}

		// Core
		if ((types & TYPE_CORE) > 0 && config instanceof ConfigServer) {
			map.put("core", CFMLEngineFactory.getInstance().getInfo().getVersion().toString());
		}
		// Files
		if ((types & TYPE_FILES) > 0) {
			Resource mapDir = dir.getRealResource("mappings");

			HashMap<String, Object> mappings = new HashMap<String, Object>();
			map.put("mappings", mappings);
			mappings.put("regular", exportMapping(config.getMappings(), mapDir.getRealResource("regular"), pathAppendix + "/mappings/regular/", regularMappingFilter));
			mappings.put("component",
					exportMapping(config.getComponentMappings(), mapDir.getRealResource("component"), pathAppendix + "/mappings/component/", componentMappingFilter));
			mappings.put("customtag",
					exportMapping(config.getCustomTagMappings(), mapDir.getRealResource("customtag"), pathAppendix + "/mappings/customtag/", customtagMappingFilter));
		}
		return map;
	}

	private static List<Object> exportMapping(Mapping[] mappings, Resource dir, String pathAppendix, String filter) throws IOException {
		List<Object> list = new ArrayList<Object>();
		Map<String, Object> m;
		for (Mapping mapping: mappings) {
			MappingImpl mi = (MappingImpl) mapping;
			m = new HashMap<String, Object>();
			list.add(m);
			m.put("virtual", mapping.getVirtual());
			m.put("inspect", ConfigWebUtil.inspectTemplate(mi.getInspectTemplateRaw(), ""));
			m.put("toplevel", mapping.isTopLevel());
			m.put("readonly", mapping.isReadonly());
			m.put("hidden", mapping.isHidden());
			m.put("physicalFirst", mapping.isPhysicalFirst());
			m.put("hidden", mapping.isHidden());

			// archive
			if (mapping.hasArchive()) {
				Resource archive = mapping.getArchive();
				if (archive.isFile()) {
					Resource arcDir = dir.getRealResource("archive/");
					arcDir.mkdir();
					m.put("archive", pathAppendix + "archive/" + archive.getName());
					IOUtil.copy(archive, arcDir.getRealResource(archive.getName()));
				}
			}

			// physical
			if (mapping.hasPhysical()) {
				Resource physical = mi.getPhysical();
				if (physical.isDirectory()) {
					String id = CreateUniqueId.invoke();
					Resource phyDir = dir.getRealResource("physical/" + id);
					phyDir.mkdirs();
					m.put("physical", pathAppendix + "physical/" + id);
					ResourceFilter f = null;
					if (!StringUtil.isEmpty(filter)) {
						f = new OrResourceFilter(new ResourceFilter[] { new WildcardPatternFilter(filter, ","), DirectoryResourceFilter.FILTER });
					}
					if (!physical.getAbsolutePath().equals("/")) // PATCH this needs more digging
						ResourceUtil.copyRecursive(physical, phyDir, f);
				}
			}
		}
		return list;
	}

	private static byte[] toBinary(Resource res) throws IOException {
		return IOUtil.toBytes(res);
	}
}
