package lucee.runtime.ai;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public class PartImpl implements Part {

	private final Struct metadata;
	private final int index;
	private final String contentType;
	private Object content;
	private String filename;

	public PartImpl(Struct metadata, int index, Object content, String contentType) {
		this.metadata = metadata;
		this.index = index;

		if (StringUtil.isEmpty(contentType)) {
			if (content instanceof CharSequence) {
				contentType = CONTENT_TYPE_TEXT;
			}
			else {
				try {
					contentType = IOUtil.getMimeType(Caster.toBinary(content), null);
				}
				catch (Exception e) {
					contentType = CONTENT_TYPE_BINARY;
				}
			}
		}
		this.contentType = contentType;
		this.content = content;
	}

	public PartImpl(String content, int index) {
		this(null, index, content, CONTENT_TYPE_TEXT);
	}

	public PartImpl(byte[] content, int index) {
		this(null, index, content, null);
	}

	@Override
	public final String getContentType() {
		return contentType;
	}

	@Override
	public String getAsString() {
		if (content instanceof String) {
			return (String) content;
		}
		else if (content instanceof byte[]) {
			try {
				return new String((byte[]) content, "UTF-8");
			}
			catch (Exception e) {
				return null;
			}
		}
		else if (content != null) {
			return content.toString();
		}
		return null;
	}

	@Override
	public byte[] getAsBinary() {
		if (content instanceof byte[]) {
			return (byte[]) content;
		}
		else if (content instanceof String) {
			try {
				return ((String) content).getBytes("UTF-8");
			}
			catch (Exception e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public Struct getAsStruct() {
		if (content instanceof Struct) {
			return (Struct) content;
		}
		return null;
	}

	@Override
	public final Struct getMetadata() {
		return metadata;
	}

	@Override
	public final int getIndex() {
		return index;
	}

	@Override
	public boolean isText() {
		return contentType != null && (contentType.startsWith("text/") || contentType.equals("application/json") || contentType.equals("application/xml"));
	}

	@Override
	public boolean isImage() {
		return contentType != null && contentType.startsWith("image/");
	}

	@Override
	public boolean isAudio() {
		return contentType != null && contentType.startsWith("audio/");
	}

	@Override
	public boolean isStructured() {
		return contentType != null && (contentType.equals("application/json") || contentType.equals("application/xml") || content instanceof Struct);
	}

	@Override
	public Object getContent() {
		return content;
	}

	public static List<Part> toParts(PageContext pc, Object[] objs) throws PageException {
		List<Part> parts = new ArrayList<>();
		for (int i = 0; i < objs.length; i++) {
			parts.add(toPart(pc, objs[i], i));
		}
		return parts;
	}

	private static Part toPart(PageContext pc, Object obj, int index) throws PageException {
		if (Decision.isStruct(obj)) {
			Struct sct = Caster.toStruct(obj);

			Struct metadata = Caster.toStruct(sct.get(KeyConstants._metadata, null));
			String contenttype = Caster.toString(sct.get(KeyConstants._contenttype, null), null);

			String path = Caster.toString(sct.get(KeyConstants._path, null), null);
			byte[] bin = null;
			if (path != null) {
				bin = Caster.toBinary(ResourceUtil.toResourceExisting(pc, path, null), null);
			}
			if (bin == null) {
				bin = Caster.toBinary(sct.get(KeyConstants._binary, null), null);
			}
			if (bin == null) {
				throw new ApplicationException("struct part missing valid data key (path or binary)");
			}
			return new PartImpl(metadata, index, bin, contenttype);

		}

		if (obj instanceof CharSequence) {
			return toPart(pc, obj.toString(), index);
		}

		return new PartImpl(Caster.toBinary(obj), index);
	}

	private static Part toPart(PageContext pc, String str, int index) throws PageException {
		// is the string maybe a path?
		// file
		if (str.length() < 4000) {
			Resource res = ResourceUtil.toResourceExisting(pc, str, (Resource) null);
			if (res != null) {
				pc.getConfig().getSecurityManager().checkFileLocation(res);
				try {

					return toPartImpl(res, index);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
			}
		}
		return new PartImpl(str, index);
	}

	public static PartImpl toPartImpl(Resource res, int index) throws IOException {
		String mt = IOUtil.getMimeType(res, null);
		// text
		if (mt != null && HTTPUtil.isTextMimeType(mt)) {
			return new PartImpl(null, index, IOUtil.toString(res, (Charset) null), mt);
		}
		return new PartImpl(null, index, IOUtil.toBytes(res), mt);
	}

	public static String getFileName(Part part) {
		if (part instanceof PartImpl) {
			return ((PartImpl) part).filename;
		}
		return null;
	}

}
