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
package lucee.commons.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;

import lucee.commons.digest.MD5;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ClassLoaderHelper;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefInteger;
import lucee.commons.lang.types.RefIntegerImpl;
import lucee.loader.TP;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.StopException;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleReference;

import com.jezhumble.javasysmon.CpuTimes;
import com.jezhumble.javasysmon.JavaSysMon;
import com.jezhumble.javasysmon.MemoryStats;

/**
 * 
 */
public final class SystemUtil {

	public static final int MEMORY_TYPE_ALL=lucee.runtime.util.SystemUtil.MEMORY_TYPE_ALL;
	public static final int MEMORY_TYPE_HEAP=lucee.runtime.util.SystemUtil.MEMORY_TYPE_HEAP;
	public static final int MEMORY_TYPE_NON_HEAP=lucee.runtime.util.SystemUtil.MEMORY_TYPE_NON_HEAP;

	public static final int ARCH_UNKNOW=lucee.runtime.util.SystemUtil.ARCH_UNKNOW;
	public static final int ARCH_32=lucee.runtime.util.SystemUtil.ARCH_32;
	public static final int ARCH_64=lucee.runtime.util.SystemUtil.ARCH_64;

	public static final String SETTING_CONTROLLER_DISABLED = "lucee.controller.disabled";
	
	public static final char CHAR_DOLLAR=(char)36;
	public static final char CHAR_POUND=(char)163;
	public static final char CHAR_EURO=(char)8364;

	public static final int JAVA_VERSION_1_0 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_0;
	public static final int JAVA_VERSION_1_1 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_1;
	public static final int JAVA_VERSION_1_2 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_2;
	public static final int JAVA_VERSION_1_3 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_3;
	public static final int JAVA_VERSION_1_4 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_4;
	public static final int JAVA_VERSION_1_5 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_5;
	public static final int JAVA_VERSION_1_6 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_6;
	public static final int JAVA_VERSION_1_7 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_7;
	public static final int JAVA_VERSION_1_8 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_8;
	public static final int JAVA_VERSION_1_9 = lucee.runtime.util.SystemUtil.JAVA_VERSION_1_9;

	public static final int OUT = lucee.runtime.util.SystemUtil.OUT;
	public static final int ERR = lucee.runtime.util.SystemUtil.ERR;
	
	private static final PrintWriter PRINTWRITER_OUT = new PrintWriter(System.out);
	private static final PrintWriter PRINTWRITER_ERR = new PrintWriter(System.err);
	
	private static PrintWriter[] printWriter=new PrintWriter[2];
	
	public static final char SYMBOL_EURO = "\u20ac".charAt(0);
	public static final char SYMBOL_POUND = "\u00a3".charAt(0);
	public static final char SYMBOL_MICRO = "\u03bc".charAt(0);
	public static final char SYMBOL_A_RING = "\u00e5".charAt(0);
	
	
	private static final boolean isWindows;
	private static final boolean isSolaris;
	private static final boolean isLinux;
	private static final boolean isMacOSX;
	private static final boolean isUnix;
	
    private static Resource tempFile;
    private static Resource homeFile;
    private static Resource[] classPathes;
    private static CharSet charset;
    private static String lineSeparator=System.getProperty("line.separator","\n");
    private static MemoryPoolMXBean permGenSpaceBean;

	public static int osArch=-1;
	public static int jreArch=-1;
	
    
    private static final String JAVA_VERSION_STRING = System.getProperty("java.version");
    public static final int JAVA_VERSION;
    
	static {
		// OS
		String os = System.getProperty("os.name").toLowerCase();
		isWindows=os.startsWith("windows");
		isSolaris=os.startsWith("solaris");
		isLinux=os.startsWith("linux");
		isMacOSX=os.startsWith("mac os x");
		isUnix=!isWindows && File.separatorChar == '/'; // deprecated
		
		String strCharset=System.getProperty("file.encoding");
		if(strCharset==null || strCharset.equalsIgnoreCase("MacRoman"))
			strCharset="cp1252";

		if(strCharset.equalsIgnoreCase("utf-8")) charset=CharSet.UTF8;
		else if(strCharset.equalsIgnoreCase("iso-8859-1")) charset=CharSet.ISO88591;
		else charset=CharsetUtil.toCharSet(strCharset,null);
		
		// Perm Gen
		permGenSpaceBean=getPermGenSpaceBean();
		// make sure the JVM does not always a new bean
		MemoryPoolMXBean tmp = getPermGenSpaceBean();
		if(tmp!=permGenSpaceBean)permGenSpaceBean=null;
		
		if(JAVA_VERSION_STRING.startsWith("1.9.")) 		JAVA_VERSION=JAVA_VERSION_1_9;
    	else if(JAVA_VERSION_STRING.startsWith("1.8."))	JAVA_VERSION=JAVA_VERSION_1_8;
    	else if(JAVA_VERSION_STRING.startsWith("1.7.")) JAVA_VERSION=JAVA_VERSION_1_7;
    	else if(JAVA_VERSION_STRING.startsWith("1.6.")) JAVA_VERSION=JAVA_VERSION_1_6;
    	else if(JAVA_VERSION_STRING.startsWith("1.5.")) JAVA_VERSION=JAVA_VERSION_1_5;
    	else if(JAVA_VERSION_STRING.startsWith("1.4.")) JAVA_VERSION=JAVA_VERSION_1_4;
    	else if(JAVA_VERSION_STRING.startsWith("1.3.")) JAVA_VERSION=JAVA_VERSION_1_3;
    	else if(JAVA_VERSION_STRING.startsWith("1.2.")) JAVA_VERSION=JAVA_VERSION_1_2;
    	else if(JAVA_VERSION_STRING.startsWith("1.1.")) JAVA_VERSION=JAVA_VERSION_1_1;
    	else JAVA_VERSION=JAVA_VERSION_1_0;
	}


	private static ClassLoader loaderCL;
	private static ClassLoader coreCL;
	
	public static ClassLoader getLoaderClassLoader() {
		if(loaderCL==null)
			loaderCL=new TP().getClass().getClassLoader();
		return loaderCL;
	}
	
	public static ClassLoader getCoreClassLoader() {
		if(coreCL==null)
			coreCL=new ClassLoaderHelper().getClass().getClassLoader();
		return coreCL;
	}
	
	public static MemoryPoolMXBean getPermGenSpaceBean() {
		java.util.List<MemoryPoolMXBean> manager = ManagementFactory.getMemoryPoolMXBeans();
		MemoryPoolMXBean bean;
		// PERM GEN
		Iterator<MemoryPoolMXBean> it = manager.iterator();
		while(it.hasNext()){
			bean = it.next();
			if("Perm Gen".equalsIgnoreCase(bean.getName()) || "CMS Perm Gen".equalsIgnoreCase(bean.getName())) {
				return bean;
			}
		}
		it = manager.iterator();
		while(it.hasNext()){
			bean = it.next();
			if(StringUtil.indexOfIgnoreCase(bean.getName(),"Perm Gen")!=-1 || StringUtil.indexOfIgnoreCase(bean.getName(),"PermGen")!=-1) {
				return bean;
			}
		}
		// take none-heap when only one
		it = manager.iterator();
		LinkedList<MemoryPoolMXBean> beans=new LinkedList<MemoryPoolMXBean>();
		while(it.hasNext()){
			bean = it.next();
			if(bean.getType().equals(MemoryType.NON_HEAP)) {
				beans.add(bean);
				return bean;
			}
		}
		if(beans.size()==1) return beans.getFirst();
		
		// Class Memory/ClassBlock Memory?
		it = manager.iterator();
		while(it.hasNext()){
			bean = it.next();
			if(StringUtil.indexOfIgnoreCase(bean.getName(),"Class Memory")!=-1) {
				return bean;
			}
		}
		return null;
	}
	
    private static Boolean isFSCaseSensitive;
	private static JavaSysMon jsm;
	private static Boolean isCLI;
	private static double loaderVersion=0D;
	private static String macAddress; 

    /**
     * returns if the file system case sensitive or not
     * @return is the file system case sensitive or not
     */
    public static boolean isFSCaseSensitive() { 
        if(isFSCaseSensitive==null) { 
                try { 
                	_isFSCaseSensitive(File.createTempFile("abcx","txt"));
                } 
                catch (IOException e) { 
            		File f = new File("abcx.txt").getAbsoluteFile();
            		try {
						f.createNewFile();
	                    _isFSCaseSensitive(f);
						
					} catch (IOException e1) {
						throw new RuntimeException(e1.getMessage());
					}
                } 
        } 
        return isFSCaseSensitive.booleanValue(); 
    }
    private static void _isFSCaseSensitive(File f) { 
        File temp=new File(f.getPath().toUpperCase()); 
        isFSCaseSensitive=temp.exists()?Boolean.FALSE:Boolean.TRUE; 
        f.delete(); 
    }


	/**
	 * fixes a java canonical path to a Windows path
	 * e.g. /C:/Windows/System32 will be changed to C:\Windows\System32
	 *
	 * @param path
	 * @return
	 */
	public static String fixWindowsPath(String path) {
		if ( isWindows && path.length() > 3 && path.charAt(0) == '/' && path.charAt(2) == ':' ) {
			path = path.substring(1).replace( '/', '\\' );
		}
		return path;
	}


    /**
     * @return is local machine a Windows Machine
     */
    public static boolean isWindows() {
        return isWindows;
    }

    /**
     * @return is local machine a Linux Machine
     */
    public static boolean isLinux() {
        return isLinux;
    }

    /**
     * @return is local machine a Solaris Machine
     */
    public static boolean isSolaris() {
        return isSolaris;
    }

    /**
     * @return is local machine a Solaris Machine
     */
    public static boolean isMacOSX() {
        return isMacOSX;
    }

    /**
     * @return is local machine a Unix Machine
     */
    public static boolean isUnix() {
        return isUnix;
    }
    

    /**
     * returns the Temp Directory of the System
     * @return temp directory
     * @throws IOException 
     */
    public static Resource getTempDirectory() {
    	return ResourcesImpl.getFileResourceProvider().getResource(CFMLEngineFactory.getTempDirectory().getAbsolutePath());
    	
    	/*if(tempFile!=null) return tempFile;
        ResourceProvider fr = ResourcesImpl.getFileResourceProvider();
        String tmpStr = System.getProperty("java.io.tmpdir");
        if(tmpStr!=null) {
            tempFile=fr.getResource(tmpStr);
            if(tempFile.exists()) {
                tempFile=ResourceUtil.getCanonicalResourceEL(tempFile);
                return tempFile;
            }
        }
        File tmp =null;
        try {
        	tmp = File.createTempFile("a","a");
            tempFile=fr.getResource(tmp.getParent());
            tempFile=ResourceUtil.getCanonicalResourceEL(tempFile);   
        }
        catch(IOException ioe) {}
        finally {
        	if(tmp!=null)tmp.delete();
        }
        return tempFile;*/
    }
    
    /**
     * returns the a unique temp file (with no auto delete)
     * @param extension 
     * @return temp directory
     * @throws IOException 
     */
    public static Resource getTempFile(String extension, boolean touch) throws IOException {
    	String filename=CreateUniqueId.invoke();
    	if(!StringUtil.isEmpty(extension,true)){
    		if(extension.startsWith("."))filename+=extension;
    		else filename+="."+extension;
    	}
		Resource file = getTempDirectory().getRealResource(filename);
		if(touch)ResourceUtil.touch(file);
		return file;
	}
    
    
    /**
     * @return return System directory
     */
    public static Resource getSystemDirectory() {
    	return ResourcesImpl.getFileResourceProvider().getResource(CFMLEngineFactory.getSystemDirectory().getAbsolutePath());
    	
        /*String pathes=System.getProperty("java.library.path");
        ResourceProvider fr = ResourcesImpl.getFileResourceProvider();
        if(pathes!=null) {
            String[] arr=ListUtil.toStringArrayEL(ListUtil.listToArray(pathes,File.pathSeparatorChar));
            for(int i=0;i<arr.length;i++) {    
                if(arr[i].toLowerCase().indexOf("windows\\system")!=-1) {
                    Resource file = fr.getResource(arr[i]);
                    if(file.exists() && file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file);
                    
                }
            }
            for(int i=0;i<arr.length;i++) {    
                if(arr[i].toLowerCase().indexOf("windows")!=-1) {
                	Resource file = fr.getResource(arr[i]);
                    if(file.exists() && file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file);
                    
                }
            }
            for(int i=0;i<arr.length;i++) {    
                if(arr[i].toLowerCase().indexOf("winnt")!=-1) {
                	Resource file = fr.getResource(arr[i]);
                    if(file.exists() && file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file);
                    
                }
            }
            for(int i=0;i<arr.length;i++) {    
                if(arr[i].toLowerCase().indexOf("win")!=-1) {
                	Resource file = fr.getResource(arr[i]);
                    if(file.exists() && file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file);
                    
                }
            }
            for(int i=0;i<arr.length;i++) {
            	Resource file = fr.getResource(arr[i]);
                if(file.exists() && file.isDirectory() && file.isWriteable()) return ResourceUtil.getCanonicalResourceEL(file);
            }
        }
        return null;*/
    }
    
    /**
     * @return return running context root
     */
    public static Resource getRuningContextRoot() {
    	ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
        
        try {
            return frp.getResource(".").getCanonicalResource();
        } catch (IOException e) {}
        URL url=InfoImpl.class.getClassLoader().getResource(".");
        try {
            return frp.getResource(FileUtil.URLToFile(url).getAbsolutePath());
        } catch (MalformedURLException e) {
            return null;
        }
    }
        
    /**
     * returns the Hoome Directory of the System
     * @return home directory
     */
    public static Resource getHomeDirectory() {
        if(homeFile!=null) return homeFile;
        
        ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
        
        String homeStr = System.getProperty("user.home");
        if(homeStr!=null) {
            homeFile=frp.getResource(homeStr);
            homeFile=ResourceUtil.getCanonicalResourceEL(homeFile);
        }
        return homeFile;
    }
    
    
    public static Resource getClassLoaderDirectory(){
    	return ResourceUtil.toResource(CFMLEngineFactory.getClassLoaderRoot(TP.class.getClassLoader()));
    }

    /**
     * get class pathes from all url ClassLoaders
     * @param ucl URL Class Loader
     * @param pathes Hashmap with allpathes
     */
    private static void getClassPathesFromClassLoader(URLClassLoader ucl, ArrayList<Resource> pathes) {
        ClassLoader pcl=ucl.getParent();
        // parent first
        if(pcl instanceof URLClassLoader)
            getClassPathesFromClassLoader((URLClassLoader) pcl, pathes);

        ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
        // get all pathes
        URL[] urls=ucl.getURLs();
        for(int i=0;i<urls.length;i++) {
            Resource file=frp.getResource(urls[i].getPath());
            if(file.exists())
                pathes.add(ResourceUtil.getCanonicalResourceEL(file));
        }
        
    }
    
    /**
     * @return returns a string list of all pathes
     */
    public static Resource[] getClassPathes() {
        
        if(classPathes!=null) 
            return classPathes;
        
        ArrayList<Resource> pathes=new ArrayList<Resource>();
        String pathSeperator=System.getProperty("path.separator");
        if(pathSeperator==null)pathSeperator=";";
            
    // java.ext.dirs
        ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
    	
        
    // pathes from system properties
        String strPathes=System.getProperty("java.class.path");
        if(strPathes!=null) {
            Array arr=ListUtil.listToArrayRemoveEmpty(strPathes,pathSeperator);
            int len=arr.size();
            for(int i=1;i<=len;i++) {
                Resource file=frp.getResource(Caster.toString(arr.get(i,""),"").trim());
                if(file.exists())
                    pathes.add(ResourceUtil.getCanonicalResourceEL(file));
            }
        }
        
        
    // pathes from url class Loader (dynamic loaded classes)
        ClassLoader cl = InfoImpl.class.getClassLoader();
        if(cl instanceof URLClassLoader) 
            getClassPathesFromClassLoader((URLClassLoader) cl, pathes);
        
        return classPathes=(Resource[]) pathes.toArray(new Resource[pathes.size()]);
        
    }

    public static long getUsedMemory() {
        Runtime r = Runtime.getRuntime();
        return r.totalMemory()-r.freeMemory();
    }
    public static long getAvailableMemory() {
        Runtime r = Runtime.getRuntime();
        return r.freeMemory();
    }

    /**
     * replace path placeholder with the real path, placeholders are [{temp-directory},{system-directory},{home-directory}]
     * @param path
     * @return updated path
     */
    public static String parsePlaceHolder(String path) {
        return CFMLEngineFactory.parsePlaceHolder(path);
    }
    
    public static String addPlaceHolder(Resource file, String defaultValue) {
     // Temp
        String path=addPlaceHolder(getTempDirectory(),file,"{temp-directory}");
        if(!StringUtil.isEmpty(path)) return path;
     // System
        path=addPlaceHolder(getSystemDirectory(),file,"{system-directory}");
        if(!StringUtil.isEmpty(path)) return path;
     // Home
        path=addPlaceHolder(getHomeDirectory(),file,"{home-directory}");
        if(!StringUtil.isEmpty(path)) return path;
        
      
        return defaultValue;
    }
    
    private static String addPlaceHolder(Resource dir, Resource file,String placeholder) {
    	if(ResourceUtil.isChildOf(file, dir)){
        	try {
				return StringUtil.replace(file.getCanonicalPath(), dir.getCanonicalPath(), placeholder, true);
			} 
        	catch (IOException e) {}
        }
    	return null;
	}
    

	public static String addPlaceHolder(Resource file,  Config config, String defaultValue) {
    	//ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
    	
        // temp
        	Resource dir = config.getTempDirectory();
        	String path = addPlaceHolder(dir,file,"{temp-directory}");
        	if(!StringUtil.isEmpty(path)) return path;
            	
        // Config 
        	dir = config.getConfigDir();
        	path = addPlaceHolder(dir,file,"{lucee-config-directory}");
        	if(!StringUtil.isEmpty(path)) return path;

        // Web root
        	dir = config.getRootDirectory();
        	path = addPlaceHolder(dir,file,"{web-root-directory}");
        	if(!StringUtil.isEmpty(path)) return path;

        return addPlaceHolder(file, defaultValue);
    }
	
	public static String parsePlaceHolder(String path, ServletContext sc, Map<String,String> labels) {
		if(path==null) return null;
        if(path.indexOf('{')!=-1){
        	if((path.indexOf("{web-context-label}"))!=-1){
        		String id=hash(sc);
        		
        		String label=labels.get(id);
        		if(StringUtil.isEmpty(label)) label=id;
        		
        		path=StringUtil.replace(path, "{web-context-label}", label, false);
        	}
        }
        return parsePlaceHolder(path, sc);
    }
    
	public static String parsePlaceHolder(String path, ServletContext sc) {
    	ResourceProvider frp = ResourcesImpl.getFileResourceProvider();
    	
    	
        if(path==null) return null;
        if(path.indexOf('{')!=-1){
        	if(StringUtil.startsWith(path,'{')){
	            
	            // Web Root
	            if(path.startsWith("{web-root")) {
	                if(path.startsWith("}",9)) 					path=frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(10)).toString();
	                else if(path.startsWith("-dir}",9)) 		path=frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(14)).toString();
	                else if(path.startsWith("-directory}",9)) 	path=frp.getResource(ReqRspUtil.getRootPath(sc)).getRealResource(path.substring(20)).toString();
	
	            }
	            else path=SystemUtil.parsePlaceHolder(path);
	        }
        	
        	if((path.indexOf("{web-context-hash}"))!=-1){
        		String id=hash(sc);
        		path=StringUtil.replace(path, "{web-context-hash}", id, false);
        	}
        }
        return path;
    }
	
	public static String hash(ServletContext sc) {
    	String id=null;
		try {
			id=MD5.getDigestAsString(ReqRspUtil.getRootPath(sc));
		} 
		catch (IOException e) {}
		return id;
    }

    public static Charset getCharset() {
    	return CharsetUtil.toCharset(charset);
    }
    public static CharSet getCharSet() {
    	return charset;
    }

	public static void setCharset(String charset) {
		SystemUtil.charset = CharsetUtil.toCharSet(charset);
	}
	public static void setCharset(Charset charset) {
		SystemUtil.charset = CharsetUtil.toCharSet(charset);
	}

	public static String getOSSpecificLineSeparator() {
		return lineSeparator;
	}

	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}
	public static void join(Thread t) {
		try {
			t.join();
		} catch (InterruptedException e) {}
	}
	
	/**
	 * locks the object (synchronized) before calling wait
	 * @param lock
	 * @param timeout
	 * @throws InterruptedException
	 */
	public static void wait(Object lock, long timeout) {
		try {
			synchronized (lock) {lock.wait(timeout);}
		} catch (InterruptedException e) {}
	}

	/**
	 * locks the object (synchronized) before calling wait (no timeout)
	 * @param lock
	 * @throws InterruptedException
	 */
	public static void wait(Object lock) {
		try {
			synchronized (lock) {lock.wait();}
		} catch (InterruptedException e) {}
	}
	
	
	
	/**
	 * locks the object (synchronized) before calling notify
	 * @param lock
	 * @param timeout
	 * @throws InterruptedException
	 */
	public static void notify(Object lock) {
		synchronized (lock) {lock.notify();}
	}
	
	/**
	 * locks the object (synchronized) before calling notifyAll
	 * @param lock
	 * @param timeout
	 * @throws InterruptedException
	 */
	public static void notifyAll(Object lock) {
		synchronized (lock) {lock.notifyAll();}
	}

	/**
	 * return the operating system architecture
	 * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
	 */
	public static int getOSArch(){
		if(osArch==-1) {
			osArch = toIntArch(System.getProperty("os.arch.data.model"));
			if(osArch==ARCH_UNKNOW)osArch = toIntArch(System.getProperty("os.arch"));
		}
		return osArch;
	}
	
	/**
	 * return the JRE (Java Runtime Engine) architecture, this can be different from the operating system architecture
	 * @return one of the following SystemUtil.ARCH_UNKNOW, SystemUtil.ARCH_32, SystemUtil.ARCH_64
	 */
	public static int getJREArch(){
		if(jreArch==-1) {
			jreArch = toIntArch(System.getProperty("sun.arch.data.model"));
			if(jreArch==ARCH_UNKNOW)jreArch = toIntArch(System.getProperty("com.ibm.vm.bitmode"));
			if(jreArch==ARCH_UNKNOW)jreArch = toIntArch(System.getProperty("java.vm.name"));
			if(jreArch==ARCH_UNKNOW) {
				int addrSize = getAddressSize();
				if(addrSize==4) return ARCH_32;
				if(addrSize==8) return ARCH_64;
			}
			
		}
		return jreArch;
	}
	
	private static int toIntArch(String strArch){
		if(!StringUtil.isEmpty(strArch)) {
			if(strArch.indexOf("64")!=-1) return ARCH_64;
			if(strArch.indexOf("32")!=-1) return ARCH_32;
			if(strArch.indexOf("i386")!=-1) return ARCH_32;
			if(strArch.indexOf("x86")!=-1) return ARCH_32;
		}
		return ARCH_UNKNOW;
	}
	

	
	public static int getAddressSize() {
		try {
			Class unsafe = ClassUtil.loadClass("sun.misc.Unsafe",null);
			if(unsafe==null) return 0;
		
			Field unsafeField = unsafe.getDeclaredField("theUnsafe");
		    unsafeField.setAccessible(true);
		    Object obj = unsafeField.get(null);
		    Method addressSize = unsafe.getMethod("addressSize", new Class[0]);
		    
		    Object res = addressSize.invoke(obj, new Object[0]);
		    return Caster.toIntValue(res,0);
		}
		catch(Throwable t){
			return 0;
		}
	    
	}
	/*private static MemoryUsage getPermGenSpaceSize() {
		MemoryUsage mu = getPermGenSpaceSize(null);
		if(mu!=null) return mu;
		
		// create error message including info about available memory blocks
		StringBuilder sb=new StringBuilder();
		java.util.List<MemoryPoolMXBean> manager = ManagementFactory.getMemoryPoolMXBeans();
		Iterator<MemoryPoolMXBean> it = manager.iterator();
		MemoryPoolMXBean bean;
		while(it.hasNext()){
			bean = it.next();
			if(sb.length()>0)sb.append(", ");
			sb.append(bean.getName());
		}
		throw new RuntimeException("PermGen Space information not available, available Memory blocks are ["+sb+"]");
	}*/
	
	private static MemoryUsage getPermGenSpaceSize(MemoryUsage defaultValue) {
		if(permGenSpaceBean!=null) return permGenSpaceBean.getUsage();
		// create on the fly when the bean is not permanent
		MemoryPoolMXBean tmp = getPermGenSpaceBean();
		if(tmp!=null) return tmp.getUsage();
		
		return defaultValue;
	}
	

	public static long getFreePermGenSpaceSize() {
		MemoryUsage mu = getPermGenSpaceSize(null);
		if(mu==null) return -1;
		
		long max = mu.getMax();
		long used = mu.getUsed();
		if(max<0 || used<0) return -1;
		return max-used;
	}
	
	public static int getPermGenFreeSpaceAsAPercentageOfAvailable() { 	
		MemoryUsage mu = getPermGenSpaceSize(null);
		if(mu == null) return -1;
		
		long max = mu.getMax();
		long used = mu.getUsed();
		if( max < 0 || used < 0) return -1;
		
		//return a value that equates to a percentage of available free memory
		return 100 - ((int)(100 * (((double)used) / ((double)max))));
	}
	
	public static int getFreePermGenSpacePromille() {
		MemoryUsage mu = getPermGenSpaceSize(null);
		if(mu==null) return -1;
		
		long max = mu.getMax();
		long used = mu.getUsed();
		if(max<0 || used<0) return -1;
		return (int)(1000L-(1000L*used/max));
	}
	
	public static Query getMemoryUsageAsQuery(int type) throws DatabaseException {
		
		
		java.util.List<MemoryPoolMXBean> manager = ManagementFactory.getMemoryPoolMXBeans();
		Iterator<MemoryPoolMXBean> it = manager.iterator();
		Query qry=new QueryImpl(new Collection.Key[]{
				KeyConstants._name,
				KeyConstants._type,
				KeyConstants._used,
				KeyConstants._max,
				KeyConstants._init
		},0,"memory");
		
		int row=0;
		MemoryPoolMXBean bean;
		MemoryUsage usage;
		MemoryType _type;
		while(it.hasNext()){
			bean = it.next();
			usage = bean.getUsage();
			_type = bean.getType();
			if(type==MEMORY_TYPE_HEAP && _type!=MemoryType.HEAP)continue;
			if(type==MEMORY_TYPE_NON_HEAP && _type!=MemoryType.NON_HEAP)continue;
				
			row++;
			qry.addRow();
			qry.setAtEL(KeyConstants._name, row, bean.getName());
			qry.setAtEL(KeyConstants._type, row, _type.name());
			qry.setAtEL(KeyConstants._max, row, Caster.toDouble(usage.getMax()));
			qry.setAtEL(KeyConstants._used, row, Caster.toDouble(usage.getUsed()));
			qry.setAtEL(KeyConstants._init, row, Caster.toDouble(usage.getInit()));
			
		}
		return qry;
	}
	
	public static Struct getMemoryUsageAsStruct(int type) {
		java.util.List<MemoryPoolMXBean> manager = ManagementFactory.getMemoryPoolMXBeans();
		Iterator<MemoryPoolMXBean> it = manager.iterator();
		
		MemoryPoolMXBean bean;
		MemoryUsage usage;
		MemoryType _type;
		long used=0,max=0,init=0;
		while(it.hasNext()){
			bean = it.next();
			usage = bean.getUsage();
			_type = bean.getType();
			if((type==MEMORY_TYPE_HEAP && _type==MemoryType.HEAP) || (type==MEMORY_TYPE_NON_HEAP && _type==MemoryType.NON_HEAP)){
				used+=usage.getUsed();
				max+=usage.getMax();
				init+=usage.getInit();
			}
		}
		Struct sct=new StructImpl();
		sct.setEL(KeyConstants._used, Caster.toDouble(used));
		sct.setEL(KeyConstants._max, Caster.toDouble(max));
		sct.setEL(KeyConstants._init, Caster.toDouble(init));
		sct.setEL(KeyImpl.init("available"), Caster.toDouble(max-used));
		return sct;
	}
	

	public static Struct getMemoryUsageCompact(int type) {
		java.util.List<MemoryPoolMXBean> manager = ManagementFactory.getMemoryPoolMXBeans();
		Iterator<MemoryPoolMXBean> it = manager.iterator();
		
		MemoryPoolMXBean bean;
		MemoryUsage usage;
		MemoryType _type;
		Struct sct=new StructImpl();
		while(it.hasNext()){
			bean = it.next();
			usage = bean.getUsage();
			_type = bean.getType();
			if(type==MEMORY_TYPE_HEAP && _type!=MemoryType.HEAP)continue;
			if(type==MEMORY_TYPE_NON_HEAP && _type!=MemoryType.NON_HEAP)continue;
				
			double d=((int)(100D/usage.getMax()*usage.getUsed()))/100D;
			sct.setEL(KeyImpl.init(bean.getName()), Caster.toDouble(d));
		}
		return sct;
	}
	
	public static String getPropertyEL(String key) {
		try{
			String str = System.getProperty(key);
			if(!StringUtil.isEmpty(str,true)) return str;
			
			Iterator<Entry<Object, Object>> it = System.getProperties().entrySet().iterator();
			Entry<Object, Object> e;
			String n;
			while(it.hasNext()){
				e = it.next();
				n=(String) e.getKey();
				if(key.equalsIgnoreCase(n)) return (String) e.getValue();
			}
			
		}
		catch(Throwable t){}
		return null;
	}
	public static long microTime() {
		return System.nanoTime()/1000L;
	}
	
	public static TemplateLine getCurrentContext() {
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		
        int line=0;
		String template;
		
		StackTraceElement trace=null;
		for(int i=0;i<traces.length;i++) {
			trace=traces[i];
			template=trace.getFileName();
			if(trace.getLineNumber()<=0 || template==null || ResourceUtil.getExtension(template,"").equals("java")) continue;
			line=trace.getLineNumber();
			return new TemplateLine(template,line);
		}
		return null;
	}
	
	public static class TemplateLine implements Serializable {

		private static final long serialVersionUID = 6610978291828389799L;

		public final String template;
		public final int line;

		public TemplateLine(String template, int line) {
			this.template=template;
			this.line=line;
		}
		@Override
		public String toString(){
			return template+":"+line;
		}
	}

	public static long getFreeBytes() throws ApplicationException {
		return physical().getFreeBytes();
	}

	public static long getTotalBytes() throws ApplicationException {
		return physical().getTotalBytes();
	}
	
	public static double getCpuUsage(long time) throws ApplicationException {
		if(time<1) throw new ApplicationException("time has to be bigger than 0");
		if(jsm==null) jsm=new JavaSysMon();
		CpuTimes cput = jsm.cpuTimes();
		if(cput==null) throw new ApplicationException("CPU information are not available for this OS");
		CpuTimes previous = new CpuTimes(cput.getUserMillis(),cput.getSystemMillis(),cput.getIdleMillis());
        sleep(time);
        
        return jsm.cpuTimes().getCpuUsage(previous)*100D;
    }
	

	private synchronized static MemoryStats physical() throws ApplicationException {
		if(jsm==null) jsm=new JavaSysMon();
		MemoryStats p = jsm.physical();
		if(p==null) throw new ApplicationException("Memory information are not available for this OS");
		return p;
	}
	public static void setPrintWriter(int type,PrintWriter pw) {
		printWriter[type]=pw;
	}
	public static PrintWriter getPrintWriter(int type) {
		if(printWriter[type]==null) {
			if(type==OUT) printWriter[OUT]=PRINTWRITER_OUT;
			else printWriter[ERR]=PRINTWRITER_ERR;
		}
		return printWriter[type];
	}
	public static boolean isCLICall() {
    	if(isCLI==null){
    		isCLI=Caster.toBoolean(System.getProperty("lucee.cli.call"),Boolean.FALSE);
    	}
    	return isCLI.booleanValue();
	}
	
	public static double getLoaderVersion() {
		// this is done via reflection to make it work in older version, where the class lucee.loader.Version does not exist
		if(loaderVersion==0D) {
			loaderVersion=4D;
			Class cVersion = ClassUtil.loadClass(getLoaderClassLoader(),"lucee.loader.Version",null);
			if(cVersion!=null) {
				try {
					Field f = cVersion.getField("VERSION");
					loaderVersion=f.getDouble(null);
				} 
				catch (Throwable t) {t.printStackTrace();}
			}
		}
		return loaderVersion;
	}
	
	public static String getMacAddress() {
		if(macAddress==null) {
			try{
				InetAddress ip = InetAddress.getLocalHost();
				NetworkInterface network = NetworkInterface.getByInetAddress(ip);
				byte[] mac = network.getHardwareAddress();
		  
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < mac.length; i++) {
					sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
				}
				macAddress= sb.toString();
			}
			catch(Throwable t){}
			
		}
		return macAddress;
	}
	
	public static URL getResource(Bundle bundle, String path) {
		String pws,pns;
		if(path.startsWith("/")) {
			pws=path;
			pns=path.substring(1);
		}
		else {
			pws="/"+path;
			pns=path;
		}
		
		URL url=null;
    	
    		Properties prop = new Properties();
    		if(bundle!=null) {
	    		try {
	    			url = bundle.getEntry(pns);
	    		}
	    		catch (Throwable t) {}
    		}
    		
    		// core class loader
    		if(url==null) {
	    		Class clazz = PageSourceImpl.class;
	    		ClassLoader cl = clazz.getClassLoader();
	    		url=cl.getResource(pns);
	    		if(url==null) {
		    		url=cl.getResource(pws);
	    		}
    		}
    		return url;

	}
	


	/**
	 * returns a system setting by either a Java property name or a System environment variable
	 * 
	 * @param propOrEnv - either a lowercased Java property name (e.g. lucee.controller.disabled) or an UPPERCASED Environment variable name ((e.g. LUCEE_CONTROLLER_DISABLED))
	 * @param defaultValue - value to return if the neither the property nor the environment setting was found 
	 * @return - the value of the property referenced by propOrEnv or the defaultValue if not found
	 */
	public static String getSetting(String propOrEnv, String defaultValue) {
		
		String v = System.getProperty(propOrEnv);
		
		if (v != null)
			return v;
		
		v = System.getenv(propOrEnv.replace('.', '_').toUpperCase());
		
		if (v != null)
			return v;
		
		return defaultValue;		
	}

    
    public static void addLibraryPathIfNoExist(Resource res,Log log){
    	String existing = System.getProperty("java.library.path");
    	
    	if(StringUtil.isEmpty(existing)) {
    		if(log!=null)log.info("Instrumentation","add "+res.getAbsolutePath()+" to library path");
            System.setProperty("java.library.path",res.getAbsolutePath());
    	}
    	else if(existing.indexOf(res.getAbsolutePath())!=-1){
    		return;
    	}
		else {
			if(log!=null)log.info("Instrumentation","add "+res.getAbsolutePath()+" to library path");
            System.setProperty("java.library.path",res.getAbsolutePath() + (isWindows()?";":":") + existing);
		}
    	// Important: java.library.path is cached
        // We will be using reflection to clear the cache
    	try{
	        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
	        fieldSysPath.setAccessible(true);
	        fieldSysPath.set(null, null);
    	}
    	catch(Throwable t){}

    }

	@Deprecated
	public static void stop(Thread thread) {
		if(thread.isAlive()){
			try{
				thread.stop(new StopException(thread));
			}
			catch(UnsupportedOperationException uoe){// Java 8 does not support Thread.stop(Throwable)
				thread.stop();
			}
		}
	}

	public static void stop(PageContext pc,Log log) {
		stop(pc,new StopException(pc.getThread()),log);
	}
	
	public static void stop(PageContext pc, Throwable t,Log log) {
		new StopThread(pc,t,log).start();
	}


    public static String getLocalHostName() {

        String result = System.getenv(isWindows() ? "COMPUTERNAME" : "HOSTNAME");

        if (!StringUtil.isEmpty(result))
            return result;

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "";
        }
    }
	public static InputStream getResourceAsStream(Bundle bundle, String path) {
		// check the bundle for the resource
		InputStream is;
		if(bundle!=null) {
    		try {
    			is = bundle.getEntry(path).openStream();
    			if(is!=null) return is;
    		}catch (Throwable t) {}
		}
		
		// try from core classloader
		ClassLoader cl = PageSourceImpl.class.getClassLoader();
		try{
			is = cl.getResourceAsStream(path);
			if(is!=null) return is;
		}catch (Throwable t) {}

		// try from loader classloader
		cl = PageSource.class.getClassLoader();
		try{
			is = cl.getResourceAsStream(path);
			if(is!=null) return is;
		}catch (Throwable t) {}

		// try from loader classloader
		cl = ClassLoader.getSystemClassLoader();
		try{
			is = cl.getResourceAsStream(path);
			if(is!=null) return is;
		}catch (Throwable t) {}
		
    return null;
}
	
	/**
	 * @return returns a class stack trace
	 */
	public static Class[] getClassContext() {
		final Ref ref=new Ref();
		new SecurityManager() {
	        {	
	        	ref.context = getClassContext();
	            
	        }
	    };
	    Class[] context= new Class[ref.context.length-2];
	    System.arraycopy(ref.context, 2,context, 0, ref.context.length-2);
		return context;
	}
	
	/**
	 * 
	 * @return the class calling me and the first class not in bootdelegation if the the is in bootdelegation
	 */
	public static Caller getCallerClass() {
		final Ref ref=new Ref();
		new SecurityManager() {
	        {	
	        	ref.context = getClassContext();
	            
	        }
	    };
	    
	    Caller rtn=new Caller();
	    
	    // element at position 2 is the caller
	    Class caller=ref.context[2];
	    RefInteger index=new RefIntegerImpl(3);
	    Class clazz=_getCallerClass(ref.context,caller,index,true,true);
	    
	    // analyze the first result
	    if(clazz==null) return rtn;
	    if(isFromBundle(clazz)) {
	    	rtn.fromBundle=clazz;
	    	return rtn;
	    }
	    if(!OSGiUtil.isInBootelegation(clazz.getName())) {
	    	rtn.fromSystem=clazz;
	    }
	    else {
	    	rtn.fromBootDelegation=clazz;
	    }
	    
	    clazz=null;
	    if(rtn.fromBootDelegation!=null) {
	    	clazz=_getCallerClass(ref.context,caller,index,false,true);
	    	if(clazz==null) return rtn;
	    	if(isFromBundle(clazz)) {
		    	rtn.fromBundle=clazz;
		    	return rtn;
		    }
	    	else rtn.fromSystem=clazz;
	    }
	    
	    
    	clazz=_getCallerClass(ref.context,caller,index,false,false);
    	if(clazz==null) return rtn;
    	rtn.fromBundle=clazz;
	    
	    
	    return rtn;
	}
	
	private static Class _getCallerClass(Class[] context, Class caller, RefInteger index, boolean acceptBootDelegation, boolean acceptSystem) {
		Class callerCaller;
		
		do{
	    	callerCaller=context[index.toInt()];
	    	index.plus(1);
	    	if(callerCaller==caller || _isSystem(callerCaller)) {
	    	    	callerCaller=null;
	    	}

	    	if(callerCaller!=null && !acceptSystem && !isFromBundle(callerCaller)) {
	    		callerCaller=null;
	    	}
	    	else if(callerCaller!=null && !acceptBootDelegation && OSGiUtil.isInBootelegation(callerCaller.getName())) {
	    		callerCaller=null;
	    	}
	    }
	    while(callerCaller==null && index.toInt()<context.length);
		return callerCaller;
	}

	public static class Caller {

		public Class fromBootDelegation;
		public Class fromSystem;
		public Class fromBundle;
		
		
		public String toString(){
			return "fromBootDelegation:"+fromBootDelegation+";fromSystem:"+fromSystem+";fromBundle:"+fromBundle;
		}

		public boolean isEmpty() {
			return fromBootDelegation==null && fromBundle==null && fromSystem==null;
		}

		public Class fromClasspath() { 
			if(fromSystem!=null) {
				if(fromSystem.getClassLoader()!=null)
					return fromSystem;
				if(fromBootDelegation!=null && fromBootDelegation.getClassLoader()!=null) return fromBootDelegation;
				return fromSystem;
			}
			return fromBootDelegation;
		}
	}
	

	private static boolean isFromBundle(Class clazz) {
		if(clazz==null) return false;
		if(!(clazz.getClassLoader() instanceof BundleReference))
			return false;
		
		BundleReference br=(BundleReference)clazz.getClassLoader();
		return !OSGiUtil.isFrameworkBundle(br.getBundle());
	}
	

	private static boolean _isSystem(Class clazz) {
		if(clazz.getName()=="java.lang.Class") return true; // Class.forName(className)
		if(clazz.getName().startsWith("com.sun.beans.finder.")) return true; 
		if(clazz.getName().startsWith("java.beans.")) return true; 
		if(clazz.getName().startsWith("java.util.ServiceLoader")) return true; 
		
		return false;
	}
}

class Ref {
	public Class[] context;
	
}

class StopThread extends Thread {
	
	private final PageContext pc;
	private final Throwable t;
	private final Log log; 

	public StopThread(PageContext pc, Throwable t, Log log) {
		this.pc=pc;
		this.t=t;
		this.log=log;
	}

	public void run(){
		PageContextImpl pci=(PageContextImpl) pc;
		Thread thread = pc.getThread();
		pci.setRequestTimeoutException(t);
		int count=0;
		if(thread.isAlive()) {
			do{
				if(count>10) break; // should never happen
				if(count>0 && log!=null) LogUtil.log(log, Log.LEVEL_ERROR, "", "could not stop the thread, trying again", thread.getStackTrace());
				
				try{
					thread.stop(t);
				}
				catch(UnsupportedOperationException uoe){
					LogUtil.log(log, Log.LEVEL_INFO, "", "Thread.stop(Throwable) is not supported by this JVM and failed with UnsupportedOperationException", thread.getStackTrace());
					try {
						// This is a private, native method on the java.lang.Thread object directly
						Method m = Thread.class.getDeclaredMethod("stop0", new Class[]{Object.class});
						m.setAccessible(true); // allow to access private method
						m.invoke(thread, new Object[]{t});
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | 
							NoSuchMethodException | SecurityException e) {
						LogUtil.log(log, Log.LEVEL_ERROR, "", t);
						thread.stop();
					}
				}
				SystemUtil.sleep(1000);
				count++;
			}
			while(thread.isAlive() && pci.isInitialized());
		}
		
		if(count>10 && log!=null) LogUtil.log(log, Log.LEVEL_ERROR, "", "could not stop the thread, giving up", thread.getStackTrace());
	}
}