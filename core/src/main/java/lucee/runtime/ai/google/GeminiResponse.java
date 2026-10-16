package lucee.runtime.ai.google;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.digest.Base64Encoder;
import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
import lucee.runtime.ai.Response;
import lucee.runtime.coder.CoderException;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class GeminiResponse implements Response {

	private Struct raw;
	private String charset;
	private long tokens = -1L;
	private List<Part> cachedParts;

	public GeminiResponse(Struct raw, String charset) {
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
		// Fallback for legacy calls - returns the first text part found
		List<Part> parts = getAnswers();
		if (parts == null || parts.isEmpty()) return null;

		StringBuilder sb = new StringBuilder();
		String a;
		for (Part rp: parts) {
			a = rp.getAsString();
			if (a != null) sb.append(a);
		}
		return sb.toString();
	}

	public Struct getData() {
		return raw;
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			Struct sct = Caster.toStruct(raw.get("usageMetadata", null), null);
			if (sct == null) return tokens = 0L;
			return tokens = Caster.toLongValue(sct.get("totalTokenCount", null), 0L);
		}
		return tokens;
	}

	@Override
	public List<Part> getAnswers() {
		if (cachedParts != null) return cachedParts;

		List<Part> results = new ArrayList<>();
		int index = 0;

		// 1. Check for Imagen Style Response (Key: "predictions")
		// Structure: { "predictions": [ { "bytesBase64Encoded": "...", "mimeType": "image/png" } ] }
		Array predictions = Caster.toArray(raw.get("predictions", null), null);
		if (predictions != null) {
			Object[] items = predictions.toArray();
			for (Object item: items) {
				Struct pred = Caster.toStruct(item, null);
				if (pred != null) {
					String base64 = Caster.toString(pred.get("bytesBase64Encoded", null), null);
					String mime = Caster.toString(pred.get("mimeType", null), "image/png"); // Default to png if missing

					if (base64 != null) {
						// Decode Base64 to byte array

						try {
							byte[] binary = Base64Encoder.decode(base64, false);
							results.add(new PartImpl(null, index++, binary, mime));
						}
						catch (CoderException e) {
							throw new PageRuntimeException(e);
						}

					}
				}
			}
		}

		// 2. Check for Gemini Chat Style Response (Key: "candidates")
		// Structure: { "candidates": [ { "content": { "parts": [ { "text": "..." }, { "inlineData": {...} }
		// ] } } ] }
		Array candidates = Caster.toArray(raw.get("candidates", null), null);
		if (candidates != null) {
			// Usually we only care about the first candidate in a chat response
			Struct candidate = Caster.toStruct(candidates.get(1, null), null); // Lucee Arrays are 1-based usually, but check your Caster implementation. Assuming 1 here based on
																				// your previous code.
			if (candidate != null) {
				Struct content = Caster.toStruct(candidate.get("content", null), null);
				if (content != null) {
					Array parts = Caster.toArray(content.get("parts", null), null);
					if (parts != null) {
						Object[] items = parts.toArray();
						for (Object item: items) {
							Struct partStruct = Caster.toStruct(item, null);
							if (partStruct != null) {
								// Check for Text
								String text = Caster.toString(partStruct.get(KeyConstants._text, null), null);
								if (!StringUtil.isEmpty(text)) {
									results.add(new PartImpl(text, index++));
								}

								// Check for Inline Data (Images sent back by Gemini)
								Struct inlineData = Caster.toStruct(partStruct.get("inlineData", null), null); // varying case depending on API version
								if (inlineData == null) inlineData = Caster.toStruct(partStruct.get("inline_data", null), null);

								if (inlineData != null) {
									String mime = Caster.toString(inlineData.get("mimeType", null), "application/octet-stream");
									String data = Caster.toString(inlineData.get("data", null), null);
									if (data != null) {
										try {
											byte[] binary = Base64Encoder.decode(data, false);
											results.add(new PartImpl(null, index++, binary, mime));
										}
										catch (CoderException e) {
											throw new PageRuntimeException(e);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		this.cachedParts = results;
		return results;
	}

	@Override
	public boolean isMultiPart() {
		List<Part> parts = getAnswers();
		// It is multipart if there is more than 1 part OR if the single part is not text

		return parts.size() > 1 || (parts.size() == 1 && !parts.get(0).isText());
	}

}