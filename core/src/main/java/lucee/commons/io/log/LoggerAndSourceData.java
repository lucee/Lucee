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
package lucee.commons.io.log;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.log4j.Log4jUtil;
import lucee.commons.io.log.log4j.LogAdapter;
import lucee.runtime.config.Config;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.engine.ThreadLocalPageContext;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * 
 */
public final class LoggerAndSourceData {
    
    private LogAdapter _log;
    private final ClassDefinition cdAppender;
    private Appender _appender;
	private final Map<String, String> appenderArgs;
	private final ClassDefinition cdLayout;
	private Layout layout;
	private final Map<String, String> layoutArgs;
	private final Level level;
	private final String name;
	private Config config;
	private final boolean readOnly;
	private final String id;

 
    public LoggerAndSourceData(Config config,String id,String name,ClassDefinition appender, Map<String, String> appenderArgs, ClassDefinition layout, Map<String, String> layoutArgs, Level level, boolean readOnly) {
    	//this.log=new LogAdapter(logger);
    	this.config=config;
    	this.id=id;
    	this.name=name;
    	this.cdAppender=appender;
    	this.appenderArgs=appenderArgs;
    	this.cdLayout=layout;
    	this.layoutArgs=layoutArgs;
    	this.level=level;
    	this.readOnly=readOnly;
    }

	public String id() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public ClassDefinition getAppenderClassDefinition() {
		return cdAppender;
	}
	
	public Appender getAppender() {
		getLog();// initilaize if necessary
		return _appender;
	}
	
	public void close() {
		if(_log!=null) {
			Appender a = _appender;
    		_log=null;
			layout = null;
    		if(a!=null)a.close();
    		_appender=null;
    	}
	}


	public Map<String, String> getAppenderArgs() {
		getLog();// initilaize if necessary
		return appenderArgs;
	}

	public Layout getLayout() {
		getLog();// initilaize if necessary
		return layout;
	}
	public ClassDefinition getLayoutClassDefinition() {
		return cdLayout;
	}

	public Map<String, String> getLayoutArgs() {
		getLog();// initilaize if necessary
		return layoutArgs;
	}

	public Level getLevel() {
		return level;
	}

	public boolean getReadOnly() {
		return readOnly;
	}

    public Log getLog() {
    	if(_log==null) {
    		config=ThreadLocalPageContext.getConfig(config);
    		layout = Log4jUtil.getLayout(cdLayout, layoutArgs);
    		_appender = Log4jUtil.getAppender(config, layout,name, cdAppender, appenderArgs);
    		_log=new LogAdapter(Log4jUtil.getLogger(config, _appender, name, level));
    	}
    	return _log;
    }
    
    public Logger getLogger() {
    	getLog();// make sure it exists
        return _log.getLogger();
    }

	public static String id(String name
			,ClassDefinition appender, Map<String, String> appenderArgs
			,ClassDefinition layout, Map<String, String> layoutArgs
			,Level level,
			boolean readOnly) {
		StringBuilder sb = new StringBuilder(name)
		.append(';')
		.append(appender)
		.append(';');
		toString(sb,appenderArgs);
		sb.append(';')
		.append(layout)
		.append(';');
		toString(sb,layoutArgs);
		sb.append(';')
		.append(level.toInt())
		.append(';')
		.append(readOnly);
		
		return HashUtil.create64BitHashAsString( sb.toString(),Character.MAX_RADIX);
	}

	private static void toString(StringBuilder sb, Map<String, String> map) {
		if(map==null) return;
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		Entry<String, String> e;
		while(it.hasNext()){
			e = it.next();
			sb.append(e.getKey()).append(':').append(e.getValue()).append('|');
		}
	}

    
}