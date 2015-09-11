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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.util.ResourceSupport;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.httpclient3.HTTPEngine3Impl;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.util.ListUtil;

import org.xml.sax.SAXException;

public final class S3Resource extends ResourceSupport {

	private static final long serialVersionUID = 2265457088552587701L;

	private static final long FUTURE=50000000000000L;
	
	private static final S3Info UNDEFINED=new Dummy("undefined",0,0,false,false,false);
	private static final S3Info ROOT=new Dummy("root",0,0,true,false,true);
	private static final S3Info LOCKED = new Dummy("locked",0,0,true,false,false);
	private static final S3Info UNDEFINED_WITH_CHILDREN = new Dummy("undefined with children 1",0,0,true,false,true);
	private static final S3Info UNDEFINED_WITH_CHILDREN2 = new Dummy("undefined with children 2",0,0,true,false,true);


	private final S3ResourceProvider provider;
	private final String bucketName;
	private String objectName;
	private final S3 s3;
	long infoLastAccess=0;
	private int storage=S3.STORAGE_UNKNOW;
	private int acl=S3.ACL_PUBLIC_READ;

	private boolean newPattern;

	private S3Resource(S3 s3,int storage, S3ResourceProvider provider, String buckedName,String objectName, boolean newPattern) {
		this.s3=s3;
		this.provider=provider;
		this.bucketName=buckedName;
		this.objectName=objectName;
		this.storage=storage;
		this.newPattern=newPattern;
	}
	

	S3Resource(S3 s3,int storage, S3ResourceProvider provider, String path, boolean newPattern) {
		this.s3=s3;
		this.provider=provider;
		this.newPattern=newPattern;
		
		if(path.equals("/") || StringUtil.isEmpty(path,true)) {
			this.bucketName=null;
			this.objectName="";
		}
		else {
			path=ResourceUtil.translatePath(path, true, false);
			String[] arr = toStringArray( ListUtil.listToArrayRemoveEmpty(path,"/"));
			bucketName=arr[0];
			for(int i=1;i<arr.length;i++) {
				if(Util.isEmpty(objectName))objectName=arr[i];
				else objectName+="/"+arr[i];
			}
			if(objectName==null)objectName="";
		}
		this.storage=storage;
		
	}

	public  static String[] toStringArray(Array array) {
        String[] arr=new String[array.size()];
        for(int i=0;i<arr.length;i++) {
            arr[i]=Caster.toString(array.get(i+1,""),"");
        }
        return arr;
    }


	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		try {
			provider.lock(this);
			if(isBucket()) {
				s3.putBuckets(bucketName, acl,storage);
			}
			else s3.put(bucketName, objectName+"/", acl, HTTPEngine3Impl.getEmptyEntity("application"));	
		}
		catch (IOException ioe) {
			throw ioe;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		finally {
			provider.unlock(this);
		}
		s3.releaseCache(getInnerPath());
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		if(isBucket()) throw new IOException("can't create file ["+getPath()+"], on this level (Bucket Level) you can only create directories");
		try {
			provider.lock(this);
			s3.put(bucketName, objectName, acl, HTTPEngine3Impl.getEmptyEntity("application"));
		} 
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		finally {
			provider.unlock(this);
		}
		s3.releaseCache(getInnerPath());
	}

	@Override
	public boolean exists() {
		
		return getInfo()
			.exists();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		ResourceUtil.checkGetInputStreamOK(this);
		provider.read(this);
		try {
			return IOUtil.toBufferedInputStream(s3.getInputStream(bucketName, objectName));
		} 
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public int getMode() {
		return 777;
	}

	@Override
	public String getName() {
		if(isRoot()) return "";
		if(isBucket()) return bucketName;
		return objectName.substring(objectName.lastIndexOf('/')+1);
	}

	@Override
	public boolean isAbsolute() {
		return true;
	}
	

	@Override
	public String getPath() {
		return getPrefix().concat(getInnerPath());
	}
	
	private String getPrefix()  {
		
		String aki=s3.getAccessKeyId();
		String sak=s3.getSecretAccessKey();
		
		StringBuilder sb=new StringBuilder(provider.getScheme()).append("://");
		
		if(!StringUtil.isEmpty(aki)){
			sb.append(aki);
			if(!StringUtil.isEmpty(sak)){
				sb.append(":").append(sak);
				if(storage!=S3Constants.STORAGE_UNKNOW){
					sb.append(":").append(S3.toStringStorage(storage,"us"));
				}
			}
			sb.append("@");
		}
		if(!newPattern)
			sb.append(s3.getHost());
		
		return sb.toString();
	}


	@Override
	public String getParent() {
		if(isRoot()) return null;
		return getPrefix().concat(getInnerParent());
	}
	
	private String getInnerPath() {
		if(isRoot()) return "/";
		return ResourceUtil.translatePath(bucketName+"/"+objectName, true, false);
	}
	
	private String getInnerParent() {
		if(isRoot()) return null;
		if(Util.isEmpty(objectName)) return "/";
		if(objectName.indexOf('/')==-1) return "/"+bucketName;
		String tmp=objectName.substring(0,objectName.lastIndexOf('/'));
		return ResourceUtil.translatePath(bucketName+"/"+tmp, true, false);
	}

	@Override
	public Resource getParentResource() {
		if(isRoot()) return null;
		return new S3Resource(s3,isBucket()?S3Constants.STORAGE_UNKNOW:storage,provider,getInnerParent(),newPattern);// MUST direkter machen
	}

	private boolean isRoot() {
		return bucketName==null;
	}
	
	private boolean isBucket() {
		return bucketName!=null && Util.isEmpty(objectName);
	}

	@Override
	public String toString() {
		return getPath();
	}
	
	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {

		ResourceUtil.checkGetOutputStreamOK(this);
		//provider.lock(this);
		
		try {
			byte[] barr = null;
			if(append){
				InputStream is=null;
				OutputStream os=null;
				try{
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					os=baos;
					IOUtil.copy(is=getInputStream(), baos,false,false);
					barr=baos.toByteArray();
				}
				catch (Exception e) {
					throw new PageRuntimeException(Caster.toPageException(e));
				}
				finally{
					IOUtil.closeEL(is);
					IOUtil.closeEL(os);
				}
			}
			S3ResourceOutputStream os = new S3ResourceOutputStream(s3,bucketName,objectName,acl);
			if(append && !(barr==null || barr.length==0))
				IOUtil.copy(new ByteArrayInputStream(barr),os,true,true);
			return os;
		}
		catch(IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
		finally {
			s3.releaseCache(getInnerPath());
		}
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath=ResourceUtil.merge(getInnerPath(), realpath);
		if(realpath.startsWith("../"))return null;
		return new S3Resource(s3,S3Constants.STORAGE_UNKNOW,provider,realpath,newPattern);
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public boolean isDirectory() {
		return getInfo().isDirectory();
	}

	@Override
	public boolean isFile() {
		return getInfo().isFile();
	}

	@Override
	public boolean isReadable() {
		return exists();
	}

	@Override
	public boolean isWriteable() {
		return exists();
	}

	@Override
	public long lastModified() {
		return getInfo().getLastModified();
	}

	private S3Info getInfo() {
		S3Info info = s3.getInfo(getInnerPath());
		
		if(info==null) {// || System.currentTimeMillis()>infoLastAccess
			if(isRoot()) {
				try {
					s3.listBuckets();
					info=ROOT;
				}
				catch (Exception e) {
					info=UNDEFINED;
				}
				infoLastAccess=FUTURE;
			}
			else {
				try {
					provider.read(this);
				} catch (IOException e) {
					return LOCKED;
				}
				try {	
					if(isBucket()) {
						Bucket[] buckets = s3.listBuckets();
						String name=getName();
						for(int i=0;i<buckets.length;i++) {
							if(buckets[i].getName().equals(name)) {
								info=buckets[i];
								infoLastAccess=System.currentTimeMillis()+provider.getCache();
								break;
							}
						}
					}
					else {
						try {
							// first check if the bucket exists
							// TODO not happy about this step
							Bucket[] buckets = s3.listBuckets();
							boolean bucketExists=false;
							for(int i=0;i<buckets.length;i++) {
								if(buckets[i].getName().equals(bucketName)) {
									bucketExists=true;
									break;
								}
							}
							
							if(bucketExists){
								String path = objectName;
								Content[] contents = s3.listContents(bucketName, path);
								if(contents.length>0) {
									boolean has=false;
									for(int i=0;i<contents.length;i++) {
										if(ResourceUtil.translatePath(contents[i].getKey(),false,false).equals(path)) {
											has=true;
											info=contents[i];
											infoLastAccess=System.currentTimeMillis()+provider.getCache();
											break;
										}
									}
									if(!has){
										for(int i=0;i<contents.length;i++) {
											if(ResourceUtil.translatePath(contents[i].getKey(),false,false).startsWith(path)) {
												info=UNDEFINED_WITH_CHILDREN;
												infoLastAccess=System.currentTimeMillis()+provider.getCache();
												break;
											}
										}
									}
								}
							}
						}
						catch(SAXException e) {
							
						}
					}

					if(info==null){
						info=UNDEFINED;
						infoLastAccess=System.currentTimeMillis()+provider.getCache();
					}
				}
				catch(Exception t) {
					return UNDEFINED;
				}
			}
			s3.setInfo(getInnerPath(), info);
		}
		return info;
	}

	@Override
	public long length() {
		return getInfo().getSize();
	}

	@Override
	public Resource[] listResources() {
		S3Resource[] children=null;
		try {
			if(isRoot()) {
				Bucket[] buckets = s3.listBuckets();
				children=new S3Resource[buckets.length];
				for(int i=0;i<children.length;i++) {
					children[i]=new S3Resource(s3,storage,provider,buckets[i].getName(),"",newPattern);
					s3.setInfo(children[i].getInnerPath(),buckets[i]);
				}
			}
			else if(isDirectory()){
				Content[] contents = s3.listContents(bucketName, isBucket()?null:objectName+"/");
				ArrayList<S3Resource> tmp = new ArrayList<S3Resource>();
				String key,name,path;
				int index;
				Set<String> names=new LinkedHashSet<String>();
				Set<String> pathes=new LinkedHashSet<String>();
				S3Resource r;
				boolean isb=isBucket();
				for(int i=0;i<contents.length;i++) {
					key=ResourceUtil.translatePath(contents[i].getKey(), false, false);
					if(!isb && !key.startsWith(objectName+"/")) continue;
					if(Util.isEmpty(key)) continue;
					index=key.indexOf('/',StringUtil.length(objectName)+1);
					if(index==-1) { 
						name=key;
						path=null;
					}
					else {
						name=key.substring(index+1);
						path=key.substring(0,index);
					}
					
					//print.out("1:"+key);
					//print.out("path:"+path);
					//print.out("name:"+name);
					if(path==null){
						names.add(name);
						tmp.add(r=new S3Resource(s3,storage,provider,contents[i].getBucketName(),key,newPattern));
						s3.setInfo(r.getInnerPath(),contents[i]);
					}
					else {
						pathes.add(path);
					}
				}
				
				Iterator<String> it = pathes.iterator();
				while(it.hasNext()) {
					path=it.next();
					if(names.contains(path)) continue;
					tmp.add(r=new S3Resource(s3,storage,provider,bucketName,path,newPattern));
					s3.setInfo(r.getInnerPath(),UNDEFINED_WITH_CHILDREN2);
				}
				
				//if(tmp.size()==0 && !isDirectory()) return null;
				
				children=tmp.toArray(new S3Resource[tmp.size()]);
			}
		}
		catch(Exception t) {
			t.printStackTrace();
			return null;
		}
		return children;
	}

	@Override
	public void remove(boolean force) throws IOException {
		if(isRoot()) throw new IOException("can not remove root of S3 Service");

		ResourceUtil.checkRemoveOK(this);
		
		
		boolean isd=isDirectory();
		if(isd) {
			Resource[] children = listResources();
			if(children.length>0) {
				if(force) {
					for(int i=0;i<children.length;i++) {
						children[i].remove(force);
					}
				}
				else {
					throw new IOException("can not remove directory ["+this+"], directory is not empty");
				}
			}
		}
		// delete res itself
		provider.lock(this);
		try {
			s3.delete(bucketName, isd?objectName+"/":objectName);
		} 
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		finally {
			s3.releaseCache(getInnerPath());
			provider.unlock(this);
		}
		
		
	}

	@Override
	public boolean setLastModified(long time) {
		s3.releaseCache(getInnerPath());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMode(int mode) throws IOException {
		s3.releaseCache(getInnerPath());
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setReadable(boolean readable) {
		s3.releaseCache(getInnerPath());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setWritable(boolean writable) {
		s3.releaseCache(getInnerPath());
		// TODO Auto-generated method stub
		return false;
	}


	public AccessControlPolicy getAccessControlPolicy() {
		String p = getInnerPath();
		try {
			AccessControlPolicy acp = s3.getACP(p);
			if(acp==null){
				acp=s3.getAccessControlPolicy(bucketName,  getObjectName());
				s3.setACP(p, acp);
			}
				
			
			return acp;
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}
	
	public void setAccessControlPolicy(AccessControlPolicy acp) {
		
		try {
			s3.setAccessControlPolicy(bucketName, getObjectName(),acp);
		} 
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
		finally {
			s3.releaseCache(getInnerPath());
		}
	}
	
	private String getObjectName() {
		if(!StringUtil.isEmpty(objectName) && isDirectory()) {
			return objectName+"/";
		}
		return objectName;
	}


	public void setACL(int acl) {
		this.acl=acl;
	}


	public void setStorage(int storage) {
		this.storage=storage;
	}
	


}


 class Dummy implements S3Info {

		private long lastModified;
		private long size;
		private boolean exists;
		private boolean file;
		private boolean directory;
		private String label;
	
	 
	public Dummy(String label,long lastModified, long size, boolean exists,boolean file, boolean directory) {
		this.label = label;
		this.lastModified = lastModified;
		this.size = size;
		this.exists = exists;
		this.file = file;
		this.directory = directory;
	}


	@Override
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return "Dummy:"+getLabel();
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}


	@Override
	public boolean exists() {
		return exists;
	}

	@Override
	public boolean isDirectory() {
		return directory;
	}

	@Override
	public boolean isFile() {
		return file;
	}

}