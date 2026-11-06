package lucee.runtime.ai.openai;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.AIUsage;
import lucee.runtime.ai.AIUsageProvider;
import lucee.runtime.ai.Response;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class OpenAIStreamResponse implements Response, AIUsageProvider {

	private Struct raw = null;

	private String charset;
	private StringBuilder answer = new StringBuilder();
	private AIResponseListener listener;
	private final List<Part> binaryParts = new ArrayList<>();
	private List<Part> cachedParts;
	private AIUsage usage;
	private String responseModel;

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

	/**
	 * the usage and the model of a chunk, the last usage reported wins because some providers repeat
	 * it with every chunk
	 */
	public void setUsage(Struct chunk) {
		if (chunk == null) return;
		AIUsage u = AIUsage.fromOpenAI(Caster.toStruct(chunk.get("usage", null), null));
		if (u != null) usage = u;
		String model = Caster.toString(chunk.get(KeyConstants._model, null), null);
		if (!StringUtil.isEmpty(model, true)) responseModel = model;
	}

	public void addPart(Struct part, int index, boolean complete) throws PageException {
		if (raw == null) raw = part;

		Array arr = Caster.toArray(part.get("choices", null), null);
		if (arr == null) return;
		Struct choice = Caster.toStruct(arr.get(1, null), null);
		if (choice == null) return;

		Struct contentSource = Caster.toStruct(choice.get(KeyConstants._delta, null), null);
		if (contentSource == null) contentSource = Caster.toStruct(choice.get(KeyConstants._message, null), null);
		if (contentSource == null) return;

		Object content = contentSource.get(KeyConstants._content, null);
		if (content == null) return;

		if (content instanceof CharSequence) {
			String str = content.toString();
			if (StringUtil.isEmpty(str, true)) return;
			answer.append(str);
			if (listener != null) listener.listen(str, index, complete);
			return;
		}

		List<Part> parts = OpenAIResponseUtil.parseContent(content);
		for (Part p: parts) {
			if (p.isText()) {
				String str = p.getAsString();
				if (!StringUtil.isEmpty(str, true)) {
					answer.append(str);
					if (listener != null) listener.listen(str, index, complete);
				}
			}
			else {
				binaryParts.add(new PartImpl(null, binaryParts.size(), p.getAsBinary(), p.getContentType()));
				if (listener != null) listener.listen(p.getAsBinary(), p.getContentType(), index, binaryParts.size() - 1, complete);
			}
		}
	}

	@Override
	public long getTotalTokenUsed() {
		return usage == null || usage.total < 0L ? 0L : usage.total;
	}

	@Override
	public AIUsage getUsage() {
		return usage;
	}

	@Override
	public String getResponseModel() {
		return responseModel;
	}

	@Override
	public List<Part> getAnswers() {
		if (cachedParts != null) return cachedParts;

		List<Part> results = new ArrayList<>();
		String fullText = answer.toString();
		if (!StringUtil.isEmpty(fullText, true)) {
			results.add(new PartImpl(fullText, 0));
		}
		if (!binaryParts.isEmpty()) {
			results.addAll(binaryParts);
		}

		return cachedParts = results;
	}

	@Override
	public boolean isMultiPart() {
		return !binaryParts.isEmpty() || getAnswers().size() > 1;
	}
}
