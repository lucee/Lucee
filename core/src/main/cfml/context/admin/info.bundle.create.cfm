<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#" 
	action="getBundles" 
	symbolicName="#url.symbolicName#"
	returnvariable="_bundles">

<cfloop query="#_bundles#">
	<cfif url.symbolicName EQ _bundles.symbolicName>
		<cfset bundle=querySlice(_bundles,_bundles.currentrow)>
	</cfif>
</cfloop>

<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>

	<h2>#stText.info.bundles.subject# #bundle.title#<cfif bundle.symbolicName != bundle.title> (#bundle.symbolicName#)</cfif></h2>
	<cfif len(bundle.description)><div class="pageintro">#bundle.description.trim()#</div></cfif>

		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.info.bundles.version#</th>
					<td>#bundle.version#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.vendor#</th>
					<td>#bundle.vendor#</td>
				</tr>
				<tr style="#csss[bundle.state]#">
					<th scope="row">#stText.info.bundles.State#</th>
					<td>#bundle.State#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.usedBy#</th>
					<td>#bundle.usedBy#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.isFragment#</th>

					<td>#yesNoFormat(bundle.fragment)#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.manifestHeaders#</th>
					<td>
						<table class="maintbl">
						<tbody>
							<cfloop struct="#bundle.headers#" index="k" item="v">
							<tr>
								<th scope="row">#k#</th>
								<td>#v#</td>
							</tr>
						</cfloop>
						</tbody>
					</table>

					</td>
				</tr>
			</tbody>
		</table>

</cfoutput>