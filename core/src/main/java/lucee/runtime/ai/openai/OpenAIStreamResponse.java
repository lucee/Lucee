package lucee.runtime.ai.openai;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.Response;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class OpenAIStreamResponse implements Response {

	private Struct raw = null;
	private Array choices = null;

	private String charset;
	private StringBuilder answer = new StringBuilder();
	private AIResponseListener listener;

	public OpenAIStreamResponse(String charset, AIResponseListener listener) {
		this.charset = charset;
		this.listener = listener;
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
		return answer.toString();
	}

	public Struct getData() {
		return raw;
	}

	public void addPart(Struct part) throws PageException {
		if (raw == null) raw = part;
		// raw.appendEL(part);
		Array arr = Caster.toArray(part.get("choices", null), null);
		// print.e(arr);
		if (arr == null) return;
		Struct sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return;
		if (choices == null) choices = arr;
		else choices.appendEL(sct);

		sct = Caster.toStruct(sct.get(KeyConstants._delta, null), null);
		if (sct == null) return;
		String str = Caster.toString(sct.get(KeyConstants._content, null), null);
		answer.append(str);
		if (listener != null) listener.listen(str);
	}

}
