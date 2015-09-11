<!--- 
Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">


<!--- 
ACTIONS --->
<cftry>
<cfswitch expression="#form.mainAction#">

<!--- Setting --->
	<cfcase value="setting">
		<cfset admin.mailLogFile=toFile(logFile)>
		<cfset admin.mailSpoolEnable=isDefined("form.spoolenable") and spoolenable>
		<cfset admin.mailSpoolInterval=spoolinterval>
		<cfset admin.mailTimeout=timeout>
		<cfset admin.store()>
	</cfcase>

<!--- UPDATE --->
	<cfcase value="#stText.Buttons.Update#">
	<!--- update --->
		<cfif form.subAction EQ "#stText.Buttons.Update#">
			<cfset ERROR.message="#stText.Buttons.Update#">
			<cfset data.hosts=toArrayFromForm("hostname")>
			<cfset data.usernames=toArrayFromForm("username")>
			<cfset data.passwords=toArrayFromForm("password")>
			<cfset data.ports=toArrayFromForm("port")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.hosts)#">
				<cfif isDefined("data.rows[#idx#]") and data.hosts[idx] NEQ "">
					<cfparam name="data.ports[#idx#]" default="21">
					<cfif trim(data.ports[idx]) EQ ""><cfset data.ports[idx]=21></cfif>
					<cfset admin.updateMailServer(data.rows[idx]-1,data.hosts[idx],data.usernames[idx],data.passwords[idx],toInt(data.ports[idx]))>
				</cfif>
			</cfloop>
	<!--- add --->
			<cfif structKeyExists(form,"row_new") and form.hostName_new NEQ "">
				<cfparam name="form.port_new" default="21">
				<cfif trim(form.port_new) EQ ""><cfset form.port_new=21></cfif>
				<cfset admin.addMailServer(form.hostName_new,form.username_new,form.password_new,toInt(form.port_new))>
			</cfif>
			<cfset admin.store()>
	<!--- delete --->
		
		<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
			<cfset data.rows=toArrayFromForm("row")>
			<!---  @todo
			<cflock type="exclusive" scope="application" timeout="5"></cflock> --->
				<cfset len=arrayLen(data.rows)>
				<cfloop index="i" from="1" to="#len#">
					<cfset idx=(len+1)-i>
					<cfif isDefined("data.rows[#idx#]")>
						<cfset admin.removeMailServer(data.rows[idx]-1)>
					</cfif>
				<cfset admin.store()>
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
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!--- 
Error Output--->
<cfif error.message NEQ "">
<cfoutput><span class="CheckError">
#error.message#<br>
#error.detail#
</span><br><br></cfoutput>
</cfif>
<script>
function checkTheBox(field) {
	var apendix=field.name.split('_')[1];
	var box=field.form['row_'+apendix];
	box.checked=true;
}
</script>


<cfset servers=config.mailServers>

<!---
Mail Settings
		
		@todo help text --->
		<h2>#stText.Mail.Settings#</h2>
		<table class="tbl" width="550">
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<tr>
			<th scope="row">#stText.Mail.LogFile#</th>
			<td width="450"><cfinput type="text" name="logFile" value="#config.mailLogger.file#" required="yes"  style="width:450px" message="#stText.Mail.LogFileMissing#"></td>
		</tr>
		<tr>
			<th scope="row">#stText.Mail.SpoolEnabled#</th>
			<td width="450"><input <cfif config.isMailSpoolEnable()>checked</cfif> type="checkbox" class="checkbox" name="spoolEnable" value="yes"></td>
		</tr>
		<tr>
			<th scope="row">#stText.Mail.SpoolInterval#</th>
			<td width="450"><cfinput type="text" name="spoolInterval" value="#config.mailSpoolInterval#" validate="integer" style="width:50px" required="yes"></td>
		</tr>
		<tr>
			<th scope="row">#stText.Mail.Timeout#</th>
			<td width="450"><cfinput type="text" name="timeout" value="#config.mailTimeout#" validate="integer" style="width:50px" required="yes"></td>
		</tr>
		<tr>
			<td colspan="2"><cfoutput>
				<input type="hidden" name="mainAction" value="setting">
				<input type="submit" class="button submit" name="_mainAction" value="#stText.Buttons.Update#">
				<input type="reset" class="reset" name="canel" value="#stText.Buttons.Cancel#">
			</cfoutput></td>
		</tr>
		</cfform>
		</table>
<br><br>

<!--- 		
Existing Collection --->
#stText.Mail.MailServers#
<table class="tbl">
	<tr>
		<td></td>
		<th scope="row">#stText.Mail.Server#</th>
		<th scope="row">#stText.Mail.Username#</th>
		<th scope="row">#stText.Mail.Password#</th>
		<th scope="row">#stText.Mail.Port#</th>
	</tr>
<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
<cfoutput>
<cfset len=arrayLen(servers)>
<cfloop index="idx" from="1" to="#len#" >
	<cfset ms=servers[idx]>
	<tr>
		<td>
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td><input type="checkbox" class="checkbox" name="row_#idx#" value="#idx#"></td>
		</tr>
		</table>
		
		</td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="hostName_#idx#" value="#ms.hostName#" required="yes"  style="width:250px" message="#stText.Mail.ServerMissing##idx#)"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="username_#idx#" value="#ms.username#" required="no"  style="width:150px" message="#stText.Mail.UserNameMissing##idx#)"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="password_#idx#" value="#ms.password#" required="no"  style="width:150px" message="#stText.Mail.PasswordMissing##idx#)"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="port_#idx#" value="#ms.port#" required="no"  style="width:40px" validate="integer" message="#stText.Mail.PortErrorFirst##idx##stText.Mail.PortErrorLast#"></td>
	</tr>
</cfloop>
	<tr>
		<td>
		<table border="0" cellpadding="0" cellspacing="0">
		<tr>
			<td><input type="checkbox" class="checkbox" name="row_new" value="0"></td>
		</tr>
		</table>
		
		</td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)"  type="text" name="hostName_new" value="" required="no"  style="width:250px"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="username_new" value="" required="no"  style="width:150px"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="password_new" value="" required="no"  style="width:150px"></td>
		<td nowrap><cfinput onKeyDown="checkTheBox(this)" type="text" name="port_new" value="" required="no" validate="integer" message="Value for Port (Row #len+1#) must be of type number" style="width:40px"></td>
	</tr>
	<tr>
		<td colspan="8">
		 <table border="0" cellpadding="0" cellspacing="0">
		 <tr>
			<td><cfmodule template="tp.cfm"  width="10" height="1"></td>		
			<td><img src="resources/img/#ad#-bgcolor.gif.cfm" width="1" height="20"></td>
			<td></td>
		 </tr>
		 <tr>
			<td></td>
			<td valign="top"><img src="resources/img/#ad#-bgcolor.gif.cfm" width="1" height="14"><img src="resources/img/#ad#-bgcolor.gif.cfm" width="36" height="1"></td>
			<td>&nbsp;
			<input type="hidden" name="mainAction" value="#stText.Buttons.Update#">
			<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Update#">
			<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
			<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Delete#">
			</td>	
		</tr>
		 </table>
		 </td>
	</tr>
</cfoutput>
  </cfform>
</table>
