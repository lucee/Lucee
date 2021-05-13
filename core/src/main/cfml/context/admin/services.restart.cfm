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
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
--->

<!---
Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!---
Error Output --->
<cfset printError(error)>

<cfoutput>


<cfhtmlbody>

<script type="text/javascript">
	var submitted = false;
	function restart(field) {
		field.disabled = true;
		submitted = true;
		url='restart.cfm?adminType=#request.admintype#';
		//createWaitBlockUI("restart in progress ...");
		$('##updateInfoDesc').html('<img src="../res/img/spinner16.gif.cfm">');
		disableBlockUI = true;

		$.ajax(url )
			.done(function( data, textStatus, xhr ) {
				var response = $.trim(data);
				if (response == ""){
					setTimeout(function(){
						// load the admin page to trigger a deploy, so css/js loads correctly
						$.get("?", function(response) {
							window.location=('?action=overview');
						});
					}, 1000); // give Lucee enough time to startup, otherwise, the admin login may show without css/js
				} else {
					$('##updateInfoDesc').addClass("error").attr("style", null).html(response);
					//window.location=('?action=#url.action#'); 
				}
			})
			.fail(function( xhr, textStatus, errorThrown ) {
				$('##updateInfoDesc').addClass("error").attr("style", null).html( "<b>" + xhr.status + "</b><br>"  + xhr.responseText);
			})
			.always(function() {
				field.disabled = false;
			});
		
	}
</script>

</cfhtmlbody>


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
	<form method="post">
		<table class="tbl" width="740">	
		<cfmodule template="remoteclients.cfm" colspan="2">
		<tr>
			<td colspan="2">
				<input type="button" class="button submit" name="mainAction" value="#stText.services.update.restart#" onclick="restart(this)">
			</td>
		</tr>
		</table>
	</form>
</div>
</cfoutput>