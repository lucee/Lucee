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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.runtime.exp.PageException;
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
				catch (Throwable t) {
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
		catch (Throwable t) {}
		
		// TODO Auto-generated method stub
		
	}

	public static String unwrap(String value) {
		if(value==null) return "";
		String res = unwrap(value,'"');
		if(res!=null) return res; // was double quote
		
		return unwrap(value,'\''); // try single quote unwrap, when there is no double quote.
	}
	
	private static String unwrap(String value, char del) {
		value=value.trim();
		if(StringUtil.startsWith(value, del) && StringUtil.endsWith(value, del)) {
			return value.substring(1, value.length()-1);
		}
		return value;
	}
	
	
	
	public static void deployExtensions(Config config, String[] ids, Log log) {
		if(!ArrayUtil.isEmpty(ids)) {
	    	String id;
			for(int i=0;i<ids.length;i++){
	    		id=ids[i].trim();
	    		if(StringUtil.isEmpty(id,true)) continue;
	    		deployExtension(config, id.trim(),log,i+1==ids.length);
	    	}
	    }
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
	public static boolean deployExtension(Config config, String id, Log log, boolean reload) {
		ConfigImpl ci=(ConfigImpl) config;
		
		// is the extension already installed
		try {
			if(XMLConfigAdmin.hasRHExtensions(ci, id)!=null) return false;
		} 
		catch (Throwable t) {}
		
		
		// check if a local extension is matching our id
		Iterator<RHExtension> it = getLocalExtensions(config).iterator();
		RHExtension ext=null,tmp;
		while(it.hasNext()){
			tmp=it.next();
			if(tmp.getId().equals(id) && (ext==null || ext.getVersion().compareTo(tmp.getVersion())<0)) {
				ext=tmp;
			}
		}
		
		
		String apiKey = config.getIdentification().getApiKey();
		RHExtensionProvider[] providers = ci.getRHExtensionProviders();
		URL url;
		
		// if we have a local version, we look if there is a newer remote version
		if(ext!=null) {
			String content;
			for(int i=0;i<providers.length;i++){
				try{
					url=providers[i].getURL();
					url=new URL(url,"/rest/extension/provider/info/"+id+"?withLogo=false"+(apiKey==null?"":"&ioid="+apiKey));
					HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/json")});
					
					if(rsp.getStatusCode()!=200) continue;
					
					content=rsp.getContentAsString();
					Struct sct = Caster.toStruct(DeserializeJSON.call(null, content));
					String remoteVersion=Caster.toString(sct.get(KeyConstants._version));
					
					// the local version is as good as the remote
					if(remoteVersion!=null && remoteVersion.compareTo(ext.getVersion())<=0) {
						log.info("extension", "installing the extension "+id+" from local provider");
						
						// avoid that the exzension from provider get removed
						Resource res = SystemUtil.getTempFile("lex", true);
						IOUtil.copy(ext.getExtensionFile(), res);
						XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res, reload);
						return true;
					}
				}
				catch(Throwable t){
				}
				
			}
		}
		
		
		
		// if not we try to download it

		log.info("extension", "installing the extension "+id+" from remote extension provider");
		Resource res = downloadExtension(ci, id,null, log);
		if(res!=null) {
			try {
				XMLConfigAdmin._updateRHExtension((ConfigImpl) config, res,reload);
				return true;
			}
			catch(Throwable t){
				log.error("extension", t);
			}
		}
		return false;
		/*for(int i=0;i<providers.length;i++){
			try{
				url=providers[i].getURL();
				url=new URL(url,"/rest/extension/provider/full/"+id+(apiKey==null?"":"?ioid="+apiKey));
				HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/cfml")});
				if(rsp.getStatusCode()!=200)
					throw new IOException("failed to load extension with id "+id);
				
				// copy it locally
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(rsp.getContentAsStream(), res, true);
				
				// now forward it to the regular process
				XMLConfigAdmin.updateRHExtension((ConfigImpl) config, res,true);
				return true;
			}
			catch(Throwable t){
				log.error("extension", t);
				
			}
		}
		return false;*/
	}
	
	public static Resource downloadExtension(Config config, String id, String version, Log log){

		String apiKey = config.getIdentification().getApiKey();
		URL url;
		RHExtensionProvider[] providers = ((ConfigImpl)config).getRHExtensionProviders();
		for(int i=0;i<providers.length;i++){
			try{
				url=providers[i].getURL();
				
				StringBuilder qs=new StringBuilder();
				addQueryParam(qs,"ioid",apiKey);
				addQueryParam(qs,"version",version);
				
				
				url=new URL(url,"/rest/extension/provider/full/"+id+qs);
				
				
				
				HTTPResponse rsp = HTTPEngine.get(url, null, null, -1, false, "UTF-8", "", null, new Header[]{new HeaderImpl("accept","application/cfml")});
				if(rsp.getStatusCode()!=200)
					throw new IOException("failed to load extension with id "+id);
				
				// copy it locally
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(rsp.getContentAsStream(), res, true);
				
				return res;
			}
			catch(Throwable t) {
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

	public static Resource getExtension(Config config, String id, String version, Log log) {
		// local
		RHExtension ext = getLocalExtension(config, id,version,null);
		if(ext!=null && ext.getExtensionFile().exists()) {
			try {
				Resource res = SystemUtil.getTempFile("lex", true);
				IOUtil.copy(ext.getExtensionFile(), res);
				return res;
			}
			catch(IOException ioe){}
		}
		
		// remote
		return downloadExtension(config, id, version, log);
	}
	
	
	public static RHExtension getLocalExtension(Config config, String id, String version, RHExtension defaultValue) {
		Iterator<RHExtension> it = getLocalExtensions(config).iterator();
		RHExtension ext;
		while(it.hasNext()){
			ext=it.next();
			if(ext.getId().equals(id) && (version==null || version.equals(ext.getVersion()))) {
				return ext;
			}
		}
		return defaultValue;
	}

	public static List<RHExtension> getLocalExtensions(Config config) {
		Resource[] locReses = config.getLocalExtensionProviderDirectory().listResources(new ExtensionResourceFilter(".lex"));
		List<RHExtension> loc=new ArrayList<RHExtension>();
		Map<String,String> map=new HashMap<String,String>();
		RHExtension ext;
		String v;
		for(int i=0;i<locReses.length;i++) {
			try {
				ext=new RHExtension(config,locReses[i],false);
				// check if we already have an extension with the same id to avoid having more than once
				v=map.get(ext.getId());
				if(v!=null && v.compareToIgnoreCase(ext.getId())>0) continue;
				
				map.put(ext.getId(), ext.getVersion());
				loc.add(ext);
			} 
			catch(Exception e) {}
		}
		return loc;
	}
	
}