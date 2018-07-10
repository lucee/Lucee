<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase"	{

	public void function testCreateCollection() localmode=true {
		try {

			curr=GetDirectoryFromPath(GetCurrentTemplatePath());
			path=curr&"_collection/";
	
			if(DirectoryExists(path)) {
				directoryDelete(path,true);
			}
			directoryCreate(path);


			collection action="list" name="test";
			lenBefore=test.recordcount;


			collection 
				action="create" 
				collection="collA" 
				path="#path#/#server.ColdFusion.ProductName#" 
				language="English";
			collection 
				action="create" 
				collection="collB" 
				path="#path#/#server.ColdFusion.ProductName#" 
				language="English";

			collection action="list" name="test";
			lenAfter=test.recordcount;

			assertEquals(lenBefore+2,lenAfter);


			collection 
				action="optimize" 
				collection="colla";

			collection 
				action="delete" 
				collection="collA";
			collection 
				action="delete" 
				collection="collB";
		}
		finally {
			if(DirectoryExists(path)) {
				directoryDelete(path,true);
			}
		}
	}

}
</cfscript>