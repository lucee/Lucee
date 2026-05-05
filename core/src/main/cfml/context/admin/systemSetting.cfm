<cfif (thisTag.executionMode == "end" or !thisTag.hasEndTag)>
<cfscript>
	if(isNull(application.systemPropOrEnvVarInfo)) {
		application.systemPropOrEnvVarInfo=GetSystemPropOrEnvVarInfo();
	}
	data=application.systemPropOrEnvVarInfo[attributes.name];
	renderCodingTip=caller.renderCodingTip;
	formatForConsole=caller.formatForConsole;
	stText=caller.stText;

	// values for example renderings
	settingsVal=attributes.value;
	codeTip=attributes.value;
	meta=getMetaData(attributes.value);
	isTimeSpan=FindNoCase("TimeSpan", meta.name);
	if(isTimeSpan) {
		settingsVal="#attributes.value.day#,#attributes.value.hour#,#attributes.value.minute#,#attributes.value.second#";
		codeTip="createTimeSpan(#attributes.value.day#,#attributes.value.hour#,#attributes.value.minute#,#attributes.value.second#)";
	}
	else {
		res=formatForConsole(codeTip);
		codeTip=res.ev;
	}


	if(left(data.systemProperties[1],6) == "lucee.") {
		codeTip="this.#mid(data.systemProperties[1],7)# = "&codeTip&";"
	}
	else {
		codeTip="this.#data.systemProperties[1]# = "&codeTip&";"
	}

	
	if(structKeyExists(attributes,"codeTip")) {
		codeTip=attributes.codeTip;
	}


	
	
	// has env var?
	has={"env":false,"prop":false};
	loop array=data.environmentVariables item="k" {
		if(structKeyExists(server.system.environment, k)) {
			has.env=true;
			break;
		}
	}
	loop array=data.systemProperties item="k" {
		if(structKeyExists(server.system.properties, k)) {
			has.prop=true;
			break;
		}
	}
	has.one=has.env or has.prop or not attributes.access?:true;
	content=thisTag.generatedContent;
	thisTag.generatedContent ="";


	stText.evsp.hasEV="this variable cannot be modified here, because it is set as an environment variable on this server.
	To modify the value, you will need to remove the environment variable on the server and then restart the server.";
	stText.evsp.hasSP="this variable cannot be modified here, because it is set as an system property on this server.
	To modify the value, you will need to remove the system property on the server and then restart the server.";
	stText.evsp.access="this variable cannot be modified here, because you not have the necessary permissions.";

	if(has.env) {
		info=stText.evsp.hasEV;
	} else if(has.prop) {
		info=stText.evsp.hasSP;
	} else {
		info=stText.evsp.access;
	}
	txt=trim(isEmpty(trim(attributes.description?:""))?data.description:attributes.description);
</cfscript>
<cfoutput>
!!!
	<cfif attributes.descOnTop?:false and len(txt)>
		#txt#<br><br>
	</cfif>	
	<cfif has.one>
		
		<cfset thisTag.generatedContent ="">
		<div>
			<label>
				<table class="optionslist" >
					<tr><td valign="middle">


				<cfif isTimeSpan>
					
						<table class="maintbl" style="border: revert;width:auto">
						<thead>
							<tr>
								<th>#stText.General.Days#</td>
								<th>#stText.General.Hours#</td>
								<th>#stText.General.Minutes#</td>
								<th>#stText.General.Seconds#</td>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td align="center"><b>#attributes.value.day#</b></td>
								<td align="center"><b>#attributes.value.hour#</b></td>
								<td align="center"><b>#attributes.value.minute#</b></td>
								<td align="center"><b>#attributes.value.second#</b></td>
							</tr>
						</tbody>
						</table>

				<input type="hidden" name="#attributes.name#_days" value="#attributes.value.day#">
				<input type="hidden" name="#attributes.name#_hours" value="#attributes.value.hour#">
				<input type="hidden" name="#attributes.name#_minutes" value="#attributes.value.minute#">
				<input type="hidden" name="#attributes.name#_seconds" value="#attributes.value.second#">
			<cfelseif isNumeric(attributes.value)>
				<table  class="maintbl">
				<tbody>
					<tr>
						<td align="center"><b>#attributes.value#</b></td>
					</tr>
				</tbody>
				</table>
				<input type="hidden" name="#attributes.name#" value="#attributes.value#">
			<cfelseif isBoolean(attributes.value)>
				<table  class="maintbl">
				<tbody>
					<tr>
						<td align="center"><b>#attributes.value?"&##9989;":"&##10060;"# #yesNoFormat(attributes.value)#</b></td>
					</tr>
				</tbody>
				</table>
				<input type="hidden" name="#attributes.name#" value="#attributes.value#">
			<cfelse>
				<table  class="maintbl">
				<tbody>
					<tr>
						<td align="center"><b>#attributes.value#</b></td>
					</tr>
				</tbody>
				</table>
				<input type="hidden" name="#attributes.name#" value="#attributes.value#">
			</cfif>

									</td>
						<td style="vertical-align: middle;">
							<span class="locked-indicator" data-tooltip="#info#">&##128274;</span>
						</td>
				</tr>
				</table>


			</label>
			
		</div>
	<cfelse>
		#content#
	</cfif>
	<cfif not has.one and (attributes.br?:false)><br></cfif>
	<cfif not (attributes.descOnTop?:false) and len(txt)>
		#isEmpty(trim(attributes.description?:""))?data.description:attributes.description#<br><br>
	</cfif>

	<cfif attributes.sp?:true>
		<cfset renderCodingTip( codeTip, attributes.codeTipDesc?:"")>
	</cfif>
	<cfset caller.renderSettings( attributes.name,settingsVal)>

</cfoutput>

	

</cfif>
