<!--- 
Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">
<cfparam name="stveritfymessages" default="#struct()#">
<cfparam name="url.action2" default="list">
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="mail"
	secValue="yes">
	
<cfadmin 
	action="getMailSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mail">
<cfadmin 
	action="getMailServers"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="ms">

<cfscript>
	variables.stars = "*********";
	function toPassword(host,pw, ms)
	{
		var i=1;
		if(arguments.pw EQ variables.stars)
		{
			for(i=arguments.ms.recordcount;i>0;i--)
			{
				if(arguments.host EQ arguments.ms.hostname[i])
					return arguments.ms.password[i];
			}
		}
		return arguments.pw;
	}
	function toTimeSpan(required string prefix){
		local.days=toArrayFromForm(prefix&"_days");
		local.hours=toArrayFromForm(prefix&"_hours");
		local.minutes=toArrayFromForm(prefix&"_minutes");
		local.seconds=toArrayFromForm(prefix&"_seconds");
		local.rtn=[];
		loop array=days index="local.i" item="local.day" {
			rtn[i]=createTimeSpan(day,hours[i],minutes[i],seconds[i]);
		}
		return rtn;
	}

	function toTSStruct(seconds){
		var data={};
		var day=60*60*24;
		var tmp=seconds/day;
		data.days=int(tmp);
		tmp=(tmp-data.days)*24;
		data.hours=int(tmp);
		tmp=(tmp-data.hours)*60;
		data.minutes=int(tmp);
		data.seconds=int((tmp-data.minutes)*60);
		return data;
	}

	function fill(nbr){
		if(len(nbr&"")<2) return "0"&nbr;
		return nbr;
	}
</cfscript>

<!--- ACTIONS --->
<cftry>
	<cfswitch expression="#form.mainAction#">
		<!--- Setting --->
		<cfcase value="#stText.Buttons.Setting#">
			<cfif form._mainAction EQ stText.Buttons.update>
				<cfadmin 
					action="updateMailSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					spoolEnable="#isDefined("form.spoolenable") and form.spoolenable#"
					timeout="#form.timeout#"
					defaultEncoding="#form.defaultEncoding#"
					remoteClients="#request.getRemoteClients()#">
			<cfelseif form._mainAction EQ stText.Buttons.resetServerAdmin>
				<!--- reset to server setting --->
				<cfadmin 
					action="updateMailSetting"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					spoolEnable=""
					timeout=""
					defaultEncoding=""
					
					remoteClients="#request.getRemoteClients()#">
			 </cfif>
		</cfcase>
		<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">
			<!--- update --->
			<cfif form.subAction EQ "#stText.Buttons.Update#">
							
				<cfset data.hosts=toArrayFromForm("hostname")>
				<cfset data.usernames=toArrayFromForm("username")>
				<cfset data.passwords=toArrayFromForm("password")>
				<cfset data.ports=toArrayFromForm("port")>
				<cfset data.tlss=toArrayFromForm("tls")>
				<cfset data.ssls=toArrayFromForm("ssl")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfset data.idles=toTimeSpan("idle")>
				<cfset data.lifes=toTimeSpan("life")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.hosts)#">
					<cfif isDefined("data.rows[#idx#]") and data.hosts[idx] NEQ "">
						<cfparam name="data.ports[idx]" default="25">
						<cfif trim(data.ports[idx]) EQ "">
							<cfset data.ports[idx]=25>
						</cfif>
						<cfset pw=toPassword(data.hosts[idx],session["password"&request.adminType], ms)>
						<cfadmin 
							action="updateMailServer"
							type="#request.adminType#"
							password="#pw#"
							hostname="#data.hosts[idx]#"
							dbusername="#data.usernames[idx]#"
							dbpassword="#toPassword(data.hosts[idx],data.passwords[idx], ms)#"
							life="#data.lifes[idx]#"
							idle="#data.idles[idx]#"
							

							port="#data.ports[idx]#"
							id="#isDefined("data.ids[#idx#]")?data.ids[idx]:''#"
							tls="#isDefined("data.tlss[#idx#]") and data.tlss[idx]#"
							ssl="#isDefined("data.ssls[#idx#]") and data.ssls[idx]#"
							remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			<!--- delete --->
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.hosts=toArrayFromForm("hostname")>
				<cfset data.usernames=toArrayFromForm("username")>
				<!---  @todo
				<cflock type="exclusive" scope="application" timeout="5"></cflock> --->
				<cfset len=arrayLen(data.hosts)>
				<cfloop index="idx" from="1" to="#len#">
					<cfif isDefined("data.rows[#idx#]") and data.hosts[idx] NEQ "">
						<cfadmin 
							action="removeMailServer"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							id="#isDefined("data.ids[#idx#]")?data.ids[idx]:''#"
							hostname="#data.hosts[idx]#"
							username="#data.usernames[idx]#"
							remoteClients="#request.getRemoteClients()#">
					</cfif>
				</cfloop>
			<cfelseif form.subAction EQ "#stText.Buttons.Verify#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.hosts=toArrayFromForm("hostName")>
				<cfset data.usernames=toArrayFromForm("username")>
				<cfset data.passwords=toArrayFromForm("password")>
				<cfset data.ports=toArrayFromForm("port")>
				<cfset doNotRedirect=true>
				<cfloop index="idx" from="1" to="#arrayLen(data.rows)#">
					<cfif isDefined("data.rows[#idx#]") and isDefined("data.hosts[#idx#]") and data.hosts[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyMailServer"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								hostname="#data.hosts[idx]#"
								port="#data.ports[idx]#"
								mailusername="#data.usernames[idx]#"
								mailpassword="#toPassword(data.hosts[idx],data.passwords[idx], ms)#">
							<cfset stVeritfyMessages[data.hosts[idx]].Label = "OK">
							<cfcatch>
								<cfset stVeritfyMessages[data.hosts[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.hosts[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
						<cfloop list="#request.adminType=='server'?'global':'global,local'#" index="contextType">
							<cfset stVeritfyMessages[data.hosts[idx]].contextType = contextType>
						</cfloop>
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
		<cfcase value="Send test mail">
			<cftry>
				<cfset data = queryRowData(ms,url.row)>
				<!--- <cfdump var="#data#" /><cfabort /> --->
				<cfmail from="#form.fromMail#" to="#form.toMail#" subject="Test email from Lucee" server="#data.hostname#" username="#data.username#" password="#data.password#" port="#data.port#" usetls="#data.tls#" usessl="#data.ssl#" async="false" debug="true">
					Hi this is a test email from your lucee server instance.
				</cfmail>
				<cfset stVeritfyMessages[data.hostname].Label = "OK">
				<cfset stVeritfyMessages[data.hostname].contextType = (request.adminType=='server'?'global':'global,local')>
				<cfset stVeritfyMessages[data.hostname].message = "Test mail has been sent successfully using [ #data.hostname# ].">
				<cfset doNotRedirect = true>
				<cfcatch type="any">
					<cfset stVeritfyMessages[data.hostname].Label = "Error">
					<cfset stVeritfyMessages[data.hostname].message = cfcatch.message>
					<cfset stVeritfyMessages[data.hostname].contextType = (request.adminType=='server'?'global':'global,local')>
				</cfcatch>
			</cftry>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>

<!--- Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and not isDefined('doNotRedirect')>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<!--- Error Output--->
<cfset printError(error)>



<cfif url.action2 EQ "edit">
	<cfinclude template="services.mail.edit.cfm">
<cfelseif url.action2 EQ "sendTestmail">
	<cfinclude template="services.mail.sendTestmail.cfm">
<cfelse>
	<cfinclude template="services.mail.list.cfm">
</cfif>
