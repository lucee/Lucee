<cfoutput>
<cfif url.action2 EQ "ipedit">
	<cfinclude template="debugging.templates.readIP.cfm">
</cfif>
<cfif url.action2 EQ "ipEdit">
	<cfset ipStruct = deserializeJSON(xmlElem[1].xmlText)>
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
						<cfinputClassic type="text" name="ip" value="#ipStruct[url.id]#" class="large">
					<cfelse>
						<cfinputClassic type="hidden" name="id" value="">
						<cfinputClassic type="text" name="ip" value="" class="large">
					</cfif>
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