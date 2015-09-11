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
package lucee.commons.io.res.type.s3;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceLock;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.net.s3.PropertiesImpl;

public final class S3ResourceProvider implements ResourceProviderPro {
	
	
	private int socketTimeout=-1;
	private int lockTimeout=20000;
	private int cache=20000;
	private ResourceLock lock;
	private String scheme="s3";
	private Map arguments;

	

	
	/**
	 * initalize ram resource
	 * @param scheme
	 * @param arguments
	 * @return RamResource
	 */
	@Override
	public ResourceProvider init(String scheme,Map arguments) {
		if(!StringUtil.isEmpty(scheme))this.scheme=scheme;
		
		if(arguments!=null) {
			this.arguments=arguments;
			// socket-timeout
			String strTimeout = (String) arguments.get("socket-timeout");
			if(strTimeout!=null) {
				socketTimeout=toIntValue(strTimeout,socketTimeout);
			}
			// lock-timeout
			strTimeout=(String) arguments.get("lock-timeout");
			if(strTimeout!=null) {
				lockTimeout=toIntValue(strTimeout,lockTimeout);
			}
			// cache
			String strCache=(String) arguments.get("cache");
			if(strCache!=null) {
				cache=toIntValue(strCache,cache);
			}
		}
		
		return this;
	}

	private int toIntValue(String str, int defaultValue) {
		try{
			return Integer.parseInt(str);
		}
		catch(Throwable t){
			return defaultValue;
		}
	}


	@Override
	public String getScheme() {
		return scheme;
	}
	
	@Override
	public Resource getResource(String path) {
		path=lucee.commons.io.res.util.ResourceUtil.removeScheme(scheme, path);
		S3 s3 = new S3();
		RefInteger storage=new RefIntegerImpl(S3Constants.STORAGE_UNKNOW);
		
		
		//path=loadWithOldPattern(s3,storage,path);
		path=loadWithNewPattern(s3,storage,path);
		
		return new S3Resource(s3,storage.toInt(),this,path,true);
	}

	
	public static String loadWithNewPattern(S3 s3,RefInteger storage, String path) {
		PageContext pc = ThreadLocalPageContext.get();
		Properties prop=null; 
		if(pc!=null){
			prop=pc.getApplicationContext().getS3();
		}
		if(prop==null) prop=new PropertiesImpl();
		
		int defaultLocation = prop.getDefaultLocation();
		storage.setValue(defaultLocation);
		String accessKeyId = prop.getAccessKeyId();
		String secretAccessKey = prop.getSecretAccessKey();
		
		int atIndex=path.indexOf('@');
		int slashIndex=path.indexOf('/');
		if(slashIndex==-1){
			slashIndex=path.length();
			path+="/";
		}
		int index;
		
		// key/id
		if(atIndex!=-1) {
			index=path.indexOf(':');
			if(index!=-1 && index<atIndex) {
				accessKeyId=path.substring(0,index);
				secretAccessKey=path.substring(index+1,atIndex);
				index=secretAccessKey.indexOf(':');
				if(index!=-1) {
					String strStorage=secretAccessKey.substring(index+1).trim().toLowerCase();
					secretAccessKey=secretAccessKey.substring(0,index);
					//print.out("storage:"+strStorage);
					storage.setValue(S3.toIntStorage(strStorage, defaultLocation));
				}
			}
			else accessKeyId=path.substring(0,atIndex);
		}
		path=prettifyPath(path.substring(atIndex+1));
		index=path.indexOf('/');
		s3.setHost(prop.getHost());
		if(index==-1){
			if(path.equalsIgnoreCase(S3Constants.HOST) || path.equalsIgnoreCase(prop.getHost())){
				s3.setHost(path);
				path="/";
			}
		}
		else {
			String host=path.substring(0,index);
			if(host.equalsIgnoreCase(S3Constants.HOST) || host.equalsIgnoreCase(prop.getHost())){
				s3.setHost(host);
				path=path.substring(index);
			}
		}
		
		
		s3.setSecretAccessKey(secretAccessKey);
		s3.setAccessKeyId(accessKeyId);
		
		return path;
	}

	private static String prettifyPath(String path) {
		path=path.replace('\\','/');
		return StringUtil.replace(path, "//", "/", false);
		// TODO /aaa/../bbb/
	}
	
	
	

	public static String loadWithOldPattern(S3 s3,RefInteger storage, String path) {
		
		
		String accessKeyId = null;
		String secretAccessKey = null;
		String host = null;
		//int port = 21;
		
		//print.out("raw:"+path);
		
		int atIndex=path.indexOf('@');
		int slashIndex=path.indexOf('/');
		if(slashIndex==-1){
			slashIndex=path.length();
			path+="/";
		}
		int index;
		
		// key/id
		if(atIndex!=-1) {
			index=path.indexOf(':');
			if(index!=-1 && index<atIndex) {
				accessKeyId=path.substring(0,index);
				secretAccessKey=path.substring(index+1,atIndex);
				index=secretAccessKey.indexOf(':');
				if(index!=-1) {
					String strStorage=secretAccessKey.substring(index+1).trim().toLowerCase();
					secretAccessKey=secretAccessKey.substring(0,index);
					//print.out("storage:"+strStorage);
					storage.setValue(S3.toIntStorage(strStorage, S3Constants.STORAGE_UNKNOW));
				}
			}
			else accessKeyId=path.substring(0,atIndex);
		}
		path=prettifyPath(path.substring(atIndex+1));
		index=path.indexOf('/');
		if(index==-1){
			host=path;
			path="/";
		}
		else {
			host=path.substring(0,index);
			path=path.substring(index);
		}
		
		s3.setHost(host);
		s3.setSecretAccessKey(secretAccessKey);
		s3.setAccessKeyId(accessKeyId);
		
		return path;
	}
	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	@Override
	public boolean isModeSupported() {
		return false;
	}

	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	@Override
	public void setResources(Resources res) {
		lock=res.createResourceLock(lockTimeout,true);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	/**
	 * @return the socketTimeout
	 */
	public int getSocketTimeout() {
		return socketTimeout;
	}

	/**
	 * @return the lockTimeout
	 */
	public int getLockTimeout() {
		return lockTimeout;
	}

	/**
	 * @return the cache
	 */
	public int getCache() {
		return cache;
	}

	@Override
	public Map getArguments() {
		return arguments;
	}

	@Override
	public char getSeparator() {
		return '/';
	}
	

}