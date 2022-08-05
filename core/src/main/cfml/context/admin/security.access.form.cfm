<cfparam name="stVeritfyMessages" default="#struct()#">

<cffunction name="def" returntype="string" output="false">
	<cfargument name="key" type="string">
	<cfreturn "">
	<cfif not structKeyExists(variables,"daccess")>
		<cfreturn "">
	<cfelseif variables.daccess[key]>
		<cfreturn "Green">
	</cfif>
		<cfreturn "Red">
</cffunction>

<cfset values=struct(
	'open':"open",
	'protected':"password protected",
	'close':"closed"
)>

<cfoutput>
	<h2>#stText.Security.general#</h2>
	<div class="itemintro">#stText.Security.generalDesc#</div>
	<cfformClassic onerror="customError" action="#go(url.action,"update#iif(type EQ "generell",de('Default'),de(''))#SecurityManager")#" method="post">
		<table class="maintbl">
			<tbody>
				<!--- Access Read --->
				<tr>
					<th scope="row">#stText.Security.accessRead#</th>
					<td class="tblContent#def('access_read')#">
						<div class="comment">#stText.Security.accessReadDesc#</div>
						
						<select name="#prefix#access_read">
							<cfoutput><cfloop collection="#values#" item="idx" ><option value="#idx#" <cfif access.access_read eq idx>selected="selected"</cfif>><cfif structKeyExists(stText.Security.DatasourceTextes,idx)>#stText.Security.DatasourceTextes[idx]#<cfelse>#values[idx]#</cfif></option></cfloop></cfoutput>
						</select>
						<!--- input type="checkbox" class="checkbox" name="#prefix#Datasource" value="yes" <cfif access.datasource>checked</cfif> --->
						
					</td>
				</tr>
				
				
				<!--- Access Write --->
				<tr>
					<th scope="row">#stText.Security.accessWrite#</th>
					<td class="tblContent#def('access_write')#">
						<div class="comment">#stText.Security.accessWriteDesc#</div>
						<select name="#prefix#access_write">
							<cfoutput><cfloop collection="#values#" item="idx" ><option value="#idx#" <cfif access.access_write eq idx>selected="selected"</cfif>><cfif structKeyExists(stText.Security.DatasourceTextes,idx)>#stText.Security.DatasourceTextes[idx]#<cfelse>#values[idx]#</cfif></option></cfloop></cfoutput>
						</select>
						<!--- input type="checkbox" class="checkbox" name="#prefix#Datasource" value="yes" <cfif access.datasource>checked</cfif> --->
						
					</td>
				</tr>
			</tbody>
		</table>

<!--- <cfformClassic onerror="customError" action="#go(url.action,"update#iif(type EQ "generell",de('Default'),de(''))#SecurityManager")#" method="post">--->
		<h2>#stText.Security.WebAdministrator#</h2>
		<div class="itemintro">#stText.Security.WebAdministratorDescription#</div>
		<table class="maintbl">
			<tbody>
				<!--- Setting --->
				<tr>
					<th scope="row">#stText.Security.Settings#</th>
					<td class="tblContent#def('setting')#">
						<input type="checkbox" class="checkbox" name="#prefix#Setting" value="yes" <cfif access.setting>checked</cfif>>
						<div class="comment">#stText.Security.SettingsDescription#</div>
					</td>
				</tr>
				
				<!--- Mail --->
				<tr>
					<th scope="row">#stText.Security.Mail#</th>
					<td class="tblContent#def('mail')#">
						<input type="checkbox" class="checkbox" name="#prefix#Mail" value="yes" <cfif access.mail>checked</cfif>>
						<div class="comment">#stText.Security.MailDescription#</div>
					</td>
				</tr> 
				
				<!--- Datasource --->
				<tr>
					<th scope="row">#stText.Security.Datasource#</th>
					<td class="tblContent#def('datasource')#">
						<div class="comment">#stText.Security.DatasourceDescription#</div>
						<cfset values=struct(
							'-1':"yes",
							'0':"no",
							'1':"1",
							'2':"2",
							'3':"3",
							'4':"4",
							'5':"5",
							'6':"6",
							'7':"7",
							'8':"8",
							'9':"9",
							'10':"10"
						)>
						<select name="#prefix#Datasource">
							<cfoutput><cfloop index="idx" from="-1" to="10" ><option value="#values[idx]#" <cfif access.datasource eq idx>selected="selected"</cfif>><cfif structKeyExists(stText.Security.DatasourceTextes,idx)>#stText.Security.DatasourceTextes[idx]#<cfelse>#idx#</cfif></option></cfloop></cfoutput>
						</select>
						<!--- input type="checkbox" class="checkbox" name="#prefix#Datasource" value="yes" <cfif access.datasource>checked</cfif> --->
						
					</td>
				</tr>
				
				<!--- Mapping --->
				<tr>
					<th scope="row">#stText.Security.Mapping#</th>
					<td class="tblContent#def('mapping')#">
						<input type="checkbox" class="checkbox" name="#prefix#Mapping" value="yes" <cfif access.mapping>checked</cfif>>
						<div class="comment">#stText.Security.MappingDescription#</div>
					</td>
				</tr>
				
				<!--- Remote --->
				<tr>
					<th scope="row">#stText.Security.Remote#</th>
					<td class="tblContent#def('remote')#">
						<input type="checkbox" class="checkbox" name="#prefix#Remote" value="yes" <cfif access.remote>checked</cfif>>
						<div class="comment">#stText.Security.RemoteDescription#</div>
					</td>
				</tr>
				<!--- CustomTag --->
				<tr>
					<th scope="row">#stText.Security.CustomTag#</th>
					<td class="tblContent#def('custom_tag')#">
						<input type="checkbox" class="checkbox" name="#prefix#CustomTag" value="yes" <cfif access.custom_tag>checked</cfif>>
						<div class="comment">#stText.Security.CustomTagDescription#</div>
					</td>
				</tr>
				
				<!--- CFX Setting --->
				<tr>
					<th scope="row">#stText.Security.CFX#</th>
					<td class="tblContent#def('cfx_setting')#">
						<input type="checkbox" class="checkbox" name="#prefix#CFXSetting" value="yes" <cfif access.cfx_setting>checked</cfif>>
						<div class="comment">#stText.Security.CFXDescription#</div>
					</td>
				</tr>
				
				<!--- Cache --->
				<tr>
					<th scope="row">#stText.Security.Cache#</th>
					<td class="tblContent#def('cache')#">
						<input type="checkbox" class="checkbox" name="#prefix#Cache" value="yes" <cfif access.cache>checked</cfif>>
						<div class="comment">#stText.Security.CacheDescription#</div>
					</td>
				</tr>
				
				<!--- Gateway --->
				<tr>
					<th scope="row">#stText.Security.Gateway#</th>
					<td class="tblContent#def('gateway')#">
						<input type="checkbox" class="checkbox" name="#prefix#Gateway" value="yes" <cfif access.gateway>checked</cfif>>
						<div class="comment">#stText.Security.GatewayDescription#</div>
					</td>
				</tr>
				<!--- ORM --->
				<tr>
					<th scope="row">#stText.Security.orm#</th>
					<td class="tblContent#def('orm')#">
						<input type="checkbox" class="checkbox" name="#prefix#Orm" value="yes" <cfif access.orm>checked</cfif>>
						<div class="comment">#stText.Security.ormDescription#</div>
					</td>
				</tr>
				<!--- Debugging --->
				<tr>
					<th scope="row">#stText.Security.Debugging#</th>
					<td class="tblContent#def('debugging')#">
						<input type="checkbox" class="checkbox" name="#prefix#Debugging" value="yes" <cfif access.debugging>checked</cfif>>
						<div class="comment">#stText.Security.DebuggingDescription#</div>
					</td>
				</tr>
				<!--- Search
				<tr>
					<th scope="row">#stText.security.search#</th>
					<td style="#def('search')#">
						<input type="checkbox" class="checkbox" name="#prefix#Search" value="yes" <cfif access.search>checked</cfif>>
						<span class="comment">#stText.Security.SearchDescription#</span>
					</td>
				</tr> --->
				<input type="hidden" name="#prefix#Search" value="yes">
				<!--- Scheduled Task 
				<tr>
					<th scope="row">#stText.Security.ScheduledTask#</th>
					<td style="#def('scheduled_task')#">
						<input type="checkbox" class="checkbox" name="#prefix#ScheduledTask" value="yes" <cfif access.scheduled_task>checked</cfif>>
						<span class="comment">#stText.Security.ScheduledTaskDescription#</span>
					</td>
				</tr>
				--->
			</tbody>
		</table>

		<input type="hidden" name="#prefix#ScheduledTask" value="yes">
		
		<h2>#stText.Security.CFMLEnvironment#</h2>
		<div class="itemintro">#stText.Security.CFMLEnvironmentDescription#</div>
		<table class="maintbl">
			<tbody>
				<!--- File --->
				<tr>
					<th scope="row">#stText.Security.File#</th>
					<td>
						<div class="comment">#stText.Security.FileDescription#</div>
						<select name="#prefix#File" onchange="changeFileAccessVisibility('fileAccess',this)">
							<option value="all" <cfif access.file EQ "all">selected</cfif>>#stText.Security.FileAll#</option>
							<option value="local" <cfif access.file EQ "local">selected</cfif>>#stText.Security.FileLocal#</option>
							<option value="none" <cfif access.file EQ "none">selected</cfif>>#stText.Security.FileNone#</option>
						</select>
						<script type="text/javascript">
							function changeFileAccessVisibility(name,field){
								var display=0;
								if(field){
									display=field.value!='local'?1:2;
								}
								var tds=document.all?document.getElementsByTagName('tr'):document.getElementsByName(name);
								var s=null;
								for(var i=0;i<tds.length;i++) {
									if(document.all && tds[i].name!=name)continue;
									s=tds[i].style;
									if(display==1) s.display='none';
									else if(display==2) s.display='';
									else if(s.display=='none') s.display='';
									else s.display='none';
								}
							}
						</script>
						<table class="tbl">
							<tbody id="fileAccessBody">
								<tr name="fileAccess" style="display:#access.file EQ 'local'?'':'none'#">
									<td colspan="5">#stText.Security.FileCustom#
										<div class="comment">#stText.Security.FileCustomDesc#</div>
									</td>
								</tr>
								<tr name="fileAccess" style="display:#access.file EQ 'local'?'':'none'#">
									<td width="350" class="tblHead" nowrap>#stText.Security.FilePath#</td>
								</tr>
								<cfloop index="idx" from="1" to="#arrayLen(access.file_access)#">
									<tr name="fileAccess" style="display:#access.file EQ 'local'?'':'none'#">
										<!--- path --->
										<td nowrap>
											<cfinputClassic type="text" name="path_#idx#" 
											value="#access.file_access[idx]#" required="no" class="large">
										</td>
									</tr>
								</cfloop>
								<!--- INSERT --->
								<tr name="fileAccess" style="display:#access.file EQ 'local'?'':'none'#">
									<td nowrap><cfinputClassic type="text" name="path_#arrayLen(access.file_access)+1#" value="" required="no" class="large"> <input type="button" name="addFileAccessDirectory" class="addFileAccessDirectory button" data-index="#arrayLen(access.file_access)+1#" value="Add"></td>
								</tr>
							</tbody>
						</table>
					</td>
				</tr>
				<!--- Direct Java Access --->
				<tr>
					<th scope="row">#stText.Security.JavaAccess#</th>
					<td class="tblContent#def('direct_java_access')#">
						<input type="checkbox" class="checkbox" name="#prefix#DirectJavaAccess" value="yes" <cfif access.direct_java_access>checked</cfif>>
						<div class="comment">#stText.Security.JavaAccessDescription#</div>
					</td>
				</tr>
				
				
				<!--- CFX Usage 
				<tr>
					<th scope="row">CFX Usage</th>
					<td>
						<input type="checkbox" class="checkbox" name="#prefix#CFXUsage" value="yes" <cfif access.cfx_usage>checked</cfif>>
						<span class="comment">Enable or disable the CFX functionality for the instaces</span>
					</td>
				</tr>--->
			</tbody>
		</table>
		
		<!--- This code allows you to add multiple file access directories in one request.   Otherwise you can prevent Lucee from functioning by accident, forcing the developer to modify the XML directly and restart Lucee to fix the problem.  --->
		<cfsavecontent variable="fileAccessDirectoryTemplate">
			<tr name="fileAccess">
				<td nowrap><cfinputClassic type="text" name="{FIELDNAME}" value="" required="no" class="large"> <input type="button" name="addFileAccessDirectory" class="addFileAccessDirectory button" value="Add" data-index="{INDEX}" onclick="this.style.display='none';"></td>
			</tr>
		</cfsavecontent>
		<input type="hidden" name="fileAccessDirectoryTemplate" id="fileAccessDirectoryTemplate" value="#htmleditformat(fileAccessDirectoryTemplate)#">
		
		
		
		<h2>#stText.Security.Functions#</h2>
		<div class="itemintro">#stText.Security.FunctionsDescription#</div>
		<table class="maintbl">
			<tbody>
				<!--- Tags --->
				<tr>
					<!--- Execute --->
					<th scope="row">#stText.Security.TagExecute#</th>
					<td class="tblContent#def('tag_execute')#">
						<input type="checkbox" class="checkbox" name="#prefix#TagExecute" value="yes" <cfif access.tag_execute>checked</cfif>>
						<div class="comment">#stText.Security.TagExecuteDescription#</div>
					</td>
				</tr>
						<!--- Import --->
				<tr>
					<th scope="row">#stText.Security.TagImport#</th>
					<td class="tblContent#def('tag_import')#">
						<input type="checkbox" class="checkbox" name="#prefix#TagImport" value="yes" <cfif access.tag_import>checked</cfif>>
						<div class="comment">#stText.Security.TagImportDescription#</div>
					</td>
				</tr>
						<!--- Object --->
				<tr>
					<th scope="row">#stText.Security.TagObject#</th>
					<td class="tblContent#def('tag_object')#">
						<input type="checkbox" class="checkbox" name="#prefix#TagObject" value="yes" <cfif access.tag_object>checked</cfif>>
						<div class="comment">#stText.Security.TagObjectDescription#</div>
					</td>
				</tr>
						<!--- Registry --->
				<tr>
					<th scope="row">#stText.Security.TagRegistry#</th>
					<td class="tblContent#def('tag_registry')#">
						<input type="checkbox" class="checkbox" name="#prefix#TagRegistry" value="yes" <cfif access.tag_registry>checked</cfif>>
						<div class="comment">#stText.Security.TagRegistryDescription#</div>
					</td>
				</tr>
						<!--- CFX --->
				<tr>
					<th scope="row">#stText.Security.CFXTags#</th>
					<td class="tblContent#def('cfx_usage')#">
						<input type="checkbox" class="checkbox" name="#prefix#CFXUsage" value="yes" <cfif access.cfx_usage>checked</cfif>>
						<div class="comment">#stText.Security.CFXTagsDescription#</div>
					</td>
				</tr>
				<cfif type NEQ "special">
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="hidden" name="mainAction" value="#prefix#Udpate">
						<input type="submit" class="button submit" name="subAction" value="#stText.Buttons.Update#">
						<input onclick="window.location='#go(url.action)#';" type="button" class="button" name="cancel" value="#stText.Buttons.Cancel#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>