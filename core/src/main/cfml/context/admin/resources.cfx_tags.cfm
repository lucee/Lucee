<!--- <cfif isDefined("form")>
	<cfinclude template="act/resources.act_mapping.cfm">
</cfif> --->


<cfset error.message="">
<cfset error.detail="">
<cfparam name="stveritfymessages" default="#struct()#">

<!--- 
Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="has.cfx_setting"
	secType="cfx_setting"
	secValue="yes">
	
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="has.cfx_usage"
	secType="cfx_usage"
	secValue="yes">


<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="updateJava">
				<cfset data.classes=toArrayFromForm("class")>
				<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfset data.procedures=toArrayFromForm("procedure")>
				<cfset data.serverlibraries=toArrayFromForm("serverlibrary")>
				<cfset data.keepalives=toArrayFromForm("keepalive")>
				<cfset data.types=toArrayFromForm("type")>
				
				
		<!--- update --->
			<cfif form.subAction EQ "#stText.Buttons.save#">
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cfadmin 
							action="updateJavaCFX"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							name="#data.names[idx]#"
							class="#data.classes[idx]#"
							remoteClients="#request.getRemoteClients()#">
						</cfif>
				</cfloop>
		<!--- verify --->
			<cfelseif form.subAction EQ "#stText.Buttons.verify#">
				<cfset noRedirect=true>
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyCFX"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								
								name="#data.names[idx]#">
								<cfset stVeritfyMessages[data.names[idx]].Label = "OK">
							<cfcatch>
								<cfset stVeritfyMessages[data.names[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.names[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
				
				
		<!--- delete --->
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
					<cfadmin 
						action="removeCFX"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						name="#data.names[idx]#"
			remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>


<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and not isDefined('noRedirect')>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<cfoutput>
	<!--- Error Output--->
	<cfif error.message NEQ "">
		<div class="error">
			#error.message#<br>
			#error.detail#
		</div>
	</cfif>
	<cfif not has.cfx_usage>
		<cfset noAccess(stText.CFX.NoAccessUsage)>
	<cfelse>
		<cfif not has.cfx_setting><cfset noAccess(stText.CFX.NoAccessSetting)></cfif>
		
		<cfadmin 
			action="getJavaCFXTags"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="jtags">

		<!------------------------------ JAVA ------------------------------->
		<h2>#stText.CFX.CFXTags#</h2>
		<cfformClassic onerror="customError" name="java" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%"><cfif has.cfx_setting><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></cfif></th>
						<th>#stText.CFX.Name#</th>
						<th>#stText.CFX.Class#</th>
						<th width="3%">#stText.Settings.DBCheck#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="jtags">
						<tr>
							<td>
								<input type="hidden" name="type_#jtags.currentrow#" value="#jtags.displayname#">
								<cfif not jtags.readOnly>
									<input type="checkbox" class="checkbox" name="row_#jtags.currentrow#" value="#jtags.currentrow#">
								</cfif>
							</td>
							<td>
								<input type="hidden" 
									name="name_#jtags.currentrow#" value="#jtags.name#">
								&lt;cfx_<b>#jtags.name#</b>&gt;
							</td>
							<cfset css=iif(not jtags.isvalid,de(' style="background-color:####E3D1D6"'),de(''))>
							
							<td class="tblContent<cfoutput>#css#</cfoutput>">
								<cfif not has.cfx_setting or jtags.readOnly>
									#jtags.class#
								<cfelse>
									<cfinputClassic 
										onKeyDown="checkTheBox(this)" type="text" name="class_#jtags.currentrow#" value="#jtags.class#" 
										required="yes" class="xlarge" message="#stText.CFX.MissingClassValue##jtags.currentrow#)">
								</cfif>
							</td>
							<!--- check --->
							<td>
								<cfif StructKeyExists(stVeritfyMessages, jtags.name)>
									<cfif stVeritfyMessages[jtags.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[jtags.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[jtags.name].message##Chr(13)#">#stVeritfyMessages[jtags.name].label#</span>
										&nbsp;<img src="resources/img/red-info.gif.cfm" 
											width="9" 
											height="9" 
											border="0" 
											title="#stVeritfyMessages[jtags.name].message##Chr(13)#">
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
						</tr>
					</cfloop>
					<cfset idx=jtags.recordcount+1>
					<cfif has.cfx_setting>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#idx#" value="#idx#">
							</td>
							<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
								name="name_#idx#" value="" required="no" class="xlarge">
							</td>
							<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
								name="class_#idx#" value="" required="no" class="xlarge">
							</td>
							<td></td>
						</tr>
					</cfif>
					<cfif has.cfx_setting>
						<cfmodule template="remoteclients.cfm" colspan="4">
					</cfif>
				</tbody>
				<cfif has.cfx_setting>
					<tfoot>
						<tr>
							<td colspan="4">
								<input type="hidden" name="type_#idx#" value="java">
								<input type="hidden" name="mainAction" value="updateJava">
								<input type="submit" class="bl button submit enablebutton" name="subAction" value="#stText.Buttons.Verify#">
								<input type="submit" class="bm button submit enablebutton" name="subAction" value="#stText.Buttons.save#">
								<input type="reset" class="bm reset enablebutton" name="cancel" id="clickCancel" value="#stText.Buttons.Cancel#">
								<input type="submit" class="br button submit enablebutton" name="subAction" value="#stText.Buttons.Delete#">
							</td>
						</tr>
					</tfoot>
				</cfif>
			</table>
		</cfformClassic>
	</cfif>
</cfoutput>