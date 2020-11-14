/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.runtime.exp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.err.ErrorPage;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.writer.CFMLWriter;
import lucee.transformer.bytecode.util.SourceNameClassVisitor.SourceInfo;

/**
 * Page Exception, all runtime Exception are sub classes of this class
 */
public abstract class PageExceptionImpl extends PageException {

	private static final long serialVersionUID = -5816929795661373219L;

	private Array tagContext = new ArrayImpl();
	private Struct additional = new StructImpl(Struct.TYPE_LINKED);
	/**
	 * Field <code>detail</code>
	 */
	protected String detail = "";
	// private Throwable rootCause;
	private int tracePointer;
	private String errorCode = "0";
	private String extendedInfo = null;

	private String type;
	private String customType;
	private boolean isInitTagContext = false;
	private LinkedList<PageSource> sources = new LinkedList<PageSource>();
	private String varName;
	private boolean exposeMessage;

	/**
	 * Class Constructor
	 * 
	 * @param message Exception Message
	 * @param type Type as String
	 */
	public PageExceptionImpl(String message, String type) {
		this(message, type, null);
	}

	/**
	 * Class Constructor
	 * 
	 * @param message Exception Message
	 * @param type Type as String
	 * @param customType CUstom Type as String
	 */
	public PageExceptionImpl(String message, String type, String customType) {
		super(message == null ? "" : message);
		// rootCause=this;
		this.type = type.toLowerCase().trim();
		this.customType = customType;
		// setAdditional("customType",getCustomTypeAsString());
	}

	/**
	 * Class Constructor
	 * 
	 * @param e exception
	 * @param type Type as String
	 */
	public PageExceptionImpl(Throwable e, String type) {
		super(StringUtil.isEmpty(e.getMessage(), true) ? e.getClass().getName() : e.getMessage());
		if (e instanceof InvocationTargetException) e = ((InvocationTargetException) e).getTargetException();

		// Throwable cause = e.getCause();
		// if(cause!=null)initCause(cause);
		initCause(e);
		setStackTrace(e.getStackTrace());

		if (e instanceof IPageException) {
			IPageException pe = (IPageException) e;
			this.additional = pe.getAdditional();
			this.setDetail(pe.getDetail());
			this.setErrorCode(pe.getErrorCode());
			this.setExtendedInfo(pe.getExtendedInfo());
		}
		this.type = type.trim();
	}

	@Override
	public String getDetail() {
		if (detail == null || detail.equals(getMessage())) return "";
		return detail;
	}

	@Override
	public String getErrorCode() {
		return errorCode == null ? "" : errorCode;
	}

	@Override
	public String getExtendedInfo() {
		return extendedInfo == null ? "" : extendedInfo;
	}

	@Override
	public void setDetail(String detail) {
		this.detail = detail;
	}

	@Override
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public void setExtendedInfo(String extendedInfo) {
		this.extendedInfo = extendedInfo;
	}

	public final Struct getCatchBlock() {
		return getCatchBlock(ThreadLocalPageContext.getConfig());
	}

	@Override
	public final Struct getCatchBlock(PageContext pc) {
		return getCatchBlock(ThreadLocalPageContext.getConfig(pc));
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		return new CatchBlockImpl(this);
	}

	public Array getTagContext(Config config) {
		if (isInitTagContext) return tagContext;
		_getTagContext(config, tagContext, getStackTraceElements(this), sources);
		isInitTagContext = true;
		return tagContext;
	}

	public static Array getTagContext(Config config, StackTraceElement[] traces) {
		Array tagContext = new ArrayImpl();
		_getTagContext(config, tagContext, traces, new LinkedList<PageSource>());
		return tagContext;
	}

	private static void _getTagContext(Config config, Array tagContext, StackTraceElement[] traces, LinkedList<PageSource> sources) {
		// StackTraceElement[] traces = getStackTraceElements(t);

		int line = 0;
		String template = "", tlast;
		Struct item;
		StackTraceElement trace = null;
		int index = -1;
		PageSource ps;

		PageContextImpl pc = null;
		if (config instanceof ConfigWeb) pc = (PageContextImpl) ThreadLocalPageContext.get();

		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			tlast = template;
			template = trace.getFileName();

			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;
			// content
			if (!StringUtil.emptyIfNull(tlast).equals(template)) index++;

			String[] content = null;
			String dspPath = template;
			try {

				Resource res = config.getResource(template);

				if (!res.exists()) {
					PageSource _ps = pc == null ? null : pc.getPageSource(template);
					res = _ps == null ? null : _ps.getPhyscalFile();
					if (res == null || !res.exists()) {
						res = config.getResource(_ps.getDisplayPath());
						if (res != null && res.exists()) dspPath = res.getAbsolutePath();
					}
					else dspPath = res.getAbsolutePath();
				}
				else dspPath = res.getAbsolutePath();

				// class was not build on the local filesystem
				if (!res.exists()) {
					SourceInfo si = pc != null ? MappingUtil.getMatch(pc, trace) : MappingUtil.getMatch(config, trace);
					if (si != null && si.relativePath != null) {
						dspPath = si.relativePath;
						res = ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), si.relativePath, true, true);
						if (!res.exists()) {
							PageSource _ps = PageSourceImpl.best(config.getPageSources(ThreadLocalPageContext.get(), null, si.relativePath, false, false, true));
							if (_ps != null && _ps.exists()) {
								res = _ps.getResource();
								if (res != null && res.exists()) dspPath = res.getAbsolutePath();
							}
							else dspPath = res.getAbsolutePath();
						}
						else dspPath = res.getAbsolutePath();
					}
				}

				if (res.exists()) {
					InputStream is = res.getInputStream();
					if (ClassUtil.isBytecode(is)) {
						content = new String[] {}; // empty code array to show ??
					}
					else content = IOUtil.toStringArray(IOUtil.getReader(res, config.getTemplateCharset()));
					IOUtil.close(is);
				}
				else {
					if (sources.size() > index) ps = sources.get(index);
					else ps = null;

					if (ps != null && trace.getClassName().equals(ps.getClassName())) {
						if (ps.physcalExists()) content = IOUtil.toStringArray(IOUtil.getReader(ps.getPhyscalFile(), config.getTemplateCharset()));
						template = ps.getDisplayPath();
					}
				}
			}
			catch (Throwable th) {}

			// check last
			if (tagContext.size() > 0) {
				try {
					Struct last = (Struct) tagContext.getE(tagContext.size());
					if (last.get(KeyConstants._Raw_Trace).equals(trace.toString())) continue;
				}
				catch (Exception e) {}
			}

			item = new StructImpl();
			line = trace.getLineNumber();
			item.setEL(KeyConstants._template, dspPath);
			item.setEL(KeyConstants._line, new Double(line));
			item.setEL(KeyConstants._id, "??");
			item.setEL(KeyConstants._Raw_Trace, trace.toString());
			item.setEL(KeyConstants._type, "cfml");
			item.setEL(KeyConstants._column, new Double(0));
			if (content != null) {
				if (content.length > 0) {
					item.setEL(KeyConstants._codePrintHTML, getCodePrint(content, line, true));
					item.setEL(KeyConstants._codePrintPlain, getCodePrint(content, line, false));
				}
				else {
					item.setEL(KeyConstants._codePrintHTML, "??");
					item.setEL(KeyConstants._codePrintPlain, "??");
				}
			}
			else {
				item.setEL(KeyConstants._codePrintHTML, "");
				item.setEL(KeyConstants._codePrintPlain, "");
			}
			// FUTURE id
			tagContext.appendEL(item);
		}
	}

	public int getPageDeep() {
		StackTraceElement[] traces = getStackTraceElements(this);

		String template = "", tlast;
		StackTraceElement trace = null;
		int index = 0;
		for (int i = 0; i < traces.length; i++) {
			trace = traces[i];
			tlast = template;
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;
			if (!StringUtil.emptyIfNull(tlast).equals(template)) index++;

		}
		return index;
	}

	@Override
	public Struct getErrorBlock(PageContext pc, ErrorPage ep) {
		Struct struct = new StructImpl();

		struct.setEL(KeyConstants._browser, pc.cgiScope().get("HTTP_USER_AGENT", ""));
		struct.setEL("datetime", new DateTimeImpl(pc));
		struct.setEL("diagnostics", getMessage() + ' ' + getDetail() + "<br>The error occurred on line " + getLine(pc.getConfig()) + " in file " + getFile(pc.getConfig()) + ".");
		struct.setEL("GeneratedContent", getGeneratedContent(pc));
		struct.setEL("HTTPReferer", pc.cgiScope().get("HTTP_REFERER", ""));
		struct.setEL("mailto", ep.getMailto());
		struct.setEL(KeyConstants._message, getMessage());
		struct.setEL("QueryString", StringUtil.emptyIfNull(pc.getHttpServletRequest().getQueryString()));
		struct.setEL("RemoteAddress", pc.cgiScope().get("REMOTE_ADDR", ""));
		struct.setEL("RootCause", getCatchBlock(pc));
		struct.setEL("StackTrace", getStackTraceAsString());
		struct.setEL(KeyConstants._template, pc.getHttpServletRequest().getServletPath());

		struct.setEL(KeyConstants._Detail, getDetail());
		struct.setEL("ErrorCode", getErrorCode());
		struct.setEL("ExtendedInfo", getExtendedInfo());
		struct.setEL(KeyConstants._type, getTypeAsString());
		struct.setEL("TagContext", getTagContext(pc.getConfig()));
		struct.setEL("additional", additional);
		// TODO RootCause,StackTrace

		return struct;
	}

	private String getGeneratedContent(PageContext pc) {
		PageContextImpl pci = (PageContextImpl) pc;
		CFMLWriter ro = pci.getRootOut();
		String gc = ro.toString();
		try {
			ro.clearBuffer();
		}
		catch (IOException ioe) {}
		if (gc == null) return "";
		return gc;
	}

	/**
	 * @return return the file where the failure occurred
	 */
	private String getFile(Config config) {
		if (getTagContext(config).size() == 0) return "";

		Struct sct = (Struct) getTagContext(config).get(1, null);
		return Caster.toString(sct.get(KeyConstants._template, ""), "");
	}

	public String getLine(Config config) {
		if (getTagContext(config).size() == 0) return "";

		Struct sct = (Struct) getTagContext(config).get(1, null);
		return Caster.toString(sct.get(KeyConstants._line, ""), "");
	}

	@Override
	public void addContext(PageSource ps, int line, int column, StackTraceElement element) {
		if (line == -187) {
			sources.add(ps);
			return;
		}

		Struct struct = new StructImpl();
		// print.out(pr.getDisplayPath());
		try {
			String[] content = ps.getSource();
			struct.set(KeyConstants._template, ps.getDisplayPath());
			struct.set(KeyConstants._line, new Double(line));
			struct.set(KeyConstants._id, "??");
			struct.set(KeyConstants._Raw_Trace, (element != null) ? element.toString() : "");
			struct.set(KeyConstants._Type, "cfml");
			struct.set(KeyConstants._column, new Double(column));
			if (content != null) {
				struct.set(KeyConstants._codePrintHTML, getCodePrint(content, line, true));
				struct.set(KeyConstants._codePrintPlain, getCodePrint(content, line, false));
			}
			tagContext.append(struct);
		}
		catch (Exception e) {}
	}

	private static String getCodePrint(String[] content, int line, boolean asHTML) {
		StringBuilder sb = new StringBuilder();
		// bad Line
		for (int i = line - 2; i < line + 3; i++) {
			if (i > 0 && i <= content.length) {
				if (asHTML && i == line) sb.append("<b>");
				if (asHTML) sb.append(i + ": " + StringUtil.escapeHTML(content[i - 1]));
				else sb.append(i + ": " + (content[i - 1]));
				if (asHTML && i == line) sb.append("</b>");
				if (asHTML) sb.append("<br>");
				sb.append('\n');
			}
		}
		return sb.toString();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {

		// FFFFCF
		DumpTable htmlBox = new DumpTable("exception", "#ff9900", "#FFCC00", "#000000");
		htmlBox.setTitle(
				Constants.NAME + " [" + pageContext.getConfig().getFactory().getEngine().getInfo().getVersion() + "] - Error (" + StringUtil.ucFirst(getTypeAsString()) + ")");

		// Message
		htmlBox.appendRow(1, new SimpleDumpData("Message"), new SimpleDumpData(getMessage()));

		// Detail
		String detail = getDetail();
		if (!StringUtil.isEmpty(detail, true)) htmlBox.appendRow(1, new SimpleDumpData("Detail"), new SimpleDumpData(detail));

		// additional
		Iterator<Key> it = additional.keyIterator();
		Collection.Key k;
		while (it.hasNext()) {
			k = it.next();
			htmlBox.appendRow(1, new SimpleDumpData(k.getString()), new SimpleDumpData(additional.get(k, "").toString()));
		}

		Array tagContext = getTagContext(pageContext.getConfig());
		// Context MUSTMUST
		if (tagContext.size() > 0) {
			// Collection.Key[] keys=tagContext.keys();
			Iterator<Object> vit = tagContext.valueIterator();
			// Entry<Key, Object> te;
			DumpTable context = new DumpTable("#ff9900", "#FFCC00", "#000000");
			// context.setTitle("The Error Occurred in");
			// context.appendRow(0,new SimpleDumpData("The Error Occurred in"));
			context.appendRow(7, new SimpleDumpData(""), new SimpleDumpData("template"), new SimpleDumpData("line"));
			try {
				boolean first = true;
				while (vit.hasNext()) {
					Struct struct = (Struct) vit.next();
					context.appendRow(1, new SimpleDumpData(first ? "called from " : "occurred in"), new SimpleDumpData(struct.get(KeyConstants._template, "") + ""),
							new SimpleDumpData(Caster.toString(struct.get(KeyConstants._line, null))));
					first = false;
				}
				htmlBox.appendRow(1, new SimpleDumpData("Context"), context);

				// Code
				String strCode = ((Struct) tagContext.get(1, null)).get(KeyConstants._codePrintPlain, "").toString();
				String[] arrCode = ListUtil.listToStringArray(strCode, '\n');
				arrCode = ListUtil.trim(arrCode);
				DumpTable code = new DumpTable("#ff9900", "#FFCC00", "#000000");

				for (int i = 0; i < arrCode.length; i++) {
					code.appendRow(i == 2 ? 1 : 0, new SimpleDumpData(arrCode[i]));
				}
				htmlBox.appendRow(1, new SimpleDumpData("Code"), code);

			}
			catch (PageException e) {}
		}

		// Java Stacktrace
		String strST = getStackTraceAsString();
		String[] arrST = ListUtil.listToStringArray(strST, '\n');
		arrST = ListUtil.trim(arrST);
		DumpTable st = new DumpTable("#ff9900", "#FFCC00", "#000000");

		for (int i = 0; i < arrST.length; i++) {
			st.appendRow(i == 0 ? 1 : 0, new SimpleDumpData(arrST[i]));
		}
		htmlBox.appendRow(1, new SimpleDumpData("Java Stacktrace"), st);

		return htmlBox;
	}

	@Override
	public String getStackTraceAsString() {
		return getStackTraceAsString(ThreadLocalPageContext.get());
	}

	public String getStackTraceAsString(PageContext pc) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		printStackTrace(pw);
		pw.flush();
		return sw.toString();
	}

	@Override
	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PageContext pc) {
		printStackTrace(System.err, pc);
	}

	@Override
	public void printStackTrace(PrintStream s) {
		printStackTrace(s, ThreadLocalPageContext.get());
	}

	public void printStackTrace(PrintStream s, PageContext pc) {
		super.printStackTrace(s);

		/*
		 * StackTraceElement[] traces = getStackTraceElements(this); StackTraceElement trace;
		 * 
		 * s.println(getMessage()); for(int i=0;i<traces.length;i++){ trace=traces[i];
		 * s.println("\tat "+toString(pc,trace)+":"+trace.getLineNumber()); }
		 */
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		printStackTrace(s, ThreadLocalPageContext.get());
	}

	public void printStackTrace(PrintWriter s, PageContext pc) {
		super.printStackTrace(s);
		/*
		 * StackTraceElement[] traces = getStackTraceElements(this); StackTraceElement trace;
		 * 
		 * s.println(getMessage()); for(int i=0;i<traces.length;i++){ trace=traces[i];
		 * s.println("\tat "+toString(pc,trace)+":"+trace.getLineNumber()); }
		 */
	}

	public static String toString(PageContext pc, StackTraceElement trace) {
		String path = null;

		if (trace.getFileName() == null || trace.getFileName().endsWith(".java")) return trace.toString();
		Config config = ThreadLocalPageContext.getConfig(pc);
		if (config != null) {
			Resource res = pc.getConfig().getResource(trace.getFileName());
			if (res.exists()) path = trace.getFileName();

			// get path from source
			if (path == null) {
				SourceInfo si = MappingUtil.getMatch(pc, trace);
				if (si != null) {
					if (si.absolutePath(pc) != null) {
						res = pc.getConfig().getResource(si.absolutePath(pc));
						if (res.exists()) path = si.absolutePath(pc);
					}
					if (path == null && si.relativePath != null) path = si.relativePath;
				}
				if (path == null) path = trace.getFileName();
			}
		}
		return trace.getClassName() + "." + trace.getMethodName() + (trace.isNativeMethod() ? "(Native Method)"
				: (path != null && trace.getLineNumber() >= 0 ? "(" + path + ":" + trace.getLineNumber() + ")" : (path != null ? "(" + path + ")" : "(Unknown Source)")));

	}

	private static StackTraceElement[] getStackTraceElements(Throwable t) {
		StackTraceElement[] st = getStackTraceElements(t, true);
		if (st == null) st = getStackTraceElements(t, false);
		return st;
	}

	private static StackTraceElement[] getStackTraceElements(Throwable t, boolean onlyWithCML) {
		StackTraceElement[] st;
		Throwable cause = t.getCause();
		if (cause != null) {
			st = getStackTraceElements(cause, onlyWithCML);
			if (st != null) return st;
		}

		st = t.getStackTrace();
		if (!onlyWithCML || hasCFMLinStacktrace(st)) {
			return st;
		}
		return null;
	}

	private static boolean hasCFMLinStacktrace(StackTraceElement[] traces) {
		for (int i = 0; i < traces.length; i++) {
			if (traces[i].getFileName() != null && !traces[i].getFileName().endsWith(".java")) return true;
		}
		return false;
	}
	/*
	 * ths code has produced duplettes private static void
	 * fillStackTraceElements(ArrayList<StackTraceElement> causes, Throwable t) { if(t==null) return;
	 * fillStackTraceElements(causes, t.getCause()); StackTraceElement[] traces = t.getStackTrace();
	 * for(int i=0;i<traces.length;i++) { //if(causes.contains(traces[i])) causes.add(traces[i]); } }
	 */

	/**
	 * set an additional key value
	 * 
	 * @param key
	 * @param value
	 */
	public void setAdditional(Collection.Key key, Object value) {
		additional.setEL(key, StringUtil.toStringEmptyIfNull(value));
	}

	@Override
	public Throwable getRootCause() {
		Throwable cause = this;
		Throwable temp;

		while ((temp = cause.getCause()) != null)
			cause = temp;
		return cause;
	}

	@Override
	public int getTracePointer() {
		return tracePointer;
	}

	@Override
	public void setTracePointer(int tracePointer) {
		this.tracePointer = tracePointer;
	}

	@Override
	public boolean typeEqual(String type) {
		if (type == null) return true;
		type = StringUtil.toUpperCase(type);
		// ANY
		if (type.equals("ANY")) return true;// MUST check
		// Type Compare
		if (getTypeAsString().equalsIgnoreCase(type)) return true;
		return getClass().getName().equalsIgnoreCase(type);
	}

	@Override
	public String getTypeAsString() {
		return type;
	}

	public String getType() { // for compatibility to ACF
		return type;
	}

	@Override
	public String getCustomTypeAsString() {
		return customType == null ? type : customType;
	}

	@Override
	public Struct getAdditional() {
		return additional;
	}

	@Override
	public Struct getAddional() {
		return additional;
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		return super.getStackTrace();
	}

	@Override
	public void setExposeMessage(boolean exposeMessage) {
		this.exposeMessage = exposeMessage;
	}

	@Override
	public boolean getExposeMessage() {
		return exposeMessage;
	}
}