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
package lucee.commons.io.res;

import java.io.IOException;

import lucee.commons.io.res.type.file.FileResourceProvider;
import lucee.commons.io.res.type.ram.RamResourceProviderOld;
import lucee.commons.io.res.util.ResourceLockImpl;


public final class ResourcesImpl implements Resources {

	private static ResourceProvider frp=new FileResourceProvider();
	private static Resources global=new ResourcesImpl();
	private ResourceProvider defaultResource=frp;
	private ResourceProvider[] resources=new ResourceProvider[0];
	
	/**
	 * adds a default factory, this factory is used, when shemecan't be mapped to a other factory
	 * @param provider
	 */
	@Override
	public void registerDefaultResourceProvider(ResourceProvider provider) {
		provider.setResources(this);
		this.defaultResource=provider;
	}

	/**
	 * adds a additional resource to System
	 * @param provider
	 */
	@Override
	public void registerResourceProvider(ResourceProvider provider) {
		
		provider.setResources(this);
		String scheme = provider.getScheme();
		if(scheme==null) return;
		
		ResourceProvider[] tmp=new ResourceProvider[resources.length+1];
		for(int i=0;i<resources.length;i++) {
			if(scheme.equalsIgnoreCase(resources[i].getScheme())) {
				resources[i]=provider;
				return;
			}
			tmp[i]=resources[i];
		}
		tmp[resources.length]=provider;
		resources=tmp;
	}
	
	/**
	 * returns a resource that matching the given path
	 * @param path
	 * @return matching resource
	 */
	@Override
	public Resource getResource(String path) {
		int index=path.indexOf("://");
		if(index!=-1) {
			String scheme=path.substring(0,index).toLowerCase().trim();
			String subPath = path.substring(index+3);
			for(int i=0;i<resources.length;i++) {
				if(scheme.equalsIgnoreCase(resources[i].getScheme()))
					return resources[i].getResource(subPath);
			}
		}
		return defaultResource.getResource(path);
		
	}

	public static Resources getGlobal() {
		return global;
	}

	public static void main(String[] args) throws IOException {
		Resources rs=ResourcesImpl.getGlobal();
		rs.registerResourceProvider(new RamResourceProviderOld());
		
		Resource changes = rs.getResource("d:/changes.txt");
		changes = rs.getResource("file://d:/changes.txt");
		System.out.println(changes.getCanonicalPath());
		
		Resource mem = rs.getResource("ram://changes.txt");
		ResourceProvider mf=mem.getResourceProvider();
		System.out.println(mem.getPath());
		System.out.println(mem);
		
		mem = mf.getResource("changes.txt");
		System.out.println(mem.getPath());
		System.out.println(mem);
		
	}

	/**
	 * @return the defaultResource
	 */
	@Override
	public ResourceProvider getDefaultResourceProvider() {
		return defaultResource;
	}

	@Override
	public ResourceProvider[] getResourceProviders() {
		ResourceProvider[] tmp = new ResourceProvider[resources.length];
		for(int i=0;i<tmp.length;i++) {
			tmp[i]=resources[i];
		}
		return tmp;
	}

	public static ResourceProvider getFileResourceProvider() {
		return frp;
	}

	@Override
	public ResourceLock createResourceLock(long timeout,boolean caseSensitive) {
		return new ResourceLockImpl(timeout,caseSensitive);
	}

	@Override
	public void reset() {
		resources=new ResourceProvider[0];
	}
}