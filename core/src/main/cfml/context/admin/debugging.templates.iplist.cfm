<cfoutput>
<cfset ipStruct = deserializeJSON(xmlElem[1].xmlText)>
<cfif !structIsEmpty(ipStruct.addedIp)>
	<h2>#stText.debug.templates.ipList#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<thead>
				<tr>
					<th width="3%"></th>
					<th width="39%">#stText.debug.templates.ip#</th>
					<th width="3%"></th>
				</tr>
			</thead>
			<tbody>
				<cfloop collection="#ipStruct.addedIP#" item="itm">
					<tr>
						<td><input type="checkbox" class="checkbox" name="id" value="#itm#"></td>
						<td>#ipStruct.addedIP[itm]#</td>
						<td class="ongwords">#renderEditButton("#request.self#?action=#url.action#&action2=ipedit&id=#itm#")#</td>
					</tr>
				</cfloop>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button submit" name="deleteIP" value="#stText.Buttons.Delete#">
						<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfif>
</cfoutput>