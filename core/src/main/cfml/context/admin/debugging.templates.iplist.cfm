<cfoutput>
<cfset ipStruct = deserializeJSON(xmlElem[1].xmlText)>
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
			<cfloop collection="#ipStruct#" item="itm">
				<tr>
					<cfif itm EQ "1" || itm EQ "2">
						<td></td>
						<td>#ipStruct[itm]#</td>
						<td></td>
					<cfelse>
						<td><input type="checkbox" class="checkbox" name="id" value="#itm#"></td>
						<td>#ipStruct[itm]#</td>
						<td class="ongwords">#renderEditButton("#request.self#?action=#url.action#&action2=ipedit&id=#itm#")#</td>
					</cfif>
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
</cfoutput>