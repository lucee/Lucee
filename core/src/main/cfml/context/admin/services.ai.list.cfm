<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
						<cfadmin 
							action="removeAIConnection"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							name="#data.names[idx]#"
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
		<cfcase value="ping">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif arrayIndexExists(data.rows, idx) and data.names[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyAIConnection"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								name="#data.names[idx]#"
								returnVariable="qa">
								<cfset stVeritfyMessages["#data.names[idx]#"].Label = "OK">
								<cfset stVeritfyMessages["#data.names[idx]#"].qa = qa>
							<cfcatch>
								<!--- <cfset error.message=error.message&data.names[idx]&": "&cfcatch.message&"<br>"> --->
								<cfset stVeritfyMessages[data.names[idx]].Label = "Error">
								<cfset stVeritfyMessages[data.names[idx]].message = cfcatch.message>
							</cfcatch>
						</cftry>
					</cfif>
				</cfloop>
				
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq "ping">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
	
<cfset querySort(connections,"name")>

<cfoutput>

	<cfif access NEQ "yes">
		<cfset noAccess(stText.Settings.cache.noAccess)>
	</cfif>
	
	<!--- LIST CACHE --->
	<cfif connections.recordcount and access EQ "yes">
		<h1>#stText.Settings.ai.titleExisting#</h1>
		<div class="itemintro">#stText.Settings.ai.descExisting#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="1%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<th width="39%">#stText.Settings.cache.name#</th>
						<th width="40%">#stText.Settings.cache.type#</th>
						<th width="40%">#stText.Settings.ai.model#</th>
						<th width="40%">#stText.Settings.ai.default#</th>
						<th width="3%"></th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="connections">
						<cftry>
							<cfset driver=drivers[connections.class]>
							<cfcatch><cfcontinue></cfcatch>
						</cftry>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#connections.currentrow#" value="#connections.currentrow#">
							</td>
							<td nowrap><input type="hidden" name="name_#connections.currentrow#" value="#connections.name#">#connections.name#</td>
							<td nowrap>#driver.getLabel()#<cfif !isNull(connections.custom.type)> / #ucFirst(connections.custom.type?:"-")#</cfif></td>
							<td nowrap>#connections.custom.model?:"-"#</td>
							<td nowrap><cfif structKeyExists(sctDefaults, connections.default)> #stText.Settings.ai["defaultType"&connections.default]?:(connections.default?:"-")#</cfif></td>
							<td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&name=#Hash(connections.name)#")#
							</td>
						</tr>
					<cfif StructKeyExists(stVeritfyMessages, connections.name)>
						<tr>
							<td colspan="6" align="center">
									<cfif stVeritfyMessages[connections.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[connections.name].label#</span><br>
										<b>Question:</b> #stVeritfyMessages[connections.name].qa.question#<br>
										<b>Answer:</b> #stVeritfyMessages[connections.name].qa.answer?:""#
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[connections.name].message##Chr(13)#">#stVeritfyMessages[connections.name].label#</span><br>
											#stVeritfyMessages[connections.name].message# #stVeritfyMessages[connections.name].detail?:""#
									</cfif>
								
							</td>
						</tr>
					</cfif>



					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="6">
							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="ping">
							<input type="submit" class="bm button submit enablebutton" name="mainAction" value="#stText.Buttons.delete#">
							<input type="reset" class="br button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			 </table>
		</cfformClassic>
		
	</cfif>
</cfoutput>
<script>
function defaultValue(field) {
	var form=field.form;
	for(var i=0;i<form.elements.length;i++){
		var f=form.elements[i];
		if(f.name.substring(0,8)=='default_' && field.name!=f.name && f.value==field.value) {
			f.selectedIndex = 0;
		}
	}
}
</script>
<!--- select default cache
<cfif connections.recordcount and access EQ "yes">
	<cfoutput>	
		<h2>#stText.Settings.ai.defaultTitle#</h2>
		<div class="itemintro">#stText.Settings.ai.defaultDesc#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl">
				<tbody>
    				<cfloop item="type" array="#defaults#">
						<tr>
							<th scope="row">#stText.Settings.ai['defaulttype'& type]#</th>
							<td>
								<select name="default_#type#" class="small" onchange="defaultValue(this);">
									<option value="">------</option>
									<cfloop query="connections">
										<option value="#connections.name#" <cfif connections.default EQ type>selected="selected"</cfif>>#connections.name#</option>
									</cfloop>
								</select>
								<div class="comment">#stText.Settings.ai['defaulttype' &type& 'Desc']#</div>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="<cfif request.adminType EQ "web">bl<cfelse>bs</cfif> button submit" name="mainAction" value="#stText.Buttons.update#">
							<cfif not request.singleMode and request.adminType EQ "web">
								<input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
		
	</cfoutput>
</cfif> --->

<!--- 
	Create Datasource --->
<cfif access EQ "yes">
	<cfoutput>
		<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
		<cfif listLen(_drivers)>
			<h1>#stText.Settings.cache.titleCreate#</h1>
			<div class="itemintro">#stText.Settings.ai.descCreate#</div>
			
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl" style="width:600px;">
					<tbody>
						<tr>
							<th scope="row" nowrap="nowrap">#stText.Settings.cache.Name#</th>
							<td><cfinputClassic type="text" name="_name" value="" class="xlarge" required="yes" 
								message="#stText.Settings.cache.nameMissing#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.cache.type#</th>
							<td>
								<select name="class" class="xlarge">
									<cfloop list="#_drivers#" index="key">
										<cfset driver=drivers[key]>
										<!--- Workaround for EHCache Extension --->
										<cfset clazz=trim(driver.getClass())>
										<cfif "lucee.extension.io.cache.eh.EHCache" EQ clazz or "lucee.runtime.cache.eh.EHCache" EQ clazz>
											<cfset clazz="org.lucee.extension.cache.eh.EHCache">
										</cfif>
										<option value="#clazz#">#trim(driver.getLabel())#</option>
									</cfloop>
								</select>
								<div class="comment">#stText.Settings.cache.typeDesc#</div>
							</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="bl button submit" name="run" value="#stText.Buttons.create#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>   
			</cfformClassic>
		<cfelse>
			<div class="text">#stText.Settings.cache.noDriver#</div>
		</cfif>
	</cfoutput>
</cfif>
