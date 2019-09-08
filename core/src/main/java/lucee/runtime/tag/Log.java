/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.tag;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.io.retirement.RetireOutputStream;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;

/**
 * Writes a message to a log file.
 *
 *
 *
 **/
public final class Log extends TagImpl {

	private static final String DEfAULT_LOG = "application";

	/**
	 * If you omit the file attribute, specifies the standard log file in which to write the message.
	 ** Ignored if you specify a file attribute
	 */
	private String log = DEfAULT_LOG;

	/** The message text to log. */
	private String text;

	/** The type or severity of the message. */
	private short type = lucee.commons.io.log.Log.LEVEL_INFO;
	/**  */
	private String file;
	private Throwable exception;

	/** Specifies whether to log the application name if one has been specified in an application tag. */
	private boolean application;
	private CharSet charset = null;

	private boolean async;

	@Override
	public void release() {
		super.release();
		log = DEfAULT_LOG;
		type = lucee.commons.io.log.Log.LEVEL_INFO;
		file = null;
		application = false;
		charset = null;
		exception = null;
		text = null;
		async = false;
	}

	/**
	 * set the value log If you omit the file attribute, specifies the standard log file in which to
	 * write the message. Ignored if you specify a file attribute
	 * 
	 * @param log value to set
	 * @throws ApplicationException
	 **/
	public void setLog(String log) throws ApplicationException {
		if (StringUtil.isEmpty(log, true)) return;
		this.log = log.trim();
		// throw new ApplicationException("invalid value for attribute log ["+log+"]","valid values are
		// [application, scheduler,console]");
	}

	/**
	 * set the value text The message text to log.
	 * 
	 * @param text value to set
	 **/
	public void setText(String text) {
		this.text = text;
	}

	public void setException(Object exception) throws PageException {
		this.exception = Throw.toPageException(exception, null);
		if (this.exception == null) throw new CasterException(exception, Exception.class);
	}

	/**
	 * set the value type The type or severity of the message.
	 * 
	 * @param type value to set
	 * @throws ApplicationException
	 **/
	public void setType(String type) throws ApplicationException {
		type = type.toLowerCase().trim();
		if (type.equals("information")) this.type = lucee.commons.io.log.Log.LEVEL_INFO;
		else if (type.equals("info")) this.type = lucee.commons.io.log.Log.LEVEL_INFO;
		else if (type.equals("warning")) this.type = lucee.commons.io.log.Log.LEVEL_WARN;
		else if (type.equals("warn")) this.type = lucee.commons.io.log.Log.LEVEL_WARN;
		else if (type.equals("error")) this.type = lucee.commons.io.log.Log.LEVEL_ERROR;
		else if (type.startsWith("fatal")) this.type = lucee.commons.io.log.Log.LEVEL_FATAL;
		else if (type.startsWith("debug")) this.type = lucee.commons.io.log.Log.LEVEL_DEBUG;
		else if (type.startsWith("trace")) this.type = lucee.commons.io.log.Log.LEVEL_TRACE;
		else throw new ApplicationException("invalid value for attribute type [" + type + "]", "valid values are [information,warning,error,fatal,debug]");

	}

	/**
	 * set the value time Specifies whether to log the system time.
	 * 
	 * @param time value to set
	 * @throws ApplicationException
	 **/
	public void setTime(boolean useTime) throws ApplicationException {
		if (useTime) return;
		// DeprecatedUtil.tagAttribute(pageContext,"Log", "time");
		throw new ApplicationException("attribute [time] for tag [log] is deprecated, only the value true is allowed");
	}

	/**
	 * set the value file
	 * 
	 * @param file value to set
	 * @throws ApplicationException
	 **/
	public void setFile(String file) throws ApplicationException {
		if (StringUtil.isEmpty(file)) return;

		if (file.indexOf('/') != -1 || file.indexOf('\\') != -1)
			throw new ApplicationException("value [" + file + "] from attribute [file] at tag [log] can only contain a filename, file separators like [\\/] are not allowed");
		if (!file.endsWith(".log")) file += ".log";
		this.file = file;
	}

	/**
	 * set the value date Specifies whether to log the system date.
	 * 
	 * @param date value to set
	 * @throws ApplicationException
	 **/
	public void setDate(boolean useDate) throws ApplicationException {
		if (useDate) return;
		// DeprecatedUtil.tagAttribute(pageContext,"Log", "date");
		throw new ApplicationException("attribute [date] for tag [log] is deprecated, only the value true is allowed");
	}

	/**
	 * set the value thread Specifies whether to log the thread ID. The thread ID identifies which
	 * internal service thread logged a message. Since a service thread normally services a CFML page
	 * request to completion, then moves on to the next queued request, the thread ID serves as a rough
	 * indication of which request logged a message. Leaving thread IDs turned on can help diagnose
	 * patterns of server activity.
	 * 
	 * @param thread value to set
	 * @throws ApplicationException
	 **/
	public void setThread(boolean thread) throws ApplicationException {
		if (thread) return;
		// DeprecatedUtil.tagAttribute(pageContext,"Log", "thread");
		throw new ApplicationException("attribute [thread] for tag [log] is deprecated, only the value true is allowed");
	}

	/**
	 * set the value application Specifies whether to log the application name if one has been specified
	 * in an application tag.
	 * 
	 * @param application value to set
	 **/
	public void setApplication(boolean application) {
		this.application = application;
	}

	// old function for backward compatibility
	public void setSpoolenable(boolean async) {
		setAsync(async);
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	@Override
	public int doStartTag() throws PageException {

		if (text == null && exception == null) throw new ApplicationException("Wrong Context, you must define one of the following attributes [text, exception]");
		PageContextImpl pci = (PageContextImpl) pageContext;
		ConfigImpl config = (ConfigImpl) pageContext.getConfig();
		lucee.commons.io.log.Log logger;
		if (file == null) {
			logger = pci.getLog(log.toLowerCase(), false);
			if (logger == null) {
				// for backward compatibility
				if ("console".equalsIgnoreCase(log))
					logger = ((ConfigImpl) pageContext.getConfig()).getLogEngine().getConsoleLog(false, "cflog", lucee.commons.io.log.Log.LEVEL_INFO);
				else {
					java.util.Collection<String> set = pci.getLogNames();
					Iterator<String> it = set.iterator();
					lucee.runtime.type.Collection.Key[] keys = new lucee.runtime.type.Collection.Key[set.size()];
					int index = 0;
					while (it.hasNext()) {
						keys[index++] = KeyImpl.init(it.next());
					}

					throw new ApplicationException(ExceptionUtil.similarKeyMessage(keys, log, "attribute log", "log names", null, true));
				}
			}
		}
		else {
			logger = getFileLog(pageContext, file, charset, async);
		}

		String contextName = pageContext.getApplicationContext().getName();
		if (contextName == null || !application) contextName = "";
		if (exception != null) {
			if (StringUtil.isEmpty(text)) logger.log(type, contextName, exception);
			else logger.log(type, contextName, text, exception);
		}
		else if (!StringUtil.isEmpty(text)) logger.log(type, contextName, text);
		else throw new ApplicationException("you must define attribute text or attribute exception with the tag cflog");
		// logger.write(toStringType(type),contextName,text);
		return SKIP_BODY;
	}

	private static lucee.commons.io.log.Log getFileLog(PageContext pc, String file, CharSet charset, boolean async) throws PageException {
		ConfigImpl config = (ConfigImpl) pc.getConfig();
		Resource logDir = config.getLogDirectory();
		Resource res = logDir.getRealResource(file);

		lucee.commons.io.log.Log log = FileLogPool.instance.get(res, CharsetUtil.toCharset(charset));
		if (log != null) return log;

		if (charset == null) charset = CharsetUtil.toCharSet(((PageContextImpl) pc).getResourceCharset());

		try {
			log = config.getLogEngine().getResourceLog(res, CharsetUtil.toCharset(charset), "cflog." + FileLogPool.toKey(file, CharsetUtil.toCharset(charset)),
					lucee.commons.io.log.Log.LEVEL_TRACE, 5, new Listener(FileLogPool.instance, res, charset), async);
			FileLogPool.instance.put(res, CharsetUtil.toCharset(charset), log);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return log;
	}

	/**
	 * @param charset the charset to set
	 */
	public void setCharset(String charset) {
		if (StringUtil.isEmpty(charset, true)) return;
		this.charset = CharsetUtil.toCharSet(charset);
	}

	private static class FileLogPool {

		private static FileLogPool instance = new FileLogPool();

		private static Map<String, lucee.commons.io.log.Log> logs = new ConcurrentHashMap<String, lucee.commons.io.log.Log>();

		public void retire(Resource res, Charset charset) {
			logs.remove(res.getAbsolutePath());
		}

		public void put(Resource res, Charset charset, lucee.commons.io.log.Log log) {
			logs.put(res.getAbsolutePath(), log);
		}

		public lucee.commons.io.log.Log get(Resource res, Charset charset) {
			lucee.commons.io.log.Log l = logs.get(res.getAbsolutePath());
			return l;
		}

		public static String toKey(String file, Charset charset) {
			if (charset == null) charset = CharsetUtil.UTF8;
			return StringUtil.toVariableName(file) + "." + StringUtil.toVariableName(charset.name());
		}
	}

	private static class Listener implements RetireListener {

		private FileLogPool pool;
		private Resource res;
		private CharSet charset;

		public Listener(FileLogPool pool, Resource res, CharSet charset) {
			this.pool = pool;
			this.res = res;
			this.charset = charset;
		}

		@Override
		public void retire(RetireOutputStream os) {
			pool.retire(res, CharsetUtil.toCharset(charset));
		}
	}
}