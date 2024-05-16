<cfoutput>
	
	<!--- list all mappings and display necessary edit fields --->
	<script type="text/javascript">
		function checkTheRadio(field) {
			var radios=field.form['extensions'];
			radios[radios.length-1].checked=true;
		}
	</script>

	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>
	
	<h2>#stText.CustomTags.CustomtagSetting#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<input type="hidden" name="subAction" value="setting" />
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.CustomTags.customTagDeepSearch#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="customTagDeepSearchDesc" value="yes" <cfif setting.deepsearch>checked</cfif>>
						<cfelse>
							<b>#yesNoFormat(setting.deepsearch)#</b>
						</cfif>
						
						<div class="comment">#stText.CustomTags.customTagDeepSearchDesc#</div>
					</td>
				</tr>
				<tr>
					<th scope="row">#stText.CustomTags.customTagLocalSearch#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="customTagLocalSearchDesc" value="yes" <cfif setting.localsearch>checked</cfif>>
						<cfelse>
							<b>#yesNoFormat(setting.localsearch)#</b>
						</cfif>
						<div class="comment">#stText.CustomTags.customTagLocalSearchDesc#</div>
					</td>
				</tr>
				<!--- customtags path cache ---->
				<tr>
					<th scope="row">#stText.CustomTags.customTagPathCache#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="customTagPathCache" value="yes" <cfif setting.customTagPathCache>checked</cfif>>
						<cfelse>
							<b>#yesNoFormat(setting.customTagPathCache)#</b>
						</cfif>
						<div class="comment">#stText.CustomTags.customTagPathCacheDesc#</div>
						<cfif setting.customTagPathCache><input type="submit" class="button submit" name="mainAction" value="#flushName#"></cfif>
					</td>
				</tr>

				<cfset arrExt=array('cfc','cfm','cfml')>
				<cfset lstSetExt=ArrayToList(setting.extensions)>
				<tr>
					<th scope="row">#stText.CustomTags.extensions#</th>
					<td>
					
						<cfset modes=array(
							struct(mode:'classic',ext:'cfm,cfml'),
							struct(mode:'mixed',ext:'cfm,cfc,lucee'),
							struct(mode:'modern',ext:'cfc,lucee')
						
						)>
						<cfif hasAccess>
							<cfset has=false>
							<ul class="radiolist">
								<cfloop array="#modes#" index="mode">
									<li>
										<label>
											<input type="radio" class="radio" name="extensions" value="#mode.ext#"<cfif mode.ext EQ lstSetExt> checked="checked"<cfset has=true></cfif>>
											<b>#mode.ext#</b>
										</label>
										<div class="comment inline">#stText.CustomTags.mode[mode.mode]#</div>
									</li>
								</cfloop>
								<li>
									<label>
										<input type="radio" class="radio" name="extensions" value="custom"<cfif not has> checked="checked"</cfif>>
									</label>
									<cfinputClassic type="text" onclick="checkTheRadio(this)" name="extensions_custom" value="#ArrayToList(setting.extensions)#" required="no" class="small" />
									<div class="comment inline">#stText.CustomTags.mode.custom#</div>
								</li>
							</ul>
						<cfelse>
							<b>#lstSetExt#</b><br />
						</cfif>
						<div class="comment">#stText.CustomTags.extensionsDesc#</div>
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
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
	
	
	
	
	
	
<!---	
	<h2>#stText.CustomTags.CustomtagMappings#</h2>
	<div class="itemintro">#stText.CustomTags.CustomtagMappingsDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th><cfif hasAccess><input type="checkbox" class="checkbox" 
						name="rro" onclick="selectAll(this)"></cfif></th>
					<th>#stText.CustomTags.name#</th>
					<th>#stText.CustomTags.Physical#</th>
					<th>#stText.CustomTags.Archive#</th>
					<th>#stText.CustomTags.Primary#</th>
					<th>#stText.Mappings.TrustedHead#</th>
				</tr>
			</thead>
			<tbody>
				<cfset count=0>
				<cfloop query="mappings">
					<tr>
						<td>
							<cfif not mappings.ReadOnly>
								<cfset count=count+1>
								<input type="hidden" name="virtual_#mappings.currentrow#" value="#mappings.virtual#">
								<input type="checkbox" class="checkbox" name="row_#mappings.currentrow#" value="#mappings.currentrow#">
							</cfif>
						</td>
						
						<cfset css=iif(len(mappings.physical) EQ 0 and len(mappings.strPhysical) NEQ 0,de('Red'),de(''))>
						<td class="tblContent#css#" title="#mappings.strphysical##chr(10)##mappings.physical#">
							<cfset name=ListCompact(mappings.virtual,'/')>
							<!--- not display uuid names, this are the default names generated by the system for old records --->
							<cfif isValid("uuid",name)>
								&nbsp;
							<cfelse>
								#name#
							</cfif>
						</td>
						
						<cfset css=iif(len(mappings.physical) EQ 0 and len(mappings.strPhysical) NEQ 0,de('Red'),de(''))>
						<td class="tblContent#css#" title="#mappings.strphysical##chr(10)##mappings.physical#">
							<cfif mappings.ReadOnly>
								<cfif len(mappings.strphysical) gt 40>
									<abbr title="#mappings.strphysical#">#cut(mappings.strphysical, 38)#...</abbr>
								<cfelse>
									#mappings.strphysical#
								</cfif>
							<cfelse>
								<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
									name="physical_#mappings.currentrow#" value="#mappings.strphysical#" required="no"  
									class="xlarge"
									message="#stText.CustomTags.PhysicalMissing##mappings.currentrow#">
							</cfif>
						</td>
						
						<cfset css=iif(len(mappings.archive) EQ 0 and len(mappings.strArchive) NEQ 0,de('Red'),de(''))>
						<td class="tblContent#css#" title="#mappings.strarchive##chr(10)##mappings.archive#">
							<cfif mappings.ReadOnly>
								<cfif len(mappings.strarchive) gt 40>
									<abbr title="#mappings.strarchive#">#cut(mappings.strarchive, 38)#...</abbr>
								<cfelse>
									#mappings.strarchive#
								</cfif>
							<cfelse>
								<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
									name="archive_#mappings.currentrow#" value="#mappings.strarchive#" required="no"  
									class="xlarge" 
									message="#stText.CustomTags.ArchiveMissing##mappings.currentrow#)">
							</cfif>
						</td>
						<td>
							<cfif mappings.ReadOnly>
								<cfif mappings.PhysicalFirst>
									#stText.Mappings.Physical#
								<cfelse>
									#stText.Mappings.Archive#
								</cfif>
							<cfelse>
								<select name="primary_#mappings.currentrow#" onchange="checkTheBox(this)">
									<option value="physical" <cfif mappings.physicalFirst>selected</cfif>>#stText.CustomTags.physical#</option>
									<option value="archive" <cfif not mappings.physicalFirst>selected</cfif>>#stText.CustomTags.archive#</option>
								</select>
							</cfif>
						</td>
						<td>
							<cfif mappings.readOnly>
							<cfif len(mappings.inspect)>
								#stText.setting['inspecttemplate'&mappings.inspect&'Short']#
							<cfelse>
								#stText.setting['inspecttemplateInheritShort']#
							</cfif>
						
						
							<cfelse>
							<select name="inspect_#mappings.currentrow#" onchange="checkTheBox(this)">
							<cfloop list="never,once,always,inherit" item="type">
									<option value="#type EQ "inherit"?"":type#" <cfif mappings.inspect EQ type or (type EQ "inherit" and mappings.inspect EQ "")>selected</cfif>>
										#stText.setting['inspecttemplate#type#Short']#
									</option>
							</cfloop>
							</select>
							</cfif>
						</td>
					</tr>
				</cfloop>
				<cfif hasAccess>
					<tr>
						<td>
							<input type="checkbox" class="checkbox" name="row_#mappings.recordcount+1#" value="#mappings.recordcount+1#">
						</td>
						<td>
							<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
								name="name_#mappings.recordcount+1#" value="" required="no" class="xlarge"  message="#stText.customtags.nameMissing#">
						</td>
						<td>
							<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
								name="physical_#mappings.recordcount+1#" value="" required="no" class="xlarge">
						</td>
						<td>
							<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
								name="archive_#mappings.recordcount+1#" value="" required="no"  class="xlarge">
						</td>
						<td>
							<select name="primary_#mappings.recordcount+1#" onchange="checkTheBox(this)">
								<option value="physical" selected>#stText.CustomTags.physical#</option>
								<option value="archive">#stText.CustomTags.archive#</option>
							</select>
						</td>
						<td>
							<select name="inspect_#mappings.recordcount+1#" onchange="checkTheBox(this)">
								<cfloop list="never,once,always,inherit" item="type">
									<option value="#type EQ 'inherit'?'':type#" <cfif type EQ 'inherit'>selected</cfif>>#stText.setting['inspecttemplate#type#Short']#</option>
								</cfloop>
							</select>
						</td>
					</tr>
				</cfif>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="6">
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="6">
							<input type="hidden" name="mainAction" value="#stText.Buttons.Update#">
							<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Update#">
							<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Delete#">
						 </td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic> --->
	
	
<!--- LIST MAPPINGS --->
	
	
	<h2>#stText.customTags.customtagMappings#</h2>
	<div class="itemintro">#stText.customTags.customtagMappingsDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)">
						</cfif>
					</th>
					<th>#stText.customTags.name#</th>
					<th style="width:25%">#stText.customTags.Physical#</th>
					<th style="width:25%">#stText.customTags.Archive#</th>
					<th>#stText.customTags.Primary#</th>
					<th>#stText.Mappings.TrustedHead#</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<cfset count=0>
				<cfloop query="mappings">
					<tr>
						
						<td>
							<cfif not mappings.ReadOnly>
								<cfset count=count+1>
								<input type="hidden" name="virtual_#mappings.currentrow#" value="#mappings.virtual#">
								<input type="checkbox" class="checkbox" name="row_#mappings.currentrow#" value="#mappings.currentrow#">
							</cfif>
						</td>
						
						<cfset css=iif(len(mappings.physical) EQ 0 and len(mappings.strPhysical) NEQ 0,de('Red'),de(''))>
						
						<td class="tblContent#css# longwords">
							<cfset name=ListCompact(mappings.virtual,'/')>
							<!--- not display uuid names, this are the default names generated by the system for old records --->
							<cfif isValid("uuid",name)>
								&nbsp;
							<cfelse>
								#name#
							</cfif>
						</td>
						<cfset css=iif(len(mappings.physical) EQ 0 and len(mappings.strPhysical) NEQ 0,de('Red'),de(''))>
						<td class="tblContent#css# longwords">
							<cfif mappings.ReadOnly>
								#mappings.strphysical#
							<cfelse>
								<cfinputClassic onKeyDown="checkTheBox(this)" type="text"
								name="physical_#mappings.currentrow#" value="#mappings.strphysical#" required="no"
								class="xlarge"
								message="#stText.customTags.PhysicalMissing##mappings.currentrow#)">
							</cfif>
						</td>
						
						<cfset css=iif(len(mappings.archive) EQ 0 and len(mappings.strArchive) NEQ 0,de('Red'),de(''))>
						<td class="tblContent#css# longwords">
							<cfif mappings.ReadOnly>
								#mappings.strarchive#
							<cfelse>
								<cfinputClassic onKeyDown="checkTheBox(this)" type="text"
								name="archive_#mappings.currentrow#" value="#mappings.strarchive#" required="no"
								class="xlarge"
								message="#stText.customTags.ArchiveMissing##mappings.currentrow#)">
							</cfif>
						</td>
						
						<td><cfif mappings.ReadOnly>
							<cfif mappings.PhysicalFirst>
									#stText.Mappings.Physical#
								<cfelse>
									#stText.Mappings.Archive#
								</cfif>
							<cfelse><select name="primary_#mappings.currentrow#" onchange="checkTheBox(this)">
							<option value="physical" <cfif mappings.physicalFirst>selected</cfif>>#stText.customTags.physical#</option>
							<option value="archive" <cfif not mappings.physicalFirst>selected</cfif>>#stText.customTags.archive#</option>
						</select></cfif></td>
						
						<td>
						<!--- inspect --->
						<cfif mappings.readOnly>
							<cfif len(mappings.inspect)>
								#stText.setting['inspecttemplate'&mappings.inspect&'Short']#
							<cfelse>
								#stText.setting['inspecttemplateInheritShort']#
							</cfif>
						
						
							<cfelse>
							<select name="inspect_#mappings.currentrow#" onchange="checkTheBox(this)">
							<cfloop list="never,once,always,inherit" item="type">
									<option value="#type EQ "inherit"?"":type#" <cfif mappings.inspect EQ type or (type EQ "inherit" and mappings.inspect EQ "")>selected</cfif>>
										#stText.setting['inspecttemplate#type#Short']#
									</option>
							</cfloop>
							</select>
							</cfif>
						
						</td>
						<!--- edit --->
						<td>
							<cfif not mappings.readOnly>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&virtual=#mappings.virtual#")#
							
							</cfif>
						</td>
					</tr>
				</cfloop>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="7" line>
				</cfif>
			</tbody>
			<tfoot>
				<cfif hasAccess>
					<tr>
						<td colspan="7">
							<input type="hidden" name="mainAction" value="#stText.Buttons.Update#">
							<input type="submit" class="bl button submit enablebutton" name="subAction" value="#stText.Buttons.Update#">
							<input type="reset" class="bm reset enablebutton" name="cancel" id="clickCancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit enablebutton" name="subAction" value="#stText.Buttons.Delete#">
						</td>	
					</tr>
				</cfif>
			</tfoot>
		</table>
	</cfformClassic>

	
	
<!--- NEW MAPPING --->	
	
		<cfif hasAccess>
		<h2>#stText.customTags.createnewcustomtagmapping#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="row_1" value="1">
			<input type="hidden" name="virtual_1" value="/#mappings.recordcount+1#">
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.customtags.name#</th>
						<td>
							<cfinputClassic type="text" name="name_1" value="" required="yes" class="large" message="#stText.customtags.nameMissing#">
							<div class="comment">#stText.customtags.nameDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.customtags.Physical#</th>
						<td>
							<cfinputClassic type="text" name="physical_1" value="" required="no" class="large">
							<div class="comment">#stText.customtags.PhysicalDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.customtags.Archive#</th>
						<td>
							<cfinputClassic type="text" name="archive_1" value="" required="no" class="large">
							<div class="comment">#stText.customtags.archiveDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.customtags.Primary#</th>
						<td>
							<select name="primary_1" class="medium">
								<option value="physical" selected>#stText.customtags.physical#</option>
								<option value="archive">#stText.customtags.archive#</option>
							</select>
							<div class="comment">#stText.customtags.primaryDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Mappings.TrustedHead#</th>
						<td>
							#stText.customtags.trustedDesc#
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
							
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="hidden" name="mainAction" value="#stText.Buttons.update#">
							<input type="hidden" name="subAction" value="#stText.Buttons.update#">
							<input type="submit" class="bs button submit" name="sdasd" value="#stText.Buttons.save#" />
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>
	
</cfoutput>