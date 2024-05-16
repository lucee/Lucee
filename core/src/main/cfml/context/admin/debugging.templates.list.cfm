<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.Update#">

			<cfif form.debug == "resetServerAdmin">
				
				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"					
	                debug=""
					debugTemplate=""	                
					remoteClients="#request.getRemoteClients()#">
			
			<cfelse>

				<cfadmin action="updateDebug"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					debug="#form.debug#"
					debugTemplate=""
					remoteClients="#request.getRemoteClients()#">
			</cfif>
		</cfcase>

    <!--- delete --->
		<cfcase value="#stText.Buttons.Delete#">
				<cfset data.rows=toArrayFromForm("row")>
				<cfset data.ids=toArrayFromForm("id")>
				<cfloop index="idx" from="1" to="#arrayLen(data.ids)#">
					<cfif isDefined("data.rows[#idx#]") and data.ids[idx] NEQ "">
						<cfadmin 
							action="removeDebugEntry"
							type="#request.adminType#"
							password="#session["password"&request.adminType]#"
							id="#data.ids[idx]#"
							remoteClients="#request.getRemoteClients()#">
						
					</cfif>
				</cfloop>
		</cfcase>
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

<cfset querySort(debug,"id")>
<cfset qryWeb=queryNew("id,label,iprange,type,custom,readonly,driver")>
<cfset qryServer=queryNew("id,label,iprange,type,custom,readonly,driver")>


<cfloop query="debug">	
	<cfif not debug.readOnly>
    	<cfset tmp=qryWeb>
	<cfelse>
    	<cfset tmp=qryServer>
	</cfif>
	<cfset QueryAddRow(tmp)>
    <cfset QuerySetCell(tmp,"id",debug.id)>
    <cfset QuerySetCell(tmp,"label",debug.label)>
    <cfset QuerySetCell(tmp,"iprange",debug.iprange)>
    <cfset QuerySetCell(tmp,"type",debug.type)>
    <cfset QuerySetCell(tmp,"custom",debug.custom)>
    <cfset QuerySetCell(tmp,"readonly",debug.readonly)>
    <cfif structKeyExists(drivers,debug.type)><cfset QuerySetCell(tmp,"driver",drivers[debug.type])></cfif>
</cfloop>

<cfoutput>
	<!--- Error Output--->
	<cfset printError(error)>
	<script type="text/javascript">
		var drivers={};
		<cfloop collection="#drivers#" item="key">drivers['#JSStringFormat(key)#']='#JSStringFormat(drivers[key].getDescription())#';
		</cfloop>
		function setDesc(id,key){
			var div = document.getElementById(id);
			if(div.hasChildNodes())
				div.removeChild(div.firstChild);
			div.appendChild(document.createTextNode(drivers[key]));
		}
	</script>
	
			#stText.debug.list.createDesc#

	<!--- LIST --->
	<cfloop list="server,web" index="k">
		<cfset isWeb=k EQ "web">
		<cfset qry=variables["qry"&k]>
		<cfif qry.recordcount>
			<h2>#stText.debug.list[k & "title"]#</h2>
			<div class="itemintro">#stText.debug.list[k & "titleDesc"]#</div>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl">
					<thead>
						<tr>
							<cfif isWeb>
								<th width="3%">
									<input type="checkbox" class="checkbox" name="rowreadonly" onclick="selectAll(this)">
								</th>
							</cfif>
							<th width="25%">#stText.debug.label#</th>
							<th>#stText.debug.ipRange#</th>
							<th width="15%"># stText.debug.type#</td>
							<cfif isWeb>
								<th width="3%"></th>
							</cfif>
						</tr>
					</thead>
					<tbody>
						<cfloop query="qry">
							<cfset drv=qry.driver>
							<cfif isNull(drv) or IsSimpleValue(drv)>
								<cfcontinue>
							</cfif>
							<tr>
								<cfif isWeb>
									<td>
										<input type="checkbox" class="checkbox" name="row_#qry.currentrow#" id="clickCheckbox" value="#qry.currentrow#">
									</td>
								</cfif>
								<td>
									<input type="hidden" name="id_#qry.currentrow#" value="#qry.id#">
									<input type="hidden" name="type_#qry.currentrow#" value="#qry.type#">
									#qry.label#
								</td>
								<td>#replace(qry.ipRange,",","<br />","all")#</td>
								
								<td>#qry.driver.getLabel()#</td>
								<cfif isWeb>
									<td>
										#renderEditButton("#request.self#?action=#url.action#&action2=create&id=#qry.id#")#
									</td>
								</cfif>
							</tr>
						</cfloop>
					</tbody>
					<cfif isWeb>
						<tfoot>
							<tr>
								<td colspan="#isWeb?5:3#">
									<input type="submit" class="bl button submit enablebutton" name="mainAction" value="#stText.Buttons.delete#" disabled style="opacity:0.5">
									<input type="reset" class="br button reset enablebutton" id="clickCancel" name="cancel" value="#stText.Buttons.Cancel#" disabled style="opacity:0.5">
								</td>	
							</tr>
						</tfoot>
					</cfif>
				</table>
			</cfformClassic>
		</cfif>
	</cfloop>

	<!--- Create debug entry --->
	<cfif access EQ "yes">
		<cfset _drivers=ListSort(StructKeyList(drivers),'textnocase')>
	
		<cfif listLen(_drivers)>
			<h2>#stText.debug.createTitle#</h2>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create" method="post">
				<table class="maintbl autowidth" style="width:400px;">
					<tbody>
						<tr>
							<th scope="row">#stText.debug.label#</th>
							<td><cfinputClassic type="text" name="label" value="" class="large" required="yes" 
								message="#stText.debug.labelMissing#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Settings.gateway.type#</th>
							<td>
								<select name="type" onchange="setDesc('typeDesc',this.value);" class="large">
									<cfloop list="#_drivers#" index="key">
									<cfset driver=drivers[key]>
										<option value="#trim(driver.getId())#">#trim(driver.getLabel())#</option>
									</cfloop>
								</select>
								<div id="typeDesc" style="position:relative"></div>
								<script>setDesc('typeDesc','#JSStringFormat(listFirst(_drivers))#');</script>
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
			#stText.debug.noDriver#
		</cfif>
	<cfelse>
		<cfset noAccess(stText.debug.noAccess)>
	</cfif>
</cfoutput>