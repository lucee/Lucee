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
package lucee.runtime.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.script.ScriptEngineFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import lucee.Info;
import lucee.cli.servlet.HTTPServletImpl;
import lucee.commons.collection.MapFactory;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.FileUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.compress.CompressUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.ResourceUtilImpl;
import lucee.commons.io.retirement.RetireOutputStreamFactory;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.commons.net.HTTPUtil;
import lucee.intergral.fusiondebug.server.FDControllerImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngineFactorySupport;
import lucee.loader.engine.CFMLEngineWrapper;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.util.Util;
import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigServer;
import lucee.runtime.config.ConfigServerImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.DeployHandler;
import lucee.runtime.config.Identification;
import lucee.runtime.config.Password;
import lucee.runtime.config.XMLConfigAdmin;
import lucee.runtime.config.XMLConfigFactory;
import lucee.runtime.config.XMLConfigServerFactory;
import lucee.runtime.config.XMLConfigWebFactory;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.PageServletException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.instrumentation.InstrumentationFactory;
import lucee.runtime.jsr223.ScriptEngineFactoryImpl;
import lucee.runtime.net.http.HTTPServletRequestWrap;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.CastImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.CreationImpl;
import lucee.runtime.op.DecisionImpl;
import lucee.runtime.op.ExceptonImpl;
import lucee.runtime.op.IOImpl;
import lucee.runtime.op.JavaProxyUtilImpl;
import lucee.runtime.op.OperationImpl;
import lucee.runtime.op.StringsImpl;
import lucee.runtime.type.StructImpl;
import lucee.runtime.util.Cast;
import lucee.runtime.util.ClassUtil;
import lucee.runtime.util.ClassUtilImpl;
import lucee.runtime.util.Creation;
import lucee.runtime.util.DBUtil;
import lucee.runtime.util.DBUtilImpl;
import lucee.runtime.util.Decision;
import lucee.runtime.util.Excepton;
import lucee.runtime.util.HTMLUtil;
import lucee.runtime.util.HTMLUtilImpl;
import lucee.runtime.util.HTTPUtilImpl;
import lucee.runtime.util.IO;
import lucee.runtime.util.ListUtil;
import lucee.runtime.util.ListUtilImpl;
import lucee.runtime.util.ORMUtil;
import lucee.runtime.util.ORMUtilImpl;
import lucee.runtime.util.Operation;
import lucee.runtime.util.PageContextUtil;
import lucee.runtime.util.Strings;
import lucee.runtime.util.SystemUtilImpl;
import lucee.runtime.util.TemplateUtil;
import lucee.runtime.util.TemplateUtilImpl;
import lucee.runtime.util.ZipUtil;
import lucee.runtime.util.ZipUtilImpl;
import lucee.runtime.video.VideoUtil;
import lucee.runtime.video.VideoUtilImpl;

import org.apache.felix.framework.Felix;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

//import com.intergral.fusiondebug.server.FDControllerFactory;

/**
 * The CFMl Engine
 */
public final class CFMLEngineImpl implements CFMLEngine {
	
	
	private static Map<String,CFMLFactory> initContextes=MapFactory.<String,CFMLFactory>getConcurrentMap();
    private static Map<String,CFMLFactory> contextes=MapFactory.<String,CFMLFactory>getConcurrentMap();
    private ConfigServerImpl configServer=null;
    private static CFMLEngineImpl engine=null;
    private CFMLEngineFactory factory;
    private final RefBoolean controlerState=new RefBooleanImpl(true);
	private boolean allowRequestTimeout=true;
	private Monitor monitor;
	private List<ServletConfig> servletConfigs=new ArrayList<ServletConfig>();
	private long uptime;
	private InfoImpl info;
	
	private BundleCollection bundleCollection;
	
	private ScriptEngineFactory cfmlScriptEngine;
	private ScriptEngineFactory cfmlTagEngine;
	private ScriptEngineFactory luceeScriptEngine;
	private ScriptEngineFactory luceeTagEngine;
	private Controler controler;
    
    //private static CFMLEngineImpl engine=new CFMLEngineImpl();

    private CFMLEngineImpl(CFMLEngineFactory factory, BundleCollection bc) {
    	this.factory=factory; 
    	this.bundleCollection=bc;
    	
    	// happen when Lucee is loaded directly
    	if(bundleCollection==null) {
    		try{
    			Properties prop = InfoImpl.getDefaultProperties(null);
    				
    			// read the config from default.properties
    			Map<String,Object> config=new HashMap<String, Object>();
    			Iterator<Entry<Object, Object>> it = prop.entrySet().iterator();
    			Entry<Object, Object> e;
    			String k;
    			while(it.hasNext()){
    				e = it.next();
    				k=(String) e.getKey();
    				if(!k.startsWith("org.") && !k.startsWith("felix.")) continue;
    				config.put(k, CFMLEngineFactorySupport.removeQuotes((String)e.getValue(),true));
    			}
    			
    			config.put(
	    				Constants.FRAMEWORK_BOOTDELEGATION,
	    				"lucee.*");
    		    			
    			
    			Felix felix = factory.getFelix(factory.getResourceRoot(),config);
    			
    			bundleCollection=new BundleCollection(felix, felix, null);
    			//bundleContext=bundleCollection.getBundleContext();
    		}
    		catch (Throwable t) {
				throw new RuntimeException(t);
			}
    	}

    	this.info=new InfoImpl(bundleCollection==null?null:bundleCollection.core);
    	Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader()); // MUST better location for this

    	int doNew;
    	Resource configDir=null;
    	try {
    		configDir = getSeverContextConfigDirectory(factory);
			doNew=XMLConfigFactory.doNew(this,configDir, true);
		}
    	catch (IOException e) {
    		throw new PageRuntimeException(e);
		} 

    	CFMLEngineFactory.registerInstance((this));// patch, not really good but it works
    	ConfigServerImpl cs = getConfigServerImpl();
        
        controler = new Controler(cs,initContextes,5*1000,controlerState);
        controler.setDaemon(true);
        controler.setPriority(Thread.MIN_PRIORITY);
        
        boolean disabled=Caster.toBooleanValue(SystemUtil.getSetting(SystemUtil.SETTING_CONTROLLER_DISABLED,null),false);
        if (!disabled) {
        	// start the controller
        	SystemOut.printDate(SystemUtil.getPrintWriter(SystemUtil.OUT), "Start CFML Controller");
        	controler.start();
        }        
        
        // copy bundled extension to local extension directory (if never done before)
        deployBundledExtension(cs);

        // required extensions
        
        boolean isRe=configDir==null?false:XMLConfigFactory.isRequiredExtension(this, configDir);
        boolean installExtensions=Caster.toBooleanValue(XMLConfigWebFactory.getSystemPropOrEnvVar("lucee.extensions.install",null),true);
        boolean updateReqExt=false;
        
        // if we have a "fresh" install  
        Set<ExtensionDefintion> extensions;
        if(installExtensions && (doNew==XMLConfigFactory.NEW_FRESH || doNew==XMLConfigFactory.NEW_FROM4)) {
        	List<ExtensionDefintion> ext = info.getRequiredExtension();
        	extensions = toSet(null,ext);
        	SystemOut.print(SystemUtil.getPrintWriter(SystemUtil.OUT),
            	"Install Extensions ("+doNew+"):"+toList(extensions));
        }
        
        // if we have an update we update the extension that re installed and we have an older version as defined in the manifest
        else if(installExtensions && (doNew==XMLConfigFactory.NEW_MINOR || !isRe)) {
        	updateReqExt=true;
        	extensions = new HashSet<ExtensionDefintion>();
        	Iterator<ExtensionDefintion> it = info.getRequiredExtension().iterator();
        	ExtensionDefintion ed;
        	RHExtension rhe;
        	while(it.hasNext()){
        		ed = it.next();
        		if(ed.getVersion()==null) continue; // no version definition no update
        		try{
        			rhe = XMLConfigAdmin.hasRHExtensions(cs, new ExtensionDefintion(ed.getId()));
        			if(rhe==null) continue; // not installed we do not update
        			
        			// if the installed is older than the one defined in the manifest we update (if possible)
        			if(!rhe.getVersion().equals(ed.getVersion())) 
        				extensions.add(ed);
        		}
        		catch(Throwable t){
        			t.printStackTrace(); // fails we update
        			extensions.add(ed);
        		}
        	}
        }
        else {
        	extensions = new HashSet<ExtensionDefintion>();
        }
        // XMLConfigAdmin.hasRHExtensions(ci, ed)
        
        // install extension defined 
        String extensionIds=XMLConfigWebFactory.getSystemPropOrEnvVar("lucee-extensions",null); // old no longer used
        if(StringUtil.isEmpty(extensionIds,true))
        	extensionIds=XMLConfigWebFactory.getSystemPropOrEnvVar("lucee.extensions",null);
        
        if(!StringUtil.isEmpty(extensionIds,true)) {
        	List<ExtensionDefintion> _extensions = RHExtension.toExtensionDefinitions(extensionIds);
        	extensions=toSet(extensions,_extensions);
        }
        if(extensions.size()>0) {
        	updateReqExt=DeployHandler.deployExtensions(
        			cs,
        			extensions.toArray(new ExtensionDefintion[extensions.size()]),
        			cs.getLog("deploy", true)
        			);
        	
        }
        if(updateReqExt && configDir!=null)XMLConfigFactory.updateRequiredExtension(this, configDir);
        
        

        touchMonitor(cs);   
        this.uptime=System.currentTimeMillis();
        //this.config=config; 
    }

	public static Set<ExtensionDefintion> toSet(Set<ExtensionDefintion> set, List<ExtensionDefintion> list) {
		HashMap<String, ExtensionDefintion> map=new HashMap<String, ExtensionDefintion>();
		ExtensionDefintion ed;
			
		// set > map
		if(set!=null) {
			Iterator<ExtensionDefintion> it = set.iterator();
			while(it.hasNext()){
				ed = it.next();
				map.put(ed.toString(),ed);
			}
		}
		
		// list > map
		if(list!=null) {
			Iterator<ExtensionDefintion> it = list.iterator();
			while(it.hasNext()){
				ed = it.next();
				map.put(ed.toString(),ed);
			}
		}
		
		// to Set
		HashSet<ExtensionDefintion> rtn = new HashSet<ExtensionDefintion>();
		Iterator<ExtensionDefintion> it = map.values().iterator();
		while(it.hasNext()){
			ed = it.next();
			rtn.add(ed);
		}
		return rtn;
	}
	
	public static String toList(Set<ExtensionDefintion> set) {
		StringBuilder sb=new StringBuilder();
		Iterator<ExtensionDefintion> it = set.iterator();
		ExtensionDefintion ed;
		while(it.hasNext()){
			ed = it.next();
			if(sb.length()>0) sb.append(", ");
			sb.append(ed.toString());
		}
		return sb.toString();
	}
	

	private void deployBundledExtension(ConfigServerImpl cs) {
		Resource dir = cs.getLocalExtensionProviderDirectory();
		List<RHExtension> existing = DeployHandler.getLocalExtensions(cs);
		String sub="extensions/";
		
		// get the index
		ClassLoader cl=CFMLEngineFactory.getInstance().getCFMLEngineFactory().getClass().getClassLoader();
		InputStream is = cl.getResourceAsStream("extensions/.index");
		if(is==null)is = cl.getResourceAsStream("/extensions/.index");
		if(is==null) return;
		Log log = cs.getLog("deploy");
			
		try {
		
			String index=IOUtil.toString(is, CharsetUtil.UTF8);
			String[] names = lucee.runtime.type.util.ListUtil.listToStringArray(index, ';');
			String name;
			Resource temp=null;
			RHExtension rhe,exist;
			Iterator<RHExtension> it;
			
			for(int i=0;i<names.length;i++){
				name=names[i];
				if(StringUtil.isEmpty(name,true)) continue;
				name=name.trim();
				is = cl.getResourceAsStream("extensions/"+name);
				if(is==null)is = cl.getResourceAsStream("/extensions/"+name);
				if(is==null) {
					log.error("extract-extension", "could not found extension ["+name+"] defined in the index in the lucee.jar");
					continue;
				}
				
				try {
					temp=SystemUtil.getTempFile("lex", true);
					Util.copy(is, temp.getOutputStream(),false,true);
					rhe = new RHExtension(cs, temp, false);
					boolean alreadyExists=false;
					it = existing.iterator();
					while(it.hasNext()){
						exist = it.next();
						if(exist.equals(rhe)) {
							alreadyExists=true;
							break;
						}
					}
					if(!alreadyExists) {
						temp.moveTo(dir.getRealResource(name));
						log.info("extract-extension", "added ["+name+"] to ["+dir+"]");
						
					}
					
				}
				finally {
					if(temp!=null && temp.exists())temp.delete();
				}
			}
		}
		catch(Throwable t){
			log.error("extract-extension", t);
		}
		return;
	}

	private void deployBundledExtensionZip(ConfigServerImpl cs) {
		Resource dir = cs.getLocalExtensionProviderDirectory();
		List<RHExtension> existing = DeployHandler.getLocalExtensions(cs);
		String sub="extensions/";
		// MUST this does not work on windows! we need to add an index
		ZipEntry entry;
		ZipInputStream zis = null;
		try {
			CodeSource src = CFMLEngineFactory.class.getProtectionDomain().getCodeSource();
			if (src == null) return;
			URL loc = src.getLocation();
			
			
			zis=new ZipInputStream(loc.openStream());
			String path,name;
			int index;
			Resource temp;
			RHExtension rhe;
			Iterator<RHExtension> it;
			RHExtension exist;
			while ((entry = zis.getNextEntry())!= null) {
				path = entry.getName();
				if(path.startsWith(sub) && path.endsWith(".lex")) { // ignore non lex files or file from else where
					index=path.lastIndexOf('/')+1;
					if(index==sub.length()) { // ignore sub directories
						name=path.substring(index);
						temp=null;
						try {
							temp=SystemUtil.getTempFile("lex", true);
							Util.copy(zis, temp.getOutputStream(),false,true);
							rhe = new RHExtension(cs, temp, false);
							boolean alreadyExists=false;
							it = existing.iterator();
							while(it.hasNext()){
								exist = it.next();
								if(exist.equals(rhe)) {
									alreadyExists=true;
									break;
								}
							}
							if(!alreadyExists) {
								temp.moveTo(dir.getRealResource(name));
							}
							
						}
						finally {
							if(temp!=null && temp.exists())temp.delete();
						}
						
					}
				}
				zis.closeEntry();
			} 
		}
		catch(Throwable t){
			t.printStackTrace();// TODO log this
		}
		finally {
			Util.closeEL(zis);
		}
		return;
	}

	public void touchMonitor(ConfigServerImpl cs) {
		if(monitor!=null && monitor.isAlive()) return; 
		monitor = new Monitor(cs,controlerState); 
        monitor.setDaemon(true);
        monitor.setPriority(Thread.MIN_PRIORITY);
        monitor.start(); 
	}

    /**
     * get singelton instance of the CFML Engine
     * @param factory
     * @return CFMLEngine
     */
    public static synchronized CFMLEngine getInstance(CFMLEngineFactory factory,BundleCollection bc) {
    	if(engine==null) {
    		if(SystemUtil.getLoaderVersion()<5.9D) {
    			if(SystemUtil.getLoaderVersion()<5.8D)
    				throw new RuntimeException("You need to update your lucee.jar to run this version, you can download the latest jar from http://download.lucee.org.");
    			else
    				System.out.println("To use all features Lucee provides, you need to update your lucee.jar, you can download the latest jar from http://download.lucee.org.");
    		}
    		engine=new CFMLEngineImpl(factory,bc);
    		
        }
        return engine;
    }
    
    /**
     * get singelton instance of the CFML Engine, throwsexception when not already init
     * @param factory
     * @return CFMLEngine
     */
    public static synchronized CFMLEngine getInstance() throws ServletException {
    	if(engine!=null) return engine;
    	throw new ServletException("CFML Engine is not loaded");
    }
    
    @Override
    public void addServletConfig(ServletConfig config) throws ServletException {
    	servletConfigs.add(config);
    	String real=ReqRspUtil.getRootPath(config.getServletContext());
    	if(!initContextes.containsKey(real)) {
        	CFMLFactory jspFactory = loadJSPFactory(getConfigServerImpl(),config,initContextes.size());
            initContextes.put(real,jspFactory);
        }        
    }
    
    @Override
    public ConfigServer getConfigServer(Password password) throws PageException {
    	getConfigServerImpl().checkAccess(password);
    	return configServer;
    }

    @Override
    public ConfigServer getConfigServer(String key, long timeNonce) throws PageException {
    	getConfigServerImpl().checkAccess(key,timeNonce);
    	return configServer;
    }
    
    public void setConfigServerImpl(ConfigServerImpl cs) {
    	this.configServer=cs;
    }

    private ConfigServerImpl getConfigServerImpl() {
    	if(configServer==null) {
    		try {
            	Resource context = getSeverContextConfigDirectory(factory); 
            	//CFMLEngineFactory.registerInstance(this);// patch, not really good but it works
                configServer=XMLConfigServerFactory.newInstance(
                        this,
                        initContextes,
                        contextes,
                        context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return configServer;
    }
    
    private Resource getSeverContextConfigDirectory(CFMLEngineFactory factory) throws IOException {
    	ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
    	return frp.getResource(factory.getResourceRoot().getAbsolutePath()).getRealResource("context");
	}

	private  CFMLFactoryImpl loadJSPFactory(ConfigServerImpl configServer, ServletConfig sg, int countExistingContextes) throws ServletException {
    	try {
            // Load Config
    		RefBoolean isCustomSetting=new RefBooleanImpl();
            Resource configDir=getConfigDirectory(sg,configServer,countExistingContextes,isCustomSetting);
            
            CFMLFactoryImpl factory=new CFMLFactoryImpl(this,sg);
            ConfigWebImpl config=XMLConfigWebFactory.newInstance(this,factory,configServer,configDir,isCustomSetting.toBooleanValue(),sg);
            factory.setConfig(config);
            return factory;
        }
        catch (Exception e) {
            ServletException se= new ServletException(e.getMessage());
            se.setStackTrace(e.getStackTrace());
            throw se;
        } 
        
    }   

    /**
     * loads Configuration File from System, from init Parameter from web.xml
     * @param sg
     * @param configServer 
     * @param countExistingContextes 
     * @return return path to directory
     */
    private Resource getConfigDirectory(ServletConfig sg, ConfigServerImpl configServer, int countExistingContextes, RefBoolean isCustomSetting) throws PageServletException {
    	isCustomSetting.setValue(true);
    	ServletContext sc=sg.getServletContext();
        String strConfig=sg.getInitParameter("configuration");
        if(StringUtil.isEmpty(strConfig))strConfig=sg.getInitParameter("lucee-web-directory");
        if(StringUtil.isEmpty(strConfig))strConfig=System.getProperty("lucee.web.dir");
        
        if(StringUtil.isEmpty(strConfig)) {
        	isCustomSetting.setValue(false);
        	strConfig="{web-root-directory}/WEB-INF/lucee/";
        }
        // only for backward compatibility
        else if(strConfig.startsWith("/WEB-INF/lucee/"))strConfig="{web-root-directory}"+strConfig;
        
        
        strConfig=StringUtil.removeQuotes(strConfig,true);
        
        
        
        // static path is not allowed
        if(countExistingContextes>1 && strConfig!=null && strConfig.indexOf('{')==-1){
        	String text="static path ["+strConfig+"] for servlet init param [lucee-web-directory] is not allowed, path must use a web-context specific placeholder.";
        	System.err.println(text);
        	throw new PageServletException(new ApplicationException(text));
        }
        strConfig=SystemUtil.parsePlaceHolder(strConfig,sc,configServer.getLabels());
        
        
        
        ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
        Resource root = frp.getResource(ReqRspUtil.getRootPath(sc));
        Resource res;
        Resource configDir=ResourceUtil.createResource(res=root.getRealResource(strConfig), FileUtil.LEVEL_PARENT_FILE,FileUtil.TYPE_DIR);
        
        if(configDir==null) {
            configDir=ResourceUtil.createResource(res=frp.getResource(strConfig), FileUtil.LEVEL_GRAND_PARENT_FILE,FileUtil.TYPE_DIR);
        }

        if(configDir==null && !isCustomSetting.toBooleanValue()) {
        	try {
				res.createDirectory(true);
				configDir=res;
			}
        	catch (IOException e) {
				throw new PageServletException(Caster.toPageException(e));
			}
        }
        if(configDir==null) {
        	throw new PageServletException(new ApplicationException("path ["+strConfig+"] is invalid"));
        }
        
        if(!configDir.exists() || ResourceUtil.isEmptyDirectory(configDir, null)){
        	Resource railoRoot;
        	// there is a railo directory
        	if(configDir.getName().equals("lucee") && (railoRoot=configDir.getParentResource().getRealResource("railo")).isDirectory()) {
        		try {
					copyRecursiveAndRename(railoRoot,configDir);
				}
				catch (IOException e) {
					try {
	    				configDir.createDirectory(true);
	    			} 
	            	catch (IOException ioe) {}
					return configDir;
				}
				// zip the railo-server di and delete it (optional)
				try {
					Resource p=railoRoot.getParentResource();
					CompressUtil.compress(CompressUtil.FORMAT_ZIP, railoRoot, p.getRealResource("railo-web-context-old.zip"), false, -1);
					ResourceUtil.removeEL(railoRoot, true);
				}
				catch(Throwable t){t.printStackTrace();}
        	}
        	else {
            	try {
    				configDir.createDirectory(true);
    			} 
            	catch (IOException e) {}	
        	}
        }
        return configDir;
    }
    
    private File getDirectoryByProp(String name) {
		String value=System.getProperty(name);
		if(Util.isEmpty(value,true)) return null;
		
		File dir=new File(value);
		dir.mkdirs();
		if (dir.isDirectory()) return dir;
		return null;
	}
    
    private static void copyRecursiveAndRename(Resource src,Resource trg) throws IOException {
	 	if(!src.exists()) return ;
		if(src.isDirectory()) {
			if(!trg.exists())trg.mkdirs();
			
			Resource[] files = src.listResources();
				for(int i=0;i<files.length;i++) {
					copyRecursiveAndRename(files[i],trg.getRealResource(files[i].getName()));
				}
		}
		else if(src.isFile()) {
			if(trg.getName().endsWith(".rc") || trg.getName().startsWith(".")) {
				return;
			}
					
			if(trg.getName().equals("railo-web.xml.cfm")) {
				trg=trg.getParentResource().getRealResource("lucee-web.xml.cfm");
				// cfLuceeConfiguration
				InputStream is = src.getInputStream();
				OutputStream os = trg.getOutputStream();
					try{
						String str=Util.toString(is);
						str=str.replace("<cfRailoConfiguration", "<!-- copy from Railo context --><cfLuceeConfiguration");
						str=str.replace("</cfRailoConfiguration", "</cfLuceeConfiguration");
						str=str.replace("<railo-configuration", "<lucee-configuration");
						str=str.replace("</railo-configuration", "</lucee-configuration");
						str=str.replace("{railo-config}", "{lucee-config}");
						str=str.replace("{railo-server}", "{lucee-server}");
						str=str.replace("{railo-web}", "{lucee-web}");
						str=str.replace("\"railo.commons.", "\"lucee.commons.");
						str=str.replace("\"railo.runtime.", "\"lucee.runtime.");
						str=str.replace("\"railo.cfx.", "\"lucee.cfx.");
						str=str.replace("/railo-context.ra", "/lucee-context.lar");
						str=str.replace("/railo-context", "/lucee");
						str=str.replace("railo-server-context", "lucee-server");
						str=str.replace("http://www.getrailo.org", "http://release.lucee.org");
						str=str.replace("http://www.getrailo.com", "http://release.lucee.org");
						
						
						ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
						
						try {
					 		Util.copy(bais, os);
					 		bais.close();
					 	}
					 	finally {
					 		Util.closeEL(is, os);
					 	}
					}
					finally {
						Util.closeEL(is,os);
					}
				return;
			}

			InputStream is = src.getInputStream();
			OutputStream os = trg.getOutputStream();
			try{
				Util.copy(is, os);
			}
			finally {
				Util.closeEL(is, os);
			}
		}
	 }
    
    @Override
    public CFMLFactory getCFMLFactory(ServletConfig srvConfig,HttpServletRequest req) throws ServletException {
    	ServletContext srvContext = srvConfig.getServletContext();
    	
    	String real=ReqRspUtil.getRootPath(srvContext);
        ConfigServerImpl cs = getConfigServerImpl();
    	
        // Load JspFactory
        
        CFMLFactory factory=contextes.get(real);
        if(factory==null) {
        	factory=initContextes.get(real);
            if(factory==null) {
                factory=loadJSPFactory(cs,srvConfig,initContextes.size());
                initContextes.put(real,factory);
            }
            contextes.put(real,factory);
            
            try {
            	String cp = req.getContextPath();
            	if(cp==null)cp="";
				((CFMLFactoryImpl)factory).setURL(new URL(req.getScheme(),req.getServerName(),req.getServerPort(),cp));
			} 
            catch (MalformedURLException e) {
				e.printStackTrace();
			}
        }
        return factory;
    }
    
    @Override
    public void service(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
    	CFMLFactory factory=getCFMLFactory(servlet.getServletConfig(), req);
    	
    	// is Lucee dialect enabled?
    	if(!((ConfigImpl)factory.getConfig()).allowLuceeDialect()){
    		try {
				PageContextImpl.notSupported();
			} catch (ApplicationException e) {
				throw new PageServletException(e);
			}
    	}
        PageContext pc = factory.getLuceePageContext(servlet,req,rsp,null,false,-1,false,true,-1,true,false);
        ThreadQueue queue = factory.getConfig().getThreadQueue();
        queue.enter(pc);
        try {
        	pc.execute(pc.getHttpServletRequest().getServletPath(),false,true);
        } 
        catch (PageException pe) {
			throw new PageServletException(pe);
		}
        finally {
        	queue.exit(pc);
            factory.releaseLuceePageContext(pc,true);
            //FDControllerFactory.notifyPageComplete();
        }
    }
    
    @Override
    public void serviceCFML(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
    	CFMLFactory factory=getCFMLFactory(servlet.getServletConfig(), req);
    	
        PageContext pc = factory.getLuceePageContext(servlet,req,rsp,null,false,-1,false,true,-1,true,false);
        ThreadQueue queue = factory.getConfig().getThreadQueue();
        queue.enter(pc);
        try {
        	/*print.out("INCLUDE");
        	print.out("servlet_path:"+req.getAttribute("javax.servlet.include.servlet_path"));
        	print.out("request_uri:"+req.getAttribute("javax.servlet.include.request_uri"));
        	print.out("context_path:"+req.getAttribute("javax.servlet.include.context_path"));
        	print.out("path_info:"+req.getAttribute("javax.servlet.include.path_info"));
        	print.out("query_string:"+req.getAttribute("javax.servlet.include.query_string"));
        	print.out("FORWARD");
        	print.out("servlet_path:"+req.getAttribute("javax.servlet.forward.servlet_path"));
        	print.out("request_uri:"+req.getAttribute("javax.servlet.forward.request_uri"));
        	print.out("context_path:"+req.getAttribute("javax.servlet.forward.context_path"));
        	print.out("path_info:"+req.getAttribute("javax.servlet.forward.path_info"));
        	print.out("query_string:"+req.getAttribute("javax.servlet.forward.query_string"));
        	print.out("---");
        	print.out(req.getServletPath());
        	print.out(pc.getHttpServletRequest().getServletPath());
        	*/
        	
        	pc.executeCFML(pc.getHttpServletRequest().getServletPath(),false,true);
        } 
        catch (PageException pe) {
			throw new PageServletException(pe);
		}
        finally {
        	queue.exit(pc);
            factory.releaseLuceePageContext(pc,true);
            //FDControllerFactory.notifyPageComplete();
        }
    }

	@Override
	public void serviceFile(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		req=new HTTPServletRequestWrap(req);
		CFMLFactory factory=getCFMLFactory( servlet.getServletConfig(), req);
        ConfigWeb config = factory.getConfig();
        PageSource ps = config.getPageSourceExisting(null, null, req.getServletPath(), false, true, true, false);
        //Resource res = ((ConfigWebImpl)config).getPhysicalResourceExistingX(null, null, req.getServletPath(), false, true, true); 
        
		if(ps==null) {
    		rsp.sendError(404);
    	}
    	else {
    		Resource res = ps.getResource();
    		if(res==null) {
    			rsp.sendError(404);
    		}
    		else {
	    		ReqRspUtil.setContentLength(rsp,res.length());
	    		String mt = servlet.getServletContext().getMimeType(req.getServletPath());
	    		if(!StringUtil.isEmpty(mt))ReqRspUtil.setContentType(rsp,mt);
	    		IOUtil.copy(res, rsp.getOutputStream(), true);
    		}
    	}
	}
	

	@Override
	public void serviceRest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		req=new HTTPServletRequestWrap(req);
		CFMLFactory factory=getCFMLFactory(servlet.getServletConfig(), req);
        
		PageContext pc = factory.getLuceePageContext(servlet,req,rsp,null,false,-1,false,true,-1,true,false);
        ThreadQueue queue = factory.getConfig().getThreadQueue();
        queue.enter(pc);
        try {
        	pc.executeRest(pc.getHttpServletRequest().getServletPath(),false);
        } 
        catch (PageException pe) {
			throw new PageServletException(pe);
		}
        finally {
        	queue.exit(pc);
            factory.releaseLuceePageContext(pc,true);
            //FDControllerFactory.notifyPageComplete();
        }
		
		
	}
    

    /*private String getContextList() {
        return List.arrayToList((String[])contextes.keySet().toArray(new String[contextes.size()]),", ");
    }*/

    @Override
    public String getVersion() {
        return info.getVersion().toString();
    }
    
    @Override
	public Info getInfo() {
        return info;
    }

    @Override
    public String getUpdateType() {
        return getConfigServerImpl().getUpdateType();
    }

    @Override
    public URL getUpdateLocation() {
        return getConfigServerImpl().getUpdateLocation();
    }

    @Override
    public Identification getIdentification() {
        return getConfigServerImpl().getIdentification();
    }

    @Override
    public boolean can(int type, Password password) {
        return getConfigServerImpl().passwordEqual(password);
    }

    @Override
	public CFMLEngineFactory getCFMLEngineFactory() {
        return factory;
    }

    @Override
	public void serviceAMF(HttpServlet servlet, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
    	req=new HTTPServletRequestWrap(req);
    	getCFMLFactory(servlet.getServletConfig(), req)
    		.getConfig().getAMFEngine().service(servlet,new HTTPServletRequestWrap(req),rsp);
    }

    @Override
    public void reset() {
    	reset(null);
    }
    
    @Override
    public void reset(String configId) {
        
    	getControler().close();
		RetireOutputStreamFactory.close();
    	
        CFMLFactoryImpl cfmlFactory;
        //ScopeContext scopeContext;
        try {
	        Iterator<String> it = contextes.keySet().iterator();
	        while(it.hasNext()) {
	        	try {
		            cfmlFactory=(CFMLFactoryImpl) contextes.get(it.next());
		            if(configId!=null && !configId.equals(cfmlFactory.getConfigWebImpl().getIdentification().getId())) continue;
		            	
		            // scopes
		            try{cfmlFactory.getScopeContext().clear();}catch(Throwable t){t.printStackTrace();}
		            
		            // PageContext
		            try{cfmlFactory.resetPageContext();}catch(Throwable t){t.printStackTrace();}
		            
		            // Query Cache
		            try{ 
		            	PageContext pc = ThreadLocalPageContext.get();
		            	if(pc!=null) {
		            		pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_QUERY,null).clear(pc);
		            		pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION,null).clear(pc);
		            		pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_INCLUDE,null).clear(pc);
		            	}
		            	//cfmlFactory.getDefaultQueryCache().clear(null);
		            }catch(Throwable t){t.printStackTrace();}
		            
		            
		            
		            // Gateway
		            try{ cfmlFactory.getConfigWebImpl().getGatewayEngine().reset();}catch(Throwable t){t.printStackTrace();}
		            
	        	}
	        	catch(Throwable t){
	        		t.printStackTrace();
	        	}
	        }
        }
    	finally {
            // Controller
            controlerState.setValue(false);
    	}
    }
    
    @Override
    public Cast getCastUtil() {
        return CastImpl.getInstance();
    }

    @Override
    public Operation getOperatonUtil() {
        return OperationImpl.getInstance();
    }

    @Override
    public Decision getDecisionUtil() {
        return DecisionImpl.getInstance();
    }

    @Override
    public Excepton getExceptionUtil() {
        return ExceptonImpl.getInstance();
    }


	@Override
	public Object getJavaProxyUtil() { // FUTURE return JavaProxyUtil
		return new JavaProxyUtilImpl();
	}
    
    @Override
    public Creation getCreationUtil() {
    	return CreationImpl.getInstance(this);
    }

    @Override
    public IO getIOUtil() {
        return IOImpl.getInstance();
    }

    @Override
    public Strings getStringUtil() {
        return StringsImpl.getInstance();
    }

	@Override
	public Object getFDController() {
		engine.allowRequestTimeout(false);
		
		return new FDControllerImpl(engine,engine.getConfigServerImpl().getSerialNumber());
	}

	public Map<String,CFMLFactory> getCFMLFactories() {
		return initContextes;
	}

	@Override
	public lucee.runtime.util.ResourceUtil getResourceUtil() {
		return ResourceUtilImpl.getInstance();
	}

	@Override
	public lucee.runtime.util.HTTPUtil getHTTPUtil() {
		return HTTPUtilImpl.getInstance();
	}

	@Override
	public PageContext getThreadPageContext() {
		return ThreadLocalPageContext.get();
	}

	@Override
	public Config getThreadConfig() {
		return ThreadLocalPageContext.getConfig();
	}

	@Override
	public void registerThreadPageContext(PageContext pc) {
		ThreadLocalPageContext.register(pc);
	}

	@Override
	public VideoUtil getVideoUtil() {
		return VideoUtilImpl.getInstance();
	}

	@Override
	public ZipUtil getZipUtil() {
		return ZipUtilImpl.getInstance();
	}

	/*public String getState() {
		return info.getStateAsString();
	}*/

	public void allowRequestTimeout(boolean allowRequestTimeout) {
		this.allowRequestTimeout=allowRequestTimeout;
	}

	public boolean allowRequestTimeout() {
		return allowRequestTimeout;
	}
	
	public boolean isRunning() {
		try{
			CFMLEngine other = CFMLEngineFactory.getInstance();
			// FUTURE patch, do better impl when changing loader
			if(other!=this && controlerState.toBooleanValue() &&  !(other instanceof CFMLEngineWrapper)) {
				SystemOut.printDate("CFMLEngine is still set to true but no longer valid, "+lucee.runtime.config.Constants.NAME+" disable this CFMLEngine.");
				controlerState.setValue(false);
				reset();
				return false;
			}
		}
		catch(Throwable t){}
		return controlerState.toBooleanValue();
	}

	@Override
	public void cli(Map<String, String> config, ServletConfig servletConfig) throws IOException,JspException,ServletException {
		ServletContext servletContext = servletConfig.getServletContext();
		HTTPServletImpl servlet=new HTTPServletImpl(servletConfig, servletContext, servletConfig.getServletName());

		// webroot
		String strWebroot=config.get("webroot");
		if(StringUtil.isEmpty(strWebroot,true)) throw new IOException("missing webroot configuration");
		Resource root=ResourcesImpl.getFileResourceProvider().getResource(strWebroot);
		root.mkdirs();
		
		// serverName
		String serverName=config.get("server-name");
		if(StringUtil.isEmpty(serverName,true))serverName="localhost";
		
		// uri
		String strUri=config.get("uri");
		if(StringUtil.isEmpty(strUri,true)) throw new IOException("missing uri configuration");
		URI uri;
		try {
			uri = lucee.commons.net.HTTPUtil.toURI(strUri);
		} catch (URISyntaxException e) {
			throw Caster.toPageException(e);
		}
		
		// cookie
		Cookie[] cookies;
		String strCookie=config.get("cookie");
		if(StringUtil.isEmpty(strCookie,true)) cookies=new Cookie[0];
		else {
			Map<String,String> mapCookies=HTTPUtil.parseParameterList(strCookie,false,null);
			int index=0;
			cookies=new Cookie[mapCookies.size()];
			Entry<String, String> entry;
			Iterator<Entry<String, String>> it = mapCookies.entrySet().iterator();
			Cookie c;
			while(it.hasNext()){
				entry = it.next();
				c=ReqRspUtil.toCookie(entry.getKey(),entry.getValue(),null);
				if(c!=null)cookies[index++]=c;
				else throw new IOException("cookie name ["+entry.getKey()+"] is invalid");
			}
		}
		

		// header
		Pair[] headers=new Pair[0];
		
		// parameters
		Pair[] parameters=new Pair[0];
		
		// attributes
		StructImpl attributes = new StructImpl();
		ByteArrayOutputStream os=new ByteArrayOutputStream();
		
		
		
		
		HttpServletRequestDummy req=new HttpServletRequestDummy(
				root,serverName,uri.getPath(),uri.getQuery(),cookies,headers,parameters,attributes,null,null);
		req.setProtocol("CLI/1.0");
		HttpServletResponse rsp=new HttpServletResponseDummy(os);
		
		serviceCFML(servlet, req, rsp);
		String res = os.toString(ReqRspUtil.getCharacterEncoding(null,rsp).name());
		System.out.println(res);
	}
	
	@Override
	public ServletConfig[] getServletConfigs(){
		return servletConfigs.toArray(new ServletConfig[servletConfigs.size()]);
	}

	@Override
	public long uptime() {
		return uptime;
	}

	/*public Bundle getCoreBundle() {
		return bundle;
	}*/

	@Override
	public BundleCollection getBundleCollection() {
		return bundleCollection;
	}
	
	@Override
	public BundleContext getBundleContext() {
		return bundleCollection.getBundleContext();
	}

	@Override
	public ClassUtil getClassUtil() {
		return new ClassUtilImpl();
	}

	@Override
	public ListUtil getListUtil() {
		return new ListUtilImpl();
	}

	@Override
	public DBUtil getDBUtil() {
		return new DBUtilImpl();
	}

	@Override
	public ORMUtil getORMUtil() {
		return new ORMUtilImpl();
	}

	@Override
	public TemplateUtil getTemplateUtil() {
		return new TemplateUtilImpl();
	}

	@Override
	public HTMLUtil getHTMLUtil() {
		return new HTMLUtilImpl();
	}

	@Override
	public ScriptEngineFactory getScriptEngineFactory(int dialect) {
		
		if(dialect==CFMLEngine.DIALECT_CFML) {
			if(cfmlScriptEngine==null) cfmlScriptEngine=new ScriptEngineFactoryImpl(this,false,dialect);
			return cfmlScriptEngine;
		}
		
		if(luceeScriptEngine==null) luceeScriptEngine=new ScriptEngineFactoryImpl(this,false,dialect);
		return luceeScriptEngine;
	}

	@Override
	public ScriptEngineFactory getTagEngineFactory(int dialect) {
		
		if(dialect==CFMLEngine.DIALECT_CFML) {
			if(cfmlTagEngine==null) cfmlTagEngine=new ScriptEngineFactoryImpl(this,true,dialect);
			return cfmlTagEngine;
		}
		
		if(luceeTagEngine==null) luceeTagEngine=new ScriptEngineFactoryImpl(this,true,dialect);
		return luceeTagEngine;
	}

	@Override
	public PageContext createPageContext(File contextRoot, String host, String scriptName, String queryString
			, Cookie[] cookies,Map<String, Object> headers,Map<String, String> parameters, 
			Map<String, Object> attributes, OutputStream os, long timeout, boolean register) throws ServletException {
		return PageContextUtil.getPageContext(contextRoot,host, scriptName, queryString, cookies, headers, parameters, attributes, os,register,timeout,false);
	}
	
	@Override
	public ConfigWeb createConfig(File contextRoot,String host, String scriptName) throws ServletException {
		// TODO do a mored rect approach
		PageContext pc = null;
		try{
			pc = PageContextUtil.getPageContext(contextRoot,host,scriptName, null, null, null, null, null, null,false,-1,false);
			return pc.getConfig();
		}
		finally{
			pc.getConfig().getFactory().releaseLuceePageContext(pc, false);
		}
		
	}

	@Override
	public void releasePageContext(PageContext pc, boolean unregister) {
		PageContextUtil.releasePageContext(pc,unregister);
	}

	@Override
	public lucee.runtime.util.SystemUtil getSystemUtil() {
		return new SystemUtilImpl();
	}

	@Override
	public TimeZone getThreadTimeZone() {
		return ThreadLocalPageContext.getTimeZone();
	}

	@Override
	public Instrumentation getInstrumentation() {
		return InstrumentationFactory.getInstrumentation(ThreadLocalPageContext.getConfig());
	}
	
	public Controler getControler() {
		return controler;
	}
}