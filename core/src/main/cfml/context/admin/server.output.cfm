<cfset error.message="">
<cfset error.detail="">

<cfadmin 
	action="getOutputSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="setting">

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
				
				<cfadmin 
					action="updateOutputSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					cfmlWriter="#form.cfmlWriter#"
					suppressContent="#isDefined('form.suppressContent') and form.suppressContent#"
					allowCompression="#isDefined('form.allowCompression') and form.allowCompression#"
					bufferOutput="#isDefined('form.bufferTagBodyOutput') and form.bufferTagBodyOutput#"
					contentLength=""
					remoteClients="#request.getRemoteClients()#">
		
			</cfcase>
		<!--- reset to server setting --->
			<cfcase value="#stText.Buttons.resetServerAdmin#">
				
				<cfadmin 
					action="updateOutputSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					cfmlWriter=""
					suppressContent=""
					showVersion=""
					allowCompression=""
					bufferOutput=""
					contentLength=""
					
					remoteClients="#request.getRemoteClients()#">
		
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
			<cfset error.cfcatch=cfcatch>
		</cfcatch>
	</cftry>
</cfif>

<!--- Error Output --->
<cfset printError(error)>
				
<!--- Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<cfif not hasAccess>
	<cfset noAccess(stText.setting.noAccess)>
</cfif>

<cfoutput>
	<div class="pageintro">
		#stText.setting[request.adminType]#
	</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- Suppress Whitespace --->
				<tr>
					<th scope="row">#stText.setting.whitespace#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="cfmlWriter" 
							value="#setting.cfmlWriter#"
							access="#hasAccess#"
							description=""
							br=false
							sp=false
							descOnTop=true>
						
						<cfset desc={
								'regular':stText.setting.cfmlWriterReg,
								'white-space':stText.setting.cfmlWriterWS,
								'white-space-pref':stText.setting.cfmlWriterWSPref
									}>
						
						<cfloop list="regular,white-space,white-space-pref" index="k">
							<input type="radio" class="checkbox" name="cfmlWriter" value="#k#" <cfif setting.cfmlWriter EQ k>checked="checked"</cfif>>
							<div class="comment">#desc[k]#</div><br> 
						</cfloop>
						</cfmodule>
					</td>
				</tr>
				<!--- Allow Compression --->
				<tr>
					<th scope="row">#stText.setting.AllowCompression#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="AllowCompression" 
							value="#setting.AllowCompression#"
							access="#hasAccess#"
							description="#stText.setting.AllowCompressionDescription#"
							br=false
							sp=true
							descOnTop=false>
							<input type="checkbox" name="AllowCompression" class="checkbox" value="true" <cfif setting.AllowCompression>checked="checked"</cfif>>
						</cfmodule>
					</td>
				</tr>

				<!--- Suppress Content when CFC Remoting --->
				<tr>
					<th scope="row">#stText.setting.suppressContent#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="suppressContent" 
							value="#setting.suppressContent#"
							access="#hasAccess#"
							description="#stText.setting.suppressContentDescription#"
							br=false
							sp=true
							descOnTop=false>
							<input type="checkbox" name="suppressContent" class="checkbox" value="true" <cfif setting.suppressContent>checked="checked"</cfif>>
						</cfmodule>
					</td>
				</tr>

				<!--- Buffer Output --->
				<tr>
					<th scope="row">#stText.setting.bufferOutput#</th>
					<td>
						<cfmodule template="systemSetting.cfm" 
							name="bufferTagBodyOutput" 
							value="#setting.bufferOutput#"
							access="#hasAccess#"
							description="#stText.setting.bufferOutputDescription#"
							br=false
							sp=true
							descOnTop=false>
							<input type="checkbox" name="bufferTagBodyOutput" class="checkbox" value="true" <cfif setting.bufferOutput>checked="checked"</cfif>>
						</cfmodule>
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
							<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif not request.singleMode and request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>