package lucee.runtime.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.JSONExpressionInterpreter;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class ConfigFile {

	public static void write(Resource configFile, Struct root, Charset charset) throws IOException, ConverterException {
		LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "writing the config file [" + configFile + "]");
		_write(configFile, root);
	}

	public static Struct read(Resource configFile, Charset charset) throws PageException, IOException {
		LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "read the config file [" + configFile + "]");
		return _read(configFile);
	}

	public static Struct reload(Resource configFile, Struct root) throws IOException, ConverterException {
		LogUtil.logGlobal((Config) null, Log.LEVEL_INFO, ConfigFactoryImpl.class.getName(), "reloading the config file [" + configFile + "]");
		_write(configFile, root);
		return root;// _read(configFile);
	}

	private static void _write(Resource configFile, Struct root) throws IOException, ConverterException {
		JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8, JSONDateFormat.PATTERN_CF, false);
		String str = json.serialize(null, root, SerializationSettings.SERIALIZE_AS_ROW, true);
		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			IOUtil.write(configFile, str, CharsetUtil.UTF8, false);
		}
	}

	private static Struct _read(Resource configFile) throws PageException, IOException {
		String raw;
		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			raw = IOUtil.toString(configFile, CharsetUtil.UTF8);
		}
		return Caster.toStruct(new JSONExpressionInterpreter().interpret(null, raw));
	}

	public static void createFileFromResource(String resource, Resource configFile, String password) throws IOException {

		InputStream is = InfoImpl.class.getResourceAsStream(resource);
		if (is == null) is = SystemUtil.getResourceAsStream(null, resource);
		if (is == null) throw new IOException("File [" + resource + "] does not exist.");

		synchronized (SystemUtil.createToken("ConfigFile", ResourceUtil.getNormalizedPathEL(configFile))) {
			if (configFile.exists()) configFile.delete();
			configFile.createNewFile();
			IOUtil.copy(is, configFile, true);
		}
		LogUtil.logGlobal(Log.LEVEL_DEBUG, ConfigFactory.class.getName(), "Written file: [" + configFile + "]");
	}

	/////////////////// Helper methods /////////////////

	public static Struct read(Resource configFile) throws PageException, IOException {
		return read(configFile, CharsetUtil.UTF8);
	}

	public static void write(Resource configFile, Struct root) throws IOException, ConverterException {
		write(configFile, root, CharsetUtil.UTF8);
	}

	public static void createConfigFile(String name, Resource configFile) throws IOException {
		createFileFromResource("/resource/config/" + name + ".json", configFile.getAbsoluteResource(), null);
	}

}
