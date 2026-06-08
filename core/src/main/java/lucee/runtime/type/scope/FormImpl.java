/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2016, Lucee Association Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.type.scope;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.io.FileCleaningTracker;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lucee.commons.collection.MapFactory;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ByteNameValuePair;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLItem;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.ServletInputStreamDummy;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.scope.jakarta.JakartaFileCleaner;
import lucee.runtime.type.scope.jakarta.JakartaServletDiskFileUpload;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * Form Scope
 */
public final class FormImpl extends ScopeSupport implements Form, ScriptProtected {

	private static final long serialVersionUID = -2618472604584253354L;

	private byte EQL = 61;
	private byte NL = 10;
	private byte AMP = 38;

	private Map<String, Item> _fileItems = MapFactory.<String, Item>getConcurrentMap();
	private Exception initException = null;

	private String encoding = null;
	private int scriptProtected = ScriptProtected.UNDEFINED;
	private static final URLItem[] empty = new URLItem[0];
	// private static final ResourceFilter FILTER = new ExtensionResourceFilter(".upload",false);
	private URLItem[] raw = empty;
	private static long count = 1;

	private static final int HEADER_TYPE_UNKNOWN = -1;
	private static final int HEADER_TEXT_PLAIN = 0;
	private static final int HEADER_MULTIPART_FORM_DATA = 1;
	private static final int HEADER_APP_URL_ENC = 2;

	private int headerType = HEADER_TYPE_UNKNOWN;
	private static boolean ALLOW_MIMETYPE_DETECTION;

	static {
		ALLOW_MIMETYPE_DETECTION = Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.fileupload.allow.mimetype.detection", null), true);
	}

	/**
	 * standart class Constructor
	 */
	public FormImpl() {
		super("form", SCOPE_FORM, Struct.TYPE_LINKED);
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(ApplicationContext ac, String encoding) throws UnsupportedEncodingException {
		encoding = encoding.trim().toUpperCase();
		if (encoding.equals(this.encoding)) return;
		this.encoding = encoding;
		if (!isInitalized()) return;

		fillDecoded(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(Scope.SCOPE_FORM), ac.getFormUrlAsStruct());
		setFieldNames();
	}

	@Override
	public void initialize(PageContext pc) {
		if (encoding == null) encoding = pc.getWebCharset().name();

		if (scriptProtected == ScriptProtected.UNDEFINED) {
			scriptProtected = ((pc.getApplicationContext().getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_FORM) > 0) ? ScriptProtected.YES : ScriptProtected.NO;
		}
		super.initialize(pc);

		String contentType = pc.getHttpServletRequest().getContentType();

		if (contentType == null) return;
		contentType = StringUtil.toLowerCase(contentType);
		if (contentType.startsWith("multipart/form-data")) {
			headerType = HEADER_MULTIPART_FORM_DATA;
			initializeMultiPart(pc, isScriptProtected());
		}
		else if (contentType.startsWith("text/plain")) {
			headerType = HEADER_TEXT_PLAIN;
			initializeUrlEncodedOrTextPlain(pc, '\n', isScriptProtected());
		}
		else if (contentType.startsWith("application/x-www-form-urlencoded")) {
			headerType = HEADER_APP_URL_ENC;
			initializeUrlEncodedOrTextPlain(pc, '&', isScriptProtected());
		}
		setFieldNames();
	}

	@Override
	public void reinitialize(ApplicationContext ac) {
		if (isInitalized()) {

			if (scriptProtected == ScriptProtected.UNDEFINED) {
				scriptProtected = ((ac.getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_FORM) > 0) ? ScriptProtected.YES : ScriptProtected.NO;
			}
			fillDecodedEL(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_FORM), ac.getFormUrlAsStruct());
			setFieldNames();
		}
	}

	void setFieldNames() {
		if (size() > 0) {
			setEL(KeyConstants._fieldnames, ListUtil.arrayToList(keys(), ","));
		}
	}

	private static DiskFileItemFactory getDiskFileItemFactory(PageContext pc) throws IOException {
		ServletContext servletContext = pc.getServletConfig().getServletContext();
		FileCleaningTracker fileCleaningTracker = JakartaFileCleaner.getFileCleaningTracker(servletContext);
		File tempDir = ResourceUtil.toFile(pc.getConfig().getTempDirectory());
		Object o = servletContext.getAttribute("jakarta.servlet.context.tempdir");
		if (o instanceof File) {
			tempDir = (File) o;
		}
		return DiskFileItemFactory.builder().setPath(tempDir.toPath()).setFileCleaningTracker(fileCleaningTracker).get();

	}

	private void initializeMultiPart(PageContext pc, boolean scriptProteced) {
		// get temp directory
		Resource tempDir = pc.getConfig().getTempDirectory();
		Resource tempFile;

		// Create a new file upload handler
		final String encoding = getEncoding();
		Charset cs = null;
		try {
			DiskFileItemFactory factory = getDiskFileItemFactory(pc);

			JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);
			if (!StringUtil.isEmpty(encoding, true)) {
				cs = CharsetUtil.toCharset(encoding.trim());
				upload.setHeaderCharset(cs);
			}

			HttpServletRequest req = pc.getHttpServletRequest();

			// Parse the request

			List<DiskFileItem> items = upload.parseRequest(req);

			// byte[] value;
			InputStream is;
			ArrayList<URLItem> list = new ArrayList<URLItem>();
			String fileName;
			for (DiskFileItem item: items) {

				// process Form Field
				if (item.isFormField() || StringUtil.isEmpty(item.getName())) {
					list.add(new URLItem(item.getFieldName(), cs != null ? item.getString(cs) : item.getString(), false));
				}
				// Process Uploaded File
				else {
					fileName = getFileName();
					tempFile = tempDir.getRealResource(fileName);
					item.write(ResourceUtil.toPath(tempFile));
					String ct = item.getContentType();
					if (ALLOW_MIMETYPE_DETECTION){
						if ( StringUtil.isEmpty(ct) && tempFile.length() > 0) {
							ct = IOUtil.getMimeType(tempFile, null);
						}
						else if ("application/octet-stream".equalsIgnoreCase(ct)) {
							ct = IOUtil.getMimeType(tempFile, ct);
						}
					}
					if (StringUtil.isEmpty(ct)) {
						is = tempFile.getInputStream();
						try {
							list.add(new URLItem(item.getFieldName(), new String(IOUtil.toBytes(is), encoding), false));
						}
						finally {
							IOUtil.close(is);
							tempFile.delete();
						}
					}
					else {
						String value = tempFile.toString();
						_fileItems.put(fileName, new Item(tempFile, ct, item.getName(), item.getFieldName()));
						list.add(new URLItem(item.getFieldName(), value, false));
					}
				}
			}

			raw = list.toArray(new URLItem[list.size()]);
			fillDecoded(raw, encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM), pc.getApplicationContext().getFormUrlAsStruct());
		}
		catch (Exception e) {
			LogUtil.log(pc, "application", "form.scope", e, Log.LEVEL_DEBUG);
			fillDecodedEL(new URLItem[0], encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM), pc.getApplicationContext().getFormUrlAsStruct());
			initException = e;
		}
	}

	private static String getFileName() {
		UUID uuid = UUID.randomUUID();
		String setUUID = uuid.toString();
		return "tmp-" + setUUID + ".upload";
	}

	/*
	 * private void initializeMultiPart(PageContext pc, boolean scriptProteced) {
	 * 
	 * File tempDir=FileWrapper.toFile(pc.getConfig().getTempDirectory());
	 * 
	 * // Create a factory for disk-based file items DiskFileItemFactory factory = new
	 * DiskFileItemFactory(-1,tempDir);
	 * 
	 * // Create a new file upload handler ServletFileUpload upload = new ServletFileUpload(factory);
	 * 
	 * upload.setHeaderEncoding(getEncoding());
	 * 
	 * //FileUpload fileUpload=new FileUpload(new DiskFileItemFactory(0,tempDir)); java.util.List list;
	 * try { list = upload.parseRequest(pc.getHttpServletRequest()); raw=new
	 * ByteNameValuePair[list.size()];
	 * 
	 * for(int i=0;i<raw.length;i++) { DiskFileItem val=(DiskFileItem) list.get(i);
	 * if(val.isFormField()) { raw[i]=new
	 * ByteNameValuePair(getBytes(val.getFieldName()),val.get(),false); } else {
	 * print.out("-------------------------------"); print.out("fieldname:"+val.getFieldName());
	 * print.out("name:"+val.getName()); print.out("formfield:"+val.isFormField());
	 * print.out("memory:"+val.isInMemory());
	 * print.out("exist:"+val.getStoreLocation().getCanonicalFile().exists());
	 * 
	 * fileItems.put(val.getFieldName().toLowerCase(),val);
	 * 
	 * raw[i]=new
	 * ByteNameValuePair(getBytes(val.getFieldName()),val.getStoreLocation().getCanonicalFile().toString
	 * ().getBytes(),false);
	 * //raw.put(val.getFieldName(),val.getStoreLocation().getCanonicalFile().toString()); } }
	 * fillDecoded(raw,encoding,scriptProteced); } catch (Exception e) {
	 * 
	 * //throw new PageRuntimeException(Caster.toPageException(e)); fillDecodedEL(new
	 * ByteNameValuePair[0],encoding,scriptProteced); initException=e; } }
	 */

	private void initializeUrlEncodedOrTextPlain(PageContext pc, char delimiter, boolean scriptProteced) {
		BufferedReader reader = null;
		try {
			reader = pc.getHttpServletRequest().getReader();
			raw = setFrom___(IOUtil.toString(reader, false), delimiter);
			fillDecoded(raw, encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM), pc.getApplicationContext().getFormUrlAsStruct());
		}
		catch (Exception e) {
			LogUtil.log(pc, "application", "form.scope", e, Log.LEVEL_DEBUG);
			fillDecodedEL(new URLItem[0], encoding, scriptProteced, pc.getApplicationContext().getSameFieldAsArray(SCOPE_FORM), pc.getApplicationContext().getFormUrlAsStruct());
			initException = e;
		}
		finally {
			try {
				IOUtil.close(reader);
			}
			catch (IOException e) {
				LogUtil.log(pc, "application", "form.scope", e, Log.LEVEL_DEBUG);
			}
		}
	}

	@Override
	public void release(PageContext pc) {
		super.release(pc);
		encoding = null;
		scriptProtected = ScriptProtected.UNDEFINED;
		raw = empty;

		if (!_fileItems.isEmpty()) {
			Iterator<Item> it = _fileItems.values().iterator();
			Item item;
			while (it.hasNext()) {
				item = it.next();
				item.getResource().delete();
			}
			_fileItems.clear();
		}
		initException = null;

	}

	@Override
	public FormItem[] getFileItems() {
		if (_fileItems == null || _fileItems.isEmpty()) return new FormImpl.Item[0];
		Iterator<Item> it = _fileItems.values().iterator();
		FormImpl.Item[] rtn = new FormImpl.Item[_fileItems.size()];
		int index = 0;
		while (it.hasNext()) {
			rtn[index++] = it.next();
		}
		return rtn;
	}

	public DiskFileItem getFileUpload(String key) {
		return null;
	}

	@Override
	public FormItem getUploadResource(String key) {

		final String keyC = makeComparable(key);

		if (_fileItems == null || _fileItems.isEmpty()) return null;

		Iterator<Entry<String, Item>> it = _fileItems.entrySet().iterator();
		Entry<String, Item> entry;
		Item item;
		while (it.hasNext()) {
			entry = it.next();
			item = entry.getValue();
			if (item.getFieldName().equalsIgnoreCase(keyC)) return item;

			// /file.tmp
			if (item.getResource().getAbsolutePath().equalsIgnoreCase(key)) return item;
			if (entry.getKey().equalsIgnoreCase(key)) return item;
		}
		return null;
	}

	private String makeComparable(String key) {
		key = StringUtil.trim(key, "");

		// form.x
		if (StringUtil.startsWithIgnoreCase(key, "form.")) key = key.substring(5).trim();

		// form . x
		try {
			Array array = ListUtil.listToArray(key, '.');
			if (array.size() > 1 && array.getE(1).toString().trim().equalsIgnoreCase("form")) {
				array.removeE(1);
				key = ListUtil.arrayToList(array, ".").trim();
			}
		}
		catch (PageException e) {
		}
		return key;
	}

	@Override
	public PageException getInitException() {
		if (initException != null) return Caster.toPageException(initException);
		return null;
	}

	@Override
	public void setScriptProtecting(ApplicationContext ac, boolean scriptProtected) {
		int _scriptProtected = scriptProtected ? ScriptProtected.YES : ScriptProtected.NO;
		if (isInitalized() && _scriptProtected != this.scriptProtected) {
			fillDecodedEL(raw, encoding, scriptProtected, ac.getSameFieldAsArray(SCOPE_FORM), ac.getFormUrlAsStruct());
			setFieldNames();
		}
		this.scriptProtected = _scriptProtected;
	}

	@Override
	public boolean isScriptProtected() {
		return scriptProtected == ScriptProtected.YES;
	}

	/**
	 * @return the raw
	 */
	public URLItem[] getRaw() {
		return raw;
	}

	public void addRaw(ApplicationContext ac, URLItem[] raw) {
		URLItem[] nr = new URLItem[this.raw.length + raw.length];
		for (int i = 0; i < this.raw.length; i++) {
			nr[i] = this.raw[i];
		}
		for (int i = 0; i < raw.length; i++) {
			nr[this.raw.length + i] = raw[i];
		}
		this.raw = nr;

		if (!isInitalized()) return;
		fillDecodedEL(this.raw, encoding, isScriptProtected(), ac != null ? ac.getSameFieldAsArray(SCOPE_FORM) : false, ac != null ? ac.getFormUrlAsStruct() : true);
		setFieldNames();
	}

	private class Item implements FormItem {
		Resource resource;
		String contentType;
		String name;
		private String fieldName;

		public Item(Resource resource, String contentType, String name, String fieldName) {
			this.fieldName = fieldName;
			this.name = name;
			this.resource = resource;
			this.contentType = contentType;
		}

		/**
		 * @return the resource
		 */
		@Override
		public Resource getResource() {
			return resource;
		}

		/**
		 * @return the contentType
		 */
		@Override
		public String getContentType() {
			return contentType;
		}

		/**
		 * @return the name
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @return the fieldName
		 */
		@Override
		public String getFieldName() {
			return fieldName;
		}
	}

	/**
	 * @return return content as a http header input stream
	 */
	@Override
	public ServletInputStream getInputStream() {

		if (headerType == HEADER_APP_URL_ENC) {
			return new ServletInputStreamDummy(toBarr(raw, AMP));
		}
		else if (headerType == HEADER_TEXT_PLAIN) {
			return new ServletInputStreamDummy(toBarr(raw, NL));
		}
		/*
		 * else if(headerType==HEADER_MULTIPART_FORM_DATA) { return new FormImplInputStream(this); // TODO }
		 */

		return new ServletInputStreamDummy(new byte[] {});
	}

	private byte[] toBarr(URLItem[] items, byte del) {

		ByteNameValuePair[] raw = new ByteNameValuePair[items.length];
		for (int i = 0; i < raw.length; i++) {
			try {
				raw[i] = new ByteNameValuePair(items[i].getName().getBytes("iso-8859-1"), items[i].getValue().getBytes("iso-8859-1"), items[i].isUrlEncoded());
			}
			catch (UnsupportedEncodingException e) {
			}
		}

		int size = 0;
		if (!ArrayUtil.isEmpty(raw)) {
			for (int i = 0; i < raw.length; i++) {
				size += raw[i].getName().length;
				size += raw[i].getValue().length;
				size += 2;
			}
			size--;
		}
		byte[] barr = new byte[size], bname, bvalue;
		int count = 0;

		for (int i = 0; i < raw.length; i++) {
			bname = raw[i].getName();
			bvalue = raw[i].getValue();
			// name
			for (int y = 0; y < bname.length; y++) {
				barr[count++] = bname[y];
			}
			barr[count++] = EQL;
			// value
			for (int y = 0; y < bvalue.length; y++) {
				barr[count++] = bvalue[y];
			}
			if (i + 1 < raw.length) barr[count++] = del;
		}
		return barr;
	}

}