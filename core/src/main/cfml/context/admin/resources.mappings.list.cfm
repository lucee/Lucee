<!--- list all mappings and display necessary edit fields --->
<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>

	<div class="pageintro">#stText.Mappings.IntroText#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="3%"><cfif hasAccess><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></cfif></th>
					<th>#stText.Mappings.VirtualHead#</th>
					<th>#stText.Mappings.PhysicalHead#</th>
					<th>#stText.Mappings.ArchiveHead#</th>
					<th>#stText.Mappings.PrimaryHead#</th>
					<th>#stText.Mappings.TrustedHead#</th>
					<th width="3%"></th>
				</tr>
			</thead>
			<tbody>
				<cfloop query="mappings">
					<cfif not mappings.hidden>
						<!--- and now display --->
						<tr>
							<!--- checkbox ---->
							<td>
								<input type="hidden" name="stopOnError_#mappings.currentrow#" value="yes">
								<cfif not mappings.readOnly>
									<input type="checkbox" class="checkbox" name="row_#mappings.currentrow#" value="#mappings.currentrow#">
								</cfif>
							</td>
							<!--- virtual --->
							<td nowrap="nowrap">
								<input type="hidden" name="virtual_#mappings.currentrow#" value="#mappings.virtual#">
								<cfif len(mappings.virtual) gt 19>
									<abbr title="#mappings.virtual#">#cut(mappings.virtual, 16)#...</abbr>
								<cfelse>
									#mappings.virtual#
								</cfif>
							</td>
							
							<!--- physical --->
							<cfset css=iif(len(mappings.physical) EQ 0 and len(mappings.strPhysical) NEQ 0,de('Red'),de(''))>
							<td class="tblContent#css# longwords">
								<cfif FALSE and len(mappings.strPhysical) gt 39>
									<abbr title="#mappings.strPhysical#">#left(mappings.strPhysical, 20)#...#right(mappings.strPhysical, 16)#</abbr>
								<cfelse>
									#mappings.strPhysical#
								</cfif>
							</td>
							<!--- archive --->
							<cfset css=iif(len(mappings.archive) EQ 0 and len(mappings.strArchive) NEQ 0,de('Red'),de(''))>
							<td class="tblContent#css# longwords">
								<cfif FALSE and len(mappings.strArchive) gt 39>
									<abbr title="#mappings.strArchive#">#left(mappings.strArchive, 20)#...#right(mappings.strArchive, 16)#</abbr>
								<cfelse>
									#mappings.strArchive#
								</cfif>
							</td>
							<!--- primary --->
							<td>
								<cfif mappings.PhysicalFirst>
									#stText.Mappings.Physical#
								<cfelse>
									#stText.Mappings.Archive#
								</cfif>
							</td>
							<!--- inspect --->
							<td>
								<cfif len(mappings.inspect)>
								#stText.setting['inspecttemplate'&mappings.inspect&'Short']#
								<cfelse>
								#stText.setting['inspecttemplateInheritShort']#
								</cfif>
								<input type="hidden" name="toplevel_#mappings.currentrow#" value="#mappings.toplevel#">
							</td>
							<!--- edit --->
							<td>
								<cfif not mappings.readOnly>
									#renderEditButton("#request.self#?action=#url.action#&action2=create&virtual=#mappings.virtual#")#
								</cfif>
							</td>
						</tr>
					</cfif>
				</cfloop>
				<!--- <tr><td colspan="7">
				
<cfsavecontent variable="codeSample">

<cfloop query="mappings"><cfif mappings.hidden || mappings.virtual=="/lucee" || mappings.virtual=="/lucee-server"><cfcontinue></cfif><cfset del="">
this.mappings=["#mappings.virtual#"]={<cfif len(mappings.strPhysical)>
&nbsp;&nbsp;&nbsp;physical:"#mappings.strPhysical#"<cfset del=","></cfif><cfif len(mappings.strArchive)>
&nbsp;&nbsp;&nbsp;#del#archive:"#mappings.strArchive#"<cfset del=","></cfif>};
</cfloop>
</cfsavecontent>
						<cfset renderCodingTip( codeSample )>
				</td>
				</tr>--->
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="7" line=true>
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="7">
							<input type="hidden" name="mainAction" value="#stText.Buttons.save#">
							<!---<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.save#">
							--->
							<input type="reset" class="bl button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="bm button submit enablebutton" name="subAction" value="#stText.Buttons.Delete#">
							<input type="submit" class="br button submit enablebutton" name="subAction" value="#stText.Buttons.compileAll#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
	<cfif hasAccess>
		<h2>Create new mapping</h2>
		<cfformClassic onerror="customError" onsubmit="return inputMapping()" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="row_1" value="1">
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.Mappings.VirtualHead#</th>
						<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="virtual_1" value="" required="yes" class="medium" id="ioMapping" message="#stText.debug.labelempty#">
							<label id="ioMapOut" class="commentError"></label>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Mappings.PhysicalHead#</th>
						<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="physical_1" value="" required="no" class="large">
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Mappings.ArchiveHead#</th>
						<td><cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="archive_1" value="" required="no" class="large">
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Mappings.PrimaryHead#</th>
						<td>
							<select name="primary_1" onchange="checkTheBox(this)" class="small">
								<option value="physical" selected>#stText.Mappings.Physical#</option>
								<option value="archive">#stText.Mappings.Archive#</option>
							</select>
						</td>
					</tr>
					<tr>
						<th scope="row">
							#stText.Mappings.TrustedHead#</div>
						</th>
						<td>
						 	<ul class="radiolist">
							<cfloop list="never,once,always,inherit" item="type">
								<li><label>
									<input class="radio" type="radio" name="inspect_1" value="#type EQ "inherit"?"":type#" <cfif type EQ "inherit"> checked="checked"</cfif>>
									<b>#stText.setting['inspectTemplate'&type]#</b>
								</label>
								<div class="comment">#stText.setting['inspectTemplate'&type&"Desc"]#</div>
								</li>
							</cfloop>
							</ul>
							
							<input type="hidden" name="toplevel_1" value="yes">
						</td>
					</tr>
					<cfmodule template="remoteclients.cfm" colspan="2" line=true>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="hidden" name="mainAction" value="#stText.Buttons.save#">
							<input type="submit" class="bs button submit" name="subAction" value="#stText.Buttons.save#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>
</cfoutput>