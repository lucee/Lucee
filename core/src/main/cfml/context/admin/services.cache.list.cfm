<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
			<cfadmin 
				action="removeCacheDefaultConnection"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				remoteClients="#request.getRemoteClients()#">				
		</cfcase>
		<cfcase value="#stText.Buttons.update#">
            <cfadmin 
                action="updateCacheDefaultConnection"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                object="#StructKeyExists(form,'default_object')?form.default_object:''#"
                template="#StructKeyExists(form,'default_template')?form.default_template:''#"
                query="#StructKeyExists(form,'default_query')?form.default_query:''#"
                resource="#StructKeyExists(form,'default_resource')?form.default_resource:''#"
                function="#StructKeyExists(form,'default_function')?form.default_function:''#"
                include="#StructKeyExists(form,'default_include')?form.default_include:''#"
                http="#StructKeyExists(form,'default_http')?form.default_http:''#"
                file="#StructKeyExists(form,'default_file')?form.default_file:''#"
                webservice="#StructKeyExists(form,'default_webservice')?form.default_webservice:''#"
                remoteClients="#request.getRemoteClients()#">				
		</cfcase>
	<!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cfadmin 
							action="removeCacheConnection"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							name="#data.names[idx]#"
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.verify#">
			<cfset data.names=toArrayFromForm("name")>
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.names=toArrayFromForm("name")>
				
				<cfloop index="idx" from="1" to="#arrayLen(data.names)#">
					<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
						<cftry>
							<cfadmin 
								action="verifyCacheConnection"
								type="#request.adminType#"
								password="#session["password"&request.adminType]#"
								name="#data.names[idx]#">
								<cfset stVeritfyMessages["#data.names[idx]#"].Label = "OK">
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
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
	
<cfset querySort(connections,"name")>
<cfset srcLocal=queryNew("name,class,custom,default,readonly,storage")>
<cfset srcGlobal=queryNew("name,class,custom,default,readonly,storage")>

<cfloop query="connections">
	<cfif not connections.readOnly>
		<cfset tmp=srcLocal>
	<cfelse>
		<cfset tmp=srcGlobal>
	</cfif>
	<cfset QueryAddRow(tmp)>
	<cfset QuerySetCell(tmp,"name",connections.name)>
	<cfset QuerySetCell(tmp,"class",connections.class)>
	<cfset QuerySetCell(tmp,"custom",connections.custom)>
	<cfset QuerySetCell(tmp,"default",connections.default)>
	<cfset QuerySetCell(tmp,"storage",connections.storage)>
	<cfset QuerySetCell(tmp,"readonly",connections.readonly)>
</cfloop>
<cfset querySort(connections,"default")>

<cfoutput>

	<cfif access NEQ "yes">
		<cfset noAccess(stText.Settings.cache.noAccess)>
	</cfif>
	<!---- READ ONLY ---->
	<cfif request.adminType EQ "web" and srcGlobal.recordcount>
		<h2>#stText.Settings.cache.titleReadOnly#</h2>
		<div class="itemintro">#stText.Settings.cache.descReadOnly#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<cfif access EQ "yes">
							<th width="1%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						</cfif>
						<th width="39%">#stText.Settings.cache.name#</th>
						<th width="40%"># stText.Settings.cache.type#</th>
						<th width="10%"># stText.Settings.cache.storage#</th>
						<th width="10%">#stText.Settings.DBCheck#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcGlobal">
						<cfset driver=drivers[srcGlobal.class]>
						<tr>
							<cfif access EQ "yes">
								<td>
									<input type="checkbox" class="checkbox" name="row_#srcGlobal.currentrow#" value="#srcGlobal.currentrow#">
								</td>
							</cfif>
							<td>#srcGlobal.name#
								<input type="hidden" name="name_#srcGlobal.currentrow#" value="#srcGlobal.name#">
							</td>
							<td>#driver.getLabel()#</td>
							<td>#yesNoFormat(srcGlobal.storage)#</td>
							<td class="center">
								<cfif StructKeyExists(stVeritfyMessages, srcGlobal.name)>
									<cfif stVeritfyMessages[srcGlobal.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcGlobal.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcGlobal.name].message##Chr(13)#">#stVeritfyMessages[srcGlobal.name].label#</span>
										&nbsp;<img src="../res/img/red-info.gif.cfm" 
											width="9" 
											height="9" 
											border="0" 
											title="#stVeritfyMessages[srcGlobal.name].message##Chr(13)#">
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<cfif access EQ "yes">
					<tfoot>
						<tr>
							<td colspan="4">
								<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.verify#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>	
						</tr>
					</tfoot>
				</cfif>
			</table>
		</cfformClassic>
		</cfif>
		
	<!--- LIST CACHE --->
	<cfif srcLocal.recordcount and access EQ "yes">
		<h2>#stText.Settings.cache.titleExisting#</h2>
		<div class="itemintro">#stText.Settings.cache.descExisting#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="1%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<th width="39%">#stText.Settings.cache.name#</th>
						<th width="40%"># stText.Settings.cache.type#</th>
						<th width="10%"># stText.Settings.cache.storage#</th>
						<th width="7%">#stText.Settings.DBCheck#</th>
						<th width="3%"></th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcLocal">
						<cftry>
							<cfset driver=drivers[srcLocal.class]>
							<cfcatch><cfcontinue></cfcatch>
						</cftry>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#srcLocal.currentrow#" value="#srcLocal.currentrow#">
							</td>
							<td nowrap><input type="hidden" name="name_#srcLocal.currentrow#" value="#srcLocal.name#">#srcLocal.name#</td>
							<td nowrap>#driver.getLabel()#</td>
							<td nowrap>#yesNoFormat(srcLocal.storage)#</td>
							<td nowrap valign="middle" align="center">
								<cfif StructKeyExists(stVeritfyMessages, srcLocal.name)>
									<cfif stVeritfyMessages[srcLocal.name].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcLocal.name].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcLocal.name].message##Chr(13)#">#stVeritfyMessages[srcLocal.name].label#</span>
										&nbsp;<img src="../res/img/red-info.gif.cfm" 
											width="9" 
											height="9" 
											border="0" 
											title="#stVeritfyMessages[srcLocal.name].message##Chr(13)#">
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
							<td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&name=#Hash(srcLocal.name)#")#
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4">
							<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.verify#">
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

<!--- select default cache --->
<cfif connections.recordcount and access EQ "yes">
	<cfoutput>
		<h2>#stText.Settings.cache.defaultTitle#</h2>
		<div class="itemintro">#stText.Settings.cache.defaultDesc#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl">
				<tbody>
	<cfset defaults={}>		
    <cfloop index="type" list="object,template,query,resource,function,include,http,file,webservice">
						<tr>
							<th scope="row">#stText.Settings.cache['defaulttype'& type]#</th>
							<td>
								<select name="default_#type#" class="small" onchange="defaultValue(this);">
									<option value="">------</option>
									<cfloop query="connections">
										<option value="#connections.name#" <cfif connections.default EQ type><cfset defaults[type]=connections.name>selected="selected"</cfif>>#connections.name#</option>
									</cfloop>
								</select>
								<div class="comment">#stText.Settings.cache['defaulttype' &type& 'Desc']#</div>
							</td>
						</tr>
					</cfloop>
						<tr>
						<td colspan="2">
<cfscript>
hasObj=!isNull(defaults.object) && len(defaults.object);
hasTem=!isNull(defaults.template) && len(defaults.template);
hasQry=!isNull(defaults.query) && len(defaults.query);
hasRes=!isNull(defaults.resource) && len(defaults.resource);
hasFun=!isNull(defaults.function) && len(defaults.function);
hasInc=!isNull(defaults.include) && len(defaults.include);
hasHTT=!isNull(defaults.http) && len(defaults.http);
hasFil=!isNull(defaults.file) && len(defaults.file);
hasWSe=!isNull(defaults.webservice) && len(defaults.webservice);

</cfscript>
<cfsavecontent variable="codeSample">
this.cache.object = "#!hasObj?"&lt;cache-name>":defaults.object#";
this.cache.template = "#!hasTem?"&lt;cache-name>":defaults.template#";
this.cache.query = "#!hasQry?"&lt;cache-name>":defaults.query#";
this.cache.resource = "#!hasRes?"&lt;cache-name>":defaults.resource#";
this.cache.function = "#!hasFun?"&lt;cache-name>":defaults.function#";
this.cache.include = "#!hasInc?"&lt;cache-name>":defaults.include#";	
this.cache.http = "#!hasHTT?"&lt;cache-name>":defaults.http#";	
this.cache.file = "#!hasFil?"&lt;cache-name>":defaults.file#";	
this.cache.webservice = "#!hasWSe?"&lt;cache-name>":defaults.webservice#";	
</cfsavecontent>
<cfset renderCodingTip( codeSample )>

						</td>
						</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="<cfif request.adminType EQ "web">bl<cfelse>bs</cfif> button submit" name="mainAction" value="#stText.Buttons.update#">
							<cfif request.adminType EQ "web">
								<input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#">
							</cfif>
						</td>
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
		
	</cfoutput>
</cfif>

<!--- 
	Create Datasource --->
<cfif access EQ "yes">
	<cfoutput>
		<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
		<cfif listLen(_drivers)>
			<h2>#stText.Settings.cache.titleCreate#</h2>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl" style="width:400px;">
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
