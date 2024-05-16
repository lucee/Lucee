package lucee.runtime.functions.system;

import java.io.IOException;
import java.nio.charset.Charset;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebFactory;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Struct;

public class ConfigTranslate extends BIF {

	private static final long serialVersionUID = -7311113511064530439L;

	public static Struct call(PageContext pc, String source, String target, String type, String mode, String charset) throws PageException {
		ConfigWeb config = pc.getConfig();
		try {
			// source
			InputSource src = null;
			if (!StringUtil.isEmpty(source, true)) {
				if ("web".equalsIgnoreCase(source.trim())) {
					Resource dir = ((ConfigWebPro) config).getWebConfigDir();
					Resource res = dir.getRealResource("lucee-web.xml.cfm");
					if (!res.isFile()) res = pc.getConfig().getConfigDir().getRealResource("lucee-web.xml");
					if (!res.isFile()) throw new FunctionException(pc, "ConfigTranslate", "first", "source",
							"could not find lucee web config at [" + res.getAbsolutePath() + "[.cfm]" + "]", null);
					src = XMLUtil.toInputSource(pc, res);
				}
				else if ("server".equalsIgnoreCase(source.trim())) {
					Resource res = pc.getConfig().getConfigServerDir().getRealResource("lucee-server.xml");
					if (!res.isFile())
						throw new FunctionException(pc, "ConfigTranslate", "first", "source", "could not find lucee server config at [" + res.getAbsolutePath() + "]", null);
					src = XMLUtil.toInputSource(pc, res);
				}
			}
			if (src == null) src = XMLUtil.toInputSource(pc, StringUtil.trim(source, true, true, ""));

			// target
			Resource trg = null;
			if (!StringUtil.isEmpty(target, true)) {
				if ("web".equalsIgnoreCase(target.trim())) {
					Resource dir = ((ConfigWebPro) config).getWebConfigDir();
					trg = dir.getRealResource(".CFConfig.json");
				}
				else if ("server".equalsIgnoreCase(target.trim())) {
					trg = pc.getConfig().getConfigServerDir().getRealResource(".CFConfig.json");
				}
				if (trg == null) trg = Caster.toResource(pc, target, false);
				if (!trg.getParentResource().isDirectory())
					throw new FunctionException(pc, "ConfigTranslate", "third", "target", "parent directory [" + trg.getParent() + "] for target does not exist.", null);
			}
			// type
			if (StringUtil.isEmpty(type)) type = "";
			else if (!"server".equalsIgnoreCase(type) && !"web".equalsIgnoreCase(type))
				throw new FunctionException(pc, "ConfigTranslate", "second", "type", "Invalid value for argument type [" + type + "], valid values are [server,web]", null);

			// mode
			if ("server".equals(type)) {
				if (StringUtil.isEmpty(mode)) mode = "";
				if (!"single".equalsIgnoreCase(mode) && !"multi".equalsIgnoreCase(mode))
					throw new FunctionException(pc, "ConfigTranslate", "second", "mode", "Invalid value for argument mode [" + mode + "], valid values are [single,multi]", null);
			}
			else {
				mode = "";
			}
			// charset TODO use it to load the path
			Charset cs = StringUtil.isEmpty(charset, true) ? pc.getResourceCharset() : CharsetUtil.toCharset(charset);

			return ConfigWebFactory.translateConfigFile((ConfigPro) pc.getConfig(), src, trg, mode, null);
		}
		catch (IOException ioe) {
			throw Caster.toPageException(ioe);
		}
		catch (SAXException se) {
			throw Caster.toPageException(se);
		}
		catch (ConverterException ce) {
			throw Caster.toPageException(ce);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), null, null, null);
		if (args.length == 3) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), null, null);
		if (args.length == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), null);
		if (args.length == 5) return call(pc, Caster.toString(args[0]), Caster.toString(args[1]), Caster.toString(args[2]), Caster.toString(args[3]), Caster.toString(args[4]));
		else throw new FunctionException(pc, "ConfigTranslate", 2, 5, args.length);
	}
}
