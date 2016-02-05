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
 ---><cfcomponent name="InfoCard" entityname="InfoCard" table="infocard2275" persistent="true" output="false" accessors="true" hint="Represents an Infocard">
	<cfproperty name="ID" column="id" fieldtype="id" ormtype="string" length="18" update="false" insert="false" />
	<cfproperty name="InfoCardNumber" column="number" sqltype="nvarchar(30)"  />
	<cfproperty name="Revision" column="revision" sqltype="nvarchar(30)"  />
	<cfproperty name="Title" column="title" sqltype="nvarchar(300)"  />

	<cffunction name="init" output="false" access="public" returntype="InfoCard">
		<cfset setID(Left(replace(CreateUUID(),"-","","ALL"),18)) />
		<cfreturn this />
	</cffunction>
</cfcomponent>