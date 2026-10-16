package lucee.runtime.ai;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ComplexAnswer extends ArrayImpl implements CharSequence {

	private String value;

	public ComplexAnswer(Response rsp) {
		Struct item;
		for (Part a: rsp.getAnswers()) {
			item = new StructImpl(Struct.TYPE_LINKED);
			appendEL(item);
			item.setEL(KeyConstants._contenttype, a.getContentType());

			if (a.isText()) {
				item.setEL(KeyConstants._type, "text");
				item.setEL(KeyConstants._content, a.getAsString());
			}
			else {
				item.setEL(KeyConstants._type, "binary");
				item.setEL(KeyConstants._content, a.getAsBinary());
			}
		}

		value = AIUtil.extractStringAnswer(rsp);

	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);
		dt.setTitle("Array (Complext Answer)");
		dt.setComment("this Object can be handled like an array, but also like a string.");
		return dt;
	}

	@Override
	public int length() {
		return value.length();
	}

	@Override
	public char charAt(int index) {
		return value.charAt(index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return value.subSequence(start, end);
	}
}
