/**
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 **/


component extends="org.lucee.cfml.test.LuceeTestCase"	{

	 function beforeAll(){
		request.WEBADMNIPASSWORD = "password";
		variables.admin=new org.lucee.cfml.Administrator("server",request.WEBADMNIPASSWORD);
		variables.adminweb=new org.lucee.cfml.Administrator("web", "password");
	}

	function run( testResults , testBox ) {
		describe( title="test case for Administrator", body=function() {
			describe( title="test-Regional functions", body=function() {
				it(title="testGetRegional()", body=function( currentSpec ) {
					var reginal=admin.getRegional();
					assertEquals(isStruct(reginal),true);
					assertEquals(listSort(structKeyList(reginal),'textnocase'),'locale,timeserver,timezone,usetimeserver');
				});

				it(title="testUpdateRegional()", body=function( currentSpec ) {
					var org=admin.getRegional();
					var timeserver='swisstime.ethz.ch';
					var timezone='gmt';
					var locale='german (swiss)'

					admin.updateRegional(timezone,locale,timeserver,true);

					var mod=admin.getRegional();
					var l=GetLocaleInfo(mod.locale,'en_US');
					assertEquals("de",l.language);
					assertEquals("CH",l.country);
					assertEquals(timeserver,mod.timeserver);
					assertEquals(timezone,mod.timezone);
					assertEquals(true,mod.usetimeserver);

					// without optional arguments
					admin.updateRegional(timezone,locale,timeserver);
					admin.updateRegional(timezone,locale);
					admin.updateRegional(timezone);
					admin.updateRegional();

					// reset
					admin.updateRegional(org.timezone,org.locale,org.timeserver,org.usetimeserver);
				});

				it(title="testResetRegional()", body=function( currentSpec ) {
					var org=admin.getRegional();
					admin.resetRegional();
					admin.updateRegional(org.timezone,org.locale,org.timeserver,org.usetimeserver);
				});
			});

			describe( title="test-Charset functions", body=function() {
				it(title="testGetCharset()", body=function( currentSpec ) {
					var charset=admin.getCharset();
					assertEquals(isStruct(charset),true);
					assertEquals(listSort(structKeyList(charset),'textnocase'),'jreCharset,resourceCharset,templateCharset,webCharset');
				});

				it(title="testUpdateCharset()", body=function( currentSpec ) {
					var org=admin.getCharset();
					var resourceCharset='utf-8';
					var templateCharset='utf-8';
					var webCharset='utf-8';

					admin.updateCharset(resourceCharset,templateCharset,webCharset);

					var mod=admin.getCharset();
					assertEquals(mod.resourceCharset,resourceCharset);
					assertEquals(mod.templateCharset,templateCharset);
					assertEquals(mod.webCharset,webCharset);
					// without optional arguments
					admin.updateCharset(resourceCharset,templateCharset);
					admin.updateCharset(resourceCharset);
					admin.updateCharset();

					// reset
					admin.updateCharset(org.resourceCharset,org.templateCharset,org.webCharset);
				});

				it(title="testResetCharset()", body=function( currentSpec ) {
					var org=admin.getCharset();
					admin.resetCharset();
					// reset
					admin.updateCharset(org.resourceCharset,org.templateCharset,org.webCharset);
				});
			});

			describe( title="test-timezone functions", body=function() {
				it(title="testGetAvailableTimeZones()", body=function( currentSpec ) {
					var timezones=admin.getAvailableTimeZones();
					assertEquals(isQuery(timezones),true);
					assertEquals(listSort(timezones.columnlist,'textnocase'),'DISPLAY,ID');
				});
					/**
					* @hint returns all available locales
					*/
				it(title="testGetAvailableLocales()", body=function( currentSpec ) {
					var locales=admin.getAvailableLocales();
					assertEquals(isStruct(locales),true);
				});
			});

			describe( title="test-Jars function", body=function() {
				
				it(title="testGetjar()", body=function( currentSpec ) {
					var Jars = admin.getJars();
					assertEquals(isQuery(Jars),true);
				});

				it(title="testRemoveJar()", body=function( currentSpec ) {
					// admin.removeJar();
				});
			});

			describe( title="test-Output Setting functions", body=function() {
				it(title="testGetOutputSetting()", body=function( currentSpec ) {
					var outputSetting = admin.getOutputSetting();
					assertEquals(isStruct(outputSetting),true);
					assertEquals(structKeyExists(outputSetting, "allowCompression") && isBoolean(outputSetting.allowCompression),true);
					assertEquals(structKeyExists(outputSetting, "bufferOutput") && isBoolean(outputSetting.bufferOutput),true);
					assertEquals(structKeyExists(outputSetting, "cfmlWriter") && isValid("string", outputSetting.cfmlWriter),true);
					assertEquals(structKeyExists(outputSetting, "contentLength") && isBoolean(outputSetting.contentLength),true);
					assertEquals(structKeyExists(outputSetting, "suppressContent") && isBoolean(outputSetting.suppressContent),true);
				});

				it(title="testUpdateOutputSetting()", body=function( currentSpec ) {
					admin.updateOutputSetting('white-space');
					var outputSetting = admin.getOutputSetting();
					assertEquals(isStruct(outputSetting),true);
					assertEquals(structKeyExists(outputSetting, "allowCompression") && isBoolean(outputSetting.allowCompression),true);
					assertEquals(structKeyExists(outputSetting, "bufferOutput") && isBoolean(outputSetting.bufferOutput),true);
					assertEquals(structKeyExists(outputSetting, "cfmlWriter") && (outputSetting.cfmlWriter EQ "white-space"), true);
					assertEquals(structKeyExists(outputSetting, "contentLength") && isBoolean(outputSetting.contentLength),true);
					assertEquals(structKeyExists(outputSetting, "suppressContent") && isBoolean(outputSetting.suppressContent),true);
				});

				it(title="testResetOutputSetting()", body=function( currentSpec ) {
					admin.resetOutputSetting();
					var outputSetting = admin.getOutputSetting();
					assertEquals(isStruct(outputSetting),true);
					assertEquals(structKeyExists(outputSetting, "allowCompression") && isBoolean(outputSetting.allowCompression),true);
					assertEquals(structKeyExists(outputSetting, "bufferOutput") && isBoolean(outputSetting.bufferOutput),true);
					assertEquals(structKeyExists(outputSetting, "cfmlWriter") && isValid("string", outputSetting.cfmlWriter),true);
					assertEquals(structKeyExists(outputSetting, "contentLength") && isBoolean(outputSetting.contentLength),true);
					assertEquals(structKeyExists(outputSetting, "suppressContent") && isBoolean(outputSetting.suppressContent),true);
				});
			});

			describe( title="test-dataSource setting functions", body=function() {
				it(title="testgetDatasourceSetting()", body=function( currentSpec ) {
					var getDatasource = admin.getDatasourceSetting();
					assertEquals((isStruct(getDatasource) && isBoolean(getDatasource.psq)) , true);
				});

				it(title="testUpdateDatasourceSetting()", body=function( currentSpec ) {
					admin.updateDatasourceSetting(true);
					var getDatasource = admin.getDatasourceSetting();
					assertEquals((isStruct(getDatasource) && getDatasource.psq EQ true) , true);
				});

				it(title="testResetDatasourceSetting()", body=function( currentSpec ) {
					admin.resetDatasourceSetting();
					var getDatasource = admin.getDatasourceSetting();
				});
			});

			describe( title="test-dataSource functions", body=function() {
				it(title="testgetDatasources()", body=function( currentSpec ) {
					var getDatasource = admin.getDatasources();
					assertEquals(IsQuery(getDatasource), true);
				});
				// Create DataSource as Name testDSN //
				it(title="testUpdateDataSource", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.name = "TestDSN";
					tmpStrt.type = "MSSQL";
					tmpStrt.newName = "TestDSN1";
					tmpStrt.host = "localhost";
					tmpStrt.database = "TestDB";
					tmpStrt.port = 1433;
					tmpStrt.timezone = "";
					tmpStrt.username = "sa";
					tmpStrt.password = "sqlPwd@12##";
					tmpStrt.connectionLimit = "10";
					tmpStrt.connectionTimeout = "0";
					tmpStrt.metaCacheTimeout = "60000";
					tmpStrt.blob = false;
					tmpStrt.clob = false;
					tmpStrt.validate = false;
					tmpStrt.storage = false;
					tmpStrt.allowed_select = false;
					tmpStrt.allowed_insert = false;
					tmpStrt.allowed_update = false;
					tmpStrt.allowed_delete = false;
					tmpStrt.allowed_alter = false;
					tmpStrt.allowed_drop = false;
					tmpStrt.allowed_revoke = false;
					tmpStrt.allowed_create = false;
					tmpStrt.allowed_grant = false;
					tmpStrt.verify = false;
					admin.updateDatasource(argumentCollection = #tmpStrt#);
					var getDatasource = admin.getDatasources();
					assertEquals(IsQuery(getDatasource), true);
					var ListOfDSNName = valueList(getDatasource.name);
					assertEquals((findnoCase('testDSN1', ListOfDSNName) GT 0), true);
				});

				it(title="testgetDatasource()", body=function( currentSpec ) {
					var getDatasource = admin.getDatasource('TestDSN1');
					assertEquals(isstruct(getDatasource) ,true);
					assertEquals(getDatasource.name EQ 'TestDSN1', true);
				});

				xit(title="testremoveDatasource()", body=function( currentSpec ) {
					// admin.removeDatasource('testDSN1');
					// var getDatasource = admin.getDatasources();
					// var ListOfDSNName = valueList(getDatasource.name);
					// assertEquals((findnoCase('testDSN1', ListOfDSNName) EQ 0), true);
				});

				it(title="checking getDatasourceDriverList()", body=function( currentSpec ) {
					var datasourceDriverList = admin.getDatasourceDriverList();
					assertEquals(isQuery(datasourceDriverList),true);
				});

				it(title="checking verifyDatasource()", body=function( currentSpec ) {
					tmpStrt.name = "TestDSN1";
					tmpStrt.dbusername = "sa";
					tmpStrt.dbpassword = "sqlPwd@12##";
					var verfiyDataSource = admin.verifyDatasource(argumentCollection = #tmpStrt#);
					assertEquals(isstruct(verfiyDataSource) ,true);
				});
		
			});

			describe( title="test-mail server functions", body=function() {
				it(title="checking getMailservers()", body=function( currentSpec ) {
					var mailservers = admin.getMailservers();
					assertEquals(isQuery(mailservers),true);
				});
				
				it(title="checking updateMailserver()", body=function( currentSpec ) {
					var mailservers = admin.getMailservers();
					var tmpStrt = {};
					tmpStrt.host = "smtp.gmail.com";
					tmpStrt.port = "587";
					tmpStrt.username = "test1";
					tmpStrt.password = "test";
					tmpStrt.tls = true;
					tmpStrt.ssl = false;
					tmpStrt.life = createTimeSpan(0,0,1,0);
					tmpStrt.idle = createTimeSpan(0,0,0,10);
					admin.updateMailserver(argumentCollection = #tmpStrt#);
					var mailservers = admin.getMailservers();
					assertEquals((isquery(mailservers) && FindNocase( 'test1',valueList(mailservers.username)) GT 0) ,true);
				});

				it(title="checking removeMailserver()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.host = "smtp.gmail.com";
					tmpStrt.username = "test1";
					admin.removeMailServer(argumentCollection = #tmpStrt#);
					var mailservers = admin.getMailservers();
					assertEquals((isquery(mailservers) && FindNocase( 'test1',valueList(mailservers.username)) EQ 0) ,true);
				});
			});

			describe( title="test-mail setting functions", body=function() {
				it(title="checking getMailSettings()", body=function( currentSpec ) {
					var getMailSettings = admin.getMailSetting();
					assertEquals(isStruct(getMailSettings),true);
					assertEquals(listSort(structKeyList(getMailSettings),'textnocase'), 'defaultencoding,maxThreads,spoolEnable,spoolInterval,timeout');
				});

				it(title="checking updateMailSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.spoolEnable = false;
					tmpStrt.spoolInterval = 05;
					tmpStrt.timeout = 20;
					tmpStrt.defaultEncoding = "utf-8";
					admin.updateMailSetting(argumentCollection = #tmpStrt#);
					var getMailSettings = admin.getMailSetting();
					assertEquals((isStruct(getMailSettings) && getMailSettings.spoolEnable EQ false),true);
				});

				it(title="checking resetMailSetting", body=function( currentSpec ) {
					admin.resetMailSetting();
					var getMailSettings = admin.getMailSetting();
					assertEquals((isStruct(getMailSettings) && getMailSettings.spoolEnable EQ false),true);
				});
			});

			describe( title="test-mapping settings function", body=function() {
				it(title="checking getMappings()", body=function( currentSpec ) {
					var getMappings = admin.getMappings();
					assertEquals(isquery(getMappings) ,true);
				});

				it(title="checking updateMappings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.virtual = "/lucee-server07";
					tmpStrt.physical = "C:\portables\lucee-express-5.1.1.30-SNAPSHOT-Dummy\lucee-server\context\context";
					tmpStrt.archive = "{lucee-config}/context/lucee-admin.lar";
					tmpStrt.primary = "Resources";
					tmpStrt.inspect = "Once";
					tmpStrt.toplevel = true;
					admin.updateMapping(argumentCollection = #tmpStrt#);
					var getMappings = admin.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/lucee-server07", ListOfvirtual) GT 0, true);
				});

				it(title="checking remove mapping()", body=function( currentSpec ) {
					var virtual = "/lucee-server07";
					admin.removeMapping(#virtual#);
					var getMappings = admin.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/lucee-server07", ListOfvirtual) EQ 0, true);
				});
			});

			describe( title="test-Compile mapping settings function", body=function() {
				it(title="checking compileMapping()", body=function( currentSpec ) {
					try{
						var hasError = false;
						admin.compileMapping('/lucee-server');
					} catch ( any e) {
						var hasError = true;
					}
					assertEquals(hasError EQ false, true);
				});

				it(title="checking createArchiveFromMapping ()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.virtual = "/lucee-server";
					tmpStrt.addCFMLFile = false;
					tmpStrt.addNonCFMLFile = false;
					tmpStrt.doDownload = false;
					admin.createArchiveFromMapping(argumentCollection = #tmpStrt#);
					var getMappings = admin.getMappings();
					var result = QueryExecute(
						sql="SELECT Archive
						 FROM getMappings where Virtual = '/lucee-server' and Archive != ''",
						options=
						{dbtype="query"}
					);
					assertEquals(result.recordcount EQ 1,true);
				});
			});

			describe( title="test-Extension functions", body=function() {
				it(title="checking getExtensions()", body=function( currentSpec ) {
					var getExtensions = admin.getExtensions();
					assertEquals(isquery(getExtensions) ,true);
				});
				
				it(title="checking updateExtensions()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.provider = "http://extension.lucee.org";
					tmpStrt.id = '2BCD080F-4E1E-48F5-BEFE794232A21AF6';
					tmpStrt.version = '1.3.1';
					admin.updateExtension(argumentCollection = #tmpStrt#);
				});
				
				it(title="checking getRHServerExtensions()", body=function( currentSpec ) {
					var getRHServerExtensions = adminweb.getRHServerExtensions();
					assertEquals(isquery(getRHServerExtensions) ,true);
				});


				it(title="checking getLocalExtensions()", body=function( currentSpec ) {
					var localExtensions = admin.getLocalExtensions();
					assertEquals(isquery(localExtensions) ,true);
				});

				it(title="checking getLocalExtension()", body=function( currentSpec ) {
					var localExtensions = admin.getLocalExtensions();
					var localExtension = admin.getLocalExtension(localExtensions.id);
					assertEquals(isstruct(localExtension) ,true);
					assertEquals(listSort(structKeyList(localExtension),'textnocase'),'applications,archives,bundles,categories,components,config,contexts,description,eventGateways,flds,functions,id,image,name,plugins,releaseType,startBundles,tags,tlds,trial,version,webcontexts');
				});

				xit(title="checking RemoveExtensions()", body=function( currentSpec ) {
					admin.removeExtension('2BCD080F-4E1E-48F5-BEFE794232A21AF6');
				});
			});

			describe( title="test-extension providers functions", body=function() {
				it(title="checking getExtensionProviders()", body=function( currentSpec ) {
					var getExtensionsProvider = admin.getExtensionProviders();
					assertEquals((isquery(getExtensionsProvider) && getExtensionsProvider.url EQ 'http://extension.lucee.org') ,true);
				});
				it(title="checking updateExtensionProvider()", body=function( currentSpec ) {
					admin.updateExtensionProvider('http://www.myhost.com');
					var getExtensionsProvider = admin.getExtensionProviders();
					assertEquals((isquery(getExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getExtensionsProvider.url)) GT 0) ,true);
				});

				it(title="checking removeExtensionProvider()", body=function( currentSpec ) {
					admin.removeExtensionProvider('http://www.myhost.com');
					var getExtensionsProvider = admin.getExtensionProviders();
					assertEquals((isquery(getExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getExtensionsProvider.url)) EQ 0) ,true);
				});
			});

			describe( title="test ORM function()", body=function() {
				it(title="checking getORMEngine()", body=function( currentSpec ) {
					var ORMEngine = admin.getORMEngine();
					assertEquals(isstruct(ORMEngine) ,true);
					assertEquals(listSort(structKeyList(ORMEngine),'textnocase'),'bundleName,bundleVersion,class');
				});

				it(title="checking getORMSetting()", body=function( currentSpec ) {
					var ORMsetting = admin.getORMSetting();
					assertEquals(isstruct(ORMsetting) ,true);
					assertEquals(listSort(structKeyList(ORMsetting),'textnocase'),'autogenmap,cacheconfig,cacheProvider,catalog,cfcLocation,dbCreate,dialect,eventHandler,eventHandling,flushAtRequestEnd,isDefaultCfclocation,logSql,namingstrategy,ormConfig,savemapping,schema,secondarycacheenabled,sqlscript,useDBForMapping');
				});
			});
			

			describe( title="test tldx and Fldx function", body=function() {
				it(title="checking getTlds()", body=function( currentSpec ) {
					var tlds = admin.getTlds();
					assertEquals(isquery(tlds) ,true);
					assertEquals((tlds.type EQ 'cfml'), true);
				});

				it(title="checking getFlds()", body=function( currentSpec ) {
					var flds = admin.getFlds();
					assertEquals(isquery(flds) ,true);
					assertEquals((flds.displayname EQ 'Lucee Core Function Library'), true);
				});
			});

			describe( title="test info functions", body=function() {
				it(title="checking getinfo()", body=function( currentSpec ) {
					var info = admin.getinfo();
					assertEquals(isstruct(info) ,true);
					assertEquals(listSort(structKeyList(info),'textnocase'),'config,configServerDir,configWebDir,javaAgentSupported,servlets');
				});
			});

			describe( title="test Monitor functions", body=function() {
				it(title="checking isMonitorEnabled()", body=function( currentSpec ) {
					var isEnable = admin.isMonitorEnabled();
					assertEquals(isBoolean(isEnable) ,true);
				});

				it(title="checking getMonitors()", body=function( currentSpec ) {
					var monitors = admin.getMonitors();
					assertEquals(isquery(monitors) ,true);
				});
			});

			describe( title="test application functions", body=function() {
				it(title="checking getApplicationSetting()", body=function( currentSpec ) {
					var appSetting = admin.getApplicationSetting();
					assertEquals(isstruct(appSetting) ,true);
					assertEquals(listSort(structKeyList(appSetting),'textnocase'),'AllowURLRequestTimeout,requestTimeout,requestTimeout_day,requestTimeout_hour,requestTimeout_minute,requestTimeout_second,scriptProtect');
					
				});

				it(title="checking getApplicationListener()", body=function( currentSpec ) {
					var appListner = admin.getApplicationListener();
					assertEquals(isstruct(appListner) ,true);
					assertEquals(listSort(structKeyList(appListner),'textnocase'),'mode,type');
					
				});
			});

			describe( title="test getproxy functions", body=function() {
				it(title="checking getproxy()", body=function( currentSpec ) {
					var proxy = admin.getproxy();
					assertEquals(isstruct(proxy) ,true);
					assertEquals(listSort(structKeyList(proxy),'textnocase'),'password,port,server,username');
				});

			});

			describe( title="test Component functions", body=function() {
				it(title="checking getComponent()", body=function( currentSpec ) {
					var getComp = admin.getComponent();
					assertEquals(isstruct(getComp) ,true);
					assertEquals(listSort(structKeyList(getComp),'textnocase'),'baseComponentTemplateCFML,baseComponentTemplateLucee,componentDataMemberDefaultAccess,ComponentDefaultImport,componentDumpTemplate,componentLocalSearch,componentPathCache,deepSearch,strBaseComponentTemplateCFML,strBaseComponentTemplateLucee,strComponentDumpTemplate,triggerDataMember,useShadow');
				});
			});

			describe( title="test scope functions", body=function() {
				it(title="checking getscope()", body=function( currentSpec ) {
					var getScope = admin.getScope();
					assertEquals(isstruct(getScope) ,true);
					assertEquals(listSort(structKeyList(getScope),'textnocase'), 'allowImplicidQueryCall,applicationTimeout,applicationTimeout_day,applicationTimeout_hour,applicationTimeout_minute,applicationTimeout_second,cgiReadonly,clientCookies,clientManagement,clientStorage,clientTimeout,clientTimeout_day,clientTimeout_hour,clientTimeout_minute,clientTimeout_second,domainCookies,localmode,mergeFormAndUrl,scopeCascadingType,sessionManagement,sessionStorage,sessionTimeout,sessionTimeout_day,sessionTimeout_hour,sessionTimeout_minute,sessionTimeout_second,sessiontype');
				});
			});

			describe( title="test QueueSetting functions", body=function() {
				it(title="checking getQueueSetting()", body=function( currentSpec ) {
					var getQueueSettings = admin.getQueueSetting();
					assertEquals(isstruct(getQueueSettings) ,true);
					assertEquals(listSort(structKeyList(getQueueSettings),'textnocase'), 'enable,max,timeout');
				});

				it(title="checking updateQueueSetting()", body=function( currentSpec ) {
					admin.updateQueueSetting(enable=true,max="100",timeout='3600');
					var updateQueueSetting = admin.getQueueSetting();
					assertEquals(isStruct(updateQueueSetting),true);
					assertEquals(structKeyExists(updateQueueSetting, "enable") && isBoolean(updateQueueSetting.enable),true);
					assertEquals(structKeyExists(updateQueueSetting, "max") && (updateQueueSetting.max EQ 100),true);
					assertEquals(structKeyExists(updateQueueSetting, "timeout") && (updateQueueSetting.timeout EQ 3600), true);
				});
			});

			describe( title="test CustomTag functions", body=function() {
				it(title="checking getCustomTagSetting()", body=function( currentSpec ) {
					var customTagSetting = admin.getCustomTagSetting();
					assertEquals(isstruct(customTagSetting) ,true);
					assertEquals(listSort(structKeyList(customTagSetting),'textnocase'), 'customTagDeepSearch,customTagLocalSearch,customTagPathCache,deepSearch,extensions,localSearch');
				});
			});

			describe( title="test surveillance functions", body=function() {
				it(title="checking surveillance()", body=function( currentSpec ) {
					var surveillance = admin.surveillance();
					assertEquals(isstruct(surveillance) ,true);
				});
			});

			describe( title="test spoolerTasks functions", body=function() {
				it(title="checking getSpoolerTasks()", body=function( currentSpec ) {
					var spoolertask = admin.getSpoolerTasks();
					assertEquals(isQuery(spoolertask) ,true);
				});
			});

			describe( title="test performance functions", body=function() {
				it(title="checking getPerformanceSettings()", body=function( currentSpec ) {
					var performanceSettings = admin.getPerformanceSettings();
					assertEquals(isstruct(performanceSettings) ,true);
					assertEquals(listSort(structKeyList(performanceSettings),'textnocase'), 'inspectTemplate,typeChecking');			
				});
			});

			describe( title="test log functions", body=function() {
				it(title="checking logsetting()", body=function( currentSpec ) {
					var logsettings = admin.getLogSettings();
					assertEquals(isquery(logsettings) ,true);
				});

				it(title="checking getExecutionLog()", body=function( currentSpec ) {
					var execLog = admin.getExecutionLog();
					assertEquals(isstruct(execLog) ,true);
					assertEquals(listSort(structKeyList(execLog),'textnocase'), 'arguments,class,enabled');
				});
			});

			describe( title="test CompilerSettings functions", body=function() {
				it(title="checking getCompilerSettings()", body=function( currentSpec ) {
					var compileSettings = admin.getCompilerSettings();
					assertEquals(isstruct(compileSettings) ,true);
					assertEquals(listSort(structKeyList(compileSettings),'textnocase'), 'DotNotationUpperCase,externalizeStringGTE,handleUnquotedAttrValueAsString,nullSupport,suppressWSBeforeArg,templateCharset');
				});
			});
			
			describe( title="test gateway functions", body=function() {
				it(title="checking getGatewayentries()", body=function( currentSpec ) {
					var gatewayEntries = adminweb.getGatewayentries();
					assertEquals(isquery(gatewayEntries) ,true);
				});

				it(title="checking getGatewayentry()", body=function( currentSpec ) {
					var gatewayEntry = adminweb.getGatewayentry('testgateway');
					assertEquals(isStruct(gatewayEntry) ,true);
					assertEquals(listSort(structKeyList(gatewayEntry),'textnocase'), 'bundleName,bundleVersion,cfcPath,class,custom,id,listenerCfcPath,readOnly,startupMode,state');
				});
			});

			describe( title="test thread functions", body=function() {
				it(title="checking thread()", body=function( currentSpec ) {
					var runningThrd = admin.getRunningThreads();
					assertEquals(isquery(runningThrd) ,true);
				});
			});

			describe( title="test bundles functions", body=function() {
				it(title="checking getBundles()", body=function( currentSpec ) {
					var bundles = admin.getBundles();
					assertEquals(isquery(bundles) ,true);
				});
				
				it(title="checking getBundle()", body=function( currentSpec ) {
					var bundles = admin.getBundles();
					var bundle = admin.getBundle( bundles.symbolicName );
					assertEquals(isStruct(bundle) ,true);
					assertEquals(listSort(structKeyList(bundle),'textnocase'), 'description,fragment,headers,id,path,state,symbolicName,title,usedBy,version');
				});
			});

			describe( title="test debugging functions", body=function() {
				it(title="checking getDebugSetting()", body=function( currentSpec ) {
					var deguggingListSetting = admin.getDebugSetting();
					assertEquals(isstruct(deguggingListSetting) ,true);
					assertEquals(listSort(structKeyList(deguggingListSetting),'textnocase'), 'maxLogs');
				});
			});

			
			describe( title="test Certificate functions", body=function() {
			
				it(title="checking getSSLCertificate()", body=function( currentSpec ) {
					var hostName = 'localHost';
					var getSSLCertificate = admin.getSSLCertificate(hostName);
					assertEquals(isquery(getSSLCertificate), true);
				});
			});

			describe( title="test plugin functions", body=function() {
				it(title="checking getPluginDirectory()", body=function( currentSpec ) {
					var pluginDirectory = admin.getPluginDirectory();
					assertEquals(isstruct(pluginDirectory), false);
				});
				
				it(title="checking getPlugins()", body=function( currentSpec ) {
					var plugin = admin.getPlugins();
					assertEquals(isquery(plugin) ,true);
				});
			});

			describe( title="test taskSetting functions", body=function() {
				it(title="checking getTaskSetting()", body=function( currentSpec ) {
					var taskSetting = admin.getTaskSetting();
					assertEquals(isstruct(taskSetting) ,true);
					assertEquals(listSort(structKeyList(taskSetting),'textnocase'), 'maxThreads');
				});
			});

			describe( title="test getMapping functions", body=function() {
				it(title="checking getMapping()", body=function( currentSpec ) {
					var virtual = "/lucee-server";
					var mapping = admin.getMapping(virtual);
					assertEquals(isstruct(mapping) ,true);
					var strctKeylist = structKeyList(mapping);
					assertEquals(FindNocase('toplevel',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getCustomTagMappings functions", body=function() {
				it(title="checking getCustomTagMappings()", body=function( currentSpec ) {
					var customTagMappings = admin.getCustomTagMappings();
					assertEquals(isquery(customTagMappings) ,true);
					var strctKeylist = structKeyList(customTagMappings);
					assertEquals(FindNocase('readonly',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getCfxTags functions", body=function() {
				it(title="checking getCfxTags()", body=function( currentSpec ) {
					var cfxTags = admin.getCfxTags();
					assertEquals(isquery(cfxTags) ,true);
					var strctKeylist = structKeyList(cfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getCPPCfxTags functions", body=function() {
				it(title="checking getCPPCfxTags()", body=function( currentSpec ) {
					var CPPCfxTags = admin.getCPPCfxTags();
					assertEquals(isquery(CPPCfxTags) ,true);
					var strctKeylist = structKeyList(CPPCfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getJavaCfxTags functions", body=function() {
				it(title="checking getJavaCfxTags()", body=function( currentSpec ) {
					var javaCfxTags = admin.getJavaCfxTags();
					assertEquals(isquery(javaCfxTags) ,true);
					var strctKeylist = structKeyList(javaCfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getDebugEntry functions", body=function() {
				it(title="checking getDebugEntry()", body=function( currentSpec ) {
					var debugEntry = admin.getDebugEntry();
					assertEquals(isquery(debugEntry) ,true);
					var strctKeylist = structKeyList(debugEntry);
					assertEquals(FindNocase('ipRange',strctKeylist) GT 0, true);
				});
			});

			describe( title="test getError functions", body=function() {
				it(title="checking getError()", body=function( currentSpec ) {
					var error = admin.getError();
					assertEquals(isStruct(error) ,true);
					var strctKeylist = structKeyList(error);
					assertEquals(FindNocase('doStatusCode',strctKeylist) GT 0, true);
				});
			});

			describe( title="test remoteclient functions", body=function() {
				it(title="checking getremoteclients", body=function( currentSpec ) {
					var remoteClients = admin.getRemoteClients();
					assertEquals(isquery(remoteClients) ,true);
				});

				it(title="checking getremoteclient", body=function( currentSpec ) {
					var remoteClients = admin.getRemoteClients();
					var remoteClient = admin.getRemoteClient(remoteClients.url);
					assertEquals(isstruct(remoteClient) ,true);
					assertEquals(listSort(structKeyList(remoteClient),'textnocase'),'adminPassword,label,proxyPassword,proxyPort,proxyServer,proxyUsername,securityKey,ServerPassword,ServerUsername,type,url,usage');
				});

				it(title="checking verifyremoteclient()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.label = "test";
					tmpStrt.url = "http://www.xmlme.com/WSShakespeare.asmx/?WSDL";
					tmpStrt.adminPassword = "qwerty";
					tmpStrt.securityKey = "qwerty";
					var verifyClient = admin.verifyremoteclient(argumentCollection = #tmpStrt#);
					assertEquals(isstruct(verifyClient) ,true);
				});

				it(title="checking getRemoteClientUsage()", body=function( currentSpec ) {
					var getremoteclient = admin.getRemoteClientUsage();
					assertEquals(isQuery(getremoteclient) ,true);
				});

				it(title="checking hasRemoteClientUsage()", body=function( currentSpec ) {
					var hasRemoteClientUsage = admin.hasRemoteClientUsage();
					assertEquals(isBoolean(hasRemoteClientUsage) ,true);
				});

				it(title="checking getRemoteClientTasks()", body=function( currentSpec ) {
					var remoteClientTask = admin.getRemoteClientTasks();
					assertEquals(isQuery(remoteClientTask) ,true);
				});
			});

			describe( title="test cache functions", body=function() {
				it(title="checking getCacheConnections()", body=function( currentSpec ) {
					var getCacheConnections = admin.getCacheConnections();
					assertEquals(isquery(getCacheConnections) ,true);
				});

				it(title="checking getCacheConnection()", body=function( currentSpec ) {
					var getCacheConnection = admin.getCacheConnection('testCache');
					assertEquals(isstruct(getCacheConnection) ,true);
					assertEquals(listSort(structKeyList(getCacheConnection),'textnocase'),'bundleName,bundleVersion,class,custom,default,name,readOnly,storage');
				});

				it(title="checking getCacheDefaultConnection()", body=function( currentSpec ) {
					var defaultCacheConnection = admin.getCacheDefaultConnection('query');
					assertEquals(isstruct(defaultCacheConnection) ,true);
					assertEquals(listSort(structKeyList(defaultCacheConnection),'textnocase'),'bundleName,bundleVersion,class,custom,default,name,readonly');
				});

				it(title="checking updateCacheConnection()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.class="lucee.runtime.cache.ram.RamCache";
					tmpStrt.name="testCache";
					tmpStrt.custom={"timeToIdleSeconds":"86400","timeToLiveSeconds":"3600"};
					tmpStrt.bundleName="";
					tmpStrt.bundleVersion="";
					tmpStrt.default="function";
					tmpStrt.storage=false;
					admin.updateCacheConnection(argumentCollection=tmpStrt);
					var getCacheConnection = admin.getCacheConnection('testCache');
					assertEquals(isstruct(getCacheConnection) ,true);
					assertEquals(getCacheConnection.default EQ 'function' ,true);
				});
			});

			describe( title="test verifyMailServer functions", body=function() {
				it(title="checking verifyMailServer()", body=function( currentSpec ) {
					var verifyMailServer = admin.verifyMailServer( hostname="smtp.gmail.com", port="587", mailusername="test@gmail.com", mailpassword="test" );
					assertEquals(verifyMailServer.label,"ok");
				});
			});

			describe( title="test verifyExtensionProvider functions", body=function() {
				it(title="checking verifyExtensionProvider()", body=function( currentSpec ) {
					var verifyExtensionProvider = admin.verifyExtensionProvider(url="http://extension.lucee.org");
					assertEquals(verifyExtensionProvider.label,"ok");
				});
			});

			describe( title="test verifyJavaCFX functions", body=function() {
				it(title="checking verifyJavaCFX()", body=function( currentSpec ) {
					var verifyJavaCFX = admin.verifyJavaCFX(name="helloworld", class="lucee.cfx.example.HelloWorld");
					assertEquals(verifyJavaCFX.label,"ok");
				});
			});

			describe( title="test verifyCFX functions", body=function() {
				it(title="checking verifyCFX()", body=function( currentSpec ) {
					var verifyCFX = admin.verifyCFX(name="helloworld");
					assertEquals(verifyCFX.label,"ok");
				});
			});

			describe( title="test resetId functions", body=function() {
				it(title="checking resetId()", body=function( currentSpec ) {
					var resetId = admin.resetId();
					assertEquals(resetId.label,"ok");
				});
			});
		});
	}
}