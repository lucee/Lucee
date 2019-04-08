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

import java.util.Random;

import lucee.commons.collection.concurrent.ConcurrentLinkedHashMapPro.Entry;

public class RandomPolicy implements EvictionPolicy {

    Random random = new Random();

    @Override
    public boolean accessOrder() {
	return false;
    }

    @Override
    public boolean insertionOrder() {
	return false;
    }

    @Override
    public Entry<?, ?> evictElement(Entry<?, ?> head) {
	int hops = random.nextInt();
	Entry<?, ?> entryToEvict = head.getAfter();
	for (int i = 0; i < hops; i++)
	    entryToEvict = entryToEvict.getAfter();
	return entryToEvict;
    }

    @Override
    public Entry<?, ?> recordInsertion(Entry<?, ?> head, Entry<?, ?> insertedEntry) {
	return null;
    }

    @Override
    public Entry<?, ?> recordAccess(Entry<?, ?> head, Entry<?, ?> accessedEntry) {
	return null;
    }

}