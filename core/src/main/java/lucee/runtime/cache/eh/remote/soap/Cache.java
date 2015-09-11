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
/* cache - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package lucee.runtime.cache.eh.remote.soap;

public class Cache
{
    private CacheConfiguration cacheConfiguration;
    private String description;
    private String name;
    private Object statistics;
    private String uri;
    
    public final CacheConfiguration getCacheConfiguration() {
	return cacheConfiguration;
    }
    
    public final void setCacheConfiguration(CacheConfiguration cacheConfiguration) {
	this.cacheConfiguration = cacheConfiguration;
    }
    
    public final String getDescription() {
	return description;
    }
    
    public final void setDescription(String description) {
	this.description = description;
    }
    
    public final String getName() {
	return name;
    }
    
    public final void setName(String name) {
	this.name = name;
    }
    
    public final Object getStatistics() {
	return statistics;
    }
    
    public final void setStatistics(Object statistics) {
	this.statistics = statistics;
    }
    
    public final String getUri() {
	return uri;
    }
    
    public final void setUri(String uri) {
	this.uri = uri;
    }
}