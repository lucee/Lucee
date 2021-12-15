<!--- 
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

	
	<cffunction name="init" output="no">
    	<cfargument name="cfcName" type="string">
    	<cfset this.cfcName=arguments.cfcName>
		<cfreturn this>
    </cffunction>
    
    
	<cffunction name="_getData" access="private" output="no">
		<cfargument name="timeout" default="5000" type="numeric">
    	
	<!--- session --->
        <cfif 
			StructKeyExists(session,"cfcs") and 
			StructKeyExists(session.cfcs,this.cfcName) and 
			StructKeyExists(session.cfcs[this.cfcName],'getInfo') and
			StructKeyExists(session.cfcs[this.cfcName].getInfo,'lastModified') and
			DateAdd("n",10,session.cfcs[this.cfcName].getInfo.lastModified) GT now()>
        	<cfset var info=session.cfcs[this.cfcName].getInfo>
            <cfif not StructKeyExists(info,'mode') or (info.mode NEQ "develop" and info.mode NEQ "development")>
        		<cfreturn session.cfcs[this.cfcName]>
            </cfif>
        </cfif>
    <!--- request --->
        <cfif 
			StructKeyExists(request,"cfcs") and 
			StructKeyExists(request.cfcs,this.cfcName) and 
			StructKeyExists(request.cfcs[this.cfcName],'getInfo') and
			StructKeyExists(request.cfcs[this.cfcName].getInfo,'lastModified')>
        	<cfreturn request.cfcs[this.cfcName]>
        </cfif>
        
	<!--- load data from external resource --->
		<cfset var name="test"&createuniqueid()>
		
		<cfset session.cfcs[this.cfcName]={}>
        <cfset request.cfcs[this.cfcName]={}>
		
		
		<cfset var data={}>
		<cfthread name="#name#" wsdl="#this.cfcName#?wsdl" sess="#session.cfcs[this.cfcName]#" req="#request.cfcs[this.cfcName]#">
			<cfset var cfc= createObject('webservice',attributes.wsdl)>
	        <cfset attributes.req.listApplications=cfc.listApplications()>
	        <cfset attributes.sess.listApplications=attributes.req.listApplications>
	        <cfset attributes.req.getInfo=cfc.getInfo()>
	        <cfset attributes.req.getInfo.lastModified=now()>
	        <cfset attributes.sess.getInfo=attributes.req.getInfo>
	        
		</cfthread>
		<!--- <cfset systemOutput('<print-stack-trace>',true,true)>--->
		<cfif arguments.timeout GT 0>
			<cfthread action="join" name="#name#" timeout="#arguments.timeout#"/>
		</cfif>
		
		
        <cfreturn request.cfcs[this.cfcName]>
    </cffunction>

	<cffunction name="getInfo" access="remote" returntype="struct" output="no">
    	<cfreturn _getData().getInfo>
    </cffunction>
    
	<cffunction name="listApplications" access="remote" returntype="query" output="no">
    	<cfreturn _getData().listApplications>
    </cffunction>
    
	<cffunction name="getDownloadDetails" access="remote" output="no">
    	<cfargument name="type" required="yes" type="string">
        <cfargument name="serverId" required="yes" type="string">
        <cfargument name="webId" required="yes" type="string">
        <cfargument name="appId" required="yes" type="string">
    	<cfargument name="additional" required="no" type="struct">
    	
    	<cfset cfc = createObject('webservice',this.cfcName&"?wsdl")>
        <cfreturn cfc.getDownloadDetails(type,serverId,webId,appId,serialize(arguments.additional))>
    </cffunction>
</cfcomponent>
