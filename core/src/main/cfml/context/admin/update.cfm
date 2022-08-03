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
 ---> 
<cfsetting showdebugoutput="false">
<cftry>
	<cfparam name="session.alwaysNew" default="true" type="boolean">
	<cfparam name="hasUpdate" default="false">
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
		<cfset stText.services.update.update="A patch <b>({available})</b> is available for your current version <b>({current})</b>.">

	<!--- Core --->
		<cfif adminType == "server">
			
			<cfset curr=server.lucee.version>
			<cfset updateInfo=getAvailableVersion()>
			<cfif server.lucee.state EQ "RC">
				<cfset get_rc = "">
				<cfloop index="rcList" array="#updateInfo.otherVersions#">
					<cfif listContainsNoCase(rcList,"-RC") EQ 1>
						<cfset get_rc = listAppend(get_rc,rcList)>
					</cfif>
				</cfloop>
				<cfset available = listlast(get_rc)>
				<cfset hasUpdate = curr LT available>
			<cfelseif server.lucee.state EQ "stable">
				<cfset get_stable = "">
				<cfloop index="stableList" array="#updateInfo.otherVersions#">
					<cfif ( !listContainsNoCase(stableList,"-SNAPSHOT") EQ 1 ) AND ( !listContainsNoCase(stableList,"-BETA") EQ 1 AND (!listContainsNoCase(stableList,"-RC") EQ 1) )>
						<cfset get_stable = listAppend(get_stable,stableList)>
					</cfif>
				</cfloop>
				<cfset available = listlast(get_stable)>
				<cfset hasUpdate = curr LT available>
			<cfelseif structKeyExists(updateInfo,"available")>
				<cfset ava_ver = listfirst(updateInfo.available,"-")>
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
				<cfset ava_ver = ava_ver&"-"&listlast(updateInfo.available,"-")>
				<cfset cur_ver = cur_ver&"-"&listlast(curr,"-")>
				<cfset hasUpdate = structKeyExists(updateInfo,"available") && ava_ver gt cur_ver>
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
					<cfset sct={}>
					<cfloop list="#extensions.columnlist()#" item="key">
						<cfset sct[key]=extensions[key]>
					</cfloop>
					<cfif !updateAvailable(sct,external)>
						<cfcontinue>
					</cfif>
					<cfset uid=extensions.id>
					<cfset link="">
					<cfset dn="">
					<cfset link="?action=ext.applications&action2=detail&id=#uid#">
					<cfoutput>
						<a href="#link#" style="color:red;text-decoration:none;">- #extensions.name#</a> #extensions.version#<br>
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
		</cfloop> --->

		<cfsavecontent variable="content" trim="true">
			<cfoutput>
				
				<!--- Core --->
				<cfif adminType == "server" and hasUpdate>
					<div class="error">
						<a href="server.cfm?action=services.update" style="color:red;text-decoration:none;">
							<cfif server.lucee.state eq "SNAPSHOT" OR server.lucee.state eq "BETA">
								#replace( stText.services.update.update, { '{available}': updateinfo.available, '{current}': curr } )#
							<cfelse>	
								#replace( stText.services.update.update, { '{available}': available, '{current}': curr } )#
							</cfif>
						</a>
					</div>
				</cfif>
				
				<!--- Extension --->
				<cfif extensions.recordcount and len(ext)>
				<div class="error">
					<a href="#self#?action=ext.applications" style="color:red;text-decoration:none;">
						There are some updates available for your installed Extensions.<br>
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
<!-- no updates available -->
<cfabort>