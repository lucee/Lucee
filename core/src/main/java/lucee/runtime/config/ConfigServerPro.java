package lucee.runtime.config;

import lucee.runtime.type.Struct;

public interface ConfigServerPro extends ConfigPro, ConfigServer {

	Struct getRawData();

}
