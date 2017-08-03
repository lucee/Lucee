package lucee.runtime.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.print;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.functions.conversion.SerializeJSON;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ExportImportHandler {
	public static final short TYPE_CONFIGURATION=1;
	public static final short TYPE_EXTENSION=2;
	public static final short TYPE_CORE=4;
	public static final short TYPE_FILES=8;
	public static final short TYPE_ALL=TYPE_CONFIGURATION+TYPE_EXTENSION+TYPE_CORE+TYPE_FILES;
	
	public static Map<String,Object> export(ConfigServer cs, short types, boolean addArtifacts) {
		Map<String,Object> map=new HashMap<String, Object>();
		Map<String,Resource> artifacts=addArtifacts?new HashMap<String,Resource>():null;
		// server
		map.put("server", _export((ConfigImpl)cs, types,artifacts));
		
		// webs
		List<Map<String,Object>> webs=new ArrayList<Map<String,Object>>();
		map.put("webs", webs);
		for(ConfigWeb cw:cs.getConfigWebs()) {
			webs.add(_export((ConfigImpl)cw, types,artifacts));
		}
		return map;
	}
	public static Map<String,Object> _export(ConfigImpl config, short types, Map<String,Resource> artifacts) {
		Map<String,Object> map=new HashMap<String, Object>();
		
		// Core
		if((types&TYPE_CONFIGURATION)>0) {
		}
		// Extension
		if((types&TYPE_EXTENSION)>0) {
			List<Object> extensions=new ArrayList<Object>();
			map.put("extensions", extensions);
			Map<String,String> m;
			for(RHExtension ext:config.getRHExtensions()) {
				m=new HashMap<String, String>();
				extensions.add(m);
				String key=CreateUniqueId.invoke();
				m.put("id", ext.getId());
				m.put("version", ext.getVersion());
				if(artifacts!=null) {
					m.put("artifactId", key);
					artifacts.put(key,ext.getExtensionFile());
				}
			}
		}
		
		// Core
		if((types&TYPE_CORE)>0 && config instanceof ConfigServer) {
			map.put("core", CFMLEngineFactory.getInstance().getInfo().getVersion().toString());
		}
		// Files
		if((types&TYPE_FILES)>0) {
			// regular mappings
			List<Object> list=new ArrayList<Object>();
			map.put("regular-mappings", list);
			Map<String,Object> m;
			Mapping[] mappings = config.getMappings();
			for(Mapping mapping:mappings) {
				MappingImpl mi=(MappingImpl) mapping;
				m=new HashMap<String, Object>();
				list.add(m);
				m.put("virtual", mapping.getVirtual());
				m.put("inspect", ConfigWebUtil.inspectTemplate(mi.getInspectTemplateRaw(),""));
				m.put("toplevel",mapping.isTopLevel());
				m.put("readonly", mapping.isReadonly());
				m.put("hidden", mapping.isHidden());
				m.put("physicalFirst", mapping.isPhysicalFirst());
				m.put("hidden", mapping.isHidden());
				
			}
		}
		return map;
	}
}
