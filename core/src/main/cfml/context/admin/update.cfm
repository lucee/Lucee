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
		
		<cfset self = adminType & ".cfm">
		<cfset stText.services.update.update="There is a Lucee update <b>( {available} )</b> available for your current version <b>( {current} )</b>.">

	<!--- Core --->
		<cfif adminType == "server">
			<cfset filterMajor = true>
			<cfset hasUpdate = false>

			<cfset curr=server.lucee.version>
			<cfset curr=listFirst(server.lucee.version,".")>
			
			<cfset updateInfo=getAvailableVersion()>
			<cfif not structKeyExists(updateInfo, "available")>
				<!--- no update available --->
			<cfelseif server.lucee.state EQ "stable">
				<cfset get_stable = []>
				<cfloop index="stableList" array="#updateInfo.otherVersions#">
					<cfif ( !listContainsNoCase(stableList,"-SNAPSHOT") EQ 1 ) AND ( !listContainsNoCase(stableList,"-BETA") EQ 1 AND (!listContainsNoCase(stableList,"-RC") EQ 1) )>
						<cfset arrayAppend(get_stable,stableList)>
					</cfif>
				</cfloop>
				<cfset available = Arraylast(get_stable)>
				<cfset hasUpdate = server.lucee.version LT available>
			<cfelse>
				<cfset ava_ver = listfirst(updateInfo.available,"-")>
				<cfif curr neq ava_ver>
					<!-- only show updates for the current major version, ie on 5.4, not show 6.0 snapshots -->
					<cfset curr=listFirst(server.lucee.version,".")>
					<cfset available = getUpdateForMajorVersion(updateInfo.otherVersions, curr )>
					<cfset ava_ver = listfirst(updateInfo.available,"-")>
				<cfelse>
					<cfset available = updateInfo.available>
				</cfif>
				<cfif len(available) eq 0 or server.lucee.version eq available>
					<cfset hasUpdate = false>
				<cfelse>
					<cfset cur_ver = listfirst(curr,"-")>
					<cfloop from="1" to="#listlen(cur_ver,".")#" index="i">
						<cfif len(listgetat(ava_ver,i,".")) eq 1>
							<cfset last = 0&listgetat(ava_ver,i,".")>
							<cfset ava_ver = listsetat(ava_ver,i,last,".")>
						</cfif>
						<cfif len(listgetat(cur_ver,i,".")) eq 1>
							<cfset last = 0&listgetat(cur_ver,i,".")>
							<cfset cur_ver = listsetat(cur_ver,i,last,".")>
						</cfif>
					</cfloop>
						<cfset ava_ver = ava_ver&"-"&listlast(available,"-")>
					<cfset cur_ver = cur_ver&"-"&listlast(curr,"-")>
					<cfset hasUpdate = structKeyExists(updateInfo,"available") && ava_ver gt cur_ver>
				</cfif>
			</cfif>
		</cfif>

	<!--- Extensions --->
		<cfparam name="err" default="#struct(message:"",detail:"")#">
		<cfinclude template="ext.functions.cfm">
		<cfadmin 
			action="getRHExtensions"
			type="#adminType#"
			password="#password#"
			returnVariable="extensions"><!--- #session["password"&url.adminType]# --->
		<cfif extensions.recordcount GT 0>
			<cfadmin 
				action="getRHExtensionProviders"
				type="#adminType#"
				password="#password#"
				returnVariable="providers">
		
			<cfset request.adminType=url.adminType>
			<cfset external=getAllExternalData()>

			<cfsavecontent variable="ext" trim="true">
				<cfloop query="extensions">
					<cfscript>
						sct = {};
						loop list="#extensions.columnlist()#" item="key" {
							sct[ key ]=extensions[ key ];
						}
						updateVersion= updateAvailable( sct, external );
						if (updateVersion eq "false")
							continue;
						uid=extensions.id
						link="";
						dn="";
						link="?action=ext.applications&action2=detail&id=#uid#";
					</cfscript>
					<cfoutput>
						<a href="#link#" style="color:red;text-decoration:none;">- #extensions.name# - <b>#updateVersion#</b> ( #sct.version# ) </a><br>
					</cfoutput>
				</cfloop>
			</cfsavecontent>
		</cfif>

	<!--- Promotion  disabled for the moment
		<cfset existingExtensions={}>
		<cfloop query="#extensions#">
			<cfset existingExtensions[extensions.id]=extensions.id>
		</cfloop>
		<cfset promotion={level:0}>
		
		<cfset request.adminType=url.adminType>
		<cfinclude template="extension.functions.cfm">
		<cfset data=loadAllProvidersData(50000,false)>
						
		<cfloop collection="#data#" item="provider" index="providerURL">
			<cfif not isSimpleValue(provider)>
				<cfset qry=provider.listApplications>
				<cfloop query="#provider.listApplications#">
					<cfset uid=qry.id>
					<cfif	(qry.type EQ request.admintype or  qry.type EQ "all") and
							!structKeyExists(existingExtensions,uid) and
							isDefined('qry.promotionLevel') and 
							isNumeric(qry.promotionLevel) and
							qry.promotionLevel GT promotion.level>
						<cfset promotion.txt=qry.promotionText>
						<cfset promotion.uri="server.cfm?action=extension.applications&action2=detail&uid=#uid#">
						<cfset promotion.uid=uid>
						<cfset promotion.level=qry.promotionLevel>
						<cfset promotion.label=qry.label>
						<cfset promotion.price=qry.price>
						<cfif len(qry.image)><cfset promotion.img=getDumpNail(qry.image,230,100)><cfelse><cfset promotion.img=""></cfif>
					</cfif>
				</cfloop>
			</cfif>
		</cfloop>
--->



		<cfsavecontent variable="content" trim="true">
			<cfoutput>
				
				<!--- Core --->
				<cfif adminType == "server" and hasUpdate>
					<div class="error">
						<a href="?action=services.update" style="color:red;text-decoration:none;">
							#replace( stText.services.update.update, { '{available}': available, '{current}': server.lucee.version } )#
						</a>
					</div>
				</cfif>
				
				<!--- Extension --->
				<cfif extensions.recordcount and len(ext)>
				<div class="error">
					<a href="#self#?action=ext.applications" style="color:red;text-decoration:none;">
						There are updates available for your installed Extension(s).<br>
						#ext#
					</a>
				</div>
				</cfif>
				
				<!--- Promotion<div class="normal"></div> disabled for the moment
				<cfif promotion.level GT 0>
					
					<h3><a href="#promotion.uri#">#promotion.label#</a></h3>
					<cfif len(promotion.img)>
						<img src="#promotion.img#"  /><br>
					</cfif>
					<span class="comment">#promotion.txt#</span>
					
				</cfif>
				 --->
				
			</cfoutput>
		</cfsavecontent>
		<cfset session[id].content=content>
		<cfset session[id].last=now()> 
	<cfelse>
		<cfset content=session[id].content>
	</cfif>

	<cfoutput>#content#</cfoutput>
	
	<cfcatch>
		<cfoutput>
			<!--- <div class="error">
				Failed to retrieve update information<br>
				<span class="comment">#cfcatch.message# #cfcatch.detail#</span>
			</div> --->
		</cfoutput>
	</cfcatch>
</cftry>
<cfabort>