package lucee.runtime.ai.openai;

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
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

final class OpenAIResponseUtil {

	private OpenAIResponseUtil() {
	}

	static List<Part> getAnswersFromRaw(Struct raw) {
		try {
			Object content = extractMessageContent(raw);
			if (content == null) {
				String refusal = extractMessageRefusal(raw);
				if (!StringUtil.isEmpty(refusal, true)) {
					List<Part> parts = new ArrayList<>();
					parts.add(new PartImpl(refusal, 0));
					return parts;
				}
				return new ArrayList<>();
			}
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
				part = parseContentPart(Caster.toStruct(it.next(), null), index);
				if (part != null) {
					parts.add(part);
					index++;
				}
			}
			return parts;
		}

		Struct sct = Caster.toStruct(content, null);
		if (sct != null) {
			Part part = parseContentPart(sct, index);
			if (part != null) parts.add(part);
		}

		return parts;
	}

	private static Part parseContentPart(Struct block, int index) throws PageException {
		if (block == null) return null;

		String type = Caster.toString(block.get(KeyConstants._type, null), null);
		if (StringUtil.isEmpty(type, true)) type = "text";

		if ("text".equals(type)) {
			String text = Caster.toString(block.get(KeyConstants._text, null), null);
			if (StringUtil.isEmpty(text, true)) return null;
			return new PartImpl(text, index);
		}

		if ("refusal".equals(type)) {
			String refusal = Caster.toString(block.get(KeyImpl.init("refusal"), null), null);
			if (StringUtil.isEmpty(refusal, true)) return null;
			return new PartImpl(refusal, index);
		}

		if ("image_url".equals(type) || "image".equals(type)) {
			return parseImagePart(block, index);
		}

		if ("audio".equals(type) || "input_audio".equals(type)) {
			return parseAudioPart(block, index);
		}

		if ("file".equals(type)) {
			return parseFilePart(block, index);
		}

		String fallback = Caster.toString(block.get(KeyConstants._text, null), null);
		if (!StringUtil.isEmpty(fallback, true)) return new PartImpl(fallback, index);

		return null;
	}

	private static Part parseImagePart(Struct block, int index) throws PageException {
		Struct image = Caster.toStruct(block.get("image_url", null), null);
		if (image == null) image = Caster.toStruct(block.get(KeyConstants._image, null), null);
		if (image == null) image = block;

		String url = Caster.toString(image.get(KeyConstants._url, null), null);
		if (StringUtil.isEmpty(url, true)) {
			url = Caster.toString(image.get("b64_json", null), null);
			if (!StringUtil.isEmpty(url, true)) {
				try {
					return new PartImpl(null, index, Base64Encoder.decode(url, false), "image/png");
				}
				catch (CoderException e) {
					throw Caster.toPageException(e);
				}
			}
			return null;
		}

		DataUrl dataUrl = parseDataUrl(url);
		if (dataUrl != null) return new PartImpl(null, index, dataUrl.data, dataUrl.mimeType);

		return new PartImpl(url, index);
	}

	private static Part parseAudioPart(Struct block, int index) throws PageException {
		Struct audio = Caster.toStruct(block.get(KeyImpl.init("input_audio"), null), null);
		if (audio == null) audio = Caster.toStruct(block.get(KeyImpl.init("audio"), null), null);
		if (audio == null) audio = block;

		String data = Caster.toString(audio.get(KeyConstants._data, null), null);
		if (!StringUtil.isEmpty(data, true)) {
			try {
				String mime = Caster.toString(audio.get("format", null), "audio/mpeg");
				if (!mime.contains("/")) mime = "audio/" + mime;
				return new PartImpl(null, index, Base64Encoder.decode(data, false), mime);
			}
			catch (CoderException e) {
				throw Caster.toPageException(e);
			}
		}

		String transcript = Caster.toString(audio.get("transcript", null), null);
		if (!StringUtil.isEmpty(transcript, true)) return new PartImpl(transcript, index);

		return null;
	}

	private static Part parseFilePart(Struct block, int index) throws PageException {
		Struct file = Caster.toStruct(block.get(KeyConstants._file, null), null);
		if (file == null) return null;

		String fileData = Caster.toString(file.get("file_data", null), null);
		if (!StringUtil.isEmpty(fileData, true)) {
			DataUrl dataUrl = parseDataUrl(fileData);
			if (dataUrl != null) return new PartImpl(null, index, dataUrl.data, dataUrl.mimeType);
		}

		String filename = Caster.toString(file.get(KeyConstants._filename, null), null);
		if (!StringUtil.isEmpty(filename, true)) return new PartImpl(filename, index);

		return null;
	}

	private static Object extractMessageContent(Struct raw) throws PageException {
		Struct message = extractMessage(raw);
		if (message == null) return null;
		return message.get(KeyConstants._content, null);
	}

	private static String extractMessageRefusal(Struct raw) throws PageException {
		Struct message = extractMessage(raw);
		if (message == null) return null;
		return Caster.toString(message.get(KeyImpl.init("refusal"), null), null);
	}

	private static Struct extractMessage(Struct raw) throws PageException {
		Array choices = Caster.toArray(raw.get("choices", null), null);
		if (choices == null || choices.size() == 0) return null;

		Struct choice = Caster.toStruct(choices.get(1, null), null);
		if (choice == null) return null;

		return Caster.toStruct(choice.get(KeyConstants._message, null), null);
	}

	private static DataUrl parseDataUrl(String url) {
		if (StringUtil.isEmpty(url, true) || !url.startsWith("data:")) return null;

		int comma = url.indexOf(',');
		if (comma < 0) return null;

		String header = url.substring(5, comma);
		String encoded = url.substring(comma + 1);
		if (StringUtil.isEmpty(encoded, true)) return null;

		String mimeType = "application/octet-stream";
		String payload = encoded;

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
			return new DataUrl(Base64Encoder.decode(payload, false), mimeType);
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
