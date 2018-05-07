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
package lucee.commons.io.log.log4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.log4j.appender.ConsoleAppender;
import lucee.commons.io.log.log4j.appender.DatasourceAppender;
import lucee.commons.io.log.log4j.appender.RollingResourceAppender;
import lucee.commons.io.log.log4j.appender.TaskAppender;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.log.log4j.layout.DatasourceLayout;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.retirement.RetireListener;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.reflection.Reflector;
import lucee.transformer.library.ClassDefinitionImpl;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.XMLLayout;

public class Log4jUtil {
	
	public static final long MAX_FILE_SIZE=1024*1024*10;
    public static final int MAX_FILES=10;
	private static final String DEFAULT_PATTERN = "%d{dd.MM.yyyy HH:mm:ss,SSS} %-5p [%c] %m%n"; 



	public static Logger getResourceLog(Config config, Resource res, Charset charset, String name, Level level, int timeout,RetireListener listener, boolean async) throws IOException {
		Appender a = new RollingResourceAppender(
				new ClassicLayout()
				,res
				,charset
				,true
				,RollingResourceAppender.DEFAULT_MAX_FILE_SIZE
				,RollingResourceAppender.DEFAULT_MAX_BACKUP_INDEX
				,timeout,listener); // no open stream at all
		
		if(async) {
			a=new TaskAppender(config, a);
		}
		
		
		return getLogger(config, a, name, level);
	}

	public static Logger getConsoleLog(Config config, boolean errorStream, String name, Level level) {
		// Printwriter
		PrintWriter pw=errorStream?config.getErrWriter():config.getOutWriter();
		if(pw==null)pw=new PrintWriter(errorStream?System.err:System.out);
		
		return getLogger(config, new ConsoleAppender(pw,new PatternLayout(DEFAULT_PATTERN)), name, level);
	}
	
	public static final Logger getLogger(Config config,Appender appender, String name, Level level){
		// fullname
		String fullname=name;
		if(config instanceof ConfigWeb) {
	    	ConfigWeb cw=(ConfigWeb) config;
	    	fullname="web."+cw.getLabel()+"."+name;
	    }
		else fullname="server."+name;
		
		Logger l = LogManager.exists(fullname);
		if(l!=null) l.removeAllAppenders();
		else l = LogManager.getLogger(fullname);
		l.setAdditivity(false);
    	l.addAppender(appender);
    	l.setLevel(level);
    	return l;
	}
	
    
    public static final Appender getAppender(Config config,Layout layout,String name,ClassDefinition cd, Map<String, String> appenderArgs){
    	if(appenderArgs==null)appenderArgs=new HashMap<String, String>();
    	// Appender
		Appender appender=null;
		if(cd!=null && cd.hasClass()) {
			// Console Appender
			if(ConsoleAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {
				// stream-type
				boolean doError=false;
				String st = Caster.toString(appenderArgs.get("streamtype"),null);
				if(!StringUtil.isEmpty(st,true)) {
					st=st.trim().toLowerCase();
					if(st.equals("err") || st.equals("error"))
						doError=true;
				}
				appenderArgs.put("streamtype",doError?"error":"output");
				
				
				// get print writer
				PrintWriter pw;
				if(doError) {
					if(config.getErrWriter()==null)pw=new PrintWriter(System.err);
					else pw=config.getErrWriter();
				} 
				else {
					if(config.getOutWriter()==null)pw=new PrintWriter(System.out);
					else pw=config.getOutWriter();
				}
				appender = new ConsoleAppender(pw,layout);
			}
			else if(DatasourceAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {
				
				// datasource
				String dsn = Caster.toString(appenderArgs.get("datasource"),null);
				if(StringUtil.isEmpty(dsn,true)) 
					dsn = Caster.toString(appenderArgs.get("datasourceName"),null);
				if(!StringUtil.isEmpty(dsn,true)) dsn=dsn.trim();
				appenderArgs.put("datasource",dsn);
				
				// username
				String user = Caster.toString(appenderArgs.get("username"),null);
				if(StringUtil.isEmpty(user,true)) 
					user = Caster.toString(appenderArgs.get("user"),null);
				if(!StringUtil.isEmpty(user,true)) user=user.trim();
				else user=null;
				appenderArgs.put("username",user);

				// password
				String pass = Caster.toString(appenderArgs.get("password"),null);
				if(StringUtil.isEmpty(pass,true)) 
					pass = Caster.toString(appenderArgs.get("pass"),null);
				if(!StringUtil.isEmpty(pass,true)) pass=pass.trim();
				else pass=null;
				appenderArgs.put("password",pass);
				
				// table
				String table = Caster.toString(appenderArgs.get("table"),null);
				if(!StringUtil.isEmpty(table,true)) table=table.trim();
				else table="LOGS";
				appenderArgs.put("table",table);
				
				// custom
				String custom = Caster.toString(appenderArgs.get("custom"),null);
				if(!StringUtil.isEmpty(custom,true)) custom=custom.trim();
				else custom=null;
				appenderArgs.put("custom",custom);
				
				try {
					appender = new DatasourceAppender(config, layout, dsn, user, pass,table,custom);
				} catch (PageException e) {
					SystemOut.printDate(e);
					appender = null;
				}
			}
			else if(RollingResourceAppender.class.getName().equalsIgnoreCase(cd.getClassName())) {
				
				// path
				Resource res=null;
				String path = Caster.toString(appenderArgs.get("path"),null);
				if(!StringUtil.isEmpty(path,true)) {
					path=path.trim();
					path=ConfigWebUtil.translateOldPath(path);
					res=ConfigWebUtil.getFile(config, config.getConfigDir(),path, ResourceUtil.TYPE_FILE);
					if(res.isDirectory()) {
						res=res.getRealResource(name+".log");
					}
				}
				if(res==null) {
					res=ConfigWebUtil.getFile(config, config.getConfigDir(),"logs/"+name+".log", ResourceUtil.TYPE_FILE);
				}
				
				
				// charset
				Charset charset = CharsetUtil.toCharset(Caster.toString(appenderArgs.get("charset"),null),null);
				if(charset==null){
					charset=config.getResourceCharset();
					appenderArgs.put("charset",charset.name());
				}
				
				// maxfiles
				int maxfiles = Caster.toIntValue(appenderArgs.get("maxfiles"),10);
				appenderArgs.put("maxfiles",Caster.toString(maxfiles));
				
				// maxfileSize
				long maxfilesize = Caster.toLongValue(appenderArgs.get("maxfilesize"),1024*1024*10);
				appenderArgs.put("maxfilesize",Caster.toString(maxfilesize));
				
				// timeout
				int timeout = Caster.toIntValue(appenderArgs.get("timeout"),60); // timeout in seconds
				appenderArgs.put("timeout",Caster.toString(timeout));
				
				try {
					appender=new RollingResourceAppender(layout,res,charset,true,maxfilesize,maxfiles,timeout,null);
				}
				catch (IOException e) {
					SystemOut.printDate(e);
				}
			}
			// class definition
			else {
				Object obj = ClassUtil.loadInstance(cd.getClazz(null),null,null);
				if(obj instanceof Appender) {
					appender=(Appender) obj;
					AppenderSkeleton as=obj instanceof AppenderSkeleton?(AppenderSkeleton)obj:null;
					Iterator<Entry<String, String>> it = appenderArgs.entrySet().iterator();
					Entry<String, String> e;
					String n;
					while(it.hasNext()){
						e = it.next();
						n=e.getKey();
						if(as!=null) {
							if("threshold".equalsIgnoreCase(n)) {
								Level level = Level.toLevel(e.getValue(),null);
								if(level!=null) {
									as.setThreshold(level);
									continue;
								}
							}
						}
						
						try {
							Reflector.callSetter(obj, e.getKey(), e.getValue());
						}
						catch (PageException e1) {
							SystemOut.printDate(e1); // TODO log
						}
					}
				}
			}
		}
		if(appender instanceof AppenderSkeleton) {
			((AppenderSkeleton)appender).activateOptions();
		}
		else if(appender==null) {
			PrintWriter pw;
			if(config.getOutWriter()==null)pw=new PrintWriter(System.out);
			else pw=config.getOutWriter();
			appender=new ConsoleAppender(pw,layout);
		}
		
		return appender;
    }
    

    public static ClassDefinition<Appender> appenderClassDefintion(String className) {
    	if("console".equalsIgnoreCase(className))return new ClassDefinitionImpl( ConsoleAppender.class);
    	if("resource".equalsIgnoreCase(className))return new ClassDefinitionImpl( RollingResourceAppender.class);
    	if("datasource".equalsIgnoreCase(className))return new ClassDefinitionImpl( DatasourceAppender.class);
    		
    	return new ClassDefinitionImpl( className);
    }
    
    public static final Layout getLayout(ClassDefinition cd, Map<String, String> layoutArgs, ClassDefinition cdAppender, String name) {
    	if(layoutArgs==null)layoutArgs=new HashMap<String, String>();
    	
    	// Layout
		Layout layout=null;
		
		if(cd!=null && cd.hasClass()) {
			// Classic Layout
			if(ClassicLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				layout=new ClassicLayout();
			}
			// Datasource Layout
			else if(DatasourceLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				layout=new DatasourceLayout(name);
			}
			// HTML Layout
			else if(HTMLLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				HTMLLayout html = new HTMLLayout();
				layout=html;
				
				// Location Info
				Boolean locInfo = Caster.toBoolean(layoutArgs.get("locationinfo"),null);
				if(locInfo!=null) html.setLocationInfo(locInfo.booleanValue());
				else locInfo=Boolean.FALSE;
				layoutArgs.put("locationinfo", locInfo.toString());
				
				// Title
				String title = Caster.toString(layoutArgs.get("title"),"");
				if(!StringUtil.isEmpty(title,true)) html.setTitle(title);
				layoutArgs.put("title", title);
				
			}
			// XML Layout
			else if(XMLLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				XMLLayout xml = new XMLLayout();
				layout=xml;
	
				// Location Info
				Boolean locInfo = Caster.toBoolean(layoutArgs.get("locationinfo"),null);
				if(locInfo!=null) xml.setLocationInfo(locInfo.booleanValue());
				else locInfo=Boolean.FALSE;
				layoutArgs.put("locationinfo", locInfo.toString());
				
				// Properties
				Boolean props = Caster.toBoolean(layoutArgs.get("properties"),null);
				if(props!=null) xml.setProperties(props.booleanValue());
				else props=Boolean.FALSE;
				layoutArgs.put("properties", props.toString());
				
			}
			// Pattern Layout
			else if(PatternLayout.class.getName().equalsIgnoreCase(cd.getClassName())) {
				PatternLayout patt = new PatternLayout();
				layout=patt;
				
				// pattern
				String pattern = Caster.toString(layoutArgs.get("pattern"),null);
				if(!StringUtil.isEmpty(pattern,true)) patt.setConversionPattern(pattern);
				else {
					patt.setConversionPattern(DEFAULT_PATTERN);
					layoutArgs.put("pattern", DEFAULT_PATTERN);
				}
			}
			// class definition
			else {
				Object obj = ClassUtil.loadInstance(cd.getClazz(null),null,null);
				if(obj instanceof Layout) {
					layout=(Layout) obj;
					Iterator<Entry<String, String>> it = layoutArgs.entrySet().iterator();
					Entry<String, String> e;
					while(it.hasNext()){
						e = it.next();
						try {
							Reflector.callSetter(obj, e.getKey(), e.getValue());
						}
						catch (PageException e1) {
							SystemOut.printDate(e1);// TODO log
						}
					}
					
				}
			}
		}
		if(layout!=null) return layout;
		
		if(cdAppender!=null  && DatasourceAppender.class.getName().equals(cdAppender.getClassName())) {
			return new DatasourceLayout(name);
		}
		return new ClassicLayout();
    }
    
    public static ClassDefinition<Layout> layoutClassDefintion(String className) {
    	if("classic".equalsIgnoreCase(className))return new ClassDefinitionImpl( ClassicLayout.class);
    	if("datasource".equalsIgnoreCase(className))return new ClassDefinitionImpl( DatasourceLayout.class);
    	if("html".equalsIgnoreCase(className))return new ClassDefinitionImpl( HTMLLayout.class);
    	if("xml".equalsIgnoreCase(className))return new ClassDefinitionImpl( XMLLayout.class);
    	if("pattern".equalsIgnoreCase(className))return new ClassDefinitionImpl( PatternLayout.class);

    	return new ClassDefinitionImpl( className);
    }
    
    //private static LoggerRepository repository=new Hierarchy(null); 

	public static Level toLevel(int level) {
		switch(level){
		case Log.LEVEL_FATAL: return Level.FATAL;
		case Log.LEVEL_ERROR: return Level.ERROR;
		case Log.LEVEL_WARN: return Level.WARN;
		case Log.LEVEL_DEBUG: return Level.DEBUG;
		case Log.LEVEL_INFO: return Level.INFO;
		case Log.LEVEL_TRACE: return Level.TRACE;
		}
		return Level.INFO;
	}

	public static int toLevel(Level level) {
		if(Level.FATAL.equals(level)) return Log.LEVEL_FATAL;
		if(Level.ERROR.equals(level)) return Log.LEVEL_ERROR;
		if(Level.WARN.equals(level)) return Log.LEVEL_WARN;
		if(Level.DEBUG.equals(level)) return Log.LEVEL_DEBUG;
		if(Level.INFO.equals(level)) return Log.LEVEL_INFO;
		if(Level.TRACE.equals(level)) return Log.LEVEL_TRACE;
		return Log.LEVEL_INFO;
	}
	
	public static Level toLevel(String strLevel, Level defaultValue) {
        if(strLevel==null) return defaultValue;
        strLevel=strLevel.toLowerCase().trim();
        if(strLevel.startsWith("info")) return Level.INFO;
        if(strLevel.startsWith("debug")) return Level.DEBUG;
        if(strLevel.startsWith("warn")) return Level.WARN;
        if(strLevel.startsWith("error")) return Level.ERROR;
        if(strLevel.startsWith("fatal")) return Level.FATAL;
        if(strLevel.startsWith("trace")) return Level.TRACE;
        return defaultValue;
    }
}