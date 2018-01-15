<cfif request.admintype EQ "server"><cflocation url="#request.self#" addtoken="no"></cfif>
<cfadmin
   action="getRHServerExtensions"
   type="#request.adminType#"
   password="#session["password"&request.adminType]#"
   returnVariable="serverExtensions">


  <cfquery name="LuceneExtInstl" dbtype="query">
  	select * from serverExtensions where ID = 'EFDEB172-F52E-4D84-9CD1A1F561B3DFC8'
  </cfquery>
<cfif LuceneExtInstl.recordcount EQ 0><cflocation url="#request.self#" addtoken="no"></cfif>

<cfparam name="form.run" default="none">
<cfparam name="error" default="#struct(message:"",detail:"")#">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="search"
	secValue="yes">

<cfif request.adminType EQ "web">
	<cftry>
		<cfswitch expression="#form.run#">
			<!--- Index --->
			<cfcase value="index">
				<cfsetting requesttimeout="300">
				<cfadmin 
					action="index"
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
									
					indexAction="update" 
					indexType="path" 
					collection="#url.collection#" 
					key="#form.path#" 
					urlpath="#form.url#" 
					extensions="#form.extensions#"
					recurse="#structKeyExists(form,"recurse") and form.recurse#"
					language="#form.language#"
					remoteClients="#request.getRemoteClients()#">
			</cfcase>
	
			<!--- Create --->
			<cfcase value="#stText.Buttons.Create#">
				<cfadmin 
					action="collection" 
					type="#request.adminType#"
					password="#session["password"&request.adminType]#"
					
					collectionAction="create" 
					collection="#form.collName#" 
					path="#form.collPath#" 
					language="#form.collLanguage#"
					remoteClients="#request.getRemoteClients()#">
			</cfcase>
	
			<!--- Action --->
			<cfcase value="action">
				<cfif StructKeyExists(form,"name")>
					<cfloop collection="#form.name#" item="key">
						<cfswitch expression="#form.action#">
							<cfcase value="#stText.Buttons.Purge#">
								<cfadmin 
									action="index" 
									type="#request.adminType#"
									password="#session["password"&request.adminType]#"
									
									indexAction="purge" 
									collection="#form.name[key]#"
									remoteClients="#request.getRemoteClients()#">
							</cfcase>
							<cfcase value="#stText.Buttons.Repair#">
								<cfadmin 
									action="collection" 
									type="#request.adminType#"
									password="#session["password"&request.adminType]#"
									
									collectionAction="repair" 
									collection="#form.name[key]#"
									remoteClients="#request.getRemoteClients()#">
							</cfcase>
							<cfcase value="#stText.Buttons.Optimize#">
								<cfadmin 
									action="collection" 
									type="#request.adminType#"
									password="#session["password"&request.adminType]#"
									
									collectionAction="optimize" 
									collection="#form.name[key]#"
									remoteClients="#request.getRemoteClients()#">
							</cfcase>	
							<cfcase value="#stText.Buttons.Delete#">
								<cfadmin 
									action="collection" 
									type="#request.adminType#"
									password="#session["password"&request.adminType]#"
									
									collectionAction="delete" 
									collection="#form.name[key]#"
									remoteClients="#request.getRemoteClients()#">
								
							</cfcase>
						</cfswitch>
					</cfloop>
				</cfif>
			</cfcase>
		</cfswitch>
		<cfcatch>
			<cfset error.message=cfcatch.message>
			<cfset error.detail=cfcatch.Detail>
		</cfcatch>
	</cftry>
	<cfif not isDefined("url.search")>
		<!--- 
		Redirect to entry --->
		<cfif cgi.request_method EQ "POST" and error.message EQ "">
			<cflocation url="#request.self#?action=#url.action#" addtoken="no">
		</cfif>
	</cfif>	

	<cfcollection action="list" name="collections">
	
	<cfoutput>
		<!--- 
		Error Output--->
		<cfif error.message NEQ "">
			<div class="error">
				#error.message#<br>
				#error.detail#
			</div>
		</cfif>

		<div class="pageintro">#stText.Search.Description#</div>
	</cfoutput>
	
	<cfif not StructKeyExists(url,"collection")>
		<!--- 
		@to setting for SearchEngine Class
		 --->
		<cfif collections.recordcount>
			<cfoutput>
				<!--- 
				Existing Collection --->
				<h2>#stText.Search.Collections#</h2>
				<form action="#request.self#?action=#url.action#" method="post" enctype="multipart/form-data">
					<table class="maintbl checkboxtbl">
						<thead>
							<tr>
								<th width="3%"><input type="checkbox" class="checkbox" name="rro" onclick="selectAll(this)"></th>
								<th width="25%">#stText.Search.Name#</th>
								<th width="10%">#stText.Search.Mapped#</th>
								<th width="10%">#stText.Search.Online#</th>
								<th width="10%">#stText.Search.External#</th>
								<th width="19%">#stText.Search.Language#</th>
								<th width="20%">#stText.Search.Last_Update#</th>
								<th width="3%">&nbsp;</th>
							</tr>
						</thead>
						<tbody>
							<cfloop query="collections">
								<tr>
									<td>
										<input type="checkbox" class="checkbox" name="name[]" value="#collections.name#">
									</td>
									<td><abbr title="#collections.name#">#cut(collections.name,16)#</abbr></td>
									<td>#collections.mapped#</td>
									<td>#collections.online#</td>
									<td>#collections.external#</td>
									<td>#collections.language#</td>
									<td>#DateFormat(collections.LastModified,"yyyy-mm-dd")# #TimeFormat(collections.LastModified,"HH:mm")#</td>
									<!---<td width="400" style=" white-space:;overflow: hidden;text-overflow: ellipsis;"<cfif len(collections.path) GT 40> title="#collections.path#"</cfif>>#collections.path##collections.path#</td>--->
									<td>
										#renderEditButton("#request.self#?action=#url.action#&collection=#collections.name#")#
										
									</td>
								</tr>
							</cfloop>
							<cfmodule template="remoteclients.cfm" colspan="8" line=true>
						</tbody>
						<tfoot>
							 <tr>
								<td colspan="8">
									<input type="hidden" name="run" value="action">
									<input type="submit" class="bl button submit" name="action" value="#stText.Buttons.Repair#">
									<input type="submit" class="bm button submit" name="action" value="#stText.Buttons.Optimize#">
									<input type="submit" class="bm button submit" name="action" value="#stText.Buttons.Purge#">
									<input type="reset" class="bm button reset" name="cancel" value="#stText.Buttons.Cancel#">
									<input type="submit" class="br button submit" name="action" value="#stText.Buttons.Delete#">
								</td>	
							</tr>
						</tfoot>
					</table>
				</form>
			</cfoutput>
		</cfif>
		
		<cfoutput>
			<!--- Create Collection --->
			<h2>#stText.Search.CreateCol#</h2>
			<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
				<table class="maintbl autowidth">
					<tbody>
						<tr>
							<th scope="row">#stText.Search.Name#</th>
							<td><cfinputClassic type="text" name="collName" value="" class="large" required="yes" message="#stText.Search.Missing_Name#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.Path#</th>
							<td><cfinputClassic type="text" name="collPath" value="" class="large" required="yes" message="#stText.Search.Missing_Path#"></td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.Language#</th>
							<td>
								<select name="collLanguage" class="medium">
									<cfset aLangs = StructKeyArray(stText.SearchLng)>
									<cfset ArraySort(aLangs, "text")>
									<cfloop from="1" to="25" index="iLng"> 
										<option value="#aLangs[iLng]#" <cfif aLangs[iLng] eq "english">selected</cfif>>#stText.SearchLng[aLangs[iLng]]#</option>
									</cfloop>
								</select>
							</td>
						</tr>
						<cfmodule template="remoteclients.cfm" colspan="2">
					</tbody>
					<tfoot>
						<tr>
							<td colspan="2">
								<input type="submit" class="bl button submit" name="run" value="#stText.Buttons.Create#">
								<input type="reset" class="br button reset" name="cancel" value="#stText.Buttons.Cancel#">
							</td>
						</tr>
					</tfoot>
				</table>
			</cfformClassic>
		</cfoutput>
	<cfelse>
		<cfset collection=struct()>
		<cfoutput query="collections">
			<cfif collections.name EQ url.collection>
				<cfloop index="item" list="#collections.columnlist#">
					<cfset collection[item]=collections[item]>
				</cfloop>
			</cfif>
			<cfif not StructIsEmpty(collection)>
				<h2>#stText.Search.Collection# #url.collection#</h2>
				<table class="maintbl">
					<tbody>
						<tr>
							<th scope="row">#stText.Search.Name#</th>
							<td>#collection.name#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.Mapped#</th>
							<td>#collection.mapped#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.Online#</th>
							<td>#collection.online#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.External#</th>
							<td>#collection.external#</td>			
						</tr>
						<tr>
							<th scope="row">#stText.Search.Language#</th>
							<td>#collection.language#</td>			
						</tr>
						<tr>
							<th scope="row">#stText.Search.Last_Update#</th>
							<td>#DateFormat(collection.LastModified,"yyyy-mm-dd")# #TimeFormat(collection.LastModified,"HH:mm")#</td>
						</tr>
						<tr>
							<th scope="row">#stText.Search.Path#</th>
							<td>#collection.path#</td>
						</tr>
					</tbody>
				</table>

				<!--- 
					@todo list index and allow delete
					@todo add/update file index
					@todo add/update url index
				---><!--- 
				Create Index --->
				<h2>#stText.Search.PathAction#</h2>
				<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&collection=#collection.name#" method="post">
					<table class="maintbl">
						<tbody>
							<tr>
								<th scope="row">#stText.Search.FileExtensions#</th>
								<td><cfinputClassic type="text" name="extensions" value=".html, .htm, .cfm, .cfml" class="large" required="yes" message="#stText.Search.FileExtensionsMissing#"></td>
							</tr>
							<tr>
								<th scope="row">#stText.Search.DirectoryPath#</th>
								<td><cfinputClassic type="text" name="path" value="" class="large" required="yes" message="#stText.Search.DirectoryPathMissing#"></td>
							</tr>
							<tr>
								<th scope="row">#stText.Search.IndexSubdirs#</th>
								<td><input type="checkbox" class="checkbox" name="recurse" value="yes"></td>
							</tr>
							<tr>
								<th scope="row">#stText.Search.URL#</th>
								<td><cfinputClassic type="text" name="url" value="" class="large" required="no"></td>
							</tr>
							<tr>
								<th scope="row">#stText.Search.Language#</th>
								<td><select name="language" class="medium">
									<cfloop collection="#stText.SearchLng#" item="key">
										<option value="#key#" <cfif key eq "english">selected</cfif>>#stText.SearchLng[key]#</option>
									</cfloop>
								</select></td>
							</tr>
							<cfmodule template="remoteclients.cfm" colspan="2">
						</tbody>
						<tfoot>
							<tr>
								<td colspan="2">
									<!--- 
									@todo kein funktioneller javascript
									 --->
									<input onclick="window.location='#request.self#?action=#url.action#';" 
										type="button" class="bl button cancel" name="canel" value="#stText.Buttons.Cancel#">
									<input type="hidden" name="run" value="index">
									<input type="submit" class="br button submit" name="_run" value="#stText.Buttons.Update#">
								</td>
							</tr>
						</tfoot>
					</table>
				</cfformClassic>
				
				<h2>#stText.Search.SearchTheCollection#</h2>
				<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&collection=#collection.name#&search=1" method="post">
					<table class="maintbl">
						<tbody>
							<tr>
								<th scope="row">#stText.Search.SearchTerm#</th>
								<td>
									<cfif StructKeyExists(form,"searchterm")>
										<cfset session.searchterm=form.searchterm>
									</cfif>
									<cfparam name="session.searchterm" default="">
									<cfinputClassic type="text" name="searchterm" value="#session.searchterm#" class="large"
										required="yes" message="#stText.Search.SearchTermMissing#">
								</td>
							</tr>
				<!--- <tr>
					<td class="tblHead" width="50"><cfoutput>#stText.Search.Language#</cfoutput></td>
					<td class="tblContent" width="300"><select name="language">
							<cfoutput>
								<cfloop from="1" to="25" index="iLng">
									<option value="#stText.SearchLng[iLng][1]#" <cfif stText.SearchLng[iLng][1] eq "english">selected</cfif>>#stText.SearchLng[iLng][2]#</option>
								</cfloop>
							</cfoutput>
						</select></td>
				</tr> --->
						</tbody>
						<tfoot>
							<tr><td colspan="2">
								<input type="submit" class="bs button submit" name="search" value="#stText.Buttons.Search#">
							</td></tr>
						</tfoot>
					</table>
				</cfformClassic>

				<cfif StructKeyExists(form,'searchterm')>
					<cfsearch 
						collection="#url.collection#" 
						name="result" type="SIMPLE" 
						criteria="#form.searchterm#">
					<cfset session.result=variables.result>
				<cfelseif StructKeyExists(session,'result')>
					<cfset result=session.result>
				</cfif>
				<cfif StructKeyExists(url,'search') and StructKeyExists(variables,'result')>
					<cfparam name="url.startrow" default="1">
					<h2>#stText.Search.ResultOfTheSearch#</h2>
					<cfif result.recordCount EQ 0>
						<div class="warning">#stText.Search.noresult#</div>
					<cfelse>
						<cfset endrow=iif(result.recordCount GT url.startrow+9,de(url.startrow+9),de(result.recordCount))>
						<table class="maintbl">
							<thead>
								<tr>
									<th width="30">
										<cfif url.startrow GT 10>
											<a style="text-decoration:none" href="#request.self#?action=#url.action#&collection=#collection.name#&startrow=#url.startrow-10#&search=1">&lt;&lt;</a>
										<cfelse>&nbsp;</cfif>
									</th>
									<th>
										<cfscript>
											stResult=replace(stText.Search.result,'{startrow}',url.startrow);
											stResult=replace(stResult,'{endrow}',endrow);
											stResult=replace(stResult,'{recordcount}',result.recordCount);
											stResult=replace(stResult,'{recordssearched}',result.recordssearched);
										</cfscript>
										#stResult#
									</th>
									<th width="30">
										<cfif url.startrow+10 LTE result.recordcount>
											<a style="text-decoration:none" href="#request.self#?action=#url.action#&collection=#collection.name#&startrow=#url.startrow+10#&search=1">&gt;&gt;</a>
										<cfelse>&nbsp;</cfif>
									</th>
								</tr>
							</thead>
							<tbody>
								<cfloop query="result" startrow="#url.startrow#" endrow="#url.startrow+9#">
									<tr>
										<td colspan="3">
											<h4><cfif len(trim(result.title)) EQ 0>{no title}<cfelse>#result.title#</cfif></h4>
											<div class="comment">#result.summary#</div>
										</td>
									</tr>
								</cfloop>
							</tbody>
						</table>
					</cfif>
				</cfif>
			</cfif>	
		</cfoutput>
	</cfif>
</cfif>
