<!--- <cfset classConfig=createObject("java","lucee.runtime.config.ConfigWeb")>
<cfset STRICT=classConfig.SCOPE_STRICT>
<cfset SMALL=classConfig.SCOPE_SMALL>
<cfset STANDART=classConfig.SCOPE_STANDART> --->
<cfset error.message="">
<cfset error.detail="">
<!--- <cfset hasAccess=securityManager.getAccess("setting") EQ ACCESS.YES>

<cfset hasAccess=securityManagerGet("setting","yes")> --->


<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfif hasAccess>
	<cftry>
		<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
		
			<cfcase value="#stText.Buttons.Update#">
				<cfif form.locale EQ "other">
					<cfset form.locale=form.locale_other>
				</cfif>
				
				<cfadmin 
					action="updateRegional"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					timezone="#form.timezone#"
					locale="#form.locale#"
					remoteClients="#request.getRemoteClients()#"
					>
			
			</cfcase>
			<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				<cfif form.locale EQ "other">
					<cfset form.locale=form.locale_other>
				</cfif>
				
				<cfadmin 
					action="updateRegional"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					timezone=""
					locale=""
					remoteClients="#request.getRemoteClients()#"
					>
			
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
		</cfcatch>
	</cftry>
</cfif>

<cfadmin 
	action="getLocales"
	locale="#stText.locale#"
	returnVariable="locales">
	
<cfadmin 
	action="getTimeZones"
	locale="#stText.locale#"
	returnVariable="timezones">
<cfadmin 
	action="getRegional"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="regional">

<cfquery name="timezones" dbtype="query">
	select * from timezones order by id,display
</cfquery>


<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>
<!--- 
Create Datasource --->

<cfoutput>
	<cfif not hasAccess>
		<cfset noAccess(stText.setting.noAccess)>
	</cfif>

	<div class="pageintro">
		<cfif request.adminType EQ "server">
			#stText.Regional.Server#
		<cfelse>
			#stText.Regional.Web#
		</cfif>
	</div>
	
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<!---
		replaced with encoding output
		<tr>
			<th scope="row">#stText.Regional.DefaultEncoding#</th>
			<td>
				<span class="comment">#stText.Regional.DefaultEncodingDescription#</span>
				<cfif hasAccess>
				<cfinputClassic type="text" name="defaultencoding" value="#regional.defaultEncoding#" 
					style="width:200px" required="yes" message="#stText.regional.missingEncoding#">
				
				<cfelse>
					<input type="hidden" name="defaultencoding" value="#regional.defaultEncoding#">
				
					<b>#regional.defaultEncoding#</b>
				</cfif>
			</td>
		</tr>
		--->
		<table class="maintbl">
			<tbody>
				<!--- Locale --->
				<tr>
					<th scope="row">#stText.Regional.Locale#</th>
					<td>
						<cfif hasAccess>
							<cfset hasFound=false>
							<cfset keys=structSort(locales,'textnocase')>
							<select name="locale" class="large">
								<option selected value=""> --- #stText.Regional.ServerProp[request.adminType]# --- </option>
								 ---><cfloop collection="#keys#" item="i"><cfset key=keys[i]><option value="#key#" <cfif key EQ regional.locale>selected<cfset hasFound=true></cfif>>#locales[key]#</option><!--- 
								 ---></cfloop>
							</select>
							<!--- <input type="text" name="locale_other" value="<cfif not hasFound>#regional.locale#</cfif>" style="width:200px"> --->
						<cfelse>
							<b>#regional.locale#</b>
						</cfif>
						<div class="comment">#stText.Regional.LocaleDescription#</div>

						<cfsavecontent variable="codeSample">
							this.locale = "#regional.locale#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Regional.TimeZone#</th>
					<td>
						<cfif hasAccess>
							<select name="timezone" class="large">
								<option selected value=""> --- #stText.Regional.ServerProp[request.adminType]# --- </option>
								<cfoutput query="timezones">
									<option value="#timezones.id#"
									<cfif timezones.id EQ regional.timezone>selected</cfif>>
									#timezones.id# - #timezones.display#</option>
								</cfoutput>
							</select>
						<cfelse>
							<b>#regional.timezone#</b>
						</cfif>
						<!--- <cfinputClassic type="text" name="timezone" value="#config.timezone.getId()#" style="width:200px" required="yes" message="Missing value for timezone"> --->
						<div class="comment">#stText.Regional.TimeZoneDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.timezone = "#regional.timezone#";
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input class="bl button submit" type="submit" name="mainAction" value="#stText.Buttons.Update#">
							<input class="br button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

</cfoutput>
