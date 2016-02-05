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
 ---><cfcomponent name="FieldLink" entityname="FieldLink" table="fieldLink2275" persistent="true" output="false" accessors="true" hint="Field Placeholder cfc">
	<cfproperty name="Field" fieldtype="id,many-to-one" cfc="Field" fkcolumn="field_id" hint="ID of Field" /> 
	<cfproperty name="InfoCard" fieldtype="id,many-to-one" cfc="InfoCard" fkcolumn="infocard_id" hint="Template ID of Field" /> 
	
	<cfproperty name="DisplayName" column="display" sqltype="nvarchar(50)"  />
	<cfproperty name="Type" column="type" sqltype="nvarchar(50)"  />
</cfcomponent>