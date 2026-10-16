package lucee.runtime.writer;

import java.io.IOException;

import jakarta.servlet.jsp.tagext.BodyContent;
import lucee.runtime.PageContext;

/*
 * methods used in compiled cfml code, this classes replaces BodyContentUtil for never version of compiled code
 */
public class BCUtil {

	public static void clearAndPop(PageContext pc, Object obj) {
		if (obj != null) {
			BodyContent bc = (BodyContent) obj;
			bc.clearBody();
			pc.popBody();
		}
	}

	public static void clear(Object obj) {
		if (obj != null) {
			BodyContent bc = (BodyContent) obj;
			bc.clearBody();
		}
	}

	// older version
	public static void flushAndPop(PageContext pc, Object obj) {
		if (obj != null) {
			try {
				((BodyContent) obj).flush();
			}
			catch (IOException e) {}
			pc.popBody();
		}
	}

	public static void flush(Object obj) {
		if (obj != null) {
			try {
				((BodyContent) obj).flush();
			}
			catch (IOException e) {}
		}
	}

	public static Object pushBody(PageContext pc) {
		return pc.pushBody();
	}
}
