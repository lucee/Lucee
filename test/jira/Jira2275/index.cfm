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
 ---><cfsetting showdebugoutput="false">	 

<!---
<cfset field = entityLoad("Field",{Name:"Test Field"}, true) />
<cfif isNull(field)>
	<cfset field = entityNew("Field",{Name:"Test Field", CustomFieldType:"Date/Time"}) />
	<cfset entitySave(field) />
</cfif>
--->
<cfset field = entityNew("Field",{Name:"Test Field", CustomFieldType:"Date/Time"}) />
<cfset entitySave(field) />


<cfset infocard = entityLoad("InfoCard",{ID:"0000RISKMANAGEMENT"}, true)>
<cfif isNull(infocard)>
	<cfset infocard = entityNew("InfoCard", {InfoCardNumber:"Test-1", Revision:"1A", Title:"Test InfoCard"}) />
	<cfset entitySave(infocard) />
</cfif>

<cfset fieldLink = entityLoad("FieldLink",{Field:field, InfoCard:infocard}, true) />
<cfif isNull(fieldLink)>
	<cfset fieldLink = entityNew("FieldLink",{Field:field, InfoCard:infocard, DisplayName:"Test Field Link", Type:"Test Field Link Type"}) />
	<cfset entitySave(fieldLink) />
</cfif>

<cfquery name="fl">
	select * from fieldLink2275
</cfquery>
<cfoutput>#listSort(fl.columnlist,"textnocase")#</cfoutput>