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
package lucee.commons.io.log.sl4j;


import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.op.Caster;

import org.slf4j.Marker;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.spi.LocationAwareLogger;

public final class LoggerAdapterImpl extends MarkerIgnoringBase implements LocationAwareLogger {

	private static final long serialVersionUID = 3875268250734654111L;
	
	private Log logger;
	private String _name;

	public LoggerAdapterImpl(Log logger, String name){
		this.logger=logger;
		this._name=name;
	}
	
	@Override
	public void debug(String msg) {log(Log.LEVEL_DEBUG,msg);}
	@Override
	public void error(String msg) {log(Log.LEVEL_ERROR,msg);}
	@Override
	public void info(String msg) {log(Log.LEVEL_INFO,msg);}
	@Override
	public void trace(String msg) {}
	@Override
	public void warn(String msg) {log(Log.LEVEL_WARN,msg);}

	@Override
	public void debug(String format, Object arg) {log(Log.LEVEL_DEBUG,format,arg);}
	@Override
	public void error(String format, Object arg) {log(Log.LEVEL_ERROR,format,arg);}
	@Override
	public void info(String format, Object arg) {log(Log.LEVEL_INFO,format,arg);}
	@Override
	public void trace(String format, Object arg) {}
	@Override
	public void warn(String format, Object arg) {log(Log.LEVEL_WARN,format,arg);}

	@Override
	public void debug(String format, Object arg1, Object arg2) {log(Log.LEVEL_DEBUG,format,arg1,arg2);}
	@Override
	public void error(String format, Object arg1, Object arg2) {log(Log.LEVEL_DEBUG,format,arg1,arg2);}
	@Override
	public void info(String format, Object arg1, Object arg2) {log(Log.LEVEL_DEBUG,format,arg1,arg2);}
	@Override
	public void trace(String format, Object arg1, Object arg2) {}
	@Override
	public void warn(String format, Object arg1, Object arg2) {log(Log.LEVEL_DEBUG,format,arg1,arg2);}

	@Override
	public void debug(String format, Object[] args) {log(Log.LEVEL_DEBUG,format,args);}
	@Override
	public void error(String format, Object[] args) {log(Log.LEVEL_DEBUG,format,args);}
	@Override
	public void info(String format, Object[] args) {log(Log.LEVEL_DEBUG,format,args);}
	@Override
	public void trace(String format, Object[] args) {}
	@Override
	public void warn(String format, Object[] args) {log(Log.LEVEL_DEBUG,format,args);}
	
	@Override
	public void debug(String msg, Throwable t) {log(Log.LEVEL_DEBUG,msg,t);}
	@Override
	public void error(String msg, Throwable t) {log(Log.LEVEL_DEBUG,msg,t);}
	@Override
	public void info(String msg, Throwable t) {log(Log.LEVEL_DEBUG,msg,t);}
	@Override
	public void trace(String msg, Throwable t) {}
	@Override
	public void warn(String msg, Throwable t) {log(Log.LEVEL_DEBUG,msg,t);}
	
	private void log(int level, String msg) {
		logger.log(level, _name, msg);
	}
	private void log(int level, String msg, Throwable t) {
		logger.log(level, _name, msg,t);
	}
	
	private void log(int level, String format, Object arg) {
		log(level, Caster.toString(arg,""));
	}
	private void log(int level, String format, Object arg1, Object arg2) {
		log(level, Caster.toString(arg1,"")+"\n"+Caster.toString(arg2,""));
	}
	private void log(int level, String format, Object[] args) {
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<args.length;i++){
			sb.append(Caster.toString(args[i],""));
			sb.append("\n");
		}
		log(level, sb.toString().trim());
	}

	














	@Override
	public boolean isDebugEnabled() {
		return logger.getLogLevel()<=Log.LEVEL_DEBUG;
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.getLogLevel()<=Log.LEVEL_ERROR;
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.getLogLevel()<=Log.LEVEL_INFO;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.getLogLevel()<=Log.LEVEL_WARN;
	}











	public void log(Marker marker, String arg1, int arg2, String arg3,Throwable arg4) {
		// log(level, Caster.toString(arg1,"")+"\n"+Caster.toString(arg2,"")+"\n"+Caster.toString(arg3,""));
	}

	@Override
	public void log(Marker arg0, String arg1, int arg2, String arg3,
			Object[] arg4, Throwable arg5) {
		// TODO Auto-generated method stub
		
	}
  
}