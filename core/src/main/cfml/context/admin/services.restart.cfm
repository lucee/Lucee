<cfif request.admintype EQ "web"><cflocation url="#request.self#" addtoken="no"></cfif>

<cfparam name="url.action2" default="none">
<cfset error.message="">
<cfset error.detail="">

<!---
<cftry>
<cfswitch expression="#url.action2#">
	<cfcase value="restart">
		<cfadmin 
			action="restart"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			
			remoteClients="#request.getRemoteClients()#">

	</cfcase>
</cfswitch>
	<cfcatch><cfrethrow>
	
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
--->

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output --->
<cfset printError(error)>

<cfoutput>

<script type="text/javascript">
	var submitted = false;
	function restart(field) {
		field.disabled = true;
		submitted = true;
		url='restart.cfm?#session.urltoken#&adminType=#request.admintype#';
		$(document).ready(function(){
			//createWaitBlockUI("restart in progress ...");
			$('##updateInfoDesc').html('<img src="../res/img/spinner16.gif.cfm">');
			disableBlockUI=true;
			

	 		$.get(url, function(response) {
	      		//window.location=('#request.self#?action=#url.action#');
	 			//http://localhost:8080/context/admin/server.cfm?action=services.restart
	 			field.disabled = false;
	 			//$.unblockUI();
	      		if((response+"").trim()=="")
					$('##updateInfoDesc').html("<p>#stText.services.update.restartOKDesc#</p>");
				else
					window.location=('#request.self#?action=#url.action#'); //$('##updateInfoDesc').html(response);

	 		});
		});
	}


	</script>
			




<br>
<br>
<br>
<br>
<br>
<br>
<div id="updateInfoDesc" style="text-align: center;">
<p>#stText.services.update.restartDesc#</p>
</div>
<div style="text-align: center;">

</cfoutput>	

<!--- 
restart --->
<cfoutput>
<table class="tbl" width="740">
<form method="post">
<cfmodule template="remoteclients.cfm" colspan="2">
<tr>
	<td colspan="2"> 
		<input type="button" class="button submit" name="mainAction" value="#stText.services.update.restart#" onclick="restart(this)">
	</td>
</tr>
</form>
</table>
</div>
</cfoutput>