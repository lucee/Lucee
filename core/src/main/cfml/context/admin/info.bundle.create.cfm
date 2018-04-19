<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#" 
	action="getBundle" 
	symbolicName="#url.symbolicName#"
	version="#isNull(url.version)?'':url.version#"
	returnvariable="bundle">

<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>

	<h2>#stText.info.bundles.subject# #bundle.title#<cfif bundle.symbolicName != bundle.title> (#bundle.symbolicName#)</cfif></h2>
	<cfif !isNull(bundle.description) && len(bundle.description)><div class="pageintro">#bundle.description.trim()#</div></cfif>

		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.info.bundles.version#</th>
					<td>#bundle.version#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.created#</th>
					<td>#extractDateFromBundleHeader(bundle.headers)#</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.path#</th>
					<td><cfif !isNull(bundle.path)>#bundle.path#</cfif></td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.size?:"Size"#</th>
					<td>
						<cfset p=bundle.path&"">
						<cfif fileExists(p)>#byteFormat(fileInfo(p).size)#</cfif>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.info.bundles.vendor#</th>
					<td><cfif !isNull(bundle.vendor)>#bundle.vendor#</cfif></td>
				</tr>
				<tr style="#csss[bundle.state]#">
					<th scope="row">#stText.info.bundles.State#</th>
					<td>#stText.info.bundles.states[bundle.state]?:bundle.state#</td>
				</tr>
				<cfif !isNull(bundle.usedBy)><tr>
					<th scope="row">#stText.info.bundles.usedBy#</th>
					<td>#bundle.usedBy#</td>
				</tr></cfif>
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