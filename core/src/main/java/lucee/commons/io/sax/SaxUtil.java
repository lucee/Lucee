package lucee.commons.io.sax;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

public class SaxUtil {
	public static Map<String, String> toMap(Attributes atts) {
		Map<String, String> rtn = new HashMap<>();
		int len = atts.getLength();
		for (int i = 0; i < len; i++) {
			rtn.put(atts.getLocalName(i), atts.getValue(i));
		}
		return rtn;
	}
}
