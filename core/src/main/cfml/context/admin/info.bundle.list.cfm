<cfparam name="url.col" default="title">
<cfparam name="url.dir" default="asc">
<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#" 
	action="getBundles" 
	returnvariable="bundles">
<cfscript>
	queryAddColumn(bundles,"size");
	queryAddColumn(bundles,"sizeAsString");
	queryAddColumn(bundles,"created");
	queryAddColumn(bundles,"createdAsString");
	loop query=bundles {
		p=bundles.path&"";
		if(fileExists(p)){
			s=fileInfo(p).size;
			querySetCell(bundles,"size",s,bundles.currentrow);
			querySetCell(bundles,"sizeAsString",byteFormat(s),bundles.currentrow);
			d=toDateFromBundleHeader(bundles.headers);
			if(isDate(d)) {
				querySetCell(bundles,"created",dateDiff("s",unix0,d),bundles.currentrow);
				querySetCell(bundles,"createdAsString",lsDateFormat(d),bundles.currentrow);
			}
			else
				querySetCell(bundles,"created",0,bundles.currentrow);
		}
	}
	querySort(bundles,url.col,url.dir);
	
</cfscript>
<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>
	<div class="pageintro">#stText.bundles.introText#</div>
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=title&dir=#url.col=='title'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.subject#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=version&dir=#url.col=='version'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.version#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=created&dir=#url.col=='created'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.created#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=size&dir=#url.col=='size'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.size?:"Size"#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=vendor&dir=#url.col=='vendor'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.vendor#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=usedBy&dir=#url.col=='usedBy'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.usedBy#</a></th>
					<th class="linkContext"><a href="#request.self#?action=#url.action#&col=state&dir=#url.col=='state'?(url.dir=='asc'?'desc':'asc'):'asc'#">#stText.info.bundles.state#</a></th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<cfloop query="bundles">
						<!--- and now display --->
						<tr>
							<!--- checkbox
							<td>
								<input type="hidden" name="stopOnError_#bundles.currentrow#" value="yes">
								<input type="checkbox" class="checkbox" name="row_#bundles.currentrow#" value="#bundles.currentrow#">
								
							</td> ---->
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

							<!--- Created --->
							<td nowrap="nowrap">
								#bundles.createdAsString#
							</td>
							<!--- Size --->
							<td nowrap="nowrap">
								#bundles.sizeAsString#
							</td>
							<!--- path
							<!--- date --->
							<td nowrap="nowrap">
								#extractDateFromBundleHeader(bundles.headers)#
							</td>
							<!--- path --->
							<td title="#bundles.path#">
							#listLast(bundles.path,"\/")#
							</td> --->
							<!--- vendor --->
							<td >
							#bundles.vendor#
							</td>

							<!--- usedBy --->
							<td nowrap="nowrap">
								#replace(bundles.usedBy,',','<br>','all')#
							</td>

							<!--- state --->
							<td style="#csss[bundles.state]#" nowrap="nowrap">
								#stText.info.bundles.states[bundles.state]?:bundles.state#
							</td>
							<!--- edit --->
							<td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&symbolicName=#bundles.symbolicName#&version=#bundles.version#")#
								
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
