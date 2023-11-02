package lucee.runtime.functions.file;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.tag.FileTag;
import lucee.runtime.type.Struct;

public class FileInfo {

	public static Struct call(PageContext pc, String path) throws PageException {
		return FileTag.getInfo(pc, ResourceUtil.toResourceExisting(pc, path), null);
	}
}
