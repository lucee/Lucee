<cfscript>
// load available mail server templates
variables.drivers={};
driverNames=structnew("linked");
driverNames=ComponentListPackageAsStruct("lucee-server.admin.mailservers",driverNames);
driverNames=ComponentListPackageAsStruct("lucee.admin.mailservers",driverNames);
driverNames=ComponentListPackageAsStruct("mailservers",driverNames);

loop struct=driverNames index="name" item="componentPath" {
	if(name == 'MailServer') continue;
	drivers[name]=createObject("component",componentPath);
}
</cfscript>

<cfoutput>


<cfhtmlbody>

<script type="text/javascript">
	active={};
	var bodies={};
	function enable(btn,type,id){
		var old=active[type];
		if(old==id) return;
		active[type]=id;

		$(document).ready(function(){
				$(btn).css('background-color','#request.adminType=="web"?'##0f75a8':'##c00'#');
				$(btn).css('color','white');
				$('##button_'+old).css('background-color','');
				bodies[old]=$('##div_'+old).detach();
				bodies[id].appendTo("##group_Connection");
		});
	}
</script>

</cfhtmlbody>


<!--- NEW Server --->

		<cfif hasAccess>
			<cfset count=0>
			<cfset len=structCount(drivers)>
			<cfset _DefaultName = "Other">
			<cfset _DefaultDriver = drivers["Other"]>
			<cfset hiddenFormContents = "">

			<!--- Some common functionalities --->
			<cfset data.life=toTSStruct(data.life)>
			<cfset data.idle=toTSStruct(data.idle)>

			<cfset driverNames = structKeyArray(drivers)>
			<cfset arraySort(driverNames,'textnocase')>

			<!--- if there is an "Other" we put it to the end --->
			<cfif arrayDelete(driverNames,'Other')>
				<cfset arrayAppend(driverNames,'Other')>
			</cfif>
			<cfset len=driverNames.len()>
			<cfloop array="#driverNames#" item="driverClass">
				<cfif isNull(drivers[driverClass])>
					<cfcontinue>
				</cfif>
				<cfset driver = drivers[driverClass]>
				<cfset _name = driver.getShortName()>
				<cfset _driver = drivers[driverClass]>
				<cfset count++>
				<cfset orientation="bm">
				<cfif count==1><cfset orientation="bl"></cfif>
				<cfif count==len><cfset orientation="br"></cfif>

				<cfset id="Connection_#hash(driver.getLabel(),'quick')#">
				<cfset isActiveEdit = data.hostName NEQ "" AND driver.getHost() EQ data.hostName AND driver.getPort() EQ data.port AND driver.useTLS() EQ data.tls AND driver.useSSL() EQ data.ssl>
				<cfset active=(driver.getLabel() EQ _DefaultDriver.getLabel() AND data.hostName EQ "") OR isActiveEdit>
				<cfif active>
					<cfset hasActive = true>
				</cfif>
				<cfif driverClass EQ "Other" AND !isDefined("hasActive")>
					<cfset active=true>
				</cfif>
				<cfif active>
					<cfset _DefaultName = driverClass>
					<cfset _DefaultDriver = drivers[driverClass]>
				</cfif>

				<input id="button_#id#" onclick="enable(this,'group_Connection','#id#');"
					type="button"
					class="#orientation# button"
					name="changeConnection"
					<cfif active> style="color:white;background-color:#(request.adminType=="web"?'##0f75a8':'##c00')#;"</cfif>
					value="#_name#">
				<cfsavecontent variable="tmpContent">
					<div id="div_#id#">
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="id_#ms.recordcount+1#" value="#data.id?:-1#">
			<input type="hidden" name="row_#ms.recordcount+1#" value="true" >

			<table class="maintbl">
				<tbody>
					<!--- host --->
					<cfif driverClass EQ "Other">
					<tr>
						<th scope="row">#stText.Mail.Server#</th>
						<td>
							<cfinputClassic type="text" name="hostName_#ms.recordcount+1#" value="#data.hostName#" required="yes" class="large" message="#stText.Mail.hostnameMissing#">
							<div class="comment">#stText.mail.serverDesc#</div>
						</td>
					</tr>
					<cfelse>
						<cfinputClassic type="hidden" name="hostName_#ms.recordcount+1#" value="#driver.getHost()#" required="yes" class="large" message="#stText.Mail.hostnameMissing#">
					</cfif>

					<!--- Port --->
					<cfif driverClass EQ "Other">
					<tr>
						<th scope="row">#stText.Mail.port#</th>
						<td>
							<cfinputClassic type="text" name="port_#ms.recordcount+1#" value="#data.port#" required="yes"
							validate="integer"
							message="#stText.Mail.PortErrorFirst#">
							<div class="comment">#stText.mail.portDesc#</div>
						</td>
					</tr>
					<cfelse>
						<cfinputClassic type="hidden" name="port_#ms.recordcount+1#" value="#driver.getPort()#" required="yes"validate="integer" message="#stText.Mail.PortErrorFirst#">
					</cfif>

					<!--- Username ---->
					<tr>
						<th scope="row">#stText.Mail.Username#</th>
						<td>
							<cfinputClassic type="text" name="username_#ms.recordcount+1#" value="#data.username#" required="no" class="large"
							message="#stText.Mail.UserNameMissing#">
							<div class="comment">#stText.mail.usernameDesc#</div>
						</td>
					</tr>
					<!--- Password --->
					<tr>
						<th scope="row">#stText.Mail.password#</th>
						<td>
							<cfinputClassic type="password" name="password_#ms.recordcount+1#" value="#isNull(data.password)?"":"*********"#" required="no" class="large"
							message="#stText.Mail.PasswordMissing#">
							<div class="comment">#stText.mail.passwordDesc#</div>
						</td>
					</tr>

					<!--- TLS --->
					<cfif driverClass EQ "Other">
					<tr>
						<th scope="row">#stText.Mail.tls#</th>
						<td>
							<cfinputClassic class="checkbox" type="checkbox" checked="#!isNull(data.tls) && data.tls#" name="tls_#ms.recordcount+1#"  value="true">
							<div class="comment">#stText.mail.tlsDesc#</div>
						</td>
					</tr>
					<cfelse>
						<cfinputClassic class="checkbox" type="checkbox" checked="#driver.useTLS()#" name="tls_#ms.recordcount+1#" value="true" style="display: none;">
					</cfif>

					<!--- SSL --->
					<cfif driverClass EQ "Other">
					<tr>
						<th scope="row">#stText.Mail.ssl#</th>
						<td>
							<cfinputClassic class="checkbox" type="checkbox" checked="#!isNull(data.ssl) && data.ssl#" name="ssl_#ms.recordcount+1#" value="true">
							<div class="comment">#stText.mail.sslDesc#</div>
						</td>
					</tr>
					<cfelse>
						<cfinputClassic class="checkbox" type="checkbox" checked="#driver.useSSL()#" name="ssl_#ms.recordcount+1#" value="true" style="display: none;">
					</cfif>

					<!--- Life Timespan --->
					<tr>
						<th scope="row">#stText.Mail.life#</th>
						<td>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>

									<tr>
										<td><cfinputClassic type="text" name="life_days_#ms.recordcount+1#" value="#data.life.days#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="life_hours_#ms.recordcount+1#" value="#data.life.hours#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="life_minutes_#ms.recordcount+1#" value="#data.life.minutes#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="life_seconds_#ms.recordcount+1#" value="#data.life.seconds#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>

							</tbody>

						</table>
						<div class="comment">#stText.Mail.lifeDesc#</div>

						</td>
					</tr>

					<!--- Idle Timespan --->
					<tr>
						<th scope="row">#stText.Mail.idle#</th>
						<td>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>

									<tr>
										<td><cfinputClassic type="text" name="idle_days_#ms.recordcount+1#" value="#data.idle.days#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutDaysValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="idle_hours_#ms.recordcount+1#" value="#data.idle.hours#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutHoursValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="idle_minutes_#ms.recordcount+1#" value="#data.idle.minutes#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutMinutesValue#request#stText.Scopes.TimeoutEndValue#"></td>
										<td><cfinputClassic type="text" name="idle_seconds_#ms.recordcount+1#" value="#data.idle.seconds#"
											class="number" required="yes" validate="integer"
											message="#stText.Scopes.TimeoutSecondsValue#request#stText.Scopes.TimeoutEndValue#"></td>
									</tr>

							</tbody>

						</table>
						<div class="comment">#stText.Mail.idleDesc#</div>
						</td>
					</tr>

				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="hidden" name="mainAction" value="#stText.Buttons.update#">
							<input type="hidden" name="subAction" value="#stText.Buttons.update#">
							<input type="submit" class="bs button submit" name="sdasd" value="#stText.Buttons.save#" />
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
					</div>
				</cfsavecontent>
				<cfset hiddenFormContents &= tmpContent>
			</cfloop>
			<div id="group_Connection">
				#hiddenFormContents#
			</div>


			<cfhtmlbody>

			<script>
				<cfloop collection="#drivers#" index="driverClass" item="driver">
					<cfset _name = driver.getShortName()>
					<cfif isNull(drivers[driverClass])>
						<cfcontinue>
					</cfif>
					<cfset _driver = drivers[driverClass]>
					<cfset id="Connection_#hash(driver.getLabel(),'quick')#">
					<cfset active = driver.getLabel() EQ _DefaultDriver.getLabel()>
					<cfif !active>
						$(document).ready(function(){
							bodies['#id#']=$('##div_#id#').detach();
						});
					<cfelse>
						active['group_Connection']='#id#';
					</cfif>
				</cfloop>
			</script>

			</cfhtmlbody>

	</cfif>
<cfif url.action2 EQ "edit">
<cfsavecontent variable="codeSample">
	this.mailservers =[ {
	  host: '#data.hostname#'
	, port: #data.port#
	, username: '#replace(data.username,"'","''","all")#'
	, password:  <span style="overflow-wrap: break-word;">'#data.passwordEncrypted?:''#'</span>
	, ssl: #data.ssl?:false#
	, tls: #data.tls?:false#<cfif
	!isNull(data.life)>
	, lifeTimespan: createTimeSpan(#data.life.days#,#data.life.hours#,#data.life.minutes#,#data.life.seconds#)</cfif><cfif
	!isNull(data.idle)>
	, idleTimespan: createTimeSpan(#data.idle.days#,#data.idle.hours#,#data.idle.minutes#,#data.idle.seconds#)</cfif>
}];
</cfsavecontent>
<cfset renderCodingTip( codeSample, "", true )>
</cfif>






</cfoutput>