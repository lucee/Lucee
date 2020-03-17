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
package lucee.commons.lang;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;

import lucee.commons.io.SystemUtil.TemplateLine;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.exp.NativeException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.util.CollectionUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public final class ExceptionUtil {

	public static String toString(StackTraceElement[] trace) {
		StringBuilder sb = new StringBuilder();
		// Print our stack trace
		for (StackTraceElement ste: trace)
			sb.append("\tat ").append(ste).append('\n');
		return sb.toString();
	}

	public static String getStacktrace(Throwable t, boolean addMessage) {
		return getStacktrace(t, addMessage, true);
	}

	public static String getStacktrace(Throwable t, boolean addMessage, boolean onlyLuceePart) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		String st = sw.toString();
		// shrink the stacktrace
		if (onlyLuceePart && st.indexOf("Caused by:") == -1) {
			int index = st.indexOf("lucee.loader.servlet.CFMLServlet.service(");
			if (index == -1) index = st.indexOf("lucee.runtime.jsr223.ScriptEngineImpl.eval(");

			if (index != -1) {
				index = st.indexOf(")", index + 1);
				if (index != -1) {
					st = st.substring(0, index + 1) + "\n...";
				}
			}
		}

		String msg = t.getMessage();
		if (addMessage && !StringUtil.isEmpty(msg) && !st.startsWith(msg.trim())) st = msg + "\n" + st;
		return st;

	}

	public static String getMessage(Throwable t) {
		String msg = t.getMessage();
		if (StringUtil.isEmpty(msg, true)) msg = t.getClass().getName();

		StringBuilder sb = new StringBuilder(msg);

		if (t instanceof PageException) {
			PageException pe = (PageException) t;
			String detail = pe.getDetail();
			if (!StringUtil.isEmpty(detail, true)) {
				sb.append('\n');
				sb.append(detail);
			}
		}
		return sb.toString();
	}

	public static PageException addHint(PageExceptionImpl pe, String hint) {
		pe.setAdditional(KeyConstants._Hint, hint);
		return pe;
	}

	/**
	 * creates a message for key not found with soundex check for similar key
	 * 
	 * @param _keys
	 * @param keyLabel
	 * @return
	 */
	public static String similarKeyMessage(Collection.Key[] _keys, String keySearched, String keyLabel, String keyLabels, String in, boolean listAll) {

		String inThe = StringUtil.isEmpty(in, true) ? "" : " in the " + in;

		boolean empty = _keys.length == 0;
		if (listAll && (_keys.length > 50 || empty)) {
			listAll = false;
		}

		String list = null;
		if (listAll) {
			Arrays.sort(_keys);
			list = ListUtil.arrayToList(_keys, ",");
		}

		String keySearchedSoundex = StringUtil.soundex(keySearched);

		for (int i = 0; i < _keys.length; i++) {

			String k = _keys[i].getString();
			if (StringUtil.soundex(k).equals(keySearchedSoundex)) {

				if (keySearched.equalsIgnoreCase(k)) continue; // must be a null value in a partial null-support environment

				String appendix;
				if (listAll) appendix = ". Here is a complete list of all available " + keyLabels + ": [" + list + "].";
				else if (empty) appendix = ". The structure is empty";
				else appendix = ".";

				return "The " + keyLabel + " [" + keySearched + "] does not exist" + inThe + ", but there is a similar " + keyLabel + " with name [" + _keys[i].getString()
						+ "] available" + appendix;
			}
		}
		String appendix;
		if (listAll) appendix = ", only the following " + keyLabels + " are available: [" + list + "].";
		else if (empty) appendix = ", the structure is empty";
		else appendix = ".";

		return "The " + keyLabel + " [" + keySearched + "] does not exist" + inThe + appendix;
	}

	public static String similarKeyMessage(Collection coll, String keySearched, String keyLabel, String keyLabels, String in, boolean listAll) {
		return similarKeyMessage(CollectionUtil.keys(coll), keySearched, keyLabel, keyLabels, in, listAll);
	}

	public static IOException toIOException(Throwable t) {
		rethrowIfNecessary(t);
		if (t instanceof IOException) return (IOException) t;
		if (t instanceof InvocationTargetException) return toIOException(((InvocationTargetException) t).getCause());
		if (t instanceof NativeException) return toIOException(((NativeException) t).getCause());

		IOException ioe = new IOException(t.getClass().getName() + ":" + t.getMessage());
		ioe.setStackTrace(t.getStackTrace());
		return ioe;
	}

	public static String createSoundexDetail(String name, Iterator<String> it, String keyName) {
		StringBuilder sb = new StringBuilder();
		String k, sname = StringUtil.soundex(name);
		while (it.hasNext()) {
			k = it.next();
			if (StringUtil.soundex(k).equals(sname)) return "did you mean [" + k + "]";
			if (sb.length() != 0) sb.append(',');
			sb.append(k);
		}
		return "available " + keyName + " are [" + sb + "]";
	}

	public static RuntimeException toRuntimeException(Throwable t) {
		rethrowIfNecessary(t);
		// TODO is there an improvement necessary?
		return new RuntimeException(t);
	}

	private static Throwable unwrap(Throwable t) {
		if (t == null) return t;
		if (t instanceof NativeException) return unwrap(((NativeException) t).getException());
		Throwable cause = t.getCause();
		if (cause != null && cause != t) return unwrap(cause);
		return t;
	}

	/**
	 * A java.lang.ThreadDeath must never be caught, so any catch(Throwable t) must go through this
	 * method in order to ensure that the throwable is not of type ThreadDeath
	 *
	 * @param t the thrown Throwable
	 */
	public static void rethrowIfNecessary(Throwable t) {
		if (unwrap(t) instanceof ThreadDeath) throw (ThreadDeath) t; // never catch a ThreadDeath
	}

	public static TemplateLine getThrowingPosition(PageContext pc, Throwable t) {
		Throwable cause = t.getCause();
		if (cause != null) getThrowingPosition(pc, cause);
		StackTraceElement[] traces = t.getStackTrace();

		String template;
		for (StackTraceElement trace: traces) {
			template = trace.getFileName();
			if (trace.getLineNumber() <= 0 || template == null || ResourceUtil.getExtension(template, "").equals("java")) continue;
			return new TemplateLine(abs((PageContextImpl) pc, template), trace.getLineNumber());
		}
		return null;
	}

	private static String abs(PageContextImpl pc, String template) {
		ConfigWeb config = pc.getConfig();

		Resource res = config.getResource(template);
		if (res.exists()) return template;

		PageSource ps = pc == null ? null : pc.getPageSource(template);
		res = ps == null ? null : ps.getPhyscalFile();
		if (res == null || !res.exists()) {
			res = config.getResource(ps.getDisplayPath());
			if (res != null && res.exists()) return res.getAbsolutePath();
		}
		else return res.getAbsolutePath();
		return template;
	}

}