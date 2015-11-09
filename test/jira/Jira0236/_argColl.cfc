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
 ---><cfcomponent>

<cffunction name="onMissingMethod" hint="method to handle missing methods" access="public" returntype="Any" output="true">
    <cfargument name="missingMethodName" type="string" required="true">
    <cfargument name="missingMethodArguments" type="struct" required="false" default="#StructNew()#">
    <cfinvoke method="test" argumentcollection="#arguments.missingMethodArguments#" returnvariable="res1"/>
    <cfinvoke method="test" argumentcollection="#arguments.missingMethodArguments#" b="37"  returnvariable="res2"/>
    #trim(res1)##trim(res2)#
</cffunction>

<cffunction name="test" hint="method to handle missing methods" access="public" returntype="Any" output="false">
	<cfargument name="a" type="Any" required="true" />
	<cfargument name="b" type="Any" required="true" />
    <cfset var rtn="">
    <cfset var keys=structKeyArray(arguments)>
    <cfset ArraySort(keys,'textnocase')>
    
    <cfloop array="#keys#" index="key">
    	<cfset rtn=rtn&key&":"&arguments[key]&";">
    </cfloop>
    
    
    <cfreturn rtn>
    
</cffunction>




</cfcomponent>