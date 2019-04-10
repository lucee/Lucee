

<cfadmin 
        action="getLogSettings" 
        type="#request.adminType#"
        password="#session["password"&request.adminType]#"
        
        returnVariable="logs"
        remoteClients="#request.getRemoteClients()#">

<cfoutput>
#stText.log.desc#


<cfloop query="logs">
<table class="tbl" width="740">
<tr>
	<td colspan="2"><h2>#ucFirst(logs.name)# #stText.log.title#</h2> <!---#attributes.title##attributes.description#---></td>
</tr>
<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
<tr>
	<th scope="row">#stText.log.level#</th>
	<td>
    <select name="#logs.name#_level">
    	<cfloop list="Info,Debug,Warn,Error,Fatal" index="l"><option <cfif l EQ level>selected</cfif>>#l#</option></cfloop>
	</select></td>
</tr>
<tr>
	<th scope="row">#stText.log.source#</th>
	<td><cfinputClassic type="text" name="#logs.name#_source" title="#logs.path#" value="#logs.virtualpath#" style="width:300px" required="yes" message=""></td>
</tr>

<tr>
	<th scope="row">#stText.log.maxFile#</th>
	<td><cfinputClassic type="text" name="#logs.name#_maxFile" value="#logs.maxFile#" style="width:60px" required="yes" message=""></td>
</tr>
<tr>
	<th scope="row">#stText.log.maxFileSize#</th>
	<td><cfinputClassic type="text" name="#logs.name#_maxFileSize" value="#isNumeric(logs.maxFileSize)?logs.maxFileSize/1024:''#" style="width:60px" required="yes" message=""></td>
</tr>






<!---<cfmodule template="remoteclients.cfm" colspan="2">--->
<tr>
	<td colspan="2">
		<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
		<input type="submit" class="button submit" name="run" value="#stText.Buttons.update#">
	</td>
</tr>
</cfformClassic>

</table>
</cfloop>
</cfoutput>
<cfdump var="#logs#">
