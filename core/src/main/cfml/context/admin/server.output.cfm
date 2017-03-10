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
					bufferOutput="#isDefined('form.bufferOutput') and form.bufferOutput#"
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
						<cfset desc={
								'regular':stText.setting.cfmlWriterReg,
								'white-space':stText.setting.cfmlWriterWS,
								'white-space-pref':stText.setting.cfmlWriterWSPref
									}>
						
						<cfif hasAccess>
							<cfloop list="regular,white-space,white-space-pref" index="k">
								<input type="radio" class="checkbox" name="cfmlWriter" value="#k#" <cfif setting.cfmlWriter EQ k>checked="checked"</cfif>>
								<div class="comment">#desc[k]#</div><br> 
							</cfloop>
						<cfelse>
							<b>#desc[setting.cfmlWriter]#
							<input type="hidden" name="cfmlWriter" value="#setting.cfmlWriter#">
						</cfif>
					</td>
				</tr>
				<!--- Allow Compression --->
				<tr>
					<th scope="row">#stText.setting.AllowCompression#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" name="AllowCompression" class="checkbox" value="true" <cfif setting.AllowCompression>checked="checked"</cfif>>
						<cfelse>
							<b>#iif(setting.AllowCompression,de('Yes'),de('No'))#</b>
							<!---<input type="hidden" name="AllowCompression" value="#setting.AllowCompression#">--->
						</cfif>
						<div class="comment">#stText.setting.AllowCompressionDescription#</div>
						

						<cfsavecontent variable="codeSample">
							this.compression = #setting.AllowCompression#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>

				<!--- Suppress Content when CFC Remoting --->
				<tr>
					<th scope="row">#stText.setting.suppressContent#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" name="suppressContent" class="checkbox" value="true" <cfif setting.suppressContent>checked="checked"</cfif>>
						<cfelse>
							<b>#iif(setting.suppressContent,de('Yes'),de('No'))#</b>
							<!---<input type="hidden" name="suppressContent" value="#setting.suppressContent#">--->
						</cfif>
						<div class="comment">#stText.setting.suppressContentDescription#</div>
						
						<cfsavecontent variable="codeSample">
							this.suppressRemoteComponentContent = #setting.suppressContent#;
						</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
					</td>
				</tr>

				
			</tbody>
		</table>

		<h3>#stText.general.dialect.cfml#</h3>
		<div class="itemintro">#stText.general.dialect.cfmlDesc#</div>
		
		<table class="maintbl">
			<tbody>

				<!--- Buffer Output --->
				<tr>
					<th scope="row">#stText.setting.bufferOutput#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" name="bufferOutput" class="checkbox" value="true" <cfif setting.bufferOutput>checked="checked"</cfif>>
						<cfelse>
							<b>#iif(setting.bufferOutput,de('Yes'),de('No'))#</b>
							<!---<input type="hidden" name="suppressContent" value="#setting.suppressContent#">--->
						</cfif>
						<div class="comment">#stText.setting.bufferOutputDescription#</div>


						<cfsavecontent variable="codeSample">
							this.bufferOutput = #setting.bufferOutput#;
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
							<input class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
</cfoutput>