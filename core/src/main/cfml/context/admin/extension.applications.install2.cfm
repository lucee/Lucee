<!--- 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cftry>
	<!---- load ExtensionManager ---->
    <cfset manager=createObject('component','extension.ExtensionManager')>
    
    <cfset display=true>
	
	<cfif structKeyExists(url, 'uploadExt')>
		<cfset detail = session.uploadExtDetails />
		<cfset appendURL = "&uploadExt=1" />
	<cfelse>
    	<cfset detail=getDetailByUid(url.uid)>
		<cfset appendURL = "" />
	</cfif>

    
	<cfset isUpdate=StructKeyExists(detail,'installed')>
    <cfparam name="config" default="#manager.createConfig()#">
    
    <cfparam name="url.step" default="1">
    
    <cfif StructKeyExists(form,'previous')>
        <cfset url.step-->
    <cfelseif StructKeyExists(form,'next')>
        <cfset url.step++>
    </cfif>
    
    <!--- create app folder --->
	<cftry>
		<cfset dest=manager.createUIDFolder(url.uid)>
		<cfcatch>
			<cfset dest=createUIDFolder(url.uid)>
		</cfcatch>
	</cftry>
    
    <!--- copy lucee extension package to destination directory, if it wasn't copied/downloaded yet --->
    <cfset destFile=manager.copyAppFile(detail.data,dest).destFile>
	
	<!--- did not agree with license? Remove the extension --->
	<cfif form.mainAction eq stText.Buttons.dontagree>
        <cfif FileExists(destFile)><cfset fileDelete(destFile)></cfif>
		<!--- session var, so we can show a msg on the next page--->
		<cfset session.extremoved = 1 />
		<cflocation url="#request.self#?action=#url.action#" addtoken="no" />
	</cfif>
    
    <!---- load xml ---->
    <cfset zip="zip://"&destFile&"!/">
    <cfset configFile=zip&"config.xml">
	<cfif not FileExists(configFile)>
		<cfset fileDelete(destFile)>
        <cfthrow message="missing config file in extension package" />
    </cfif>
    
    <cfset install=manager.loadInstallCFC(zip)>

	<cfcatch>
    	<cfset display=false>
		<cfif structKeyExists(variables, "destFile")>
			<cfif FileExists(destFile)><cfset fileDelete(destFile)></cfif>
		</cfif>
		<cfset printError(cfcatch,true)>
    </cfcatch>
</cftry>
        

<!--- validate --->
<cfset valid=true>
<cfif StructKeyExists(form,"step") and not StructKeyExists(form,"previous")>
    <cftry>
    	<cfset rst=struct(fields:struct(),common:'')>
        <cfset install.validate(rst,zip,config,form.step)>
        
        <cfif len(rst.common) or structCount(rst.fields)>
        	<cfset valid=false>
            <cfset url.step=form.step>
            
            <cfif structCount(rst.fields)>
            	<cfset err=rst.fields>
            </cfif>
            <cfif len(rst.common)>
                <cfset printError(struct(message:rst.common))>
            </cfif>
        </cfif>
        
        <cfcatch>
			<cfset valid=false>
            <cfset url.step=form.step>
            <cfset printError(cfcatch,true)>
        </cfcatch>
    </cftry>
</cfif>

<!--- load XML --->
<cftry>
    <cfset xmlConfig=XMLParse(configFile,false).config>
    <cfset extForm=manager.translateXML(install,config,xmlConfig)>
	<cfset steps=extForm.getSteps()>

	<cfcatch>
    	<cfset display=false>
		<cfif FileExists(destFile)><cfset fileDelete(destFile)></cfif>
    	<cfset printError(cfcatch,true)>
    </cfcatch>
</cftry>

<!--- install --->
<cfset done=true>
<cfif (valid and StructKeyExists(form,"install")) or ( StructKeyExists(variables,"steps") and arrayLen(steps) EQ 0)>
    <cftry>
		<cfset rst=struct(fields:struct(),common:'')>
    	<cfif isUpdate>
			<cfset message=install.update(rst,zip,config,detail.installed.config)>
        <cfelse>
            <cfset message=install.install(rst,zip,config)>
        </cfif>
        
        <cfif not IsDefined('message') or not len(message)>
        	<cfset message=stText.ext.installDone>
        </cfif>
        <cfif len(rst.common) or structCount(rst.fields)>
        	<cfset done=false>
            
            <cfif structCount(rst.fields)>
            	<cfset err=rst.fields>
            </cfif>
            <cfif len(rst.common)>
                <cfset printError(struct(message:rst.common))>
            </cfif>
        </cfif>
        
        <cfif done>
            <cfadmin 
                action="updateExtension"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                
                config="#config#"
                provider="#detail.data.provider#"
                
                id="#detail.data.id#"
                version="#detail.data.version#"
                name="#detail.data.name#"
                label="#detail.data.label#"
                description="#detail.data.description#"	
                category="#detail.data.category#"	
                image="#detail.data.image#"	
                
                author="#detail.data.author#"	
                codename ="#detail.data.codename#"	
                video="#detail.data.video#"	
                support="#detail.data.support#"	
                documentation="#detail.data.documentation#"	
                forum="#detail.data.forum#"	
                mailinglist="#detail.data.mailinglist#"	
                network="#detail.data.network#"	
                _type="#detail.data.type#"
                created="#detail.data.created#"
                >
            <cfset session.confirm.text=message>    
            <cfset session.confirm.success=true>    
            <cflocation url="#request.self#?action=#url.action#&action2=confirm">
		</cfif>
        
        
        <cfcatch>
        	<cfset done=false>
            <cfset printError(cfcatch,true)>
        </cfcatch>
    </cftry>
</cfif>



<!--- generate form --->
<cfset fields="">

<cfif IsDefined('err._message')>
	<cfset printError(cfcatch,true)>
</cfif>

<cfif display and arrayLen(steps) GT 0>
	<cfif url.step GT arrayLen(steps)><cfset url.step=arrayLen(steps)></cfif>
	<cfif url.step LT 1><cfset url.step=1></cfif>
	<cfset stepLen=arrayLen(steps)>
	<cfset isFirst=url.step EQ 1>
	<cfset isLast=url.step EQ arrayLen(steps)>
	<cfset formPrefix="dyn#url.step#_">
	<cfset groups=steps[url.step].getGroups()>
	
	<cfoutput>
		<cfif len(trim(steps[url.step].getLabel())&trim(steps[url.step].getDescription()))>
			<cfif len(trim(steps[url.step].getLabel()))>
				<h2>#steps[url.step].getLabel()#</h2>
			</cfif>
			<div class="pageintro">#steps[url.step].getDescription()#</div>
		</cfif>
		<cfform onerror="customError" action="#request.self#?action=#url.action#&action2=install2&uid=#url.uid#&step=#url.step##appendURL#" method="post" enctype="multipart/form-data">
			<cfif stepLen GT 1>
				<cfset stepOf=replace(stText.ext.stepOf,'{current}',url.step)>
				<cfset stepOf=replace(stepOf,'{total}',arrayLen(steps))>
				<div class="right">
					#stepOf#
				</div>
			</cfif>
			<cfset variables.hiddenFieldsHtml = "" />
			<cfloop array="#groups#" index="group">
				<cfif len(trim(group.getLabel()))>
					<h3>#group.getLabel()#</h3>
				</cfif>
				<div class="itemintro">#group.getDescription()#</div>
				<table class="maintbl">
					<tbody>
						<cfset items=group.getItems()>
						<cfloop array="#items#" index="item">
							<!--- value --->
							<cfif StructKeyExists(form,formPrefix&item.getName())>
								<cfset value=form[formPrefix&item.getName()]>	
								<cfset variables.fromForm=value>
							<cfelseif StructKeyExists(detail,"installed") and StructKeyExists(detail.installed.config,item.getName())>
								<!--- TODO direkt geht nicht !---><cfset tmp=detail.installed.config>
								<cfset value=tmp[item.getName()]>	
							<cfelse>
								<cfset value=item.getValue()>
							</cfif>

							<cfset fields&=","&formPrefix&item.getName()>
							<cfset isError=isDefined('err.'&item.getName())><!--- @todo wenn das feld message heisst gibt es konflikt --->
							<cfif item.getType() EQ "hidden">
								<cfset variables.hiddenFieldsHtml &= '#server.separator.line#<input type="hidden" name="#formPrefix##item.getName()#" value="#HTMLEditFormat(item.getValue())#" />' />
							<cfelse>
								<tr>
									<cfif trim(item.getLabel()) neq "">
										<th scope="row">#item.getLabel()#</th>
										<td>
									<cfelse>
										<td colspan="2">
									</cfif>
										<cfif isError>
											<div class="error">#err[item.getName()]#</div>
										</cfif>
										<!--- select --->
										<cfif item.getType() EQ "select">
											<cfset options=item.getOptions()>
											<cfif arrayLen(options)>
												<select name="#formPrefix##item.getName()#" class="large">
													<cfloop array="#options#" index="option">
														<cfif structKeyExists(variables, 'fromForm')>
															<cfset selected = variables.fromForm EQ option.getValue()>
														<cfelse>
															<cfset selected=option.getSelected()>
														</cfif>
														<option value="#HTMLEditFormat(option.getValue())#" <cfif selected>selected="selected"</cfif>>#option.getLabel()#</option>
													</cfloop>
												</select>
											<cfelse>
												<input type="checkbox" class="checkbox" name="#formPrefix##item.getName()#" value="#HTMLEditFormat(item.getValue())#"  <cfif item.getSelected()> checked="checked"</cfif>/>
											</cfif>
										<!--- radio/checkbox --->
										<cfelseif item.getType() EQ "radio" or item.getType() EQ "checkbox">
											<cfset options=item.getOptions()>
											<cfif arrayLen(options)>
												<table class="optionslist">
													<cfloop array="#options#" index="option">
														<cfif structKeyExists(variables, 'fromForm')>
															<cfset selected = variables.fromForm EQ option.getValue()>
														<cfelse>
															<cfset selected=option.getSelected()>
														</cfif>
														<tr>
															<td><input type="#item.getType()#" class="#item.getType()#" name="#formPrefix##item.getName()#" value="#HTMLEditFormat(option.getValue())#" <cfif selected> checked="checked"</cfif> />
															</td>
															<td>
																#option.getLabel()#
																<cfif len(trim(option.getDescription()))>
																	<br /><span class="comment inline">#option.getDescription()#</span>
																</cfif>
															</td>
														</tr>
													</cfloop>
												</table>
											<cfelse>
												<cfif structKeyExists(variables, 'fromForm')>
													<cfset selected = variables.fromForm EQ item.getValue()>
												<cfelse>
													<cfset selected=item.getSelected()>
												</cfif>
											   <input type="#item.getType()#" class="#item.getType()#" name="#formPrefix##item.getName()#" value="#HTMLEditFormat(value)#"<cfif selected> checked="checked"</cfif> />
											</cfif>
										<!--- text --->
										<cfelse>
											<input type="#item.getType()#" name="#formPrefix##item.getName()#" value="#HTMLEditFormat(value)#" class="large" />
										</cfif>
										
										<cfif len(trim(item.getDescription()))>
											<div class="comment inline">#item.getDescription()#</div>
										</cfif>
									</td>
								</tr>
							</cfif>
						</cfloop>
					</tbody>
				</table>
			</cfloop>
			<table class="maintbl">
				<tfoot>
				    <tr>
						<td colspan="2">
							#variables.hiddenFieldsHtml#
							<input type="hidden" name="step" value="#url.step#">
							<input type="hidden" name="repPath" value="#zip#">
							<input type="hidden" name="fields" value="#ListCompact(fields)#">
							<cfloop collection="#form#" item="key">
								<cfif len(key) gt 3 and left(key,3) EQ "dyn">
									<cfset stp=mid(key,4,find('_',key)-4)>
									<cfif stp NEQ url.step><input type="hidden" name="#key#" value="#HTMLEditFormat( form[key])#"></cfif>
								</cfif>
							</cfloop>
							<cfif stepLen EQ 1>
								<input type="submit" class="button submit" name="install" value="#stText.Buttons[iif(not StructKeyExists(detail,"installed"),de('install'),de('update'))]#">
							<cfelseif isFirst>
								<input type="submit" class="button submit" name="next" value="#stText.Buttons.next#">
							<cfelseif isLast>
								<input type="submit" class="button submit" name="previous" value="#stText.Buttons.previous#">
								<input type="submit" class="button submit" name="install" value="#stText.Buttons[iif(not StructKeyExists(detail,"installed"),de('install'),de('update'))]#">
							<cfelse>
								<input type="submit" class="button submit" name="previous" value="#stText.Buttons.previous#">
								<input type="submit" class="button submit" name="next" value="#stText.Buttons.next#">
							</cfif>
	    				</td>
				    </tr>
				</tfoot>
			</table>
		</cfform>
	</cfoutput>
</cfif>