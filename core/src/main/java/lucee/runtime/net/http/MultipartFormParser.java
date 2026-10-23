package lucee.runtime.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Iterates over the items of a multipart/form-data request (RFC 7578), a section of type
 * multipart/mixed inside of an item (RFC 1867) is flattened, its parts are items of the field of
 * the section.
 */
public final class MultipartFormParser {

	private final MultipartReader reader;
	private final Charset headerCharset;
	private final byte[] boundary;
	private String sectionFieldName;
	private Item current;

	public MultipartFormParser(InputStream in, String contentType, Charset headerCharset) throws IOException {
		if (contentType == null || !contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/")) {
			throw new MultipartReader.MalformedMultipartException("the request does not contain multipart content but [" + contentType + "]");
		}
		String boundary = getBoundary(contentType);
		if (boundary == null || boundary.isEmpty()) throw new MultipartReader.MalformedMultipartException("the request was rejected because no multipart boundary was found");
		this.headerCharset = headerCharset != null ? headerCharset : StandardCharsets.UTF_8;
		this.boundary = boundary.getBytes(StandardCharsets.ISO_8859_1);
		this.reader = new MultipartReader(in, this.boundary);
	}

	/**
	 * @return the next item, or null if there is none. The body of the previous item is not available
	 *         anymore
	 */
	public Item next() throws IOException {
		while (true) {
			if (!reader.nextPart()) {
				if (sectionFieldName == null) return null;
				// the end of a multipart/mixed section, the parts of the enclosing section follow
				sectionFieldName = null;
				reader.restoreBoundary(boundary);
				continue;
			}
			Map<String, String> headers = parseHeaders(reader.readHeaders(headerCharset));
			if (sectionFieldName == null) {
				String fieldName = getFieldName(headers);
				if (fieldName == null) continue; // no name, the part is skipped
				String contentType = headers.get("content-type");
				if (contentType != null && contentType.toLowerCase(Locale.ENGLISH).startsWith("multipart/mixed")) {
					String sub = getBoundary(contentType);
					if (sub == null || sub.isEmpty()) throw new MultipartReader.MalformedMultipartException("no boundary found in the content type [" + contentType + "]");
					sectionFieldName = fieldName;
					reader.setBoundary(sub.getBytes(StandardCharsets.ISO_8859_1));
					continue;
				}
				current = new Item(fieldName, getFileName(headers), contentType, headers, reader.getBody());
				return current;
			}
			// part of a multipart/mixed section, a part without a file name is skipped
			String fileName = getFileName(headers);
			if (fileName == null) continue;
			current = new Item(sectionFieldName, fileName, headers.get("content-type"), headers, reader.getBody());
			return current;
		}
	}

	public static final class Item {

		private final String fieldName;
		private final String fileName;
		private final String contentType;
		private final Map<String, String> headers;
		private final InputStream body;

		private Item(String fieldName, String fileName, String contentType, Map<String, String> headers, InputStream body) {
			this.fieldName = fieldName;
			this.fileName = fileName;
			this.contentType = contentType;
			this.headers = headers;
			this.body = body;
		}

		public String getFieldName() {
			return fieldName;
		}

		/**
		 * @return the name of the file as sent by the client, null if the item is a simple form field
		 */
		public String getName() throws IOException {
			if (fileName != null && fileName.indexOf('\0') != -1) {
				throw new MultipartReader.MalformedMultipartException("invalid file name: " + fileName.replace("\0", "\\0"));
			}
			return fileName;
		}

		public boolean isFormField() {
			return fileName == null;
		}

		public String getContentType() {
			return contentType;
		}

		/**
		 * @return the headers, the names are lower case
		 */
		public Map<String, String> getHeaders() {
			return headers;
		}

		public InputStream getInputStream() {
			return body;
		}

		/**
		 * the content as a string, the charset of the content type is used if none is given
		 */
		public String getString(Charset charset) throws IOException {
			java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
			byte[] b = new byte[8192];
			int r;
			while ((r = body.read(b)) != -1)
				baos.write(b, 0, r);
			return new String(baos.toByteArray(), charset != null ? charset : charsetOf(contentType));
		}
	}

	static Charset charsetOf(String contentType) {
		if (contentType != null) {
			String cs = parameters(contentType).get("charset");
			if (cs != null) {
				try {
					return Charset.forName(cs);
				}
				catch (Exception e) {
				}
			}
		}
		return StandardCharsets.ISO_8859_1;
	}

	private static String getBoundary(String contentType) {
		return parameters(contentType).get("boundary");
	}

	private static String getFieldName(Map<String, String> headers) {
		String name = dispositionParameter(headers, "name", false);
		return name == null || name.isEmpty() ? null : name;
	}

	private static String getFileName(Map<String, String> headers) {
		return dispositionParameter(headers, "filename", true);
	}

	private static String dispositionParameter(Map<String, String> headers, String name, boolean allowAttachment) {
		String disposition = headers.get("content-disposition");
		if (disposition == null) return null;
		String lower = disposition.toLowerCase(Locale.ENGLISH);
		if (!lower.startsWith("form-data") && !(allowAttachment && lower.startsWith("attachment"))) return null;
		return parameters(disposition).get(name);
	}

	/**
	 * the headers of a part, a folded line is unfolded and the values of a header that appears several
	 * times are joined with a comma
	 */
	static Map<String, String> parseHeaders(String raw) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		List<String> lines = new ArrayList<String>();
		for (String line: raw.split("\r\n", -1)) {
			if (line.isEmpty()) continue;
			if ((line.charAt(0) == ' ' || line.charAt(0) == '\t') && !lines.isEmpty()) {
				lines.set(lines.size() - 1, lines.get(lines.size() - 1) + " " + line.trim());
			}
			else lines.add(line);
		}
		for (String line: lines) {
			int index = line.indexOf(':');
			if (index <= 0) continue;
			String name = line.substring(0, index).trim().toLowerCase(Locale.ENGLISH);
			String value = line.substring(index + 1).trim();
			String existing = result.get(name);
			result.put(name, existing == null ? value : existing + "," + value);
		}
		return result;
	}

	/**
	 * the parameters of a header value like {@code form-data; name="a"; filename="b.txt"}, the names
	 * are lower case, the quotes around a value are removed. A name that ends with an asterisk
	 * ({@code filename*=UTF-8''%C3%BC.txt}, RFC 2231/5987) is decoded and takes precedence over the
	 * same name without an asterisk, a value with encoded words ({@code =?UTF-8?B?...?=}, RFC 2047)
	 * is decoded as well.
	 */
	static Map<String, String> parameters(String value) {
		Map<String, String> result = new LinkedHashMap<String, String>();
		Map<String, String> extended = new LinkedHashMap<String, String>();
		int len = value.length();
		int pos = value.indexOf(';');
		while (pos != -1 && pos < len) {
			pos++;
			// the end of this parameter, a semicolon inside of a quoted value does not count
			int end = pos;
			boolean quoted = false;
			boolean escaped = false;
			while (end < len) {
				char c = value.charAt(end);
				if (!quoted && c == ';') break;
				if (!escaped && c == '"') quoted = !quoted;
				escaped = !escaped && c == '\\';
				end++;
			}
			String param = value.substring(pos, end);
			int eq = param.indexOf('=');
			if (eq > 0) {
				String name = param.substring(0, eq).trim().toLowerCase(Locale.ENGLISH);
				String val = param.substring(eq + 1).trim();
				if (val.length() >= 2 && val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') val = val.substring(1, val.length() - 1);
				if (name.endsWith("*")) {
					name = name.substring(0, name.length() - 1);
					if (!name.isEmpty() && !extended.containsKey(name)) extended.put(name, decodeExtended(val));
				}
				else if (!name.isEmpty() && !result.containsKey(name)) result.put(name, decodeEncodedWords(val));
			}
			pos = end < len ? end : -1;
		}
		result.putAll(extended);
		return result;
	}

	/**
	 * RFC 2231/5987: {@code charset'language'percent-encoded-value}, a value that does not have this
	 * format or has an unknown charset is returned as it is
	 */
	static String decodeExtended(String value) {
		int first = value.indexOf('\'');
		if (first == -1) return value;
		int second = value.indexOf('\'', first + 1);
		if (second == -1) return value;
		Charset cs;
		try {
			cs = Charset.forName(value.substring(0, first));
		}
		catch (Exception e) {
			return value;
		}
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(value.length());
		for (int i = second + 1; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '%') {
				if (i + 2 >= value.length()) break; // unterminated sequence
				int hi = Character.digit(value.charAt(i + 1), 16);
				int lo = Character.digit(value.charAt(i + 2), 16);
				if (hi == -1 || lo == -1) {
					baos.write(c);
					continue;
				}
				baos.write((hi << 4) | lo);
				i += 2;
			}
			else baos.write((byte) c);
		}
		return new String(baos.toByteArray(), cs);
	}

	private static final java.util.regex.Pattern ENCODED_WORD = java.util.regex.Pattern.compile("=\\?([^?\\s]+)\\?([bBqQ])\\?([^?\\s]*)\\?=");
	private static final java.util.regex.Pattern WHITESPACE_BETWEEN_WORDS = java.util.regex.Pattern.compile("(\\?=)\\s+(?==\\?)");

	/**
	 * RFC 2047: {@code =?charset?B?base64?=} or {@code =?charset?Q?quoted-printable?=}, a word that
	 * cannot be decoded is kept as it is
	 */
	static String decodeEncodedWords(String value) {
		if (value.indexOf("=?") == -1) return value;
		// white space between two encoded words is not part of the text
		String text = WHITESPACE_BETWEEN_WORDS.matcher(value).replaceAll("$1");
		java.util.regex.Matcher m = ENCODED_WORD.matcher(text);
		StringBuilder sb = new StringBuilder(text.length());
		int last = 0;
		while (m.find()) {
			sb.append(text, last, m.start());
			String decoded = null;
			try {
				Charset cs = Charset.forName(m.group(1));
				byte[] bytes;
				if (m.group(2).equalsIgnoreCase("B")) bytes = java.util.Base64.getMimeDecoder().decode(m.group(3));
				else {
					String q = m.group(3);
					java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(q.length());
					for (int i = 0; i < q.length(); i++) {
						char c = q.charAt(i);
						if (c == '_') baos.write(' ');
						else if (c == '=' && i + 2 < q.length()) {
							int hi = Character.digit(q.charAt(i + 1), 16);
							int lo = Character.digit(q.charAt(i + 2), 16);
							if (hi == -1 || lo == -1) throw new IllegalArgumentException();
							baos.write((hi << 4) | lo);
							i += 2;
						}
						else baos.write((byte) c);
					}
					bytes = baos.toByteArray();
				}
				decoded = new String(bytes, cs);
			}
			catch (Exception e) {
			}
			sb.append(decoded != null ? decoded : m.group());
			last = m.end();
		}
		sb.append(text, last, text.length());
		return sb.toString();
	}
}
