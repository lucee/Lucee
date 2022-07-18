<cfoutput>
	<div class="pageintro">
		<cfif request.adminType EQ "server">
			#stText.Components.Server#
		<cfelse>
			#stText.Components.Web#
		</cfif>
	</div>
	
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- Base Component ---->
				
				<tr>
					<th scope="row">#stText.Components.BaseComponent# </th>
<cfset css=iif(len(component["baseComponentTemplateCFML"]) EQ 0 and len(component["strBaseComponentTemplateCFML"]) NEQ 0,de('Red'),de(''))>
					<td class="tblContent#css#" title="#component["strBaseComponentTemplateCFML"]#
#component["BaseComponentTemplateCFML"]#">
						


						<!---<table class="maintbl">
						<cfloop list="CFML,Lucee" item="dialect">
					<cfset css=iif(len(component["baseComponentTemplate"&dialect]) EQ 0 and
					 len(component["strBaseComponentTemplate"&dialect]) NEQ 0,de('Red'),de(''))>
							<tr>
							<th scope="row">Dialect #dialect#</th>
							<td class="tblContent#css#" title="#component["strBaseComponentTemplate"&dialect]#
#component["BaseComponentTemplate"&dialect]#">
							<cfif hasAccess>
								<cfinputClassic type="text" name="baseComponentTemplate#dialect#" value="#component["strBaseComponentTemplate"&dialect]#" style="width:350px" 
									required="no" 
									message="#stText.Components.BaseComponentMissing#">
							<cfelse>
								<b>#component["strBaseComponentTemplate"&dialect]#</b>
							</cfif>
							</td>
							</tr>
						</cfloop>--->


							<cfif hasAccess>
								<cfinputClassic type="text" name="baseComponentTemplateCFML" value="#component["strBaseComponentTemplateCFML"]#" style="width:350px" 
									required="no" 
									message="#stText.Components.BaseComponentMissing#">
							<cfelse>
								<b>#component["strBaseComponentTemplateCFML"]#</b>
							</cfif>
							
						<cfoutput><input type="hidden" name="baseComponentTemplateLucee" value="#component["strBaseComponentTemplateLucee"]#" ></cfoutput>


						<div class="comment">#stText.Components.BaseComponentDescription#</div>
					</td>
				</tr>
				<!--- Auto Import ---->
				<tr>
					<th scope="row">#stText.Components.AutoImport#</th>
					<td>
						<cfif hasAccess>
							<cfinputClassic type="text" name="componentDefaultImport" value="#component.componentDefaultImport#" style="width:350px" 
								required="no" 
								message="#stText.Components.AutoImportMissing#">
						<cfelse>
							<b>#component.componentDefaultImport#</b>
						</cfif>
						<div class="comment">#stText.Components.AutoImportDescription#</div>
					</td>
				</tr>
				<!--- Search Local ---->
				<tr>
					<th scope="row">#stText.Components.componentLocalSearch#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="componentLocalSearch" value="yes" <cfif component.componentLocalSearch>checked</cfif>>
						<cfelse>
							<b>#YesNoFormat(component.componentLocalSearch)#</b>
						</cfif>
						<div class="comment">#stText.Components.componentLocalSearchDesc#</div>
					</td>
				</tr>
				<!--- Search Mappings ---->
				<tr>
					<th scope="row">#stText.Components.componentMappingSearch#</th>
					<td>
						<b>Yes (coming soon)</b>
						<div class="comment">#stText.Components.componentMappingSearchDesc#</div>
					</td>
				</tr>
				<!--- Deep Search ---->
				<tr>
					<th scope="row">#stText.Components.componentDeepSearch#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="componentDeepSearchDesc" value="yes" <cfif component.deepsearch>checked</cfif>>
						<cfelse>
							<b>#yesNoFormat(component.deepsearch)#</b>
						</cfif>
						<div class="comment">#stText.Components.componentDeepSearchDesc#</div>
					</td>
				</tr>
				<!--- component path cache ---->
				<tr>
					<th scope="row">#stText.Components.componentPathCache#</th>
					<td>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="componentPathCache" value="yes" <cfif component.componentPathCache>checked</cfif>>
							<div class="comment">#stText.Components.componentPathCacheDesc#</div>
							<cfif component.componentPathCache>
								<input type="submit" class="button submit" name="mainAction" value="#flushName#">
							</cfif>
						<cfelse>
							<b>#YesNoFormat(component.componentPathCache)#</b>
							<div class="comment">#stText.Components.componentPathCacheDesc#</div>
						</cfif>
					</td>
				</tr>
				<!--- Component Dump Template ---->
				<tr>
					<th scope="row">#stText.Components.ComponentDumpTemplate#</th>
					<cfset css=iif(len(component.componentDumpTemplate) EQ 0 and len(component.strComponentDumpTemplate) NEQ 0,de('Red'),de(''))>
					<td class="tblContent#css#" title="#component.strcomponentDumpTemplate#
#component.componentDumpTemplate#">
						<cfif hasAccess>
							<cfinputClassic type="text" name="componentDumpTemplate" value="#component.strcomponentDumpTemplate#" class="large"
								required="no" 
								message="#stText.Components.ComponentDumpTemplateMissing#">
						<cfelse>
							<b>#component.strcomponentDumpTemplate#</b>
						</cfif>
						<cfset _url="#findNoCase("https",cgi.server_protocol)?"https":"http"#://#cgi.http_host##cgi.context_path#/lucee/Admin.cfc">
						<div class="comment">#replace(stText.Components.ComponentDumpTemplateDescription,'{url}',"<a href=""#_url#"">#_url#</a>",'all')#</div>
					</td>
				</tr>
<!---			
			</tbody>
		</table>

		<h3>#stText.general.dialect.cfml#</h3>
		<div class="itemintro">#stText.general.dialect.cfmlDesc#</div>
		
		<table class="maintbl">
			<tbody>
--->
				<!--- Data Member Access Type --->
				<tr>
					<th scope="row">#stText.Components.DataMemberAccessType#</th>
					<td>
						<cfset access=component.componentDataMemberDefaultAccess>
						<cfif hasAccess>
							<select name="componentDataMemberDefaultAccess" class="medium">
								<option value="private" <cfif access EQ "private">selected</cfif>>#stText.Components.DMATPrivate#</option>
								<option value="package" <cfif access EQ "package">selected</cfif>>#stText.Components.DMATPackage#</option>
								<option value="public" <cfif access EQ "public">selected</cfif>>#stText.Components.DMATPublic#</option>
								<option value="remote" <cfif access EQ "remote">selected</cfif>>#stText.Components.DMATRemote#</option>
							</select>
						<cfelse>
							<b>#access#</b>
						</cfif>
						<div class="comment">#stText.Components.DataMemberAccessTypeDescription#</div>
					</td>
				</tr>
				<!---
				Trigger Data Member --->
				<tr>
					<th scope="row">#stText.Components.triggerDataMember#</th>
					<td>
						<cfif hasAccess>
							<input class="checkbox" type="checkbox" class="checkbox" name="triggerDataMember" 
							value="yes" <cfif component.triggerDataMember>checked</cfif>>
						<cfelse>
							<b>#iif(component.triggerDataMember,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Components.triggerDataMemberDescription#</div>
						<!--- Tip --->
						<cfset renderCodingTip( "this.invokeImplicitAccessor = "&component.triggerDataMember&";" )>
					</td>
				</tr>
				<!---
				Use Shadow --->
				<tr>
					<th scope="row">#stText.Components.useShadow#</th>
					<td>
						<cfif hasAccess>
							<input class="checkbox" type="checkbox" class="checkbox" name="useShadow" 
							value="yes" <cfif component.useShadow>checked</cfif>>
						<cfelse>
							<b>#iif(component.useShadow,de('Yes'),de('No'))#</b>
						</cfif>
						<div class="comment">#stText.Components.useShadowDescription#</div>
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
							<input class="bl button submit" type="submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
	
	<h2>#stText.Components.componentMappings#</h2>
	<div class="itemintro">#stText.Components.componentMappingsDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th>
						<cfif hasAccess>
							<input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)">
						</cfif>
					</th>
					<th>#stText.Components.name#</th>
					<th style="width:25%">#stText.Components.Physical#</th>
					<th style="width:25%">#stText.Components.Archive#</th>
					<th>#stText.Components.Primary#</th>
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
								message="#stText.Components.PhysicalMissing##mappings.currentrow#)">
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
								message="#stText.Components.ArchiveMissing##mappings.currentrow#)">
							</cfif>
						</td>
						
						<td><cfif mappings.ReadOnly>
							<cfif mappings.PhysicalFirst>
									#stText.Mappings.Physical#
								<cfelse>
									#stText.Mappings.Archive#
								</cfif>
							<cfelse><select name="primary_#mappings.currentrow#" onchange="checkTheBox(this)">
							<option value="physical" <cfif mappings.physicalFirst>selected</cfif>>#stText.Components.physical#</option>
							<option value="archive" <cfif not mappings.physicalFirst>selected</cfif>>#stText.Components.archive#</option>
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
							<input type="submit" class="bl button submit" name="subAction" value="#stText.Buttons.Update#">
							<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit" name="subAction" value="#stText.Buttons.Delete#">
						</td>	
					</tr>
				</cfif>
			</tfoot>
		</table>
	</cfformClassic>

	<cfif hasAccess>
		<h2>#stText.components.createnewcompmapping#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="row_1" value="1">
			<input type="hidden" name="virtual_1" value="/#mappings.recordcount+1#">
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.Components.name#</th>
						<td>
							<cfinputClassic type="text" name="name_1" value="" required="yes" class="large" message="#stText.Components.nameMissing#">
							<div class="comment">#stText.Components.nameDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Components.Physical#</th>
						<td>
							<cfinputClassic type="text" name="physical_1" value="" required="no" class="large">
							<div class="comment">#stText.Components.PhysicalDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Components.Archive#</th>
						<td>
							<cfinputClassic type="text" name="archive_1" value="" required="no" class="large">
							<div class="comment">#stText.Components.archiveDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Components.Primary#</th>
						<td>
							<select name="primary_1" class="medium">
								<option value="physical" selected>#stText.Components.physical#</option>
								<option value="archive">#stText.Components.archive#</option>
							</select>
							<div class="comment">#stText.Components.primaryDesc#</div>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.Mappings.TrustedHead#</th>
						<td>
							#stText.Components.trustedDesc#
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