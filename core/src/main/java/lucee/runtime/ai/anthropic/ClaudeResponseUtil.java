package lucee.runtime.ai.anthropic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.digest.Base64Encoder;
import lucee.commons.lang.StringUtil;
import lucee.runtime.ai.Part;
import lucee.runtime.ai.PartImpl;
import lucee.runtime.coder.CoderException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

final class ClaudeResponseUtil {

	private ClaudeResponseUtil() {
	}

	static List<Part> getAnswersFromRaw(Struct raw) {
		try {
			Object content = raw.get(KeyConstants._content, null);
			if (content == null) return new ArrayList<>();
			return parseContent(content);
		}
		catch (PageException e) {
			throw new PageRuntimeException(e);
		}
	}

	static String getTextAnswer(Struct raw) {
		List<Part> parts = getAnswersFromRaw(raw);
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

	static List<Part> parseContent(Object content) throws PageException {
		List<Part> parts = new ArrayList<>();
		int index = 0;

		if (content == null) return parts;

		if (content instanceof CharSequence) {
			String text = content.toString();
			if (!StringUtil.isEmpty(text, true)) parts.add(new PartImpl(text, index));
			return parts;
		}

		Array arr = Caster.toArray(content, null);
		if (arr != null) {
			Iterator<Object> it = arr.valueIterator();
			Part part;
			while (it.hasNext()) {
				part = parseContentBlock(Caster.toStruct(it.next(), null), index);
				if (part != null) {
					parts.add(part);
					index++;
				}
			}
			return parts;
		}

		Struct sct = Caster.toStruct(content, null);
		if (sct != null) {
			Part part = parseContentBlock(sct, index);
			if (part != null) parts.add(part);
		}

		return parts;
	}

	private static Part parseContentBlock(Struct block, int index) throws PageException {
		if (block == null) return null;

		String type = Caster.toString(block.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type, true)) type = "text";

		if ("text".equals(type) || "code".equals(type)) {
			String text = Caster.toString(block.get(KeyConstants._text, null), null);
			if (StringUtil.isEmpty(text, true)) return null;
			return new PartImpl(text, index);
		}

		if ("thinking".equals(type)) {
			String thinking = Caster.toString(block.get("thinking", null), null);
			if (StringUtil.isEmpty(thinking, true)) thinking = Caster.toString(block.get(KeyConstants._text, null), null);
			if (StringUtil.isEmpty(thinking, true)) return null;
			return new PartImpl(thinking, index);
		}

		if ("image".equals(type) || "document".equals(type)) {
			return parseSourcePart(block, index);
		}

		if ("tool_use".equals(type) || "tool_result".equals(type) || "server_tool_use".equals(type)) {
			return new PartImpl(null, index, block, "application/json");
		}

		String fallback = Caster.toString(block.get(KeyConstants._text, null), null);
		if (!StringUtil.isEmpty(fallback, true)) return new PartImpl(fallback, index);

		return null;
	}

	private static Part parseSourcePart(Struct block, int index) throws PageException {
		Struct source = Caster.toStruct(block.get(KeyConstants._source, null), null);
		if (source == null) return null;

		String sourceType = Caster.toString(source.get(KeyConstants._type, null), null);
		if ("base64".equals(sourceType)) {
			String data = Caster.toString(source.get(KeyConstants._data, null), null);
			if (StringUtil.isEmpty(data, true)) return null;
			String mime = Caster.toString(source.get("media_type", null), null);
			if (StringUtil.isEmpty(mime, true)) mime = "application/octet-stream";
			try {
				return new PartImpl(null, index, Base64Encoder.decode(data, false), mime);
			}
			catch (CoderException e) {
				throw Caster.toPageException(e);
			}
		}

		if ("url".equals(sourceType)) {
			String url = Caster.toString(source.get(KeyConstants._url, null), null);
			if (StringUtil.isEmpty(url, true)) return null;
			DataUrl dataUrl = parseDataUrl(url);
			if (dataUrl != null) return new PartImpl(null, index, dataUrl.data, dataUrl.mimeType);
			return new PartImpl(url, index);
		}

		return null;
	}

	private static DataUrl parseDataUrl(String url) {
		if (StringUtil.isEmpty(url, true) || !url.startsWith("data:")) return null;

		int comma = url.indexOf(',');
		if (comma < 0) return null;

		String header = url.substring(5, comma);
		String encoded = url.substring(comma + 1);
		if (StringUtil.isEmpty(encoded, true)) return null;

		String mimeType = "application/octet-stream";
		int semi = header.indexOf(';');
		if (semi >= 0) {
			mimeType = header.substring(0, semi);
			String encoding = header.substring(semi + 1);
			if (!"base64".equalsIgnoreCase(encoding)) return null;
		}
		else if (!StringUtil.isEmpty(header, true)) {
			mimeType = header;
		}

		try {
			return new DataUrl(Base64Encoder.decode(encoded, false), mimeType);
		}
		catch (CoderException e) {
			return null;
		}
	}

	private static final class DataUrl {
		private final byte[] data;
		private final String mimeType;

		private DataUrl(byte[] data, String mimeType) {
			this.data = data;
			this.mimeType = mimeType;
		}
	}

}
