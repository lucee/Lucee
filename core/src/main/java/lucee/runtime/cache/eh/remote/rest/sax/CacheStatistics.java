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
package lucee.runtime.cache.eh.remote.rest.sax;

public class CacheStatistics {


	private double averageGetTime;
	private int cacheHits;
	private int diskStoreSize;
	private int evictionCount;
	private int inMemoryHits;
	private int memoryStoreSize;
	private int misses;
	private int onDiskHits;
	private int size;
	private String statisticsAccuracy;
	
	/**
	 * @return the averageGetTime
	 */
	public double getAverageGetTime() {
		return averageGetTime;
	}
	/**
	 * @param averageGetTime the averageGetTime to set
	 */
	public void setAverageGetTime(double averageGetTime) {
		this.averageGetTime = averageGetTime;
	}
	/**
	 * @return the cacheHits
	 */
	public int getCacheHits() {
		return cacheHits;
	}
	/**
	 * @param cacheHits the cacheHits to set
	 */
	public void setCacheHits(int cacheHits) {
		this.cacheHits = cacheHits;
	}
	/**
	 * @return the diskStoreSize
	 */
	public int getDiskStoreSize() {
		return diskStoreSize;
	}
	/**
	 * @param diskStoreSize the diskStoreSize to set
	 */
	public void setDiskStoreSize(int diskStoreSize) {
		this.diskStoreSize = diskStoreSize;
	}
	/**
	 * @return the evictionCount
	 */
	public int getEvictionCount() {
		return evictionCount;
	}
	/**
	 * @param evictionCount the evictionCount to set
	 */
	public void setEvictionCount(int evictionCount) {
		this.evictionCount = evictionCount;
	}
	/**
	 * @return the inMemoryHits
	 */
	public int getInMemoryHits() {
		return inMemoryHits;
	}
	/**
	 * @param inMemoryHits the inMemoryHits to set
	 */
	public void setInMemoryHits(int inMemoryHits) {
		this.inMemoryHits = inMemoryHits;
	}
	/**
	 * @return the memoryStoreSize
	 */
	public int getMemoryStoreSize() {
		return memoryStoreSize;
	}
	/**
	 * @param memoryStoreSize the memoryStoreSize to set
	 */
	public void setMemoryStoreSize(int memoryStoreSize) {
		this.memoryStoreSize = memoryStoreSize;
	}
	/**
	 * @return the misses
	 */
	public int getMisses() {
		return misses;
	}
	/**
	 * @param misses the misses to set
	 */
	public void setMisses(int misses) {
		this.misses = misses;
	}
	/**
	 * @return the onDiskHits
	 */
	public int getOnDiskHits() {
		return onDiskHits;
	}
	/**
	 * @param onDiskHits the onDiskHits to set
	 */
	public void setOnDiskHits(int onDiskHits) {
		this.onDiskHits = onDiskHits;
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	/**
	 * @return the statisticsAccuracy
	 */
	public String getStatisticsAccuracy() {
		return statisticsAccuracy;
	}
	/**
	 * @param statisticsAccuracy the statisticsAccuracy to set
	 */
	public void setStatisticsAccuracy(String statisticsAccuracy) {
		this.statisticsAccuracy = statisticsAccuracy;
	}
	
}