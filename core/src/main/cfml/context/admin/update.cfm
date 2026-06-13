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
 ---><cfsetting showdebugoutput="false">
<cfparam name="url.json" default="false" type="boolean">
<cftry>
	<cfparam name="session.alwaysNew" default="true" type="boolean">
	<cfinclude template="services.update.functions.cfm">
	<cfset adminType=url.adminType>
	<cfset request.adminType=url.adminType>
	<cfset password=session["password"&adminType]>
	<cfset id="rai:"&hash(adminType&":"&password)>
	<cfif not structKeyExists(session,id)>
		<cfset session[id]={}>
	</cfif>

	<cfif true or !structKeyExists(session[id],"content") 
		|| !structKeyExists(session[id],"last") 
		|| DateDiff("m",session[id].last,now()) GT 5
		|| session.alwaysNew>
		<cfinclude template="web_functions.cfm">
		
		<!--- <cfset self = adminType & ".cfm"> --->
		<cfset stText.services.update.update="There is a Lucee update <b>( {available} )</b> available for your current version <b>( {current} )</b>.">

		<!--- Core --->
		<cfif adminType == "server">
			<cfset updateInfo = getAvailableVersion()>
			<cfset updateResult = hasNewerVersion( server.lucee.version, updateInfo )>
			<cfset hasUpdate = updateResult.hasUpdate>
			<cfset available = updateResult.availableVersion>
		</cfif>

		<!--- Extensions --->
		<cfparam name="err" default="#struct(message:"",detail:"")#">
		<cfinclude template="ext.functions.cfm">
		<cfadmin 
			action="getExtensions"
			type="#adminType#"
			password="#password#"
			returnVariable="extensions"><!--- #session["password"&url.adminType]# --->
		
		<cfif extensions.recordcount GT 0>
			
			
			<cfset request.adminType=url.adminType>
			<cfset external=getLuceeExtensions(getExtensionGroups())>
			<cfset extUpdates = []>
			<cfsavecontent variable="ext" trim="true">
				<cfloop query="extensions">
					<cfscript>
						sct = {};
						loop list="#extensions.columnlist()#" item="key" {
							sct[ key ]=extensions[ key ];
						}
						updateVersion = updateAvailable( sct, external );
						if ( updateVersion eq "false" )
							continue;
						link = "?action=ext.applications&action2=detail&id=#extensions.id#&groupId=#extensions.groupId#&artifactId=#extensions.artifactId#";
						arrayAppend( extUpdates, {
							"name": extensions.name,
							"current": sct.version,
							"available": updateVersion
						} );
					</cfscript>
					<cfoutput>
						<a href="#link#">- #extensions.name# - <b>#updateVersion#</b> ( #sct.version# ) </a><br>
					</cfoutput>
				</cfloop>
			</cfsavecontent>
		</cfif>

		<cfsavecontent variable="content" trim="true">
			<cfoutput>
				<cfif ( adminType == "server" and hasUpdate ) or ( extensions.recordcount and len( ext ) )>
					<cfif adminType == "server" and hasUpdate>
						<p><a href="?action=services.update">
							#replace( stText.services.update.update, { '{available}': available, '{current}': server.lucee.version } )#
						</a></p>
					</cfif>
					<cfif extensions.recordcount and len( ext )>
						<p><a href="?action=ext.applications">
							There are updates available for your installed Extension(s).<br>
							#ext#
						</a></p>
					</cfif>
				</cfif>
			</cfoutput>
		</cfsavecontent>
		<cfset session[id].content=content>
		<cfset session[id].last=now()> 
	<cfelse>
		<cfset content=session[id].content>
	</cfif>
<cfscript>
if(url.json?:false) {
	result = {
		"currentVersion": server.lucee.version,
		"hasUpdate": adminType == "server" && ( hasUpdate ?: false ),
		"availableVersion": available ?: "",
		"extensionUpdates": extUpdates ?: []
	};
	cfsetting(showdebugoutput=false);
	cfcontent(reset=true, type="application/json");
	echo(serializeJson( result ));
	abort;
}
</cfscript>
	<cfoutput>#content#</cfoutput>
	<cfcatch>
		<cfset systemOutput(cfcatch,true)>
	</cfcatch>
</cftry>
<cfabort>