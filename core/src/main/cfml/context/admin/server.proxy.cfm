<cfset error.message="">
<cfset error.detail="">

<cfadmin
	action="getProxy"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="proxy">

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
<cfparam name="form.enabled" default="false">

<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.proxy.enable#">
			<cfadmin
				action="updateProxy"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				proxyEnabled="true"
				proxyServer="#proxy.server#"
				proxyPort="#proxy.port#"
				proxyUsername="#proxy.username#"
				proxyPassword="#proxy.password#"
				>

		</cfcase>
		<cfcase value="#stText.proxy.disable#">

			<cfadmin
				action="updateProxy"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				proxyEnabled="false"
				proxyServer=""
				proxyPort="80"
				proxyUsername=""
				proxyPassword=""
				>

		</cfcase>
		<cfcase value="#stText.Buttons.Update#">

			<cfadmin
				action="updateProxy"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				proxyEnabled="#form.enabled#"
				proxyServer="#form.server#"
				proxyPort="#form.port#"
				proxyUsername="#form.username#"
				proxyPassword="#form.password?:""#"	
				>

		</cfcase>
		<cfcase value="#stText.Buttons.Delete#">

			<cfadmin
				action="removeProxy"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				>

		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>


<!--- Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- Error Output --->
<cfset printError(error)>
<!--- Create Datasource --->

<cfscript>
	if (!proxy.keyExists("enabled"))
		proxy.enabled = false;
</cfscript>

<cfoutput>
<script>
var proxy={};
function doEnableProxy(enableProxy) {
	var form=enableProxy.form;
	var disable=!enableProxy.checked;
	var fields=['server','port','username','password'];

	for(var i=0;i<fields.length;i++) {
		var field=fields[i];
		// a locked secret (e.g. password set by env var) has no form field
		if(!form[field]) continue;
		form[field].disabled=disable;
		if(disable) {
			proxy[field]=form[field].value;
			form[field].value='';
		}
		else {
			if(form[field].value=='')
			form[field].value=proxy[field];
		}
	}
}
</script>
<div class="pageintro">#stText.proxy[request.adminType]#</div>
<h2>#stText.proxy.settings#</h2>
<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
<table class="maintbl" width="600">
<!--- Server --->
<tr>
	<th scope="row">#stText.proxy.serverName#</th>
	<td>
		<cfmodule template="systemSetting.cfm"
			name="server"
			propName="proxy_server"
			value="#proxy.server#"
			access="#hasAccess#"
			description="#stText.proxy.serverDescription#"
			descOnTop=true>
			<cfinputClassic type="text" name="server" value="#proxy.server#"
				style="width:200px" required="no">
		</cfmodule>
	</td>
</tr>

<!--- Port --->
<tr>
	<th scope="row">#stText.proxy.port#</th>
	<td>
		<cfmodule template="systemSetting.cfm"
			name="port"
			propName="proxy_port"
			value="#proxy.port#"
			access="#hasAccess#"
			description="#stText.proxy.portDescription#"
			descOnTop=true>
			<cfinputClassic type="text" name="port" value="#proxy.port#"
				style="width:50px" required="yes" message="#stText.proxy.missingPort#">
		</cfmodule>
	</td>
</tr>

<!--- Username --->
<tr>
	<th scope="row">#stText.proxy.username#</th>
	<td>
		<cfmodule template="systemSetting.cfm"
			name="username"
			propName="proxy_username"
			value="#proxy.username#"
			access="#hasAccess#"
			description="#stText.proxy.usernameDescription#"
			descOnTop=true>
			<cfinputClassic type="text" name="username" value="#proxy.username#"
				style="width:200px" required="no">
		</cfmodule>
	</td>
</tr>

<!--- Password --->
<tr>
	<th scope="row">#stText.proxy.password#</th>
	<td>
		<cfmodule template="systemSetting.cfm"
			name="password"
			propName="proxy_password"
			value="#proxy.password#"
			masked=true
			access="#hasAccess#"
			description="#stText.proxy.passwordDescription#"
			descOnTop=true>
			<cfinputClassic type="password" name="password" value="#proxy.password#"
				style="width:200px" required="no" passthrough='autocomplete="off"'>
		</cfmodule>
	</td>
</tr>

<!--- Enabled --->
<tr>
	<th scope="row">#stText.proxy.enable#</th>
	<td>
		<cfmodule template="systemSetting.cfm"
			name="enabled"
			propName="proxy_enabled"
			value="#proxy.enabled#"
			access="#hasAccess#">
			<cfinputClassic type="checkbox" name="enabled" value="true" checked="#proxy.enabled#">
		</cfmodule>
	</td>
</tr>

<cfif hasAccess>
	<tr>
		<td colspan="2">
			<input class="button submit" type="submit" name="mainAction" value="#stText.Buttons.Update#">
			<input class="button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
		</td>
	</tr>
</cfif>

	
</table>
</cfformClassic>

<br>
<b>java.net.useSystemProxies:</b> is set to #server.system.properties["java.net.useSystemProxies"]?: " (not set)"#
<br>
</cfoutput>