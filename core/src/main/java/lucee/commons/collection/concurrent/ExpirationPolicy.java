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
package lucee.commons.collection.concurrent;

import lucee.commons.collection.concurrent.ConcurrentLinkedHashMapPro.Entry;

public class ExpirationPolicy implements EvictionPolicy {

	long ageThresholdMillis;
	long idleTimeThresholdMillis;

	public ExpirationPolicy(long ageThresholdMillis, long idleTimeThresholdMillis) {
		this.ageThresholdMillis = ageThresholdMillis;
		this.idleTimeThresholdMillis = idleTimeThresholdMillis;
	}

	@Override
	public boolean accessOrder() {
		return true;
	}

	@Override
	public boolean insertionOrder() {
		return false;
	}

	@Override
	public Entry<?, ?> evictElement(Entry<?, ?> head) {
		return head.getAfter();
	}

	@Override
	public Entry<?, ?> recordInsertion(Entry<?, ?> head, Entry<?, ?> insertedEntry) {
		return null;
	}

	@Override
	public Entry<?, ?> recordAccess(Entry<?, ?> head, Entry<?, ?> accessedEntry) {
		long now = System.currentTimeMillis();
		long accessedEntryAge = (now - accessedEntry.getCreationTime());
		long accessedEntryIdleTime = (now - accessedEntry.getLastAccessTime());
		if (accessedEntryIdleTime < idleTimeThresholdMillis && accessedEntryAge < ageThresholdMillis) return head;
		return accessedEntry.getAfter();
	}

}