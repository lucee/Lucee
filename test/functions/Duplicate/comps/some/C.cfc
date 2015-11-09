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
 ---><cfcomponent extends="B3" hint="this is component c" displayname="Cer">
	<cfset cfc_C="">
   <cfset a="C_a"> 
   <cfset var_In_C="">
   
   <cffunction access="public" name="getVariableList" output="false" returntype="string" displayname="getVariableLister" hint="some function">
   		<!--- <cfdump var="#super.getVariableListX#" label="super.getVariableListX">		
   		<cfdump var="#super.super#" label="super.super">	
   		<cfdump var="#super#" label="super">	 --->
		
   		<cfdump var="#(this)#" label="this">		
   		<cfreturn structkeyList(variables)>
   </cffunction>
</cfcomponent>