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

import java.util.Map;

import lucee.commons.io.res.type.file.FileResourceProvider;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.transformer.library.ClassDefinitionImpl;

public final class ResourcesImpl implements Resources {

	private static ResourceProvider frp = new FileResourceProvider();
	private static Resources global = new ResourcesImpl();
	private ResourceProvider defaultResource = frp;
	private ResourceProviderFactory[] resources = new ResourceProviderFactory[0];

	/**
	 * adds a default factory, this factory is used, when sheme can't be mapped to another factory
	 * 
	 * @param provider
	 */
	@Override
	public void registerDefaultResourceProvider(ResourceProvider provider) {
		provider.setResources(this);
		this.defaultResource = provider;
	}

	/**
	 * adds an additional resource to System
	 * 
	 * @param provider
	 */
	@Override
	public void registerResourceProvider(ResourceProvider provider) {
		provider.setResources(this);
		String scheme = provider.getScheme();
		if (StringUtil.isEmpty(scheme)) return;

		ResourceProviderFactory[] tmp = new ResourceProviderFactory[resources.length + 1];
		for (int i = 0; i < resources.length; i++) {
			if (scheme.equalsIgnoreCase(resources[i].getScheme())) {
				resources[i] = new ResourceProviderFactory(this, provider);
				return;
			}
			tmp[i] = resources[i];
		}
		tmp[resources.length] = new ResourceProviderFactory(this, provider);
		resources = tmp;
	}

	public void registerResourceProvider(ResourceProviderFactory rpf) {
		rpf = rpf.duplicate(this);
		String scheme = rpf.getScheme();
		if (StringUtil.isEmpty(scheme)) return;

		ResourceProviderFactory[] tmp = new ResourceProviderFactory[resources.length + 1];
		for (int i = 0; i < resources.length; i++) {
			if (scheme.equalsIgnoreCase(resources[i].getScheme())) {
				resources[i] = rpf;
				return;
			}
			tmp[i] = resources[i];
		}
		tmp[resources.length] = rpf;
		resources = tmp;
	}

	public void registerResourceProvider(String scheme, ClassDefinition cd, Map arguments) {
		if (StringUtil.isEmpty(scheme)) return;

		ResourceProviderFactory[] tmp = new ResourceProviderFactory[resources.length + 1];
		for (int i = 0; i < resources.length; i++) {
			if (scheme.equalsIgnoreCase(resources[i].getScheme())) {
				resources[i] = new ResourceProviderFactory(this, scheme, cd, arguments);
				return;
			}
			tmp[i] = resources[i];
		}
		tmp[resources.length] = new ResourceProviderFactory(this, scheme, cd, arguments);
		resources = tmp;
	}

	public static class ResourceProviderFactory {
		private Resources reses;
		private final String scheme;
		private final ClassDefinition cd;
		private final Map arguments;
		private ResourceProvider instance;

		private ResourceProviderFactory(Resources reses, String scheme, ClassDefinition cd, Map arguments) {
			this.reses = reses;
			this.scheme = scheme;
			this.cd = cd;
			this.arguments = arguments;
		}

		public ResourceProviderFactory(Resources reses, ResourceProvider provider) {
			this.reses = reses;
			this.scheme = provider.getScheme();
			this.cd = new ClassDefinitionImpl(provider.getClass());
			this.arguments = provider.getArguments();
		}

		public ResourceProviderFactory duplicate(ResourcesImpl reses) {
			return new ResourceProviderFactory(reses, scheme, cd, arguments);
		}

		public String getScheme() {
			return scheme;
		}

		public ResourceProvider instance() {
			if (instance == null) {
				try {
					Object o = ClassUtil.loadInstance(cd.getClazz());
					if (o instanceof ResourceProvider) {
						ResourceProvider rp = (ResourceProvider) o;
						rp.init(scheme, arguments);
						rp.setResources(reses);
						instance = rp;
					}
					else throw new ClassException("object [" + Caster.toClassName(o) + "] must implement the interface " + ResourceProvider.class.getName());
				}
				catch (Exception e) {
					throw new PageRuntimeException(Caster.toPageException(e));
				}
			}
			return instance;
		}
	}

	/**
	 * returns a resource that matching the given path
	 * 
	 * @param path
	 * @return matching resource
	 */
	@Override
	public Resource getResource(String path) {
		int index = path.indexOf("://");
		if (index != -1) {
			String scheme = path.substring(0, index).toLowerCase().trim();
			String subPath = path.substring(index + 3);
			for (int i = 0; i < resources.length; i++) {
				if (scheme.equalsIgnoreCase(resources[i].getScheme())) {
					return resources[i].instance().getResource(subPath);
				}
			}
		}
		return defaultResource.getResource(path);
	}

	public String getScheme() {
		// TODO Auto-generated method stub
		return null;
	}

	public static Resources getGlobal() {
		return global;
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
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = resources[i].instance();
		}
		return tmp;
	}

	public ResourceProviderFactory[] getResourceProviderFactories() {
		ResourceProviderFactory[] tmp = new ResourceProviderFactory[resources.length];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = resources[i];
		}
		return tmp;
	}

	public static ResourceProvider getFileResourceProvider() {
		return frp;
	}

	@Override
	public ResourceLock createResourceLock(long timeout, boolean caseSensitive) {
		return new ResourceLockImpl(timeout, caseSensitive);
	}

	@Override
	public void reset() {
		resources = new ResourceProviderFactory[0];
	}
}