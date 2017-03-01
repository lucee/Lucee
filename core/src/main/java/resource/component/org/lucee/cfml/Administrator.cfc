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

	/**
	* @hint verifies whether it is datasource or not
	* @name name of the datasource to be verified
	* @dbusername username of the database
	* @dbpassword password of the database
	*/
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

	/**
	* @hint verifies whether it is mail server or not
	* @hostname name of the host server to be verified
	* @port port number of the host
	* @mailusername username of the mail
	* @mailpassword password of the mail
	*/
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

	/**
	* @hint returns the details about mapping
	* @virtual specifies the virtual name for the mapping
	*/
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
	* @hint compiles the CTmapping for any errors
	* @virtual virtual name for the CTmapping to be compiled.
	*/
	public void function compileCTMapping(required string virtual){
		admin
			action="compileCTMapping"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint compiles the component mapping for any errors
	* @virtual virtual name for the component mapping to be compiled.
	*/
	public void function compileComponentMapping(required string virtual){
		admin
			action="compileComponentMapping"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
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

	/**
	* @hint returns the list of custom tag mappings
	*/
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
	* @hint updates the extension information.
	* @enabled enable or disable to update the extension information.
	*/
	public void function updateExtensionInfo(boolean enabled=false){
		admin
			action="updateExtensionInfo"
			type="#variables.type#"
			password="#variables.password#"
			enabled="#arguments.enabled#";
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

	/**
	* @hint returns the list of RH extensions
	*/
	public query function getRHExtensions(){
		admin
			action="getRHExtensions"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of RH server extension
	*/
	public query function getRHServerExtensions(){
		admin
			action="getRHServerExtensions"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the details about local extension
	* @id specifies the id of local extension
	*/
	public struct function getLocalExtension( required string id  ){
		admin
			action="getLocalExtension"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of local extensions
	*/
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
			action="getExtensionProviders"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return local.providers;
	}

	/**
	* @hint updates the extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function updateExtensionProvider( required string url ){
		admin
			action="updateExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
	}

	/**
	* @hint removes the extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function removeExtensionProvider( required string url ){
		admin
			action="removeExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
	}

	/**
	* @hint verifies whether it is an extension provider or not
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
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
	* @hint returns the list of extension providers for this context.
	*/
	public query function getRHExtensionProviders(){
		admin
			action="getRHExtensionProviders"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return local.providers;
	}

	/**
	* @hint updates RH extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function updateRHExtensionProvider( required string url ){
		admin
			action="updateRHExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
	}

	/**
	* @hint removes the extension provider for this context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function removeRHExtensionProvider( required string url ){
		admin
			action="removeRHExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"

			url="#trim(arguments.url)#";
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
	* @hint updates the component mappings settings
	* @virtual specifies as identifier when automatically import a Lucee Archive build based on this Mapping
	* @physical specifies directory path where the components are located, this path should not include the package
	* @archive specifies file path to a components Lucee Archive (.lar).
	* @inspect checks for changes in the source file for a already loaded component
	*/
	public void function updateComponentMapping(required string virtual, required string physical, required string archive, string inspect="never"){
		admin
			action="updateComponentMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			archive="#arguments.archive#"
			inspect="#arguments.inspect#";
	}

	/**
	* @hint removes the component mappings
	* @virtual specifies the virtual name of the component mappings
	*/
	public void function removeComponentMapping(required string virtual){
		admin
			action="removeComponentMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#";
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

	/**
	* @hint verifies whether cache has connection or not
	* @name specifies the name of the cache to be verified
	*/
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
	* @hint remove a cache connection
	* @name name for the cache connection
	*/
	public void function removeCacheConnection( required string name ) {
		admin
			action="removeCacheConnection"
			type="#variables.type#"
			password="#variables.password#"

			name="#arguments.name#"

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
		admin
			action="updateCacheDefaultConnection"
			type="#variables.type#"
			password="#variables.password#"
			object="#arguments.object#"
			template="#arguments.template#"
			query="#arguments.query#"
			resource="#arguments.resource#"
			"function"="#arguments['function']#"
			include="#arguments.include#"
			http="#arguments.http#"
			file="#arguments.file#"
			webservice="#arguments.webservice#"
			remoteClients="#variables.remoteClients#";

	}

	/**
	* @hint remove all default cache connection for various lucee elements
	*/
	public void function removeCacheDefaultConnection() {
		admin
			action="removeCacheDefaultConnection"
			type="#variables.type#"
			password="#variables.password#"
			remoteClients="#variables.remoteClients#";
	}


	/**
	* @hint updates the cache within information
	* @cachedWithinType specifies the type of cache to be updated
	* @cachedWithin specifies the time limit for the cache
	*/.
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
	* @hint updates the details for a remote client usage
	* @code specifies the code of the remote client
	* @displayName specifies the name of the remote client usage
	*/
	public void function updateRemoteClientUsage( required string code, required string displayName ){
		admin
			action="updateRemoteClientUsage"
			type="#variables.type#"
			remotetype="#variables.type#"
			password="#variables.password#"
			code="#arguments.code#"
			displayname="#arguments.displayName#";
	}

	/**
	* @hint removes/deletes a remote client usage
	* @code specifies the code of the remote client
	*/
	public void function removeRemoteClientUsage( required string code ){
		admin
			action="removeRemoteClientUsage"
			type="#variables.type#"
			password="#variables.password#"

			code="#arguments.code#";
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
	* @hint removes/deletes a remote client task
	* @id specifies the id of the remote client task
	*/
	public void function removeRemoteClientTask( required string id ){
		admin
			action="removeRemoteClientTask"
			type="#variables.type#"
			password="#variables.password#"

			id="#arguments.id#";
	}

	/**
	* @hint executes the specified remote client task
	* @id specifies the id of the remote client task
	*/
	public void function executeRemoteClientTask( required string id ){
		admin
			action="executeRemoteClientTask"
			type="#variables.type#"
			password="#variables.password#"

			id="#arguments.id#";
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
	* @hint resets the server caching settings
	*/
	public void function resetPerformanceSettings(){
		admin
			action="updatePerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"

			inspectTemplate=""
			typeChecking=""

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
	* @hint removes the gateway entry
	* @id id of the gateway to be removed
	*/
	public void function removeGatewayEntry( required string id ){
		admin
			action="removeGatewayEntry"
			type="#variables.type#"
			password="#variables.password#"
			id="#trim(arguments.id)#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint returns the details about gateway
	* @id specifies the gateway id
	* @gatewayAction specifies the action of gateway
	*/
	public void function gateway( required string id, required string gatewayAction ){
		admin
			action="gateway"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			gatewayAction="#arguments.gatewayAction#";
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
	* @hint updates the monitor details
	* @monitorEnabled specifies whether to enable monitor or not
	*/
	public void function updateMonitorEnabled(required boolean monitorEnabled){
		admin
			action="updateMonitorEnabled"
			type="#variables.type#"
			password="#variables.password#"
			monitorEnabled="#arguments.monitorEnabled#";
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
			name="#arguments.name#"
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
	* @hint creates/updates a debug entry.
	* @label label for the debugging template entry
	* @type type of the debugging template entry
	* @ipRange ip range for the debugging template entry
	* @custom a struct contains all the custom fields for debugging template entry
	*/
	public void function updateDebugEntry( required string label, required string type, required string ipRange, required struct custom ){
		// load available drivers
		var driverNames=structnew("linked");
		driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames);
		driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames);
		driverNames=ComponentListPackageAsStruct("debug",driverNames);


		var drivers={};
		loop collection="#driverNames#" index="local.n" item="local.fn"{
			if(n EQ "Debug" or n EQ "Field" or n EQ "Group"){
				continue;
			}
			tmp=createObject('component',fn);
			drivers[trim(tmp.getId())]=tmp;
		}

		var driver=drivers[trim(arguments.type)];
		var meta=getMetaData(driver);
		admin
			action="updateDebugEntry"
			type="#variables.type#"
			password="#variables.password#"

			label="#arguments.label#"
			debugtype="#arguments.type#"
			iprange="#arguments.ipRange#"
			fullname="#meta.fullName#"
			path="#contractPath(meta.path)#"
			custom="#arguments.custom#"

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint removes a debug entry
	* @id id of the debug entry to be removed.
	*/
	public void function removeDebugEntry( required string id ){
		admin
			action="removeDebugEntry"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			remoteClients="#variables.remoteClients#";
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
	* @hint updates the debugging settings
	* @debug sets whether  debugging is enabled
	* @database this option sets to log the database activity for the SQL Query events and Stored Procedure events.
	* @queryUsage this option sets to log the query usage information.
	* @exception this option sets to log the all exceptions raised for the request.
	* @tracing this option sets to log the trace event information.
	* @dump this option sets to enable output produced with help of the tag cfdump and send to debugging.
	* @timer this option sets to show timer event information.
	* @implicitAccess this option sets to log all accesses to scopes, queries and threads that happens implicit (cascaded).
	*/
	public void function updateDebug( boolean debug=false, boolean database=false, boolean queryUsage=false, boolean exception=false, boolean tracing=false, boolean dump=false, boolean timer=false, boolean implicitAccess=false ){
		admin
			action="updateDebug"
			type="#variables.type#"
			password="#variables.password#"

			debug="#arguments.debug#"
			database="#arguments.database#"
			exception="#arguments.exception#"
			tracing="#arguments.tracing#"
			dump="#arguments.dump#"
			timer="#arguments.timer#"
			implicitAccess="#arguments.implicitAccess#"
			queryUsage="#arguments.queryUsage#"

			debugTemplate=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the debugging settings
	*/
	public void function resetDebug(){
		admin
			action="updateDebug"
			type="#variables.type#"
			password="#variables.password#"

			debug=""
			database=""
			exception=""
			tracing=""
			dump=""
			timer=""
			implicitAccess=""
			queryUsage=""

			debugTemplate=""
			remoteClients="#variables.remoteClients#";
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

	/**
	* @hint update the SSL certificates
	* @host specifies the host server name
	* @port specifies the port number of the host server
	*/
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
			action="removePlugin"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
	}

	/**
	* @hint returns the list of context directories path
	*/
	public query function getContextDirectory(){
		admin
			action="getContextDirectory"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint returns the list of contexts
	*/
	public query function getContextes(){
		admin
			action="getContextes"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.contextes";
		return contextes;
	}

	/**
	* @hint returns the list of contexts
	*/
	public query function getContexts(){
		admin
			action="getContexts"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.contexts";
		return contexts;
	}

	/**
	* @hint update the context directories
	* @source specifies the source path to get data
	* @destination specifies the destination path to store data
	*/
	public void function updateContext( required string source, required string destination ){
		admin
			action="updateContext"
			type="#variables.type#"
			password="#variables.password#"
			source="#arguments.source#"
			destination="#arguments.destination#";
	}

	/**
	* @hint removes the context directories from the path
	* @destination specifies the destination path to remove context
	*/
	public void function removeContext( required string destination ){
		admin
			action="removeContext"
			type="#variables.type#"
			password="#variables.password#"
			destination="#arguments.destination#";
	}

	/**
	* @hint returns the list of FLD's
	*/
	public query function getFlds(){
		admin
			action="getFlds"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint returns the list of TLD's
	*/
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

	/**
	* @hint returns the details of task settings
	*/
	public struct function getTaskSetting(){
		admin
			action="getTaskSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		return rtn;
	}

	/**
	* @hint update the task settings
	* @maxThreads specifies the maximum number of parallel threads used to execute tasks at the same time
	*/
	public void function updateTaskSetting(required numeric maxThreads){
		admin
			action="updateTaskSetting"
			type="#variables.type#"
			password="#variables.password#"
			maxThreads="#arguments.maxThreads#"
	}

	/**
	* @hint returns the list of CFX tags
	*/
	public query function getCfxTags(){
		admin
			action="getCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
		return providers;
	}

	/**
	* @hint verifies whether it is CFX tag or not
	* @name specifies the name of the tag to verify
	*/
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

	/**
	* @hint returns the list of CPPCFX tags
	*/
	public query function getCPPCfxTags(){
		admin
			action="getCPPCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint returns the list of javaCFX tags
	*/
	public query function getJavaCfxTags(){
		admin
			action="getJavaCfxTags"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint verifies whether it is javaCFX tag or not
	* @name specifies the name of the javaCFXtag to verify
	* @class class name for the javaCFX
	* @bundleName bundle name for the javaCFX
	* @bundleVersion bundle version for the javaCFX
	*/
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

	/**
	* @hint updates the execution log settings
	* @class specifies the class name of execution log
	* @arguments specifies the structure of arguments
	* @enabled specifies whether log is enable or not
	*/
	public void function updateExecutionLog(struct arguments={},required string class,boolean enabled=false){
		admin
			action="updateExecutionLog"
			type="#variables.type#"
			password="#variables.password#"
			arguments="#arguments.arguments#"
			class="#arguments.class#"
			enabled="#arguments.enabled#";
	}

	/**
	* @hint updates the log settings
	* @appenderClass specifies the appender class to display the log
	* @layoutClass specifies the layout format to display
	* @name specifies the name of the log to update
	* @appenderArgs specifies the structure of appender class
	* @layoutArgs specifies the structure of layout class
	*/
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

	/**
	* @hint updates the application listener
	* @listenerType specifies the type of listener to update
	* @listenerMode specifies the mode of the listener
	*/
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

	/**
	* @hint updates the proxy settings
	* @proxyenabled specifies whether the proxy is enable or not
	* @proxyserver specifies the proxy server
	* @proxyport specifies the port of proxy host server
	* @proxyusername specifies the username of the proxy to access
	* @proxypassword specifies the password of the proxy to access
	*/
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

	/**
	* @hint updates the JDBC driver
	* @classname specifies the class name of JDBC driver
	* @label specifies the name of the JDBC driver to update
	* @bundleName specifies the bundle name of JDBC driver
	* @bundleVersion specifies the bundle version of JDBC driver
	*/
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
	* @hint updates the password for administrator
	* @oldPassword existing password for administrator
	* @newPassword new password for administrator
	*/
	public void function updatePassword( required string oldPassword, required string newPassword ){
		admin
			action="updatePassword"
			type="#variables.type#"
			oldPassword="#arguments.oldPassword#"
			newPassword="#arguments.newPassword#";
	}

	/**
	* @contextPath contextPath for which password needs to be resetted.
	*/
	public void function resetPassword( required string contextPath ){
		admin
			action="resetPassword"
			type="#variables.type#"
			password="#variables.password#"
			contextPath="#arguments.contextPath#";
	}

	/**
	* @hint to get default password.
	*/
	public string function getDefaultPassword() {
		admin
			action="getDefaultPassword"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="defaultPassword";
		return defaultPassword;
	}

	/**
	* @hint update the default password.
	* @newPassword the new password to set.
	*/
	public void function updateDefaultPassword( required string newPassword) {
		admin
			action="updateDefaultPassword"
			type="#variables.type#"
			password="#variables.password#"
			newPassword="#arguments.newPassword#";
	}

	/**
	* @hint remove the default password.
	*/
	public void function removeDefaultPassword() {
		admin
			action="removeDefaultPassword"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint to get hashedPassword.
	*/
	public string function hashpassword() {
		admin
			action="hashPassword"
			type="#variables.type#"
			pw="#variables.password#"
			returnVariable="hashedPassword";
		return hashedPassword;
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
	* @hint returns the details of custom tag settings
	* @deepSearch Search for custom tags in subdirectories.
	* @localSearch Search in the caller directory for the custom tag
	* @component path is cached and not resolved again
	* @extensions These are the extensions used for Custom Tags, in the order they are searched.
	*/
	public void function updateCustomTagSetting( required boolean deepSearch, required boolean localSearch, required customTagPathCache, required string extensions ) {
		admin
			action="updateCustomTagSetting"
			type="#variables.type#"
			password="#variables.password#"

			deepSearch="#arguments.deepSearch#"
			localSearch="#arguments.localSearch#"
			customTagPathCache="#arguments.customTagPathCache#"
			extensions="#arguments.extensions#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint update exiting custom tag
	* @virtual The name is used as identifier when you automaticly import a Lucee Archive build based on this Mapping.
	* @physical Directory path where the custom tags are located.
	* @archive File path to a custom tag Lucee Archive (.lar).
	* @primary Defines where Lucee does looks first for a requested custom tags
	* @inspect When does Lucee checks for changes in the source file for a already loaded custom tags.
	*/
	public void function updateCustomTag( required string virtual, required string physical, required string archive, string primary="Resource", string inspect="" ) {
		admin
			action="updateCustomTag"
			type="#variables.type#"
			password="#variables.password#"

			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			archive="#arguments.archive#"
			primary="#arguments.primary#"
			inspect="#arguments.inspect#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint update exiting custom tag
	* @virtual The name is used as identifier when you automaticly import a Lucee Archive build based on this Mapping.
	*/
	public any function removecustomtag( required string virtual ) {
		admin
			action="removeCustomTag"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			remoteClients="#variables.remoteClients#";
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

	/**
	* @hint returns the details about error
	*/
	public struct function getError(){
		admin
			action="getError"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hints checks whether the current user has access
	* @secType area for which access needs to be checked
	*/
	public boolean function securityManager( required string secType ){
		admin
			action="securityManager"
			type="#request.adminType#"
			password="#session["password"&request.adminType]#"
			secType="#arguments.secType#";
			returnVariable="access"
	}

	/**
	* @hint returns the list of authKeys
	*/
	public array function listAuthKey(){
		admin
			action="listAuthKey"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint update the authKey details
	* @key specifies the key name to update
	*/
	public void function updateAuthKey(string key=""){
		admin
			action="updateAuthKey"
			type="#variables.type#"
			password="#variables.password#"
			key="#arguments.key#";
	}

	/**
	* @hint removes the authKey details
	* @key specifies the key name to remove
	*/
	public void function removeAuthKey(string key=""){
		admin
			action="removeAuthKey"
			type="#variables.type#"
			password="#variables.password#"
			key="#arguments.key#";
	}

	/**
	* @hint returns the list of resource providers
	*/
	public query function getResourceProviders(){
		admin
			action="getResourceProviders"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint update the details of resource provider
	* @class specifies the class name to update
	* @scheme specifies the type of resource to update
	* @arguments specifies the arguments passed to update
	*/
	public void function updateResourceProvider(required string class, required string scheme, required string arguments){
		admin
			action="updateResourceProvider"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#"
			scheme="#arguments.scheme#"
			arguments="#arguments.arguments#";
	}

	/**
	* @hint update the details of resource provider
	* @class specifies the class name to update
	* @arguments specifies the arguments passed to update
	*/
	public void function updateDefaultResourceProvider(required string class, required string arguments){
		admin
			action="updateDefaultResourceProvider"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#"
			arguments="#arguments.arguments#";
	}

	/**
	* @hint removes the resource provider
	* @scheme specifies the type of resource to remove
	*/
	public void function removeResourceProvider(required string scheme){
		admin
			action="removeResourceProvider"
			type="#variables.type#"
			password="#variables.password#"
			scheme="#arguments.scheme#";
	}

	/**
	* @hint returns the information about cluster class
	*/
	public string function getClusterClass(){
		admin
			action="getClusterClass"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint update the details of cluster class
	* @class specifies the class name to update
	*/
	public void function updateClusterClass(required string class){
		admin
			action="updateClusterClass"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#";
	}

	/**
	* @hint returns the information about adminSync class
	*/
	public string function getAdminSyncClass(){
		admin
			action="getAdminSyncClass"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint update the details of adminSync class
	* @class specifies the class name to update
	*/
	public void function updateAdminSyncClass(required string class){
		admin
			action="updateAdminSyncClass"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#";
	}

	/**
	* @hint returns the information about videoExecuter class
	*/
	public string function getVideoExecuterClass(){
		admin
			action="getVideoExecuterClass"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint update the details of videoExecuter class
	* @class specifies the class name to update
	*/
	public void function updateVideoExecuterClass(required string class){
		admin
			action="updateVideoExecuterClass"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#";
	}

	/**
	* @hint returns the information about update details
	*/
	public struct function getUpdate(){
		admin
			action="getUpdate"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
	}

	/**
	* @hint executes and run the update details
	*/
	public void function runUpdate(){
		admin
			action="runUpdate"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint updates the update details
	* @updatetype specifies the type of the update
	* @updatelocation specifies the location to update
	*/
	public void function updateUpdate(required string updatetype, required string updatelocation){
		admin
			action="updateUpdate"
			type="#variables.type#"
			password="#variables.password#"
			updatetype="#arguments.updatetype#"
			updatelocation="#arguments.updatelocation#";
	}

	/**
	* @hint removes the update details
	*/
	public void function removeUpdate(){
		admin
			action="removeUpdate"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint reset the ID
	*/
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