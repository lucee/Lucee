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
package lucee.runtime.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.SystemOut;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.library.function.FunctionLibException;
import lucee.transformer.library.tag.TagLibException;

import org.osgi.framework.BundleException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.jacob.com.LibraryLoader;


/**
 * 
 */
public final class XMLConfigServerFactory extends XMLConfigFactory{
    
    /**
     * creates a new ServletConfig Impl Object
     * @param engine 
     * @param initContextes
     * @param contextes
     * @param configDir
     * @return new Instance
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws BundleException 
     */
    public static ConfigServerImpl newInstance(CFMLEngineImpl engine,Map<String,CFMLFactory> initContextes, Map<String,CFMLFactory> contextes, Resource configDir) 
        throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {

    	boolean isCLI=SystemUtil.isCLICall();
    	if(isCLI){
    		Resource logs = configDir.getRealResource("logs");
    		logs.mkdirs();
    		Resource out = logs.getRealResource("out");
    		Resource err = logs.getRealResource("err");
    		ResourceUtil.touch(out);
    		ResourceUtil.touch(err);
    		if(logs instanceof FileResource) {
    			SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter((FileResource)out));
    			SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter((FileResource)err));
    		}
    		else{
    			SystemUtil.setPrintWriter(SystemUtil.OUT, new PrintWriter(IOUtil.getWriter(out,"UTF-8")));
    			SystemUtil.setPrintWriter(SystemUtil.ERR, new PrintWriter(IOUtil.getWriter(err,"UTF-8")));	
    		}
    	}
    	SystemOut.print(SystemUtil.getPrintWriter(SystemUtil.OUT),
    			"===================================================================\n"+
    			"SERVER CONTEXT\n" +
    			"-------------------------------------------------------------------\n"+
    			"- config:"+configDir+"\n"+
    			"- loader-version:"+SystemUtil.getLoaderVersion()+"\n"+
    			"- core-version:"+engine.getInfo().getVersion()+"\n"+
    			"===================================================================\n"
    			
    			);
          		
    	int iDoNew = doNew(engine,configDir,false);
		boolean doNew = iDoNew!=NEW_NONE;
		
    	Resource configFile=configDir.getRealResource("lucee-server.xml");
    	
    	
        if(!configFile.exists()) {
		    configFile.createFile(true);
			//InputStream in = new TextFile("").getClass().getResourceAsStream("/resource/config/server.xml");
			createFileFromResource(
			     "/resource/config/server.xml",
			     configFile.getAbsoluteResource(),
			     "tpiasfap"
			);
		}
		
        Document doc=loadDocumentCreateIfFails(configFile, "server");
        
     // get version
        Element luceeConfiguration = doc.getDocumentElement();
        String strVersion = luceeConfiguration.getAttribute("version");
        double version=Caster.toDoubleValue(strVersion, 1.0d);
        boolean cleanupDatasources=version<5.0D;
        
        ConfigServerImpl config=new ConfigServerImpl(engine,initContextes,contextes,configDir,configFile);
		load(config,doc,false,doNew);
	    
		createContextFiles(configDir,config,doNew,cleanupDatasources);

        ((CFMLEngineImpl)ConfigWebUtil.getEngine(config)).onStart(config,false);
	    return config;
    }
    
	/**
     * reloads the Config Object
     * @param configServer
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws PageException
     * @throws IOException
     * @throws TagLibException
     * @throws FunctionLibException
     * @throws BundleException 
     */
    public static void reloadInstance(CFMLEngine engine, ConfigServerImpl configServer) 
    	throws SAXException, ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
        Resource configFile=configServer.getConfigFile();
        
        if(configFile==null) return ;
        if(second(configServer.getLoadTime())>second(configFile.lastModified())) return ;
        int iDoNew = doNew(engine, configServer.getConfigDir(),false);
		boolean doNew = iDoNew!=NEW_NONE;
		
        load(configServer,loadDocument(configFile),true,doNew);
        ((CFMLEngineImpl)ConfigWebUtil.getEngine(configServer)).onStart(configServer,true);
    }
    
    private static long second(long ms) {
		return ms/1000;
	}
    
    /**
     * @param configServer 
     * @param doc
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws FunctionLibException
     * @throws TagLibException
     * @throws PageException
     * @throws BundleException 
     */
    static void load(ConfigServerImpl configServer, Document doc, boolean isReload, boolean doNew) throws ClassException, PageException, IOException, TagLibException, FunctionLibException, BundleException {
        XMLConfigWebFactory.load(null,configServer,doc, isReload,doNew);
        loadLabel(configServer,doc);
    }
    

	private static void loadLabel(ConfigServerImpl configServer, Document doc) {
		Element el= getChildByName(doc.getDocumentElement(),"labels");
        Element[] children=getChildren(el,"label");
        
        Map<String, String> labels=new HashMap<String, String>();
        if(children!=null)for(int i=0;i<children.length;i++) {
           el=children[i];
           
           String id=el.getAttribute("id");
           String name=el.getAttribute("name");
           if(id!=null && name!=null) { 
               labels.put(id, name);
           }
        }
        configServer.setLabels(labels);
	}
	
	private static void createContextFiles(Resource configDir, ConfigServer config, boolean doNew, boolean cleanupDatasources) {
		
		Resource contextDir = configDir.getRealResource("context");
		Resource adminDir = contextDir.getRealResource("admin");
		

		// Debug
		Resource debug = adminDir.getRealResource("debug");
		create("/resource/context/admin/debug/",new String[]{
				"Debug.cfc"
				,"Field.cfc"
				,"Group.cfc"
				,"Classic.cfc"
				,"Modern.cfc"
				,"Comment.cfc"
				},debug,doNew);
		
		
		// DB Drivers types
		Resource dbDir = adminDir.getRealResource("dbdriver");
		Resource typesDir = dbDir.getRealResource("types");
		create("/resource/context/admin/dbdriver/types/",new String[]{
		"IDriver.cfc"
		,"Driver.cfc"
		,"IDatasource.cfc"
		,"IDriverSelector.cfc"
		,"Field.cfc"
		},typesDir,doNew);
		
	
			create("/resource/context/admin/dbdriver/",new String[]{
					"Other.cfc"
			},dbDir,doNew);
			
		// Cache Drivers
		Resource cDir = adminDir.getRealResource("cdriver");
		create("/resource/context/admin/cdriver/",new String[]{
		"Cache.cfc"
		,"RamCache.cfc"
		//,"EHCache.cfc"
		,"Field.cfc"
		,"Group.cfc"}
		,cDir,doNew);
		
		
		Resource wcdDir = configDir.getRealResource("web-context-deployment/admin");
		Resource cdDir = wcdDir.getRealResource("cdriver");
		
		try {
			ResourceUtil.deleteEmptyFolders(wcdDir);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// Gateway Drivers
		Resource gDir = adminDir.getRealResource("gdriver");
		create("/resource/context/admin/gdriver/",new String[]{
		"TaskGatewayDriver.cfc","DirectoryWatcher.cfc","MailWatcher.cfc","Gateway.cfc","Field.cfc","Group.cfc"}
		,gDir,doNew);
		
		// Logging/appender
		Resource app = adminDir.getRealResource("logging/appender");
		create("/resource/context/admin/logging/appender/",new String[]{
		"ConsoleAppender.cfc","ResourceAppender.cfc","Appender.cfc","Field.cfc","Group.cfc"}
		,app,doNew);
		
		// Logging/layout
		Resource lay = adminDir.getRealResource("logging/layout");
		create("/resource/context/admin/logging/layout/",new String[]{
		"ClassicLayout.cfc","HTMLLayout.cfc","PatternLayout.cfc","XMLLayout.cfc","Layout.cfc","Field.cfc","Group.cfc"}
		,lay,doNew);
		
		// Security
		Resource secDir = configDir.getRealResource("security");
        if(!secDir.exists())secDir.mkdirs();
        Resource res = create("/resource/security/","cacerts",secDir,false);
		System.setProperty("javax.net.ssl.trustStore",res.toString());
		
        // ESAPI
        Resource propDir = configDir.getRealResource("properties");
        if(!propDir.exists())propDir.mkdirs();
        create("/resource/properties/","ESAPI.properties",propDir,doNew);
		System.setProperty("org.owasp.esapi.resources", propDir.toString());


		// Jacob
		if (SystemUtil.isWindows()) {

			Resource binDir = configDir.getRealResource("bin");
			if (binDir != null) {

				if (!binDir.exists())
					binDir.mkdirs();

				String name = (SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "jacob-x64.dll" : "jacob-i586.dll";

				Resource jacob = binDir.getRealResource(name);
				if (!jacob.exists()) {
					createFileFromResourceEL("/resource/bin/windows" + ((SystemUtil.getJREArch() == SystemUtil.ARCH_64) ? "64" : "32") + "/" + name, jacob);
				}
				// SystemOut.printDate(SystemUtil.PRINTWRITER_OUT,"set-property -> "+LibraryLoader.JACOB_DLL_PATH+":"+jacob.getAbsolutePath());
				System.setProperty(LibraryLoader.JACOB_DLL_PATH, jacob.getAbsolutePath());
				// SystemOut.printDate(SystemUtil.PRINTWRITER_OUT,"set-property -> "+LibraryLoader.JACOB_DLL_NAME+":"+name);
				System.setProperty(LibraryLoader.JACOB_DLL_NAME, name);
			}
		}
	}
	
}