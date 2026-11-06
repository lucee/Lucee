
package lucee.runtime.functions.system;

import java.net.MalformedURLException;

import lucee.loader.util.VersionImpl;
import lucee.runtime.config.maven.Version;

import lucee.runtime.PageContext;
import lucee.runtime.config.s3.S3UpdateProvider;
import lucee.runtime.config.s3.S3UpdateProvider.Artifact;
import lucee.runtime.config.s3.S3UpdateProvider.Element;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class LuceeVersionsDetailS3 extends BIF {
	private static final Key ETAG = KeyImpl.init("etag");
	private static final long serialVersionUID = 1009881259163647851L;

	public static Struct call(PageContext pc, String version) throws PageException {

		try {
			Version v = VersionImpl.parseOSGi(version);
			S3UpdateProvider sup = S3UpdateProvider.getInstance();
			for (Element e: sup.read()) {
				if (v.equals(e.getVersion())) {
					return toStruct(e);
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

	public static Struct toStruct(Element el) throws PageException {
		Struct sct = new StructImpl();

		sct.set(ETAG, el.getETag());
		sct.set(KeyConstants._lastModified, el.getLastModifed());
		sct.set(KeyConstants._size, el.getSize());
		sct.set(KeyConstants._version, el.getVersion().toString());
		try {
			for (Artifact a: el.getArtifacts()) {
				sct.set(a.classifier != null ? a.classifier : a.type, a.getURL().toExternalForm());
			}
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}
		return sct;
	}
}