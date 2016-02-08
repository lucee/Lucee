package lucee.runtime.osgi;

import java.io.InputStream;
import java.net.URL;

import lucee.commons.io.log.Log;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebUtil;

import org.osgi.framework.Bundle;

public class EnvClassLoader extends ClassLoader {

	private ConfigImpl config;
	private ClassLoader[] parents;
	//private ClassLoader loaderCL;

	private static final short CLASS=1;
	private static final short URL=2;
	private static final short STREAM=3;


	
	public EnvClassLoader(ConfigImpl config) {
		super(config.getClassLoaderCore());
		this.config=config;
		
		ClassLoader coreCL = getParent();
		//loaderCL = TP.class.getClassLoader(); //this gives a wrong result because bootdelegetation should handle this!
		
		parents=new ClassLoader[]{coreCL};
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException   {
		return loadClass(name, false);
	}
	
	@Override
	public URL getResource(String name) {
		return (java.net.URL) load(name, URL);
	}

	@Override
	public InputStream getResourceAsStream(String name) {	
		InputStream is = (InputStream) load(name, STREAM);
		if(is!=null) return is;
		
		// PATCH 
		//if(name.equalsIgnoreCase("META-INF/services/org.apache.xerces.xni.parser.XMLParserConfiguration"))
		//	return (InputStream) load("org/apache/xerces/parsers/org.apache.xerces.xni.parser.XMLParserConfiguration", STREAM);
		
		return null;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if(c==null)c = (Class<?>) load(name, CLASS);
		if(c==null)c = findClass(name);
		if (resolve)resolveClass(c);
		return c;
	}
	
	private synchronized Object load(String name, short type) {
		//print.e("looking for("+toType(type)+"):"+name);
		
		
		Object obj=null;
		// now we check in the core and loader for the class (this includes all jars loaded by the core)
		for(ClassLoader p:parents) {
			try {
				if(type==CLASS)obj = p.loadClass(name);
				else if(type==URL)obj = p.getResource(name);
				else obj = p.getResourceAsStream(name);
				if(obj!=null)break;
			} 
			catch (Throwable t) {}
		}
		if(obj!=null) return obj;
		
		
		// now we check extension bundles
		Bundle[] bundles = ConfigWebUtil.getEngine(config).getBundleContext().getBundles();
		Bundle b=null;
		for(int i=0;i<bundles.length;i++) {
			b=bundles[i];
			if(b!=null && !isFrameworkBundle(b)) {
				try {
					if(type==CLASS)obj = b.loadClass(name);
					else if(type==URL)obj = b.getResource(name);
					else obj = ((ClassLoader)b).getResourceAsStream(name);
					if(obj!=null)break;
				} 
				catch (Throwable t) {
					b=null;
				}
			}
			else b=null;
		}
		
		Log log = config.getLog("application", true);
		// not found
		if(obj==null) {
			log.error(EnvClassLoader.class.getName(), "not able to find	 "+toType(type)+" "+name);
		}
		// found
		else  {// should always be the case!
			ClassLoader cl = obj.getClass().getClassLoader();
			log.error(EnvClassLoader.class.getName(), "found "+toType(type)+" "+name+" in CL "+cl);
		}
		
		
		/*Bundle b;
		while(it.hasNext()) {
			b = it.next().getLoadedBundle();
			if(b!=null){
				print.e("checking:"+b);
				try {
					if(type==CLASS)obj = b.loadClass(name);
					else if(type==URL)obj = b.getResource(name);
					else obj = ((ClassLoader)b).getResourceAsStream(name);
					print.e("sucess");
					if(obj!=null)break;
				} 
				catch (Throwable t) {print.e("failed");
				}
			}
		}*/
		
		return obj;
   }


	/*private void test(Bundle[] bundles, short type, String name) {
		Bundle b=null;
		List<Bundle> list=new ArrayList<Bundle>();
		Object obj;
		for(int i=0;i<bundles.length;i++) {
			b=bundles[i];
			if(b.getSymbolicName().equalsIgnoreCase("hibernate.extension") && "org.lucee.extension.orm.hibernate.jdbc.ConnectionProviderImpl".equals(name)) {
				print.e("=>"+b+":"+b.hashCode());
				try {
					Class<?> clazz = b.loadClass(name);
					ClassLoader cl = clazz.getClassLoader();
					print.e("-=>"+cl+":"+cl.hashCode());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
				
				try {
					if(type==CLASS)obj = b.loadClass(name);
					else if(type==URL)obj = b.getResource(name);
					else obj = ((ClassLoader)b).getResourceAsStream(name);
					if(obj!=null) {
						list.add(b);
					}
				} 
				catch (Throwable t) {
					b=null;
				}
			
		}
		if(list.size()>1) {
			print.e("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
			print.e("found in more than one bundle!");
			Iterator<Bundle> it = list.iterator();
			while(it.hasNext()){
				b=it.next();
				print.e("- "+b);
			}
		}
		if(list.size()==0) {
			print.e("000000000000000000000000000000000000000000000000000000");
			print.e("not found:"+name);
			
		}
	}*/

	private boolean isFrameworkBundle(Bundle b) {
		return "org.apache.felix.framework".equalsIgnoreCase(b.getSymbolicName()); // TODO move to cire util class tha does not exist yet
	}

	private String toType(short type) {
		if(CLASS==type) return "class";
		if(STREAM==type) return "stream";
		return "url";
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {//if(name.indexOf("sub")!=-1)print.ds(name);	
		throw new ClassNotFoundException("class "+name+" not found in the core, the loader and all the extension bundles");
	}
}