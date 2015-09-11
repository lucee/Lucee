<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#" 
	action="getBundles" 
	returnvariable="bundles">

<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>

	<div class="pageintro">#stText.bundles.introText#</div>
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="3%"><cfif hasAccess><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></cfif></th>
					<th>#stText.info.bundles.subject#</th>
					<th>#stText.info.bundles.version#</th>
					<th>#stText.info.bundles.vendor#</th>
					<th>#stText.info.bundles.usedBy#</th>
					<th>#stText.info.bundles.state#</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<cfloop query="bundles">
						<!--- and now display --->
						<tr>
							<!--- checkbox ---->
							<td>
								<input type="hidden" name="stopOnError_#bundles.currentrow#" value="yes">
								<input type="checkbox" class="checkbox" name="row_#bundles.currentrow#" value="#bundles.currentrow#">
								
							</td>
							<!--- subject --->
							<td>
								<input type="hidden" name="virtual_#bundles.currentrow#" value="#bundles.title#">
								#bundles.title#<cfif bundles.symbolicName != bundles.title> (#bundles.symbolicName#)</cfif>
								<cfif len(bundles.description)><br><span class="comment">#bundles.description.trim()#</span></cfif>
							</td>
							
							<!--- version --->
							<td nowrap="nowrap">
								#bundles.version#
							</td>
							<!--- vendor --->
							<td >
							#bundles.vendor#
							</td>

							<!--- usedBy --->
							<td nowrap="nowrap">
								#bundles.usedBy#
							</td>

							<!--- state --->
							

							<td style="#csss[bundles.state]#" nowrap="nowrap">
								#bundles.state#
							</td>
							<!--- edit --->
							<td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&symbolicName=#bundles.symbolicName#")#
								
							</td> 
						</tr>
					
				</cfloop>
				
			</tbody>
			<!---
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="7">
							<input type="hidden" name="mainAction" value="#stText.Buttons.save#">
							<!---<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.save#">
							--->
							<input type="reset" class="bl button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.Delete#">
						</td>
					</tr>
				</tfoot>
			</cfif>
			--->
		</table>
</cfoutput>