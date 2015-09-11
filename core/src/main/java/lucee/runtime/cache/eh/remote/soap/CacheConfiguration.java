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
/* cacheConfiguration - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
package lucee.runtime.cache.eh.remote.soap;

public class CacheConfiguration
{
    private Boolean clearOnFlush;
    private Long diskExpiryThreadIntervalSeconds;
    private Boolean diskPersistent;
    private Integer diskSpoolBufferSizeMB;
    private Boolean eternal;
    private Integer maxElementsInMemory;
    private Integer maxElementsOnDisk;
    private String name;
    private Boolean overflowToDisk;
    private Long timeToIdleSeconds;
    private Long timeToLiveSeconds;
    
    public final Boolean getClearOnFlush() {
	return clearOnFlush;
    }
    
    public final void setClearOnFlush(Boolean clearOnFlush) {
	this.clearOnFlush = clearOnFlush;
    }
    
    public final Long getDiskExpiryThreadIntervalSeconds() {
	return diskExpiryThreadIntervalSeconds;
    }
    
    public final void setDiskExpiryThreadIntervalSeconds
	(Long diskExpiryThreadIntervalSeconds) {
	this.diskExpiryThreadIntervalSeconds = diskExpiryThreadIntervalSeconds;
    }
    
    public final Boolean getDiskPersistent() {
	return diskPersistent;
    }
    
    public final void setDiskPersistent(Boolean diskPersistent) {
	this.diskPersistent = diskPersistent;
    }
    
    public final Integer getDiskSpoolBufferSizeMB() {
	return diskSpoolBufferSizeMB;
    }
    
    public final void setDiskSpoolBufferSizeMB(Integer diskSpoolBufferSizeMB) {
	this.diskSpoolBufferSizeMB = diskSpoolBufferSizeMB;
    }
    
    public final Boolean getEternal() {
	return eternal;
    }
    
    public final void setEternal(Boolean eternal) {
	this.eternal = eternal;
    }
    
    public final Integer getMaxElementsInMemory() {
	return maxElementsInMemory;
    }
    
    public final void setMaxElementsInMemory(Integer maxElementsInMemory) {
	this.maxElementsInMemory = maxElementsInMemory;
    }
    
    public final Integer getMaxElementsOnDisk() {
	return maxElementsOnDisk;
    }
    
    public final void setMaxElementsOnDisk(Integer maxElementsOnDisk) {
	this.maxElementsOnDisk = maxElementsOnDisk;
    }
    
    public final String getName() {
	return name;
    }
    
    public final void setName(String name) {
	this.name = name;
    }
    
    public final Boolean getOverflowToDisk() {
	return overflowToDisk;
    }
    
    public final void setOverflowToDisk(Boolean overflowToDisk) {
	this.overflowToDisk = overflowToDisk;
    }
    
    public final Long getTimeToIdleSeconds() {
	return timeToIdleSeconds;
    }
    
    public final void setTimeToIdleSeconds(Long timeToIdleSeconds) {
	this.timeToIdleSeconds = timeToIdleSeconds;
    }
    
    public final Long getTimeToLiveSeconds() {
	return timeToLiveSeconds;
    }
    
    public final void setTimeToLiveSeconds(Long timeToLiveSeconds) {
	this.timeToLiveSeconds = timeToLiveSeconds;
    }
}