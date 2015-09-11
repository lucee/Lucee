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
				
				proxyEnabled="#proxy.enabled#"
				proxyServer="#form.server#"
				proxyPort="#form.port#"
				proxyUsername="#form.username#"
				proxyPassword="#form.password#"
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
	</cfcatch>
</cftry>


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
<script>
var proxy={};
function doEnableProxy(enableProxy) {
	var form=enableProxy.form;
	var disable=!enableProxy.checked;
	var fields=['server','port','username','password'];
	
	for(var i=0;i<fields.length;i++) {
		var field=fields[i];
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



<table class="tbl" width="600">

<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">

<tr>
	<td colspan="2">#stText.proxy[request.adminType]#</td>
</tr>
<tr>
	<td colspan="2"><cfmodule template="tp.cfm"  width="1" height="6"></td>
</tr>
<tr>
	<td colspan="2">
		
		<input class="button submit" type="submit" name="mainAction" value="#iif(proxy.enabled,de(stText.proxy.disable),de(stText.proxy.enable))#">
	
	</td>
</tr>
	
<tr>
	<td colspan="2"><cfmodule template="tp.cfm"  width="1" height="10"></td>
</tr>
<cfif proxy.enabled>

<tr>
	<td colspan="2"><h2>#stText.proxy.settings#</h2></td>
</tr>
<!--- Server --->
<tr>
	<th scope="row">#stText.proxy.server#</th>
	<td>
		<div class="comment">#stText.proxy.serverDescription#</div><br />
		<cfif hasAccess>
		<cfinput type="text" name="server" value="#proxy.server#" 
			style="width:200px" required="no">
		
		<cfelse>
			<input type="hidden" name="server" value="#proxy.server#">
		
			<b>#proxy.server#</b>
		</cfif>
	</td>
</tr>

<!--- Port --->
<tr>
	<th scope="row">#stText.proxy.port#</th>
	<td>
		<div class="comment">#stText.proxy.portDescription#</div><br />
		<cfif hasAccess>
		<cfinput type="text" name="port" value="#proxy.port#" 
			style="width:50px" required="yes" message="#stText.proxy.missingPort#">
		
		<cfelse>
			<input type="hidden" name="port" value="#proxy.port#">
		
			<b>#proxy.port#</b>
		</cfif>
	</td>
</tr>

<!--- Username --->
<tr>
	<th scope="row">#stText.proxy.username#</th>
	<td>
		<div class="comment">#stText.proxy.usernameDescription#</div><br />
		<cfif hasAccess>
		<cfinput type="text" name="username" value="#proxy.username#" 
			style="width:200px" required="no">
		
		<cfelse>
			<input type="hidden" name="username" value="#proxy.username#">
		
			<b>#proxy.username#</b>
		</cfif>
	</td>
</tr>

<!--- Password --->
<tr>
	<th scope="row">#stText.proxy.password#</th>
	<td>
		<div class="comment">#stText.proxy.passwordDescription#</div><br />
		<cfif hasAccess>
		<cfinput type="password" name="password" value="#proxy.password#" 
			style="width:200px" required="no" passthrough='autocomplete="off"'>
		
		<cfelse>
			<input type="hidden" name="password" value="#proxy.password#">
		
			<b>#proxy.password#</b>
		</cfif>
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
</cfif>

</cfform>
	
</table></cfoutput>
<br><br>