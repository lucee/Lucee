package lucee.runtime.ai.openai;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.ai.Response;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class OpenAIResponse implements Response {

	private Struct raw;
	private String charset;

	public OpenAIResponse(Struct raw, String charset) {
		this.raw = raw;
		this.charset = charset;
	}

	@Override
	public String toString() {
		try {
			JSONConverter json = new JSONConverter(false, CharsetUtil.toCharset(charset), JSONDateFormat.PATTERN_CF, false);
			return json.serialize(null, raw, SerializationSettings.SERIALIZE_AS_UNDEFINED, true);
		}
		catch (ConverterException e) {
			return raw.toString();
		}
	}

	@Override
	public String getAnswer() {
		Array arr = Caster.toArray(raw.get("choices", null), null);

		if (arr == null) return null;
		Struct sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return null;
		sct = Caster.toStruct(sct.get(KeyConstants._message, null), null);
		if (sct == null) return null;
		return Caster.toString(sct.get(KeyConstants._content, null), null);
	}

	public Struct getData() {
		return raw;
	}

}
