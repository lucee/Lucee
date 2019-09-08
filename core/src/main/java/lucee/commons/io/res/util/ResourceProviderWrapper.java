/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.commons.io.res.util;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;

public class ResourceProviderWrapper implements ResourceProviderPro {

	private ResourceProvider provider;

	public ResourceProviderWrapper(ResourceProvider provider) {
		this.provider = provider;
	}

	@Override
	public Map getArguments() {
		return provider.getArguments();
	}

	@Override
	public Resource getResource(String path) {
		return provider.getResource(path);
	}

	@Override
	public String getScheme() {
		return provider.getScheme();
	}

	@Override
	public ResourceProvider init(String scheme, Map arguments) {
		return provider.init(scheme, arguments);
	}

	@Override
	public boolean isAttributesSupported() {
		return provider.isAttributesSupported();
	}

	@Override
	public boolean isCaseSensitive() {
		return provider.isCaseSensitive();
	}

	@Override
	public boolean isModeSupported() {
		return provider.isModeSupported();
	}

	@Override
	public void lock(Resource res) throws IOException {
		provider.lock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		provider.read(res);
	}

	@Override
	public void setResources(Resources resources) {
		provider.setResources(resources);
	}

	@Override
	public void unlock(Resource res) {
		provider.unlock(res);
	}

	@Override
	public char getSeparator() {
		return ResourceUtil.getSeparator(provider);
	}

}