<cfoutput>
	<h2>#stText.debug.settingTitle#</h2>
	<div class="pageintro">
		#stText.debug.settingDesc#
	</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.debug.maxLogs#</th>
					<td>
						<select name="maxLogs">
							<cfset selected=false>
							<cfloop list="10,20,50,100,200,500,1000" index="idx">
								<option <cfif idx EQ setting.maxLogs><cfset selected=true>selected="selected"</cfif> value="#idx#">#idx#</option>
							</cfloop>
							<cfif !selected>
								<option selected="selected" value="#setting.maxLogs#">#setting.maxLogs#</option>
							</cfif>
						</select>
					</td>
				</tr>
				<!---
				<tr>
					<th scope="row">#stText.debug.minExeTime#</th>
					<td><input name="minExeTime" value="0" style="width:60px"/> ms<br /><span class="comment">#stText.debug.minExeTimeDesc#</span></td>
				</tr>
				<tr>
					<th scope="row">#stText.debug.pathRestriction#</th>
					<td><input name="minExeTime" value="0" style="width:60px"/> ms<br /><span class="comment">#stText.debug.pathRestrictionDesc#</span></td>
				</tr>
				--->
				<cfmodule template="remoteclients.cfm" colspan="2">
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bl button submit" name="mainAction" value="#stText.Buttons.Update#">
						<input type="submit" class="bm button submit" name="mainAction" value="#stText.Buttons.Purge#">
						<input type="reset" class="<cfif request.adminType EQ "web">bm<cfelse>br</cfif> button reset" name="cancel" value="#stText.Buttons.Cancel#">
						<cfif request.adminType EQ "web"><input class="br button submit" type="submit" name="mainAction" value="#stText.Buttons.resetServerAdmin#"></cfif>
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
	
	<cfif isWeb>
		<!---<h2>#stText.debug.filterTitle#</h2>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
		<table class="tbl" width="740">
		<tr>
			<th scope="row">#stText.debug.minExeTime#</th>
			<td>
				<table class="tbl">
				<tr>
					<td class="tblHead" >Total</td
				</tr>
				<tr>
					<td><input name="minExeTimeTotal" value="0" style="width:60px"/></td>
				</tr>
				</table>
			</td>
		</tr>
		<tr>
			<th scope="row">#stText.debug.pathRestriction#</th>
			<td><textarea name="pathRestriction" cols="60" rows="10" style="width:100%"></textarea><br /><span class="comment">#stText.debug.pathRestrictionDesc#</span></td>
		</tr>
		<cfmodule template="remoteclients.cfm" colspan="2">
		<tr>
			<td colspan="2">
				<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.Update#">
				<input type="reset" class="reset" name="cancel" value="#stText.Buttons.Cancel#">
			</td>
		</tr>
		</cfformClassic>
		</table>
		<br /><br />--->
	
		<h2>#stText.debug.outputTitle#</h2>
		<div class="itemintro">#stText.debug.outputDesc#</div>
		<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post" name="debug_settings">
			<table class="maintbl">
				<thead>
					<tr>
						<th width="50%" rowspan="2">#stText.Debug.path#</th>
						<th width="35%" rowspan="2">#stText.Debug.reqTime#</th>
						<th width="15%" colspan="3">#stText.Debug.exeTime#</th>
					</tr>
					<tr>
						<th width="5%">#stText.Debug.exeTimeQuery#</th>
						<th width="5%">#stText.Debug.exeTimeApp#</th>
						<th width="5%">#stText.Debug.exeTimeTotal#</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>
							<input type="text" name="path" class="xlarge" value="#session.debugFilter.path#" />
							<div class="comment">#stText.Debug.filterPath#</div>
						</td>
					    <td nowrap><input type="text" name="starttime" class="xlarge" value="#LSDateFormat(session.debugFilter.starttime)# #LSTimeFormat(session.debugFilter.starttime)#" /></td>
					    <td nowrap><input type="text" name="query" class="number" value="#session.debugFilter.query#" /></td>
    					<td nowrap><input type="text" name="app" class="number" value="#session.debugFilter.app#" /></td>
    					<td nowrap><input type="text" name="total" class="number" value="#session.debugFilter.total#" /></td>
					</tr>
				</tbody>
				<tfoot>
					<tr>
						<td colspan="5"><input type="submit" name="mainAction" class="bs button submit" value="#stText.Debug.filter#" /></th>
					</tr>
				</tfoot>
				<cfif not arrayIsEmpty(logs)>
					<tbody>
						<cfloop from="#arrayLen(logs)#" to="1" index="i" step="-1">
							<cfset el=logs[i]>
							<cfset _total=0><cfloop query="el.pages"><cfset _total+=el.pages.total></cfloop>
							<cfset _query=0><cfloop query="el.pages"><cfset _query+=el.pages.query></cfloop>
							<cfset _app=0><cfloop query="el.pages"><cfset _app+=el.pages.app></cfloop>								
							<cfif 
								doFilter(session.debugFilter.path,_path,false) and 
								doFilterMin(session.debugFilter.query,_query) and 
								doFilterMin(session.debugFilter.app,_app) and 
								doFilterMin(session.debugFilter.total,_total)> 
								<tr>
									<td><a href="#request.self#?action=#url.action#&action2=detail&id=#hash(el.id&":"&el.startTime)#">#encodeForHtml(el.scope.cgi.REQUEST_URL)#</a></td>
									<td>#LSDateFormat(el.starttime)# #LSTimeFormat(el.starttime)#</td>
									<td nowrap align="right"><cfif listFirst(formatUnit(_query)," ") gt 0>#formatUnit(_query)#<cfelse>-</cfif></td>
									<td nowrap align="right">#formatUnit(_app)#</td>
									<td nowrap align="right">#formatUnit(_total)#</td>
								</tr>
							</cfif>
						</cfloop>
					</tbody>
				</cfif>
			</table>
		</cfformClassic>
	</cfif>
</cfoutput>
