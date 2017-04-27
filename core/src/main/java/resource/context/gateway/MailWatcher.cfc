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
 ---><cfcomponent>
	
    
    <cfset state="stopped">
	
	<cffunction name="init" access="public" output="no" returntype="void">
		<cfargument name="id" required="false" type="string">
		<cfargument name="config" required="false" type="struct">
		<cfargument name="listener" required="false" type="component">
    	<cfset variables.id=id>
        <cfset variables.config=config>
        <cfset variables.listener=listener>
        
        <cflog text="init" type="information" file="MailWatcher">
        
	</cffunction>


	<cffunction name="start" access="public" output="no" returntype="void">
		<cfwhile state EQ "stopping">
        	<cfset sleep(10)>
        </cfwhile>
        <cfset variables.state="running">
        
        
        <cflog text="start" type="information" file="MailWatcher">
		
        
        <cfset var last=now()>
        <cfset var mail="">
        <cfwhile variables.state EQ "running">
        	<cftry>
				<cfset mails=getMailsNewerThan(config.server,config.port,config.username,config.password,config.attachmentpath,last)>
                <cfloop array="#mails#" index="el">
                	<cfif len(trim(config.functionName))><cfset variables.listener[config.functionName](el)></cfif>
                </cfloop>
                
                <cfcatch>
                	<cflog text="#cfcatch.message#" type="Error" file="MailWatcher">
                </cfcatch>
            </cftry>
            <cfset last=now()>
            
            <cfif variables.state NEQ "running">
            	<cfbreak>
            </cfif>
            <cfset sleep(config.interval)>
    	</cfwhile>
        <cfset variables.state="stopped">
        
	</cffunction>
    
    
    
    <cffunction name="getMailsNewerThan" returntype="array" output="yes">
        <cfargument name="server" type="string" required="yes">
        <cfargument name="port" type="numeric" required="yes">
        <cfargument name="user" type="string" required="yes">
        <cfargument name="pass" type="string" required="yes">
        <cfargument name="attachmentpath" type="string" required="yes">
        <cfargument name="newerThan" type="date" required="yes">
        
        <cfset var mails="">
        <cfset var arr=[]>
        <cfset var sct="">
        
        <cfpop 
            action="getall" 
            name="mails" 
            server="#arguments.server#" 
            port="#arguments.port#" 
            username="#arguments.user#" 
            password="#arguments.pass#" 
            attachmentpath="#arguments.attachmentpath#" 
            generateuniquefilenames="yes">
         
        
        <cfloop query="mails">
            <cfif mails.date GTE newerThan>
                <cfset sct={}>
                <cfloop index="col" list="#mails.columnlist#">
                    <cfset sct[col]=mails[col]>
                </cfloop>
                <cfset ArrayAppend(arr,sct)>
            </cfif>
        </cfloop>
        <cfreturn arr>
    </cffunction>

    

	<cffunction name="stop" access="public" output="no" returntype="void">
    	<cflog text="stop" type="information" file="MailWatcher">
		<cfset variables.state="stopping">
	</cffunction>

	<cffunction name="restart" access="public" output="no" returntype="void">
		<cfif state EQ "running"><cfset stop()></cfif>
        <cfset start()>
	</cffunction>

	<cffunction name="getState" access="public" output="no" returntype="string">
		<cfreturn state>
	</cffunction>

	<cffunction name="sendMessage" access="public" output="no" returntype="string">
		<cfargument name="data" required="false" type="struct">
		<cfreturn "sendGatewayMessage() has not been implemented for the event gateway [MailWatcher]. If you want to modify it, please edit the following CFC:"& expandpath("./") & "MailWatcher.cfc">
	</cffunction>

</cfcomponent>