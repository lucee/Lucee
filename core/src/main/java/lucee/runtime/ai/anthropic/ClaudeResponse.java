package lucee.runtime.ai.anthropic;

import java.util.List;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.Part;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public final class ClaudeResponse implements Response {
	private Struct raw;
	private String charset;
	private long tokens = -1L;
	private List<Part> cachedParts;

	public ClaudeResponse(Struct raw, String charset) {
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
		List<Part> parts = getAnswers();
		if (parts.isEmpty()) return null;

		StringBuilder sb = new StringBuilder();
		String text;
		for (Part part: parts) {
			if (!part.isText()) continue;
			text = part.getAsString();
			if (text != null) sb.append(text);
		}
		return sb.length() > 0 ? sb.toString() : null;
	}

	public Struct getData() {
		return raw;
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			Struct usage = Caster.toStruct(raw.get("usage", null), null);
			if (usage == null) return tokens = 0L;

			long inputTokens = Caster.toLongValue(usage.get("input_tokens", null), 0L);
			long outputTokens = Caster.toLongValue(usage.get("output_tokens", null), 0L);

			return tokens = inputTokens + outputTokens;
		}
		return tokens;
	}

	@Override
	public List<Part> getAnswers() {
		if (cachedParts != null) return cachedParts;
		return cachedParts = ClaudeResponseUtil.getAnswersFromRaw(raw);
	}

	@Override
	public boolean isMultiPart() {
		List<Part> parts = getAnswers();
		return parts.size() > 1 || (parts.size() == 1 && !parts.get(0).isText());
	}
}
