<cfcomponent>
<cfscript>
	/**
	** @hint constructor of the component
	* @type type contex type, valid values are "server" or "web"
	* @password password for this context
	*/
	function init(required string type,required string password, string remoteClients){
		variables.type=arguments.type;
		variables.password=arguments.password;
		variables.remoteClients=!isNull(arguments.remoteClients)?arguments.remoteClients:"";
	}

	/**
	* @hint returns reginal information about this context, this includes the locale, the timezone,a timeserver address and if the timeserver is used or not
	*/
	public struct function getRegional(){
		admin
			action="getRegional"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the regional settings of this context
	* @timezone timezone used for this context, this can be for example "gmt+1" or "Europe/Zurich", use the function "getAvailableTimeZones" to get a list of available timezones
	* @locale the locale used for this context, this can be for example "de_ch", use the function getAvailableLocales() to get a list of all possible locales.
	* @timeserver timeserver used for this context, this can be for example "swisstime.ethz.ch"
	* @usetimeserver defines if the timeserver is used or not
	*/
	public void function updateRegional(string timezone, string locale,string timeserver,boolean usetimeserver){
		var regional="";

		// check timezone
		if(isNull(arguments.timezone) || isEmpty(arguments.timezone)) {
			regional=getRegional();
			arguments.timezone=regional.timezone;
		}

		// check locale
		if(isNull(arguments.locale) || isEmpty(arguments.locale)) {
			if(isSimpleValue(regional))regional=getRegional();
			arguments.locale=regional.locale;
		}

		// check timeserver
		if(isNull(arguments.timeserver) || isEmpty(arguments.timeserver)) {
			if(isSimpleValue(regional))regional=getRegional();
			arguments.timeserver=regional.timeserver;
		}

		// check usetimeserver
		if(isNull(arguments.usetimeserver)) {
			if(isSimpleValue(regional))regional=getRegional();
			arguments.usetimeserver=regional.usetimeserver;
		}

		admin
			action="updateRegional"
			type="#variables.type#"
			password="#variables.password#"

			timezone="#arguments.timezone#"
			locale="#arguments.locale#"
			timeserver="#arguments.timeserver#"
			usetimeserver="#arguments.usetimeserver#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint remove web specific regional settings and set back to server context settings, this function only works with type "web" and is ignored with type "server"
	*/
	public void function resetRegional(){
		if(variables.type != "web") return;

		admin
			action="updateRegional"
			type="#variables.type#"
			password="#variables.password#"
			timezone=""
			locale=""
			timeserver=""
			usetimeserver=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns charset information about this context
	*/
	public struct function getCharset(){
		admin
			action="getCharset"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the charset settings of this context
	* @resourceCharset default charset used for read/write resources (cffile,wilewrite ...)
	* @templateCharset default charset used for read CFML Templates (cfm,cfc)
	* @webCharset default charset used for the response stream and for reading data from request
	*/
	public void function updateCharset(string resourceCharset, string templateCharset,string webCharset){
		var charset="";

		// check resourceCharset
		if(isNull(arguments.resourceCharset) || isEmpty(arguments.resourceCharset)) {
			charset=getCharset();
			arguments.resourceCharset=charset.resourceCharset;
		}

		// check templateCharset
		if(isNull(arguments.templateCharset) || isEmpty(arguments.templateCharset)) {
			if(isSimpleValue(charset))charset=getCharset();
			arguments.templateCharset=charset.templateCharset;
		}

		// check webCharset
		if(isNull(arguments.webCharset) || isEmpty(arguments.webCharset)) {
			if(isSimpleValue(charset))charset=getCharset();
			arguments.webCharset=charset.webCharset;
		}

		admin
			action="updateCharset"
			type="#variables.type#"
			password="#variables.password#"

			templateCharset="#arguments.templateCharset#"
			webCharset="#arguments.webCharset#"
			resourceCharset="#arguments.resourceCharset#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint remove web specific charset settings and set back to server context settings, this function only works with type "web" and is ignored with type "server"
	*/
	public void function resetCharset(){
		if(variables.type != "web") return;

		admin
			action="updateCharset"
			type="#variables.type#"
			password="#variables.password#"
			templateCharset=""
			webCharset=""
			resourceCharset=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns output settings for this context
	*/
	public struct function getOutputSetting(){
		admin
			action="getOutputSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates output settings for this context
	* @cfmlWriter an argument for Whitespace management in lucee Output settings
	* @suppressContent an argument for suppressContent in lucee Output settings
	* @allowCompression an argument for allowCompression in lucee Output settings
	* @bufferOutput an argument for bufferOutput in lucee Output settings
	*/
	public void function updateOutputSetting( required string cfmlWriter, boolean suppressContent, boolean allowCompression, boolean bufferOutput ){
		admin
			action="updateOutputSetting"
			type="#variables.type#"
			password="#variables.password#"

			cfmlWriter="#arguments.cfmlWriter#"
			suppressContent="#isDefined('arguments.suppressContent') and arguments.suppressContent#"
			allowCompression="#isDefined('arguments.allowCompression') and arguments.allowCompression#"
			bufferOutput="#isDefined('arguments.bufferOutput') and arguments.bufferOutput#"
			contentLength=""

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets output settings for this context
	*/
	public void function resetOutputSetting() {
		admin
			action="updateOutputSetting"
			type="#variables.type#"
			password="#variables.password#"

			cfmlWriter=""
			suppressContent=""
			showVersion=""
			allowCompression=""
			bufferOutput=""
			contentLength=""

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns all available timezones
	*/
	public query function getAvailableTimeZones(){
		admin
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn"
			action="getTimeZones";
			querySort(rtn,"id,display");
			return rtn;
	}

	/**
	* @hint returns all available locales
	*/
	public struct function getAvailableLocales(){
		admin
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn"
			action="getLocales";
			return rtn;
	}


	/**
	* @hint returns the list of available JARS
	*/
	public query function getJars(){
		admin
			action="getJars"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint updates or inserts of not existing a jar to the Lucee handled lib folder
	* @path path (including file name) to the jar file, this can be any virtual file systm supported (local filesystem, zip, ftp, s3, ram ...)
	*/
	public void function updateJar(required string path){
		admin
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn"

			action="updateJar"
			jar="#arguments.path#";
	}

	/**
	* @hint removes a existing jar from the Lucee handled lib folder, if the jar does not exists, the call is simply ignored 
	* @name name of the jar (no path) of the jar file to remove
	*/
	public void function removeJar(required string name){
		admin
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn"

			action="removeJar"
			name="#arguments.name#";
	}

	/**
	* @hint returns the Preserve single quotes setting from datasource page
	*/
	public struct function getDatasourceSetting() {
		admin
			action="getDatasourceSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.dbSetting";

		return local.dbSetting;
	}

	/**
	* @hint update general datasource settings
	* @psq if set to true, lucee preserves all single quotes within a query tag and escapes them
	*/
	public void function updateDatasourceSetting( required boolean psq ){
		admin
			action="updatePSQ"
			type="#variables.type#"
			password="#variables.password#"

			psq="#arguments.psq#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the Preserve single quotes setting from datasource page
	*/
	public void function resetDatasourceSetting(){
		admin
			action="updatePSQ"
			type="#variables.type#"
			password="#variables.password#"

			psq=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the all the datasources defined for this context
	*/
	public query function getDatasources(){
		admin
			action="getDatasources"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.datasources";
		return local.datasources;
	}

	/**
	* @hint returns the datasource information
	* @name Specifies the name of the datasource
	*/
	public struct function getDatasource( required string name ){
		admin
			action="getDatasource"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint updates a specific datasource defined for this context
	* @name name of the datasouce to be updated
	* @type type of the datasource to be updated
	* @newName target name to be replaced with current datasource name
	* @host Host name where the database server is located
	* @database Name of the database to connect
	* @port The port to connect the database
	* @timezone timezone of the database server
	* @username The username for the database
	* @password The password for the database
	* @ConnectionLimit Restricts the maximum number of simultaneous connections at one time
	* @ConnectionTimeout To define a time in minutes for how long a connection is kept alive before it will be closed
	* @metaCacheTimeout To define how long Stored Procedures Meta Data are stored in cache
	* @blob Enable binary large object retrieval (BLOB)
	* @clob Enable long text retrieval (CLOB)
	* @validate Validate the connection before use (only works with JDBC 4.0 Drivers)
	* @allowedSelect allow database permission for select
	* @allowedInsert allow database permission for insert
	* @allowedUpdate allow database permission for update
	* @allowedDelete allow database permission for delete
	* @allowedAlter allow database permission for alter
	* @allowedDrop allow database permission for drop
	* @allowedRevoke allow database permission for revoke
	* @allowedCreate allow database permission for create
	* @allowedGrant allow database permission for grant
	* @storage Allow to use this datasource as client/session storage.
	* @customUseUnicode Should the driver use Unicode character encodings when handling strings?
	* @customCharacterEncoding Should only be used when the driver can't determine the character set mapping, or you are trying to 'force' the driver to use a character set that MySQL either doesn't natively support (such as UTF-8)If it is set to true, what character encoding should the driver use when dealing with strings?
	* @customUseOldAliasMetadataBehavior Should the driver use the legacy behavior for "AS" clauses on columns and tables, and only return aliases (if any) rather than the original column/table name? In 5.0.x, the default value was true.
	* @customAllowMultiQueries Allow the use of ";" to delimit multiple queries during one statement
	* @customZeroDateTimeBehavior What should happen when the driver encounters DATETIME values that are composed entirely of zeroes (used by MySQL to represent invalid dates)? Valid values are "exception", "round" and "convertToNull"
	* @customAutoReconnect Should the driver try to re-establish stale and/or dead connections?
	* @customJdbcCompliantTruncation If set to false then values for table fields are automatically truncated so that they fit into the field.
	* @customTinyInt1isBit if set to "true" (default) tinyInt(1) is converted to a bit value otherwise as integer.
	* @customUseLegacyDatetimeCode Use code for DATE/TIME/DATETIME/TIMESTAMP handling in result sets and statements
	* @verify whether connection needs to be verified
	*/
	public void function updateDatasource(
		required string name,
		required string type,
		required string newName,
		required string host,
		required string database,
		required numeric port,
		required string username,
		required string password,

		string timezone="",
		numeric ConnectionLimit=-1,
		numeric ConnectionTimeout=0,
		numeric metaCacheTimeout=60000,

		boolean blob=false,
		boolean clob=false,
		boolean validate=false,
		boolean storage=false,
		boolean verify=false,

		boolean allowedSelect=false,
		boolean allowedInsert=false,
		boolean allowedUpdate=false,
		boolean allowedDelete=false,
		boolean allowedAlter=false,
		boolean allowedDrop=false,
		boolean allowedRevoke=false,
		boolean allowedCreate=false,
		boolean allowedGrant=false,

		boolean customUseUnicode=false,
		string customCharacterEncoding=false,
		boolean customUseOldAliasMetadataBehavior=false,
		boolean customAllowMultiQueries=false,
		string customZeroDateTimeBehavior=false,
		boolean customAutoReconnect=false,
		boolean customJdbcCompliantTruncation=false,
		boolean customTinyInt1isBit=false,
		boolean customUseLegacyDatetimeCode=false
	){

		var driverNames=structnew("linked");
		driverNames=ComponentListPackageAsStruct("lucee-server.admin.dbdriver",driverNames);
		driverNames=ComponentListPackageAsStruct("lucee.admin.dbdriver",driverNames);
		driverNames=ComponentListPackageAsStruct("dbdriver",driverNames);

		var driver=createObject("component", drivernames[ arguments.type ]);
		var custom=structNew();
		loop collection="#arguments#" item="key"{
			if(findNoCase("custom",key) EQ 1){
				l=len(key);
				custom[mid(key,7,l-7+1)]=arguments[key];
			}
		}

		admin
			action="updateDatasource"
			type="#variables.type#"
			password="#variables.password#"

			classname="#driver.getClass()#"
			dsn="#driver.getDSN()#"
			customParameterSyntax="#isNull(driver.customParameterSyntax)?nullValue():driver.customParameterSyntax()#"
			literalTimestampWithTSOffset="#isNull(driver.literalTimestampWithTSOffset)?false:driver.literalTimestampWithTSOffset()#"
			alwaysSetTimeout="#isNull(driver.alwaysSetTimeout)?false:driver.alwaysSetTimeout()#"

			name="#arguments.name#"
			newName="#arguments.newName#"

			host="#arguments.host#"
			database="#arguments.database#"
			port="#arguments.port#"
			timezone="#arguments.timezone#"
			dbusername="#arguments.username#"
			dbpassword="#arguments.password#"

			connectionLimit="#arguments.connectionLimit#"
			connectionTimeout="#arguments.connectionTimeout#"
			metaCacheTimeout="#arguments.metaCacheTimeout#"
			blob="#getArguments('blob',false)#"
			clob="#getArguments('clob',false)#"
			validate="#getArguments('validate',false)#"
			storage="#getArguments('storage',false)#"

			allowed_select="#getArguments('allowedSelect',false)#"
			allowed_insert="#getArguments('allowedInsert',false)#"
			allowed_update="#getArguments('allowedUpdate',false)#"
			allowed_delete="#getArguments('allowedDelete',false)#"
			allowed_alter="#getArguments('allowedAlter',false)#"
			allowed_drop="#getArguments('allowedDrop',false)#"
			allowed_revoke="#getArguments('allowedRevoke',false)#"
			allowed_create="#getArguments('allowedCreate',false)#"
			allowed_grant="#getArguments('allowedGrant',false)#"
			verify="#arguments.verify#"
			custom="#custom#"
			dbdriver="#arguments.type#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint removes a specific datasource defined for this context
	* @dsn name of the datasource to be removed from this context
	*/
	public void function removeDatasource( required string dsn ){
		admin
			action="removeDatasource"
			type="#variables.type#"
			password="#variables.password#"

			name="#arguments.dsn#"
			remoteClients="#variables.remoteClients#";
	}

	//verifyDatasource
	public struct function verifyDatasource( required string name, required string dbusername, required string dbpassword ){
		var tmpStruct = {};
		try{
			admin
				action="verifyDatasource"
				type="#variables.type#"
				password="#variables.password#"
				name="#arguments.name#"
				dbusername="#arguments.dbusername#"
				dbpassword="#arguments.dbpassword#";
				tmpStruct.label = "Ok";
		} catch ( any e ){
			tmpStruct.label = "Error";
			tmpStruct.message = e.message;
		}
		return tmpStruct;
	}

	/**
	* @hint returns the list of datasource driver details
	*/
	public query function getDatasourceDriverList(){
		admin
			action="getDatasourceDriverList"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns a list mail servers defined for this context
	*/
	public query function getMailservers(){
		admin
			action="getMailservers"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.mailservers";
		return local.mailservers;
	}

	/**
	* @hint updates a specific mail server defined for this context
	* @host Mail server host name (for example smtp.gmail.com).
	* @port Port of the mail server (for example 587).
	* @username Username of the mail server.
	* @password Password of the mail server.
	* @tls Enable Transport Layer Security.
	* @ssl Enable secure connections via SSL.
	* @life Overall timeout for the connections established to the mail server.
	* @idle Idle timeout for the connections established to the mail server.
	*/
	public void function updateMailServer( required string host, required string port, required string username, required string password, required boolean tls, required boolean ssl, required string life, required string idle ){
		admin
			action="updateMailServer"
			type="#variables.type#"
			password="#variables.password#"

			hostname="#arguments.host#"
			dbusername="#arguments.username#"
			dbpassword="#arguments.password#"
			life="#arguments.life#"
			idle="#arguments.idle#"

			port="#arguments.port#"
			id="new"
			tls="#arguments.tls#"
			ssl="#arguments.ssl#"
			remoteClients="#variables.remoteClients#";
	}

	//verifyMailServer
	public struct function verifyMailServer( required string hostname, required string port, required string mailusername, required string mailpassword ){
		local.stVeritfyMessages={};
		try{
			admin
				action="verifyMailServer"
				type="#variables.type#"
				password="#variables.password#"
				hostname="#arguments.hostname#"
				port="#arguments.port#"
				mailusername="#arguments.mailusername#"
				mailpassword="#arguments.mailpassword#";
				stVeritfyMessages.label="ok"
		}catch( any e ){
			stVeritfyMessages.label="error";
			stVeritfyMessages.catch=e.message;
		}
		return stVeritfyMessages;
	}


	/**
	* @hint removes a specific mailserver defined for this context.
	* @host hostname for the mail server to be removed.
	* @username username of the mail server to be removed.
	*/
	public void function removeMailServer( required string host, required string username ){
		admin
			action="removeMailServer"
			type="#variables.type#"
			password="#variables.password#"

			hostname="#arguments.host#"
			username="#arguments.username#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns mail settings for this context.
	*/
	public struct function getMailSetting(){
		admin
			action="getMailSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.mail";
		return local.mail;
	}

	/**
	* @hint updates the mail settings for this context
	* @spoolenable If enabled the mails are sent in a background thread and the main request does not have to wait until the mails are sent.
	* @timeout Time in seconds that the Task Manager waits to send a single mail, when the time is reached the Task Manager stops the thread and the mail gets moved to unsent folder, where the Task Manager will pick it up later to try to send it again.
	* @defaultEncoding Default encoding used for mail servers
	*/
	public void function updateMailSetting( required boolean spoolEnable, required numeric timeOut, required string defaultEncoding ){
		admin
			action="updateMailSetting"
			type="#variables.type#"
			password="#variables.password#"

			spoolEnable="#arguments.spoolEnable#"
			timeout="#arguments.timeOut#"
			defaultEncoding="#arguments.defaultEncoding#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the mail settings for this context
	*/
	public void function resetMailSetting(){
		admin
			action="updateMailSetting"
			type="#variables.type#"
			password="#variables.password#"

			spoolEnable=""
			timeout=""
			defaultEncoding=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the list of mappings defined for this context
	*/
	public query function getMappings(){
		admin
			action="getMappings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.mappings";
		return local.mappings;
	}

	public struct function getMapping( required string virtual ){
		admin
			action="getMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual = "#arguments.virtual#"
			returnVariable="local.providers";
		return providers;
	}

	/**
	* @hint updates/inserts a specific mapping for this context
	* @virtual virtual name for the mapping
	* @physical physical path for the mapping
	* @archive archive path for the mapping, if needed.
	* @primary type of mapping ( physical/archive )
	* @inspect type of inspection for the mapping(never/once/always/"").
	*/
	public void function updateMapping(required string virtual, string physical="", string archive="", string primary="", string inspect="") {
		admin
			action="updateMapping"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			archive="#arguments.archive#"
			primary="#arguments.primary#"
			inspect="#arguments.inspect#"
			toplevel="yes"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint removes a mapping defined in this context
	* @virtual virtual name for the mapping to be removed.
	*/
	public void function removeMapping(required string virtual){
		admin
			action="removeMapping"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint compiles the mapping for any errors
	* @virtual virtual name for the mapping to be compiled.
	*/
	public void function compileMapping(required string virtual, boolean stopOnError=true){
		admin
			action="compileMapping"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
			stoponerror="#stopOnError#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint creates new archieve for the mapping
	* @virtual virtual name for the mapping.
	* @addCFMLFile Add all CFML Source Templates as well (.cfm,.cfc,.cfml).
	* @addNonCFMLFile Add all Non CFML Source Templates as well (.js,.css,.gif,.png ...)
	* @doDownload Whether need to download the archive or not.
	*/
	public void function createArchiveFromMapping(required string virtual, boolean addCFMLFile=true, boolean addNonCFMLFile=true, required string target){
		var ext="lar";
		var filename=arguments.virtual;
		filename=mid(filename,2,len(filename));
		if(len(filename)){
			filename="archive-"&filename&"."&ext;
		}else{
			filename="archive-root."&ext;
		}
		filename=Replace(filename,"/","-","all");
		var target=expandPath("#cgi.context_path#/lucee/archives/"&filename);
		count=0;
		while(fileExists(target)){
			count=count+1;
			target="#cgi.context_path#/lucee/archives/"&filename;
			target=replace(target,'.'&ext,count&'.'&ext);
			target=expandPath(target);
		}
		admin
			action="createArchive"
			type="#variables.type#"
			password="#variables.password#"

			file="#arguments.target#"
			virtual="#arguments.virtual#"
			addCFMLFiles="#arguments.addCFMLFile#"
			addNonCFMLFiles="#arguments.addNonCFMLFile#"
			append="true"
			remoteClients="#variables.remoteClients#";

		// downloadFile(arguments.target);
	}

	//getCustomTagMappings
	public query function getCustomTagMappings(){
		admin
			action="getCustomTagMappings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return providers;
	}

	/**
	* @hint returns the list of extensions for this context.
	*/
	public query function getExtensions(){
		admin
			action="getRHExtensions"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.extensions";
		return local.extensions;
	}

	/**
	* @hint returns the extension Info
	*/
	public struct function getExtensionInfo(){
		admin
			action="getExtensionInfo"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.info";
		return info;
	}

	/**
	* @hint updates(install/upgrade/downgrade) a specific extension.
	* @provider provider of the extension
	* @id id of the extension
	* @version version of the extension
	*/
	public void function updateExtension( required string provider, required string id , required string version ){
		admin
			action="updateRHExtension"
			type="#variables.type#"
			password="#variables.password#"
			source="#downloadFull(arguments.provider,arguments.id,arguments.version)#";
	}

	/**
	* @hint removes(uninstall) a specific extension.
	* @id id of the extension to be removed
	*/
	public void function removeExtension( required string id  ){
		admin
			action="removeRHExtension"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#";
	}

	//getRHServerExtensions
	public query function getRHServerExtensions(){
		admin
			action="getRHServerExtensions"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	//getLocalExtension
	public struct function getLocalExtension( required string id  ){
		admin
			action="getLocalExtension"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			returnVariable="local.rtn";
		return rtn;
	}

	//getLocalExtensions
	public query function getLocalExtensions(){
		admin
			action="getLocalExtensions"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of extension providers for this context.
	*/
	public query function getExtensionProviders(){
		admin
			action="getRHExtensionProviders"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return local.providers;
	}

	/**
	* @hint Adds a new extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function updateExtensionProvider( required string url ){
		admin
			action="updateRHExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
	}

	/**
	* @hint Adds a new extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function removeExtensionProvider( required string url ){
		admin
			action="removeRHExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
	}

	//verifyExtensionProvider
	public struct function verifyExtensionProvider( required string url ){
		local.verifyExtensionProvider={};
		try{
			admin
				action="verifyExtensionProvider"
				type="#variables.type#"
				password="#variables.password#"
				url="#arguments.url#";
			verifyExtensionProvider.label="ok";
		}catch( any e ){
			verifyExtensionProvider.label="error";
			verifyExtensionProvider.catch=e.message;
		}
		return verifyExtensionProvider;
	}

	/**
	* @hint returns the ORM settings
	*/
	public struct function getORMSetting(){
		admin
			action="getORMSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint updates ORM settings
	* @autoGenMap Specifies whether Lucee should automatically generate mapping for the persistent CFCs.
	* @eventHandling Specifies whether ORM Event callbacks should be given.
	* @flushAtRequestEnd Specifies whether ormflush should be called automatically at request end.
	* @logSQL Specifies whether the SQL queries that are executed by ORM will be logged.
	* @saveMapping Specifies whether the generated Hibernate mapping file has to be saved to file system.
	* @useDBForMapping Specifies whether the database has to be inspected to identify the missing information required to generate the Hibernate mapping.
	* @catalog Specifies the default Catalog that should be used by ORM.
	* @cfcLocation Specifies the directory that should be used to search for persistent CFCs to generate the mapping.
	* @dbCreate Specifies whether Lucee should automatically generate mapping for the persistent CFCs, possible values are [none,update,dropcreate]
	* @schema Specifies the default Schema that should be used by ORM.
	*/
	public struct function updateORMSetting( boolean autoGenMap=false, boolean eventHandling=false, boolean flushAtRequestEnd=false, boolean logSQL=false, boolean saveMapping=false, boolean useDBForMapping=false, string catalog="", string cfcLocation="", string dbCreate="none", string schema="" ){
		var res = {};
		try{
			var settings = getORMSetting();
			admin
				action="updateORMSetting"
				type="#variables.type#"
				password="#variables.password#"

				autogenmap="#arguments.autoGenMap#"
				eventHandling="#arguments.eventHandling#"
				flushatrequestend="#arguments.flushAtRequestEnd#"
				logSQL="#arguments.logSQL#"
				savemapping="#arguments.saveMapping#"
				useDBForMapping="#arguments.useDBForMapping#"

				catalog="#arguments.catalog#"
				cfclocation="#arguments.cfcLocation#"
				dbcreate="#arguments.dbCreate#"
				schema="#arguments.schema#"

				sqlscript="#settings.sqlScript#"
				cacheconfig="#settings.cacheConfig#"
				cacheProvider="#settings.cacheProvider#"
				ormConfig="#settings.ormConfig#"
				secondarycacheenabled="#settings.secondaryCacheEnabled#"

				remoteClients="#variables.remoteClients#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}
		return res;
	}

	/**
	* @hint resets the ORM settings
	*/
	public struct function resetORMSetting(){
		var res = {};
		try{
			admin
				action="resetORMSetting"
				type="#variables.type#"
				password="#variables.password#"
				remoteClients="#variables.remoteClients#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}
		return res;
	}

	/**
	* @hint returns the ORM engine details
	*/
	public struct function getORMEngine(){
		admin
			action="getORMEngine"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of component information
	*/
	public Struct function getComponent(){
		admin
			action="getComponent"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates component mapping settings
	* @baseComponentTemplateCFML Every component(CFC) that does not explicitly extend another component (attribute "extends") will by default extend this component.
	* @baseComponentTemplateLucee Every component(lucee) that does not explicitly extend another component (attribute "extends") will by default extend this component.
	* @componentDumpTemplate If you call a component directly, this template will be invoked to dump the component.
	* @componentDataMemberDefaultAccess Define the accessor for the data-members of a component. This defines how variables of the "this" scope of a component can be accessed from outside of the component., values available for this argument are [private,public,package,remote]
	* @triggerDataMember If there is no accessible data member (property, element of the this scope) inside a component, Lucee searches for available matching "getters" or "setters" for the requested property.
	* @useShadow Defines whether a component has an independent variables scope parallel to the "this" scope (CFML standard) or not.
	* @componentDefaultImport this package definition is imported into every template.
	* @componentLocalSearch Search relative to the caller directory for the component
	* @componentPathCache component path is cached and not resolved again
	* @componentDeepSearchDesc Search for CFCs in the subdirectories of the "Additional Resources" below.
	*/
	public struct function updateComponent(string baseComponentTemplateCFML="", string baseComponentTemplateLucee="", string componentDumpTemplate="", string componentDataMemberDefaultAccess="public", boolean triggerDataMember=false, boolean useShadow=true, string componentDefaultImport="org.lucee.cfml.*", boolean componentLocalSearch=false, boolean componentPathCache=false, boolean componentDeepSearchDesc=false){
		var res = {};
		try{
			admin
				action="updateComponent"
				type="#variables.type#"
				password="#variables.password#"

				baseComponentTemplateCFML="#arguments.baseComponentTemplateCFML#"
				baseComponentTemplateLucee="#arguments.baseComponentTemplateLucee#"
				componentDumpTemplate="#arguments.componentDumpTemplate#"
				componentDataMemberDefaultAccess="#arguments.componentDataMemberDefaultAccess#"
				triggerDataMember="#arguments.triggerDataMember#"
				useShadow="#arguments.useShadow#"
				componentDefaultImport="#arguments.componentDefaultImport#"
				componentLocalSearch="#arguments.componentLocalSearch#"
				componentPathCache="#arguments.componentPathCache#"
				deepSearch="#arguments.componentDeepSearchDesc#"

				remoteClients="#variables.remoteClients#";

			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the list of component mappings
	*/
	public query function getComponentMappings(){
		admin
			action="getComponentMappings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.mappings";
		return local.mappings;
	}

	/**
	* @hint returns the list of cache connections
	*/
	public query function getCacheConnections(){
		admin
			action="getCacheConnections"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of cache connections
	* @name specifies the name of the cache
	*/
	public struct function getCacheConnection( required string name ){
		admin
			action="getCacheConnection"
			type="#variables.type#"
			password="#variables.password#"
			name = "#arguments.name#"
			returnVariable="local.rtn";
			return rtn;
	}

	//verifyCacheConnection
	public struct function verifyCacheConnection( required string name ){
		var tmpStruct = {};
		try{
			admin
				action="verifyCacheConnection"
				type="#variables.type#"
				password="#variables.password#"
				name="#arguments.name#";
				tmpStruct.label = "Ok";
		} catch ( any e ){
			tmpStruct.label = "Error";
			tmpStruct.message = e.message;
		}
		return tmpStruct;
	}

	/**
	* @hint updates a cache connection
	* @class class name for the cache connection
	* @name name for the cache connection
	* @custom custom fields for the cache connection
	* @bundleName bundle name for the cache connection
	* @bundleVersion bundle version for the cache connection
	* @default for which type, this cache is set to default
	* @readonly whether this cache connection is readonly.
	* @storage whether allow to use this cache as client/session storage.
	*/
	public void function updateCacheConnection(
		required string class,
		required string name,
		struct custom={},
		string bundleName= "",
		string bundleVersion="",
		string default="",
		boolean readonly=false,
		boolean storage=false
	){
		admin
			action="updateCacheConnection"
			type="#variables.type#"
			password="#variables.password#"

			class="#arguments.class#"
			name="#arguments.name#"
			custom="#arguments.custom#"
			bundleName="#arguments.bundleName#"
			bundleVersion="#arguments.bundleVersion#"
			default="#arguments.default#"
			readonly="#getArguments('readonly', false)#"
			storage="#getArguments('storage', false)#"

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the details of default cache connections
	* @cachetype specifies the type of the cache
	*/
	public struct function getCacheDefaultConnection( required string cachetype ){
		admin
			action="getCacheDefaultConnection"
			type="#variables.type#"
			cachetype="#arguments.cacheType#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the default cache connection for various lucee elements
	* @object sets the default cache connection for object
	* @template sets the default cache connection for template
	* @query sets the default cache connection for query
	* @resource sets the default cache connection for resource
	* @function sets the default cache connection for function
	* @include sets the default cache connection for include
	* @http sets the default cache connection for http
	* @file sets the default cache connection for file
	* @webservice sets the default cache connection for webservice
	*/
	public void function updateCacheDefaultConnection(
		string object = "",
		string template = "",
		string query = "",
		string resource = "",
		string function = "",
		string include = "",
		string http = "",
		string file = "",
		string webservice = ""
	){
		var res = {};
		try{
			admin
				action="updateCacheDefaultConnection"
				type="#variables.type#"
				password="#variables.password#"
				object="#arguments.object#"
				template="#arguments.template#"
				query="#arguments.query#"
				resource="#arguments.resource#"
				function="#arguments['function']#"
				include="#arguments.include#"
				http="#arguments.http#"
				file="#arguments.file#"
				webservice="#arguments.webservice#"
				remoteClients="#variables.remoteClients#";

			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	//updateCachedWithin
	public void function updateCachedWithin( required string cachedWithinType, required string cachedWithin ){
		admin
			action="updateCachedWithin"
			type="#variables.type#"
			password="#variables.password#"
			cachedWithinType="#arguments.cachedWithinType#"
			cachedWithin="#arguments.cachedWithin#";
	}

	/**
	* @hint returns the list of remote clients
	*/
	public query function getRemoteClients(){
		admin
			action="getRemoteClients"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of a remote client
	* @url specifies the URL of the remote client
	*/
	public struct function getRemoteClient( required string url ){
		admin
			action="getRemoteClient"
			type="#variables.type#"
			password="#variables.password#"
			url = "#arguments.url#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the details for a remote client
	* @url url for the remote client, combination of url_server & url_path
	* @securityKey remote security key for remote client
	* @serverUsername username for http access authentication
	* @serverPassword password for http access authentication
	* @adminPassword password for the access to the remote Lucee Server Administrator
	* @label name or label for the remote client
	* @usage Define for what the Remote Client is used
	* @proxyServer host name of the proxy server
	* @proxyUsername username for the proxy server
	* @proxyPassword password for the proxy server
	* @proxyPort port for the proxy server
	*/
	public void function updateRemoteClient( required string url, required string securityKey, string serverUsername="", string serverPassword="", required string adminPassword, required string label, string usage="", string proxyServer="", string proxyUsername="", string proxyPassword="", string proxyPort="" ){
		admin
			action="updateRemoteClient"
			type="#variables.type#"
			remotetype="#variables.type#"
			password="#variables.password#"

			attributeCollection="#arguments#";
	}

	/**
	* @hint removes/deletes a remote client
	* @url url of the remote client to be removed
	*/
	public void function removeRemoteClient( required string url ){
		admin
			action="removeRemoteClient"
			type="#variables.type#"
			password="#variables.password#"

			url="#arguments.url#";
	}

	/**
	* @hint returns whether the client usage has remote connection
	*/
	public boolean function hasRemoteClientUsage(){
		admin
			action="hasRemoteClientUsage"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of remote client usage
	*/
	public query function getRemoteClientUsage(){
		admin
			action="getRemoteClientUsage"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint verifies whether it is remote client
	* @label specifies name of the remote client
	* @url specifies the url path of the remote client
	* @adminPassword specifies the administrator password for remote client
	* @securityKey specifies the security key for the remote
	*/
	public struct function verifyRemoteClient( required string label, required string url, required string adminPassword, required string securityKey ){
		var tmpStruct = {};
		try{
			admin
				action="verifyRemoteClient"
				type="#variables.type#"
				password="#variables.password#"
				label="#arguments.label#"
				url="#arguments.url#"
				adminPassword="#arguments.adminPassword#"
				securityKey="#arguments.securityKey#"
			tmpStruct.label = "Ok";
		} catch ( any e ) {
			tmpStruct.label = "Error";
			tmpStruct.message = e.message;
		}
		return tmpStruct;
	}

	/**
	* @hint returns the list of remote client tasks
	*/
	public query function getRemoteClientTasks(){
		admin
			action="getRemoteClientTasks"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of compiler settings
	*/
	public struct function getCompilerSettings(){
		admin
			action="getCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the compiler settings for lucee server
	* @templateCharset Default characterset used to read templates (*.cfm and *.cfc files)
	* @dotNotation Convert all struct keys defined with "dot notation" to upper case or need to preserve case.
	* @nullSupport If set, lucee has complete support for null, otherwise a partial null support.
	* @suppressWSBeforeArg If set, Lucee suppresses whitespace defined between the "cffunction" starting tag and the last "cfargument" tag.
	* @handleUnquotedAttrValueAsString Handle unquoted tag attribute values as strings.
	* @externalizeStringGTE Externalize strings from generated class files to separate files.
	*/
	public void function updateCompilerSettings( required string templateCharset, required string dotNotation, boolean nullSupport=false, boolean suppressWSBeforeArg=true, boolean handleUnquotedAttrValueAsString=true, numeric externalizeStringGTE=-1 ){
		var dotNotUpper=true;
		if(isDefined('arguments.dotNotation') and arguments.dotNotation EQ "oc"){
			dotNotUpper=false;
		}
		admin
			action="updateCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"

			nullSupport="#arguments.nullSupport#"
			dotNotationUpperCase="#dotNotUpper#"
			suppressWSBeforeArg="#arguments.suppressWSBeforeArg#"
			handleUnquotedAttrValueAsString="#arguments.handleUnquotedAttrValueAsString#"
			templateCharset="#arguments.templateCharset#"
			externalizeStringGTE="#arguments.externalizeStringGTE#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the details of performance settings
	*/
	public struct function getPerformanceSettings(){
		admin
			action="getPerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates server caching settings
	* @inspectTemplate sets the type of inspection for files inside the template cache
	* @typeChecking If disabled Lucee ignores type definitions with function arguments and return values
	*/
	public void function updatePerformanceSettings( required string inspectTemplate, boolean typeChecking=false ){
		admin
			action="updatePerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"

			typeChecking="#arguments.typeChecking#"
			inspectTemplate="#arguments.inspectTemplate#"

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the list of gateway entries
	*/
	public query function getGatewayEntries( type ){
		admin
			action="getGatewayEntries"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of specified gateway entry
	* @id specifies the gateway id
	*/
	public struct function getGatewayEntry( required string id){
		admin
			action="getGatewayEntry"
			type="#variables.type#"
			password="#variables.password#"
			id = "#arguments.id#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates a gateway entry
	* @id id of the gateway
	* @class class for the gateway
	* @cfcPath path to the component of the gateway
	* @listenerCfcPath Path to the Component that is listening to the Gateways action
	* @startupMode mode of operation at startup, possible values are [automatic,manual,disabled]
	* @custom structure contains custom fields for the specific gateway
	*/
	public void function updateGatewayEntry( required string id, string class="", string cfcPath="", string listenerCfcPath="", required string startupMode, struct custom={} ){
		admin
			action="updateGatewayEntry"
			type="#variables.type#"
			password="#variables.password#"

			id="#trim(arguments.id)#"
			class="#trim(arguments.class)#"
			cfcPath="#trim(arguments.cfcPath)#"
			listenerCfcPath="#trim(arguments.listenerCfcPath)#"
			startupMode="#trim(arguments.startupMode)#"
			custom="#arguments.custom#"

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the details about gateway
	* @id specifies the gateway id
	* @gatewayAction specifies the action of gateway
	*/
	public struct function gateway( required string id, required string gatewayAction ){
		admin
			action="gateway"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			gatewayAction="#arguments.gatewayAction#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint Checks whether the monitor is enable or not
	*/
	public boolean function isMonitorEnabled(){
		admin
			action="isMonitorEnabled"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of of monitors available
	*/
	public query function getMonitors(){
		admin
			action="getMonitors"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the details of a monitor
	* @monitorType type of the monitor
	* @name name of the monitor
	*/
	public struct function getMonitor( required string monitorType,  required string name){
		admin
			action="getMonitor"
			type="#variables.type#"
			password="#variables.password#"
			monitorType="#arguments.monitorType#"
			name="arguments.name"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of bundles
	*/
	public query function getBundles(){
		admin
			action="getBundles"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of bundle
	* @name specifies the name of the bundle
	*/
	public struct function getBundle( required string name ){
		admin
			action="getBundle"
			type="#variables.type#"
			password="#variables.password#"
			symbolicName = "#arguments.name#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of available debugging templates
	*/
	public query function getDebuggingList(){
		admin
			action="getDebuggingList"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the logged debug data
	*/
	public array function getLoggedDebugData(){
		admin
			action="getLoggedDebugData"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.logs";
		return logs;
	}

	/**
	* @hint returns the list of debug entries
	*/
	public query function getDebugEntry(){
		admin
			action="getDebugEntry"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return providers;
	}

	/**
	* @hint returns the debug log settings
	*/
	public struct function getDebugSetting(){
		admin
			action="getDebugSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint updates the debug log settings
	* @maxLogs defines maximum no.of logs
	*/
	public void function updateDebugSetting( required numeric maxLogs ){
		admin
			action="updateDebugSetting"
			type="#variables.type#"
			password="#variables.password#"
			maxLogs="#arguments.maxLogs#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the debug log settings
	*/
	public void function resetDebugSetting(){
		admin
			action="updateDebugSetting"
			type="#variables.type#"
			password="#variables.password#"
			maxLogs=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	** @hint returns the debugging settings
	*/
	public struct function getDebug(){
		admin
			action="getDebug"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.debug";
		return local.debug;
	}

	/**
	* @hint returns the list of SSL certificates
	* @host specifies the host server name
	*/
	public query function getSSLCertificate( required string host ){
		admin
			action="getSSLCertificate"
			type="#variables.type#"
			password="#variables.password#"
			host="#arguments.host#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateSSLCertificate
	public query function updateSSLCertificate( required string host, numeric port = 443 ){
		admin
			action="updateSSLCertificate"
			type="#variables.type#"
			password="#variables.password#"
			host="#arguments.host#"
			port="#arguments.port#";
	}

	/**
	* @hint returns the plugin directory path
	*/
	public string function getPluginDirectory(){
		admin
			action="getPluginDirectory"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of plugins
	*/
	public query function getPlugins(){
		admin
			action="getPlugins"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the list of SSL certificates
	* @host specifies the host name
	*/
	public struct function updatePlugin( required string source ){
		admin
			action="updatePlugin"
			type="#variables.type#"
			password="#variables.password#"
			source="#arguments.source#";
	}

	/**
	* @hint removes the plugin
	* @host specifies the plugin name
	*/
	public struct function removePlugin( required string name ){
		admin
			action="doRemovePlugin"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
	}

	//getContextDirectory
	public query function getContextDirectory(){
		admin
			action="getContextDirectory"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateContext
	public query function updateContext( required string source, required string destination ){
		admin
			action="updateContext"
			type="#variables.type#"
			password="#variables.password#"
			source="#arguments.source#"
			destination="#arguments.destination#";
	}

	//removeContext
	public query function removeContext( required string destination ){
		admin
			action="removeContext"
			type="#variables.type#"
			password="#variables.password#"
			destination="#arguments.destination#";
	}

	//getFlds
	public query function getFlds(){
		admin
			action="getFlds"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	//getTlds
	public query function getTlds(){
		admin
			action="getTlds"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of spooler tasks
	*/
	public query function getSpoolerTasks(){
		admin
			action="getSpoolerTasks"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getTaskSetting
	public struct function getTaskSetting(){
		admin
			action="getTaskSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	//getCfxTags
	public query function getCfxTags(){
		admin
			action="getCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return providers;
	}

	//verifyCFX
	public struct function verifyCFX( required string name ){
		local.verifyCFX={};
		try{
			admin
				action="verifyCFX"
				type="#variables.type#"
				password="#variables.password#"
				name="#arguments.name#";
			verifyJavaCFX.label="ok";
		}catch( any e ){
			local.verifyCFX.label="error";
			local.verifyCFX.catch=e.message;
		}
		return local.verifyCFX;
	}

	//getCPPCfxTags
	public query function getCPPCfxTags(){
		admin
			action="getCPPCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	//getJavaCfxTags
	public query function getJavaCfxTags(){
		admin
			action="getJavaCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	//verifyJavaCFX
	public struct function verifyJavaCFX( required string name, required string class, string bundleName, string bundleVersion ){
		local.verifyJavaCFX={};
		try{
			admin
				action="verifyJavaCFX"
				type="#variables.type#"
				password="#variables.password#"
				name="#arguments.name#"
				class="#arguments.class#"
				bundleName="#arguments.bundleName#"
				bundleVersion="#arguments.bundleVersion#";
			verifyJavaCFX.label="ok";
		}catch( any e ){
			verifyJavaCFX.label="error";
			verifyJavaCFX.catch=e.message;
		}
		return verifyJavaCFX;
	}

	/**
	* @hint returns the Login settings
	*/
	public struct function getLoginSettings(){
		admin
			action="getLoginSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.loginSettings";
		return loginSettings;
	}

	/**
	* @hint updates the login settings
	* @rememberMe Allow "Remember Me" functionality.
	* @captcha Use Captcha in the login to make sure the form is submitted by a human.
	* @delay Sets the delay between login attempts. This is a global setting for all user requests.
	*/
	public struct function updateLoginSettings( boolean rememberMe=false, boolean captcha=false, numeric delay=0  ){
		var res = {};
		try{
			admin
				action="updateLoginSettings"
				type="#variables.type#"
				password="#variables.password#"
				rememberme="#arguments.rememberme#"
				captcha="#arguments.captcha#"
				delay="#arguments.delay#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}
		return res;
	}

	/**
	* @hint returns the list of log settings
	*/
	public query function getLogSettings(){
		admin
			action="getLogSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details about execution log
	*/
	public struct function getExecutionLog(){
		admin
			action="getExecutionLog"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateLogSettings
	public struct function updateLogSettings( required string level, required string appenderClass, required string layoutClass, required string name, struct appenderArgs={}, struct layoutArgs={} ){
		local.updateLogSettings={};
		try{
			admin
					action="updateLogSettings"
					type="#variables.type#"
					password="#variables.password#"
					name="#arguments.name#"
					level="#arguments.level#"
					appenderClass="#arguments.appenderClass#"
					appenderArgs="#arguments.appenderArgs#"
					layoutClass="#arguments.layoutClass#"
					layoutArgs="#arguments.layoutArgs#";
			updateLogSettings.label="ok"
		}catch( any e ){
			updateLogSettings.label="error";
			updateLogSettings.catch=e.message;
		}
		return updateLogSettings;
	}

	/**
	* @hint returns the listener details of application
	*/
	public struct function getApplicationListener(){
		admin
			action="getApplicationListener"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateApplicationListener->
	public void function updateApplicationListener( required string listenerType, required string listenerMode ){
		admin
			action="updateApplicationListener"
			type="#variables.type#"
			password="#variables.password#"
			listenerType="#arguments.listenerType#"
			listenerMode="#arguments.listenerMode#";
	}

	/**
	* @hint returns the proxy details
	*/
	public struct function getProxy(){
		admin
			action="getProxy"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateproxy
	public void function updateproxy( boolean proxyenabled, string proxyserver="testProxy", numeric proxyport=443, string proxyusername="admin", string proxypassword="password" ){
		admin
			action="updateproxy"
			type="#variables.type#"
			password="#variables.password#"
			proxyenabled="#arguments.proxyenabled#"
			proxyserver="#arguments.proxyserver#"
			proxyport="#arguments.proxyport#"
			proxyusername="#arguments.proxyusername#"
			proxypassword="#arguments.proxypassword#";
	}

	/**
	* @hint returns the details of scopes
	*/
	public struct function getScope(){
		admin
			action="getScope"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates scope settings
	* @scopeCascadingType Depending on this setting Lucee scans certain scopes to find a variable called from the CFML source.
	* @allowImplicidQueryCall When a variable has no scope defined, Lucee will search available result sets (CFML Standard) or not.
	* @mergeFormAndUrl Defines if the scopes URL and Form will be merged together. If a key already exists in Form and URL Scopes, the value from the Form Scope is used.
	* @sessionManagement Enable or disable session management.
	* @clientManagement Enable or disable client management.
	* @domainCookies Enable or disable domain cookies.
	* @clientCookies Enable or disable client cookies.
	* @clientTimeout Sets the amount of time Lucee will keep the client scope alive.
	* @sessionTimeout Sets the amount of time Lucee will keep the session scope alive.
	* @applicationTimeout Sets the amount of time Lucee will keep the application scope alive.
	* @clientStorage Default storage for client, can be either [memory,file,cookie or any datasources or any caches defined]
	* @sessionStorage Default storage for session, can be either [memory,file,cookie or any datasources or any caches defined]
	* @sessionType type of session handled by lucee, can be either [application,jee]
	* @localMode Defines how the local scope of a function is invoked when a variable with no scope definition is used, can be either [classic,modern]
	* @cgiReadonly Defines whether the CGI Scope is read only or not.
	*/
	public struct function updateScope( required string scopeCascadingType, required boolean allowImplicidQueryCall, required boolean mergeFormAndUrl, required boolean sessionManagement, required boolean clientManagement, required boolean domainCookies, required boolean clientCookies, required timespan clientTimeout, required timespan sessionTimeout, required string clientStorage, required string sessionStorage, required timespan applicationTimeout, required string sessionType, required string localMode, required boolean cgiReadonly ){
		var res = {};
		try{
			admin
				action="updateScope"
				type="#variables.type#"
				password="#variables.password#"
				scopeCascadingType="#arguments.scopeCascadingType#"
				allowImplicidQueryCall="#arguments.allowImplicidQueryCall#"
				mergeFormAndUrl="#arguments.mergeFormAndUrl#"
				sessionManagement="#arguments.sessionManagement#"
				clientManagement="#arguments.clientManagement#"
				domainCookies="#arguments.domainCookies#"
				clientCookies="#arguments.clientCookies#"
				clientTimeout="#arguments.clientTimeout#"
				sessionTimeout="#arguments.sessionTimeout#"
				clientStorage="#arguments.clientStorage#"
				sessionStorage="#arguments.sessionStorage#"
				applicationTimeout="#arguments.applicationTimeout#"
				sessionType="#arguments.sessionType#"
				localMode="#arguments.localMode#"
				cgiReadonly="#arguments.cgiReadonly#";

			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the rest settings
	*/
	public struct function getRestSettings(){
		admin
			action="getRestSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.restSettings";
		return restSettings;
	}

	/**
	* @hint updates rest mapping settings
	* @list enable list Services when "/rest/" is called
	*/
	public struct function updateRestSettings( boolean list=false ){
		var res = {};
		try{
			admin
				action="updateRestSettings"
				type="#variables.type#"
				password="#variables.password#"
				list="#arguments.list#"
				remoteClients="#variables.remoteClients#";

			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the list of rest mappings
	*/
	public query function getRestMappings(){
		admin
			action="getRestMappings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.restMappings";
		return restMappings;
	}

	/**
	* @hint updates a rest mapping
	* @virtual virtual name for the rest mapping
	* @physical physical directory for the rest mapping
	* @default Whether this mapping is default for rest
	*/
	public struct function updateRestMapping( required string virtual, required string physical, boolean default=false ){
		var res = {};
		try{
			admin
				action="updateRestMapping"
				type="#variables.type#"
				password="#variables.password#"
				virtual="#arguments.virtual#"
				physical="#arguments.physical#"
				default="#arguments.default#";

			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint updates a rest mapping
	* @virtual virtual name for the rest mapping to be removed
	*/
	public struct function removeRestMapping( required string virtual ){
		var res = {};
		try{
			admin
				action="removeRestMapping"
				type="#variables.type#"
				password="#variables.password#"
				virtual="#arguments.virtual#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the details of application settings
	*/
	public struct function getApplicationSetting(){
		admin
			action="getApplicationSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates common application settings
	* @requestTimeout Sets the amount of time the engine will wait for a request to finish before a request timeout will be raised.
	* @scriptProtect secures your system from "cross-site scripting"
	* @allowURLRequestTimeout Whether lucee needs to obey the URL parameter RequestTimeout or not
	*/
	public struct function updateApplicationSetting( required timespan requestTimeout, required string scriptProtect, required boolean allowURLRequestTimeout ){
		var res = {};
		try{
			admin
				action="updateApplicationSetting"
				type="#variables.type#"
				password="#variables.password#"

				scriptProtect="#arguments.scriptProtect#"
				allowURLRequestTimeout="#arguments.allowURLRequestTimeout#"
				requestTimeout="#arguments.requestTimeout#"

				remoteClients="#variables.remoteClients#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the details of queue settings
	*/
	public struct function getQueueSetting(){
		admin
			action="getQueueSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint updates the concurrent request handling settings.
	* @max limits the max number of concurrent requests.
	* @timeout timeout for a request in concurrent request queue.
	* @enable enable or disable concurrent request queue.
	*/
	public struct function updateQueueSetting( required numeric max, required numeric timeout, required boolean enable ){
		var res = {};
		try{
			admin
				action="updateQueueSetting"
				type="#variables.type#"
				password="#variables.password#"

				max="#arguments.max#"
				timeout="#arguments.timeout#"
				enable="#arguments.enable#"

				remoteClients="#variables.remoteClients#";
			res.label = "OK";
		}catch( any e ){
			res.label = "Error";
			res.exception = e;
		}

		return res;
	}

	/**
	* @hint returns the list of JDBC drivers
	*/
	public query function getJDBCDrivers(){
		admin
			action="getJDBCDrivers"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updateJDBCDriver
	public void function updateJDBCDriver( required string classname, required string label, string bundleName, string bundleVersion ){
		admin
			action="updateJDBCDriver"
			type="#variables.type#"
			password="#variables.password#"

			classname="#arguments.classname#"
			label="#arguments.label#"
			bundleName="#arguments.bundleName#"
			bundleVersion="#arguments.bundleVersion#";
	}

	/**
	* @hint returns the configuration information details
	*/
	public struct function getInfo(){
		admin
			action="getinfo"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the surveillance details
	*/
	public struct function getSurveillance(){
		admin
			action="surveillance"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the details of custom tag settings
	*/
	public struct function getCustomTagSetting(){
		admin
			action="getCustomTagSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of running threads
	*/
	public query function getRunningThreads(){
		admin
			action="getRunningThreads"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getError
	public struct function getError(){
		admin
			action="getError"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	//resetId
	public struct function resetId(){
		local.resetId={};
		try{
			admin
				action="resetId"
				type="#variables.type#"
				password="#variables.password#";
			resetId.label="ok";
		}catch( any e ){
			resetId.label="error";
			resetId.catch=e.message;
		}
		return resetId;
	}

	/* Private functions */
	private struct function ComponentListPackageAsStruct(string package, cfcNames=structnew("linked")){
		try{
			local._cfcNames=ComponentListPackage(package);
			loop array="#_cfcNames#" index="i" item="el" {
				cfcNames[el]=package&"."&el;
			}
		}
		catch(e){}
		return cfcNames;
	}

	private function getArguments(Key, default) {
		if(not structKeyExists(arguments,Key)) return default;
		return arguments[Key];
	}

	private function downloadFull(required string provider,required string id , string version){
		return _download("full",provider,id,version);
	}

	private function _download(String type,required string provider,required string id, string version){


		var start=getTickCount();
		// get info from remote
		admin
			action="getAPIKey"
			type=variables.type
			password=variables.password
			returnVariable="apiKey";

		var uri=provider&"/rest/extension/provider/"&type&"/"&id;

		if(provider=="local") { // TODO use version from argument scope
			admin
				action="getLocalExtension"
				type=variables.type
				password=variables.password
				id="#id#"
				asBinary=true
				returnVariable="local.ext";
			return local.ext;
		}
		else {
			http url="#uri#?coreVersion=#server.lucee.version##len(arguments.version)?'&version='&arguments.version:''#" result="local.http" {
				httpparam type="header" name="accept" value="application/cfml";
				if(!isNull(apiKey))httpparam type="url" name="ioid" value="#apikey#";

			}
			if(!isNull(http.status_code) && http.status_code==200) {
				return http.fileContent;
			}
			throw http.fileContent;
		}
	}
</cfscript>
<cffunction name="downloadFile" access="private" returntype="void">
	<cfargument name="target" type="string" required="true">

	<cfset filename = listLast(listLast(arguments.target, "/"), "\")>
	<CFHEADER NAME="Content-Disposition" VALUE="inline; filename=#filename#"><cfcontent file="#arguments.target#" deletefile="yes" type="application/unknow">
</cffunction>
</cfcomponent>