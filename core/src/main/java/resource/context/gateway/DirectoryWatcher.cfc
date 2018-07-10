<!--- 
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 ---><cfcomponent output="no">

	<cfset variables.logFileName = "DirectoryWatcher" />
	<cfset variables.state="stopped" />


	<cffunction name="init" access="public" output="no" returntype="void">
		<cfargument name="id" required="false" type="string">
		<cfargument name="config" required="false" type="struct">
		<cfargument name="listener" required="false" type="component">
		<cfset var cfcatch = "" />
		<cftry>
			<cfset variables.id=id>
			<cfset variables.config=config>
			<cfset variables.listener=listener>

			<cflog text="init" type="information" file="#variables.logFileName#">
			<cfcatch>
				<cfset _handleError(cfcatch, "init") />
			</cfcatch>
		</cftry>
	</cffunction>


	<cffunction name="start" access="public" output="no" returntype="void">
		<cfset var sleepStep = iif(variables.config.interval lt 500, 'variables.config.interval', de(500)) />
		<cfset var i=-1 />
		<cfset var cfcatch = "" />
		<cftry>
			<cfwhile variables.state EQ "stopping">
				<cfset sleep(10)>
			</cfwhile>
			<cfset variables.state="running">

			<cfset variables._filter = cleanExtensions(variables.config.extensions) />

			<cflog text="start" type="information" file="#variables.logFileName#">
			<cfset var funcNames={add:config.addFunction, change:config.changeFunction, delete:config.deleteFunction}>

			<!--- check if the directory actually exists --->
			<cfif not DirectoryExists(variables.config.directory)>
				<cflog text="Directory [#variables.config.directory#] does not exist or is not a directory" type="Error" file="#variables.logFileName#" />
			</cfif>
			<cfif not StructKeyExists(variables.config,"recurse")>
				<cfset variables.config.recurse=false>
			</cfif>

			<cfset var files=loadFiles(variables.config.directory, variables.config.recurse, variables._filter) />
			<cfcatch>
				<cfset _handleError(cfcatch, "start") />
			</cfcatch>
		</cftry>

		<!--- first execution --->
		<cfwhile variables.state EQ "running">
			<cftry>
				<cfset var coll=compareFiles(files,funcNames,config.directory, config.recurse, variables._filter)>
				<cfset files=coll.data>
				<cfset var name="">
				<cfset var funcName="">
				<cfcatch>
					<cfset _handleError(cfcatch, "start") />
				</cfcatch>
			</cftry>
			<cfloop collection="#coll.diff#" item="name">
				<cftry>
					<cfset funcName=coll.diff[name].action>
					<cfif len(funcName)>
						<cfset variables.listener[funcName](coll.diff[name])>
					</cfif>
					<cfcatch>
						<cfset _handleError(cfcatch, "start") />
					</cfcatch>
				</cftry>
			</cfloop>
			<cfif variables.state NEQ "running">
				<cfbreak />
			</cfif>
			<!--- sleep untill the next run, but cut it into half seconds, so we can stop the gateway --->
			<cfloop from="#sleepStep#" to="#variables.config.interval#" step="#sleepStep#" index="i">
				<cfset sleep(sleepStep) />
				<cfif variables.state neq "running">
					<cfbreak />
				</cfif>
			</cfloop>
			<!--- some extra sleeping if --->
			<cfif variables.config.interval mod sleepStep and variables.state eq "running">
				<cfset sleep((variables.config.interval mod sleepStep)) />
			</cfif>
		</cfwhile>
		<cfset variables.state="stopped" />
	</cffunction>


	<cffunction name="loadFiles" access="private" output="no" returntype="struct">
		<cfargument name="directory" type="string" required="yes">
		<cfargument name="recurse" type="boolean" required="no" default="#false#">
		<cfargument name="fileFilter" type="string" required="no" default="*" />
		<cfset var cfcatch = "" />
		<cftry>
			<cfset var dir = getFiles(arguments.directory, arguments.recurse, arguments.fileFilter) />
			<cfset var sct={} />
			<cfloop query="dir">
				<cfset sct[dir.directory&server.separator.file&dir.name] = createElement(dir) />
			</cfloop>
			<cfreturn sct />
			<cfcatch>
				<cfset _handleError(cfcatch, "loadFiles") />
			</cfcatch>
		</cftry>
	</cffunction>


	<cffunction name="getFiles" access="private" output="no" returntype="query">
		<cfargument name="directory" type="string" required="yes">
		<cfargument name="recurse" type="boolean" required="no" default="false" />
		<cfargument name="fileFilter" type="string" required="no" default="*" />
		<cfset var cfcatch = "" />
		<cftry>
			<cfset var qDir = "" />
			<cfdirectory directory="#arguments.directory#" action="list" name="qDir" type="file"
				filter="#arguments.fileFilter#" recurse="#arguments.recurse#" />
			<cfreturn qDir />
			<cfcatch>
				<cfset _handleError(cfcatch, "getFiles") />
			</cfcatch>
		</cftry>
	</cffunction>


	<cffunction name="compareFiles" access="private" output="no" returntype="struct">
		<cfargument name="last" type="struct" required="yes">
		<cfargument name="funcNames" type="struct" required="yes">
		<cfargument name="directory" type="string" required="yes">
		<cfargument name="recurse" type="boolean" required="no" default="false" />
		<cfargument name="fileFilter" type="string" required="no" default="*" />
		<cfset var cfcatch = "" />
		<cftry>
			<cfset var dir = getFiles(arguments.directory, arguments.recurse, arguments.fileFilter) />
			<cfset var sct={}>
			<cfset var diff={}>
			<cfset var name="">
			<cfset var tmp="">
			<!--- check for new and changed files --->
			<cfloop query="dir">
				<cfset name=dir.directory&server.separator.file&dir.name>
				<!--- populate the struct with all currently found files/directories --->
				<cfset sct[name]=createElement(dir)>
				<!--- file existed already --->
				<cfif StructKeyExists(arguments.last,name)>
					<!--- date last modified has changed? --->
					<cfif dir.dateLastModified NEQ arguments.last[name].dateLastModified>
						<cfset tmp = createElement(dir)>
						<cfset tmp.action = arguments.funcNames.change>
						<cfset diff[name] = tmp>
					</cfif>
				<!--- new file --->
				<cfelse>
					<cfset tmp=createElement(dir)>
					<cfset tmp.action=funcNames.add>
					<cfset diff[name]=tmp>
				</cfif>
			</cfloop>

			<!--- check if files are deleted --->
			<cfloop collection="#last#" item="name">
				<cfif not StructKeyExists(sct,name)>
					<cfset last[name].action=funcNames.delete>
					<cfset diff[name]=last[name]>
				</cfif>
			</cfloop>

			<cfreturn {data:sct,diff:diff}>
			<cfcatch>
				<cfset _handleError(cfcatch, "compareFiles") />
			</cfcatch>
		</cftry>
	</cffunction>



	<cffunction name="createElement" access="private" output="no" returntype="struct">
		<cfargument name="dir" type="query" required="yes">
		<cfreturn {dateLastModified:dir.dateLastModified, size:dir.size, name:dir.name, directory:dir.directory,id:variables.id}>
	</cffunction>


	<cffunction name="stop" access="public" output="no" returntype="void">
		<cflog text="stop" type="information" file="#variables.logFileName#">
		<cfset variables.state="stopping">
	</cffunction>

	<cffunction name="restart" access="public" output="no" returntype="void">
		<cfif state EQ "running"><cfset stop()></cfif>
		<cfset start()>
	</cffunction>

	<cffunction name="getState" access="public" output="no" returntype="string">
		<cfreturn variables.state />
	</cffunction>

	<cffunction name="sendMessage" access="public" output="no" returntype="string">
		<cfargument name="data" required="false" type="struct">
		<cfreturn "sendGatewayMessage() has not been implemented for the event gateway [DirectoryWatcher]. If you want to modify it, please edit the following CFC:"& expandpath("./") & "DirectoryWatcher.cfc">
	</cffunction>


	<cffunction name="cleanExtensions" access="private" output="no" returntype="string">
		<cfargument name="extensions" required="true" type="string">
		<!--- replace the commas and optional trailing spaces with pipes ("|"), because that's the delimiter cfdirectory works with. --->
		<cfreturn rereplace(trim(arguments.extensions), " *, *", "|", "all") />
	</cffunction>


	<cffunction name="_handleError" returntype="void" access="private" output="no">
		<cfargument name="catchData" required="yes" />
		<cfargument name="functionName" type="string" required="no" default="unknown" />
		<cflog text="Function #arguments.functionName#: #arguments.catchData.message# #arguments.catchData.detail#"
		type="error" file="#variables.logFileName#" />
	</cffunction>

</cfcomponent>