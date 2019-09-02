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
package lucee.commons.io.res;

public interface Resources {

	/**
	 * adds a default factory, this factory is used, when shemecan't be mapped to another factory
	 * 
	 * @param provider resource provider to register
	 */
	public void registerDefaultResourceProvider(ResourceProvider provider);

	/**
	 * adds an additional resource to System
	 * 
	 * @param provider resource provider to register
	 */
	public void registerResourceProvider(ResourceProvider provider);

	/**
	 * returns a resource that matching the given path
	 * 
	 * @param path path to resource
	 * @return matching resource
	 */
	public Resource getResource(String path);

	/**
	 * @return the defaultResource
	 */
	public ResourceProvider getDefaultResourceProvider();

	public ResourceProvider[] getResourceProviders();

	public ResourceLock createResourceLock(long timeout, boolean caseSensitive);

	public void reset();
}