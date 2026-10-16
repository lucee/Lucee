package lucee.commons.io.sax;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public final class SaxUtil {
	public static Map<String, String> toMap(Attributes atts) {
		int len = atts.getLength();
		Map<String, String> rtn = new HashMap<>(len);
		for (int i = 0; i < len; i++) {
			rtn.put(atts.getLocalName(i), atts.getValue(i));
		}
		return rtn;
	}
}
