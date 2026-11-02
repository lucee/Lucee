package lucee.runtime.functions.system;

import java.util.LinkedHashMap;
import java.util.Map;

import lucee.loader.util.VersionImpl;
import lucee.runtime.config.maven.Version;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.s3.S3UpdateProvider;
import lucee.runtime.config.s3.S3UpdateProvider.Element;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;

public final class LuceeVersionsListS3 extends BIF {

	private static final long serialVersionUID = -7700672961703779256L;
	private static final int TYPE_ALL = 0;
	private static final int TYPE_SNAPSHOT = 1;
	private static final int TYPE_RELEASE = 2;

	public static Array call(PageContext pc, String type) throws PageException {
		return invoke("LuceeVersionsListS3", pc, type);
	}

	public static Array invoke(String functionName, PageContext pc, String type) throws PageException {
		// validate type
		int t = TYPE_ALL;
		boolean latest = false;
		if (!StringUtil.isEmpty(type, true)) {
			type = type.trim().toLowerCase();
			if ("all".equals(type)) t = TYPE_ALL;
			else if ("snapshot".equals(type)) t = TYPE_SNAPSHOT;
			else if ("release".equals(type)) t = TYPE_RELEASE;
			else if ("latest".equals(type)) {
				latest = true;
				t = TYPE_ALL;
			}
			else if ("latest:release".equals(type)) {
				latest = true;
				t = TYPE_RELEASE;
			}
			else if ("latest:snapshot".equals(type)) {
				latest = true;
				t = TYPE_SNAPSHOT;
			}
			else throw new FunctionException(pc, functionName, 1, "type",
					"type name [" + type + "] is invalid, valid types names are [all,snapshot,relase,latest,latest:release,latest:snapshot]");
		}

		try {
			S3UpdateProvider sup = S3UpdateProvider.getInstance();
			String key;
			// just the latest of every cycle
			if (latest) {
				Map<String, Element> map = new LinkedHashMap<>();
				Element existing;
				Version v;
				for (Element e: sup.read()) {
					v = e.getVersion();
					key = new StringBuilder().append(v.getMajor()).append('.').append(v.getMinor()).append('.').append(v.getMicro()).toString();
					if (t == TYPE_ALL || (t == TYPE_SNAPSHOT && v.getQualifier().endsWith("-SNAPSHOT")) || (t == TYPE_RELEASE && !v.getQualifier().endsWith("-SNAPSHOT"))) {
						existing = map.get(key);
						if (existing == null || VersionImpl.compare(existing.getVersion(), v) < 0) {
							map.put(key, e);
						}
					}
				}
				Array rtn = new ArrayImpl();
				for (Element e: map.values()) {
					rtn.append(LuceeVersionsDetailS3.toStruct(e));
				}
				return rtn;
			}
			// all
			Array rtn = new ArrayImpl();
			Version v;
			int row;
			for (Element e: sup.read()) {
				v = e.getVersion();
				if (t == TYPE_ALL || (t == TYPE_SNAPSHOT && v.getQualifier().endsWith("-SNAPSHOT")) || (t == TYPE_RELEASE && !v.getQualifier().endsWith("-SNAPSHOT"))) {
					rtn.append(LuceeVersionsDetailS3.toStruct(e));
				}
			}
			return rtn;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return invoke("LuceeVersionsListS3", pc, Caster.toString(args[0]));
		if (args.length == 0) return invoke("LuceeVersionsListS3", pc, null);

		throw new FunctionException(pc, "LuceeVersionsListS3", 0, 1, args.length);
	}
}