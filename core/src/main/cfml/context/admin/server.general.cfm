<cfset error.message="">
<cfset error.detail="">


<!--- Component --->
<cfadmin 
	action="getComponent"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="component">
<cfset setting.component={
	compatibility:{
		baseComponentTemplate:component.strBaseComponentTemplateCFML,
		componentDumpTemplate:component.strComponentDumpTemplate,
		
		componentDataMemberDefaultAccess:'public',
		triggerDataMember:false,
		useShadow:true,
		componentLocalSearch:false,
		componentPathCache:false,
		deepSearch:false,
		baseComponentTemplateCFML:component.strBaseComponentTemplateCFML,
		baseComponentTemplateLucee:component.baseComponentTemplateLucee,
		componentDefaultImport:component.ComponentDefaultImport
	},
	strict:{
		baseComponentTemplate:component.strBaseComponentTemplateCFML,
		componentDumpTemplate:component.strComponentDumpTemplate,
		
		componentDataMemberDefaultAccess:'private',
		triggerDataMember:false,
		useShadow:false,
		componentLocalSearch:false,
		componentPathCache:false,
		deepSearch:false,
		baseComponentTemplateCFML:component.strBaseComponentTemplateCFML,
		baseComponentTemplateLucee:component.baseComponentTemplateLucee,
		componentDefaultImport:component.ComponentDefaultImport
	},
	speed:{
		baseComponentTemplate:component.strBaseComponentTemplateCFML,
		componentDumpTemplate:component.strComponentDumpTemplate,
		
		componentDataMemberDefaultAccess:'private',
		triggerDataMember:false,
		useShadow:false,
		componentLocalSearch:false,
		componentPathCache:false,
		deepSearch:false,
		baseComponentTemplateCFML:component.strBaseComponentTemplateCFML,
		baseComponentTemplateLucee:component.baseComponentTemplateLucee,
		componentDefaultImport:component.ComponentDefaultImport
	}
}>

<!--- Charset --->
<cfadmin 
	action="getCharset"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="charset">
<cfset setting.charset={
	compatibility:{
		templateCharset:charset.jreCharset,
		webCharset:'UTF-8',
		resourceCharset:charset.jreCharset
	},
	strict:{
		templateCharset:charset.jreCharset,
		webCharset:'UTF-8',
		resourceCharset:charset.jreCharset
	},
	speed:{
		templateCharset:charset.jreCharset,
		webCharset:'UTF-8',
		resourceCharset:charset.jreCharset
	}
}>

<!--- Scope --->
<cfadmin 
	action="getScope"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="scope">
<cfset setting.scope={
	compatibility:{
		sessionType:scope.sessionType,
		sessionTimeout:scope.sessionTimeout,
		applicationTimeout:scope.applicationTimeout,
		sessionManagement:scope.sessionManagement,
		clientManagement:scope.clientManagement,
		clientCookies:scope.clientCookies,
		domaincookies:scope.domaincookies,
		clientTimeout:scope.clientTimeout,
		clientStorage:scope.clientStorage,
		sessionStorage:scope.sessionStorage,
		cgiReadonly:scope.cgiReadonly,
				
		localMode:'update',
		scopeCascadingType:'standard',
		allowImplicidQueryCall:true,
		mergeFormAndUrl:false
	},
	strict:{
		sessionType:scope.sessionType,
		sessionTimeout:scope.sessionTimeout,
		applicationTimeout:scope.applicationTimeout,
		sessionManagement:scope.sessionManagement,
		clientManagement:scope.clientManagement,
		clientCookies:scope.clientCookies,
		domaincookies:scope.domaincookies,
		clientTimeout:scope.clientTimeout,
		clientStorage:scope.clientStorage,
		sessionStorage:scope.sessionStorage,
		cgiReadonly:scope.cgiReadonly,
				
		localMode:'update',
		scopeCascadingType:'strict',
		allowImplicidQueryCall:false,
		mergeFormAndUrl:false
	},
	speed:{
		sessionType:scope.sessionType,
		sessionTimeout:scope.sessionTimeout,
		applicationTimeout:scope.applicationTimeout,
		sessionManagement:scope.sessionManagement,
		clientManagement:scope.clientManagement,
		clientCookies:scope.clientCookies,
		domaincookies:scope.domaincookies,
		clientTimeout:scope.clientTimeout,
		clientStorage:scope.clientStorage,
		sessionStorage:scope.sessionStorage,
		cgiReadonly:scope.cgiReadonly,
				
		localMode:'always',
		scopeCascadingType:'strict',
		allowImplicidQueryCall:false,
		mergeFormAndUrl:true
	}
}>

<!--- Datasource --->
<cfadmin 
	action="getDatasourceSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="dbSetting">
<cfset setting.datasource={
	compatibility:{
		psq:true
	},
	strict:{
		psq:false
	},
	speed:{
		psq:false
	}
}>

<!--- customtag --->
<cfadmin 
	action="getCustomtagSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="customtag">
<cfset setting.customtag={
	compatibility:{
		deepSearch:true,
		localSearch:true,
		extensions="cfm,cfml",
		customTagPathCache:false

	},
	strict:{
		deepSearch:false,
		localSearch:false,
		extensions="cfc,cfm",
		customTagPathCache:false
	},
	speed:{
		deepSearch:false,
		localSearch:false,
		extensions="cfc,cfm",
		customTagPathCache:false
	}
}>


<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="setting"
	secValue="yes">

<!--- 
Defaults --->	
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">





<cftry>
	<cfswitch expression="#form.mainAction#">
	<!--- UPDATE --->
		
        <cfcase value="#stText.Buttons.Update#">
        	<!--- component --->
        	<cfadmin action="updateComponent"
                type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				attributeCollection="#setting.component[form.mode]#"
                remoteClients="#request.getRemoteClients()#"
				>
            <!--- charset --->
            <cfadmin action="updateCharset"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
                attributeCollection="#setting.charset[form.mode]#"
                remoteClients="#request.getRemoteClients()#">
            <!--- scope --->
			<cfadmin 
				action="updateScope"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				attributeCollection="#setting.scope[form.mode]#"
				remoteClients="#request.getRemoteClients()#">
            <!--- datasource --->
            <cfadmin 
				action="updatePSQ"
				type="#request.adminType#"
				password="#session["password"&request.adminType]#"
				attributeCollection="#setting.datasource[form.mode]#"
				remoteClients="#request.getRemoteClients()#">
            <!--- customtag --->
            <cfadmin 
                action="updateCustomTagSetting"
                type="#request.adminType#"
                password="#session["password"&request.adminType]#"
				attributeCollection="#setting.customtag[form.mode]#"
                remoteClients="#request.getRemoteClients()#">


			
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
<!--- 
Create Datasource --->
<cfoutput>
<cfset colorCompatibility="green">
<cfset colorSpeed="orange">
<cfset colorStrict="purple">
<cfset style="padding:2px 10px 2px 10px;">
<cffunction name="doStyle">
	<cfargument name="value">
	<cfargument name="group">
	<cfargument name="name">
    
    
    <cfset var compat=setting[group].compatibility[name]>
    <cfset var speed=setting[group].speed[name]>
    <cfset var strict=setting[group].strict[name]>
    <cfset var color="">
	<cfif value EQ compat>
    	<cfset color=colorCompatibility>
    <cfelseif value EQ speed>
    	<cfset color=colorSpeed>
    <cfelseif value EQ strict>
    	<cfset color=colorStrict>
    </cfif>
    
    
	<cfreturn 'border-color:#color#;#style#'>
</cffunction>



#stText.setting.general[request.adminType]#



<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">

<table class="tbl" width="700">
<!---- data member default access --->

        <tr>
        	<td style="border-color:#colorCompatibility#;" width="10"><cfinputClassic type="radio" class="radio" name="mode" value="compatibility"></td>
            <td style="border-color:#colorCompatibility#;#style#"><b>#stText.setting.general.compatibility#</b><br />
            <div class="comment">#stText.setting.general.compatibilityDesc#</div></td>
        </tr>
        <tr>
        	<td style="border-color:#colorStrict#;" width="10"><cfinputClassic type="radio" class="radio" name="mode" value="strict"></td>
            <td style="border-color:#colorStrict#;#style#"><b>#stText.setting.general.strict#</b><br />
            <div class="comment">#stText.setting.general.strictDesc#</div></td>
        </tr>
        <tr>
        	<td style="border-color:#colorSpeed#;" width="10"><cfinputClassic type="radio" class="radio" name="mode" value="speed"></td>
            <td style="border-color:#colorSpeed#;#style#"><b>#stText.setting.general.speed#</b>
            <br /><div class="comment">#stText.setting.general.speedDesc#</div></td>
        </tr>
        
        
<tr>
	<td colspan="2">
		
      <input class="button submit" type="submit" name="mainAction" value="#stText.Buttons.update#">
		<input class="button reset" type="reset" name="cancel" value="#stText.Buttons.Cancel#">
	</td>
</tr>
</table>






<!------------------------------
			COMPONENT 
------------------------------->
<h2>#stText.setting.general.component#</h2>
#stText.Components[request.adminType]#
<table class="tbl" width="700">
<!---- data member default access --->
<cfset access=component.componentDataMemberDefaultAccess>
<tr>
	<th scope="row">#stText.Components.DataMemberAccessType#</th>
	<td style="#doStyle(access,'component','componentDataMemberDefaultAccess')#">
    	<b>#stText.Components['DMAT'& access]#</b>
        <br /><div class="comment">#stText.Components.DataMemberAccessTypeDescription#</div>
	</td>
</tr>
<!--- Trigger Data Member --->
<tr>
	<th scope="row">#stText.Components.triggerDataMember#</th>
	<td  style="#doStyle(component.triggerDataMember,'component','triggerDataMember')#">
    	<b>#yesNoFormat(component.triggerDataMember)#</b>
        <br /><div class="comment">#stText.Components.triggerDataMemberDescription#</div>
	</td>
</tr>
<!--- Use Shadow --->
<tr>
	<th scope="row">#stText.Components.useShadow#</th>
	<td  style="#doStyle(component.useShadow,'component','useShadow')#">
		<b>#yesNoFormat(component.useShadow)#</b>
		<br /><div class="comment">#stText.Components.useShadowDescription#</div><br>
      	
	</td>
</tr>
</table>





<!------------------------------
			CHARSET 
------------------------------->
<h2>#stText.setting.general.charset#</h2>
#stText.charset[request.adminType]#

<table class="tbl" width="700">
<!--- Template --->
<tr>
	<th scope="row">#stText.charset.templateCharset#</th>
	<td style="#doStyle(charset.templateCharset,'charset','templateCharset')#">
    	<b>#charset.templateCharset#</b><br />
		<div class="comment">#stText.charset.templateCharsetDescription#</div><br />
	</td>
</tr>

<!--- Web --->
<tr>
	<th scope="row">#stText.charset.webCharset#</th>
	<td style="#doStyle(charset.webCharset,'charset','webCharset')#">
		<b>#charset.webCharset#</b><br />
		<div class="comment">#stText.charset.webCharsetDescription#</div><br />
	</td>
</tr>

<!--- Resource --->
<tr>
	<th scope="row">#stText.charset.resourceCharset#</th>
	<td style="#doStyle(charset.resourceCharset,'charset','resourceCharset')#">
		<b>#charset.resourceCharset#</b><br />
		<div class="comment">#stText.charset.resourceCharsetDescription#</div><br />
	</td>
</tr>
</table>




<!------------------------------
			SCOPE 
------------------------------->
<h2>#stText.setting.general.scope#</h2>
#stText.scopes[request.adminType]#

<table class="tbl" width="700">
<!--- scope cascading --->
<tr>
	<th scope="row">#stText.Scopes.Cascading#</th>
	<td style="#doStyle(scope.scopeCascadingType,'scope','scopeCascadingType')#">
		<b>#ucFirst(stText.Scopes[scope.scopeCascadingType])#</b>
        <br /><div class="comment">#stText.Scopes.CascadingDescription#</div>
	</td>
</tr>
<!--- cascade to result --->
<tr>
	<th scope="row">#stText.Scopes.CascadeToResultSet#</th>
	<td style="#doStyle(scope.allowImplicidQueryCall,'scope','allowImplicidQueryCall')#">
		<b>#yesNoFormat(scope.allowImplicidQueryCall)#</b>
		<br /><div class="comment">#stText.Scopes.CascadeToResultSetDescription#</div>
	</td>
</tr>
<!--- Merge URL and Form --->
<tr>
	<th scope="row">#stText.Scopes.mergeUrlForm#</th>
	<td style="#doStyle(scope.mergeFormAndUrl,'scope','mergeFormAndUrl')#">
		<b>#yesNoFormat(scope.mergeFormAndUrl)#</b>
		<br /><div class="comment">#stText.Scopes.mergeUrlFormDescription#</div>
	</td>
</tr>
<!--- Local Mode --->
<tr>
	<th scope="row">#stText.Scopes.LocalMode#</th>
	<td style="#doStyle(scope.localMode,'scope','localMode')#">
		<b>#scope.localMode#</b>
        <br /><div class="comment">#stText.Scopes.LocalModeDesc#</div>
	</td>
</tr>
</table>




<!------------------------------
			DATASOURCE 
------------------------------->
<h2>#stText.setting.general.datasource#</h2>
#stText.Settings.DatasourceSettings#
<table class="tbl" width="700">
<!--- PSQ --->
<tr>
	<th scope="row">#stText.Settings.PreserveSingleQuotes#</th>
	<td style="#doStyle(dbSetting.psq,'datasource','psq')#">
	<b>#yesNoFormat(dbSetting.psq)#</b>
	<br /><div class="comment">#stText.Settings.PreserveSingleQuotesDescription#</div></td>
	
</tr>

</table>




<!------------------------------
			CUSTOM TAGS 
------------------------------->
<h2>#stText.setting.general.customtag#</h2>
#stText.CustomTags.CustomtagSetting#
<table class="tbl" width="700">
<!--- Deep Search --->
<tr>
	<th scope="row">#stText.CustomTags.customTagDeepSearch#</th>
	<td style="#doStyle(customtag.deepsearch,'customtag','deepsearch')#">
    <b>#yesNoFormat(customtag.deepsearch)#</b><br />
	<div class="comment">#stText.CustomTags.customTagDeepSearchDesc#</div></td>
</tr>
<!--- Local Search --->
<tr>
	<th scope="row">#stText.CustomTags.customTagLocalSearch#</th>
	<td style="#doStyle(customtag.localsearch,'customtag','localsearch')#">
	<b>#yesNoFormat(customtag.localsearch)#</b><br />
	<div class="comment">#stText.CustomTags.customTagLocalSearchDesc#</div></td>
	
</tr>
<!--- Extension --->
<cfset value=ArrayToList(customtag.extensions)>
<tr>
	<th scope="row">#stText.CustomTags.extensions#</th>
	<td style="#doStyle(value,'customtag','extensions')#">
    	<b>#value#</b><br />
        <div class="comment">#stText.CustomTags.extensionsDesc#</div>
    
    
    </td>
</tr>
</table>
</cfformClassic>
</cfoutput>
<br><br>