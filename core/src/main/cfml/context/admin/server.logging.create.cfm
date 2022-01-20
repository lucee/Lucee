<cffunction name="addZero">
	<cfargument name="str">
 <!---   <while len(str) LT 2>
    	<cfset str="0"&str>
    </while>--->
    <cfreturn arguments.str>
</cffunction>

<cfset goto="list">
<cftry>
	<cfset stVeritfyMessages = StructNew()>


	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		<cfcase value="#stText.Buttons.submit#">
			<cfset custom=struct()>
			<cfset layoutArgs={}>
			<cfset appenderArgs={}>

			<!--- custom --->
			<cfloop collection="#form#" item="key">
				<cfif left(key,13) EQ "custompart_d_">
					<cfset name=mid(key,14,10000)>
					<cfset custom[name]=(form["custompart_d_"&name]*86400)+(form["custompart_h_"&name]*3600)+(form["custompart_m_"&name]*60)+form["custompart_s_"&name]>
				</cfif>
			</cfloop>
			<cfloop collection="#form#" item="key">
				<cfif left(key,7) EQ "custom_">
					<cfset tmp=mid(key,8,10000)>
					<cfif isNumeric(listFirst(tmp,'_'))>
						<cfset tmp=listRest(tmp,'_')>
					</cfif>
					<cfif left(tmp,9) EQ "appender_">
						<cfset appenderArgs[mid(tmp,10,10000)]=form[key]>
					<cfelseif left(tmp,7) EQ "layout_">
						<cfset layoutArgs[mid(tmp,8,10000)]=form[key]>
					</cfif>

				</cfif>
			</cfloop>

			<cfset layoutClass=trim(form.appenderLayoutClass?:'')>
			<cfif isEmpty(layoutClass)><cfset layoutClass=trim(form.layoutClass)></cfif>
			
			<cfadmin
				action="updateLogSettings"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				name="#trim(form._name)#"
				level="#form.level#"
				appenderClass="#trim(form.appenderClass)#"
				appenderBundleName="#trim(form.appenderBundleName?:'')#"
				appenderBundleVersion="#trim(form.appenderBundleVersion?:'')#"
				appenderArgs="#appenderArgs#"
				layoutClass="#layoutClass#"
				layoutBundleName="#trim(form.layoutBundleName?:'')#"
				layoutBundleVersion="#trim(form.layoutBundleVersion?:'')#"
				layoutArgs="#(layoutArgs)#"

				remoteClients="#request.getRemoteClients()#">

		</cfcase>
	</cfswitch>
	<cfcatch><cfrethrow>
		<cfset error.message=cfcatch.message>
		<cfset error.detail=cfcatch.Detail>
		<cfset error.cfcatch=cfcatch>
	</cfcatch>
</cftry>
<!---
Redirtect to entry --->
<cfset __name=StructKeyExists(url,'name')?url.name:form._name>

<cfif cgi.request_method EQ "POST" and error.message EQ "" and form.mainAction neq "none">
	<cfif goto EQ "create">
		<cflocation url="#request.self#?action=#url.action#&action2=#goto#&name=#__name#" addtoken="no">
	</cfif>
	<cflocation url="#request.self#?action=#url.action#" addtoken="no">




</cfif>

<cfset isNew=false>
<cfif StructKeyExists(url,'name')>
	<cfloop query="logs" >
		<cfif hash(logs.name) EQ url.name>
			<cfset log=querySlice(logs,logs.currentrow,1)>
			<cfset layout=layouts[log.layoutClass]?:nullValue()>
			<cfset appender=appenders[log.appenderClass]?:nullValue()>
		</cfif>
	</cfloop>
<cfelse>
	<cfset isNew=true>
	<cfset log=struct()>
	<cfset log.name=form._name>
	
	<cfset log.appenderClass=trim(form.appenderClass)>
	<cfset log.appenderBundleName=trim(form.appenderBundleName?:'')>
	<cfset log.appenderBundleVersion=trim(form.appenderBundleVersion?:'')>
	
	<cfset log.layoutClass=trim(form.layoutClass)>
	<cfset log.layoutBundleName=trim(form.layoutBundleName?:'')>
	<cfset log.layoutBundleVersion=trim(form.layoutBundleVersion?:'')>
	
	<cfset log.appenderArgs={}>
	<cfset log.layoutArgs={}>
	<cfset log.level="ERROR">
	<cfset layout=layouts[log.layoutClass]>
	<cfset appender=isNull(appenders[log.appenderClass])?nullValue():appenders[trim(log.appenderClass)]>
</cfif>

<cfoutput>
	<!---
	Error Output --->
	<cfset printError(error)>


<cfhtmlbody>

<script>
disableBlockUI=true;
active={};
var bodies={};

function enable(type,id){
	var fullId=type+"_"+id;

	var old=active[type];
	if(old==fullId) return;
	active[type]=fullId;


	var btn=$("##button_"+fullId);

	$(document).ready(function(){
			//$('.button submit').css('background','url("")');
			btn.css('background-color','#request.adminType=="web"?'##39c':'##c00'#');
			btn.css('color','white');
			$('##button_'+old).css('background-color','');

			bodies[old]=$('##div_'+old).detach();

			bodies[fullId].appendTo("##group_"+type);
	});
}

function hideLayout() {
	$("##allgroup_layout").hide();

}
function showLayout() {
	$("##allgroup_layout").show();
}
</script>

</cfhtmlbody>


	<h1>Log "#log.name#"</h1>
	<div class="pageintro">#stText.Settings.logging.detailDesc#</div>
	<cfformClassic onerror="customError" action="#request.self#?action=#url.action#&action2=create#iif(isDefined('url.name'),de('&name=##url.name##'),de(''))#" method="post">
		<cfinputClassic type="hidden" name="_name" value="#log.name#" >
		<table class="maintbl">
			<tbody>
				<tr>
					<th scope="row">#stText.Settings.logging.Level#</th>
					<td><select name="level">
						<cfloop list="TRACE,DEBUG,INFO,WARN,ERROR,FATAL" item="ll"><option<cfif log.level EQ ll> selected</cfif>>#ll#</option></cfloop>
					</select></td>
				</tr>
				<!---
				<cfif !isNull(appender) && !arrayLen(appender.getCustomFields())>
				<tr>
					<th scope="row">#stText.Settings.logging.Appender#</th>
					<td>#appender.getLabel()#</td>
				</tr>
				</cfif>
				<cfif isNull(layout) or !arrayLen(layout.getCustomFields())>
				<tr>
					<th scope="row">#stText.Settings.logging.Layout#</th>
					<td>#isNull(layout)?'':layout.getLabel()#</td>
				</tr>
				</cfif>
			--->
			</tbody>
		</table>
<!--- LOOP APPENDER|LAYOUT --->
		<cfloop list="appender,layout" item="_name">
		<cfset argsCol=_name&"Args">
		<cfif isNull(variables[_name])>
			<xcfcontinue>
		</cfif>
		
		
		<cfset disable=false>
		<cfif _name=="layout" and !isNull(variables["appender"])>
			<cfset disable=true>
			<cfset disable=!isNull(variables["appender"].getLayout)>
		</cfif>
		
		<div id="allgroup_#_name#" <cfif disable>style="display: none;"</cfif>> 
		<cfset drivers=variables[_name&"s"]>
		<cfset _driver=isNull(variables[_name])?drivers[structKeyArray(drivers)[1]]:variables[_name]>
		<!--- <cfif !arrayLen(driver.getCustomFields())><cfbreak></cfif>--->
		<br />
		
		<h2>#ucFirst(_name)#</h2>
		<cfset count=0>
		<cfset len=structCount(drivers)>

		<cfloop collection="#drivers#" index="driverClass" item="driver">
			<cfset count++>
			<cfset orientation="bm">
			<cfif count==1><cfset orientation="bl"></cfif>
			<cfif count==len><cfset orientation="br"></cfif>
			<cfset _id=hash(driver.getClass(),'quick')>
			<cfset id="#_name#_#_id#">
			<cfset active=!isNull(_driver) && driver.getClass() EQ _driver.getClass()>
		<input id="button_#id#" onclick="enable('#_name#','#_id#');<cfif _name EQ 'appender'>#structKeyExists(driver,'getLayout')?'hide':'show'#Layout();</cfif>"
				type="button"
				class="#orientation# button submit"
				name="change#_name#"
				<cfif !isNull(_driver) && driver.getClass() EQ _driver.getClass()> style="color:white;background-color:#request.adminType=="web"?'##39c':'##c00'#;"</cfif>
				value="#driver.getLabel()#">
		</cfloop>
		<div id="group_#_name#">
		<cfset cnt=0>
		<cfloop collection="#drivers#" index="driverClass" item="driver">
			<cfset cnt++>
			<cfset id="#_name#_#hash(driver.getClass(),'quick')#">
			<cfset active=!isNull(_driver) && driver.getClass() EQ _driver.getClass()>
		<div id="div_#id#">
			
		<input type="hidden" name="#_name#Class" value="#driver.getClass()#">
		<input type="hidden" name="#_name#BundleName" 
			value="#structKeyExists(driver,'getBundleName')?driver.getBundleName():''#">
		<input type="hidden" name="#_name#BundleVersion" 
			value="#structKeyExists(driver,'getBundleVersion')?driver.getBundleVersion():''#">
		<cfif _name=="appender">
			<input type="hidden" name="appenderLayoutClass" value="#isNull(driver.getLayout)?'':driver.getLayout()#">
		</cfif>

		<br>#driver.getDescription()#
		<table class="maintbl">
			<tbody>
				<cfset custom=log[argsCol]>
				<cfloop array="#driver.getCustomFields()#" index="field">
					<cfif isInstanceOf(field,"Group")>
							</tbody>
						</table>
						<h#field.getLevel()#>#field.getDisplayName()#</h#field.getLevel()#>
						<div class="itemintro">#field.getDescription()#</div>
						<table class="maintbl">
							<tbody>
						<cfcontinue>
					</cfif>

					<cfset doBR=true>
					<cfif !active>
						<cfset default=field.getDefaultValue()>
					<cfelseif StructKeyExists(custom,field.getName())>
						<cfset default=custom[field.getName()]>
					<cfelseif isNew>
						<cfset default=field.getDefaultValue()>
					<cfelse>
						<cfset default="">
					</cfif>
					<cfset type=field.getType()>
					<tr>
						<th scope="row">#field.getDisplayName()#</th>
						<td>
							<cfif type EQ "text" or type EQ "password">
								<cfinputClassic type="#type#"
									name="custom_#cnt#_#_name#_#field.getName()#"
									value="#default#" class="large" required="#field.getRequired()#"
									message="Missing value for field #field.getDisplayName()#">
							<cfelseif type EQ "textarea">
								<textarea class="large" style="height:70px;" name="custom_#cnt#_#_name#_#field.getName()#">#default#</textarea>
							<cfelseif type EQ "time">
								<cfsilent>
									<cfset doBR=false>
									<cfif len(default) EQ 0>
										<cfset default=0>
									<cfelse>
										<cfset default=default+0>
									</cfif>

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
								<table class="maintbl autowidth">
									<thead>
										<tr>
											<th>#stText.General.Days#</td>
											<th>#stText.General.Hours#</td>
											<th>#stText.General.Minutes#</td>
											<th>#stText.General.Seconds#</td>
										</tr>
									</thead>
									<tbody>
										<tr>
											<td><cfinputClassic type="text"
												name="custompart_d_#_name#_#field.getName()#"
												value="#addZero(d)#" class="number" required="#field.getRequired()#"   validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text"
												name="custompart_h_#_name#_#field.getName()#"
												value="#addZero(h)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text"
												name="custompart_m_#_name#_#field.getName()#"
												value="#addZero(m)#" class="number" required="#field.getRequired()#"  maxlength="2" validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
											<td><cfinputClassic type="text"
												name="custompart_s_#_name#_#field.getName()#"
												value="#addZero(s)#" class="number" required="#field.getRequired()#"  maxlength="2"  validate="integer"
												message="Missing value for field #field.getDisplayName()#"></td>
										</tr>
									</tbody>
								</table>
							<cfelseif type EQ "select">
								<cfif default EQ field.getDefaultValue() and field.getRequired()>
									<cfset default=listFirst(default)>
								</cfif>
								<select name="custom_#cnt#_#_name#_#field.getName()#">
									<cfif not field.getRequired()><option value=""> ---------- </option></cfif>
									<cfloop index="item" list="#field.getValues()#">
										<option <cfif item EQ default>selected="selected"</cfif> >#item#</option>
									</cfloop>
								</select>
							<cfelseif type EQ "radio" or type EQ "checkbox">
								<cfset desc=field.getDescription()>
								<cfif isStruct(desc) and StructKeyExists(desc,'_top')>
									<div class="comment">#desc._top#</div>
								</cfif>
								<cfif listLen(field.getValues()) GT 1>
									<ul class="radiolist">
										<cfloop index="item" list="#field.getValues()#">
											<li>
												<label>
													<cfinputClassic type="#type#" class="#type#" name="custom_#cnt#_#_name#_#field.getName()#" value="#item#" checked="#item EQ default#">
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
									<cfinputClassic type="#type#" class="#type#" name="custom_#cnt#_#_name#_#field.getName()#" value="#item#" checked="#item EQ default#">
								</cfif>
								<cfif isStruct(desc) and StructKeyExists(desc,'_bottom')>
									<div class="comment">#desc._bottom#</div>
								</cfif>
							</cfif>
							<cfif isSimpleValue(field.getDescription()) and len(trim(field.getDescription()))>
								<div class="comment">#field.getDescription()#</div>
							</cfif>
						</td>
					</tr>
				</cfloop>
			</tbody>

		</table>
		</div>

			</cfloop>
		</div>

		<cfhtmlbody>

		<script>
			<cfloop collection="#drivers#" index="driverClass" item="driver">
				<cfset id="#_name#_#hash(driver.getClass(),'quick')#">
				<cfset active=!isNull(_driver) && driver.getClass() EQ _driver.getClass()>

			<cfif !active>
			$(document).ready(function(){
	    		bodies['#id#']=$('##div_#id#').detach();
			});
			<cfelse>
			active['#_name#']='#id#';
			</cfif>

			</cfloop>
		</script>

		</cfhtmlbody>
	</div>
		</cfloop>


		<table class="maintbl">
		<tfoot>
				<tr>
					<td colspan="2">
						<input type="submit" class="button submit" name="mainAction" value="#stText.Buttons.submit#">
					</td>
				</tr>
			</tfoot>
		</table>
	</cfformClassic>
</cfoutput>

<!---
<cfoutput>
<form action="#action('update')#" method="post">
	<table border="0" cellpadding="0" cellspacing="0" bgcolor="##FFCC00"
		style="background-color:##FFCC00;border-style:solid;border-color:##000000;border-width:1px;padding:10px;">
	<tr>
		<td valign="top" >
			<textarea style="background-color:##FFCC00;border-style:solid;border-color:##000000;border-width:0px;" name="note" cols="40" rows="10">#req.note#</textarea>
		</td>
	</tr>
	</table>
	<br />
	<input class="button submit" type="submit" name="submit" value="#lang.btnSubmit#" />
</form>

</cfoutput>
--->