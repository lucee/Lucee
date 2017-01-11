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
import java.io.Serializable;
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
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringList;
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
import lucee.runtime.type.Array;
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
			
			Bundle existing = loadBundleFromLocal(context,bf.getSymbolicName(),bf.getVersion(),false,null);
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
			// we make this very simply so an old loader that is calling this still works
			return context.installBundle(path, is);
		}
		finally {
			// we make this very simply so an old loader that is calling this still works
			if(closeStream && is!=null){
				try {
					is.close();
				}
				catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
			}
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
    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);} // class is not visible to the Lucee core
		
		// now we check all started bundled (not only bundles used by core)
		Bundle[] bundles = bc.getBundleContext().getBundles();
    	for(Bundle b:bundles){
    		if(b==bc.core) continue;
			try {
				return b.loadClass(className);
			} catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);} // class is not visible to that bundle
    	}
    	
    	// now we check lucee loader (SystemClassLoader?)
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	try {
    		//print.e("loader:");
    		return factory.getClass().getClassLoader().loadClass(className);
		}
		catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
    	
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
	    		catch(Throwable t2){ExceptionUtil.rethrowIfNecessary(t2);}
	    	}
		}
		catch(Throwable t1){ExceptionUtil.rethrowIfNecessary(t1);}
    	
		return defaultValue;
	}
		
	public static Bundle loadBundle(BundleFile bf, Bundle defaultValue) {
    	if(!bf.isBundle()) return defaultValue;
    	
		try {
			return loadBundle(bf);
		} catch (Exception e) {
			return defaultValue;
		}
    }
	
	public static Bundle loadBundle(BundleFile bf) throws IOException, BundleException {
    	
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	
    	// check in loaded bundles
    	BundleContext bc = engine.getBundleContext();
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		if(bf.getSymbolicName().equals(b.getSymbolicName())) {
    			if(b.getVersion().equals(bf.getVersion())) return b;
    		}
    	}
    	return _loadBundle(bc, bf.getFile());
    }
	
	public static Bundle loadBundleByPackage(String packageName, List<VersionDefinition> versionDefinitions, 
			Set<Bundle> loadedBundles, boolean startIfNecessary, Set<Bundle> parents) throws BundleException, IOException {
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();
    	
    	// if part of bootdelegation we ignore
    	if(OSGiUtil.isPackageInBootelegation(packageName)) {
    		return null;
    	}
    	
    	// is it in jar directory but not loaded
    	File dir=factory.getBundleDirectory();
		File[] children = dir.listFiles(JAR_EXT_FILTER);
		List<PackageDefinition> pds;
    	for(File child:children) {
    		BundleFile bf=new BundleFile(child);
			if(bf.isBundle()) {
				pds=toPackageDefinitions(bf.getExportPackage(), packageName, versionDefinitions);
				if(pds!=null && !pds.isEmpty()) {
					Bundle b=exists(loadedBundles,bf);
					if(b!=null) {
						
						if(startIfNecessary && !parents.contains(b)) _startIfNecessary(b,parents);
						return null;
					}
					b = loadBundle(bf);
					if(b!=null) {
						loadedBundles.add(b);
						if(startIfNecessary &&  !parents.contains(b))_startIfNecessary(b,parents);
						return b;
					}
				}
			}
    	}
    	return null;
	}

	private static Bundle exists(Set<Bundle> loadedBundles, BundleFile bf) {
		if(loadedBundles!=null) {
			Bundle b;
			Iterator<Bundle> it = loadedBundles.iterator();
			while(it.hasNext()) {
				b=it.next();
				if(b.getSymbolicName().equals(bf.getSymbolicName()) && b.getVersion().equals(bf.getVersion())) return b;
			}
		}
		return null;
	}

	private static Bundle exists(Set<Bundle> loadedBundles, BundleDefinition bd) {
		if(loadedBundles!=null) {
			Bundle b;
			Iterator<Bundle> it = loadedBundles.iterator();
			while(it.hasNext()) {
				b=it.next();
				if(b.getSymbolicName().equals(bd.getName()) && b.getVersion().equals(bd.getVersion())) return b;
			}
		}
		return null;
	}

	
	public static Bundle loadBundle(String name, Version version,Identification id, boolean startIfNecessary) throws BundleException {
		return _loadBundle(name, version, id, startIfNecessary,null);
	}
	public static Bundle _loadBundle(String name, Version version,Identification id, boolean startIfNecessary, Set<Bundle> parents) throws BundleException {
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
    				if(startIfNecessary)_startIfNecessary(b,parents);
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
	    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		}
    	
    	String localDir="";
    	try {
			localDir = " ("+factory.getBundleDirectory()+")";
		} catch (IOException e) {}
    	String upLoc="";
    	try {
    		upLoc = " ("+factory.getUpdateLocation()+")";
		} catch (IOException e) {}
    	
    	if(versionsFound.length()>0)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] is not available in version ["+version+"] locally"+localDir+" or from the update provider"+upLoc+", the following versions are available locally ["+versionsFound+"].");
    	if(version!=null)
    		throw new BundleException("The OSGi Bundle with name ["+name+"] in version ["+version+"] is not available locally"+localDir+" or from the update provider"+upLoc+".");
    	throw new BundleException("The OSGi Bundle with name ["+name+"] is not available locally"+localDir+" or from the update provider"+upLoc+".");
    }
	

	private static List<PackageDefinition> toPackageDefinitions(String str, String filterPackageName, List<VersionDefinition> versionDefinitions) {
		if(StringUtil.isEmpty(str)) return null;
		StringTokenizer st=new StringTokenizer(str, ",");
		List<PackageDefinition> list=new ArrayList<PackageDefinition>();
		PackageDefinition pd;
		while(st.hasMoreTokens()) {
			pd=toPackageDefinition(st.nextToken().trim(),filterPackageName,versionDefinitions);
			if(pd!=null) list.add(pd);
		}
		return list;
	}

	private static PackageDefinition toPackageDefinition(String str, String filterPackageName, List<VersionDefinition> versionDefinitions) {
		// first part is the package
		StringList list = ListUtil.toList(str, ';');
		PackageDefinition pd=null;
		String token;
		Version v;
		while(list.hasNext()) {
			token=list.next().trim();
			if(pd==null) {
				if(!token.equals(filterPackageName)) return null;
				pd=new PackageDefinition(token);
			}
			// only intressted in version
			else {
				StringList entry = ListUtil.toList(token, '=');
				if(entry.size()==2 && entry.next().trim().equalsIgnoreCase("version")) {
					String version=StringUtil.unwrap(entry.next().trim());
					if(!version.equals("0.0.0")) {
						v = OSGiUtil.toVersion(version,null);
						if(v!=null) {
							if(versionDefinitions!=null) {
								Iterator<VersionDefinition> it = versionDefinitions.iterator();
								while(it.hasNext()) {
									if(!it.next().matches(v)) {
										return null;
									}
								}
							}
							pd.setVersion(v);
						}
					}
				}
				
			}
		}
		return pd;
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
	    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
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
	    	catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
		}
    	
    	return defaultValue;
    }

	private static BundleFile _getBundleFile(CFMLEngineFactory factory, String name, Version version, StringBuilder versionsFound) {
		try{
    		File dir=factory.getBundleDirectory();
			
    		// first we check if there is a file match (fastest solution)
			if(version!=null){
				File[] jars = new File[]{ 
						new File(dir, name + "-"+ version.toString() + (".jar")),
						new File(dir, name + "-"+ version.toString().replace('.', '-') + (".jar"))
				};
				for(int i=0;i<jars.length;i++) {
					File jar=jars[i];
					if(jar.exists()) {
						BundleFile bf=new BundleFile(jar);
						if(bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
							if(version.equals(bf.getVersion())) {
								return bf;
			    			}
						}
					}
				}
			}
			
			File[] children = dir.listFiles(JAR_EXT_FILTER);
	    	
			// now we make a closer filename test
			String curr;
			if(version!=null) {
				File match=null;
		    	String v=version.toString();
				for(int i=0;i<children.length;i++){
		    		curr=children[i].getName();
		    		if(
			    			curr.equalsIgnoreCase(name+"-"+v.replace('-', '.')) ||
			    			curr.equalsIgnoreCase(name.replace('.', '-')+"-"+v) ||
			    			curr.equalsIgnoreCase(name.replace('.', '-')+"-"+v.replace('.', '-')) ||
			    			curr.equalsIgnoreCase(name.replace('.', '-')+"-"+v.replace('-', '.')) ||
			    			curr.equalsIgnoreCase(name.replace('-', '.')+"-"+v) ||
			    			curr.equalsIgnoreCase(name.replace('-', '.')+"-"+v.replace('.', '-')) ||
			    			curr.equalsIgnoreCase(name.replace('-', '.')+"-"+v.replace('-', '.'))
		    		) {
		    			match=children[i];
		    			break;
		    		}
		    	}
		    	if(match!=null) {
		    		BundleFile bf=new BundleFile(match);
					if(bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
						if(version.equals(bf.getVersion())) {
							return bf;
		    			}
					}
		    	}
			}
			else {
				List<BundleFile> matches=new ArrayList<BundleFile>();
				BundleFile bf;
		    	for(int i=0;i<children.length;i++){
		    		curr=children[i].getName();
		    		if(
			    			curr.startsWith(name+"-") ||
			    			curr.startsWith(name.replace('-', '.')+"-") ||
			    			curr.startsWith(name.replace('.', '-')+"-")
		    		) {
		    			bf=new BundleFile(children[i]);
		    			if(bf.isBundle() && name.equalsIgnoreCase(bf.getSymbolicName())) {
							matches.add(bf);
						}
		    		}
		    	}
		    	if(!matches.isEmpty()) {
		    		bf=null; BundleFile _bf;
					Iterator<BundleFile> it = matches.iterator();
		    		while(it.hasNext()) {
		    			_bf=it.next();
		    			if(bf==null || Util.isNewerThan(_bf.getVersion(),bf.getVersion()))
		    				bf=_bf;
		    		}
		    		if(bf!=null) {
						return bf;
	    			}
		    	}
			}
	    	
			// now we check by Manifest comparsion
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
		Set<String> set=new HashSet<>();
		List<BundleDefinition> list=new ArrayList<>();
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		list.add(new BundleDefinition(b));
    		set.add(b.getSymbolicName()+":"+b.getVersion());
    	}
    	// is it in jar directory but not loaded
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
	    		catch(Throwable t){ExceptionUtil.rethrowIfNecessary(t);}
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
	
	public static Bundle loadBundleFromLocal(String name, Version version, boolean loadIfNecessary, Bundle defaultValue) {
		CFMLEngine engine = ConfigWebUtil.getEngine(ThreadLocalPageContext.getConfig());
		return loadBundleFromLocal(engine.getBundleContext(), name, version,loadIfNecessary, defaultValue);
	}
	
	
	public static Bundle loadBundleFromLocal(BundleContext bc,String name, Version version, boolean loadIfNecessary, Bundle defaultValue) {
		name=name.trim();
    	Bundle[] bundles = bc.getBundles();
    	for(Bundle b:bundles){
    		if(name.equalsIgnoreCase(b.getSymbolicName())) {
    			if(version==null || version.equals(b.getVersion())) {
    				return b;
    			}
    		}
    	}
    	if(!loadIfNecessary) return defaultValue;
    	
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
	public static void removeLocalBundle(String name, Version version, boolean removePhysical, boolean doubleTap) throws BundleException {
		name=name.trim();
		CFMLEngine engine = CFMLEngineFactory.getInstance();
    	CFMLEngineFactory factory = engine.getCFMLEngineFactory();

    	BundleFile bf = _getBundleFile(factory, name, version, null);
    	if(bf!=null) {
    		BundleDefinition bd = bf.toBundleDefinition();
    		if(bd!=null) {
	    		Bundle b = bd.getLocalBundle();
	    		if(b!=null) {
	    			stopIfNecessary(b);
					b.uninstall();
	    		}
        	}
    	}
    	
    	if(!removePhysical) return;
    	
    	// remove file
    	if(bf!=null) {
    		if(!bf.getFile().delete() && doubleTap) bf.getFile().deleteOnExit();
    	}
    }
	
	public static void removeLocalBundleSilently(String name, Version version, boolean removePhysical) {
		try {
			removeLocalBundle(name, version, removePhysical, true);
		}
		catch(Throwable t) {ExceptionUtil.rethrowIfNecessary(t);}
	}

	// bundle stuff
	public static void startIfNecessary(Bundle[] bundles) throws BundleException {
		for(Bundle b:bundles){
			startIfNecessary(b);
		}
	}
	public static Bundle startIfNecessary(Bundle bundle) throws BundleException {
		return _startIfNecessary(bundle, null);
	}
	
	private static Bundle _startIfNecessary(Bundle bundle, Set<Bundle> parents) throws BundleException {
		if(bundle.getState()==Bundle.ACTIVE) return bundle;
		return _start(bundle,parents);
	}

	public static Bundle start(Bundle bundle) throws BundleException {
		return _start(bundle, null);
	}
	
	public static Bundle _start(Bundle bundle, Set<Bundle> parents) throws BundleException {
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
			List<BundleDefinition> listBundles = getRequiredBundles(bundle);
			List<PackageQuery> listPackages = getRequiredPackages(bundle);
			List<BundleDefinition> failedBD = null;
			List<PackageQuery> failedPD = null;
			Set<Bundle> loadedBundles = new HashSet<Bundle>();
			loadedBundles.add(bundle);
			
			// load bundles
			{
				Bundle b;
				BundleDefinition bd;
				Iterator<BundleDefinition> it = listBundles.iterator();
				while(it.hasNext()){
					bd=it.next();
					b=exists(loadedBundles, bd);
					if(b!=null) {
						startIfNecessary(b);
						continue;
					}
					try{
						if(parents==null) parents=new HashSet<Bundle>();
						parents.add(bundle);
						
						b=_loadBundle(
								bd.name, 
								bd.getVersion(), 
								ThreadLocalPageContext
								.getConfig()
								.getIdentification(), true,parents);
						loadedBundles.add(b);
					}
					catch(BundleException _be){
						if(failedBD==null) failedBD=new ArrayList<OSGiUtil.BundleDefinition>();
						failedBD.add(bd);
						log(_be);
					}
				}
			}
			
			// load packages
			{
				PackageQuery pq;
				Iterator<PackageQuery> it = listPackages.iterator();
				while(it.hasNext()){
					pq=it.next();
					try{
						if(parents==null) parents=new HashSet<Bundle>();
						parents.add(bundle);
						loadBundleByPackage(pq.getName(),pq.getVersionDefinitons(),loadedBundles,true,parents);
					}
					catch(Exception _be){
						if(failedPD==null) failedPD=new ArrayList<OSGiUtil.PackageQuery>();
						failedPD.add(pq);
						log(_be);
					}
				}
			}
			
			try {
				//startIfNecessary(loadedBundles.toArray(new Bundle[loadedBundles.size()]));
				BundleUtil.start(bundle);
			}
			catch(BundleException be2) {
				if(failedBD!=null) {
					Iterator<BundleDefinition> itt = failedBD.iterator();
					BundleDefinition _bd;
					StringBuilder sb=new StringBuilder(" Lucee was not able to download the following bundles [");
					while(itt.hasNext()){
						_bd=itt.next();
						sb.append(_bd.name+":"+_bd.getVersionAsString()).append(';');
					}
					sb.append("]");
					throw new BundleException(be2.getMessage()+sb,be2.getCause());
				} 
				throw be2;
			}
			
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
	
	public static List<BundleDefinition> getRequiredBundles(Bundle bundle) throws BundleException {
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
					op=VersionDefinition.LTE;
					start+=2;
				}
				else {
					start=value.indexOf(">=",index);
					if(start!=-1 && start<end) {
						op=VersionDefinition.GTE;
						start+=2;
					}
					else {
						start=value.indexOf("=",index);
						if(start!=-1 && start<end) {
							op=VersionDefinition.EQ;
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
	

	public static List<PackageQuery> getRequiredPackages(Bundle bundle) throws BundleException {
		List<PackageQuery> rtn=new ArrayList<PackageQuery>();
		BundleRevision br = bundle.adapt(BundleRevision.class);
		List<Requirement> requirements = br.getRequirements(null);
		Iterator<Requirement> it = requirements.iterator();
		Requirement r;
		Entry<String, String> e;
		String value;
		PackageQuery pd;
		while(it.hasNext()){
			r = it.next();
			Iterator<Entry<String, String>> iit = r.getDirectives().entrySet().iterator();
			inner:while(iit.hasNext()){
				e = iit.next();
				if(!"filter".equals(e.getKey())) continue;
				value=e.getValue();
				pd=toPackageQuery(value);
				if(pd!=null)rtn.add(pd);
			}
		}
		return rtn;
	}

	private static PackageQuery toPackageQuery(String value) throws BundleException {

		// name(&(osgi.wiring.package=org.jboss.logging)(version>=3.3.0)(!(version>=4.0.0)))
		int index=value.indexOf("(osgi.wiring.package");
		if(index==-1) {
			return null;
		}
		int start=value.indexOf('=',index);
		int end=value.indexOf(')',index);
		if(start==-1 || end==-1 || end<start) {
			return null;
		}
		String name=value.substring(start+1,end).trim();
		PackageQuery pd = new PackageQuery(name);
		int last=end,op;
		boolean not;
		// version
		while((index=value.indexOf("(version",last))!=-1) {
			op=-1;
			
			end=value.indexOf(')',index);
			
			start=value.indexOf("<=",index);
			if(start!=-1 && start<end) {
				op=VersionDefinition.LTE;
				start+=2;
			}
			else {
				start=value.indexOf(">=",index);
				if(start!=-1 && start<end) {
					op=VersionDefinition.GTE;
					start+=2;
				}
				else {
					start=value.indexOf("==",index);
					if(start!=-1 && start<end) {
						op=VersionDefinition.EQ;
						start+=2;
					}
					else {
						start=value.indexOf("!=",index);
						if(start!=-1 && start<end) {
							op=VersionDefinition.NEQ;
							start+=2;
						}
						else {
							start=value.indexOf("=",index);
							if(start!=-1 && start<end) {
								op=VersionDefinition.EQ;
								start+=1;
							}
							else {
								start=value.indexOf("<",index);
								if(start!=-1 && start<end) {
									op=VersionDefinition.LT;
									start+=1;
								}
								else {
									start=value.indexOf(">",index);
									if(start!=-1 && start<end) {
										op=VersionDefinition.GT;
										start+=1;
									}
								}
							}
						}
					}
				}
			}
			not=value.charAt(index-1)=='!';
			last=end;
			if(op==-1 || start==-1 || end==-1 || end<start) continue;
			pd.addVersion(op,value.substring(start,end).trim(),not);
		}
		
		return pd;
	}



	private static Bundle _loadBundle(BundleContext context, File bundle) throws IOException, BundleException {
		return _loadBundle(context, bundle.getAbsolutePath(),new FileInputStream(bundle),true);
	}
	
	


	public static class VersionDefinition implements Serializable {
		
		private static final long serialVersionUID = 4915024473510761950L;
		
		public static final int LTE = 1;
		public static final int GTE = 2;
		public static final int EQ = 4;
		public static final int LT = 8;
		public static final int GT = 16;
		public static final int NEQ = 32;


		private Version version;
		private int op;

		public VersionDefinition(Version version, int op, boolean not) {
			this.version=version;
			
			if(not) {
				if(op==LTE) {op=GT;not=false;}
				else if(op==LT) {op=GTE;not=false;}
				else if(op==GTE) {op=LT;not=false;}
				else if(op==GT) {op=LTE;not=false;}
				else if(op==EQ) {op=NEQ;not=false;}
				else if(op==NEQ) {op=EQ;not=false;}
			}
			this.op=op;
			
		}
		
		public boolean matches(Version v) {
			if(EQ==op) return v.compareTo(version)==0;
			if(LTE==op) return v.compareTo(version)<=0;
			if(LT==op) return v.compareTo(version)<0;
			if(GTE==op) return v.compareTo(version)>=0;
			if(GT==op) return v.compareTo(version)>0;
			if(NEQ==op) return v.compareTo(version)!=0;
			return false;
		}
		
		public Version getVersion() {
			return version;
		}

		public int getOp() {
			return op;
		}
		
		public String getVersionAsString() {
			return version==null?null:version.toString();
		}
		
		public String toString() {
			StringBuilder sb=new StringBuilder("version ");
			sb.append(getOpAsString()).append(' ')
			.append(version);
			
			return sb.toString();
		}
		
		public String getOpAsString() {
			switch(getOp()){
			case EQ:return "EQ";
			case LTE:return "LTE";
			case GTE:return "GTE";
			case NEQ:return "NEQ";
			case LT:return "LT";
			case GT:return "GT";
			}
			return null;
		}
		
	}
	public static class PackageQuery {
		private final String name; 
		private List<VersionDefinition> versions=new ArrayList<OSGiUtil.VersionDefinition>();

		public PackageQuery(String name) {
			this.name=name;
		}

		public void addVersion(int op, String version, boolean not) throws BundleException {
			versions.add(new VersionDefinition(OSGiUtil.toVersion(version),op,not));
		}

		public String getName() {
			return name;
		}
		public List<VersionDefinition> getVersionDefinitons() {
			return versions;
		}
		public String toString() {
			StringBuilder sb=new StringBuilder();
			sb.append("name:").append(name);
			Iterator<VersionDefinition> it = versions.iterator();
			while(it.hasNext()) {
				sb.append(';').append(it.next());
			}
			
			return sb.toString();
		}
	}
	
	public static class PackageDefinition {
		private final String name; 
		private Version version;

		public PackageDefinition(String name) {
			this.name=name;
		}

		public void setVersion(String version) throws BundleException {
			this.version=OSGiUtil.toVersion(version);
		}
		public void setVersion(Version version) {
			this.version=version;
		}

		public String getName() {
			return name;
		}
		public Version getVersion() {
			return version;
		}
		public String toString() {
			StringBuilder sb=new StringBuilder();
			sb.append("name:").append(name);
			sb.append("version:").append(version);
			return sb.toString();
		}
	}
	
	public static class BundleDefinition implements Serializable {

		private final String name;
		private Bundle bundle;
		private VersionDefinition versionDef;

		public BundleDefinition(String name) {
			this.name=name;
		}
		public BundleDefinition(String name, String version) throws BundleException {
			this.name=name;
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			setVersion(VersionDefinition.EQ, version);
		}
		
		public BundleDefinition(String name, Version version) {
			this.name=name;
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			setVersion(VersionDefinition.EQ, version);
		}

		public BundleDefinition(Bundle bundle) {
			this.name=bundle.getSymbolicName();
			if(name==null) throw new IllegalArgumentException("name cannot be null");
			
			setVersion(VersionDefinition.EQ, bundle.getVersion());
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
				bundle=OSGiUtil.loadBundle(name, getVersion(), config==null?null:config.getIdentification(), false);
			}
			return bundle;
		}
		
		public Bundle getLocalBundle() {
			if(bundle==null) {
				bundle=OSGiUtil.loadBundleFromLocal(name, getVersion(),true, null);
			}
			return bundle;
		}

		public BundleFile getBundleFile(boolean downloadIfNecessary) throws BundleException {
			Config config = ThreadLocalPageContext.getConfig();
			return OSGiUtil.getBundleFile(name, getVersion(), config==null?null:config.getIdentification(),downloadIfNecessary);
			
		}

		public int getOp() {
			return versionDef==null?VersionDefinition.EQ:versionDef.getOp();
		}
		
		public Version getVersion() {
			return versionDef==null?null:versionDef.getVersion();
		}

		public VersionDefinition getVersionDefiniton() {
			return versionDef;
		}

		public String getVersionAsString() {
			return versionDef==null?null:versionDef.getVersionAsString();
		}

		public void setVersion(int op, String version) throws BundleException {
			setVersion(op,OSGiUtil.toVersion(version));
		}
		public void setVersion(int op, Version version) {
			this.versionDef=new VersionDefinition(version, op, false);
		}
		
		@Override
		public String toString() {
			return "name:"+name+";version:"+versionDef+";";
		}
		
		@Override
		public boolean equals(Object obj){
			if(this==obj) return true;
			if(!(obj instanceof BundleDefinition)) return false;
			
			return toString().equals(obj.toString());
			
		}
		

	}
	
	

	private static void log(int level, String msg) {
		try {
			Config config = ThreadLocalPageContext.getConfig();
			Log log = config!=null?config.getLog("application"):null;
			if(log!=null) log.log(level, "OSGi", msg);
		}
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			/* this can fail when called from an old loader */
			System.out.println(msg);
		}
	}
	private static void log(Throwable t) {
		try {
			Config config = ThreadLocalPageContext.getConfig();
			Log log = config!=null?config.getLog("application"):null;
			if(log!=null) log.log(Log.LEVEL_ERROR, "OSGi", t);
		}
		catch(Throwable _t) {
			ExceptionUtil.rethrowIfNecessary(_t);
			/* this can fail when called from an old loader */
			System.out.println(t.getMessage());
		}
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
			try {
	    		Properties prop = new Properties();
	    		is = OSGiUtil.class.getClassLoader().getResourceAsStream("default.properties");
	        	prop.load(is);
	        	String bd = prop.getProperty("org.osgi.framework.bootdelegation");
	        	if(!StringUtil.isEmpty(bd)) {
	        		bd+=",java.lang,java.lang.*";
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

	public static boolean isClassInBootelegation(String className) {
		return isInBootelegation(className, false);
	}
	public static boolean isPackageInBootelegation(String className) {
		return isInBootelegation(className, true);
	}
	
	private static boolean isInBootelegation(String name, boolean isPackage) {
		// extract package
		String pack;
		if(isPackage) pack=name;
		else {
			int index=name.lastIndexOf('.');
			if(index==-1) return false;
			pack=name.substring(0,index);
		}
		
		
		
		String[] arr = OSGiUtil.getBootdelegation();
		for(String bd:arr){
			bd=bd.trim();
			// with wildcard
			if(bd.endsWith(".*")) {
				bd=bd.substring(0,bd.length()-1);
				if(pack.startsWith(bd)) return true;
			}
			// no wildcard
			else {
				if(bd.equals(pack)) return true;
			}
		}
		return false;
	}



	public static BundleDefinition[] toBundleDefinitions( BundleInfo[] bundles) {
		if(bundles==null) return new BundleDefinition[0];
		
		BundleDefinition[] rtn=new BundleDefinition[bundles.length];
		for(int i=0;i<bundles.length;i++){
			rtn[i]=bundles[i].toBundleDefinition();
		}
		return rtn;
	}

	public static Bundle getFrameworkBundle(Config config, Bundle defaultValue) {
		Bundle[] bundles = ConfigWebUtil.getEngine(config).getBundleContext().getBundles();
		Bundle b=null;
		for(int i=0;i<bundles.length;i++) {
			b=bundles[i];
			if(b!=null && isFrameworkBundle(b))  return b;
		}
		return defaultValue;
	}

	
	public static  boolean isFrameworkBundle(Bundle b) {// FELIX specific
		
		return "org.apache.felix.framework".equalsIgnoreCase(b.getSymbolicName()); // TODO move to cire util class tha does not exist yet
	}
}