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
package lucee.commons.io.log;

/**
 * Helper class for the logs
 */
public final class LogUtil {

	/*public static void log(Log log, int level, String logName, Throwable t) { 
		log(log,level,logName,"",t);
	}

	public static void log(Log log, int level, String logName,String msg, Throwable t) { 
		if(log instanceof LogAdapter) {
			log.log(level, logName, msg,t);
		}
		else {
			String em = ExceptionUtil.getMessage(t);
			String est = ExceptionUtil.getStacktrace(t, false);
			if(msg.equals(em)) msg=em+";"+est;
			else msg+=";"+em+";"+est;
			
			if(log!=null) {
				log.log(level, logName,msg);
			}
			else {
				PrintStream ps=(level>=Log.LEVEL_WARN)?System.err:System.out;
				ps.println(logName+";"+msg);
			}

		}
	}

	public static void log(Log log, int level, String logName, String msg, StackTraceElement[] stackTrace) {
		Throwable t = new Throwable();
		t.setStackTrace(stackTrace);
		log(log,level,logName,msg,t);
	}

	public static Log getLog(PageContext pc, String name) {
		return ((PageContextImpl)pc).getLog(name);
	}
	
	public static Log getLog(PageContext pc, String name, boolean createIfNecessary) {
		return ((PageContextImpl)pc).getLog(name,createIfNecessary);
	}*/
	
	public static int toLevel(String strLevel, int defaultValue) {
        if(strLevel==null) return defaultValue;
        strLevel=strLevel.toLowerCase().trim();
        if(strLevel.startsWith("info")) return Log.LEVEL_INFO;
        if(strLevel.startsWith("debug")) return Log.LEVEL_DEBUG;
        if(strLevel.startsWith("warn")) return Log.LEVEL_WARN;
        if(strLevel.startsWith("error")) return Log.LEVEL_ERROR;
        if(strLevel.startsWith("fatal")) return Log.LEVEL_FATAL;
        if(strLevel.startsWith("trace")) return Log.LEVEL_TRACE;
        return defaultValue;
    }
	
	public static String levelToString(int level,String defaultValue) {
        if(Log.LEVEL_INFO==level) return "info";
        if(Log.LEVEL_DEBUG==level) return "debug";
        if(Log.LEVEL_WARN==level) return "warn";
        if(Log.LEVEL_ERROR==level) return "error";
        if(Log.LEVEL_FATAL==level) return "fatal";
        if(Log.LEVEL_TRACE==level) return "trace";
        
		return defaultValue;
    }
}