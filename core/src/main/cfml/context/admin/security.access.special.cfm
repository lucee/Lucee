<cfsilent>
<cfparam name="url.id" default="">
<cfparam name="url.action2" default="">
<cfset index=1>
<cfset context="">
<cfset row="">


<cfadmin 
	action="getContexts"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="contextes">
	


	<cfset size=0>
	<cfset QueryAddColumn(contextes,"text",array())>
	<cfloop query="contextes">
			<cfif len(contextes.label)>
				<cfset _path=contextes.label&" ("&contextes.path&")">
			<cfelse>
				<cfset _path=contextes.path>
			</cfif>
			<cfset contextes.text=_path>
			<cfif size LT len(_path)>
				<cfset size=len(_path)>
			</cfif>
			<cfif url.id EQ contextes.id>
				<cfset row=contextes.currentrow>
			</cfif>
	</cfloop>
</cfsilent>

<!--- 
Detail
 --->
<cfif url.action2 EQ "edit">
	
	<cfadmin 
		action="getDefaultSecurityManager"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		returnVariable="daccess">
	<cfadmin 
		action="getSecurityManager"
		type="#request.adminType#"
		password="#session["password"&request.adminType]#"
		
		id="#url.id#"
		returnVariable="access">
	<cfinclude template="security.access.form.cfm">

<!--- 
Overview
 --->
<cfelse>
	<cfset count=0>
	<cfloop query="contextes"><cfif contextes.hasOwnSecContext><cfset count++></cfif></cfloop>
	<cfoutput>	
		<h2>#stText.Security.specListTitle#</h2>
		<div class="itemintro">#stText.Security.specListText#</div>
		<cfform onerror="customError" action="#go(url.action,"removeSecurityManager")#" method="post">
			<table class="maintbl">
				<thead>
					<tr>
						<th width="1%"></th>
						<th width="39%">#stText.Security.specListHost#</th>
						<th width="59%">#stText.Security.specListPath#</th>
						<th width="1%"></th>
					</tr>
				</thead>
				<tbody>
					<cfset hasNoneIndividual=false>
					<cfloop query="contextes">
						<cfif contextes.hasOwnSecContext >
							<!--- and now display --->
							<tr>
								<td>
									<input type="checkbox" class="checkbox" name="ids_#contextes.currentrow#" value="#contextes.id#">
								</td>
								<td nowrap>#contextes.label#&nbsp;</td>
								<td nowrap>#contextes.path#</td>
								<td>
									#renderEditButton("#go(url.action,"edit",struct(id:contextes.id))#")#
									
								</td>
							</tr>
						<cfelse>
							<cfset hasNoneIndividual=true>
						</cfif>
					</cfloop>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="4">
							<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
							<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.Delete#">
						</td>	
					</tr>
				</tfoot>
			</table>
		</cfform>
	</cfoutput>

	<cfif hasNoneIndividual>
		<cfoutput>
			<!--- Create new Indicvidual sec --->
			<h2>#stText.Security.specListNewTitle#</h2>
			<cfform onerror="customError" action="#go(url.action,'createSecurityManager')#" method="post">
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Security.specListWebContext#</th>
							<td>
								<select name="id">
									<cfoutput><cfloop query="contextes"><cfif not contextes.hasOwnSecContext>
										<option value="#contextes.id#">#contextes.text#</option>
									</cfif></cfloop></cfoutput>
								</select>
							</td>
						</tr>
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="button submit" name="run" value="#stText.Buttons.Create#">
								<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>   
			</cfform>
		</cfoutput>
	</cfif>
</cfif>