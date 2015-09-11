/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.osgi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.osgi.BundleCollection;
import lucee.loader.osgi.BundleUtil;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Requirement;

public class OSGiUtil {
	
	
	private static final FilenameFilter JAR_EXT_FILTER = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			};
	private static String[] bootDelegation; 


	/**
	 * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing bundle is unloaded first.
	 * @param factory
	 * @param context
	 * @param bundle
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Bundle installBundle(BundleContext context,Resource bundle, boolean checkExistence) throws IOException, BundleException {
		if(checkExistence) {
			BundleFile bf=new BundleFile(bundle);
			if(!bf.isBundle()) throw new BundleException(bundle+" is not a valid bundle!");
			
			Bundle existing = loadBundleFromLocal(context,bf.getSymbolicName(),bf.getVersion(),null);
			if(existing!=null) return existing;
		}
		
		return _loadBundle(context, bundle.getAbsolutePath(), bundle.getInputStream(), true);
	}

	
	
	/**
	 * does not check if the bundle already exists!
	 * @param context
	 * @param path
	 * @param is
	 * @param closeStream
	 * @return
	 * @throws BundleException
	 */
	private static Bundle _loadBundle(BundleContext context,String path, InputStream is, boolean closeStream) throws BundleException {
		
		log(Log.LEVEL_INFO,"add bundle:" + path);
		try {
			return BundleUtil.installBundle(context, path, is);
			//return context.installBundle(path, is);
		}
		finally {
			if(closeStream)CFMLEngineFactory.closeEL(is);
		}
	}
	
	
	
	/**
	 * only installs a bundle, if the bundle does not already exist, if the bundle exists the existing bundle is unloaded first.
	 * the bundle is not stored physically on the system.
	 * @param factory
	 * @param context
	 * @param bundle
	 * @return
	 * @throws IOException
	 * @throws BundleException
	 */
	public static Bundle installBundle(BundleContext context,InputStream bundleIS,boolean closeStream, boolean checkExistence) throws IOException, BundleException {
		// store locally to test the bundle
		String name=System.currentTimeMillis()+".tmp";
		Resource dir = SystemUtil.getTempDirectory();
		Resource tmp = dir.getRealResource(name);
		int count=0;
		while(tmp.exists())tmp=dir.getRealResource((count++)+"_"+name);
		IOUtil.copy(bundleIS, tmp, closeStream);
		
		try {
			return installBundle(context, tmp,checkExistence);
		}
		finally{
			tmp.delete();
		}
	}
	
	
	public static Version toVersion(String version, Version defaultValue) {
		if(StringUtil.isEmpty(version)) return defaultValue;
		// String[] arr = ListUtil.listToStringArray(version, '.');
		String[] arr;
		try {
			arr = ListUtil.toStringArrayTrim(ListUtil.listToArray(version.trim(), '.'));
		}
		catch (PageException e) {
			return defaultValue; // should not happen
		}
		
		Integer major,minor,micro;
		String qualifier;
		
		
		if(arr.length==1) {
			major=Caster.toInteger(arr[0],null);
			minor=0;
			micro=0;
			qualifier=null;
		}
		else if(arr.length==2) {
			major=Caster.toInteger(arr[0],null);
			minor=Caster.toInteger(arr[1],null);
			micro=0;
			qualifier=null;
		}
		else if(arr.length==3) {
			major=Caster.toInteger(arr[0],null);
			minor=Caster.toInteger(arr[1],null);
			micro=Caster.toInteger(arr[2],null);
			qualifier=null;
		}
		else {
			major=Caster.toInteger(arr[0],null);
			minor=Caster.toInteger(arr[1],null);
			micro=Caster.toInteger(arr[2],null);
			qualifier=arr[3];
		}
		
		if(major==null || minor==null || micro==null)
			return defaultValue;
		
		
		
		if(qualifier==null) 
			return new Version(major,minor,micro);
		return new Version(major,minor,micro,qualifier);
	}
	
	public static Version toVersion(String version) throws BundleException {
		Version v = toVersion(version,null);
		if(v!=null) return v;
		throw new BundleException("given version ["+version+"] is invalid, a valid version is following this pattern <major-number>.<minor-number>.<micro-number>[.<qualifier>]");
	}

	private static Manifest getManifest(Resource bundle) throws IOException {
		InputStream is=null;
		Manifest mf=null;
		try{
			is=bundle.getInputStream();
			ZipInputStream zis = new ZipInputStream(is);
			
		    ZipEntry entry;
		    
		    while ((entry = zis.getNextEntry()) != null && mf==null) {
		    	if("META-INF/MANIFEST.MF".equals(entry.getName())) {
		    		mf=new Manifest(zis);
		    	}
		    	zis.closeEntry();
		    }
		}
		finally {
			IOUtil.closeEL(is);
		}
		return mf;
	}

	/*public static FrameworkFactory getFrameworkFactory() throws Exception {
		ClassLoader cl = OSGiUtil.class.getClassLoader();
        java.net.URL url = cl.getResource("META-INF/services/org.osgi.framework.launch.FrameworkFactory");
        if (url != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            try {
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    s = s.trim();
                    // Try to load first non-empty, non-commented line.
                    if ((s.length() > 0) && (s.charAt(0) != '#')) {
                        return (FrameworkFactory) ClassUtil.loadInstance(cl, s);
                    }
                }
            }
            finally {
                if (br != null) br.close();
            }
        }
        throw new Exception("Could not find framework factory.");
    }*/
	
	
	/**
	 * tries to load a class with ni bundle defintion
	 * @param name
	 * @param version
	 * @param id
	 * @param startIfNecessary
	 * @return
	 * @throws BundleException
	 */
	public static Class loadClass(String className, Class defaultValue) {
		className=className.trim();
		
		
		
		
		CFMLEngine engine = CFMLEngineFactory.getInstance();
		BundleCollection bc = engine.getBundleCollection();
    	
		// first we try to load the class from the Lucee core
		try {
    		// load from core
			return bc.core.loadClass(className);
		}
    	catch (Throwable t) {} // class is not visible to the Lucee core
		
		// now we check all started bundled (not only bundles used by core)
		Bundle[] bundles = bc.getBundleContext().getBundles();
    	for(Bundle b:bundles){
    		if(b==bc.core) continue;
			try {
				return b.loadClass(className);
			} catch (Throwable t) {} // class is not visible to that bundle
    	}
    	
    	// now we check lucee loader (SystemClassLoader?)
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	try {
    		//print.e("loader:");
    		return factory.getClass().getClassLoader().loadClass(className);
		}
		catch (Throwable t) {}
    	
    	/*
    	try {
			return Class.forName(className);
		} catch (Throwable t3) {
			
		}*/
    	
    	// now we check bundles not loaded
    	Set<String> loaded=new HashSet<String>();
    	for(Bundle b:bundles){
    		loaded.add(b.getSymbolicName()+"|"+b.getVersion());
    	}
    	
    	
    	try {
	    	File dir=factory.getBundleDirectory();
			File[] children = dir.listFiles(JAR_EXT_FILTER);
	    	BundleFile bf;
	    	for(int i=0;i<children.length;i++){
	    		try {
	    		bf=new BundleFile(children[i]);
	    		
	    		if(bf.isBundle() && !loaded.contains(bf.getSymbolicName()+"|"+bf.getVersion()) && bf.hasClass(className)) {
	    			Bundle b=null;
	    			try {
	    				b = _loadBundle(bc.getBundleContext(), bf.getFile());
	    			}
	    			catch (IOException e) {}
	    			
	    			if(b!=null) {
	    				startIfNecessary(b);
	    				return b.loadClass(className);
	    			}
	    		}
	    		}
	    		catch(Throwable t2){t2.printStackTrace();}
	    	}
		}
		catch(Throwable t1){t1.printStackTrace();}
    	
		return defaultValue;
	}
		
	
	
	public static Bundle loadBundle(String name, Version version,Identification id, boolean startIfNecessary) throws BundleException {
		name=name.trim();
		
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	
    	
    	// check in loaded bundles
    	BundleContext bc = engine.getBundleContext();
    	Bundle[] bundles = bc.getBundles();
    	StringBuilder versionsFound=new StringBuilder();
    	for(Bundle b:bundles){
    		if(name.equalsIgnoreCase(b.getSymbolicName())) {
    			if(version==null || version.equals(b.getVersion())) {
    				if(startIfNecessary)startIfNecessary(b);
    				return b;
    			}
    			if(versionsFound.length()>0) versionsFound.append(", ");
        		versionsFound.append(b.getVersion().toString());
    		}
    	}
    	
    	// is it in jar directory but not loaded
    	BundleFile bf = _getBundleFile(factory, name, version, versionsFound);
    	if(bf!=null && bf.isBundle()) {
    		Bundle b=null;
			try {
				b = _loadBundle(bc, bf.getFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(b!=null) {
				if(startIfNecessary)startIfNecessary(b);
				return b;
			}
    	}
    	
    	// if not found try to download
    	if(version!=null) {
	    	try{
	    		File f = factory.downloadBundle(name, version.toString(),id);
		    	Bundle b = _loadBundle(bc, f);
		    	if(startIfNecessary)start(b);
		    	return b;
	    	}
	    	catch(Throwable t){}
		}
    	
    	String localDir="";
    	try {
			localDir = " ("+factory.getBundleDirectory()+")";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String upLoc="";
    	try {
    		upLoc = " ("+factory.getUpdateLocation()+")";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if(versionsFound.length()>0)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] is not available in version ["+version+"] locally"+localDir+" or from the update provider"+upLoc+", the following versions are available locally ["+versionsFound+"].");
    	if(version!=null)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] in version ["+version+"] is not available locally"+localDir+" or from the update provider"+upLoc+".");
    	throw new BundleException("The OSGi Bundle with name ["+name+"] is not available locally"+localDir+" or from the update provider"+upLoc+".");
    }
	
	/**
	 * this should be used when you not want to load a Bundle to the system
	 * @param name
	 * @param version
	 * @param id only necessray if downloadIfNecessary is set to true
	 * @param downloadIfNecessary
	 * @return
	 * @throws BundleException
	 */
	public static BundleFile getBundleFile(String name, Version version,Identification id, boolean downloadIfNecessary) throws BundleException {
		name=name.trim();
		
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	
    	
    	StringBuilder versionsFound=new StringBuilder();
    	
    	// is it in jar directory but not loaded
    	BundleFile bf=_getBundleFile(factory,name,version,versionsFound);
    	if(bf!=null) return bf;
    	
    	// if not found try to download
    	if(downloadIfNecessary && version!=null) {
	    	try{
	    		bf=new BundleFile(factory.downloadBundle(name, version.toString(),id));
	    		if(bf.isBundle()) return bf;
	    	}
	    	catch(Throwable t){}
		}
    	
    	if(versionsFound.length()>0)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] is not available in version ["+version+"] locally or from the update provider, the following versions are available locally ["+versionsFound+"].");
    	if(version!=null)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] in version ["+version+"] is not available locally or from the update provider.");
    	throw new BundleException("The OSGi Bundle with name ["+name+"] is not available locally or from the update provider.");
    }
	
	public static BundleFile getBundleFile(String name, Version version,Identification id, boolean downloadIfNecessary, BundleFile defaultValue) {
		name=name.trim();
		
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	
    	
    	StringBuilder versionsFound=new StringBuilder();
    	
    	// is it in jar directory but not loaded
    	BundleFile bf=_getBundleFile(factory,name,version,versionsFound);
    	if(bf!=null) return bf;
    	
    	// if not found try to download
    	if(downloadIfNecessary && version!=null) {
	    	try{
	    		bf=new BundleFile(factory.downloadBundle(name, version.toString(),id));
	    		if(bf.isBundle()) return bf;
	    	}
	    	catch(Throwable t){}
		}
    	
    	return defaultValue;
    }

	private static BundleFile _getBundleFile(CFMLEngineFactory factory, String name, Version version, StringBuilder versionsFound) {
		try{
    		File dir=factory.getBundleDirectory();
			
    		// first we check if there is a file match (fastest solution)
			if(version!=null){
				File jar = new File(dir, name + "-"
					+ version.toString().replace('.', '-') + (".jar"));
				if(jar.exists()) {
					BundleFile bf=new BundleFile(jar);
					if(bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
						if(version.equals(bf.getVersion())) {
							return bf;
		    			}
					}
				}
			}
			
			// now we check by Manifest comparsion
			File[] children = dir.listFiles(JAR_EXT_FILTER);
	    	BundleFile bf;
	    	for(int i=0;i<children.length;i++){
	    		bf=new BundleFile(children[i]);
	    		
	    		if(bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
	    			if(version==null || version.equals(bf.getVersion())) {
						return bf;
	    			}
	    			if(versionsFound!=null) {
	    				if(versionsFound.length()>0) versionsFound.append(", ");
	    				versionsFound.append(bf.getVersionAsString());
	    			}
	    		}
	    	}
    	}
    	catch(Exception e){}
		return null;
	}



	/**
	 * get all local bundles (even bundles not loaded/installed)
	 * @param name
	 * @param version
	 * @return
	 */
	public static List<BundleDefinition> getBundleDefinitions() {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return getBundleDefinitions(engine.getBundleContext());
	}
	
	
	public static List<BundleDefinition> getBundleDefinitions(BundleContext bc) {
		long start=System.currentTimeMillis();
		Set<String> set=new HashSet<>();
		List<BundleDefinition> list=new ArrayList<>();
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		list.add(new BundleDefinition(b));
    		set.add(b.getSymbolicName()+":"+b.getVersion());
    	}
    	// is it in jar directory but not loaded
    	start=System.currentTimeMillis();
    	CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		try{
    		File[] children = factory.getBundleDirectory().listFiles(JAR_EXT_FILTER);
	    	BundleFile bf;
	    	for(int i=0;i<children.length;i++){
	    		try {
		    		bf=new BundleFile(children[i]);
		    		if(bf.isBundle() && !set.contains(bf.getSymbolicName()+":"+bf.getVersion())) 
		    			list.add(new BundleDefinition(bf.getSymbolicName(),bf.getVersion()));
	    		}
	    		catch(Throwable t){t.printStackTrace();}
	    	}
    	}
    	catch(IOException ioe){}
    	
    	return list;
    }
	

	public static Bundle getBundleLoaded(String name, Version version, Bundle defaultValue) {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return getBundleLoaded(engine.getBundleContext(), name, version, defaultValue);
	}
	
	public static Bundle getBundleLoaded(BundleContext bc,String name, Version version, Bundle defaultValue) {
		name=name.trim();
		
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		if(name.equalsIgnoreCase(b.getSymbolicName())) {
    			if(version==null || version.equals(b.getVersion())) {
    				return b;
    			}
    		}
    	}
    	return defaultValue;
    }

	public static Bundle loadBundleFromLocal(String name, Version version, Bundle defaultValue) {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return loadBundleFromLocal(engine.getBundleContext(), name, version, defaultValue);
	}
	
	
	public static Bundle loadBundleFromLocal(BundleContext bc,String name, Version version, Bundle defaultValue) {
		name=name.trim();
		
		
    	
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		if(name.equalsIgnoreCase(b.getSymbolicName())) {
    			if(version==null || version.equals(b.getVersion())) {
    				return b;
    			}
    		}
    	}
    	
    	// is it in jar directory but not loaded
    	
    	CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		CFMLEngineFactory factory = engine.getCFMLEngineFactory();
		BundleFile bf = _getBundleFile(factory, name, version, null);
		if(bf!=null) {
			try {
				return _loadBundle(bc, bf.getFile());
			} 
			catch (Exception e) {}
		}
		
    	return defaultValue;
    }
	
	/**
	 * get local bundle, but does not download from update provider! 
	 * @param name
	 * @param version
	 * @return
	 * @throws BundleException 
	 */
	public static void removeLocalBundle(String name, Version version, boolean removePhysical) throws BundleException {
		name=name.trim();
		
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();

    	// stop loaded bundle
    	BundleContext bc = engine.getBundleContext();
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		if(name.equalsIgnoreCase(b.getSymbolicName())) {
    			if(version==null || version.equals(b.getVersion())) {
    				stopIfNecessary(b);
    				b.uninstall();
    			}
    		}
    	}
    	
    	if(!removePhysical) return;
    	
    	// remove file
    	BundleFile bf = _getBundleFile(factory, name, version, null);
    	if(bf!=null) bf.getFile().delete();
    }

	// bundle stuff
	public static void startIfNecessary(Bundle[] bundles) throws BundleException {
		for(Bundle b:bundles){
			startIfNecessary(b);
		}
	}
	public static Bundle startIfNecessary(Bundle bundle) throws BundleException {
		if(bundle.getState()==Bundle.ACTIVE) return bundle;
		return start(bundle);
	}
	
	public static Bundle start(Bundle bundle)
			throws BundleException {
		String fh = bundle.getHeaders().get("Fragment-Host");
		
		if (!Util.isEmpty(fh)) {
			log(Log.LEVEL_INFO,
					"do not start [" + bundle.getSymbolicName()
							+ "], because this is a fragment bundle for [" + fh
							+ "]");
			return bundle;
		}

		log(Log.LEVEL_INFO, "start bundle:" + bundle.getSymbolicName()
				+ ":" + bundle.getVersion().toString());
		
		try {
			BundleUtil.start(bundle);
		}
		catch(BundleException be){
			// check if required related bundles are missing and load them if necessary
			List<BundleDefinition> list = getRequirements(bundle);
			BundleDefinition bd;
			Iterator<BundleDefinition> it = list.iterator();
			while(it.hasNext()){
				bd=it.next();
				try{
					loadBundle(
							bd.name, 
							bd.version, 
							ThreadLocalPageContext
							.getConfig()
							.getIdentification(), true);
				}
				catch(BundleException _be){
					log(_be);
				}
			}
			BundleUtil.start(bundle);
		}
		return bundle;
	}

	public static void stopIfNecessary(Bundle bundle) throws BundleException {
		if(isFragment(bundle) || bundle.getState()!=Bundle.ACTIVE) return;
		stop(bundle);
	}

	public static void stop(Bundle b) throws BundleException {
		b.stop();
	}
	
	public static void uninstall(Bundle b) throws BundleException {
		b.uninstall();
	}

	public static boolean isFragment(Bundle bundle) {
		return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
	}
	
	public static boolean isFragment(BundleFile bf) {
		return !StringUtil.isEmpty(bf.getFragementHost(),true);
	}
	
	private static List<BundleDefinition> getRequirements(Bundle bundle) throws BundleException {
		List<BundleDefinition> rtn=new ArrayList<BundleDefinition>();
		BundleRevision br = bundle.adapt(BundleRevision.class);
		List<Requirement> requirements = br.getRequirements(null);
		Iterator<Requirement> it = requirements.iterator();
		Requirement r;
		Entry<String, String> e;
		String value,name;
		int index,start,end,op;
		BundleDefinition bd;
		
		while(it.hasNext()){
			r = it.next();
			Iterator<Entry<String, String>> iit = r.getDirectives().entrySet().iterator();
			while(iit.hasNext()){
				e = iit.next();
				if(!"filter".equals(e.getKey())) continue;
				value=e.getValue();
				
				// name
				index=value.indexOf("(osgi.wiring.bundle");
				if(index==-1) continue;
				start=value.indexOf('=',index);
				end=value.indexOf(')',index);
				if(start==-1 || end==-1 || end<start) continue;
				name=value.substring(start+1,end).trim();
				rtn.add(bd=new BundleDefinition(name));
				
				// version
				op=-1;
				index=value.indexOf("(bundle-version");
				if(index==-1) continue;
				end=value.indexOf(')',index);
				
				start=value.indexOf("<=",index);
				if(start!=-1 && start<end) {
					op=BundleDefinition.LTE;
					start+=2;
				}
				else {
					start=value.indexOf(">=",index);
					if(start!=-1 && start<end) {
						op=BundleDefinition.LTE;
						start+=2;
					}
					else {
						start=value.indexOf("=",index);
						if(start!=-1 && start<end) {
							op=BundleDefinition.EQ;
							start++;
						}
					}
				}
				
				if(op==-1 || start==-1 || end==-1 || end<start) continue;
				bd.setVersion(op,value.substring(start,end).trim());
				
			}
		}
		return rtn;
		// (&(osgi.wiring.bundle=slf4j.api)(bundle-version>=1.6.4))
		
	}
	
	
	private static Bundle _loadBundle(BundleContext context, File bundle) throws IOException, BundleException {
		return _loadBundle(context, bundle.getAbsolutePath(),new FileInputStream(bundle),true);
	}
	
	
	public static class BundleDefinition {

		public static final int LTE = 1;
		public static final int GTE = 2;
		public static final int EQ = 4;
		private final String name;
		private int op;
		private Version version;
		private Bundle bundle;

		public BundleDefinition(String name) {
			this.name=name;
		}
		public BundleDefinition(String name, String version) throws BundleException {
			this.name=name;
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			setVersion(EQ, version);
		}
		
		public BundleDefinition(String name, Version version) {
			this.name=name;
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			
			this.version=version;
			this.op=EQ;
		}

		public BundleDefinition(Bundle bundle) {
			this.name=bundle.getSymbolicName();
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			
			this.version=bundle.getVersion();
			this.op=EQ;
			this.bundle=bundle;
		}

		public String getName() {
			return name;
		}

		/**
		 * only return a bundle if already loaded, does not load the bundle
		 * @return
		 */
		public Bundle getLoadedBundle() {
			return bundle;
		}
		
		/**
		 * get Bundle, also load if necessary from local or remote
		 * @return
		 * @throws BundleException
		 */
		public Bundle getBundle(Config config) throws BundleException {
			if(bundle==null) {
				config = ThreadLocalPageContext.getConfig(config);
				bundle=OSGiUtil.loadBundle(name, version, config==null?null:config.getIdentification(), false);
			}
			return bundle;
		}
		

		public BundleFile getBundleFile() throws BundleException {
			Config config = ThreadLocalPageContext.getConfig();
			return OSGiUtil.getBundleFile(name, version, config==null?null:config.getIdentification(),true);
			
		}

		public int getOp() {
			return op;
		}

		public Version getVersion() {
			return version;
		}

		public String getVersionAsString() {
			return version==null?null:version.toString();
		}

		public void setVersion(int op, String version) throws BundleException {
			this.op=op;
			this.version=OSGiUtil.toVersion(version);
		}
		
		@Override
		public String toString() {
			return "name:"+name+";version:"+version+";op:"+getOpAsString()+";";
		}
		
		@Override
		public boolean equals(Object obj){
			if(this==obj) return true;
			if(!(obj instanceof BundleDefinition)) return false;
			
			return toString().equals(obj.toString());
			
		}
		

		public String getOpAsString() {
			switch(op){
			case EQ:return "EQ";
			case LTE:return "LTE";
			case GTE:return "GTE";
			}
			return null;
		}

	}
	
	

	private static void log(int level, String msg) {
		Config config = ThreadLocalPageContext.getConfig();
		Log log = config!=null?config.getLog("application"):null;
		if(log!=null) log.log(level, "OSGi", msg);
	}
	private static void log(Throwable t) {
		Config config = ThreadLocalPageContext.getConfig();
		Log log = config!=null?config.getLog("application"):null;
		if(log!=null) log.log(Log.LEVEL_ERROR, "OSGi", t);
	}



	public static String toState(int state, String defaultValue) {
		switch(state){
		case Bundle.ACTIVE: return "active";
		case Bundle.INSTALLED: return "installed";
		case Bundle.UNINSTALLED: return "uninstalled";
		case Bundle.RESOLVED: return "resolved";
		case Bundle.STARTING: return "starting";
		case Bundle.STOPPING: return "stopping";
		}
		return defaultValue;
		
	}


	/**
	 * value can be a String (for a single entry) or a List<String> for multiple entries
	 * @param b
	 * @return
	 */
	public static Map<String,Object> getHeaders(Bundle b) {
		Dictionary<String, String> headers = b.getHeaders();
		Enumeration<String> keys = headers.keys();
		Enumeration<String> values = headers.elements();
		
		
		String key,value;
		Object existing;
		List<String> list;
		Map<String, Object> _headers=new HashMap<String, Object>();
		while(keys.hasMoreElements()){
			key=keys.nextElement();
			value=StringUtil.unwrap(values.nextElement());
			existing = _headers.get(key);
			if(existing!=null) {
				if(existing instanceof String) {
					list=new ArrayList<>();
					list.add((String)existing);
					_headers.put(key, list);
				}
				else
					list=(List<String>) existing;
				list.add(value);
			}
			else _headers.put(key, value);
		}
		
		
		
		return _headers;
	}



	public static String[] getBootdelegation() {
		if(bootDelegation==null) {
			InputStream is = null;
			List<String> list=new ArrayList<>();
	    	try {
	    		Properties prop = new Properties();
	    		is = OSGiUtil.class.getClassLoader().getResourceAsStream("default.properties");
	        	prop.load(is);
	        	String bd = prop.getProperty("org.osgi.framework.bootdelegation");
	        	if(!StringUtil.isEmpty(bd)) {
	        		bootDelegation=ListUtil.trimItems(ListUtil.listToStringArray(StringUtil.unwrap(bd),','));
	        	}
			}
	    	catch(IOException ioe){
	    	}
	    	finally {
				IOUtil.closeEL(is);
			}
		}
		if(bootDelegation==null) return new String[0];
		return bootDelegation;
	}



	public static BundleDefinition[] toBundleDefinitions( BundleFile[] bundlesFiles) {
		if(bundlesFiles==null) return new BundleDefinition[0];
		
		BundleDefinition[] rtn=new BundleDefinition[bundlesFiles.length];
		for(int i=0;i<bundlesFiles.length;i++){
			rtn[i]=bundlesFiles[i].toBundleDefinition();
		}
		return rtn;
	}
}