/**
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
component extends="org.lucee.cfml.test.LuceeTestCase"{

	setting requesttimeout=1800;

	function beforeAll(){

		variables.admin=new org.lucee.cfml.Administrator("server",request.ServerAdminPassword);
		variables.adminWeb=new org.lucee.cfml.Administrator("web", request.WebAdminPassword);
	}

	function run( testResults , testBox ) {
		describe( title="test case for Administrator", body=function() {

		// Regional
			describe( title="test-Regional functions", body=function() {
				beforeEach(function( currentSpec ){
					getRegional = adminweb.getRegional();
					assertEquals(isStruct(getRegional) ,true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateRegional(argumentCollection = getRegional);
				});

				it(title="testGetRegional()", body=function( currentSpec ) {
					var reginal=adminWeb.getRegional();
					assertEquals(isStruct(reginal),true);
					assertEquals(listSort(structKeyList(reginal),'textnocase'),'locale,timeserver,timezone,usetimeserver');
				});

				it(title="testUpdateRegional()", body=function( currentSpec ) {
					var timeserver='swisstime.ethz.ch';
					var timezone='gmt';
					var locale='german (swiss)'

					adminWeb.updateRegional(timezone,locale,timeserver,true);

					var mod=adminWeb.getRegional();
					var l=GetLocaleInfo(mod.locale,'en_US');
					assertEquals("de",l.language);
					assertEquals("CH",l.country);
					assertEquals(timeserver,mod.timeserver);
					assertEquals(timezone,mod.timezone);
					assertEquals(true,mod.usetimeserver);

					// without optional arguments
					adminWeb.updateRegional(timezone,locale,timeserver);
					adminWeb.updateRegional(timezone,locale);
					adminWeb.updateRegional(timezone);
					adminWeb.updateRegional();
				});

				it(title="testResetRegional()", body=function( currentSpec ) {
					var adminReginals=admin.getRegional();
					adminweb.resetRegional();
					var adminWebReginals=adminweb.getRegional();

					assertEquals(adminWebReginals.locale EQ adminReginals.locale,true);
					assertEquals(adminWebReginals.timeserver EQ adminReginals.timeserver,true);
					assertEquals(adminWebReginals.timezone EQ adminReginals.timezone,true);
					assertEquals(adminWebReginals.usetimeserver EQ adminReginals.usetimeserver,true);
				});
			});


		// Charset
			describe( title="test-Charset functions", body=function() {
				beforeEach(function( currentSpec ){
					getCharset = adminWeb.getCharset();
					assertEquals(isStruct(getCharset) ,true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateCharset(argumentCollection = getCharset);
				});

				it(title="testGetCharset()", body=function( currentSpec ) {
					var charset=adminWeb.getCharset();
					assertEquals(isStruct(charset),true);
					assertEquals(listSort(structKeyList(charset),'textnocase'),'jreCharset,resourceCharset,templateCharset,webCharset');
				});

				it(title="testUpdateCharset()", body=function( currentSpec ) {
					var resourceCharset='utf-8';
					var templateCharset='utf-8';
					var webCharset='utf-8';

					adminWeb.updateCharset(resourceCharset,templateCharset,webCharset);

					var mod=adminWeb.getCharset();
					assertEquals(mod.resourceCharset,resourceCharset);
					assertEquals(mod.templateCharset,templateCharset);
					assertEquals(mod.webCharset,webCharset);
					// without optional arguments
					adminWeb.updateCharset(resourceCharset,templateCharset);
					adminWeb.updateCharset(resourceCharset);
					adminWeb.updateCharset();
				});

				it(title="testResetCharset()", body=function( currentSpec ) {
					var adminCharset=admin.getCharset();
					adminWeb.resetCharset();
					var adminWebCharset=adminweb.getCharset();

					assertEquals(adminWebCharset.jreCharset EQ adminCharset.jreCharset,true);
					assertEquals(adminWebCharset.resourceCharset EQ adminCharset.resourceCharset,true);
					assertEquals(adminWebCharset.templateCharset EQ adminCharset.templateCharset,true);
					assertEquals(adminWebCharset.webCharset EQ adminCharset.webCharset,true);
				});
			});

		// OutputSetting
			describe( title="test-Output Setting functions", body=function() {
				beforeEach(function( currentSpec ){
					getOutputSetting = adminWeb.getOutputSetting();
					assertEquals(isStruct(getOutputSetting) ,true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateOutputSetting(argumentCollection = getOutputSetting);
				});

				it(title="testGetOutputSetting()", body=function( currentSpec ) {
					var outputSetting = adminWeb.getOutputSetting();
					assertEquals(isStruct(outputSetting),true);
					assertEquals(structKeyExists(outputSetting, "allowCompression") && isBoolean(outputSetting.allowCompression),true);
					assertEquals(structKeyExists(outputSetting, "bufferOutput") && isBoolean(outputSetting.bufferOutput),true);
					assertEquals(structKeyExists(outputSetting, "cfmlWriter") && isValid("string", outputSetting.cfmlWriter),true);
					assertEquals(structKeyExists(outputSetting, "contentLength") && isBoolean(outputSetting.contentLength),true);
					assertEquals(structKeyExists(outputSetting, "suppressContent") && isBoolean(outputSetting.suppressContent),true);
				});

				it(title="testUpdateOutputSetting()", body=function( currentSpec ) {
					adminWeb.updateOutputSetting('white-space');
					var outputSetting = adminWeb.getOutputSetting();
					assertEquals(isStruct(outputSetting),true);
					assertEquals(structKeyExists(outputSetting, "allowCompression") && isBoolean(outputSetting.allowCompression),true);
					assertEquals(structKeyExists(outputSetting, "bufferOutput") && isBoolean(outputSetting.bufferOutput),true);
					assertEquals(structKeyExists(outputSetting, "cfmlWriter") && (outputSetting.cfmlWriter EQ "white-space"), true);
					assertEquals(structKeyExists(outputSetting, "contentLength") && isBoolean(outputSetting.contentLength),true);
					assertEquals(structKeyExists(outputSetting, "suppressContent") && isBoolean(outputSetting.suppressContent),true);
				});

				it(title="testResetOutputSetting()", body=function( currentSpec ) {
					var adminOutputSetting = admin.getOutputSetting();
					adminWeb.resetOutputSetting();
					var adminWebOutputSetting = adminWeb.getOutputSetting();

					assertEquals(adminWebOutputSetting.allowCompression EQ adminOutputSetting.allowCompression,true);
					assertEquals(adminWebOutputSetting.bufferOutput EQ adminOutputSetting.bufferOutput,true);
					assertEquals(adminWebOutputSetting.cfmlWriter EQ adminOutputSetting.cfmlWriter,true);
					assertEquals(adminWebOutputSetting.contentLength EQ adminOutputSetting.contentLength,true);
					assertEquals(adminWebOutputSetting.suppressContent EQ adminOutputSetting.suppressContent,true);
				});
			});

		// available Timezone/Locale
			describe( title="test-timezone functions", body=function() {
				it(title="testGetAvailableTimeZones()", body=function( currentSpec ) {
					var timezones=adminWeb.getAvailableTimeZones();
					assertEquals(isQuery(timezones),true);
					assertEquals(listSort(timezones.columnlist,'textnocase'),'DISPLAY,ID');
				});

				it(title="testGetAvailableLocales()", body=function( currentSpec ) {
					var locales=adminWeb.getAvailableLocales();
					assertEquals(isStruct(locales),true);
				});
			});

		// Datasource Setting
			describe( title="test-dataSource setting functions", body=function() {
				beforeEach(function( currentSpec ){
					getDatasourceSetting = adminWeb.getDatasourceSetting();
					assertEquals((isStruct(getDatasourceSetting) && isBoolean(getDatasourceSetting.psq)) , true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateDatasourceSetting(argumentCollection = getDatasourceSetting);
				});

				it(title="testgetDatasourceSetting()", body=function( currentSpec ) {
					var datasourceSetting = adminWeb.getDatasourceSetting();
					assertEquals((isStruct(datasourceSetting) && isBoolean(datasourceSetting.psq)) , true);
				});

				it(title="testUpdateDatasourceSetting()", body=function( currentSpec ) {
					adminWeb.updateDatasourceSetting(true);
					var datasourceSetting = adminWeb.getDatasourceSetting();
					assertEquals((isStruct(datasourceSetting) && datasourceSetting.psq EQ true) , true);
				});

				it(title="testResetDatasourceSetting()", body=function( currentSpec ) {
					var adminDatasourceSetting = admin.getDatasourceSetting();
					adminWeb.resetDatasourceSetting();
					var adminWebDatasourceSetting = adminWeb.getDatasourceSetting();
					assertEquals(adminWebDatasourceSetting.psq EQ adminDatasourceSetting.psq,true);
				});
			});

		// Datasource
			describe( title="test-dataSource functions", body=function() {
				it(title="testgetDatasources()", body=function( currentSpec ) {
					var datasource = adminWeb.getDatasources();
					assertEquals(IsQuery(datasource), true);
				});

				it(title="testUpdateDataSource", body=function( currentSpec ) {
					var mySQL = getCredencials();
					if(structCount(mySQL)) {
						var tmpStrt = {};
						tmpStrt.name = "TestDSN";
						tmpStrt.type = "MYSQL";
						tmpStrt.newName = "TestDSN1";
						tmpStrt.host = mySQL.server;
						tmpStrt.database = mySQL.database;
						tmpStrt.port = mySQL.port;
						tmpStrt.timezone = "";
						tmpStrt.username = mySQL.username;
						tmpStrt.password = mySQL.password;
						tmpStrt.connectionLimit = "10";
						tmpStrt.connectionTimeout = "0";
						tmpStrt.metaCacheTimeout = "60000";
						tmpStrt.blob = false;
						tmpStrt.clob = false;
						tmpStrt.validate = false;
						tmpStrt.storage = false; // TODO remove all allow functions from Admin.cfc amd this testcase
						tmpStrt.allowedSelect = false;
						tmpStrt.allowedInsert = false;
						tmpStrt.allowedUpdate = false;
						tmpStrt.allowedDelete = false;
						tmpStrt.allowedAlter = false;
						tmpStrt.allowedDrop = false;
						tmpStrt.allowedRevoke = false;
						tmpStrt.allowedCreate = false;
						tmpStrt.allowedGrant = false;
						tmpStrt.verify = false;
						adminWeb.updateDatasource(argumentCollection = tmpStrt);
						var datasource = adminWeb.getDatasources();
						assertEquals(IsQuery(datasource), true);
						var ListOfDSNName = valueList(datasource.name);
						assertEquals((findnoCase('testDSN1', ListOfDSNName) GT 0), true);
					}

				});

				it(title="testgetDatasource()", body=function( currentSpec ) {
					if(structCount(getCredencials())) {
						var datasource = adminWeb.getDatasource('TestDSN1');
						assertEquals(isstruct(datasource) ,true);
						assertEquals(datasource.name EQ 'TestDSN1', true);
					}
				});

				it(title="checking verifyDatasource()", body=function( currentSpec ) {
					if(structCount(getCredencials())) {
						var datasource = adminWeb.getDatasource('TestDSN1');
						assertEquals(isstruct(datasource), true);
						tmpStrt.name = datasource.name;
						tmpStrt.dbusername = datasource.username;
						tmpStrt.dbpassword = datasource.password;
						adminWeb.verifyDatasource(argumentCollection = #tmpStrt#);
					}
				});

				it(title="testremoveDatasource()", body=function( currentSpec ) {
					if(structCount(getCredencials())) {
						adminWeb.removeDatasource('testDSN1');
						var datasource = adminWeb.getDatasources();
						var ListOfDSNName = valueList(datasource.name);
						assertEquals((findnoCase('testDSN1', ListOfDSNName) EQ 0), true);
					}
				});

				it(title="checking getDatasourceDriverList()", body=function( currentSpec ) {
					var datasourceDriverList = adminWeb.getDatasourceDriverList();
					assertEquals(isQuery(datasourceDriverList),true);
				});
			});


			// Mail
			describe( title="test-mail server functions", body=function() {
				it(title="checking getMailservers()", body=function( currentSpec ) {
					var mailservers = adminWeb.getMailservers();
					assertEquals(isQuery(mailservers),true);
				});

				it(title="checking updateMailserver()", body=function( currentSpec ) {
					var mailservers = adminWeb.getMailservers();
					var tmpStrt = {};
					tmpStrt.host = "Smtp.gmail.com";
					tmpStrt.port = "587";
					tmpStrt.username = "test1";
					tmpStrt.password = "test";
					tmpStrt.tls = true;
					tmpStrt.ssl = false;
					tmpStrt.life = createTimeSpan(0,0,1,0);
					tmpStrt.idle = createTimeSpan(0,0,0,10);
					adminWeb.updateMailserver(argumentCollection = #tmpStrt#);
					var mailservers = adminWeb.getMailservers();
					assertEquals((isquery(mailservers) && FindNocase( 'test1',valueList(mailservers.username)) GT 0) ,true);
				});

				it(title="checking verifyMailServer()", body=function( currentSpec ) {
					adminWeb.verifyMailServer( hostname="smtp.gmail.com", port="587", mailusername="test@gmail.com", mailpassword="test" );
				});

				it(title="checking removeMailserver()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.host = "Smtp.gmail.com";
					tmpStrt.username = "test1";
					adminWeb.removeMailServer(argumentCollection = #tmpStrt#);
					var mailservers = adminWeb.getMailservers();
					assertEquals((isquery(mailservers) && FindNocase( 'test1',valueList(mailservers.username)) EQ 0) ,true);
				});
			});

			// Mail Setting
			describe( title="test-mail setting functions", body=function() {
				beforeEach(function( currentSpec ){
					getMailSetting = adminWeb.getMailSetting();
					assertEquals(isStruct(getMailSetting), true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateMailSetting(argumentCollection = getMailSetting);
				});

				it(title="checking getMailSettings()", body=function( currentSpec ) {
					var mailSettings = adminWeb.getMailSetting();
					assertEquals(isStruct(mailSettings),true);
					assertEquals(listSort(structKeyList(mailSettings),'textnocase'), 'defaultencoding,maxThreads,spoolEnable,spoolInterval,timeout');
				});

				it(title="checking updateMailSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.spoolEnable = false;
					tmpStrt.timeout = 20;
					tmpStrt.defaultEncoding = "utf-8";
					adminWeb.updateMailSetting(argumentCollection = #tmpStrt#);
					var mailSettings = adminWeb.getMailSetting();
					assertEquals((isStruct(mailSettings) && mailSettings.spoolEnable EQ false),true);
				});

				it(title="checking resetMailSetting", body=function( currentSpec ) {
					var adminMailSettings = admin.getMailSetting();
					adminWeb.resetMailSetting();
					var adminWebMailSettings = adminWeb.getMailSetting();

					assertEquals(adminWebMailSettings.defaultencoding EQ adminMailSettings.defaultencoding,true);
					assertEquals(adminWebMailSettings.maxThreads EQ adminMailSettings.maxThreads,true);
					assertEquals(adminWebMailSettings.spoolEnable EQ adminMailSettings.spoolEnable,true);
					assertEquals(adminWebMailSettings.spoolInterval EQ adminMailSettings.spoolInterval,true);
					assertEquals(adminWebMailSettings.timeout EQ adminMailSettings.timeout,true);
				});
			});

			// Mapping
			/*describe( title="test-mapping settings function", body=function() {
				it(title="checking getMappings()", body=function( currentSpec ) {
					var getMappings = adminWeb.getMappings();
					assertEquals(isquery(getMappings) ,true);
				});

				it(title="checking getMapping()", body=function( currentSpec ) {
					var virtual = "/lucee-server";
					var mapping = adminWeb.getMapping(virtual);
					assertEquals(isstruct(mapping) ,true);
					var strctKeylist = structKeyList(mapping);
					assertEquals(FindNocase('toplevel',strctKeylist) GT 0, true);
				});

				it(title="checking updateMapping()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\TestArchive";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";

					var tmpStrt = {};
					tmpStrt.virtual = "/TestArchive";
					tmpStrt.physical = path;
					tmpStrt.archive = "";
					tmpStrt.primary = "Resources";
					tmpStrt.inspect = "Once";
					tmpStrt.toplevel = true;
					adminWeb.updateMapping(argumentCollection = #tmpStrt#);
					var getMappings = adminWeb.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/TestArchive", ListOfvirtual) GT 0, true);
				});

				it(title="checking createArchiveFromMapping()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\TestArchive";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";
					var trg=curr&"/Administrator/TestArchive.lar";
					var tmpStrt = {};
					tmpStrt.virtual = "/TestArchive";
					tmpStrt.addCFMLFile = false;
					tmpStrt.addNonCFMLFile = false;
					tmpStrt.target = trg;
					adminWeb.createArchiveFromMapping(argumentCollection = tmpStrt);
					var getMappings = adminWeb.getMappings();
					var result = QueryExecute(
						sql="SELECT Archive
						 FROM getMappings where Virtual = '/TestArchive'",
						options=
						{dbtype="query"}
					);
					assertEquals(1,result.recordcount);


					assertTrue(fileExists(trg));
					if(fileExists(trg))fileDelete(trg);
				});

				it(title="checking compileMapping()", body=function( currentSpec ) {
					adminWeb.compileMapping('/TestArchive', true);
				});

				it(title="checking compileCTMapping()", body=function( currentSpec ) {
					adminWeb.compileCTMapping('/TestArchive');
				});

				it(title="checking compileComponentMapping()", body=function( currentSpec ) {
					adminWeb.compileComponentMapping('/TestArchive');
				});

				it(title="checking getCustomTagMappings()", body=function( currentSpec ) {
					var customTagMappings = adminWeb.getCustomTagMappings();
					assertEquals(isquery(customTagMappings) ,true);
					var strctKeylist = structKeyList(customTagMappings);
					assertEquals(FindNocase('readonly',strctKeylist) GT 0, true);
				});

				it(title="checking remove mapping()", body=function( currentSpec ) {
					var virtual = "/TestArchive";
					adminWeb.removeMapping(virtual);
					var getMappings = adminWeb.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/TestArchive", ListOfvirtual) EQ 0, true);
				});
			});*/

			// Extension
			describe( title="test-Extension functions", body=function() {
				it(title="checking getExtensions()", body=function( currentSpec ) {
					var getExtensions = adminWeb.getExtensions();
					assertEquals(isquery(getExtensions) ,true);
				});

				it(title="checking getExtensionInfo()", body=function( currentSpec ) {
					var extensionsInfo = adminWeb.getExtensionInfo();
					assertEquals(isStruct(extensionsInfo) ,true);
					assertEquals(listSort(structKeyList(extensionsInfo),'textnocase'),'directory,enabled');
				});

				/*it(title="checking updateExtensionInfo()", body=function( currentSpec ) {
					adminWeb.updateExtensionInfo(enabled=true);
					var extensionsInfo = adminWeb.getExtensionInfo();
					assertEquals(isStruct(extensionsInfo) ,true);
					assertEquals(extensionsInfo.enabled EQ true ,true);
				});*/

				it(title="checking updateExtension()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.provider = "https://extension.lucee.org";
					tmpStrt.id = '2BCD080F-4E1E-48F5-BEFE794232A21AF6';
					tmpStrt.version = '1.3.1';
					adminWeb.updateExtension(argumentCollection = #tmpStrt#);
				});

				it(title="checking RemoveExtension()", body=function( currentSpec ) {
					adminWeb.removeExtension('2BCD080F-4E1E-48F5-BEFE794232A21AF6');
				});

				it(title="checking getServerExtensions()", body=function( currentSpec ) {
					var getRHServerExtensions = adminWeb.getServerExtensions();
					assertEquals(isquery(getRHServerExtensions) ,true);
					assertEquals(listSort(structKeyList(getRHServerExtensions),'textnocase'),'applications,archives,bundles,categories,components,config,contexts,description,eventGateways,flds,functions,id,image,name,plugins,releaseType,startBundles,tags,tlds,trial,version,webcontexts');
				});

				it(title="checking getLocalExtensions()", body=function( currentSpec ) {
					var localExtensions = adminWeb.getLocalExtensions();
					assertEquals(isquery(localExtensions) ,true);
				});

				it(title="checking getLocalExtension()", body=function( currentSpec ) {
					var localExtensions = adminWeb.getLocalExtensions();
					var localExtension = adminWeb.getLocalExtension(localExtensions.id);
					assertEquals(isstruct(localExtension) ,true);
					assertEquals(listSort(structKeyList(localExtension),'textnocase'),'applications,archives,bundles,categories,components,config,contexts,description,eventGateways,flds,functions,id,image,name,plugins,releaseType,startBundles,tags,tlds,trial,version,webcontexts');
				});
			});

			// Extension Provider
			describe( title="test-extension providers functions", body=function() {
				it(title="checking getExtensionProviders()", body=function( currentSpec ) {
					var getExtensionsProvider = adminWeb.getExtensionProviders();
					assertEquals(isquery(getExtensionsProvider) ,true);
					assertEquals(listSort(structKeyList(getExtensionsProvider),'textnocase'),'readonly,url');
				});

				it(title="checking updateExtensionProvider()", body=function( currentSpec ) {
					adminWeb.updateExtensionProvider('http://www.myhost.com');
					var getExtensionsProvider = adminWeb.getExtensionProviders();
					assertEquals((isquery(getExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getExtensionsProvider.url)) GT 0) ,true);
				});

				it(title="checking removeExtensionProvider()", body=function( currentSpec ) {
					adminWeb.removeExtensionProvider('http://www.myhost.com');
					var getExtensionsProvider = adminWeb.getExtensionProviders();
					assertEquals((isquery(getExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getExtensionsProvider.url)) EQ 0) ,true);
				});

				it(title="checking verifyExtensionProvider()", body=function( currentSpec ) {
					var getExtensionsProvider = adminWeb.getExtensionProviders();
					adminWeb.verifyExtensionProvider(url=getExtensionsProvider.url);
				});
			});

			// ORM
			/*describe( title="test ORM function()", body=function() {
				beforeEach(function( currentSpec ){
					ORMSettingList = "checking getORMSetting(),checking updateORMSetting(),checking resetORMSetting()";
					ORMEngineList = "checking getORMEngine(),checking updateORMEngine(),checking removeORMEngine()";
					if(listFindNoCase(ORMSettingList, currentSpec)){
						getORMSetting = adminWeb.getORMSetting();
						assertEquals(isStruct(getORMSetting), true);
					}
					if(listFindNoCase(ORMEngineList, currentSpec)){
						getORMEngine = adminWeb.getORMEngine();
						assertEquals(isStruct(getORMEngine), true);
					}
				});

				afterEach(function( currentSpec ){
					if(listFindNoCase(ORMSettingList,currentSpec)){
						adminWeb.updateORMSetting(argumentCollection = getORMSetting, cfcLocation = getORMSetting.cfcLocation[1]);
					}
					if(listFindNoCase(ORMEngineList,currentSpec)){
						adminWeb.updateORMEngine(argumentCollection = getORMEngine);
					}
				});

				it(title="checking getORMSetting()", body=function( currentSpec ) {
					var ORMsetting = adminWeb.getORMSetting();
					assertEquals(isstruct(ORMsetting) ,true);
					assertEquals(listSort(structKeyList(ORMsetting),'textnocase'),'autogenmap,cacheconfig,cacheProvider,catalog,cfcLocation,dbCreate,dialect,eventHandler,eventHandling,flushAtRequestEnd,isDefaultCfclocation,logSql,namingstrategy,ormConfig,savemapping,schema,secondarycacheenabled,sqlscript,useDBForMapping');
				});

				it(title="checking updateORMSetting()", body=function( currentSpec ) {
					adminWeb.updateORMSetting(schema="testSchema",autoGenMap=true);
					var ORMsetting = adminWeb.getORMSetting();
					assertEquals(isstruct(ORMsetting) ,true);
					assertEquals(ORMsetting.schema EQ 'testSchema' ,true);
				});

				it(title="checking resetORMSetting()", body=function( currentSpec ) {
					var adminORMsetting = admin.getORMSetting();
					adminWeb.resetORMSetting();
					var adminWebORMsetting = adminWeb.getORMSetting();
					var columnlists = ["autogenmap","catalog","eventHandling","flushAtRequestEnd","isDefaultCfclocation","logSql","savemapping","schema","useDBForMapping"];
					for(var columnlist in columnlists){
						assertEquals(adminWebORMsetting["#columnlist#"] EQ adminORMsetting["#columnlist#"] ,true);
					}
				});

				it(title="checking getORMEngine()", body=function( currentSpec ) {
					var ORMEngine = adminWeb.getORMEngine();
					assertEquals(isstruct(ORMEngine) ,true);
					assertEquals(listSort(structKeyList(ORMEngine),'textnocase'),'bundleName,bundleVersion,class');
				});

				it(title="checking updateORMEngine()", body=function( currentSpec ) {
					var tmpstruct = {};
					tmpstruct.class = "lucee.runtime.orm.ORMEngine";
					tmpstruct.bundleName = "";
					tmpstruct.bundleVersion = "";
					adminWeb.updateORMEngine(argumentCollection=tmpstruct);
					var updatedORMEngine = adminWeb.getORMEngine();
					assertEquals(isstruct(updatedORMEngine) ,true);
					assertEquals(updatedORMEngine.class EQ 'lucee.runtime.orm.ORMEngine' ,true);
				});

				it(title="checking removeORMEngine()", body=function( currentSpec ) {
					adminWeb.removeORMEngine();
				});
			});*/

			// Component
			/*describe( title="test Component functions", body=function() {
				it(title="checking getComponent()", body=function( currentSpec ) {
					var getComp = adminWeb.getComponent();
					assertEquals(isstruct(getComp) ,true);
					assertEquals(listSort(structKeyList(getComp),'textnocase'),'baseComponentTemplateCFML,baseComponentTemplateLucee,componentDataMemberDefaultAccess,ComponentDefaultImport,componentDumpTemplate,componentLocalSearch,componentPathCache,deepSearch,strBaseComponentTemplateCFML,strBaseComponentTemplateLucee,strComponentDumpTemplate,triggerDataMember,useShadow');
				});

				it(title="checking updateComponent()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.triggerDataMember = true;
					adminWeb.updateComponent(argumentCollection=tmpStrt);
					var getComp = adminWeb.getComponent();
					assertEquals(isstruct(getComp) ,true);
					assertEquals(getComp.triggerDataMember EQ 'true' ,true);
				});

				it(title="checking getComponentMappings()", body=function( currentSpec ) {
					var getCompMap = adminWeb.getComponentMappings();
					assertEquals(isQuery(getCompMap) ,true);
					assertEquals(listSort(structKeyList(getCompMap),'textnocase'),'archive,hidden,inspect,physical,physicalFirst,readonly,strarchive,strphysical,virtual');
				});

				it(title="checking updateComponentMapping()", body=function( currentSpec ) {

					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";
					var tmpStrt = {};
					tmpStrt.virtual = "/TestCompArchive";
					tmpStrt.physical = path;
					tmpStrt.archive = "";
					tmpStrt.primary = "";
					tmpStrt.inspect = "once";
					adminWeb.updateComponentMapping(argumentCollection=tmpStrt);
					var getCompMap = adminWeb.getComponentMappings();
					assertEquals(isQuery(getCompMap) ,true);
					assertEquals(findNoCase("/TestCompArchive",valueList(getCompMap.virtual)) NEQ 0,true);
				});

				it(title="checking createComponentArchive()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\TestArchive";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";
					var trg = curr&"/Administrator/TestCompArchive.lar";

					var tmpStrt = {};
					tmpStrt.virtual = "/TestCompArchive";
					tmpStrt.file = trg;
					tmpStrt.addCFMLFile = true;
					tmpStrt.addNonCFMLFile = true;

					adminWeb.createComponentArchive(argumentCollection=tmpStrt);


					assertTrue(fileExists(trg));
					if(fileExists(trg))fileDelete(trg);
				});

				it(title="checking removeComponentMapping()", body=function( currentSpec ) {
					adminWeb.removeComponentMapping("/TestCompArchive");
					var getCompMap = adminWeb.getComponentMappings();
					assertEquals(isQuery(getCompMap) ,true);
					assertEquals(findNoCase("/TestCompArchive",valueList(getCompMap.virtual)) EQ 0,true);
				});
			});*/

			// Cache
			describe( title="test cache functions", body=function() {
				it(title="checking getCacheConnections()", body=function( currentSpec ) {
					var getCacheConnections = adminWeb.getCacheConnections();
					assertEquals(isquery(getCacheConnections) ,true);
				});

				it(title="checking updateCacheConnection()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.class="lucee.runtime.cache.ram.RamCache";
					tmpStrt.name="testCache";
					tmpStrt.custom={"timeToIdleSeconds":"86400","timeToLiveSeconds":"3600"};
					tmpStrt.bundleName="";
					tmpStrt.bundleVersion="";
					tmpStrt.default="query";
					adminWeb.updateCacheConnection(argumentCollection=tmpStrt);
					var getCacheConnection = adminWeb.getCacheConnection('testCache');
					assertEquals(isstruct(getCacheConnection) ,true);
					assertEquals(getCacheConnection.default EQ 'query' ,true);
				});

				it(title="checking verifyCacheConnection()", body=function( currentSpec ) {
					var getCacheConnections = adminWeb.getCacheConnections();
					assertEquals(isquery(getCacheConnections) ,true);
					var verifyCache = adminWeb.verifyCacheConnection(getCacheConnections.name);
				});

				it(title="checking getCacheConnection()", body=function( currentSpec ) {
					var getCacheConnection = adminWeb.getCacheConnection('testCache');
					assertEquals(isstruct(getCacheConnection) ,true);
					assertEquals(listSort(structKeyList(getCacheConnection),'textnocase'),'bundleName,bundleVersion,class,custom,default,name,readOnly,storage');
				});

				it(title="checking removeCacheConnection()", body=function( currentSpec ){
					var removeCacheConnection = adminWeb.removeCacheConnection('testCache');
					var getCacheConnections = adminWeb.getCacheConnections();
					assertEquals( listFindNoCase( valueList(getCacheConnections.name), 'testCache' ), false );
				});
			});

			// Compiler
			describe( title="test CompilerSettings functions", body=function() {
				beforeEach(function( currentSpec ){
					if( currentSpec == 'checking getCompilerSettings()' ){
						getCompilerSettings = adminWeb.getCompilerSettings();
						assertEquals(isStruct(getCompilerSettings), true);
					}
				});

				afterEach(function( currentSpec ){
					adminWeb.updateCompilerSettings(argumentCollection = getCompilerSettings);
				});

				it(title="checking getCompilerSettings()", body=function( currentSpec ) {
					var compileSettings = adminWeb.getCompilerSettings();
					assertEquals(isStruct(compileSettings) ,true);
					assertEquals(listSort(structKeyList(compileSettings),'textnocase'), 'DotNotationUpperCase,externalizeStringGTE,handleUnquotedAttrValueAsString,nullSupport,suppressWSBeforeArg,templateCharset');
				});

				it(title="checking updateCompilerSettings()", body=function( currentSpec ) {
					var compileSettings = adminWeb.getCompilerSettings();
					assertEquals(isStruct(compileSettings) ,true);
					compileSettings.handleUnquotedAttrValueAsString=false;
					adminWeb.updateCompilerSettings(argumentCollection=compileSettings);

					var updatedCompileSettings = adminWeb.getCompilerSettings();
					assertEquals(isStruct(updatedCompileSettings) ,true);
					assertEquals(updatedCompileSettings.handleUnquotedAttrValueAsString, false);
				});

				it(title="checking resetCompilerSettings()", body=function( currentSpec ) {
					var adminUpdatedCompileSettings = admin.getCompilerSettings();
					adminWeb.resetCompilerSettings();

					var adminWebupdatedCompileSettings = adminWeb.getCompilerSettings();

					assertEquals(adminWebupdatedCompileSettings.DotNotationUpperCase EQ adminUpdatedCompileSettings.DotNotationUpperCase, true);
					assertEquals(adminWebupdatedCompileSettings.externalizeStringGTE EQ adminUpdatedCompileSettings.externalizeStringGTE, true);
					assertEquals(adminWebupdatedCompileSettings.handleUnquotedAttrValueAsString EQ adminUpdatedCompileSettings.handleUnquotedAttrValueAsString, true);
					assertEquals(adminWebupdatedCompileSettings.nullSupport EQ adminUpdatedCompileSettings.nullSupport, true);
					assertEquals(adminWebupdatedCompileSettings.suppressWSBeforeArg EQ adminUpdatedCompileSettings.suppressWSBeforeArg, true);
					assertEquals(adminWebupdatedCompileSettings.templateCharset.toString() EQ adminUpdatedCompileSettings.templateCharset.toString(), true);
				});
			});

			// Performance
			describe( title="test performance functions", body=function() {
				beforeEach(function( currentSpec ){
					getPerformanceSettings = adminWeb.getPerformanceSettings();
					assertEquals(isStruct(getPerformanceSettings), true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updatePerformanceSettings(argumentCollection = getPerformanceSettings);
				});

				it(title="checking getPerformanceSettings()", body=function( currentSpec ) {
					var performanceSettings = adminWeb.getPerformanceSettings();
					assertEquals(isstruct(performanceSettings) ,true);
					assertEquals(listSort(structKeyList(performanceSettings),'textnocase'), 'inspectTemplate,typeChecking');
				});

				it(title="checking updatePerformanceSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					var performanceSettings = adminWeb.getPerformanceSettings();
					assertEquals(isstruct(performanceSettings) ,true);
					tmpStrt.inspectTemplate = performanceSettings.inspectTemplate;
					tmpStrt.typeChecking = true;

					adminWeb.updatePerformanceSettings(argumentCollection = tmpStrt);

					var updatedPerformanceSettings = adminWeb.getPerformanceSettings();
					assertEquals(isstruct(updatedPerformanceSettings) ,true);
					assertEquals(updatedPerformanceSettings.typeChecking EQ true ,true);
				});

				it(title="checking resetPerformanceSettings()", body=function( currentSpec ) {
					var adminPerformanceSettings = admin.getPerformanceSettings();
					adminWeb.resetPerformanceSettings();
					var adminWebPerformanceSettings = adminWeb.getPerformanceSettings();

					assertEquals(adminWebPerformanceSettings.inspectTemplate EQ adminPerformanceSettings.inspectTemplate, true);
					assertEquals(adminWebPerformanceSettings.typeChecking EQ adminPerformanceSettings.typeChecking, true);
				});
			});

			// Gateway
			describe( title="test gateway functions", body=function() {
				it(title="checking getGatewayentries()", body=function( currentSpec ) {
					var gatewayEntries = adminweb.getGatewayentries();
					assertEquals(isquery(gatewayEntries) ,true);
				});

				it(title="checking updateGatewayentry()", body=function( currentSpec ) {
					var tmpstruct = {};
					tmpstruct.id = "testDirectorygateway";
					tmpstruct.class = "";
					tmpstruct.cfcPath = "lucee.extension.gateway.DirectoryWatcher";
					tmpstruct.listenerCfcPath = "lucee.extension.gateway.DirectoryWatcherListener";
					tmpstruct.startupMode = "automatic";
					tmpstruct.custom = {};
					tmpstruct.custom.directory = "#expandPath('/')#";
					tmpstruct.custom.recurse = "false";
					tmpstruct.custom.interval = "6000";
					tmpstruct.custom.extensions = "*";
					tmpstruct.custom.changeFunction = "onChange";
					tmpstruct.custom.addFunction = "onAdd";
					tmpstruct.custom.deleteFunction = "onDelete";

					adminweb.updateGatewayEntry(argumentCollection = tmpstruct);
				});

				/*it(title="checking getGatewayentry()", body=function( currentSpec ) {
					var gatewayEntry = adminweb.getGatewayentry('testDirectorygateway');
					assertEquals(isStruct(gatewayEntry) ,true);
					assertEquals(listSort(structKeyList(gatewayEntry),'textnocase'), 'bundleName,bundleVersion,cfcPath,class,custom,id,listenerCfcPath,readOnly,startupMode,state');
				});

				it(title="checking gateway()", body=function( currentSpec ) {
					adminweb.gateway( id="testDirectorygateway", gatewayAction="stop" );
					var gatewayEntry = adminweb.getGatewayentry('testdirectorygateway');
					assertEquals( listFindNoCase("stopped,stopping", gatewayEntry.state) NEQ 0, true );
				});

				it(title="checking removeGatewayEntry()", body=function( currentSpec ) {
					adminweb.removeGatewayEntry( id="testDirectorygateway" );
					var gatewayEntry = adminweb.getGatewayentries();
					assertEquals( listFindNoCase(valueList(gatewayEntry.id), "testDirectorygateway") EQ 0, false );
				});*/
			});

			// Bundles
			describe( title="test bundles functions", body=function() {
				it(title="checking getBundles()", body=function( currentSpec ) {
					var bundles = adminWeb.getBundles();
					assertEquals(isquery(bundles) ,true);
				});

				it(title="checking getBundle()", body=function( currentSpec ) {
					var bundles = adminWeb.getBundles();
					var bundle = adminWeb.getBundle( bundles.symbolicName );
					assertEquals(isStruct(bundle) ,true);
					assertEquals(listSort(structKeyList(bundle),'textnocase'), 'description,fragment,headers,id,path,state,symbolicName,title,usedBy,version');
				});
			});

			// Debug
			describe( title="test debugging functions", body=function() {
				beforeEach(function( currentSpec ){
					debugSettingList = "checking getDebugSetting(),checking updateDebugSetting(),checking resetDebugSetting()";
					debugList = "checking getDebug(),checking updateDebug(),checking resetDebug()";
					if(listFindNoCase(debugSettingList, currentSpec)){
						getDebugSetting = adminWeb.getDebugSetting();
						assertEquals(isStruct(getDebugSetting), true);
					}
					if(listFindNoCase(debugList, currentSpec)){
						getDebug = adminWeb.getDebug();
						assertEquals(isStruct(getDebug), true);
					}
				});

				afterEach(function( currentSpec ){
					if(listFindNoCase(debugSettingList,currentSpec)){
						adminWeb.updateDebugSetting(argumentCollection = getDebugSetting);
					}
					if(listFindNoCase(debugList,currentSpec)){
						adminWeb.updateDebug(argumentCollection = getDebug);
					}
				});

				it(title="checking getLoggedDebugData()", body=function( currentSpec ) {
					var debugLogs = adminWeb.getLoggedDebugData();
					assertEquals(isArray(debugLogs), true);
				});

				it(title="checking getDebugEntry()", body=function( currentSpec ) {
					var debugEntry = admin.getDebugEntry();
					assertEquals(isquery(debugEntry) ,true);
					var strctKeylist = structKeyList(debugEntry);
					assertEquals(FindNocase('ipRange',strctKeylist) GT 0, true);
				});

				it(title="checking updateDebugEntry()", body=function( currentSpec ) {
					var tmpstruct = {};
					tmpstruct.label = "testDebug";
					tmpstruct.type = "lucee-classic";
					tmpstruct.ipRange = "127.0.0.1";
					tmpstruct.custom = {};

					adminweb.updateDebugEntry(argumentCollection = tmpstruct);

					var qDebugEntries = adminweb.getDebugEntry();
					query name="local.qDebugEntriesFiltered" dbtype="query"{
						echo("SELECT * FROM qDebugEntries WHERE label = 'testDebug'")
					}
					assertEquals(qDebugEntriesFiltered.recordCount, 1);
				});

				it(title="checking removeDebugEntry()", body=function( currentSpec ) {
					var qDebugEntries = adminweb.getDebugEntry();
					query name="local.qDebugEntriesFiltered1" dbtype="query"{
						echo("SELECT * FROM qDebugEntries WHERE label = 'testDebug'")
					}
					assertEquals(qDebugEntriesFiltered1.recordCount, 1);

					adminweb.removeDebugEntry(qDebugEntriesFiltered1.id);

					var qDebugEntries = adminweb.getDebugEntry();
					query name="local.qDebugEntriesFiltered2" dbtype="query"{
						echo("SELECT * FROM qDebugEntries WHERE label = 'testDebug'")
					}
					assertEquals(qDebugEntriesFiltered2.recordCount, 0);
				});

				it(title="checking getDebugSetting()", body=function( currentSpec ) {
					var deguggingListSetting = admin.getDebugSetting();
					assertEquals(isstruct(deguggingListSetting) ,true);
					assertEquals(listSort(structKeyList(deguggingListSetting),'textnocase'), 'maxLogs');
				});

				it(title="checking updateDebugSetting()", body=function( currentSpec ) {
					admin.updateDebugSetting( maxLogs=100 );
					var debuggingListSetting = admin.getDebugSetting();
					assertEquals(debuggingListSetting.maxLogs EQ 100, true);
				});

				it(title="checking resetDebugSetting()", body=function( currentSpec ) {
					var adminDebuggingListSetting = admin.getDebugSetting();
					adminWeb.resetDebugSetting();
					var adminWebDebuggingListSetting = adminWeb.getDebugSetting();

					assertEquals(adminWebDebuggingListSetting.maxLogs EQ adminDebuggingListSetting.maxLogs, true);
				});

				it(title="checking getDebug()", body=function( currentSpec ) {
					var debuggingSetting = admin.getDebug();
					assertEquals(isstruct(debuggingSetting) ,true);
					assertEquals(listSort(structKeyList(debuggingSetting),'textnocase'), 'database,debug,dump,exception,implicitAccess,queryUsage,timer,tracing');
				});

				it(title="checking updateDebug()", body=function( currentSpec ) {
					admin.updateDebug( implicitAccess=true );
					var debuggingSetting = admin.getDebug();
					assertEquals(debuggingSetting.implicitAccess EQ true, true);
				});

				it(title="checking resetDebug()", body=function( currentSpec ) {
					var adminDebuggingSetting = admin.getDebug();
					adminWeb.resetDebug();
					var adminWebDebuggingSetting = adminWeb.getDebug();
					var columnlists = ["database","debug","dump","exception","implicitAccess","queryUsage","timer","tracing"];

					for(var columnlist in columnlists){
						assertEquals(adminWebDebuggingSetting["#columnlist#"] EQ adminDebuggingSetting["#columnlist#"], true);
					}
				});
			});

			// Plugin
			describe( title="test plugin functions", body=function() {
				it(title="checking getPluginDirectory()", body=function( currentSpec ) {
					var pluginDirectory = adminWeb.getPluginDirectory();
					assertEquals(isstruct(pluginDirectory), false);
				});
			});

			describe( title="test context functions", body=function() {
				it(title="checking getContextes()", body=function( currentSpec ) {
					var getContext = adminWeb.getContextes();
					assertEquals(isQuery(getContext), true);
					assertEquals(listSort(structKeyList(getContext),'textnocase'), 'clientElements,clientSize,config_file,hash,hasOwnSecContext,id,label,path,sessionElements,sessionSize,url');
				});

				it(title="checking getContexts()", body=function( currentSpec ) {
					var getContexts = adminWeb.getContexts();
					assertEquals(isQuery(getContexts), true);
					assertEquals(listSort(structKeyList(getContexts),'textnocase'), 'clientElements,clientSize,config_file,hash,hasOwnSecContext,id,label,path,sessionElements,sessionSize,url');
				});

				it(title="checking updateContext()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\en.xml";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/en.xml";
					var tmpStruct={};
					tmpStruct.source=path;
					tmpStruct.destination="en.xml";
					admin.updateContext(argumentCollection=tmpStruct);
				});

				it(title="checking updateLabel()", body=function( currentSpec ) {
					var getContexts = adminWeb.getContexts();
					var firstContextHash=getContexts.hash[1];
					var firstContextLabel=getContexts.label[1];
					admin.updateLabel(label="firstContext", hash="#firstContextHash#");
					var updatedContexts = adminWeb.getContexts();
					var firstUpdatedContextHash=updatedContexts.hash[1];
					expect(firstUpdatedContextHash).toBe(firstContextHash);
					// resetting it to to original hash
					admin.updateLabel(label="#firstContextLabel#", hash="#firstContextHash#");
				});

			});

			describe( title="test tldx and Fldx function", body=function() {
				it(title="checking getFlds()", body=function( currentSpec ) {
					var flds = adminWeb.getFlds();
					assertEquals(isquery(flds) ,true);
					assertEquals((flds.displayname EQ 'Lucee Core Function Library'), true);
				});

				it(title="checking getTlds()", body=function( currentSpec ) {
					var tlds = adminWeb.getTlds();
					assertEquals(isquery(tlds) ,true);
					assertEquals((tlds.type EQ 'cfml'), true);
				});
			});

			describe( title="test Tasks functions", body=function() {
				beforeEach(function( currentSpec ){
					taskSettingsList = "checking getTaskSetting(),checking updateTaskSetting(),checking resetTaskSetting()";
					if(listFindNoCase(taskSettingsList, currentSpec)){
						getTaskSetting = adminWeb.getTaskSetting();
						assertEquals(isStruct(getTaskSetting), true);
					}
				});

				afterEach(function( currentSpec ){
					if(listFindNoCase(taskSettingsList,currentSpec)){
						adminWeb.updateTaskSetting(argumentCollection = getTaskSetting);
					}
				});

				it(title="checking getTasks()", body=function( currentSpec ) {
					var spoolertask = adminWeb.getTasks();
					assertEquals(isQuery(spoolertask) ,true);
					assertEquals(listSort(structKeyList(spoolertask),'textnocase'), 'closed,detail,exceptions,id,lastExecution,name,nextExecution,tries,triesmax,type');
				});

				it(title="checking executeTask()", body=function( currentSpec ) {
					adminWeb.executeTask(id="testSpooler");
				});

				it(title="checking removeTask()", body=function( currentSpec ) {
					adminWeb.removeTask(id="testSpooler");
					var spoolertask = adminWeb.getTasks();
					assertEquals(isQuery(spoolertask) ,true);
					assertEquals(listFindNocase(valueList(spoolertask.id),"testSpooler") EQ 0 ,true);
				});

				it(title="checking removeAllTask()", body=function( currentSpec ) {
					adminWeb.removeAllTask();
					var spoolertask = adminWeb.getTasks();
					assertEquals(isQuery(spoolertask) ,true);
					assertEquals(spoolertask.recordcount EQ 0 ,true);
				});

				it(title="checking getTaskSetting()", body=function( currentSpec ) {
					var taskSetting = adminWeb.getTaskSetting();
					assertEquals(isstruct(taskSetting) ,true);
					assertEquals(listSort(structKeyList(taskSetting),'textnocase'), 'maxThreads');
				});

				it(title="checking updateTaskSetting()", body=function( currentSpec ) {
					adminWeb.updateTaskSetting(maxThreads=30);
					var taskSetting = adminWeb.getTaskSetting();
					assertEquals(isstruct(taskSetting) ,true);
					assertEquals(taskSetting.maxThreads EQ 30 ,true);
				});

				it(title="checking resetTaskSetting()", body=function( currentSpec ) {
					var adminTaskSetting = admin.getTaskSetting();
					assertEquals(isstruct(adminTaskSetting) ,true);
					adminWeb.resetTaskSetting();
					var adminWebTaskSetting = adminWeb.getTaskSetting();
					assertEquals(isstruct(adminWebTaskSetting) ,true);
					assertEquals(adminWebTaskSetting.maxThreads EQ adminTaskSetting.maxThreads,true);
				});
			});

			describe( title="test CfxTags functions", body=function() {
				it(title="checking verifyCFX()", body=function( currentSpec ) {
					adminWeb.verifyCFX(name="helloworld");
				});

				it(title="checking getJavaCfxTags()", body=function( currentSpec ) {
					var javaCfxTags = adminWeb.getJavaCfxTags();
					assertEquals(isquery(javaCfxTags) ,true);
					var strctKeylist = structKeyList(javaCfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});

				it(title="checking verifyJavaCFX()", body=function( currentSpec ) {
					adminWeb.verifyJavaCFX(name="helloworld", class="lucee.cfx.example.HelloWorld");
				});

				it(title="checking updatejavacfx()", body=function( currentSpec ) {
					var tmpstruct = {};
					tmpstruct.name = "testJavaCFX";
					tmpstruct.class = "lucee.cfx.example.HelloWorld";
					adminWeb.updatejavacfx(argumentCollection=tmpstruct);
					var javaCfxTags = adminWeb.getJavaCfxTags();
					assertEquals(isquery(javaCfxTags) ,true);
					assertEquals(listFindNocase(valueList(javaCfxTags.name),"testJavaCFX") GT 0, true);
				});

				it(title="checking removecfx()", body=function( currentSpec ) {
					adminWeb.removecfx(name="helloworld");
				});
			});

			describe( title="test LoginSettings functions", body=function() {
				beforeEach(function( currentSpec ){
					getLoginSettings = admin.getLoginSettings();
					assertEquals(isStruct(getLoginSettings), true);
				});

				afterEach(function( currentSpec ){
					admin.updateLoginSettings(argumentCollection = getLoginSettings);
				});

				it(title="checking getLoginSettings()", body=function( currentSpec ) {
					var loginSettings = admin.getLoginSettings();
					assertEquals(isstruct(loginSettings) ,true);
					assertEquals(listSort(structKeyList(loginSettings),'textnocase'),'captcha,delay,rememberme');
				});

				it(title="checking updateLoginSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.captcha = "true";
					admin.updateLoginSettings(argumentCollection=tmpStrt);
					var loginSettings = admin.getLoginSettings();
					assertEquals(isstruct(loginSettings) ,true);
					assertEquals(loginSettings.captcha EQ 'true' ,true);
				});
			});

			xdescribe( title="test log functions", body=function() {
				it(title="checking getLogSettings()", body=function( currentSpec ) {
					var logsettings = adminweb.getLogSettings();
					assertEquals(isquery(logsettings) ,true);
					assertEquals(listSort(structKeyList(logsettings),'textnocase'), 'appenderArgs,appenderBundleName,appenderBundleVersion,appenderClass,layoutArgs,layoutBundleName,layoutBundleVersion,layoutClass,level,name,readonly');
				});

				it(title="checking updateLogSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					var logsettings = adminweb.getLogSettings();
					assertEquals(isquery(logsettings) ,true);
					tmpStrt.name = "testlog";
					tmpStrt.level = logsettings.level;
					tmpStrt.appenderClass = logsettings.appenderClass;
					tmpStrt.layoutClass = logsettings.layoutClass;
					tmpStrt.appenderArgs = {"charset":"windows-1252","maxFiles":"10","maxFileSize":"10485760","path":"{lucee-config}/logs/exception.log","timeout":"180"};
					tmpStrt.layoutArgs = logsettings.layoutArgs;
					adminweb.updateLogSettings(argumentCollection = tmpStrt);
					logsettings = adminweb.getLogSettings();
					assertEquals(listFindNoCase(valueList(logsettings.name),"testLOG") NEQ 0 ,true);
				});

				it(title="checking removeLogSetting()", body=function( currentSpec ) {
					adminweb.removeLogSetting(name="testlog");
					var logsettings = adminweb.getLogSettings();
					assertEquals(isQuery(logsettings) ,true);
					assertEquals(listFindNoCase(valueList(logsettings.name),"testlog") EQ 0 ,true);
				});
			});

			describe( title="test application listener functions", body=function() {
				beforeEach(function( currentSpec ){
					getApplicationListener = adminWeb.getApplicationListener();
					assertEquals(isStruct(getApplicationListener), true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateApplicationListener(argumentCollection = getApplicationListener);
				});

				it(title="checking getApplicationListener()", body=function( currentSpec ) {
					var appListner = adminWeb.getApplicationListener();
					assertEquals(isstruct(appListner) ,true);
					assertEquals(listSort(structKeyList(appListner),'textnocase'),'mode,type');
				});

				it(title="checking updateApplicationListener()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.type = "mixed";
					tmpStrt.mode = "root";
					adminWeb.updateApplicationListener(argumentCollection = tmpStrt);
					var appListner = adminWeb.getApplicationListener();
					assertEquals(isstruct(appListner) ,true);
					assertEquals(appListner.mode EQ 'root' ,true);
				});

				it(title="checking resetApplicationListener()", body=function( currentSpec ) {
					var adminAppListner = adminWeb.getApplicationListener();
					assertEquals(isStruct(adminAppListner) ,true);
					adminWeb.resetApplicationListener();
					var adminWebAppListner = adminWeb.getApplicationListener();
					assertEquals(isStruct(adminWebAppListner) ,true);
					assertEquals(adminWebAppListner.mode EQ adminAppListner.mode,true);
					assertEquals(adminWebAppListner.type EQ adminAppListner.type,true);
				});
			});

			describe( title="test scope functions", body=function() {
				beforeEach(function( currentSpec ){
					getScope = adminWeb.getScope();
				});

				afterEach(function( currentSpec ){
					adminWeb.updateScope(argumentCollection = getScope);
				});

				it(title="checking getscope()", body=function( currentSpec ) {
					var scope = adminWeb.getScope();
					assertEquals(isstruct(scope) ,true);
					assertEquals(listSort(structKeyList(scope),'textnocase'), 'allowImplicidQueryCall,applicationTimeout,applicationTimeout_day,applicationTimeout_hour,applicationTimeout_minute,applicationTimeout_second,cgiReadonly,clientCookies,clientManagement,clientStorage,clientTimeout,clientTimeout_day,clientTimeout_hour,clientTimeout_minute,clientTimeout_second,domainCookies,localmode,mergeFormAndUrl,scopeCascadingType,sessionManagement,sessionStorage,sessionTimeout,sessionTimeout_day,sessionTimeout_hour,sessionTimeout_minute,sessionTimeout_second,sessiontype');
				});

				it(title="checking updateScope()", body=function( currentSpec ) {
					var scope = adminWeb.getScope();
					assertEquals(isstruct(scope) ,true);
					var tmpStrt = {};
					tmpStrt.scopeCascadingType = scope.scopeCascadingType;
					tmpStrt.allowImplicidQueryCall = scope.allowImplicidQueryCall;
					tmpStrt.mergeFormAndUrl = scope.mergeFormAndUrl;
					tmpStrt.sessionManagement = scope.sessionManagement;
					tmpStrt.clientManagement = scope.clientManagement;
					tmpStrt.domainCookies = scope.domainCookies;
					tmpStrt.clientCookies = scope.clientCookies;
					tmpStrt.clientTimeout = createTimeSpan(100,0,0,0);
					tmpStrt.sessionTimeout = createTimeSpan(0,0,50,0);
					tmpStrt.clientStorage = scope.clientStorage;
					tmpStrt.sessionStorage = scope.sessionStorage;
					tmpStrt.applicationTimeout = scope.applicationTimeout;
					tmpStrt.sessionType = scope.sessionType;
					tmpStrt.localMode = scope.localMode;
					tmpStrt.cgiReadonly = scope.cgiReadonly;
					adminWeb.updateScope(argumentCollection = tmpStrt);

					var getUpdatedScope = adminWeb.getScope();
					assertEquals(isstruct(getUpdatedScope) ,true);
					assertEquals(getUpdatedScope.clientTimeout_day EQ 100 ,true);
					assertEquals(getUpdatedScope.sessionTimeout_minute EQ 50 ,true);
				});

				it(title="checking resetScope()", body=function( currentSpec ) {
					var adminScope = admin.getScope();
					assertEquals(isstruct(adminScope) ,true);
					adminWeb.resetScope();
					var adminWebScope = adminWeb.getScope();
					assertEquals(isstruct(adminWebScope) ,true);
					assertEquals(adminWebScope.clientTimeout_day EQ adminScope.clientTimeout_day ,true);
					assertEquals(adminWebScope.sessionTimeout_minute EQ adminScope.sessionTimeout_minute ,true);
					assertEquals(adminWebScope.allowImplicidQueryCall EQ adminScope.allowImplicidQueryCall ,true);
					assertEquals(adminWebScope.clientStorage EQ adminScope.clientStorage ,true);
					assertEquals(adminWebScope.sessionStorage EQ adminScope.sessionStorage ,true);
					assertEquals(adminWebScope.localmode EQ adminScope.localmode ,true);
				});
			});

			describe( title="test restSettings functions", body=function() {
				beforeEach(function( currentSpec ){
					getRestSettings = adminWeb.getRestSettings();
					assertEquals(isStruct(getRestSettings), true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateRestSettings(argumentCollection = getRestSettings);
				});

				it(title="checking getRestSettings()", body=function( currentSpec ) {
					var restSettings = adminWeb.getRestSettings();
					assertEquals(isstruct(restSettings) ,true);
					assertEquals(listSort(structKeyList(restSettings),'textnocase'),'list');
				});

				it(title="checking updateRestSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.list = "true";
					adminWeb.updateRestSettings(argumentCollection=tmpStrt);
					var restSettings = adminWeb.getRestSettings();
					assertEquals(isStruct(restSettings) ,true);
					assertEquals(restSettings.list EQ 'true' ,true);
				});

				it(title="checking resetRestSettings()", body=function( currentSpec ) {
					var adminRestSettings = admin.getRestSettings();
					assertEquals(isStruct(adminRestSettings) ,true);
					adminWeb.resetRestSettings();
					var adminWebRestSettings = adminWeb.getRestSettings();
					assertEquals(isStruct(adminWebRestSettings) ,true);
					assertEquals(adminWebRestSettings.list EQ adminRestSettings.list, true);
				});
			});


			describe( title="test restMappings functions", body=function() {
				it(title="checking getRestMappings()", body=function( currentSpec ) {
					var restMappings = adminWeb.getRestMappings();
					assertEquals(isQuery(restMappings) ,true);
					assertEquals(listSort(structKeyList(restMappings),'textnocase'),'default,hidden,physical,readonly,strphysical,virtual');
				});

				it(title="checking updateRestMapping()", body=function( currentSpec ) {

					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var tmpStrt = {};
					tmpStrt.default = "true";
					tmpStrt.virtual = "/testRestMapping";
					tmpStrt.physical = curr&('/testRestMapping');
					adminWeb.updateRestMapping(argumentCollection=tmpStrt);
					var restMappings = adminWeb.getRestMappings();
					assertEquals((isquery(restMappings) && findNocase( "/testRestMapping", valueList(restMappings.virtual)) EQ 1) ,true);
				});

				it(title="checking removeRestMapping()", body=function( currentSpec ) {
					adminWeb.removeRestMapping("testRestMapping");
					var restMappingsAfterRemove = adminWeb.getRestMappings();
					assertEquals((isquery(restMappingsAfterRemove) && findNocase( "/testRestMapping", valueList(restMappingsAfterRemove.virtual)) EQ 0) ,true);
				});
			});

			describe( title="test application functions", body=function() {
				beforeEach(function( currentSpec ){
					getApplicationSetting = adminWeb.getApplicationSetting();
					assertEquals(isStruct(getApplicationSetting) ,true);
				});

				afterEach(function( currentSpec ){
					adminWeb.updateApplicationSetting(argumentCollection = getApplicationSetting);
				});

				it(title="checking getApplicationSetting()", body=function( currentSpec ) {
					var appSetting = adminWeb.getApplicationSetting();
					assertEquals(isStruct(appSetting) ,true);
					assertEquals(listSort(structKeyList(appSetting),'textnocase'),'AllowURLRequestTimeout,requestTimeout,requestTimeout_day,requestTimeout_hour,requestTimeout_minute,requestTimeout_second,scriptProtect');
				});

				it(title="checking updateApplicationSetting()", body=function( currentSpec ) {
					var appSetting = adminWeb.getApplicationSetting();
					var tmpStrt = {};
					tmpStrt.requestTimeout = createTimeSpan(0,2,0,0);
					tmpStrt.scriptProtect = appSetting.scriptProtect;
					tmpStrt.allowURLRequestTimeout = appSetting.allowURLRequestTimeout;
					adminWeb.updateApplicationSetting(argumentCollection = tmpStrt);

					var updatedAppSetting = adminWeb.getApplicationSetting();
					assertEquals(isStruct(updatedAppSetting) ,true);
					assertEquals(updatedAppSetting.requestTimeout_hour EQ 2 ,true);
				});

				it(title="checking resetApplicationSetting()", body=function( currentSpec ) {
					var adminAppSetting = admin.getApplicationSetting();
					assertEquals(isStruct(adminAppSetting) ,true);
					adminWeb.resetApplicationSetting();
					var adminWebAppSetting = adminWeb.getApplicationSetting();
					assertEquals(isStruct(adminWebAppSetting) ,true);
					assertEquals(adminWebAppSetting.requestTimeout_day EQ adminAppSetting.requestTimeout_day,true);
					assertEquals(adminWebAppSetting.requestTimeout_hour EQ adminAppSetting.requestTimeout_hour,true);
					assertEquals(adminWebAppSetting.requestTimeout_minute EQ adminAppSetting.requestTimeout_minute,true);
					assertEquals(adminWebAppSetting.requestTimeout_second EQ adminAppSetting.requestTimeout_second,true);
				});
			});

			describe( title="test QueueSetting functions", body=function() {
				beforeEach(function( currentSpec ){
					getQueueSetting = admin.getQueueSetting();
					assertEquals(isStruct(getQueueSetting) ,true);
				});

				afterEach(function( currentSpec ){
					admin.updateQueueSetting(argumentCollection = getQueueSetting);
				});

				it(title="checking getQueueSetting()", body=function( currentSpec ) {
					var queueSettings = admin.getQueueSetting();
					assertEquals(isstruct(queueSettings) ,true);
					assertEquals(listSort(structKeyList(queueSettings),'textnocase'), 'enable,max,timeout');
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

			describe( title="test password function", body=function() {
				it(title="checking updatePassword()", body=function( currentSpec ) {
					admin.updatePassword(oldPassword="#request.ServerAdminPassword#", newPassword="server" );
					try{
						// This fails, if prev statement updates the password for server admin.
						admin.updatePassword(oldPassword="#request.ServerAdminPassword#", newPassword="server" );
					}catch( any e ){
						assertEquals( e.message, 'No access, password is invalid' );
					}
					admin.updatePassword(oldPassword="server", newPassword="#request.ServerAdminPassword#" );
				});

				it(title="checking getDefaultPassword()", body=function( currentSpec ) {
					var defaultPassword = admin.getDefaultPassword();
					expect(defaultPassword).toBeTypeOf("string");
				});

				it(title="checking removeDefaultPassword()", body=function( currentSpec ) {
					admin.removeDefaultPassword();
					var defaultPassword = admin.getDefaultPassword();
				});

				it(title="checking updateDefaultPassword()", body=function( currentSpec ) {
					admin.updateDefaultPassword(newPassword="server");
					var defaultPassword = admin.getDefaultPassword();
					expect(defaultPassword).toBeTypeOf("string");
				});

				it(title="checking resetPassword()", body=function( currentSpec ) {
					var contexts = adminWeb.getContexts();
					admin.resetPassword(contextPath=contexts.path);
					// resetting the password for current web context to original value
					adminweb.updatePassword(oldPassword="server", newPassword="#request.WebAdminPassword#" );
				});

				it(title="checking hashpassword()", body=function( currentSpec ) {
					var hashpassword = admin.hashpassword();
				});
			});

			describe( title="test CustomTag functions", body=function() {
				beforeEach(function( currentSpec ){
					if(currentSpec == 'checking getCustomTagSetting()'){
						customTag_Setting = adminWeb.getCustomTagSetting();
					}
				});

				afterEach(function( currentSpec ){
					if(currentSpec == 'checking updateCustomTagSetting()'){
						customTag_Setting.extensions = ArraytoList(customTag_Setting.extensions);
						adminWeb.updateCustomTagSetting(argumentCollection = customTag_Setting);
					}

					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var trg = curr&"/Administrator/TestCTArchive.lar";
					if(fileExists(trg))fileDelete(trg);
				});

				it(title="checking getCustomTagSetting()", body=function( currentSpec ) {
					var customTagSetting = adminWeb.getCustomTagSetting();
					assertEquals(isstruct(customTagSetting) ,true);
					assertEquals(listSort(structKeyList(customTagSetting),'textnocase'), 'customTagDeepSearch,customTagLocalSearch,customTagPathCache,deepSearch,extensions,localSearch');
				});

				it(title="checking updateCustomTagSetting()", body=function( currentSpec ) {
					var customTagSetting = adminWeb.getCustomTagSetting();
					customTagSetting.deepSearch = true;
					customTagSetting.customTagLocalSearch = false;
					customTagSetting.extensions = ArraytoList(customTagSetting.extensions);
					adminWeb.updateCustomTagSetting(argumentCollection = customTagSetting);
					var updatedSetting = adminWeb.getCustomTagSetting();
					assertEquals( updatedSetting.deepSearch EQ true, true );
				});

				it(title="checking updatecustomtag()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\TestArchive";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";

					adminWeb.updatecustomtag( virtual="/testcustomtag", physical=path, archive="", primary="Resource", inspect="");
					var customTagMappings = adminWeb.getCustomTagMappings();
					assertEquals(isQuery(customTagMappings) ,true);
					assertEquals( listFindNoCase(valueList(customTagMappings.virtual), "/testcustomtag") NEQ 0, true );
				});

				it(title="checking createCTArchive()", body=function( currentSpec ) {
					//var path = "#expandpath('../')#test\components\Administrator\TestArchive";
					var curr=getDirectoryFromPath(GetCurrentTemplatePath());
					var path = curr&"/Administrator/TestArchive";
					var trg = curr&"/Administrator/TestCTArchive.lar";

					var tmpStrt = {};
					tmpStrt.virtual = "/testcustomtag";
					tmpStrt.file =trg;
					tmpStrt.addCFMLFile = true;
					tmpStrt.addNonCFMLFile = true;
					adminWeb.createCTArchive(argumentCollection=tmpStrt);

					assertTrue(fileExists(trg));
					if(fileExists(trg))fileDelete(trg);

				});

				it(title="checking removecustomtag()", body=function( currentSpec ) {
					adminWeb.removecustomtag( virtual="/testcustomtag" );
					var customTagMappings = adminWeb.getCustomTagMappings();
					assertEquals( listFindNoCase(valueList(customTagMappings.virtual), "/testcustomtag"), false );
				});
			});

			// Error
			describe( title="test error functions", body=function() {
				beforeEach(function( currentSpec ){
					if(currentSpec == 'checking getError()'){
						errorGet=adminWeb.getError();
						errorTemplate = {};
						errorTemplate.template404 = errorGet.templates.404;
						errorTemplate.template500 = errorGet.templates.500;
					}
				});

				afterEach(function( currentSpec ){
					if(currentSpec == 'checking updateError()' || currentSpec == 'checking resetError()'){
						adminWeb.updateError(argumentCollection=errorTemplate);
					}
				});

				it(title="checking getError()", body=function( currentSpec ) {
					var error = adminWeb.getError();
					assertEquals(isStruct(error) ,true);
					var strctKeylist = structKeyList(error);
					assertEquals(FindNocase('doStatusCode',strctKeylist) GT 0, true);
				});

				it(title="checking updateError()", body=function( currentSpec ) {
					var error = adminWeb.getError();
					var tmpstruct = {};
					tmpstruct.template500 = "/lucee/templates/error/test.cfm";
					tmpstruct.template404 = "/lucee/templates/error/test.cfm";
					tmpstruct.statuscode = true;
					adminWeb.updateError(argumentCollection=tmpstruct);
					var updatedError = adminWeb.getError();
					assertEquals(isStruct(updatedError) ,true);
					assertEquals(updatedError.str[404] EQ "/lucee/templates/error/test.cfm", true);
					assertEquals(updatedError.str[500] EQ "/lucee/templates/error/test.cfm", true);
					assertEquals(updatedError.doStatusCode EQ true, true);
				});

				it(title="checking resetError()", body=function( currentSpec ) {
					var adminError = admin.getError();
					assertEquals(isStruct(adminError) ,true);
					adminWeb.resetError();
					var adminWebError = adminWeb.getError();
					assertEquals(isStruct(adminWebError) ,true);
					assertEquals(adminWebError.str[404] EQ adminError.str[404], true);
					assertEquals(adminWebError.str[500] EQ adminError.str[500], true);
					assertEquals(adminWebError.doStatusCode EQ adminError.doStatusCode, true);
				});
			});

			// security manager
			describe( title="test securityManager functions", body=function() {
				beforeEach(function( currentSpec ){
					if(currentSpec == 'checking createSecurityManager()'){
						getConxt=admin.getContexts();
						contextsSingle = QueryRowData(getConxt, 1);
						getSecManager = admin.getSecurityManager(contextsSingle.id);
					}
				});

				afterEach(function( currentSpec ){
					if(currentSpec == 'checking updateSecurityManager()'){
						getSecManager.id = contextsSingle.id;
						getSecManager.datasource=getSecManager.datasource != -1 ? getSecManager.datasource : "yes";
						admin.updateSecurityManager(argumentCollection = getSecManager);
					}
				});

				it(title="checking securityManager()", body=function( currentSpec ) {
					var debuggingSecurityManager = admin.securityManager(secType="debugging", secvalue="" );
					assertEquals(isBoolean(debuggingSecurityManager), true);
				});

				it(title="checking createSecurityManager()", body=function( currentSpec ) {
					var contexts=admin.getContexts();
					admin.createsecuritymanager(id=contexts.id[1]);
					var testContexts=admin.getContexts();
					var result = QueryRowData(testContexts, 1);
					assertEquals( result.hasOwnSecContext, true );
				});

				it(title="checking getSecurityManager()", body=function( currentSpec ) {
					var contexts=admin.getContexts();
					var SecurityManager=admin.getSecurityManager(id=contexts.id[1]);
					assertEquals(listSort(structKeyList(SecurityManager),'textnocase'), 'access_read,access_write,cache,cfx_setting,cfx_usage,custom_tag,datasource,debugging,direct_java_access,file,file_access,gateway,mail,mapping,orm,remote,scheduled_task,search,setting,tag_execute,tag_import,tag_object,tag_registry');
				});

				it(title="checking updateSecurityManager()", body=function( currentSpec ) {
					var updateSecurityManager={};
					var context=admin.getContexts().id[1];
					var SecurityManager=admin.getSecurityManager(id=context);

					SecurityManager.id=context;
					SecurityManager.datasource=SecurityManager.datasource != -1 ? SecurityManager.datasource : "yes";
					updateSecurityManager=SecurityManager;
					updateSecurityManager.tag_registry=!SecurityManager.tag_registry;
					admin.updateSecurityManager(argumentCollection=updateSecurityManager);
					updatedSecurityManager=admin.getSecurityManager(id=context);
					assertEquals( val(updatedSecurityManager.tag_registry) == val(!SecurityManager.tag_registry), true);
				});

				it(title="checking removeSecurityManager()", body=function( currentSpec ) {
					var contexts=admin.getContexts();
					var securityManagerId =  QueryRowData(contexts, 1);
					admin.removeSecurityManager(id=securityManagerId.id);
					var testContexts=admin.getContexts();
					var result = QueryRowData(testContexts, 1);
					assertEquals( result.hasOwnSecContext, false );
				});

				it(title="checking getDefaultSecurityManager()", body=function( currentSpec ) {
					var defaultSecurityManager=admin.getDefaultSecurityManager();
					assertEquals(listSort(structKeyList(defaultSecurityManager),'textnocase'), 'access_read,access_write,cache,cfx_setting,cfx_usage,custom_tag,datasource,debugging,direct_java_access,file,file_access,gateway,mail,mapping,orm,remote,scheduled_task,search,setting,tag_execute,tag_import,tag_object,tag_registry');
				});

				it(title="checking updateDefaultSecurityManager()", body=function( currentSpec ) {
					var updateDefaultSecurityManager={};
					var defaultSecurityManager=admin.getDefaultSecurityManager();

					defaultSecurityManager.datasource=defaultSecurityManager.datasource != -1 ? defaultSecurityManager.datasource : "yes";
					updateDefaultSecurityManager=defaultSecurityManager;
					updateDefaultSecurityManager.tag_registry=!defaultSecurityManager.tag_registry;

					admin.updateDefaultSecurityManager(argumentCollection=updateDefaultSecurityManager);
					updatedDefaultSecurityManager=admin.getDefaultSecurityManager();
					assertEquals( val(updatedDefaultSecurityManager.tag_registry) == val(!defaultSecurityManager.tag_registry), true);
				});
			});

			// storage
			describe( title="test storage functions", body=function() {
				it(title="checking storageSet()", body=function( currentSpec ) {
					adminWeb.storageSet( key="test", value="result" );
					var getStorage = adminWeb.storageGet( key="test" );
					assertEquals(getStorage EQ 'result', true);
				});

				it(title="checking storageGet()", body=function( currentSpec ) {
					var getStorage = adminWeb.storageGet( key="test" );
					assertEquals(getStorage EQ 'result', true);
				});
			});

			// API key
			describe( title="test API functions", body=function() {
				it(title="checking updateAPIkey()", body=function( currentSpec ) {
					variables.APIkey=createGUid();
					adminWeb.updateAPIkey(APIkey);
					var getAPIkey=adminWeb.getAPIkey();
					assertEquals(getAPIkey EQ APIkey, true);
				});

				it(title="checking getAPIkey()", body=function( currentSpec ) {
					var getAPIkey=adminWeb.getAPIkey();
					assertEquals(getAPIkey EQ APIkey, true);
				});

				it(title="checking removeAPIKey()", body=function( currentSpec ) {
					adminWeb.removeAPIKey();
				});
			});

			// update
			/*describe( title="test update functions", body=function() {
				beforeEach(function( currentSpec ){
					if(currentSpec == 'checking getUpdate()'){
						update = admin.getUpdate();
					}
				});

				afterEach(function( currentSpec ){
					if(currentSpec == 'checking updateUpdate()'){
						admin.updateUpdate(argumentCollection = update);
					}
				});

				it(title="checking getUpdate()", body=function( currentSpec ) {
					var getUpdate = admin.getUpdate();
					assertEquals(isStruct(getUpdate), true);
					assertEquals(listSort(structKeyList(getUpdate),'textnocase'),'location,type');
				});

				it(title="checking updateUpdate()", body=function( currentSpec ) {
					admin.updateUpdate(type="automatic",location="http://test.lucee.org");
					var updatedGetUpdate = admin.getUpdate();
					assertEquals(isStruct(updatedGetUpdate), true);
					assertEquals(updatedGetUpdate.type EQ "automatic", true);
					assertEquals(updatedGetUpdate.location EQ "http://test.lucee.org", true);
				});

				it(title="checking runUpdate()", body=function( currentSpec ) {
					//admin.runUpdate();
				});

				it(title="checking removeUpdate()", body=function( currentSpec ) {
					// admin.removeUpdate();
				});
			});*/

			// reset id
			describe( title="test resetId functions", body=function() {
				it(title="checking resetId()", body=function( currentSpec ) {
					adminWeb.resetId();
				});
			});


			// restart
			/*describe( title="test restart functions", body=function() {
				it(title="checking restart()", body=function( currentSpec ) {
					//admin.restart();
				});
			});*/

			// min version
			describe( title="test getMinVersion functions", body=function() {
				it(title="checking getMinVersion()", body=function( currentSpec ) {
					var minVersion = admin.getMinVersion();
					assertEquals(len(minVersion) GT 0,true);
				});
			});


			// Patches
			describe( title="test listPatches functions", body=function() {
				it(title="checking listPatches()", body=function( currentSpec ) {
					var listPatches = admin.listPatches();
					assertEquals(isArray(listPatches),true);
				});
			});

			// Update
			describe( title="test changeVersionTo function", body=function() {
				it(title="checking changeVersionTo()", body=function( currentSpec ) {
					restBasePath="/rest/update/provider/";
					var getUpdate = admin.getUpdate();

					http
					url="#getUpdate.location##restBasePath#info/#server.lucee.version#"
					method="get" resolveurl="no" result="local.http";

					assertEquals(isJson(http.filecontent), true);
					updateAvailable=deserializeJson(http.filecontent);

					if (arrayIsEmpty(updateAvailable.otherVersions)){

						systemOutput("WARNING: updateAvailable.otherVersions is empty", true, true);
					}
					else {

						LatestVersion = ArrayLast(updateAvailable.otherVersions);
					}

					/*if( ReplaceNocase(replaceNocase(LatestVersion, ".", "", "ALL"), "-SNAPSHOT", "") GT ReplaceNocase(replaceNocase(server.lucee.version, ".", "", "ALL"), "-SNAPSHOT", "") ){
						admin.changeVersionTo(LatestVersion);
					}
					changing the version breaks testing for all following testcases!!!!
					*/
				});
			});

			describe( title="test connect function", body=function() {
				it(title="checking connect()", body=function( currentSpec ) {
					adminWeb.connect();
				});
			});
		});
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var mySQL={};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) &&
			!isNull(server.system.environment.MYSQL_USERNAME) &&
			!isNull(server.system.environment.MYSQL_PASSWORD) &&
			!isNull(server.system.environment.MYSQL_PORT) &&
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) &&
			!isNull(server.system.properties.MYSQL_USERNAME) &&
			!isNull(server.system.properties.MYSQL_PASSWORD) &&
			!isNull(server.system.properties.MYSQL_PORT) &&
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return mysql;
	}
}