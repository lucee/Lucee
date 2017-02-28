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


component extends="org.lucee.cfml.test.LuceeTestCase"{
	 function beforeAll(){
		request.WEBADMINPASSWORD = "password";
		request.SERVERADMINPASSWORD = "password";
		variables.admin=new org.lucee.cfml.Administrator("server",request.SERVERADMINPASSWORD);
		variables.adminweb=new org.lucee.cfml.Administrator("web", request.WEBADMINPASSWORD);
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

			describe( title="test-timezone functions", body=function() {
				it(title="testGetAvailableTimeZones()", body=function( currentSpec ) {
					var timezones=admin.getAvailableTimeZones();
					assertEquals(isQuery(timezones),true);
					assertEquals(listSort(timezones.columnlist,'textnocase'),'DISPLAY,ID');
				});

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

				it(title="testgetDatasource()", body=function( currentSpec ) {
					var getDatasource = admin.getDatasource('TestDSN1');
					assertEquals(isstruct(getDatasource) ,true);
					assertEquals(getDatasource.name EQ 'TestDSN1', true);
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
					admin.updateDatasource(argumentCollection = #tmpStrt#);
					var getDatasource = admin.getDatasources();
					assertEquals(IsQuery(getDatasource), true);
					var ListOfDSNName = valueList(getDatasource.name);
					assertEquals((findnoCase('testDSN1', ListOfDSNName) GT 0), true);
				});

				it(title="testremoveDatasource()", body=function( currentSpec ) {
					admin.removeDatasource('testDSN1');
					var getDatasource = admin.getDatasources();
					var ListOfDSNName = valueList(getDatasource.name);
					assertEquals((findnoCase('testDSN1', ListOfDSNName) EQ 0), true);
				});

				it(title="checking verifyDatasource()", body=function( currentSpec ) {
					var getDatasource = admin.getDatasources();
					assertEquals(IsQuery(getDatasource), true);
					tmpStrt.name = getDatasource.name;
					tmpStrt.dbusername = getDatasource.username;
					tmpStrt.dbpassword = getDatasource.password;
					var verfiyDataSource = admin.verifyDatasource(argumentCollection = #tmpStrt#);
					assertEquals(isstruct(verfiyDataSource) ,true);
					assertEquals(verfiyDataSource.label,"ok");
				});

				it(title="checking getDatasourceDriverList()", body=function( currentSpec ) {
					var datasourceDriverList = admin.getDatasourceDriverList();
					assertEquals(isQuery(datasourceDriverList),true);
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

				it(title="checking verifyMailServer()", body=function( currentSpec ) {
					var verifyMailServer = admin.verifyMailServer( hostname="smtp.gmail.com", port="587", mailusername="test@gmail.com", mailpassword="test" );
					assertEquals(verifyMailServer.label,"ok");
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

				it(title="checking getMapping()", body=function( currentSpec ) {
					var virtual = "/lucee-server";
					var mapping = admin.getMapping(virtual);
					assertEquals(isstruct(mapping) ,true);
					var strctKeylist = structKeyList(mapping);
					assertEquals(FindNocase('toplevel',strctKeylist) GT 0, true);
				});

				it(title="checking updateMapping()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.virtual = "/TestArchive";
					tmpStrt.physical = "#expandPath('./LDEV1159/TestArchive')#";
					tmpStrt.archive = "#expandPath('./')#TestArchive.lar";
					tmpStrt.primary = "Resources";
					tmpStrt.inspect = "Once";
					tmpStrt.toplevel = true;
					admin.updateMapping(argumentCollection = #tmpStrt#);
					var getMappings = admin.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/TestArchive", ListOfvirtual) GT 0, true);
				});

				it(title="checking remove mapping()", body=function( currentSpec ) {
					var virtual = "/TestArchive";
					admin.removeMapping(virtual);
					var getMappings = admin.getMappings();
					var ListOfvirtual = valueList(getMappings.virtual);
					assertEquals(Find("/TestArchive", ListOfvirtual) EQ 0, true);
				});

				it(title="checking compileMapping()", body=function( currentSpec ) {
					admin.compileMapping('/TestArchive', true);
				});

				it(title="checking compileCTMapping()", body=function( currentSpec ) {
					admin.compileCTMapping('/TestArchive');
				});

				it(title="checking compileComponentMapping()", body=function( currentSpec ) {
					admin.compileComponentMapping('/TestArchive');
				});

				it(title="checking createArchiveFromMapping()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.virtual = "/TestArchive";
					tmpStrt.addCFMLFile = false;
					tmpStrt.addNonCFMLFile = false;
					tmpStrt.doDownload = false;
					tmpStrt.target = "#expandPath('./LDEV1159/TestArchive.lar')#";
					admin.createArchiveFromMapping(argumentCollection = tmpStrt);
					var getMappings = admin.getMappings();
					var result = QueryExecute(
						sql="SELECT Archive
						 FROM getMappings where Virtual = '/TestArchive' and Archive != ''",
						options=
						{dbtype="query"}
					);
					assertEquals(result.recordcount EQ 1,true);
				});

				it(title="checking getCustomTagMappings()", body=function( currentSpec ) {
					var customTagMappings = admin.getCustomTagMappings();
					assertEquals(isquery(customTagMappings) ,true);
					var strctKeylist = structKeyList(customTagMappings);
					assertEquals(FindNocase('readonly',strctKeylist) GT 0, true);
				});
			});

			describe( title="test-Extension functions", body=function() {
				it(title="checking getExtensions()", body=function( currentSpec ) {
					var getExtensions = admin.getExtensions();
					assertEquals(isquery(getExtensions) ,true);
				});

				it(title="checking getExtensionInfo()", body=function( currentSpec ) {
					var extensionsInfo = admin.getExtensionInfo();
					assertEquals(isStruct(extensionsInfo) ,true);
					assertEquals(listSort(structKeyList(extensionsInfo),'textnocase'),'directory,enabled');
				});

				it(title="checking updateExtensionInfo()", body=function( currentSpec ) {
					admin.updateExtensionInfo(enabled=true);
					var extensionsInfo = admin.getExtensionInfo();
					assertEquals(isStruct(extensionsInfo) ,true);
					assertEquals(extensionsInfo.enabled EQ true ,true);
				});

				it(title="checking updateExtension()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.provider = "http://extension.lucee.org";
					tmpStrt.id = '2BCD080F-4E1E-48F5-BEFE794232A21AF6';
					tmpStrt.version = '1.3.1';
					admin.updateExtension(argumentCollection = #tmpStrt#);
				});

				it(title="checking RemoveExtension()", body=function( currentSpec ) {
					admin.removeExtension('2BCD080F-4E1E-48F5-BEFE794232A21AF6');
				});

				it(title="checking getRHExtensions()", body=function( currentSpec ) {
					var getRHExtensions = adminweb.getRHExtensions();
					assertEquals(isquery(getRHExtensions) ,true);
					assertEquals(listSort(structKeyList(getRHExtensions),'textnocase'),'applications,archives,bundles,categories,components,config,contexts,description,eventGateways,flds,functions,id,image,name,plugins,releaseType,startBundles,tags,tlds,trial,version,webcontexts');

				});

				it(title="checking getRHServerExtensions()", body=function( currentSpec ) {
					var getRHServerExtensions = adminweb.getRHServerExtensions();
					assertEquals(isquery(getRHServerExtensions) ,true);
					assertEquals(listSort(structKeyList(getRHServerExtensions),'textnocase'),'applications,archives,bundles,categories,components,config,contexts,description,eventGateways,flds,functions,id,image,name,plugins,releaseType,startBundles,tags,tlds,trial,version,webcontexts');
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
			});

			describe( title="test-extension providers functions", body=function() {
				it(title="checking getExtensionProviders()", body=function( currentSpec ) {
					var getExtensionsProvider = admin.getExtensionProviders();
					assertEquals(isquery(getExtensionsProvider) ,true);
					assertEquals(listSort(structKeyList(getExtensionsProvider),'textnocase'),'isreadonly,url');
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

				it(title="checking verifyExtensionProvider()", body=function( currentSpec ) {
					var verifyExtensionProvider = admin.verifyExtensionProvider(url="http://extension.lucee.org");
					assertEquals(verifyExtensionProvider.label,"ok");
				});

				it(title="checking getRHExtensionProviders()", body=function( currentSpec ) {
					var getRHExtensionsProvider = admin.getRHExtensionProviders();
					assertEquals(isquery(getRHExtensionsProvider) ,true);
					assertEquals(listSort(structKeyList(getRHExtensionsProvider),'textnocase'),'readonly,url');
				});

				it(title="checking updateRHExtensionProvider()", body=function( currentSpec ) {
					admin.updateRHExtensionProvider('http://www.myhost.com');
					var getRHExtensionsProvider = admin.getRHExtensionProviders();
					assertEquals((isquery(getRHExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getRHExtensionsProvider.url)) GT 0) ,true);
				});

				it(title="checking removeRHExtensionProvider()", body=function( currentSpec ) {
					admin.removeRHExtensionProvider('http://www.myhost.com');
					var getRHExtensionsProvider = admin.getRHExtensionProviders();
					assertEquals((isquery(getRHExtensionsProvider) && FindNocase( 'http://www.myhost.com',valueList(getRHExtensionsProvider.url)) EQ 0) ,true);
				});
			});

			describe( title="test ORM function()", body=function() {
				it(title="checking getORMSetting()", body=function( currentSpec ) {
					var ORMsetting = admin.getORMSetting();
					assertEquals(isstruct(ORMsetting) ,true);
					assertEquals(listSort(structKeyList(ORMsetting),'textnocase'),'autogenmap,cacheconfig,cacheProvider,catalog,cfcLocation,dbCreate,dialect,eventHandler,eventHandling,flushAtRequestEnd,isDefaultCfclocation,logSql,namingstrategy,ormConfig,savemapping,schema,secondarycacheenabled,sqlscript,useDBForMapping');
				});

				it(title="checking updateORMSetting()", body=function( currentSpec ) {
					var updateORM = admin.updateORMSetting(schema="testSchema");
					assertEquals(isstruct(updateORM) ,true);
					assertEquals(updateORM.label,"OK");
					var ORMsetting = admin.getORMSetting();
					assertEquals(isstruct(ORMsetting) ,true);
					assertEquals(ORMsetting.schema EQ 'testSchema' ,true);
				});

				it(title="checking resetORMSetting()", body=function( currentSpec ) {
					var resetORM = admin.resetORMSetting();
					assertEquals(isstruct(resetORM) ,true);
					assertEquals(resetORM.label,"ok");
				});

				it(title="checking getORMEngine()", body=function( currentSpec ) {
					var ORMEngine = admin.getORMEngine();
					assertEquals(isstruct(ORMEngine) ,true);
					assertEquals(listSort(structKeyList(ORMEngine),'textnocase'),'bundleName,bundleVersion,class');
				});
			});

			describe( title="test Component functions", body=function() {
				it(title="checking getComponent()", body=function( currentSpec ) {
					var getComp = admin.getComponent();
					assertEquals(isstruct(getComp) ,true);
					assertEquals(listSort(structKeyList(getComp),'textnocase'),'baseComponentTemplateCFML,baseComponentTemplateLucee,componentDataMemberDefaultAccess,ComponentDefaultImport,componentDumpTemplate,componentLocalSearch,componentPathCache,deepSearch,strBaseComponentTemplateCFML,strBaseComponentTemplateLucee,strComponentDumpTemplate,triggerDataMember,useShadow');
				});

				it(title="checking updateComponent()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.triggerDataMember = true;
					var updateComponent = admin.updateComponent(argumentCollection=tmpStrt);
					assertEquals(isstruct(updateComponent) ,true);
					assertEquals(updateComponent.label EQ 'Ok' ,true);
					var getComp = admin.getComponent();
					assertEquals(isstruct(getComp) ,true);
					assertEquals(getComp.triggerDataMember EQ 'true' ,true);
				});

				it(title="checking getComponentMappings()", body=function( currentSpec ) {
					var getCompMap = admin.getComponentMappings();
					assertEquals(isQuery(getCompMap) ,true);
					assertEquals(listSort(structKeyList(getCompMap),'textnocase'),'archive,hidden,inspect,physical,physicalFirst,readonly,strarchive,strphysical,virtual');
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

				it(title="checking verifyCacheConnection()", body=function( currentSpec ) {
					var getCacheConnections = admin.getCacheConnections();
					assertEquals(isquery(getCacheConnections) ,true);
					var verifyCache = admin.verifyCacheConnection(getCacheConnections.name);
					assertEquals(isstruct(verifyCache) ,true);
					assertEquals(verifyCache.label,"ok");
				});

				it(title="checking updateCacheConnection()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.class="lucee.runtime.cache.ram.RamCache";
					tmpStrt.name="testCache";
					tmpStrt.custom={"timeToIdleSeconds":"86400","timeToLiveSeconds":"3600"};
					tmpStrt.bundleName="";
					tmpStrt.bundleVersion="";
					tmpStrt.default="query";
					admin.updateCacheConnection(argumentCollection=tmpStrt);
					var getCacheConnection = admin.getCacheConnection('testCache');
					assertEquals(isstruct(getCacheConnection) ,true);
					assertEquals(getCacheConnection.default EQ 'query' ,true);
				});

				it(title="checking getCacheDefaultConnection()", body=function( currentSpec ) {
					var defaultCacheConnection = admin.getCacheDefaultConnection('query');
					assertEquals(isstruct(defaultCacheConnection) ,true);
					assertEquals(defaultCacheConnection.name ,"testCache");
					assertEquals(listSort(structKeyList(defaultCacheConnection),'textnocase'),'bundleName,bundleVersion,class,custom,default,name,readonly');
				});

				it(title="checking removeCacheConnection()", body=function( currentSpec ){
					var removeCacheConnection = admin.removeCacheConnection('testCache');
					var getCacheConnections = admin.getCacheConnections();
					assertEquals( listFindNoCase( valueList(getCacheConnections.name), 'testCache' ), false );
				});

				it(title="checking updateCacheDefaultConnection()", body=function( currentSpec ){
					var tmpStrt = {};
					tmpStrt.class="lucee.runtime.cache.ram.RamCache";
					tmpStrt.name="testDefaultCache";
					tmpStrt.custom={"timeToIdleSeconds":"86400","timeToLiveSeconds":"3600"};
					tmpStrt.bundleName="";
					tmpStrt.bundleVersion="";
					tmpStrt.default="query";
					admin.updateCacheConnection(argumentCollection=tmpStrt);

					admin.updateCacheDefaultConnection(object="testDefaultCache");
					var defaultCacheConnection = admin.getCacheDefaultConnection('object');
					assertEquals(isstruct(defaultCacheConnection) ,true);
					assertEquals(listSort(structKeyList(defaultCacheConnection),'textnocase'),'bundleName,bundleVersion,class,custom,default,name,readonly');
				});


				it(title="checking removeCacheDefaultConnection()", body=function( currentSpec ){
					admin.removeCacheDefaultConnection();
					var defaultCacheConnection = admin.getCacheDefaultConnection('object');
					assertEquals(isstruct(defaultCacheConnection) ,true);
					assertEquals(structIsEmpty(defaultCacheConnection) ,true);
				});

			});

			describe( title="test remoteclient functions", body=function() {
				it(title="checking getRemoteClients", body=function( currentSpec ) {
					var remoteClients = admin.getRemoteClients();
					assertEquals(isquery(remoteClients) ,true);
				});

				it(title="checking getRemoteClient", body=function( currentSpec ) {
					var remoteClient = admin.getRemoteClient("#CGI.SERVER_NAME#/lucee/admin.cfc?wsdl");
					assertEquals(isstruct(remoteClient) ,true);
					assertEquals(listSort(structKeyList(remoteClient),'textnocase'),'adminPassword,label,proxyPassword,proxyPort,proxyServer,proxyUsername,securityKey,ServerPassword,ServerUsername,type,url,usage');
				});

				it(title="checking updateRemoteClient", body=function( currentSpec ) {
					var tmpStruct = {};
					tmpStruct.url = "#CGI.SERVER_NAME#/lucee/admin.cfc?wsdl";
					tmpStruct.securityKey = "Test";
					tmpStruct.serverUsername = "Test";
					tmpStruct.serverPassword = "Test";
					tmpStruct.adminPassword = "Test";
					tmpStruct.label = "TestRemoteClient";
					tmpStruct.usage = "";
					tmpStruct.proxyServer = "";
					tmpStruct.proxyUsername = "";
					tmpStruct.proxyPassword = "";
					tmpStruct.proxyPort = "";
					admin.updateRemoteClient(argumentCollection=tmpStruct);
				});

				it(title="checking removeRemoteClient", body=function( currentSpec ) {
					admin.removeRemoteClient("#CGI.SERVER_NAME#/lucee/admin.cfc?wsdl");
				});

				it(title="checking hasRemoteClientUsage()", body=function( currentSpec ) {
					var hasRemoteClientUsage = admin.hasRemoteClientUsage();
					assertEquals(isBoolean(hasRemoteClientUsage) ,true);
				});

				it(title="checking getRemoteClientUsage()", body=function( currentSpec ) {
					var getremoteclient = admin.getRemoteClientUsage();
					assertEquals(isQuery(getremoteclient) ,true);
				});

				it(title="checking verifyRemoteClient()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.label = "test";
					tmpStrt.url = "#CGI.SERVER_NAME#/lucee/admin.cfc?wsdl";
					tmpStrt.adminPassword = "Test";
					tmpStrt.securityKey = "Test";
					var verifyClient = admin.verifyRemoteClient(argumentCollection = #tmpStrt#);
					assertEquals(isstruct(verifyClient) ,true);
				});

				it(title="checking getRemoteClientTasks()", body=function( currentSpec ) {
					var remoteClientTask = admin.getRemoteClientTasks();
					assertEquals(isQuery(remoteClientTask) ,true);
				});
			});

			describe( title="test CompilerSettings functions", body=function() {
				it(title="checking getCompilerSettings()", body=function( currentSpec ) {
					var compileSettings = admin.getCompilerSettings();
					assertEquals(isstruct(compileSettings) ,true);
					assertEquals(listSort(structKeyList(compileSettings),'textnocase'), 'DotNotationUpperCase,externalizeStringGTE,handleUnquotedAttrValueAsString,nullSupport,suppressWSBeforeArg,templateCharset');
				});

				it(title="checking updateCompilerSettings()", body=function( currentSpec ) {
					var tmpStruct={};
					tmpStruct.templateCharset="windows-1252";
					tmpStruct.dotNotation="oc";
					tmpStruct.nullSupport=false;
					tmpStruct.suppressWSBeforeArg=true;
					tmpStruct.handleUnquotedAttrValueAsString=true;
					tmpStruct.externalizeStringGTE=-1;
					admin.updateCompilerSettings(argumentCollection=tmpStruct);
				});
			});

			describe( title="test performance functions", body=function() {
				it(title="checking getPerformanceSettings()", body=function( currentSpec ) {
					var performanceSettings = admin.getPerformanceSettings();
					assertEquals(isstruct(performanceSettings) ,true);
					assertEquals(listSort(structKeyList(performanceSettings),'textnocase'), 'inspectTemplate,typeChecking');
				});

				it(title="checking updatePerformanceSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					var performanceSettings = admin.getPerformanceSettings();
					assertEquals(isstruct(performanceSettings) ,true);
					tmpStrt.inspectTemplate = performanceSettings.inspectTemplate;
					tmpStrt.typeChecking = true;

					admin.updatePerformanceSettings(argumentCollection = tmpStrt);

					var updatedPerformanceSettings = admin.getPerformanceSettings();
					assertEquals(isstruct(updatedPerformanceSettings) ,true);
					assertEquals(updatedPerformanceSettings.typeChecking EQ true ,true);
				});

				it(title="checking resetPerformanceSettings()", body=function( currentSpec ) {
					admin.resetPerformanceSettings();
				});
			});

			describe( title="test gateway functions", body=function() {
				it(title="checking getGatewayentries()", body=function( currentSpec ) {
					var gatewayEntries = adminweb.getGatewayentries();
					assertEquals(isquery(gatewayEntries) ,true);
				});

				it(title="checking getGatewayentry()", body=function( currentSpec ) {
					var gatewayEntry = adminweb.getGatewayentry('testDirectorygateway');
					assertEquals(isStruct(gatewayEntry) ,true);
					assertEquals(listSort(structKeyList(gatewayEntry),'textnocase'), 'bundleName,bundleVersion,cfcPath,class,custom,id,listenerCfcPath,readOnly,startupMode,state');
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

				it(title="checking gateway()", body=function( currentSpec ) {
					adminweb.gateway( id="testDirectorygateway", gatewayAction="stop" );
					var gatewayEntry = adminweb.getGatewayentry('testdirectorygateway');
					assertEquals(gatewayEntry.state, "stopping");
				});

				it(title="checking removeGatewayEntry()", body=function( currentSpec ) {
					adminweb.removeGatewayEntry( id="testgateway" );
					var gatewayEntry = adminweb.getGatewayentries();
					assertEquals( listFindNoCase(valueList(gatewayEntry.id), "testgateway"), false );
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
				// beforeEach(function( currentSpec ){
				// 	if( currentSpec EQ "checking getDebuggingList()"){
				// 		writeDump("TEst");
				// 		if( !directoryExists(expandPath("/") & "/lucee/templates/debugging") ){
				// 			directoryCreate(expandPath("/") & "/lucee/templates/debugging");
				// 		}
				// 		var fileList="admin,web,public";
				// 		loop list="#fileList#" index="myfile"{
				// 			if( !fileExists(expandPath("/") & "/lucee/templates/debugging/debugging-#myfile#.cfm") )
				// 				fileCopy( expandPath("./LDEV1159/debugging/debugging-#myfile#.cfm"), expandPath("/") & "/lucee/templates/debugging/debugging-#myfile#.cfm" );
				// 		}
				// 	}
				// });
				it(title="checking getDebuggingList()", body=function( currentSpec ) {
					var debuggingTemplates = admin.getDebuggingList();
					assertEquals(isQuery(debuggingTemplates) ,true);
					assertEquals(listSort(structKeyList(debuggingTemplates),'textnocase'), 'name');
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

				xit(title="checking updateDebugEntry()", body=function( currentSpec ) {
					// var tmpstruct = {};
					// tmpstruct.label = "testDebug";
					// tmpstruct.type = "lucee-classic";
					// tmpstruct.ipRange = "127.0.0.1";
					// tmpstruct.custom = {};

					// adminweb.updateDebugEntry(argumentCollection = tmpstruct);
				});

				xit(title="checking removeDebugEntry()", body=function( currentSpec ) {
					// var debugEntry = admin.getDebugEntry();
					// admin.removeDebugEntry("");
				});

				it(title="checking getDebugSetting()", body=function( currentSpec ) {
					var deguggingListSetting = admin.getDebugSetting();
					assertEquals(isstruct(deguggingListSetting) ,true);
					assertEquals(listSort(structKeyList(deguggingListSetting),'textnocase'), 'maxLogs');
				});

				it(title="checking updateDebugSetting()", body=function( currentSpec ) {
					admin.updateDebugSetting( maxLogs=100 );
				});

				it(title="checking resetDebugSetting()", body=function( currentSpec ) {
					admin.resetDebugSetting();
				});

				it(title="checking getDebug()", body=function( currentSpec ) {
					var debuggingSetting = admin.getDebug();
					assertEquals(isstruct(debuggingSetting) ,true);
					assertEquals(listSort(structKeyList(debuggingSetting),'textnocase'), 'database,debug,dump,exception,implicitAccess,queryUsage,timer,tracing');
				});

				it(title="checking updateDebug()", body=function( currentSpec ) {
					admin.updateDebug( implicitAccess=true );
				});

				it(title="checking resetDebug()", body=function( currentSpec ) {
					admin.resetDebug();
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

			describe( title="test context functions", body=function() {
				it(title="checking getContextDirectory()", body=function( currentSpec ) {
					var contextDirectory = admin.getContextDirectory();
					assertEquals(isquery(contextDirectory), false);
					assertEquals(listSort(structKeyList(contextDirectory),'textnocase'), 'clientElements,clientSize,config_file,hash,hasOwnSecContext,id,label,path,sessionElements,sessionSize,url');

				});

				it(title="checking getContextes()", body=function( currentSpec ) {
					var getContext = admin.getContextes();
					assertEquals(isquery(getContext), false);
					assertEquals(listSort(structKeyList(getContext),'textnocase'), 'clientElements,clientSize,config_file,hash,hasOwnSecContext,id,label,path,sessionElements,sessionSize,url');
				});

				it(title="checking getContexts()", body=function( currentSpec ) {
					var getContexts = admin.getContexts();
					assertEquals(isquery(getContexts), false);
					assertEquals(listSort(structKeyList(getContexts),'textnocase'), 'clientElements,clientSize,config_file,hash,hasOwnSecContext,id,label,path,sessionElements,sessionSize,url');
				});
			});

			describe( title="test password function", body=function() {
				it(title="checking updatePassword()", body=function( currentSpec ) {
					admin.updatePassword(oldPassword="#request.SERVERADMINPASSWORD#", newPassword="server" );
					try{
						admin.updatePassword(oldPassword="#request.SERVERADMINPASSWORD#", newPassword="server" );
					}catch( any e ){
						assertEquals( e.message, 'No access, password is invalid' );
					}
					admin.updatePassword(oldPassword="server", newPassword="#request.SERVERADMINPASSWORD#" );
				});

				it(title="checking getDefaultPassword()", body=function( currentSpec ) {
					var defaultPassword = admin.getDefaultPassword();
					assertEquals(defaultPassword, "");
				});

				it(title="checking updateDefaultPassword()", body=function( currentSpec ) {
					admin.updateDefaultPassword(newPassword="server");
					var defaultPassword = admin.getDefaultPassword();
					assertEquals(defaultPassword, "cf2c550c667429be657529fe8c469ef7ce0814619c73459995b82657f9aa3a94");
				});

				it(title="checking removeDefaultPassword()", body=function( currentSpec ) {
					admin.removeDefaultPassword();
					var defaultPassword = admin.getDefaultPassword();
					assertEquals(defaultPassword, "");
				});
				
				it(title="checking resetPassword()", body=function( currentSpec ) {
					admin.resetPassword(contextPath="#server.coldfusion.rootdir#");
				});

				it(title="checking hashpassword()", body=function( currentSpec ) {
					var hashpassword = admin.hashpassword();
					assertEquals(hashpassword, "a4209ccad407e10eb905f8dae17f6e9329adcffe34b54256617dca81ebc395a0");
				});
			});

			describe( title="test tldx and Fldx function", body=function() {
				it(title="checking getFlds()", body=function( currentSpec ) {
					var flds = admin.getFlds();
					assertEquals(isquery(flds) ,true);
					assertEquals((flds.displayname EQ 'Lucee Core Function Library'), true);
				});

				it(title="checking getTlds()", body=function( currentSpec ) {
					var tlds = admin.getTlds();
					assertEquals(isquery(tlds) ,true);
					assertEquals((tlds.type EQ 'cfml'), true);
				});
			});

			describe( title="test Tasks functions", body=function() {
				it(title="checking getSpoolerTasks()", body=function( currentSpec ) {
					var spoolertask = admin.getSpoolerTasks();
					assertEquals(isQuery(spoolertask) ,true);
					assertEquals(listSort(structKeyList(spoolertask),'textnocase'), 'closed,detail,exceptions,id,lastExecution,name,nextExecution,tries,triesmax,type');
				});

				it(title="checking getTaskSetting()", body=function( currentSpec ) {
					var taskSetting = admin.getTaskSetting();
					assertEquals(isstruct(taskSetting) ,true);
					assertEquals(listSort(structKeyList(taskSetting),'textnocase'), 'maxThreads');
				});
			});

			describe( title="test CfxTags functions", body=function() {
				it(title="checking getCfxTags()", body=function( currentSpec ) {
					var cfxTags = admin.getCfxTags();
					assertEquals(isquery(cfxTags) ,true);
					var strctKeylist = structKeyList(cfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});

				it(title="checking verifyCFX()", body=function( currentSpec ) {
					var verifyCFX = admin.verifyCFX(name="helloworld");
					assertEquals(verifyCFX.label,"error");
				});

				it(title="checking getCPPCfxTags()", body=function( currentSpec ) {
					var CPPCfxTags = admin.getCPPCfxTags();
					assertEquals(isquery(CPPCfxTags) ,true);
					var strctKeylist = structKeyList(CPPCfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});

				it(title="checking getJavaCfxTags()", body=function( currentSpec ) {
					var javaCfxTags = admin.getJavaCfxTags();
					assertEquals(isquery(javaCfxTags) ,true);
					var strctKeylist = structKeyList(javaCfxTags);
					assertEquals(FindNocase('isvalid',strctKeylist) GT 0, true);
				});

				it(title="checking verifyJavaCFX()", body=function( currentSpec ) {
					var verifyJavaCFX = admin.verifyJavaCFX(name="helloworld", class="lucee.cfx.example.HelloWorld");
					assertEquals(verifyJavaCFX.label,"ok");
				});
			});

			describe( title="test LoginSettings functions", body=function() {
				it(title="checking getLoginSettings()", body=function( currentSpec ) {
					var loginSettings = admin.getLoginSettings();
					assertEquals(isstruct(loginSettings) ,true);
					assertEquals(listSort(structKeyList(loginSettings),'textnocase'),'captcha,delay,rememberme');
				});

				it(title="checking updateLoginSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.captcha = "false";
					var updateLogin = admin.updateLoginSettings(argumentCollection=tmpStrt);
					assertEquals(isstruct(updateLogin) ,true);
					assertEquals(updateLogin.label,"OK");
					var loginSettings = admin.getLoginSettings();
					assertEquals(isstruct(loginSettings) ,true);
					assertEquals(loginSettings.captcha EQ 'false' ,true);
				});
			});

			describe( title="test log functions", body=function() {
				it(title="checking logsetting()", body=function( currentSpec ) {
					var logsettings = admin.getLogSettings();
					assertEquals(isquery(logsettings) ,true);
					assertEquals(listSort(structKeyList(logsettings),'textnocase'), 'appenderArgs,appenderBundleName,appenderBundleVersion,appenderClass,layoutArgs,layoutBundleName,layoutBundleVersion,layoutClass,level,name,readonly');
				});

				it(title="checking getExecutionLog()", body=function( currentSpec ) {
					var execLog = admin.getExecutionLog();
					assertEquals(isstruct(execLog) ,true);
					assertEquals(listSort(structKeyList(execLog),'textnocase'), 'arguments,class,enabled');
				});

				it(title="checking updateLogSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					var logsettings = admin.getLogSettings();
					assertEquals(isquery(logsettings) ,true);
					tmpStrt.name = logsettings.name;
					tmpStrt.level = logsettings.level;
					tmpStrt.appenderClass = logsettings.appenderClass;
					tmpStrt.layoutClass = logsettings.layoutClass;
					tmpStrt.appenderArgs = {"charset":"windows-1252","maxFiles":"10","maxFileSize":"10485760","path":"{lucee-config}/logs/exception.log","timeout":"180"};
					tmpStrt.layoutArgs = logsettings.layoutArgs;
					var updateLog = admin.updateLogSettings(argumentCollection = tmpStrt);
					assertEquals(isstruct(updateLog) ,true);
					assertEquals(updateLog.label,"ok");
					logsettings = admin.getLogSettings();
					assertEquals(logsettings.appenderArgs[1].timeout EQ 180 ,true);
				});
			});

			describe( title="test application listener functions", body=function() {
				it(title="checking getApplicationListener()", body=function( currentSpec ) {
					var appListner = admin.getApplicationListener();
					assertEquals(isstruct(appListner) ,true);
					assertEquals(listSort(structKeyList(appListner),'textnocase'),'mode,type');
				});

				it(title="checking updateApplicationListener()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.listenerType = "mixed";
					tmpStrt.listenerMode = "root";
					admin.updateApplicationListener(argumentCollection = #tmpStrt#);
					var appListner = admin.getApplicationListener();
					assertEquals(isstruct(appListner) ,true);
					assertEquals(appListner.mode EQ 'root' ,true);
				});
			});

			describe( title="test proxy functions", body=function() {
				it(title="checking getproxy()", body=function( currentSpec ) {
					var proxy = admin.getproxy();
					assertEquals(isstruct(proxy) ,true);
					assertEquals(listSort(structKeyList(proxy),'textnocase'),'password,port,server,username');
				});
			});

			describe( title="test scope functions", body=function() {
				it(title="checking getscope()", body=function( currentSpec ) {
					var getScope = admin.getScope();
					assertEquals(isstruct(getScope) ,true);
					assertEquals(listSort(structKeyList(getScope),'textnocase'), 'allowImplicidQueryCall,applicationTimeout,applicationTimeout_day,applicationTimeout_hour,applicationTimeout_minute,applicationTimeout_second,cgiReadonly,clientCookies,clientManagement,clientStorage,clientTimeout,clientTimeout_day,clientTimeout_hour,clientTimeout_minute,clientTimeout_second,domainCookies,localmode,mergeFormAndUrl,scopeCascadingType,sessionManagement,sessionStorage,sessionTimeout,sessionTimeout_day,sessionTimeout_hour,sessionTimeout_minute,sessionTimeout_second,sessiontype');
				});

				it(title="checking updateScope()", body=function( currentSpec ) {
					var getScope = admin.getScope();
					assertEquals(isstruct(getScope) ,true);
					var tmpStrt = {};
					tmpStrt.scopeCascadingType = getScope.scopeCascadingType;
					tmpStrt.allowImplicidQueryCall = getScope.allowImplicidQueryCall;
					tmpStrt.mergeFormAndUrl = getScope.mergeFormAndUrl;
					tmpStrt.sessionManagement = getScope.sessionManagement;
					tmpStrt.clientManagement = getScope.clientManagement;
					tmpStrt.domainCookies = getScope.domainCookies;
					tmpStrt.clientCookies = getScope.clientCookies;
					tmpStrt.clientTimeout = createTimeSpan(100,0,0,0);
					tmpStrt.sessionTimeout = createTimeSpan(0,0,50,0);
					tmpStrt.clientStorage = getScope.clientStorage;
					tmpStrt.sessionStorage = getScope.sessionStorage;
					tmpStrt.applicationTimeout = getScope.applicationTimeout;
					tmpStrt.sessionType = getScope.sessionType;
					tmpStrt.localMode = getScope.localMode;
					tmpStrt.cgiReadonly = getScope.cgiReadonly;
					var updateScope = admin.updateScope(argumentCollection = tmpStrt);
					assertEquals(isstruct(updateScope) ,true);
					assertEquals(updateScope.label EQ 'OK' ,true);
					var getUpdatedScope = admin.getScope();
					assertEquals(isstruct(getUpdatedScope) ,true);
					assertEquals(getUpdatedScope.clientTimeout_day EQ 100 ,true);
					assertEquals(getUpdatedScope.sessionTimeout_minute EQ 50 ,true);
				});
			});

			describe( title="test restSettings functions", body=function() {
				it(title="checking getRestSettings()", body=function( currentSpec ) {
					var restSettings = admin.getRestSettings();
					assertEquals(isstruct(restSettings) ,true);
					assertEquals(listSort(structKeyList(restSettings),'textnocase'),'list');
				});

				it(title="checking updateRestSettings()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.list = "true";
					var updateRest = admin.updateRestSettings(argumentCollection=tmpStrt);
					assertEquals(isstruct(updateRest) ,true);
					assertEquals(updateRest.label,"OK");
					var restSettings = admin.getRestSettings();
					assertEquals(isstruct(restSettings) ,true);
					assertEquals(restSettings.list EQ 'true' ,true);
				});
			});

			describe( title="test restMappings functions", body=function() {
				it(title="checking getRestMappings()", body=function( currentSpec ) {
					var restMappings = admin.getRestMappings();
					assertEquals(isQuery(restMappings) ,true);
					assertEquals(listSort(structKeyList(restMappings),'textnocase'),'default,hidden,physical,readonly,strphysical,virtual');
				});

				it(title="checking updateRestMapping()", body=function( currentSpec ) {
					var tmpStrt = {};
					tmpStrt.default = "true";
					tmpStrt.virtual = "/testRestMapping";
					tmpStrt.physical = "#expandPath('./testRestMapping')#";
					var updateRestMap = admin.updateRestMapping(argumentCollection=tmpStrt);
					assertEquals(isstruct(updateRestMap) ,true);
					assertEquals(updateRestMap.label,"OK");
					var restMappings = admin.getRestMappings();
					assertEquals(isQuery(restMappings) ,true);
					assertEquals(restMappings.default[1] EQ 'true' ,true);
				});

				it(title="checking removeRestMapping()", body=function( currentSpec ) {
					var removeRestMap = admin.removeRestMapping("testRestMapping");
					assertEquals(isstruct(removeRestMap) ,true);
					assertEquals(removeRestMap.label,"OK");
					var restMappingsAfterRemove = admin.getRestMappings();
					assertEquals((isquery(restMappingsAfterRemove) && findNocase( "testRestMapping", valueList(restMappingsAfterRemove.virtual)) EQ 0) ,true);
				});
			});

			describe( title="test application functions", body=function() {
				it(title="checking getApplicationSetting()", body=function( currentSpec ) {
					var appSetting = admin.getApplicationSetting();
					assertEquals(isstruct(appSetting) ,true);
					assertEquals(listSort(structKeyList(appSetting),'textnocase'),'AllowURLRequestTimeout,requestTimeout,requestTimeout_day,requestTimeout_hour,requestTimeout_minute,requestTimeout_second,scriptProtect');
				});

				it(title="checking updateApplicationSetting()", body=function( currentSpec ) {
					var appSetting = admin.getApplicationSetting();
					assertEquals(isstruct(appSetting) ,true);
					var tmpStrt = {};
					tmpStrt.requestTimeout = createTimeSpan(0,2,0,0);
					tmpStrt.scriptProtect = appSetting.scriptProtect;
					tmpStrt.allowURLRequestTimeout = appSetting.allowURLRequestTimeout;
					var updateApplication = admin.updateApplicationSetting(argumentCollection = #tmpStrt#);
					assertEquals(isstruct(updateApplication) ,true);
					assertEquals(updateApplication.label EQ 'OK' ,true);
					var updatedAppSetting = admin.getApplicationSetting();
					assertEquals(isstruct(updatedAppSetting) ,true);
					assertEquals(updatedAppSetting.requestTimeout_hour EQ 2 ,true);
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

			describe( title="test info functions", body=function() {
				it(title="checking getinfo()", body=function( currentSpec ) {
					var info = admin.getinfo();
					assertEquals(isstruct(info) ,true);
					assertEquals(listSort(structKeyList(info),'textnocase'),'config,configServerDir,configWebDir,javaAgentSupported,servlets');
				});
			});

			describe( title="test surveillance functions", body=function() {
				it(title="checking getSurveillance()", body=function( currentSpec ) {
					var surveillance = admin.getSurveillance();
					assertEquals(isstruct(surveillance) ,true);
				});
			});

			describe( title="test CustomTag functions", body=function() {
				it(title="checking getCustomTagSetting()", body=function( currentSpec ) {
					var customTagSetting = admin.getCustomTagSetting();
					assertEquals(isstruct(customTagSetting) ,true);
					assertEquals(listSort(structKeyList(customTagSetting),'textnocase'), 'customTagDeepSearch,customTagLocalSearch,customTagPathCache,deepSearch,extensions,localSearch');
				});
			});

			describe( title="test thread functions", body=function() {
				it(title="checking getRunningThreads()", body=function( currentSpec ) {
					var runningThrd = admin.getRunningThreads();
					assertEquals(isquery(runningThrd) ,true);
				});
			});

			describe( title="test error functions", body=function() {
				it(title="checking getError()", body=function( currentSpec ) {
					var error = admin.getError();
					assertEquals(isStruct(error) ,true);
					var strctKeylist = structKeyList(error);
					assertEquals(FindNocase('doStatusCode',strctKeylist) GT 0, true);
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