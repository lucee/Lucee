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
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.extension.ExtensionDefintion;
import lucee.runtime.extension.RHExtension;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;

public class DeployHandler {

	private static final ResourceFilter ALL_EXT = new ExtensionResourceFilter(new String[]{".lex",".lar",".lco"});
	//private static final ResourceFilter ARCHIVE_EXT = new ExtensionResourceFilter(new String[]{".ra",".ras"});

	/**
	 * deploys all files found 
	 * @param config
	 */
	public static void deploy(Config config){
		if(!contextIsValid(config)) return;

		synchronized (config) {
			Resource dir = config.getDeployDirectory();
			if(!dir.exists()) dir.mkdirs();
			
			Resource[] children = dir.listResources(ALL_EXT);
			Resource child;
			String ext;
			for(int i=0;i<children.length;i++){
				child=children[i];
				try {
					// Lucee archives
					ext=ResourceUtil.getExtension(child, null);
					if("lar".equalsIgnoreCase(ext)) {
						//deployArchive(config,child,true);
						XMLConfigAdmin.updateArchive((ConfigImpl) config, child,true);
					}

					// Lucee Extensions
					else if("lex".equalsIgnoreCase(ext))
						XMLConfigAdmin._updateRHExtension((ConfigImpl) config, child,true);
					
					// Lucee core
					else if(config instanceof ConfigServer && "lco".equalsIgnoreCase(ext))
						XMLConfigAdmin.updateCore((ConfigServerImpl) config, child,true);
				}
				catch(Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					Log log = config.getLog("deploy");
					log.error("Extension", t);
				}
			}
		}
	}

	private static boolean contextIsValid(Config config) {
		// this test is not very good but it works
		ConfigWeb[] webs;
		if(config instanceof ConfigWeb)
			webs =new ConfigWeb[]{((ConfigWeb)config)};
		else 
			webs=((ConfigServer)config).getConfigWebs();
		
		for(int i=0;i<webs.length;i++){
			try{
				ReqRspUtil.getRootPath(webs[i].getServletContext());
			}
			catch(Throwable t){
				ExceptionUtil.rethrowIfNecessary(t);
				return false;
			}
		}
		return true;
	}
	
	public static void moveToFailedFolder(Resource deployDirectory,Resource res) {
		Resource dir = deployDirectory.getRealResource("failed-to-deploy");
		Resource dst = dir.getRealResource(res.getName());
		dir.mkdirs();
		
		try {
			if(dst.exists()) dst.remove(true);
			ResourceUtil.moveTo(res, dst,true);
		}
		catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		
		// TODO Auto-generated method stub
		
	}

	
	
	public static boolean deployExtensions(Config config, ExtensionDefintion[] eds, Log log) {
		boolean allSucessfull=true;
		if(!ArrayUtil.isEmpty(eds)) {
	    	ExtensionDefintion ed;
			for(int i=0;i<eds.length;i++){
				ed=eds[i];
				if(StringUtil.isEmpty(ed.getId(),true)) continue;
	    		if(!deployExtension(config, ed,log,i+1==eds.length))
	    			allSucessfull=false;
	    	}
	    }
		return allSucessfull;
	}

	/**
	 * install a extension based on the given id and version
	 * @param config
	 * @param id the id of the extension
	 * @param version pass null if you don't need a specific version
	 * @return 
	 * @throws IOException 
	 * @throws PageException 
	 */
	public static boolean deployExtension(Config config, ExtensionDefintion ed, Log log, boolean reload) {
		ConfigImpl ci=(ConfigImpl) config;
		
		// is the extension already installed
		try {
			if(XMLConfigAdmin.hasRHExtensions(ci, ed)!=null) return false;
		} 
		catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		
		// check if a local extension is matching our id
		Iterator<RHExtension> it = getLocalExtensions(config).iterator();
		RHExtension ext=null,tmp;
		
		log.info("extension", "installing the extension "+ed);
		while(it.hasNext()){
			tmp=it.next();
			if(ed.equals(tmp)) {
				ext=tmp;
				break;
			}
		}
		
		// if we have one and also the defined version matches, there is no need to check online
		if(ext!=null && ed.getVersion()!=null) {
			try{
				log.info("extension", "installing the extension "+ed+" from local provider");
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(ext.getExtensionFile(), res);
				XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res, reload);
				return true;
			}
			catch(Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				ext=null;
				t.printStackTrace();
			}
		}
		
		String apiKey = config.getIdentification().getApiKey();
		RHExtensionProvider[] providers = ci.getRHExtensionProviders();
		URL url;
		
		// if we have a local version, we look if there is a newer remote version
		if(ext!=null) {
			String content;
			for(int i=0;i<providers.length;i++) {
				try{
					url=providers[i].getURL();
					StringBuilder qs=new StringBuilder();
					qs.append("?withLogo=false");
					if(ed.getVersion()!=null)qs.append("&version=").append(ed.getVersion());
					if(apiKey!=null)qs.append("&ioid=").append(apiKey);
					
					url=new URL(url,"/rest/extension/provider/info/"+ed.getId()+qs);
					HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/json")});
					
					if(rsp.getStatusCode()!=200) continue;
					
					content=rsp.getContentAsString();
					Struct sct = Caster.toStruct(DeserializeJSON.call(null, content));
					String remoteVersion=Caster.toString(sct.get(KeyConstants._version));
					
					// the local version is as good as the remote
					if(remoteVersion!=null && remoteVersion.compareTo(ext.getVersion())<=0) {
						log.info("extension", "installing the extension "+ed+" from local provider");
						
						// avoid that the exzension from provider get removed
						Resource res = SystemUtil.getTempFile("lex", true);
						IOUtil.copy(ext.getExtensionFile(), res);
						XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res, reload);
						return true;
					}
				}
				catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
			}
		}
		
		// if we have an ext at this stage this mean the remote providers was not acessible or have not this extension
		if(ext!=null) {
			try{
				log.info("extension", "installing the extension "+ed+" from local provider");
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(ext.getExtensionFile(), res);
				XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res, reload);
				return true;
			}
			catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
		}
		
		// if not we try to download it
		log.info("extension", "installing the extension "+ed+" from remote extension provider");
		Resource res = downloadExtension(ci, ed, log);
		if(res!=null) {
			try {
				XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res,reload);
				return true;
			}
			catch(Throwable t){
				ExceptionUtil.rethrowIfNecessary(t);
				log.error("extension", t);
			}
		}
		return false;
	}
	
	public static Resource downloadExtension(Config config, ExtensionDefintion ed, Log log){

		String apiKey = config.getIdentification().getApiKey();
		URL url;
		RHExtensionProvider[] providers = ((ConfigImpl)config).getRHExtensionProviders();
		for(int i=0;i<providers.length;i++){
			try{
				url=providers[i].getURL();
				
				StringBuilder qs=new StringBuilder();
				addQueryParam(qs,"ioid",apiKey);
				addQueryParam(qs,"version",ed.getVersion());
				
				
				url=new URL(url,"/rest/extension/provider/full/"+ed.getId()+qs);
				
				
				
				HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/cfml")});
				if(rsp.getStatusCode()!=200)
					throw new IOException("failed to load extension: "+ed);
				
				// copy it locally
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(rsp.getContentAsStream(), res, true);
				
				return res;
			}
			catch(Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if(log!=null)log.error("extension", t);
			}
		}
		return null;
	}
	
	

	private static void addQueryParam(StringBuilder qs, String name,String value) {
		if(StringUtil.isEmpty(value)) return;
		qs
		.append(qs.length()==0?"?":"&")
		.append(name)
		.append("=")
		.append(value);
	}

	public static Resource getExtension(Config config, ExtensionDefintion ed, Log log) {
		// local
		RHExtension ext = getLocalExtension(config, ed,null);
		if(ext!=null && ext.getExtensionFile().exists()) {
			try {
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(ext.getExtensionFile(), res);
				return res;
			}
			catch(IOException ioe){}
		}
		
		// remote
		return downloadExtension(config, ed, log);
	}
	
	
	public static RHExtension getLocalExtension(Config config, ExtensionDefintion ed, RHExtension defaultValue) {
		Iterator<RHExtension> it = getLocalExtensions(config).iterator();
		RHExtension ext;
		while(it.hasNext()){
			ext=it.next();
			if(ed.equals(ext)) {
				return ext;
			}
		}
		return defaultValue;
	}

	public static List<RHExtension> getLocalExtensions(Config config) {
		return ((ConfigImpl)config).loadLocalExtensions();
	}
}