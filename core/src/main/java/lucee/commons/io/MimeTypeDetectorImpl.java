package lucee.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.util.MimeTypeDetector;

/**
 * Built-in mime type detection, based on a file extension map and the magic bytes of common
 * formats. An extension (for example the Tika extension) can replace it with a more accurate
 * implementation.
 */
public final class MimeTypeDetectorImpl implements MimeTypeDetector {

	private static final String OCTET_STREAM = "application/octet-stream";
	private static final String TEXT_PLAIN = "text/plain";
	private static final String ZIP = "application/zip";
	private static final String OLE = "application/x-ole-storage";

	private static final int SNIFF_LENGTH = 1024;

	// common types that are not part of ResourceUtil.EXT_MT
	private static final Map<String, String> EXT_MT_EXTRA = new HashMap<String, String>();
	static {
		String[][] types = new String[][] { { "m4a", "audio/mp4" }, { "m4b", "audio/mp4" }, { "m4v", "video/x-m4v" }, { "aac", "audio/aac" }, { "flac", "audio/x-flac" },
				{ "ogg", "audio/ogg" }, { "oga", "audio/ogg" }, { "opus", "audio/ogg" }, { "weba", "audio/webm" }, { "mid", "audio/midi" }, { "midi", "audio/midi" },
				{ "ogv", "video/ogg" }, { "webm", "video/webm" }, { "mkv", "video/x-matroska" }, { "3gp", "video/3gpp" }, { "webp", "image/webp" }, { "svg", "image/svg+xml" },
				{ "ico", "image/vnd.microsoft.icon" }, { "avif", "image/avif" }, { "heic", "image/heic" }, { "jp2", "image/jp2" }, { "json", "application/json" },
				{ "xml", "application/xml" }, { "csv", "text/csv" }, { "tsv", "text/tab-separated-values" }, { "md", "text/markdown" }, { "mjs", "text/javascript" },
				{ "wasm", "application/wasm" }, { "woff", "font/woff" }, { "woff2", "font/woff2" }, { "ttf", "font/ttf" }, { "otf", "font/otf" },
				{ "eot", "application/vnd.ms-fontobject" }, { "gz", "application/gzip" }, { "tgz", "application/gzip" }, { "bz2", "application/x-bzip2" },
				{ "7z", "application/x-7z-compressed" }, { "rar", "application/vnd.rar" }, { "jar", "application/java-archive" }, { "class", "application/java-vm" },
				{ "ics", "text/calendar" }, { "vcf", "text/vcard" }, { "odt", "application/vnd.oasis.opendocument.text" },
				{ "ods", "application/vnd.oasis.opendocument.spreadsheet" }, { "odp", "application/vnd.oasis.opendocument.presentation" }, { "epub", "application/epub+zip" },
				{ "rss", "application/rss+xml" }, { "atom", "application/atom+xml" }, { "sql", "application/sql" }, { "log", "text/plain" }, { "ini", "text/plain" },
				{ "properties", "text/plain" }, { "java", "text/x-java-source" }, { "py", "text/x-python" }, { "cfc", "text/plain" } };
		for (String[] type: types) {
			EXT_MT_EXTRA.put(type[0], type[1]);
		}
	}

	private static final MimeTypeDetectorImpl INSTANCE = new MimeTypeDetectorImpl();

	public static MimeTypeDetectorImpl getInstance() {
		return INSTANCE;
	}

	private MimeTypeDetectorImpl() {
	}

	@Override
	public String getMimeType(byte[] barr, String defaultValue) {
		try {
			return sniff(barr, null, null);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	@Override
	public String getMimeType(InputStream is, String defaultValue) {
		try {
			return getMimeType(IOUtil.toBytesMax(is, SNIFF_LENGTH), defaultValue);
		}
		catch (IOException e) {
			return defaultValue;
		}
	}

	@Override
	public String getMimeType(String fileName, String defaultValue) {
		String mt = fromExtension(ResourceUtil.getExtension(fileName, null));
		return mt == null ? defaultValue : mt;
	}

	@Override
	public String getMimeType(Resource res, String defaultValue) {
		return getMimeType(res, null, defaultValue);
	}

	@Override
	public String getMimeType(Resource res, String fileName, String defaultValue) {
		String name = StringUtil.isEmpty(fileName, true) ? res.getName() : fileName.trim();
		String byName = fromExtension(ResourceUtil.getExtension(name, null));

		InputStream is = null;
		try {
			is = res.getInputStream();
			return sniff(IOUtil.toBytesMax(is, SNIFF_LENGTH), byName, name);
		}
		catch (Exception e) {
			return byName != null ? byName : defaultValue;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	@Override
	public String getMimeType(URL url, String defaultValue) {
		String byName = fromExtension(ResourceUtil.getExtension(url.getPath(), null));
		if (byName != null) return byName;

		InputStream is = null;
		try {
			is = url.openStream();
			return sniff(IOUtil.toBytesMax(is, SNIFF_LENGTH), null, null);
		}
		catch (Exception e) {
			return defaultValue;
		}
		finally {
			IOUtil.closeEL(is);
		}
	}

	private static String fromExtension(String ext) {
		if (StringUtil.isEmpty(ext, true)) return null;
		String key = ext.trim().toLowerCase(Locale.ROOT);
		String mt = ResourceUtil.EXT_MT.get(key);
		if (StringUtil.isEmpty(mt)) mt = EXT_MT_EXTRA.get(key);
		return StringUtil.isEmpty(mt) ? null : mt;
	}

	/**
	 * @param barr first bytes of the content
	 * @param byName mime type found by the extension, can be null
	 * @param name name of the file, can be null
	 */
	private static String sniff(byte[] barr, String byName, String name) {
		String magic = barr == null || barr.length == 0 ? null : magic(barr);

		// container formats (docx, jar, ...) are told apart by the extension
		if (magic != null && (ZIP.equals(magic) || OLE.equals(magic)) && byName != null) return byName;
		if (magic != null) return magic;
		if (byName != null) return byName;
		if (barr != null && barr.length > 0 && isText(barr)) return TEXT_PLAIN;
		return OCTET_STREAM;
	}

	private static String magic(byte[] b) {
		int len = b.length;

		if (len >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') return "application/pdf";
		if (len >= 4 && b[0] == '%' && b[1] == '!' && b[2] == 'P' && b[3] == 'S') return "application/postscript";
		if (len >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "image/png";
		if (len >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
		if (len >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8' && (b[4] == '7' || b[4] == '9') && b[5] == 'a') return "image/gif";
		if (len >= 2 && b[0] == 'B' && b[1] == 'M' && len > 14) return "image/bmp";
		if (len >= 4 && ((b[0] == 'I' && b[1] == 'I' && b[2] == 42 && b[3] == 0) || (b[0] == 'M' && b[1] == 'M' && b[2] == 0 && b[3] == 42))) return "image/tiff";
		if (len >= 4 && b[0] == 0 && b[1] == 0 && b[2] == 1 && b[3] == 0) return "image/vnd.microsoft.icon";
		if (len >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F') {
			if (b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "image/webp";
			if (b[8] == 'W' && b[9] == 'A' && b[10] == 'V' && b[11] == 'E') return "audio/vnd.wave";
			if (b[8] == 'A' && b[9] == 'V' && b[10] == 'I' && b[11] == ' ') return "video/x-msvideo";
		}
		if (len >= 4 && b[0] == 'P' && b[1] == 'K' && (b[2] == 3 || b[2] == 5 || b[2] == 7) && (b[3] == 4 || b[3] == 6 || b[3] == 8)) return ZIP;
		if (len >= 4 && (b[0] & 0xFF) == 0xD0 && (b[1] & 0xFF) == 0xCF && (b[2] & 0xFF) == 0x11 && (b[3] & 0xFF) == 0xE0) return OLE;
		if (len >= 2 && (b[0] & 0xFF) == 0x1F && (b[1] & 0xFF) == 0x8B) return "application/gzip";
		if (len >= 3 && b[0] == 'B' && b[1] == 'Z' && b[2] == 'h') return "application/x-bzip2";
		if (len >= 6 && b[0] == '7' && b[1] == 'z' && (b[2] & 0xFF) == 0xBC && (b[3] & 0xFF) == 0xAF) return "application/x-7z-compressed";
		if (len >= 6 && b[0] == 'R' && b[1] == 'a' && b[2] == 'r' && b[3] == '!') return "application/vnd.rar";
		if (len >= 262 && b[257] == 'u' && b[258] == 's' && b[259] == 't' && b[260] == 'a' && b[261] == 'r') return "application/x-tar";
		if (len >= 4 && (b[0] & 0xFF) == 0xCA && (b[1] & 0xFF) == 0xFE && (b[2] & 0xFF) == 0xBA && (b[3] & 0xFF) == 0xBE) return "application/java-vm";
		if (len >= 4 && b[0] == 'w' && b[1] == 'O' && b[2] == 'F' && (b[3] == 'F' || b[3] == '2')) return b[3] == 'F' ? "font/woff" : "font/woff2";
		if (len >= 3 && b[0] == 'I' && b[1] == 'D' && b[2] == '3') return "audio/mpeg";
		if (len >= 2 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xE0) == 0xE0) return "audio/mpeg";
		if (len >= 4 && b[0] == 'O' && b[1] == 'g' && b[2] == 'g' && b[3] == 'S') return "audio/ogg";
		if (len >= 4 && b[0] == 'f' && b[1] == 'L' && b[2] == 'a' && b[3] == 'C') return "audio/x-flac";
		if (len >= 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') {
			if (b[8] == 'q' && b[9] == 't') return "video/quicktime";
			if (b[8] == 'M' && b[9] == '4' && b[10] == 'A') return "audio/mp4";
			return "video/mp4";
		}
		if (len >= 4 && (b[0] & 0xFF) == 0x1A && (b[1] & 0xFF) == 0x45 && (b[2] & 0xFF) == 0xDF && (b[3] & 0xFF) == 0xA3) return "video/webm";
		if (len >= 5 && b[0] == '{' && b[1] == '\\' && b[2] == 'r' && b[3] == 't' && b[4] == 'f') return "application/rtf";
		if (len >= 16 && b[0] == 'S' && b[1] == 'Q' && b[2] == 'L' && b[3] == 'i' && b[4] == 't' && b[5] == 'e') return "application/vnd.sqlite3";

		// text based formats
		String start = startOfText(b);
		if (start != null) {
			if (start.startsWith("<?xml")) return start.indexOf("<svg") != -1 ? "image/svg+xml" : "application/xml";
			if (start.startsWith("<svg")) return "image/svg+xml";
			if (start.startsWith("<!doctype html") || start.startsWith("<html")) return "text/html";
		}
		return null;
	}

	private static String startOfText(byte[] b) {
		int off = 0;
		if (b.length >= 3 && (b[0] & 0xFF) == 0xEF && (b[1] & 0xFF) == 0xBB && (b[2] & 0xFF) == 0xBF) off = 3;
		while (off < b.length && (b[off] == ' ' || b[off] == '\t' || b[off] == '\r' || b[off] == '\n')) off++;
		if (off >= b.length || b[off] != '<') return null;
		int end = Math.min(b.length, off + 512);
		for (int i = off; i < end; i++) {
			if (b[i] == 0) return null;
		}
		return new String(b, off, end - off, java.nio.charset.StandardCharsets.ISO_8859_1).toLowerCase(Locale.ROOT);
	}

	private static boolean isText(byte[] b) {
		int len = Math.min(b.length, SNIFF_LENGTH);
		for (int i = 0; i < len; i++) {
			int c = b[i] & 0xFF;
			if (c == 0) return false;
			if (c < 32 && c != '\t' && c != '\n' && c != '\r' && c != '\f' && c != 27) return false;
		}
		return true;
	}
}
