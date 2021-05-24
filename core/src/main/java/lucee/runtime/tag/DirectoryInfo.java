package lucee.runtime.functions.file;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.tag.Directory;
import lucee.runtime.type.Struct;

public class DirectoryInfo {
    public static Struct call(PageContext pc, String path) throws PageException {
        return Directory.getInfo(pc, ResourceUtil.toResourceExisting(pc, path), null);
    }
}