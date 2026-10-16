package lucee.runtime.functions.system;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class GetPropertyString implements Function {

	public static String call(PageContext pc, String fileName, String property, String encoding) throws PageException {
		Charset cs = StringUtil.isEmpty(encoding, true) ? CharsetUtil.UTF8 : CharsetUtil.toCharset(encoding);
		try {
			Resource res = ResourceUtil.toResourceNotExisting(pc, fileName);
			if (!res.isFile()) throw new ApplicationException("File ["+ fileName + "] is not a file");

			Properties props = new Properties();
			try (Reader reader = new InputStreamReader(res.getInputStream(), cs)) {
				props.load(reader);
			}
			return props.getProperty(property, "");
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}
}
