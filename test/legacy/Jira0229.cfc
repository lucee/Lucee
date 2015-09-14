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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	
<cfscript>
  function replaceWS(string str,string replacement=''){
    return rereplace(str,"[[:space:]]+",replacement,"all");
  }
</cfscript>

	<cffunction name="setUp"></cffunction>
	<cffunction name="test">
	
<!--- test to see what happens with getBaseTagData --->
<cfimport prefix="tags" taglib="./Jira0229/" />

<cfsavecontent variable="local.content">
<cfsetting enablecfoutputonly="yes">
<!--- getBaseTagData will thrown an error --->
<tags:mytag label="lvl 1">
  <tags:mytag label="lvl 2">
    <tags:mytag label="lvl 3"></tags:mytag>
  </tags:mytag>
</tags:mytag>
<cfsetting enablecfoutputonly="no">
</cfsavecontent>
<cfset assertEquals(
		"{Start:lvl1}{Start:lvl2}{label:lvl2;parent-label:lvl1}{Start:lvl3}{label:lvl3;parent-label:lvl2;parent-parent-label:lvl1}{End:lvl3}{End:lvl2}{End:lvl1}"
		,replaceWS(trim(content)))>


<!--- the instance number on getBaseTagData correctly 
      works when the tags are not immediately nested --->
<cfsavecontent variable="local.content">
<cfsetting enablecfoutputonly="yes">
<tags:mytag label="lvl 1">
  <tags:mytag2>
    <tags:mytag label="lvl 2">
      <tags:mytag2>
        <tags:mytag label="lvl 3"></tags:mytag>
      </tags:mytag2>
    </tags:mytag>
  </tags:mytag2>
</tags:mytag>
<cfsetting enablecfoutputonly="no">
</cfsavecontent>

<cfset assertEquals(
		"{Start:lvl1}{Start:lvl2}{label:lvl2;parent-label:lvl1}{Start:lvl3}{label:lvl3;parent-label:lvl2;parent-parent-label:lvl2}{End:lvl3}{End:lvl2}{End:lvl1}"
		,replaceWS(trim(content)))>

	
	</cffunction>
</cfcomponent>