
package lucee.runtime.functions.system;

import java.net.URL;

import org.osgi.framework.Version;

import lucee.runtime.PageContext;
import lucee.runtime.config.s3.S3UpdateProvider;
import lucee.runtime.config.s3.S3UpdateProvider.Element;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class LuceeVersionsDetailS3 extends BIF {

	private static final long serialVersionUID = 1009881259163647851L;

	public static Struct call(PageContext pc, String version) throws PageException {

		try {
			Version v = OSGiUtil.toVersion(version);
			S3UpdateProvider sup = S3UpdateProvider.getInstance(S3UpdateProvider.DEFAULT_PROVIDER_LIST, S3UpdateProvider.DEFAULT_PROVIDER_DETAILS);
			for (Element e: sup.read()) {
				if (v.equals(e.getVersion())) {
					Struct sct = new StructImpl();
					sct.set("etag", e.getETag());
					sct.set(KeyConstants._lastModified, e.getLastModifed());
					sct.set(KeyConstants._size, e.getSize());
					sct.set(KeyConstants._version, e.getVersion().toString());
					URL url = e.getLCO();
					if (url != null) sct.set("lco", url.toExternalForm());
					url = e.getJAR();
					if (url != null) sct.set("jar", url.toExternalForm());
					return sct;
				}
			}

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		throw new FunctionException(pc, "LuceeVersionsDetailS3", 1, "version", "no version [" + version + "] found.");
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));

		throw new FunctionException(pc, "LuceeVersionsDetailS3", 1, 1, args.length);
	}
}