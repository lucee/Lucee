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
package lucee.runtime.cache.eh.remote.soap;

public class Element
{
    private Boolean eternal;
    private Long expirationDate;
    private Object key;
    private String mimeType;
    private String resourceUri;
    private Integer timeToIdleSeconds;
    private Integer timeToLiveSeconds;
    private byte[] value;
    
    public final Boolean getEternal() {
	return eternal;
    }
    
    public final void setEternal(Boolean eternal) {
	this.eternal = eternal;
    }
    
    public final Long getExpirationDate() {
	return expirationDate;
    }
    
    public final void setExpirationDate(Long expirationDate) {
	this.expirationDate = expirationDate;
    }
    
    public final Object getKey() {
	return key;
    }
    
    public final void setKey(Object key) {
	this.key = key;
    }
    
    public final String getMimeType() {
	return mimeType;
    }
    
    public final void setMimeType(String mimeType) {
	this.mimeType = mimeType;
    }
    
    public final String getResourceUri() {
	return resourceUri;
    }
    
    public final void setResourceUri(String resourceUri) {
	this.resourceUri = resourceUri;
    }
    
    public final Integer getTimeToIdleSeconds() {
	return timeToIdleSeconds;
    }
    
    public final void setTimeToIdleSeconds(Integer timeToIdleSeconds) {
	this.timeToIdleSeconds = timeToIdleSeconds;
    }
    
    public final Integer getTimeToLiveSeconds() {
	return timeToLiveSeconds;
    }
    
    public final void setTimeToLiveSeconds(Integer timeToLiveSeconds) {
	this.timeToLiveSeconds = timeToLiveSeconds;
    }
    
    public final byte[] getValue() {
	return value;
    }
    
    public final void setValue(byte[] value) {
	this.value = value;
    }
}