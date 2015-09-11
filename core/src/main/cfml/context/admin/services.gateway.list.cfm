<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.update#">
            <cfadmin 
                action="updateGatewayEntry"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                object="#form.object#"
                template="#form.template#"
                remoteClients="#request.getRemoteClients()#">				
		</cfcase>
    <!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
					<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
						<cfadmin 
							action="removeGatewayEntry"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							id="#data.ids[idx]#"
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.restart#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
					<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
						<cfadmin 
							action="gateway"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							id="#data.ids[idx]#"
                            
                            gatewayAction="restart"
                            
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
		<cfcase value="#stText.Buttons.stopstart#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
					<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
                    	<cfadmin 
                            action="getGatewayEntry"
                            type="#request.adminType#"
                            password="#session["password"&request.adminType]#"
                            id="#data.ids[idx]#"
                            returnVariable="gateway">
						
						<cfswitch expression="#gateway.state#">
                            <cfcase value="running"><cfset ga="stop"></cfcase>
                            <cfcase value="failed,stopped"><cfset ga="start"><cfset css="Red"></cfcase>
                            <cfdefaultcase><cfset ga=""></cfdefaultcase>
                        </cfswitch>
                        <cfif len(ga)>
                        <cfadmin 
							action="gateway"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							id="#data.ids[idx]#"
                            
                            gatewayAction="#ga#"
                            
							remoteClients="#request.getRemoteClients()#">
						</cfif>
					</cfif>
				</cfloop>
		</cfcase>
		<!---<cfcase value="#stText.Buttons.verify#">
			<cfset data.ids=toArrayFromForm("id")>
			<cfset data.rows=toArrayFromForm("row")>
			<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
				<cfif isDefined("data.rows[#idx#]") and data.names[idx] NEQ "">
					<cftry>
						<cfadmin 
							action="verifyCacheConnection"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							name="#data.ids[idx]#">
							<cfset stVeritfyMessages["#data.ids[idx]#"].Label = "OK">
						<cfcatch>
							<!--- <cfset error.message=error.message&data.ids[idx]&": "&cfcatch.message&"<br>"> --->
							<cfset stVeritfyMessages[data.ids[idx]].Label = "Error">
							<cfset stVeritfyMessages[data.ids[idx]].message = cfcatch.message>
						</cfcatch>
					</cftry>
				</cfif>
			</cfloop>
		</cfcase>--->
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
    
<cfset querySort(entries,"id")>
<cfset srcLocal=queryNew("id,class,cfcpath,custom,readonly,driver,state")>
<cfset srcGlobal=queryNew("id,class,cfcpath,custom,readonly,driver,state")>

<cfloop query="entries">	
	<cfif not entries.readOnly>
    	<cfset tmp=srcLocal>
	<cfelse>
    	<cfset tmp=srcGlobal>
	</cfif>
	<cfset QueryAddRow(tmp)>
    <cfset QuerySetCell(tmp,"id",entries.id)>
    <cfset QuerySetCell(tmp,"class",entries.class)>
    <cfset QuerySetCell(tmp,"cfcPath",entries.cfcPath)>
    <cfset QuerySetCell(tmp,"custom",entries.custom)>
    <cfset QuerySetCell(tmp,"readonly",entries.readonly)>
    <cfset QuerySetCell(tmp,"driver",entries.driver)>
    <cfset QuerySetCell(tmp,"state",entries.state)>
</cfloop>

<cfoutput>
	<!--- Error Output--->
	<cfset printError(error)>

	<!---- READ ONLY ---->
	<cfif request.adminType EQ "web" and srcGlobal.recordcount>
		<h2>#stText.Settings.gateway.titleReadOnly#</h2>
		
		<div class="pageintro">#stText.Settings.cache.descReadOnly#</div>
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<th width="3%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<th width="31%">#stText.Settings.gateway.id#</th>
						<th width="31%">#stText.Settings.gateway.type#</th>
						<th width="31%">#stText.Settings.gateway.state#</th>
						<th width="4%">#stText.Settings.DBCheck#</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcGlobal">
						<cfswitch expression="#srcGlobal.state#">
							<cfcase value="running"><cfset css="Green"></cfcase>
							<cfcase value="failed,stopped"><cfset css="Red"></cfcase>
							<cfdefaultcase><cfset css="Yellow"></cfdefaultcase>
						</cfswitch>
						<cfset driver=drivers[srcGlobal.class]>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#srcGlobal.currentrow#" value="#srcGlobal.currentrow#">
							</td>
							<td class="tblContent#css#"><input type="hidden" name="id_#srcGlobal.currentrow#" value="#srcGlobal.id#">#srcGlobal.id#</td>
							<td class="tblContent#css#">#driver.getLabel()#</td>
							<td class="tblContent#css#">#srcGlobal.state#</td>
							<td class="tblContent#css# center">
								<cfif StructKeyExists(stVeritfyMessages, srcGlobal.id)>
									<cfif stVeritfyMessages[srcGlobal.id].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcGlobal.id].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcGlobal.id].message##Chr(13)#">#stVeritfyMessages[srcGlobal.id].label#</span>
										&nbsp;<img src="resources/img/red-info.gif.cfm" 
											width="9" 
											height="9" 
											border="0" 
											title="#stVeritfyMessages[srcGlobal.id].message##Chr(13)#">
									</cfif>
								<cfelse>
									&nbsp;				
								</cfif>
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td></td>
						<td colspan="3">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.refresh#">
							<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfform>
	</cfif>

	<!--- LIST --->
	<cfif srcLocal.recordcount>
		<h2>#stText.Settings.gateway.titleExisting#</h2>
		<div class="itemintro">#stText.Settings.gateway.descExisting#</div>
    
		<cfform onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<cfif srcLocal.recordcount gt 1>
							<th width="3%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<cfelse>
							<th width="3%">&nbsp;</th>
						</cfif>
						<th width="30%">#stText.Settings.gateway.id#</th>
						<th width="30%">#stText.Settings.gateway.type#</th>
						<th width="30%">#stText.Settings.gateway.state#</th>
						<th width="4%">#stText.Settings.DBCheck#</th>
						<th width="3%">&nbsp;</th>
					</tr>
				</thead>
				<tbody>
					<cfloop query="srcLocal">
						<cfif IsSimpleValue(srcLocal.driver)>
							<cfcontinue>
						</cfif>
						<cfswitch expression="#srcLocal.state#">
							<cfcase value="running"><cfset css="Green"></cfcase>
							<cfcase value="failed,stopped"><cfset css="Red"></cfcase>
							<cfdefaultcase><cfset css="Yellow"></cfdefaultcase>
						</cfswitch>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#srcLocal.currentrow#" value="#srcLocal.currentrow#">
							</td>
							<td><input type="hidden" name="id_#srcLocal.currentrow#" value="#srcLocal.id#">#srcLocal.id#</td>
							<td>#srcLocal.driver.getLabel()#</td>
							<td class="tblContent#css#" nowrap>#srcLocal.state#</td>
							<cfif StructKeyExists(stVeritfyMessages, srcLocal.id)>
								<td class="tblContent#css# center">
									<cfif stVeritfyMessages[srcLocal.id].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[srcLocal.id].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[srcLocal.id].message##Chr(13)#">#stVeritfyMessages[srcLocal.id].label#</span>
										&nbsp;<img src="resources/img/red-info.gif.cfm" width="9" height="9" title="#stVeritfyMessages[srcLocal.id].message##Chr(13)#" />
									</cfif>
								</td>
							<cfelse>
								<td>&nbsp;</td>
							</cfif>
							<td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&id=#Hash(srcLocal.id)#")#
							</td>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td></td>
						<td colspan="4" id="btns">
							<input type="submit" class="bl button" name="mainAction" value="#stText.Buttons.refresh#">
							<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.delete#">
							<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.restart#">
							<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.stopstart#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfform>
	</cfif>
</cfoutput>

<!--- Create gateway entry --->
<cfif access EQ "yes">
	<cfoutput>
		<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
	    <cfif listLen(_drivers)>
			<h2>#stText.Settings.gateway.titleCreate#</h2>
			<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Settings.gateway.id#</th>
							<td><cfinput type="text" name="_id" value="" class="medium" required="yes" message="#stText.Settings.gateway.nameMissing#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.gateway.type#</th>
							<td>
								<select name="name" class="medium">
									<cfloop list="#_drivers#" index="key">
										<cfset driver=drivers[key]>
										<option value="#key#">#trim(driver.getLabel())#</option>
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
			</cfform>
	    <cfelse>
    		<div class="txt">#stText.Settings.gateway.noDriver#</div>
    	</cfif>
	</cfoutput>
<cfelse>
 	<cfset noAccess(stText.Settings.gateway.noAccess)>
</cfif>