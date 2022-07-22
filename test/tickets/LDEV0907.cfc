<!---
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	variables.qry=query(a:[1,2,3,4,5,6]);

	private function exe(time) {
	    query
	        name="qoq"
	        dbtype="query"
	        cachedWithin=createTimeSpan(0,0,0,time) {
	        echo('select * from qry where a>'&time);
	    }
	    return qoq;
	}

	public void function test(){
		assertFalse(exe(0).isCached());
		assertFalse(exe(0).isCached());

		assertFalse(exe(1).isCached());
		assertTrue(exe(1).isCached());
	}

	private void function test2(){ // TODO came from pull request 35
		assertFalse(exe(0).isCached());
		assertFalse(exe(0).isCached());

		assertFalse(exe(1).isCached());
		assertTrue(exe(1).isCached());

		// Cache should not be used here, even if it is populated.
		assertFalse(exe(0).isCached());

		// Legacy ACF/Railo/Lucee pre-5.0 behavior is that the last query would
		// have cleared that statement from the query cache.
		assertFalse(exe(1).isCached());
	}
}
</cfscript>