<cfsilent>
	<cfparam name="request.recount" default="0">
	<cfparam name="attributes.line" default="0">
	<cfparam name="attributes.attention" default="">
	<cfset request.recount=request.recount+1>

	<cffunction name="hasClients" output="no" returntype="boolean">
		<cfargument name="clients" type="query">
	
		<cfloop query="arguments.clients">
			<cfif ListFindNoCase(arguments.clients.usage,"synchronisation")>
				<cfreturn true>
			</cfif>
		</cfloop>
		<cfreturn false>
	</cffunction>
	<cfadmin 
		action="getRemoteClients"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="clients">
</cfsilent>
<cfif hasClients(clients)>
	<cfoutput>
		<!---<cfif attributes.line and attributes.colspan gt 2>
			<tr>
				<td colspan="#attributes.colspan#">
					<hr />
				 </td>
			</tr>
		</cfif>--->
	</tbody>
</table>
<h4 class="rsync">#caller.stText.remote.sync.title#</h4>
<table class="maintbl rsync">
	<tbody>
		<tr>
<!---			<cfif attributes.colspan neq 2>
				<td colspan="#attributes.colspan#" class="tblHead">
					<table class="maintbl">
						<tr>
			</cfif>--->
							<th scope="row">Choose clients
								<div class="comment">#caller.stText.remote.sync.desc#</div>
							</th>
							<td>
								<ul class="radiolist">
									<cfloop query="clients"><cfif ListFindNoCase(clients.usage,"synchronisation")>
										<li>
											<label>
												<input type="checkbox" class="checkbox" name="_securtyKeys[]" value="#clients.securityKey#" checked="checked" />
												<b>#clients.label#</b>
											</label>
										</li>
									</cfif></cfloop>
								</ul>
								<cfif len(attributes.attention)>
									<div class="comment inline">#attributes.attention#</div>
								</cfif>
							</td>
<!---			<cfif attributes.colspan neq 2>
						</tr>
					</table>
				</td>
			</cfif>--->
		</tr>
	</cfoutput>
</cfif>