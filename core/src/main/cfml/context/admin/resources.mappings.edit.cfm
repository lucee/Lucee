<cfsilent>
	<cfset mapping=struct()>
	<cfloop query="mappings">
		<cfif mappings.virtual EQ url.virtual>
			<cfloop index="key" list="#mappings.columnlist#">
				<cfset mapping[key]=mappings[key]>
			</cfloop>
			<cfset mapping.id=mappings.currentrow>
		</cfif>
	</cfloop>
</cfsilent>

<cfoutput>
	<div class="pageintro">#stText.Mappings.editDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?virtual=#mapping.virtual#&action=#url.action#&action2=#url.action2#" method="post">
		<input type="hidden" name="mainAction" value="#stText.Buttons.save#">
		<input type="hidden"  name="row_#mapping.id#" value="#mapping.id#">
		<cfif !isEmpty(mapping.strArchive) && isEmpty(mapping.archive)>
			<div class="error"><p style="color:red"><b>#stText.mappings.archiveAlert#</b></p></div>
		<cfelseif !isempty(mapping.strPhysical) && isempty(mapping.physical)>
			<div class="error"><p style="color:red" class="error"><b>#stText.mappings.physicalAlert#</b></p></div>
		</cfif>
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Mappings.VirtualHead#</th>
					<td title="#mapping.virtual#" nowrap><input type="hidden" 
							name="virtual_#mapping.id#" value="#mapping.virtual#">#mapping.virtual#</td>
				</tr>
				<tr>
					<th scope="row">#stText.Mappings.PhysicalHead#</th>
					<cfset css=iif(len(mapping.physical) EQ 0 and len(mapping.strPhysical) NEQ 0,de('Red'),de(''))>
					<td class="tblContent#css#" nowrap <cfif len(mapping.strPhysical)>title="#mapping.strPhysical##newLine()##mapping.Physical#"</cfif>><cfif mapping.readOnly>#cut(mapping.strPhysical,72)#<cfelse><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="physical_#mapping.id#" value="#mapping.strPhysical#" required="no"  
							style="width:100%" message="#stText.Mappings.PhysicalMissing##mapping.id#)"></cfif></td>
				</tr>
				<tr>
					<th scope="row">#stText.Mappings.ArchiveHead#</th>
					<cfset css=iif(len(mapping.archive) EQ 0 and len(mapping.strArchive) NEQ 0,de('Red'),de(''))>
					<td class="tblContent#css#" nowrap <cfif len(mapping.strArchive)>title="#mapping.strArchive##newLine()##mapping.Archive#"</cfif>><cfif mapping.readOnly>#cut(mappings.strArchive,72)#<cfelse><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
						name="archive_#mapping.id#" value="#mapping.strArchive#" required="no"  
						style="width:100%" message="#stText.Mappings.ArchiveMissing##mapping.id#)"></cfif></td>
				</tr>
				<tr>
					<th scope="row">#stText.Mappings.PrimaryHead#</th>
					<td nowrap><cfif mapping.readOnly><cfif mapping.PhysicalFirst>physical<cfelse>archive</cfif><cfelse><select name="primary_#mapping.id#" onchange="checkTheBox(this)">
						<option value="physical" <cfif mapping.PhysicalFirst>selected</cfif>>#stText.Mappings.Physical#</option>
						<option value="archive" <cfif not mapping.PhysicalFirst>selected</cfif>>#stText.Mappings.Archive#</option>
					</select></cfif></td>
				</tr>
				<tr>
					<th scope="row">#stText.setting.inspecttemplate#</th>
					<td>
						<cfif mapping.readOnly>
							<cfloop list="never,once,always,inherit" item="type">
							<cfif mapping.inspect EQ type or (type EQ "inherit" and mapping.inspect EQ "")>
							#stText.setting['inspectTemplate'&type]#
							<div class="comment">#stText.setting['inspectTemplate'&type&"Desc"]#</div>
							</cfif>
							</cfloop>
						<cfelse>
							<ul class="radiolist">
								<cfloop list="never,once,always,inherit" item="type">
									<li><label>
										<input class="radio" type="radio" name="inspect_#mapping.id#" value="#type EQ "inherit"?"":type#" <cfif mapping.inspect EQ type or (type EQ "inherit" and mapping.inspect EQ "")> checked="checked"</cfif>>
										<b>#stText.setting['inspectTemplate'&type]#</b>
									</label>
									<div class="comment">#stText.setting['inspectTemplate'&type&"Desc"]#</div>
									</li>
								</cfloop>
							</ul>
						</cfif>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.Mappings.ToplevelHead#</th>
					<td nowrap>
						<cfif mapping.readOnly>
							#iif(mapping.toplevel,de("Yes"),de("No"))#
						<cfelse>
							<input type="checkbox" class="checkbox" name="toplevel_#mapping.id#" 
							value="yes" <cfif mapping.toplevel>checked</cfif>>
						</cfif>
						<div class="comment">#stText.Mappings.ToplevelDesc#</div>
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.save#">
							<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="br button" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>


<cfsavecontent variable="codeSample"><cfset count=0><cfset del="">
this.mappings["#mapping.virtual#"]=<cfif len(mapping.strPhysical) && !len(mapping.strArchive)>
&nbsp;&nbsp;&nbsp;"#mapping.strPhysical#"<cfelse>{<cfif len(mapping.strPhysical)><cfset count++>
&nbsp;&nbsp;&nbsp;physical:"#mapping.strPhysical#"<cfset del=","></cfif><cfif len(mapping.strArchive)><cfset count++>
&nbsp;&nbsp;&nbsp;#del#archive:"#mapping.strArchive#"<cfset del=","></cfif><cfif count==2 && !mapping.PhysicalFirst>
&nbsp;&nbsp;&nbsp;#del#primary:"<cfif mapping.PhysicalFirst>physical<cfelse>archive</cfif>"<cfset del=","></cfif>}</cfif>;
&nbsp;
// "#stText.setting.inspecttemplate#"/"#stText.Mappings.ToplevelHead#" setting not supported with application type mappings
</cfsavecontent>
<cfset renderCodingTip( codeSample, "", true )>

		<!--- Compile --->
		<h2>#stText.Mappings.compileTitle#</h2>
		<div class="itemintro">#stText.Mappings.compileDesc#</div>
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Mappings.compileStopOnError#</th>
					<td nowrap><input 
					type="checkbox" class="checkbox" name="stopOnError_#mapping.id#" value="yes" checked="checked"> <div class="comment">#stText.Mappings.compileStopOnErrorDesc#</div></td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.compileAll#">
							<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="br button" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>

		<!--- Create Archive --->
		<h2>#stText.Mappings.archiveTitle#</h2>
		<div class="itemintro">#stText.Mappings.archiveDesc#</div>
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.mappings.addCFMLFiles#</th>
					<td nowrap>
						<input type="checkbox" class="checkbox" name="addCFMLFiles_#mapping.id#" value="yes" checked>
						<div class="comment">#stText.mappings.addCFMLFilesDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.mappings.addNonCFMLFiles#</th>
					<td nowrap>
						<input type="checkbox" class="checkbox" name="addNonCFMLFiles_#mapping.id#" value="yes" checked> 
						<div class="comment">#stText.mappings.addNonCFMLFilesDesc#</div>
					</td>
				</tr>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="2" attention="#htmleditformat(stText.remote.downloadArchive)#">
				</cfif>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button cancel" name="subAction" value="#stText.Buttons.downloadArchive#">
						<input type="submit" class="bm button submit" name="subAction" value="#stText.Buttons.addArchive#">
						<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="br button" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>




</cfoutput>