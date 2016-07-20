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
					<cfif data.types[idx] EQ "cpp">
						<cfadmin 
							action="updateCPPCFX"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							name="#data.names[idx]#"
							procedure="#data.procedures[idx]#"
							serverlibrary="#data.serverlibraries[idx]#"
							keepalive="#isDefined('data.keepalives[idx]') and data.keepalives[idx]#"
							remoteClients="#request.getRemoteClients()#">
					<cfelse>
						<cfadmin 
							action="updateJavaCFX"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							name="#data.names[idx]#"
							class="#data.classes[idx]#"
							remoteClients="#request.getRemoteClients()#">
					</cfif>
					
					
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
		<cfadmin 
			action="getCPPCFXTags"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			returnVariable="ctags">

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
								<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.Verify#">
								<input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.save#">
								<input type="reset" class="bm reset" name="cancel" value="#stText.Buttons.Cancel#">
								<input type="submit" class="br button submit" name="subAction" value="#stText.Buttons.Delete#">
							</td>
						</tr>
					</tfoot>
				</cfif>
			</table>
		</cfformClassic>

		<cfif structKeyExists(session,'enable') and session.enable EQ "cfxcpp">
			<h2>#stText.CFX.cpp.CFXTags#</h2>
			<div class="warning nohighlight">
				The C++ CFX tags Implementation is currently in Beta State. Its functionality can change before it's final release.
				If you have any problems while using the C++ CFX tags Implementation, please post the bugs and errors in our <a href="https://bitbucket.org/lucee/lucee/issues" target="_blank" class="CheckError">bugtracking system</a>. 
			</div>
			<!------------------------------ C++ ------------------------------->
			<cfformClassic onerror="customError" name="cpp" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl checkboxtbl">
					<thead>
						<tr>
							<th width="3%"><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></th>
							<th>#stText.CFX.Name#</th>
							<th>#stText.CFX.serverlibrary#</th>
							<th>#stText.CFX.procedure#</th>
							<th>#stText.CFX.keepAlive#</th>
							<th width="3%">#stText.Settings.DBCheck#</th>
						</tr>
					</thead>
					<tbody>
						<cfloop query="ctags">
							<tr>
								<!--- read-only --->
								<td>
									<input type="hidden" name="type_#ctags.currentrow#" value="#ctags.displayname#">
									<cfif not ctags.readOnly>
										<input type="checkbox" class="checkbox" name="row_#ctags.currentrow#" 
										value="#ctags.currentrow#">
									</cfif>
								</td>
								<!--- name --->
								<td>
									<input type="hidden" name="name_#ctags.currentrow#" value="#ctags.name#">
									&lt;cfx_<b>#ctags.name#</b>&gt;
								</td>
								
								<cfset css=iif(not ctags.isvalid,de(' style="background-color:####E3D1D6"'),de(''))>
								<!--- serverlibrary --->
								<td class="tblContent<cfoutput>#css#</cfoutput>">
									<cfif not has.cfx_setting or ctags.readOnly>
										#ctags.serverlibrary#
									<cfelse>
										<cfinputClassic 
											onKeyDown="checkTheBox(this)" type="text" name="serverlibrary_#ctags.currentrow#" value="#ctags.serverlibrary#" 
											required="yes" class="xlarge" message="#stText.CFX.MissingClassValue##ctags.currentrow#)">
									</cfif>
								</td>
								<!--- procedure --->
								<td class="tblContent<cfoutput>#css#</cfoutput>">
									<cfif not has.cfx_setting or ctags.readOnly>
										#ctags.procedure#
									<cfelse>
										<cfinputClassic 
											onKeyDown="checkTheBox(this)" type="text" name="procedure_#ctags.currentrow#" value="#ctags.procedure#" 
											required="yes" class="xlarge" message="#stText.CFX.MissingClassValue##ctags.currentrow#)">
									</cfif>
								</td>
								<!--- keepAlive --->
								<td class="tblContent<cfoutput>#css#</cfoutput>">
									<cfif not has.cfx_setting or ctags.readOnly>
										#yesNoFormat(ctags.procedure)#
									<cfelse>
										<input type="checkbox" class="checkbox" onclick="checkTheBox(this)" name="keepalive_#ctags.currentrow#" value="true" <cfif ctags.keepAlive>checked</cfif>>
									</cfif>
								</td>
								<!--- check --->
								<td>
									<cfif StructKeyExists(stVeritfyMessages, ctags.name)>
										<cfif stVeritfyMessages[ctags.name].label eq "OK">
											<span class="CheckOk">#stVeritfyMessages[ctags.name].label#</span>
										<cfelse>
											<span class="CheckError" title="#stVeritfyMessages[ctags.name].message##Chr(13)#">#stVeritfyMessages[ctags.name].label#</span>
											&nbsp;<img src="resources/img/red-info.gif.cfm" 
												width="9" 
												height="9" 
												border="0" 
												title="#stVeritfyMessages[ctags.name].message##Chr(13)#">
										</cfif>
									<cfelse>
										&nbsp;				
									</cfif>
								</td>
							</tr>
						</cfloop>
						
						<cfset idx=ctags.recordcount+1>
						<cfif has.cfx_setting>
							<tr>
								<td>
									<input type="checkbox" class="checkbox" name="row_#idx#" value="#idx#">
								</td>
								<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
									name="name_#idx#" value="" required="no" class="xlarge">
								</td>
								<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
									name="serverlibrary_#idx#" value="" required="no" class="xlarge">
								</td>
								<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
									name="procedure_#idx#" value="ProcessTagRequest" required="no" class="xlarge">
								</td>
								<td>
									<input type="checkbox" class="checkbox" onclick="checkTheBox(this)" name="keepalive_#idx#" value="true">
								</td>
								<td></td>
							</tr>
							<tr>
								<td></td>
								<td align="center" colspan="5">
									<cfif server.os.archModel NEQ server.java.archModel>
										<cfset archText=stText.CFX.cpp.archDiff>	
									<cfelse>
										<cfset archText=stText.CFX.cpp.arch>	
									</cfif>
									<cfset archText=replace(archText,"{os-arch}",server.os.archModel,"all")>
									<cfset archText=replace(archText,"{jre-arch}",server.java.archModel,"all")>	
									<div class="comment"  style="color:red">#archText#</div>
								</td>
							</tr>
						</cfif>
						<cfif has.cfx_setting>
							<cfmodule template="remoteclients.cfm" colspan="8" line>
						</cfif>
					</tbody>
					<cfif has.cfx_setting>
						<tfoot>
							<tr>
								<td colspan="6">
									<input type="hidden" name="type_#idx#" value="cpp">
									<input type="hidden" name="mainAction" value="updateJava">
									<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.Verify#">
									<input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.save#">
									<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
									<input type="submit" class="br button submit" name="subAction" value="#stText.Buttons.Delete#">
								 </td>
							</tr>
						</tfoot>
					</cfif>
				</table>
			</cfformClassic>
		</cfif>
	</cfif>
</cfoutput>