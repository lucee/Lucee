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
package lucee.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import lucee.commons.io.FileUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.MappingUtil;
import lucee.commons.lang.PCLCollection;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.listener.ApplicationListener;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.util.ArrayUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**  
 * Mapping class
 */
public final class MappingImpl implements Mapping {

	private static final long serialVersionUID = 6431380676262041196L;
	
	private String virtual;
	private String lcVirtual;
	private boolean topLevel;
	private short inspect;
	private boolean physicalFirst;
	private PhysicalClassLoader pcl;
	private PCLCollection pcoll;
	private Resource archive;
	
	private boolean hasArchive;
	private final Config config;
	private Resource classRootDirectory;
	private PageSourcePool pageSourcePool=new PageSourcePool();
	
	private boolean readonly=false;
	private boolean hidden=false;
	private final String strArchive;
	
	private final String strPhysical;
	private Resource physical;
	
	private String lcVirtualWithSlash;
	private Map<String, SoftReference<Object>> customTagPath = new ConcurrentHashMap<String, SoftReference<Object>>();
	
	private boolean appMapping;
	private boolean ignoreVirtual;

	private ApplicationListener appListener;

	private Bundle archiveBundle;

	private long archMod;

	private int listenerMode;
	private int listenerType;

	/**
	 * constructor of the class
	 * @param config
	 * @param virtual
	 * @param strPhysical
	 * @param strArchive
	 * @param inspect
	 * @param physicalFirst
	 * @param hidden
	 * @param readonly
	 * @param topLevel
	 * @param appMapping
	 * @param ignoreVirtual
	 * @param appListener
	 */
	public MappingImpl(Config config, String virtual, String strPhysical,String strArchive, short inspect, 
			boolean physicalFirst, boolean hidden, boolean readonly,boolean topLevel, boolean appMapping, 
			boolean ignoreVirtual,ApplicationListener appListener,int listenerMode,int listenerType) {
		this.ignoreVirtual=ignoreVirtual;
		this.config=config;
		this.hidden=hidden;
		this.readonly=readonly;
		this.strPhysical=StringUtil.isEmpty(strPhysical)?null:strPhysical;
		this.strArchive=StringUtil.isEmpty(strArchive)?null:strArchive;
		this.inspect=inspect;
		this.topLevel=topLevel;
		this.appMapping=appMapping;
		this.physicalFirst=physicalFirst;
		this.appListener=appListener;
		this.listenerMode=listenerMode;
		this.listenerType=listenerType;
		
		// virtual
		if(virtual.length()==0)virtual="/";
		if(!virtual.equals("/") && virtual.endsWith("/"))this.virtual=virtual.substring(0,virtual.length()-1);
		else this.virtual=virtual;
		this.lcVirtual=this.virtual.toLowerCase();
		this.lcVirtualWithSlash=lcVirtual.endsWith("/")?this.lcVirtual:this.lcVirtual+'/';

		ServletContext cs = (config instanceof ConfigWebImpl)?((ConfigWebImpl)config).getServletContext():null;
		
		
		// Physical
		physical=ConfigWebUtil.getExistingResource(cs,strPhysical,null,config.getConfigDir(),FileUtil.TYPE_DIR,
				config);
		// Archive
		archive=ConfigWebUtil.getExistingResource(cs,strArchive,null,config.getConfigDir(),FileUtil.TYPE_FILE,config);
		loadArchive();
		
		hasArchive=archive!=null;

		if(archive==null) this.physicalFirst=true;
		else if(physical==null) this.physicalFirst=false;
		else this.physicalFirst=physicalFirst;
		
		
		//if(!hasArchive && !hasPhysical) throw new IOException("missing physical and archive path, one of them must be defined");
	}
	
	private void loadArchive() {
		if(archive==null || archMod==archive.lastModified()) return;
		
		CFMLEngine engine = ConfigWebUtil.getEngine(config);
		BundleContext bc = engine.getBundleContext();
		try {
			archiveBundle=OSGiUtil.installBundle( bc, archive,true);
		}
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			archMod=archive.lastModified();
			config.getLog("application").log(Log.LEVEL_ERROR, "OSGi", t);
			archive=null;
		}
	}
	
	@Override
	public Class<?> getArchiveClass(String className) throws ClassNotFoundException {
		if(archiveBundle!=null) {
			return archiveBundle.loadClass(className);
		}
		//else if(archiveClassLoader!=null) return archiveClassLoader.loadClass(className);
		throw new ClassNotFoundException("there is no archive context to load "+className+" from it");
	}
	
	@Override
	public Class<?> getArchiveClass(String className, Class<?> defaultValue) {
		try {
			if(archiveBundle!=null) 
				return archiveBundle.loadClass(className);
			//else if(archiveClassLoader!=null) return archiveClassLoader.loadClass(className);
		}
		catch (ClassNotFoundException e) {}
		
		return defaultValue;
	}
	
	@Override
	public InputStream getArchiveResourceAsStream(String name) {
		// MUST implement
		return null;
	}

	
	
	


	public Class<?> loadClass(String className) {
		Class<?> clazz;
		if(isPhysicalFirst()) {
			clazz=getPhysicalClass(className,(Class<?>)null);
			if(clazz!=null) return clazz;
			clazz=getArchiveClass(className, null);
			if(clazz!=null) return clazz;
		}
		
		clazz=getArchiveClass(className, null);
		if(clazz!=null) return clazz;
		clazz=getPhysicalClass(className,(Class<?>)null);
		if(clazz!=null) return clazz;
		
		return null;
	}

	public PCLCollection touchClassLoader() throws IOException {
		if(pcoll==null){
			pcoll=new PCLCollection(this,getClassRootDirectory(),getConfig().getClassLoader(),100);
		}
		return pcoll;
	}
	
	private PhysicalClassLoader touchPhysicalClassLoader() throws IOException {
		if(pcl==null){
			pcl=new PhysicalClassLoader(config,getClassRootDirectory());
		}
		return pcl;
	}
	
	@Override
	public Class<?> getPhysicalClass(String className) throws ClassNotFoundException,IOException {
		return touchPhysicalClassLoader().loadClass(className);
		//return touchClassLoader().loadClass(className);
	}
	
	public Class<?> getPhysicalClass(String className, Class<?> defaultValue) {
		try {
			return getPhysicalClass(className);
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	public Class<?> getPhysicalClass(String className, byte[] code) throws IOException {
		
		try {
			return touchPhysicalClassLoader().loadClass(className,code);
		} catch (UnmodifiableClassException e) {
			throw new IOException(e);
		}

		//boolean isCFC = className.indexOf("_cfc$")!=-1;//aaaa ResourceUtil.getExtension(ps.getRealpath(), "").equalsIgnoreCase("cfc"); 
		//return touchClassLoader().loadClass(className,code,isCFC);
	}



	
	

	/**
	 * remove all Page from Pool using this classloader
	 * @param cl
	 */
	public void clearPages(ClassLoader cl){
		pageSourcePool.clearPages(cl);
	}
	
	@Override
	public Resource getPhysical() {
		return physical;
	}

	@Override
	public String getVirtualLowerCase() {
		return lcVirtual;
	}
	@Override
	public String getVirtualLowerCaseWithSlash() {
		return lcVirtualWithSlash;
	}	

	@Override
	public Resource getArchive() {
		//initArchive();
		return archive;
	}

	@Override
	public boolean hasArchive() {
		return hasArchive;
	}
	
	@Override
	public boolean hasPhysical() {
		return physical!=null;
	}

	@Override
	public Resource getClassRootDirectory() {
		if(classRootDirectory==null) {
			String path=getPhysical()!=null?
					getPhysical().getAbsolutePath():
					getArchive().getAbsolutePath();
			
			classRootDirectory=config.getClassDirectory().getRealResource(
										StringUtil.toIdentityVariableName(
												path)
								);
		}
		return classRootDirectory;
	}
	
	/**
	 * clones a mapping and make it readOnly
	 * @param config
	 * @return cloned mapping
	 * @throws IOException
	 */
	public MappingImpl cloneReadOnly(ConfigImpl config) {
		return new MappingImpl(config,virtual,strPhysical,strArchive,inspect,physicalFirst,hidden,true,topLevel,appMapping,ignoreVirtual,appListener,listenerMode,listenerType);
	}

	@Override
	public short getInspectTemplate() {
		if(inspect==Config.INSPECT_UNDEFINED) return config.getInspectTemplate();
		return inspect;
	}
	
	/**
	 * inspect template setting (Config.INSPECT_*), if not defined with the mapping, Config.INSPECT_UNDEFINED is returned
	 * @return
	 */
	public short getInspectTemplateRaw() {
		return inspect;
	}
	
	
	

	@Override
	public PageSource getPageSource(String realPath) {
		boolean isOutSide = false;
		realPath=realPath.replace('\\','/');
		if(realPath.indexOf('/')!=0) {
			if(realPath.startsWith("../")) {
				isOutSide=true;
			}
			else if(realPath.startsWith("./")) {
				realPath=realPath.substring(1);
			}
			else {
				realPath="/"+realPath;
			}
		}
		return getPageSource(realPath,isOutSide);
	}
	
	@Override
	public PageSource getPageSource(String path, boolean isOut) {
		PageSource source=pageSourcePool.getPageSource(path,true);
		if(source!=null) return source;

		PageSourceImpl newSource = new PageSourceImpl(this,path,isOut);
		pageSourcePool.setPage(path,newSource);
		
		return newSource;//new PageSource(this,path);
	}
	
	/**
	 * @return Returns the pageSourcePool.
	 */
	public PageSourcePool getPageSourcePool() {
		return pageSourcePool;
	}

	@Override
	public void check() {
		//if(config instanceof ConfigServer) return;
		//ConfigWebImpl cw=(ConfigWebImpl) config;
		ServletContext cs = (config instanceof ConfigWebImpl)?((ConfigWebImpl)config).getServletContext():null;
		
		
		// Physical
		if(getPhysical()==null && strPhysical!=null && strPhysical.length()>0) {
			physical=ConfigWebUtil.getExistingResource(cs,strPhysical,null,config.getConfigDir(),FileUtil.TYPE_DIR,config);
			
		}
		// Archive
		if(getArchive()==null && strArchive!=null && strArchive.length()>0) {
			
				archive=ConfigWebUtil.getExistingResource(cs,strArchive,null,config.getConfigDir(),FileUtil.TYPE_FILE,config);
				loadArchive();
				
				hasArchive=archive!=null;
			
		}
	}

	@Override
	public Config getConfig() {
		return config;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public boolean isPhysicalFirst() {
		return physicalFirst;
	}

	@Override
	public boolean isReadonly() {
		return readonly;
	}

	@Override
	public String getStrArchive() {
		return strArchive;
	}

	@Override
	public String getStrPhysical() {
		return strPhysical;
	}

	@Override
	@Deprecated
	public boolean isTrusted() {
		return getInspectTemplate()==Config.INSPECT_NEVER;
	}

	@Override
	public String getVirtual() {
		return virtual;
	}

	public boolean isAppMapping() {
		return appMapping;
	}


	@Override
	public boolean isTopLevel() {
		return topLevel;
	}
	
	public PageSource getCustomTagPath(String name, boolean doCustomTagDeepSearch) {
		return searchFor(name, name.toLowerCase().trim(), doCustomTagDeepSearch);
	}
	
	public boolean ignoreVirtual(){
		return ignoreVirtual;
	}
	
	
	private PageSource searchFor(String filename, String lcName, boolean doCustomTagDeepSearch) {
		PageSource source=getPageSource(filename);
		if(isOK(source)) {
			return source;
		}
		customTagPath.remove(lcName);
		if(doCustomTagDeepSearch){
			source = MappingUtil.searchMappingRecursive(this, filename, false);
			if(isOK(source)) return source;
		}
		return null;
	}

	public static boolean isOK(PageSource ps) {
		if(ps==null) return false;
		return ps.executable();
	}

	public static PageSource isOK(PageSource[] arr) {
		if(ArrayUtil.isEmpty(arr)) return null;
		for(int i=0;i<arr.length;i++) {
			if(isOK(arr[i])) return arr[i];
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return "StrPhysical:"+getStrPhysical()+";"+
		 "StrArchive:"+getStrArchive()+";"+
		 "Virtual:"+getVirtual()+";"+
		 "Archive:"+getArchive()+";"+
		 "Physical:"+getPhysical()+";"+
		 "topLevel:"+topLevel+";"+
		 "inspect:"+ConfigWebUtil.inspectTemplate(getInspectTemplateRaw(),"")+";"+
		 "physicalFirst:"+physicalFirst+";"+
		 "readonly:"+readonly+";"+
		 "hidden:"+hidden+";";
	}

	public ApplicationListener getApplicationListener() {
		if(appListener!=null) return appListener;
		return config.getApplicationListener();
	}
	
	public boolean getDotNotationUpperCase(){
		return ((ConfigImpl)config).getDotNotationUpperCase();
	}

	public void shrink() {
		// MUST implement
		
	}

	@Override
	public int getListenerMode() {
		return listenerMode;
	}

	@Override
	public int getListenerType() {
		return listenerType;
	}

	public void flush() {
		getPageSourcePool().clear();
	}
}