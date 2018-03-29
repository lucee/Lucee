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
		<cfargument name="id" required="true" type="string">
		<cfargument name="config" required="true" type="struct">
		<cfargument name="listener" required="false" type="component">
		<cfset var cfcatch = "" />
		<cftry>
			<cfset variables.id=arguments.id>
			<cfset variables.config=arguments.config>
			<cfif len(arguments.listener) eq 0>
				<cflog text="init #variables.id# Listener is not a component" type="Error" file="#variables.logFileName#">
				<cfreturn>
			</cfif>
			<cflog text="init #variables.id# [#GetComponentMetaData(arguments.listener).path#]" type="information" file="#variables.logFileName#">
			<cfset variables.listener=arguments.listener>
			<cfcatch>
				<cfset _handleError(cfcatch, "init") />
			</cfcatch>
		</cftry>
	</cffunction>

	<cffunction name="start" access="public" output="no" returntype="void">
		<cfset var sleepStep = iif(variables.config.interval lt 500, 'variables.config.interval', de(500)) />
		<cfset var i=-1 />
		<cfset var cfcatch = "" />
		<cfset var startTime = getTickCount()>

		<cfif not StructKeyExists(variables, "listener")>
			<cfset setState("stopped")/>
			<cfreturn>
		</cfif>

		<cftry>
			<cfwhile variables.state EQ "stopping">
				<cfset sleep(10)>
			</cfwhile>
			<cfset setState("running")>

			<cfset variables._filter = cleanExtensions(variables.config.extensions) />

			<cflog text="start #variables.id# Directory[#variables.config.directory#]" type="information" file="#variables.logFileName#">
			<cfset var funcNames={add:config.addFunction, change:config.changeFunction, delete:config.deleteFunction}>
			<cftry>
				<!--- check if the directory actually exists

				https://luceeserver.atlassian.net/browse/LDEV-1767

				 --->
				<cfif not DirectoryExists(variables.config.directory)>
					<cflog text="start #variables.id# Directory [#variables.config.directory#] does not exist or is not a directory" type="Error" file="#variables.logFileName#" />
				</cfif>
				<cfcatch>
					<cflog text="poll #variables.id# Directory [#variables.config.directory#] DirectoryExists threw #cfcatch.message# #cfcatch.stacktrace#" 	type="Error" 	file="#variables.logFileName#" />
					<cfset setState("stopped") />
					<cfreturn>
				</cfcatch>
			</cftry>
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
			<cfif startTime eq -1>
				<!--- don't compare during first run, nothing will have changed at start --->
				<cfset startTime = getTickCount()>			
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
			</cfif>		
			<cfif variables.state NEQ "running">
				<cfbreak />
			</cfif>
			<!--- large directories can take a while and involve heavy io --->
			<cfscript>
				var executionTime = getTickCount() - startTime;
				var warningTimeout = 1000;
				if (structKeyExists(variables.config, "warningTimeout") )
					warningTimeout = variables.config.warningTimeout;
				if (warningTimeout gt 0 and executionTime gt warningTimeout)
					cflog (text="poll #variables.id# Directory [#variables.config.directory#] took #(executionTime)#ms",
							type="Information", 	
							file="#variables.logFileName#");
				startTime = -1;		
			</cfscript>
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
		<cfset setState("stopped") />
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
				<cfset _handleError(cfcatch, "loadFiles")/>
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
		<cflog text="stop #variables.id#" type="information" file="#variables.logFileName#">
		<cfset variables.setState("stopping")>
	</cffunction>

	<cffunction name="restart" access="public" output="no" returntype="void">
		<cflog text="restart #variables.id#" type="information" file="#variables.logFileName#">
		<cfif state EQ "running"><cfset stop()></cfif>
		<cfset start()>
	</cffunction>

	<cffunction name="setState" access="public" output="no" returntype="void">
		<cfargument name="newState" type="string" required="yes">
		<cflog text="poll #variables.id# #arguments.newState#"
				type="Information" 	
				file="#variables.logFileName#" />
		<cfscript>
			switch (arguments.newState){
				case "stopping":					
				case "running":
				case "stopped":
					variables.state=arguments.newState;
					break;
				default:
					throw (message="Unknown state: #arguments.newState#");
			}
		</cfscript>		
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
		<cflog text="#variables.id# Function #arguments.functionName#: #arguments.catchData.message# #arguments.catchData.detail# #arguments.catchData.stacktrace#"
		type="error" file="#variables.logFileName#" />
	</cffunction>

</cfcomponent>