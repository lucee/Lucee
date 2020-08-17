<cfsilent>
<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfscript>
	
	function toTSStruct(seconds){
		var data={};
		var day=60*60*24;
		var tmp=seconds/day;
		data.days=int(tmp);
		tmp=(tmp-data.days)*24;
		data.hours=int(tmp);
		tmp=(tmp-data.hours)*60;
		data.minutes=int(tmp);
		data.seconds=int((tmp-data.minutes)*60);
		return data;
	}
</cfscript>
<cfadmin 
	action="getLocales"
	locale="#stText.locale#"
	returnVariable="locales">
	
<cfadmin 
	action="getTimeZones"
	locale="#stText.locale#"
	returnVariable="timezones">
	
<cfadmin 
	action="getRegional"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="regional">

<cfadmin 
	action="getScope"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="scope">
	
<cfadmin 
	action="getInfo"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="info">

<cfadmin 
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	action="getPerformanceSettings"
	returnVariable="PerformanceSettings">
	
<cfadmin 
	action="getApplicationSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="appSettings">
<cfset requestTimeout=
		appSettings.requestTimeout_second +
		(appSettings.requestTimeout_minute*60) +
		(appSettings.requestTimeout_hour*3600) +
		(appSettings.requestTimeout_day*3600*24)>


<cfadmin 
	action="getCharset"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="charset">		

<cfadmin 
	action="getOutputSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="outputSetting">


<cfadmin 
	action="getMappings"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mappings">


<cfadmin 
	action="getMailservers"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="mailservers">
<!--- regex --->
<cfadmin 
	action="getRegex"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="regex">

<!--- cache --->
<cfadmin 
	action="getCacheConnections"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="cacheConnections">
<cfset defaults={}>
    <cfloop index="type" list="object,template,query,resource,function,include">
		<cfloop query="cacheConnections">
		<cfif cacheConnections.default EQ type>
			<cfset defaults[type]=cacheConnections.name>
		</cfif>
		</cfloop>
	</cfloop>
<cfscript>
hasObj=!isNull(defaults.object) && len(defaults.object);
hasTem=!isNull(defaults.template) && len(defaults.template);
hasQry=!isNull(defaults.query) && len(defaults.query);
hasRes=!isNull(defaults.resource) && len(defaults.resource);
hasFun=!isNull(defaults.function) && len(defaults.function);
hasInc=!isNull(defaults.include) && len(defaults.include);
hasCache=hasObj || hasTem || hasQry || hasRes || hasFun || hasInc;
</cfscript>

<!--- datasource --->
<cfadmin 
	action="getDatasources"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="datasources">

</cfsilent>
<cfoutput>

	<cfsavecontent variable="codeSample">
component {

	this.name = "#info.label ?: '&lt;application-name&gt;' #"; // name of the application context

// regional
	// default locale used for formating dates, numbers ...
	this.locale = "#regional.locale#"; 
	// default timezone used
	this.timezone = "#regional.timezone#"; 

// scope handling
	// lifespan of an untouched application scope
	this.applicationTimeout = createTimeSpan( #scope.applicationTimeout_day#, #scope.applicationTimeout_hour#, #scope.applicationTimeout_minute#, #scope.applicationTimeout_second# ); 
	
	// session handling enabled or not
	this.sessionManagement = #scope.sessionManagement#; 
	// cfml or jee based sessions
	this.sessionType = "#scope.sessionType#"; 
	// untouched session lifespan
	this.sessionTimeout = createTimeSpan( #scope.sessionTimeout_day#, #scope.sessionTimeout_hour#, #scope.sessionTimeout_minute#, #scope.sessionTimeout_second# ); 
	this.sessionStorage = "#scope.sessionStorage#";
	
	// client scope enabled or not
	this.clientManagement = #scope.clientManagement#; 
	this.clientTimeout = createTimeSpan( #scope.clientTimeout_day#, #scope.clientTimeout_hour#, #scope.clientTimeout_minute#, #scope.clientTimeout_second# );
	this.clientStorage = "#scope.clientStorage#";
						
	// using domain cookies or not
	this.setDomainCookies = #scope.domainCookies#; 
	this.setClientCookies = #scope.clientCookies#;

	// prefer the local scope at un-scoped write
	this.localMode = "#scope.LocalMode#"; 
	
	// buffer the output of a tag/function body to output in case of an exception
	this.bufferOutput = #outputSetting.bufferOutput#; 
	this.compression = #outputSetting.AllowCompression#;
	this.suppressRemoteComponentContent = #outputSetting.suppressContent#;
	
	// If set to false Lucee ignores type definitions with function arguments and return values
	this.typeChecking = #PerformanceSettings.typeChecking#;
	
	
// request
	// max lifespan of a running request
	this.requestTimeout=createTimeSpan(#appSettings.requestTimeout_day#,#appSettings.requestTimeout_hour#,#appSettings.requestTimeout_minute#,#appSettings.requestTimeout_second#); 

// charset
	this.charset.web="#charset.webCharset#";
	this.charset.resource="#charset.resourceCharset#";
	
	this.scopeCascading = "#scope.scopeCascadingType#";
	this.searchResults = #trueFalseFormat(scope.allowImplicidQueryCall)#;
// regex
	this.regex.type = "#regex.type#";
//////////////////////////////////////////////
//               MAIL SERVERS               //
//////////////////////////////////////////////
	this.mailservers =[ 
<cfloop query="#mailservers#"><cfset life=toTSStruct(mailservers.life)><cfset idle=toTSStruct(mailservers.idle)>
		<cfif mailservers.currentrow GT 1>,</cfif>{
		  host: '#mailservers.hostname#'
		, port: #mailservers.port#
		, username: '#replace(mailservers.username,"'","''","all")#'
		, password: '#mailservers.passwordEncrypted?:''#'
		, ssl: #mailservers.ssl?:false#
		, tls: #mailservers.tls?:false#<cfif 
		!isNull(mailservers.life)>
		, lifeTimespan: createTimeSpan(#life.days#,#life.hours#,#life.minutes#,#life.seconds#)</cfif><cfif 
		!isNull(mailservers.idle)>
		, idleTimespan: createTimeSpan(#idle.days#,#idle.hours#,#idle.minutes#,#idle.seconds#)</cfif>
		}
</cfloop>
	];
//////////////////////////////////////////////
//               DATASOURCES                //
//////////////////////////////////////////////
	<cfloop query="#datasources#"><cfscript>
optional=[];
// not supported yet optional.append('default:false // default: false');
if(datasources.blob) optional.append('blob:#datasources.blob# // default: false');
if(datasources.clob) optional.append('clob:#datasources.clob# // default: false');
if(isNumeric(datasources.connectionLimit))optional.append('connectionLimit:#datasources.connectionLimit# // default:-1');
if(datasources.connectionTimeout NEQ 1)optional.append('connectionTimeout:#datasources.connectionTimeout# // default: 1; unit: seconds');
if(datasources.metaCacheTimeout NEQ 60000)optional.append(',metaCacheTimeout:#datasources.metaCacheTimeout# // default: 60000; unit: milliseconds');
if(len(datasources.timezone))optional.append("timezone:'#replace(datasources.timezone,"'","''","all")#'");
if(datasources.storage) optional.append('storage:#datasources.storage# // default: false');
if(datasources.readOnly) optional.append('readOnly:#datasources.readOnly# // default: false');
</cfscript>
	this.datasources<cfif isValid('variableName',datasources.name) and !find('.',datasources.name)>["#datasources.name#"]<cfelse>['#datasources.name#']</cfif> = {
	  class: '#datasources.classname#'
	, connectionString: '#replace(datasources.dsnTranslated,"'","''","all")#'<cfif len(datasources.password)>
	, username: '#replace(datasources.username,"'","''","all")#'
	, password: "#datasources.passwordEncrypted#"</cfif><cfif optional.len()>
	
	// optional settings
	<cfloop array="#optional#" index="i" item="value">, #value#<cfif i LT optional.len()>
	</cfif></cfloop></cfif>
	};
	</cfloop>
//////////////////////////////////////////////
//                 CACHES                   //
//////////////////////////////////////////////
	<cfloop query="#cacheConnections#">this.cache.connections["#cacheConnections.name#"] = {
		  class: '#cacheConnections.class#'#isNull(cacheConnections.bundleName) || isEmpty(cacheConnections.bundleName)?"":"
		, bundleName: '"&cacheConnections.bundleName&"'"##isNull(cacheConnections.bundleVersion) || isEmpty(cacheConnections.bundleVersion)?"":"
		, bundleVersion: '"&cacheConnections.bundleVersion&"'"##!cacheConnections.readOnly?"":"
		, readOnly: "&cacheConnections.readonly#
		, storage: #cacheConnections.storage#
		, custom: #isStruct(cacheConnections.custom)?serialize(cacheConnections.custom):'{}'#
		, default: '#cacheConnections.default#'
	};
	</cfloop><cfif hasCache>
	// cache defaults
<cfif hasObj>	this.cache.object = "#!hasObj?"&lt;cache-name>":defaults.object#";
</cfif><cfif hasTem>	this.cache.template = "#!hasTem?"&lt;cache-name>":defaults.template#";
</cfif><cfif hasQry>	this.cache.query = "#!hasQry?"&lt;cache-name>":defaults.query#";
</cfif><cfif hasRes>	this.cache.resource = "#!hasRes?"&lt;cache-name>":defaults.resource#";
</cfif><cfif hasFun>	this.cache.function = "#!hasFun?"&lt;cache-name>":defaults.function#";
</cfif><cfif hasInc>	this.cache.include = "#!hasInc?"&lt;cache-name>":defaults.include#";</cfif>
</cfif>	

//////////////////////////////////////////////
//               MAPPINGS                   //
//////////////////////////////////////////////
<cfloop query="mappings"><cfif mappings.hidden || mappings.virtual=="/lucee" || mappings.virtual=="/lucee-server"><cfcontinue></cfif><cfset del=""><cfset count=0>
this.mappings["#mappings.virtual#"]=<cfif len(mappings.strPhysical) && !len(mappings.strArchive)>"#mappings.strPhysical#"<cfelse>{<cfif len(mappings.strPhysical)><cfset count++>
		physical:"#mappings.strPhysical#"<cfset del=","></cfif><cfif len(mappings.strArchive)><cfset count++>
		#del#archive:"#mappings.strArchive#"<cfset del=","></cfif><cfif count==2 && !mappings.PhysicalFirst>
		#del#primary:"<cfif mappings.PhysicalFirst>physical<cfelse>archive</cfif>"<cfset del=","></cfif>}</cfif>;
</cfloop>
}
</cfsavecontent>


<cfif form.subAction EQ stText.Buttons.export>
	<cfheader name="Content-Disposition" value="inline; filename=Application.cfc">
	<cfcontent reset="true" variable="#toBinary(codeSample,"utf-8")#" type="application/cfml">
	<cfabort>
</cfif>



<h1>#stText.settings.exportAppCFC#</h1>	
<div class="pageintro">#stText.settings.exportAppCFCDesc#</div>
<cfformClassic onerror="customError" action="#request.self#?action=#url.action#" method="post">
<input type="submit" class="button submit" onclick="disableBlockUI=true;" name="subAction" value="#stText.Buttons.export#">
</cfformClassic>


<cfset renderCodingTip( codeSample,false, true )>

</cfoutput>
