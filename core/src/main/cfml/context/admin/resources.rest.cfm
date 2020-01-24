<cfset hasAccess=true>
<cfset newRecord="sd812jvjv23uif2u32d">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfset error={message:"",detail:""}>


<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE/settings --->
		<cfcase value="#stText.Buttons.Update#">
            <cfadmin
                action="updateRestSettings"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                remoteClients="#request.getRemoteClients()#"

                list="#structKeyExists(form,'list') and form.list#"
                >
		</cfcase>
        <!--- reset/settings --->
		<cfcase value="#stText.Buttons.resetServerAdmin#">
            <cfadmin
                action="updateRestSettings"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                remoteClients="#request.getRemoteClients()#"

                list=""
                >
		</cfcase>
        <!--- save/mapping --->
		<cfcase value="#stText.Buttons.save#">

			<cfset data.physicals=toArrayFromForm("physical")>
            <cfset data.virtuals=toArrayFromForm("virtual")>
            <cfset data.rows=toArrayFromForm("row")>


            <cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
            	<cfset _default=StructKeyExists(form,'default') and form.default EQ idx>
				<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">aaa
                <cfadmin
                    action="updateRestMapping"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"

                    virtual="#data.virtuals[idx]#"
                    physical="#data.physicals[idx]#"
                    default="#_default#"

        remoteClients="#request.getRemoteClients()#">
                </cfif>
            </cfloop>
		</cfcase>

        <!--- delete/mapping --->
		<cfcase value="#stText.Buttons.delete#">

			<cfset data.virtuals=toArrayFromForm("virtual")>
            <cfset data.rows=toArrayFromForm("row")>

            <cfloop index="idx" from="1" to="#arrayLen(data.virtuals)#">
            	<cfset _default=StructKeyExists(form,'default') and form.default EQ idx>
				<cfif isDefined("data.rows[#idx#]") and data.virtuals[idx] NEQ "">aaa
                <cfadmin
                    action="removeRestMapping"
                    type="#request.adminType#"
                    password="#session["password"&request.adminType]#"

                    virtual="#data.virtuals[idx]#"

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
<cfif cgi.request_method EQ "POST" and error.message EQ "">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>

<!---
Error Output --->
<cfset printError(error)>


<cfadmin
	action="getRestMappings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="rest">
<cfadmin
	action="getRestSettings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="settings">


<!---
list all mappings and display necessary edit fields --->

<cfhtmlbody>

<script type="text/javascript">
	function changeDefault(field) {
		var form=field.form;
		for(var i=0;i<form.length;i++){
			if(form[i].name=='default') {
				$(form["row_"+form[i].value]).prop('checked', form[i].checked).triggerHandler('change');

				//alert(form[i].value+":"+form[i].checked);

				//row_#rest.currentrow#
			}
		}
	}
</script>

</cfhtmlbody>

<cfoutput>
	<cfif not hasAccess><cfset noAccess(stText.setting.noAccess)></cfif>
	<div class="pageintro">#stText.rest.desc#</div>
	<!--- Settings --->
	<h2>#stText.rest.setting#</h2>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.rest.list#</th>
					<td>
						<cfif hasAccess NEQ 0><input type="checkbox" class="checkbox" name="list" value="yes" <cfif settings.list>checked</cfif>>
						<cfelse><b>#yesNoFormat(settings.list)#</b></cfif>
						<div class="comment">#stText.rest.listDesc#</div>
					</td>
				</tr>
<!---
<tr>
	<td class="tblHead" width="150">#stText.rest.changes#</td>
	<td class="tblContent">
	<cfif hasAccess NEQ 0><input type="checkbox" class="checkbox" name="allowChanges" value="yes" <cfif settings.allowChanges>checked</cfif>><cfelse><b>#yesNoFormat(settings.allowChanges)#</b></cfif>
	<span class="comment">#stText.rest.changesDesc#</span></td>
</tr>--->


				<cfif hasAccess NEQ 0>
					<cfmodule template="remoteclients.cfm" colspan="2">
				</cfif>
			</tbody>
			<cfif hasAccess NEQ 0>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
							<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>

	<!--- Mappings --->
	<h2>#stText.rest.mapping#</h2>
	<div class="itemintro">#stText.rest.mappingDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="3%"><cfif hasAccess><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></cfif></th>
					<th width="24%">#stText.rest.VirtualHead#</th>
					<th width="65%">#stText.rest.PhysicalHead#</th>
					<th width="5%">#stText.rest.DefaultHead#</th>
					<!---<th width="3%"></th>--->
				</tr>
			</thead>
			<tbody>
				<cfloop query="rest">
					<cfif not rest.hidden>
						<tr>
							<!--- checkbox ---->
							<td>
								<input type="hidden" name="stopOnError_#rest.currentrow#" value="yes">
								<cfif not rest.readOnly>
									<input type="checkbox" class="checkbox" name="row_#rest.currentrow#" value="#rest.currentrow#">
								</cfif>
							</td>
							<cfset css=iif(len(rest.physical) EQ 0 and len(rest.strPhysical) NEQ 0,de('Red'),de(''))>
							<!--- virtual --->
							<td class="tblContent#css#">
								<input type="hidden" name="virtual_#rest.currentrow#" value="#rest.virtual#">
								#rest.virtual#
							</td>
							<!--- physical --->
							<td class="tblContent#css#">
								<cfset abs=expandPath(rest.strPhysical)>
								<cfif !len(css) && abs NEQ rest.strPhysical><abbr title="#abs#"></cfif><cfif rest.readOnly>
									#rest.strPhysical#
								<cfelse>
									<cfinputClassic  onKeyDown="checkTheBox(this)" type="text"
									name="physical_#rest.currentrow#" value="#rest.strPhysical#" required="no"
									class="xlarge" message="#stText.rest.PhysicalMissing##rest.currentrow#)">
								</cfif><cfif !len(css) &&  abs NEQ rest.strPhysical></abbr></cfif>
							</td>
							<!--- default --->
							<td>
								<cfif rest.readOnly>
									#yesNoFormat(rest.default)#
								<cfelse>
									<input type="radio" class="radio" name="default" value="#rest.currentrow#" onchange="changeDefault(this)" <cfif rest.default>checked="checked"</cfif>/>
								</cfif>
							</td>
							<!--- edit
							<td>
								<cfif not rest.readOnly>
									#renderEditButton("#request.self#?action=#url.action#&action2=create&virtual=#rest.virtual#")#

								</cfif>
							</td> --->
						</tr>
					</cfif>
				</cfloop>
				<cfif hasAccess>
					<cfmodule template="remoteclients.cfm" colspan="4" line=true>
				</cfif>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					<tr>
						<td colspan="5">
							<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.save#">
							<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="br button submit" name="mainAction" value="#stText.Buttons.Delete#">
						</td>
					</tr>
				</tfoot>
			</cfif>
		</table>

		<cfif hasAccess>
			<h2>Create new mapping</h2>
			<table class="maintbl">
				<tbody>
					<tr>
						<th scope="row">#stText.rest.VirtualHead#</th>
						<td>
							<input type="hidden" name="row_#rest.recordcount+1#" value="#rest.recordcount+1#">
							<cfinputClassic type="text" name="virtual_#rest.recordcount+1#" value="" required="yes" class="medium" message="#stText.mapping.virtual#"/>
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.rest.PhysicalHead#</th>
						<td>
							<cfinputClassic type="text" name="physical_#rest.recordcount+1#" value="" required="yes" class="large"  message="#stText.mapping.physical#">
						</td>
					</tr>
					<tr>
						<th scope="row">#stText.rest.DefaultHead#</th>
						<td>
							<input type="radio" class="radio" name="default" value="#rest.recordcount+1#" onchange="changeDefault(this)"/>
						</td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="2">
							<input type="submit" class="bs button submit" name="mainAction" value="#stText.Buttons.save#">
						</td>
					</tr>
				</tfoot>
			</table>
		</cfif>
	</cfformClassic>
</cfoutput>
