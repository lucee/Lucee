package lucee.runtime.ai;

import org.osgi.framework.BundleException;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class AIEngineFactory {

	private ClassDefinition<? extends AIEngine> cd;
	private Struct properties;
	private String _default;
	private String name;

	public AIEngineFactory(ClassDefinition<? extends AIEngine> cd, Struct properties, String name, String _default) {
		this.cd = cd;
		this.properties = properties == null ? new StructImpl() : properties;
		this.name = name.trim();
		this._default = StringUtil.isEmpty(_default, true) ? null : _default.trim();
	}

	public static AIEngineFactory load(Config config, ClassDefinition<? extends AIEngine> cd, Struct custom, String name, String _default, boolean validate)
			throws ClassException, BundleException {
		// validate class
		if (validate) cd.getClazz();
		return new AIEngineFactory(cd, custom, name, _default);
	}

	public AIEngine createInstance(Config config) throws PageException, ClassException, BundleException {
		AIEngine aie = (AIEngine) ClassUtil.loadInstance(cd.getClazz());
		LogUtil.log(config, Log.LEVEL_TRACE, "ai", "ai-factory", "create AI instance [" + cd.toString() + "]");
		return aie.init(this, properties);
	}

	public String getDefault() {
		return _default;
	}

	public String getName() {
		return name;
	}
}
