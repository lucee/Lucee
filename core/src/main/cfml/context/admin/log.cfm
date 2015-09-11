

<cfadmin 
        action="getLogSettings" 
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
        returnVariable="logs"
        remoteClients="#request.getRemoteClients()#">

<cfset stText.log.title="Log">
<cfset stText.log.desc="This page allows to set the logelevl and location of every single log.">
<cfset stText.log.level="Level">
<cfset stText.log.source="Source">
<cfset stText.log.name="Name">
<cfset stText.log.maxFile="Max Files">
<cfset stText.log.maxFileSize="Max File Size in KB">

<cfoutput>
#stText.log.desc#


<cfloop query="logs">
<table class="tbl" width="740">
<tr>
	<td colspan="2"><h2>#ucFirst(logs.name)# #stText.log.title#</h2> <!---#attributes.title##attributes.description#---></td>
</tr>
<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
<tr>
	<th scope="row">#stText.log.level#</th>
	<td>
    <select name="#logs.name#_level">
    	<cfloop list="Info,Debug,Warn,Error,Fatal" index="l"><option <cfif l EQ level>selected</cfif>>#l#</option></cfloop>
	</select></td>
</tr>
<tr>
	<th scope="row">#stText.log.source#</th>
	<td><cfinput type="text" name="#logs.name#_source" title="#logs.path#" value="#logs.virtualpath#" style="width:300px" required="yes" message=""></td>
</tr>

<tr>
	<th scope="row">#stText.log.maxFile#</th>
	<td><cfinput type="text" name="#logs.name#_maxFile" value="#logs.maxFile#" style="width:60px" required="yes" message=""></td>
</tr>
<tr>
	<th scope="row">#stText.log.maxFileSize#</th>
	<td><cfinput type="text" name="#logs.name#_maxFileSize" value="#isNumeric(logs.maxFileSize)?logs.maxFileSize/1024:''#" style="width:60px" required="yes" message=""></td>
</tr>






<!---<cfmodule template="remoteclients.cfm" colspan="2">--->
<tr>
	<td colspan="2">
		<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
		<input type="submit" class="button submit" name="run" value="#stText.Buttons.update#">
	</td>
</tr>
</cfform>

</table>
</cfloop>
</cfoutput>
<cfdump var="#logs#">
