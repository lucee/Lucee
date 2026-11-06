package lucee.runtime.ai.anthropic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

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
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class ClaudeStreamResponse implements Response, AIUsageProvider {
	private Struct raw = null;
	private String charset;
	private StringBuilder answer = new StringBuilder();
	private AIResponseListener listener;
	private final List<Part> binaryParts = new ArrayList<>();
	private List<Part> cachedParts;
	private final Struct usageData = new StructImpl();
	private String responseModel;

	public ClaudeStreamResponse(String charset, AIResponseListener listener) {
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

	public void addPart(Struct part, int index, boolean complete) throws PageException {

		if (raw == null) raw = part;

		String type = Caster.toString(part.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type)) return;

		if ("message_start".equals(type)) {
			Struct msg = Caster.toStruct(part.get("message", null), null);
			if (msg != null) {
				collectUsage(Caster.toStruct(msg.get("usage", null), null));
				String model = Caster.toString(msg.get(KeyConstants._model, null), null);
				if (!StringUtil.isEmpty(model, true)) responseModel = model;
			}
			return;
		}
		if ("message_delta".equals(type)) {
			collectUsage(Caster.toStruct(part.get("usage", null), null));
			return;
		}

		if ("content_block_start".equals(type)) {
			Struct block = Caster.toStruct(part.get("content_block", null), null);
			if (block == null) return;
			String blockType = Caster.toString(block.get(KeyConstants._type, null), null);
			if ("image".equals(blockType) || "document".equals(blockType)) {
				List<Part> parsed = ClaudeResponseUtil.parseContent(block);
				for (Part p: parsed) {
					if (p.isText()) continue;
					binaryParts.add(new PartImpl(null, binaryParts.size(), p.getAsBinary(), p.getContentType()));
					if (listener != null) listener.listen(p.getAsBinary(), p.getContentType(), index, binaryParts.size() - 1, complete);
				}
			}
			return;
		}

		if (!type.startsWith("content_block")) return;

		Struct delta = Caster.toStruct(part.get("delta", null), null);
		if (delta == null) return;

		type = Caster.toString(delta.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type) || !type.startsWith("text")) return;

		String text = Caster.toString(delta.get(KeyConstants._text, null), null);
		if (StringUtil.isEmpty(text)) return;

		answer.append(text);
		if (listener != null) listener.listen(text, index, complete);
	}

	// message_start has the input tokens and message_delta the final (cumulative) output tokens, the values of a later event replace the earlier ones
	private void collectUsage(Struct usage) {
		if (usage == null) return;
		Iterator<Entry<Key, Object>> it = usage.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			if (e.getValue() instanceof Number) usageData.setEL(e.getKey(), e.getValue());
		}
	}

	@Override
	public long getTotalTokenUsed() {
		AIUsage u = getUsage();
		return u == null || u.total < 0L ? 0L : u.total;
	}

	@Override
	public AIUsage getUsage() {
		return AIUsage.fromClaude(usageData);
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
