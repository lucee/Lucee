
<cfset stText.CustomTags.name="Name">
<cfset stText.CustomTags.nameMissing="Missing the name for the new Mapping, this name is used when you deploy a Lucee Archive (.ra,.ras) based on this Mapping.">
<cfset stText.CustomTags.nameDesc="The name is used as identifier when you automaticly import a Lucee Archive build based on this Mapping.">
<cfset stText.CustomTags.PhysicalDesc="Directory path where the custom tags are located.">
<cfset stText.CustomTags.archiveDesc="File path to a custom tag Lucee Archive (.ra or .ras).">
<cfset stText.CustomTags.PrimaryDesc="Defines where Lucee does looks first for a requested custom tags">
<cfset stText.CustomTags.trustedDesc="When does Lucee checks for changes in the source file for a already loaded custom tags">


<!--- <cfif isDefined("form")>
	<cfinclude template="act/resources.act_mapping.cfm">
</cfif> --->
<cfset error.message="">
<cfset error.detail="">
<cfscript>
	function arrayRemoveValue(arr,value){
		var index=arrayFindNoCase(arr,value);
		if(index GT 0)ArrayDeleteAt(arr,index);
	}
</cfscript>

<!--- Defaults --->
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">

<!--- <cfset hasAccess=securityManager.getAccess("custom_tag") EQ ACCESS.YES> --->
<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="custom_tag"
	secValue="yes">


<cfset flushName="#stText.Buttons.flush# (#structCount(ctCacheList())#)">
<cftry>
	<cfswitch expression="#form.mainAction#">
		<!--- UPDATE --->
		<cfcase value="#flushName#">
			 <cfset ctCacheClear()>
		</cfcase>
		<!--- update --->
		<cfcase value="#stText.Buttons.Update#">
			<cfif form.subAction EQ "setting">
				<cfif form.extensions EQ "custom">
					<cfset form.extensions=form.extensions_custom>
				</cfif>
			
				<cfadmin 
						action="updateCustomTagSetting"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						deepSearch="#isDefined('form.customTagDeepSearchDesc') and form.customTagDeepSearchDesc EQ true#"
						localSearch="#isDefined('form.customTagLocalSearchDesc') and form.customTagLocalSearchDesc EQ true#"
						customTagPathCache="#isDefined('form.customTagPathCache') and form.customTagPathCache EQ true#"
						
						
						
						extensions="#form.extensions#"
			remoteClients="#request.getRemoteClients()#">
			<cfelseif form.subAction EQ "#stText.Buttons.Update#">
				<cfset data.names=toArrayFromForm("name")>
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.physicals=toArrayFromForm("physical")>
				<cfset data.archives=toArrayFromForm("archive")>
				<cfset data.primaries=toArrayFromForm("primary")>
				<cfset data.inspects=toArrayFromForm("inspect")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.physicals)#">
					<cfif isDefined("data.rows[#idx#]")>
						<cfset data.inspects[idx]=isDefined("data.inspects[#idx#]")?data.inspects[idx]:"">
					
					<cfset name=data.names[idx]?:"">
					<cfset virtual=trim(data.virtuals[idx]?:"")>
					<cfif len(name)>
						<cfset virtual="/"&name>
					</cfif>
					
					<cfadmin 
						action="updateCustomTag"
						type="#request.adminType#"
						password="#session["password"&request.adminType]#"
						
						virtual="#virtual#"
						physical="#data.physicals[idx]#"
						archive="#data.archives[idx]#"
						primary="#data.primaries[idx]#"
						inspect="#data.inspects[idx]#"
			remoteClients="#request.getRemoteClients()#">

					</cfif>
				</cfloop>
		
			<cfelseif form.subAction EQ "#stText.Buttons.Delete#">
				<cfset data.virtuals=toArrayFromForm("virtual")>
				<cfset data.rows=toArrayFromForm("row")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
					
					<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">
						<cfadmin 
							action="removeCustomTag"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							
							virtual="#data.virtuals[idx]#"
			remoteClients="#request.getRemoteClients()#">
					
						<!--- <cfset admin.removeCustomTag(data.virtuals[idx])> --->
					</cfif>
				</cfloop>
			</cfif>
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>

<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<cfadmin 
	action="getCustomTagMappings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mappings">
	
	

<cfadmin 
	action="getCustomtagSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="setting">

<cfoutput>
	<!--- Error Output--->
	<cfif error.message NEQ "">
		<div class="error">
			#error.message#<br>
			#error.detail#
		</div>
	</cfif>

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
				<!--- component path cache ---->
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
							struct(mode:'classic',ext:'cfm'),
							struct(mode:'standard',ext:'cfm,cfml'),
							struct(mode:'mixed',ext:'cfm,cfc'),
							struct(mode:'modern',ext:'cfc')
						
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
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
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
	</cfformClassic>
</cfoutput>