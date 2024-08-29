package lucee.runtime.ai.google;

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
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class GeminiStreamResponse implements Response {

	private Array raw = new ArrayImpl();

	private String charset;
	private StringBuilder answer = new StringBuilder();

	private AIResponseListener listener;

	private long tokens = -1L;

	public GeminiStreamResponse(String charset, AIResponseListener listener) {
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

	public Array getData() {
		return raw;
	}

	public void addPart(Struct part) throws PageException {
		raw.appendEL(part);
		Array arr = Caster.toArray(part.get("candidates", null), null);
		if (arr == null) return;
		Struct sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return;
		sct = Caster.toStruct(sct.get("content", null), null);
		if (sct == null) return;

		arr = Caster.toArray(sct.get("parts", null), null);
		if (arr == null) return;

		sct = Caster.toStruct(arr.get(1, null), null);
		if (sct == null) return;

		String str = Caster.toString(sct.get(KeyConstants._text, null), null);
		if (listener != null) listener.listen(str);
		answer.append(str);
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			Struct sct = Caster.toStruct(raw.get(raw.size(), null), null);
			if (sct == null) return tokens = 0L;

			sct = Caster.toStruct(sct.get("usageMetadata", null), null);
			if (sct == null) return tokens = 0L;
			return tokens = Caster.toLongValue(sct.get("totalTokenCount", null), 0L);
		}
		return tokens;
	}
}
