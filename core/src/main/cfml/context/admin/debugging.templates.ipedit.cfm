<cfoutput>
<cfinclude template="debugging.templates.readIP.cfm">
<cfset ipStruct = deserializeJSON(xmlElem[1].xmlText)>
<cfif url.action2 EQ "ipEdit">
	<h2>#stText.debug.templates.editIP#</h2>
<cfelse>
	<h2>#stText.debug.templates.addIP#</h2>
</cfif>
<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
	<table class="maintbl autowidth" style="width:400px;">
		<tbody>
			<tr>
				<th scope="row">#stText.debug.templates.ip#</th>
				<td>
					<cfif url.action2 EQ "ipEdit">
						<cfinputClassic type="hidden" name="id" value="#url.id#">
						<cfinputClassic type="text" name="ip" value="#ipStruct.addedIp[url.id]#" class="large">
					<cfelse>
						<cfinputClassic type="hidden" name="id" value="">
						<cfinputClassic type="text" name="ip" value="" class="large">
					</cfif>
				</td>
			</tr>
			<tr>
				<th scope="row">#stText.debug.templates.allowIp#</th>
				<td>
					<input type="checkbox" class="checkbox" name="allowLocalIp" value=true <cfif ipStruct.allowLocalIP>checked</cfif>>
					<div class="comment">#stText.debug.templates.allowIpDesc#</div>
				</td>
			</tr>
		</tbody>
		<tfoot>
			<tr>
				<td colspan="2">
					<cfif url.action2 EQ "ipEdit">
						<input type="submit" class="bl button submit" name="EditIP" value="#stText.buttons.EditIP#">
						<input onclick="window.location='server.cfm?action=debugging.templates';" class="br button" name="cancel" value="cancel" type="button">
					<cfelse>
						<input type="submit" class="bl button submit" name="addIP" value="#stText.Buttons.addIP#">
						<input class="br reset" name="cancel" value="cancel" type="reset">
					</cfif>
				</td>
			</tr>
		</tfoot>
	</table>
</cfformClassic>
</cfoutput>