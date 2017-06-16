<cfoutput>
<cfinclude template="debugging.templates.readIP.cfm">
<cfif url.action2 EQ "ipEdit">
	<cfset ipEdit = XmlSearch(xmlObj, '//*[  @id=''#url.id#'' and local-name()=''IPsList'' ]')>
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
					<cfinputClassic type="hidden" name="id" value="#ipEdit[1].XmlAttributes.id#">
					<cfinputClassic type="text" name="ip" value="#ipEdit[1].XmlText#" class="large">
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