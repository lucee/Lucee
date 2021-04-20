<cfadmin 
	action="getGatewayEntries"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="entriesevent">
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
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>
    
<cfset querySort(entries,"id")>
<cfset src.local=queryNew("id,class,cfcpath,custom,readonly,driver,state")>
<cfset src.global=queryNew("id,class,cfcpath,custom,readonly,driver,state")>

<cfloop query="entries">	
	<cfif !entries.readOnly>
    	<cfset tmp=src.local>
	<cfelse>
    	<cfset tmp=src.global>
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
<!---<cfdump var="#getPageContext().getConfig().getGatewayEntries()#">
<cfdump var="#drivers#">
<cfdump var="#variables#">
<cfabort>--->
<cfoutput>
	<!--- Error Output--->
	<cfset printError(error)>

	<!---- READ ONLY 
	<cfif request.adminType EQ "web" and srcGlobal.recordcount>
		<h2>#stText.Settings.gateway.titleReadOnly#</h2>
		
		<div class="pageintro">#stText.Settings.cache.descReadOnly#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
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
						<cfif IsSimpleValue(srcGlobal.driver)>
							<cfcontinue>
						</cfif>
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
		</cfformClassic>
	</cfif> --->

	<!--- LIST --->
	<cfloop list="global,local" item="type">
		<cfset qry=src[type]>
	
	<cfif qry.recordcount>
		<h2>#type=='local'?stText.Settings.gateway.titleExisting:stText.Settings.gateway.titleReadONly#</h2>
		<div class="itemintro">#type=='local'?stText.Settings.gateway.descExisting:stText.settings.gateway.descreadonly#</div>
    	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<table class="maintbl checkboxtbl">
				<thead>
					<tr>
						<cfif qry.recordcount gt 1>
							<th width="3%"><input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)"></th>
						<cfelse>
							<th width="3%">&nbsp;</th>
						</cfif>
						<th width="30%">#stText.Settings.gateway.id#</th>
						<th width="30%">#stText.Settings.gateway.type#</th>
						<th width="30%">#stText.Settings.gateway.state#</th>
						<!---<th width="4%">#stText.Settings.DBCheck#</th>--->
						<cfif type=='local'><th width="3%">&nbsp;</th></cfif>
	

					</tr>
				</thead>
				<tbody>
					<cfloop query="qry">						
						<cfswitch expression="#qry.state#">
							<cfcase value="running"><cfset css="Green"></cfcase>
							<cfcase value="failed,stopped"><cfset css="Red"></cfcase>
							<cfdefaultcase><cfset css="Yellow"></cfdefaultcase>
						</cfswitch>
						<tr>
							<td>
								<input type="checkbox" class="checkbox" name="row_#qry.currentrow#" value="#qry.currentrow#">
							</td>
							<td><input type="hidden" name="id_#qry.currentrow#" value="#qry.id#">#qry.id#</td>
							<cfif IsSimpleValue(qry.driver)>
								<td>#qry.driver#</td>
							<cfelse>	
								<td>#qry.driver.getLabel()#</td>
							</cfif>							
							<td class="tblContent#css#" nowrap>#qry.state#</td>
							<!---<cfif StructKeyExists(stVeritfyMessages, qry.id)>
								<td class="tblContent#css# center">
									<cfif stVeritfyMessages[qry.id].label eq "OK">
										<span class="CheckOk">#stVeritfyMessages[qry.id].label#</span>
									<cfelse>
										<span class="CheckError" title="#stVeritfyMessages[qry.id].message##Chr(13)#">#stVeritfyMessages[qry.id].label#</span>
										&nbsp;<img src="resources/img/red-info.gif.cfm" width="9" height="9" title="#stVeritfyMessages[qry.id].message##Chr(13)#" />
									</cfif>
								</td>
							<cfelse>
								<td>&nbsp;</td>
							</cfif>--->
							<cfif type=='local'><td>
								#renderEditButton("#request.self#?action=#url.action#&action2=create&id=#Hash(qry.id)#")#
							</td></cfif>
						</tr>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4" id="btns">
							<input type="submit" class="bl button" name="mainAction" value="#stText.Buttons.refresh#">
							<cfif type=='local'><input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.delete#"></cfif>
							<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.restart#">
							<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.stopstart#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>
	</cfloop>


</cfoutput>

<!--- Create gateway entry --->
<cfif access EQ "yes">
	<cfoutput>
		<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
	    <cfif listLen(_drivers)>
			<h2>#stText.Settings.gateway.titleCreate#</h2>
			<div class="msg"></div>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl">
					<tbody>
						<tr>
							<td>
								<cfif arrayIndexExists(queryColumnData(entriesevent,"id"),1)>
									<input type="hidden" name="existingEvents" value='#serialize(queryColumnData(entriesevent,'id'))#'>
								</cfif>
							</td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.gateway.id#</th>
							<td><cfinputClassic type="text" name="_id" value="" class="medium" required="yes" message="#stText.Settings.gateway.nameMissing#"></td>
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
								<input type="submit" class="bl button submit" onclick="return createEventevent(this, _id, existingEvents,'eventGateway')" name="run" value="#stText.Buttons.create#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>   
			</cfformClassic>
	    <cfelse>
    		<div class="txt">#stText.Settings.gateway.noDriver#</div>
    	</cfif>
	</cfoutput>
<cfelse>
 	<cfset noAccess(stText.Settings.gateway.noAccess)>
</cfif>
