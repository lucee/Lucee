<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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

<!--- make sure the db itself is working --->
<cfquery>
	select 1 as one
</cfquery>


<cfscript>
e = entityNew("E",{id=1,foo="bar",susi="sorglos"});
entitySave(e);
ormFlush();


// see https://mycuteblog.com/fix-for-hibernate-legacy-style-query-parameters-no-longer-supported/
//qry=ORMExecuteQuery( "from E where id in ( ? )", [1] );
qry=ORMExecuteQuery( "from E where id in ( :id )", {id:1} );



</cfscript>