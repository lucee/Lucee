<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
 --->
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testLocalDocs() {
		local.specificDocs = "tags.cfm?item=cfapplication,functions.cfm?item=getapplicationsettings,components.cfm?item=org.lucee.cfml.Administrator";
		loop list="index.cfm,tags.cfm,functions.cfm,components.cfm,objects.cfm,#specificDocs#" item="local.p"{
			// systemOutput(local.p);
			local.docs = _internalRequest(
				template: "/lucee/doc/#local.p#"
			);
			expect( docs.status ).toBe( 200, "Status code #local.p#" );
		}
	}
	
}