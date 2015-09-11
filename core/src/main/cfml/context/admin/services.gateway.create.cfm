<cffunction name="addZero">
	<cfargument name="str">
 <!---   <while len(str) LT 2>
		<cfset str="0"&str>
	</while>--->
	<cfreturn str>
</cffunction>

<cfset isNew=false>
<cfif StructKeyExists(url,'id')>
	<cfloop query="entries" >
		<cfif hash(entries.id) EQ url.id>
			<cfset entry=querySlice(entries,entries.currentrow,1)>
			<cfset driver=entry.driver>
		</cfif> 
	</cfloop>
<cfelse>
	<cfif not StructKeyExists(form,"name")>
		<cflocation url="#request.self#" addtoken="no">
	</cfif>
	<cfset driver=drivers[form.name]>
	<cfset isNew=true>
	<cfset entry=struct()>
	<cfset entry.class=structKeyExists(driver,'getClass')?driver.getClass():"">
	<cfset entry.default=false>
	<cfset entry.id=form._id>
	<cfset entry.cfcpath=structKeyExists(driver,'getCFCPath')?driver.getCFCPath():"">
	<cfset entry.listenercfcpath=structKeyExists(driver,'getListenerPath')?driver.getListenerPath():"">
	<cfset entry.startupMode="automatic">
	<cfset entry.custom=struct()>
</cfif>


<cftry>
	<cfset stVeritfyMessages = StructNew()>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.submit#">
			<cfset custom=struct()>
		
			<!--- custom --->
			<cfloop collection="#form#" item="key">
				<cfif left(key,13) EQ "custompart_d_">
					<cfset name=mid(key,14,10000)>
					<cfset custom[name]=(form["custompart_d_"&name]*86400)+(form["custompart_h_"&name]*3600)+(form["custompart_m_"&name]*60)+form["custompart_s_"&name]>
				</cfif>
			</cfloop>	   
			<cfloop collection="#form#" item="key">
				<cfif left(key,7) EQ "custom_">
					<cfset custom[mid(key,8,10000)]=form[key]>
				</cfif>
			</cfloop>
			<!---<cfif not fileExists(trim(form.cfcPath))>
				<cfthrow message="#form.cfcPath# does not exist">
			</cfif>--->
			<cfif not structkeyExists(form,'listenerCfcPath')><cfset form.listenerCfcPath=""></cfif>
			<cfset driver.onBeforeUpdate(trim(form.listenerCfcPath),trim(form.startupMode),custom)>
		
			<cfadmin 
				action="updateGatewayEntry"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				
				
				id="#trim(form.id)#" 
				class="#trim(form.class)#" 
				cfcPath="#trim(form.cfcPath)#"
				listenerCfcPath="#trim(form.listenerCfcPath)#" 
				startupMode="#trim(form.startupMode)#" 
				custom="#custom#"
				
				remoteClients="#request.getRemoteClients()#">
					
		</cfcase>
	</cfswitch>
	<cfcatch>
		<cfset driver.onBeforeError(cfcatch)>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
	</cfcatch>
</cftry>
<!--- 
Redirtect to entry --->
<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq "none">
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">
</cfif>


<cfoutput>
	<!--- 
	Error Output--->
	<cfset printError(error)>

	<h2>#driver.getLabel()#</h2>
	<div class="pageintro">#driver.getDescription()#</div>
	<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=create#iif(isDefined('url.id'),de('&id=##url.id##'),de(''))#" method="post">
		<cfinput type="hidden" name="name" value="#listLast(getMetaData(driver).name,'.')#">
		<cfinput type="hidden" name="class" value="#entry.class#">
		<cfinput type="hidden" name="cfcPath" value="#entry.cfcPath#">
		<cfinput type="hidden" name="id" value="#entry.id#" >
		<cfinput type="hidden" name="_id" value="#entry.id#" >
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Settings.gateway.id#</th>
					<td>#entry.id#</td>
				</tr>
				<cfif driver.getListenerCfcMode() NEQ "none">
					<tr>
						<th scope="row">#stText.Settings.gateway.ListenerCfcPath#</th>
						<td><cfinput type="text" name="listenerCfcPath" value="#entry.listenerCfcPath#" required="#driver.getListenerCfcMode() EQ "required"#" class="large" message="Missing value for field listener CFC Path">
						<div class="comment">
							<cfif structKeyExists(driver,'getListenerCFCDescription')>
								#driver.getListenerCFCDescription()#
							<cfelse>
								#stText.Settings.gateway.ListenerCfcPathDesc#
							</cfif>
						</div></td>
					</tr>
				</cfif>
				<tr>
					<th scope="row">#stText.Settings.gateway.startupMode#</th>
					<td>
						<select name="startupMode" class="medium">
							<option value="automatic"<cfif entry.startupMode EQ "automatic"> selected="selected"</cfif>>#stText.Settings.gateway.startupModeAutomatic#</option>
							<option value="manual"<cfif entry.startupMode EQ "manual"> selected="selected"</cfif>>#stText.Settings.gateway.startupModeManual#</option>
							<option value="disabled"<cfif entry.startupMode EQ "disabled"> selected="selected"</cfif>>#stText.Settings.gateway.startupModeDisabled#</option>
						</select>
					</td>
				</tr>
			</tbody>
	
			<cfset custom=entry.custom>
			<cfloop array="#driver.getCustomFields()#" index="field">
				<cfif isInstanceOf(field,"Group")>
					</tbody></table>

					<h#field.getLevel()#>#field.getDisplayName()#</h#field.getLevel()#>
					<div class="itemintro">#field.getDescription()#</div>
					<table class="maintbl">
						<tbody>
					<cfcontinue>
				</cfif>
				<cfset doBR=true>
				<cfif StructKeyExists(custom,field.getName())>
					<cfset default=custom[field.getName()]>
				<cfelseif isNew>
					<cfset default=field.getDefaultValue()>
				<cfelse>
					<cfset default="">
				</cfif>
				<cfset type=field.getType()>
				<cfif type NEQ "hidden">
					<tr>
						<th scope="row">#field.getDisplayName()#</th>
						<td width="300">
				</cfif>
				<cfif type EQ "text" or type EQ "password">
					<cfinput type="#type#" 
						name="custom_#field.getName()#" 
						value="#default#" class="large" required="#field.getRequired()#" 
						message="Missing value for field #field.getDisplayName()#">
					<cfelseif type EQ "textarea">
						<textarea style="height:70px;" class="large" name="custom_#field.getName()#">#default#</textarea>
					<cfelseif type EQ "hidden">
						<cfinput type="hidden" name="custom_#field.getName()#" value="#default#">
					<cfelseif type EQ "time">
						<cfsilent>
							<cfset doBR=false>
							<cfset default=default+0>
							<cfset s=default>
							<cfset m=0>
							<cfset h=0>
							<cfset d=0>
							
							<cfif s GT 0>
								<cfset m=int(s/60)>
								<cfset s-=m*60>
							</cfif>
							<cfif m GT 0>
								<cfset h=int(m/60)>
								<cfset m-=h*60>
							</cfif>
							<cfif h GT 0>
								<cfset d=int(h/24)>
								<cfset h-=d*24>
							</cfif>
						</cfsilent>
						<table class="maintbl" style="width:auto">
							<thead>
								<tr>
									<th>#stText.General.Days#</th>
									<th>#stText.General.Hours#</th>
									<th>#stText.General.Minutes#</th>
									<th>#stText.General.Seconds#</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><cfinput type="text" 
										name="custompart_d_#field.getName()#" 
										value="#addZero(d)#" class="number" required="#field.getRequired()#"   validate="integer"
										message="Missing value for field #field.getDisplayName()#"></td>
									<td><cfinput type="text" 
										name="custompart_h_#field.getName()#" 
										value="#addZero(h)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
										message="Missing value for field #field.getDisplayName()#"></td>
									<td><cfinput type="text" 
										name="custompart_m_#field.getName()#" 
										value="#addZero(m)#" class="number" required="#field.getRequired()#"  maxlength="2" validate="integer" 
										message="Missing value for field #field.getDisplayName()#"></td>
									<td><cfinput type="text" 
										name="custompart_s_#field.getName()#" 
										value="#addZero(s)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
										message="Missing value for field #field.getDisplayName()#"></td>
								</tr>
							</tbody>
						</table>
					<cfelseif type EQ "select">
						<cfif default EQ field.getDefaultValue() and field.getRequired()>
							<cfset default=listFirst(default)>
						</cfif>
						<select name="custom_#field.getName()#">
							<cfif not field.getRequired()><option value=""> ---------- </option></cfif>
							<cfif len(trim(default))>
								<cfloop index="item" list="#field.getValues()#">
									<option <cfif item EQ default>selected="selected"</cfif> >#item#</option>
								</cfloop>
							</cfif>
						</select>
					<cfelseif type EQ "radio" or type EQ "checkbox">
						<cfset desc=field.getDescription()>
						<cfif isStruct(desc) and StructKeyExists(desc,'_top')>
							<div class="comment" style="padding-bottom:4px">#desc._top#</div>
						</cfif>
						<cfif listLen(field.getValues()) GT 1>
							<ul class="radiolist">
								<cfloop index="item" list="#field.getValues()#">
									<li>
										<label>
											<cfinput type="#type#" class="#type#" name="custom_#field.getName()#" value="#item#" checked="#item EQ default#">
											<b>#item#</b>
										</label>
										<cfif isStruct(desc) and StructKeyExists(desc,item)>
											<div class="comment" style="padding-bottom:4px">#desc[item]#</div>
										</cfif>
									</li>
								</cfloop>
							</ul>
						<cfelse>
							<cfset item = field.getValues() />
							<cfinput type="#type#" class="#type#" name="custom_#field.getName()#" value="#item#" checked="#item EQ default#">
						</cfif>
						<cfif isStruct(desc) and StructKeyExists(desc,'_bottom')>
							<div class="comment" style="padding-top:4px">#desc._bottom#</div>
						</cfif>
					</cfif>
					<cfif type NEQ "hidden">
						<cfif isSimpleValue(field.getDescription()) and len(trim(field.getDescription()))>
							<div class="comment">#field.getDescription()#</div>
						</cfif>
					</td>
				</tr>
					</cfif>
				</cfloop>
			</tbody>
			<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="bs button submit" name="mainAction" value="#stText.Buttons.submit#">
<!---						<input onclick="window.location='#request.self#?action=#url.action#';" type="button" class="button cancel" name="cancel" value="#stText.Buttons.Cancel#">
--->
					</td>
				</tr>
			</tfoot>
		</table>
	</cfform>
</cfoutput>