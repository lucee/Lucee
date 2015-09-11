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

<cfparam name="session.alwaysNew" default="true" type="boolean">


<cffunction name="getAvailableVersion" output="false">
	<cfargument name="update">
	<cfset var http="">
	<cftry>
		<cfhttp 
			url="#update.location#/lucee/remote/version/Info.cfc?method=getpatchversionfor&level=#server.ColdFusion.ProductLevel#&version=#server.lucee.version#" 
			method="get" resolveurl="no" result="http">
		<cfwddx action="wddx2cfml" input="#http.fileContent#" output="local.wddx">
		<cfset session.availableVersion=wddx>
		<cfreturn session.availableVersion>
		<cfcatch>
			<cfreturn "">
		</cfcatch>
	</cftry>
</cffunction>



<cftry>

	<cfset adminType=url.adminType>
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
			<cfadmin 
				action="getUpdate"
				type="#adminType#"
				password="#password#"
				returnvariable="update">
			<cfset curr=server.lucee.version>
			<cfset avi=getAvailableVersion(update)>
			<cfset hasUpdate=curr LT avi>
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
			<cfadmin 
				action="getExtensionProviders"
				type="#adminType#"
				password="#password#"
				returnVariable="providers">
			<cfset request.adminType=url.adminType>
			<cfset data=getAllExternalData()>
			<cfsavecontent variable="ext" trim="true">
				<cfloop query="extensions">
					<cfset sct={}>
					<cfloop list="#extensions.columnlist()#" item="key">
						<cfset sct[key]=extensions[key]>
					</cfloop>

					<cfif !updateAvailable(sct,extensions)>
						<cfcontinue>
					</cfif>
					<cfset uid=createId(extensions.provider,extensions.id)>
					<cfset link="">
					<cfset dn="">
					<cfset link="#self#?action=extension.applications&action2=detail&uid=#uid#">
					<cfoutput>
						<a href="#link#" style="text-decoration:none;">- #extensions.label#</a><br>
					</cfoutput>
				</cfloop>
			</cfsavecontent>
		</cfif>

	<!--- Promotion --->
		<cfset existingExtensions={}>
		<cfloop query="#extensions#">
			<cfset existingExtensions[createId(extensions.provider,extensions.id)]=extensions.id>
		</cfloop>
		<cfset promotion={level:0}>
		
		<cfset request.adminType=url.adminType>
		<cfinclude template="extension.functions.cfm">
		<cfset data=loadAllProvidersData(50000,false)>
						
		<cfloop collection="#data#" item="provider" index="providerURL">
			<cfif not isSimpleValue(provider)>
				<cfset qry=provider.listApplications>
				<cfloop query="#provider.listApplications#">
					<cfset uid=createId(providerURL,qry.id)>
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




		<cfsavecontent variable="content" trim="true">
			<cfoutput>
				
				<!--- Core --->
				<cfif adminType == "server" and hasUpdate>
					<div class="error">
						<a href="server.cfm?action=services.update" style="color:red;text-decoration:none;">
						#replace( stText.services.update.update, { '{available}': avi, '{current}': curr } )#
						</a>
					</div>
				</cfif>
				
				<!--- Extension --->
				<cfif extensions.recordcount and len(ext)>
				<div class="error">
					<a href="#self#?action=extension.applications" style="color:red;text-decoration:none;">
						There are some updates available for your installed Extensions.<br>
						#ext#
					</a>
				</div>
				</cfif>
				
				<!--- Promotion<div class="normal"></div> --->
				<cfif promotion.level GT 0>
					
					<h3><a href="#promotion.uri#">#promotion.label#</a></h3>
					<cfif len(promotion.img)>
						<img src="#promotion.img#"  /><br>
					</cfif>
					<span class="comment">#promotion.txt#</span>
					
				</cfif>
				
				
			</cfoutput>
		</cfsavecontent>
		<cfset session[id].content=content>
		<cfset session[id].last=now()> 
	<cfelse>
		<cfset content=session[id].content>
	</cfif>

	<cfoutput>#content#</cfoutput>
	
	<cfcatch><cfrethrow>
		<cfoutput>
			<div class="error">
				Failed to retrieve update information:
				#cfcatch.message# #cfcatch.detail#
			</div>
		</cfoutput>
	</cfcatch>
</cftry>