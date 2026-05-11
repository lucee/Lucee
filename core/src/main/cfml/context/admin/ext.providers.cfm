<!--- Action --->
<cfinclude template="ext.functions.cfm">

<cfscript>
function getExtensionGroups() {
	cfadmin(
		action="getExtensionGroups",
		type="#request.adminType#",
		password="#session["password"&request.adminType]#",
		returnVariable="local.groupIds");
		
	return groupIds;
}

stVeritfyMessages={};

cfparam(name="error", default={message:"",detail:""});
cfparam(name="form.mainAction", default="none");
error.message="";

try {

	if (form.mainAction != "none") {
		
		groupIds = getExtensionGroups();
		existing={};
		loop array=groupIds item="g" {
			existing[g]=true;
		}

		data.groupIds=toArrayFromForm("groupId");
		data.rows=toArrayFromForm("row");
		// SAVE
		if(form.mainAction==stText.Buttons.save) {
			loop array=data.groupIds index="idx" item="g" {
				if( !isNull(data.rows[idx]) && !structKeyExists(existing,g)) {
					cfadmin(
						action="updateExtensionGroups",
						type=request.adminType,
						password=session["password"&request.adminType],
						groupId=trim(g));
				}
			}
		}
		// DELETE
		else if(form.mainAction==stText.Buttons.delete) {
			loop array=data.groupIds index="idx" item="g" {
				if(!isNull(data.rows[idx]) && structKeyExists(existing,g)) {
					cfadmin(
						action="removeExtensionGroups",
						type=request.adminType,
						password=session["password"&request.adminType],
						groupId=trim(g));
				}
			}
		}
		// VERIFY
		else if(form.mainAction==stText.Buttons.verify) {
			loop array=data.groupIds index="idx" item="g" {
				if(!isNull(data.rows[idx]) && structKeyExists(existing,g)) {
					// list extension prom that groupId, throws an exception if there are none
					try{
						artifacts=luceeExtension(g);
						if(arrayLen(artifacts) == 0) {
							stVeritfyMessages[g] = {
								label = "Error",
								message = "No extensions found",
								detail = "we could connect to the repository but no extensions were found"
							};
						}
						else {
							stVeritfyMessages[g] = {
								label = "Ok",
								message = "OK",
								detail = "we could connect to the repository and found #arrayLen(artifacts)# extensions"
							};
						}
					}
					catch(ex) {
						stVeritfyMessages[g] = {
							label = "Error",
							message = ex.message,
							detail = ex.detail?:""
						};
					}
					artifacts=luceeExtension(g);
				}
			}
		}
	}
} 
catch(any cfcatch) {
	error.message=cfcatch.message;
	error.detail=cfcatch.Detail;
	error.cfcatch=cfcatch;
}


</cfscript>

<!--- Redirect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq stText.Buttons.verify>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<!--- Error Output --->
<cfset printError(error)>




<cfscript>
	groupIds = getExtensionGroups();


	hasAccess=true;



	stText.ext.provext.groupIds="Maven GroupId";
	stText.ext.provext.new="Add Extension Provider";
	stText.ext.provext.list="List of Maven groupIds Lucee scans for available extensions. Each groupId must host artifacts ending in ""-extension"" (e.g. ""yaml-extension"").";
	stText.ext.provext.groupIdsDesc="Maven groupId to scan for extensions (e.g. org.lucee, com.rasia)";
</cfscript>


<!--- 

list all mappings and display necessary edit fields --->

<cfoutput>


	<div class="itemintro">#stText.ext.prov.IntroText#</div>

	<cfset renderSettings("extensionProviders",{ value:groupIds} )>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
		<div class="pageintro">#stText.ext.provext.list#</div>
		<table class="maintbl checkboxtbl">
			<thead>
				<tr>
					<th width="10px"><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></th>
					<th>#stText.ext.provext.groupIds#</th>
					<th>#stText.Settings.DBCheck#</th>
				</tr>
			</thead>
			<tbody id="extproviderlist">
				<cfloop array="#groupIds#" index="index" item="groupId">
					<tr>
						<!--- checkbox ---->
						<td>
							<cfset edit=renderEditButton2("extensionProviders","",groupId)>
							<cfif groupId EQ "org.lucee">
							<cfelseif len(edit) EQ 0>
								<input type="checkbox" class="checkbox" name="row_#index#" value="#index#">
							<cfelse>
								#edit#
							</cfif>
						</td>
						<!--- GroupId --->
						<td>
							<input type="hidden" name="groupId_#index#" value="#groupId#">
							#groupId#
						</td>
						
		
						<!--- check --->
						<cfif StructKeyExists(stVeritfyMessages, groupId)>
							<cfset msg=stVeritfyMessages[groupId]>
							<cfset title="">
							<cfif (structKeyExists(msg,"message") && len(trim(msg.message))) || 
								  (structKeyExists(msg,"detail")  && len(trim(msg.detail)))>
								<cfset m=structKeyExists(msg,"message")?trim(msg.message):"">
								<cfset d=structKeyExists(msg,"detail")?trim(msg.detail):"">
								<cfset title=' title="#m# #d#"'>
							</cfif>


							<td >
								<span class="Check#msg.label#">#msg.message#</span>
								<cfif len(trim(msg.detail))><p>#msg.detail#</p></cfif>
							</td>
						<cfelse>
							<td>&nbsp;</td>
						</cfif>
					</tr>
				</cfloop>
			</tbody>
			<cfif hasAccess>
				<tfoot>
					 <tr>
						<td colspan="3">
							<input type="submit" class="button submit enablebutton" name="mainAction" value="#stText.Buttons.verify#">
							<input type="submit" class="button submit enablebutton" name="mainAction" value="#stText.Buttons.Delete#">
							<input type="reset" class="reset enablebutton" name="cancel" id="clickCancel" value="#stText.Buttons.Cancel#">
						</td>	
					</tr>
				</tfoot>
			</cfif>
		</table>
	</cfformClassic>
	
	<cfif hasAccess>
		<h2>#stText.ext.provext.new#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
			<input type="hidden" name="row_1" value="1">
			<table class="maintbl" style="width:75%">
				<tbody>
					<tr> 
						<th scope="row">#stText.ext.provext.groupIds#</th>
						<td>
							<cfinputClassic onKeyDown="checkTheBox(this)" type="text" 
							name="groupId_1" value="" required="yes" class="xlarge">
							<div class="comment">#stText.ext.provext.groupIdsDesc#</div>
						</td>
					</tr>
				</tbody>
				<tfoot>
					 <tr>
						<td colspan="2">
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.save#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfformClassic>
	</cfif>


</cfoutput>
