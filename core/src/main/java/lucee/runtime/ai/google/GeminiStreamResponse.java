package lucee.runtime.ai.google;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.digest.Base64Encoder;
import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.AIResponseListener;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
import lucee.runtime.ai.Response;
import lucee.runtime.coder.CoderException;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public final class GeminiStreamResponse implements Response {

	private Array raw = new ArrayImpl();
	private String charset;
	private StringBuilder answer = new StringBuilder();

	// We store binary parts locally so getAnswers() works after the stream finishes
	private List<Part> binaryParts = new ArrayList<>();

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

	/**
	 * Called by the engine for every chunk received from the stream.
	 */
	public void addPart(Struct part, int chunkIndex, boolean complete) throws PageException {
		raw.appendEL(part);

		// 1. Imagen Style Response (Key: "predictions")
		Array predictions = Caster.toArray(part.get("predictions", null), null);
		if (predictions != null) {
			Object[] items = predictions.toArray();
			int predIndex = 0;
			for (Object item: items) {
				Struct pred = Caster.toStruct(item, null);
				if (pred != null) {
					String base64 = Caster.toString(pred.get("bytesBase64Encoded", null), null);
					String mime = Caster.toString(pred.get("mimeType", null), "image/png");

					if (base64 != null) {
						try {
							byte[] binary = Base64Encoder.decode(base64, false);

							// Store internally for getAnswers()
							binaryParts.add(new PartImpl(null, binaryParts.size(), binary, mime));

							// STREAM TO LISTENER: Use the multipart signature
							if (listener != null) {
								listener.listen(binary, mime, chunkIndex, predIndex++, complete);
							}
						}
						catch (CoderException e) {
							throw new PageRuntimeException(e);
						}
					}
				}
			}
		}

		// 2. Chat Style Response (Key: "candidates")
		Array candidates = Caster.toArray(part.get("candidates", null), null);
		if (candidates != null) {
			Struct candidate = Caster.toStruct(candidates.get(1, null), null);
			if (candidate != null) {
				Struct content = Caster.toStruct(candidate.get("content", null), null);
				if (content != null) {
					Array parts = Caster.toArray(content.get("parts", null), null);
					if (parts != null) {
						Object[] items = parts.toArray();
						int partIndex = 0;
						for (Object item: items) {
							Struct partStruct = Caster.toStruct(item, null);
							if (partStruct != null) {

								// A. Handle Text Updates
								String text = Caster.toString(partStruct.get(KeyConstants._text, null), null);
								if (!StringUtil.isEmpty(text)) {
									answer.append(text);

									// STREAM TEXT: Use the standard text signature
									if (listener != null) {
										listener.listen(text, "text/plain", chunkIndex, partIndex, complete);
									}
								}

								// B. Handle Inline Data (Images)
								Struct inlineData = Caster.toStruct(partStruct.get("inlineData", null), null);
								if (inlineData == null) inlineData = Caster.toStruct(partStruct.get("inline_data", null), null);

								if (inlineData != null) {
									String data = Caster.toString(inlineData.get(KeyConstants._data, null), null);
									if (data != null) {
										String mime = Caster.toString(inlineData.get(KeyConstants._mimeType, null), "application/octet-stream");
										try {
											byte[] binary = Base64Encoder.decode(data, false);

											// Store internally
											binaryParts.add(new PartImpl(null, binaryParts.size(), binary, mime));

											// STREAM MULTIPART: Use the multipart signature
											if (listener != null) {
												listener.listen(binary, mime, chunkIndex, partIndex, complete);
											}
										}
										catch (CoderException e) {
											throw new PageRuntimeException(e);
										}
									}
								}
							}
							partIndex++;
						}
					}
				}
			}
		}
	}

	@Override
	public long getTotalTokenUsed() {
		if (tokens == -1L) {
			if (raw.size() == 0) return 0L;

			Struct sct = Caster.toStruct(raw.get(raw.size(), null), null);
			if (sct == null) return tokens = 0L;

			sct = Caster.toStruct(sct.get("usageMetadata", null), null);
			if (sct == null) return tokens = 0L;
			return tokens = Caster.toLongValue(sct.get("totalTokenCount", null), 0L);
		}
		return tokens;
	}

	@Override
	public List<Part> getAnswers() {
		List<Part> results = new ArrayList<>();

		// 1. Add the full accumulated text
		String fullText = answer.toString();
		if (!StringUtil.isEmpty(fullText)) {
			results.add(new PartImpl(fullText, 0));
		}

		// 2. Add any binary parts collected
		if (!binaryParts.isEmpty()) {
			results.addAll(binaryParts);
		}

		return results;
	}

	@Override
	public boolean isMultiPart() {
		return !binaryParts.isEmpty();
	}
}