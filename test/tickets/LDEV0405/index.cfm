<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
 ---><cfsetting showdebugoutput="no">


<cfscript>
function save(string nbr) {
	var e = entityNew("Comp"&nbr);
	e.setUnitId("1");
	e.setEntityId("2");
	e.setEntityTypeId("3");
	
	query datasource="ds#nbr#" name="local.q" {
		echo("select * from INFORMATION_SCHEMA.sessions");  // hyperSQL uses system_sessions, h2 only has sessions
	}
	echo("-"&q.recordcount);

	entitySave(e);
	//dump(entityLoad("Comp"&nbr));

}



save("1");
save("2");
save("3");
save("4");




</cfscript>
