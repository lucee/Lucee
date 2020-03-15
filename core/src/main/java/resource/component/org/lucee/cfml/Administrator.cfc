component {
	/**
	** @hint constructor of the component
	* @type type contex type, valid values are "server" or "web"
	* @password password for current context
	*/
	function init(required string type,required string password, string remoteClients){
		variables.type=arguments.type;
		variables.password=arguments.password;
		variables.remoteClients=!isNull(arguments.remoteClients)?arguments.remoteClients:"";
	}

	/**
	* @hint returns Regional information about current context, this includes the locale, the timezone,a timeserver address and if the timeserver is used or not
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
	* @hint updates the regional settings of current context
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
		if(variables.type != "web") {
			throw "reset Regional function supports only in web";
		}

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
	* @hint returns charset information about current context
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
	* @hint updates the charset settings of current context
	* @resourceCharset default charset used for read/write resources (cffile,filewrite ...)
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
		if(variables.type != "web") {
			throw "reset resetCharset function supports only in web";
		}

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
	* @hint returns output settings for current context
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
	* @hint updates output settings for current context
	* @cfmlWriter  Whitespace management in lucee Output settings
	* @suppressContent  suppressContent in lucee Output settings
	* @allowCompression  allowCompression in lucee Output settings
	* @bufferOutput  bufferOutput in lucee Output settings
	*/
	public void function updateOutputSetting( required string cfmlWriter, boolean suppressContent, boolean allowCompression, boolean bufferOutput ){
		var existing = getOutputSetting();
		admin
			action="updateOutputSetting"
			type="#variables.type#"
			password="#variables.password#"
			cfmlWriter="#arguments.cfmlWriter#"
			suppressContent=isNull(arguments.suppressContent) || isEmpty(arguments.suppressContent) ? existing.suppressContent : arguments.suppressContent
			allowCompression=isNull(arguments.allowCompression) || isEmpty(arguments.allowCompression) ? existing.allowCompression : arguments.allowCompression
			bufferOutput=isNull(arguments.bufferOutput) || isEmpty(arguments.bufferOutput) ? existing.bufferOutput : arguments.allowCompression
			contentLength=""

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets output settings for current context
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
	* @hint returns Preserve single quotes (") settings
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
	public void function updateDatasourceSetting(boolean psq ){
		var existing = getDatasourceSetting();
		admin
			action="updatePSQ"
			type="#variables.type#"
			password="#variables.password#"

			psq=isNull(arguments.psq) || isEmpty(arguments.psq) ? existing.psq : arguments.psq
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the Preserve single quotes setting
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
	* @hint returns the all the datasources defined for current context
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
	* @hint returns the datasource information for for current context
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
	* @hint updates a specific datasource defined for current context
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
	* @hint removes a specific datasource defined for current context
	* @dsn name of the datasource to be removed from current context
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
	public void function verifyDatasource( required string name, required string dbusername, required string dbpassword ){
		admin
			action="verifyDatasource"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			dbusername="#arguments.dbusername#"
			dbpassword="#arguments.dbpassword#";
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
	* @hint returns a list mail servers defined for current context
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
	* @hint updates a specific mail server defined for current context
	* @host Mail server host name (for example smtp.gmail.com).
	* @port Port of the mail server (for example 587).
	* @username Username of the mail server.
	* @password Password of the mail server.
	* @tls Enable Transport Layer Security.
	* @ssl Enable secure connections via SSL.
	* @life Overall timeout for the connections established to the mail server.
	* @idle Idle timeout for the connections established to the mail server.
	*/
	public void function updateMailServer( required string host, required string port, string username="", string password="", boolean tls=false, boolean ssl=false, timespan life=CreateTimeSpan(0, 0, 1, 0), timespan idle=CreateTimeSpan(0, 0, 0, 10) ){

		var mailServers = getMailservers();
		if( structKeyExists(arguments, 'username') && arguments.username == ''  ){
			query name="existing" dbtype="query"{
				echo("SELECT * FROM mailservers WHERE hostName = '#arguments.host#' and port = '#arguments.port#' ")
			}
		} else{
			query name="existing" dbtype="query"{
				echo("SELECT * FROM mailservers WHERE hostName = '#arguments.host#' and port = '#arguments.port#' and username = '#arguments.username#' ")
			}
		}

		admin
			action="updateMailServer"
			type="#variables.type#"
			password="#variables.password#"

			hostname="#arguments.host#"
			dbusername=isNull(arguments.username) || isEmpty(arguments.username) ? existing.username : arguments.username
			dbpassword=isNull(arguments.password) || isEmpty(arguments.password) ? existing.password : arguments.password
			life=isNull(arguments.life) || isEmpty(arguments.life) ? existing.life : arguments.life
			idle=isNull(arguments.idle) || isEmpty(arguments.idle) ? existing.idle : arguments.idle

			port="#arguments.port#"
			id="new"
			tls=isNull(arguments.tls) || isEmpty(arguments.tls) ? existing.tls : arguments.tls
			ssl=isNull(arguments.ssl) || isEmpty(arguments.ssl) ? existing.ssl : arguments.ssl
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint verifies whether it is mail server or not
	* @hostname name of the host server to be verified
	* @port port number of the host
	* @mailusername username of the mail
	* @mailpassword password of the mail
	*/
	public void function verifyMailServer( required string hostname, required string port, required string mailusername, required string mailpassword ){
		admin
			action="verifyMailServer"
			type="#variables.type#"
			password="#variables.password#"
			hostname="#arguments.hostname#"
			port="#arguments.port#"
			mailusername="#arguments.mailusername#"
			mailpassword="#arguments.mailpassword#";
	}


	/**
	* @hint removes a specific mailserver defined for current context.
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
	* @hint returns mail settings for current context.
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
	* @hint updates the mail settings for current context
	* @defaultEncoding Default encoding used for mail servers
	* @spoolenable If enabled the mails are sent in a background thread and the main request does not have to wait until the mails are sent.
	* @timeout Time in seconds that the Task Manager waits to send a single mail, when the time is reached the Task Manager stops the thread and the mail gets moved to unsent folder, where the Task Manager will pick it up later to try to send it again.
	*/
	public void function updateMailSetting( string defaultEncoding="UTF-8", boolean spoolEnable, numeric timeOut ){
		var existing = getMailSetting();
		admin
			action="updateMailSetting"
			type="#variables.type#"
			password="#variables.password#"

			spoolEnable=isNull(arguments.spoolEnable) || isEmpty(arguments.spoolEnable) ? existing.spoolEnable : arguments.spoolEnable
			timeout=isNull(arguments.timeout) || isEmpty(arguments.timeout) ? existing.timeout : arguments.timeout
			defaultEncoding=isNull(arguments.defaultEncoding) || isEmpty(arguments.defaultEncoding) ? existing.defaultEncoding : arguments.defaultEncoding
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the mail settings for current context
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
	* @hint returns the list of mappings defined for current context
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
	* @hint updates/inserts a specific mapping for current context
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
			archive=isNull(arguments.physical) || isEmpty(arguments.physical) ? existing.physical : arguments.physical
			primary=isNull(arguments.primary) || isEmpty(arguments.primary) ? existing.primary : arguments.primary
			inspect=isNull(arguments.inspect) || isEmpty(arguments.inspect) ? existing.inspect : arguments.inspect
			toplevel="yes"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint removes a mapping defined in current context
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
	*/
	public void function createArchiveFromMapping(required string virtual, required string target, boolean addCFMLFile=true, boolean addNonCFMLFile=true){
		var ext="lar";
		var filename=arguments.virtual;
		filename=mid(filename,2,len(filename));
		if(len(filename)){
			filename="archive-"&filename&"."&ext;
		}else{
			filename="archive-root."&ext;
		}
		filename=Replace(filename,"/","-","all");
		var target=expandPath("#cgi.context_path#/lucee/"&filename);
		count=0;
		while(fileExists(target)){
			count=count+1;
			target="#cgi.context_path#/lucee/"&filename;
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
	* @hint returns the list of extensions for current context.
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
	* @id id of the extension
	* @version version of the extension
	*/
	public void function updateExtension(required string id , string version ) {
		if(isValid('uuid',id)) {
			if(!isNull(arguments.version) && !isEmpty(arguments.version)) {
				admin
					action="updateRHExtension"
					type=variables.type
					password=variables.password
					id=arguments.id
					version=arguments.version;
			}
			else {
				admin
					action="updateRHExtension"
					type=variables.type
					password=variables.password
					id=arguments.id;
			}
		}
		else {
			admin
				action="updateRHExtension"
				type=variables.type
				password=variables.password
				source=arguments.id;
		}
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
	* @hint returns the list of server extension
	*/
	public query function getServerExtensions(){
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
	* @hint returns the list of extension providers for current context.
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
	* @hint verifies whether it is an extension provider or not
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function verifyExtensionProvider( required string url ){
		admin
			action="verifyExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"
			url="#arguments.url#";
	}

	/**
	* @hint updates extension provider for current context.
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
	* @hint removes the extension provider for current context.
	* @url URL to the Extension Provider (Example: http://www.myhost.com)
	*/
	public void function removeExtensionProvider( required string url ){
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
	public void function updateORMSetting( boolean autoGenMap, boolean eventHandling, boolean flushAtRequestEnd, boolean logSQL, boolean, boolean useDBForMapping, string catalog, string cfcLocation, string dbCreate, string schema ){
		var existing = getORMSetting();

		admin
			action="updateORMSetting"
			type="#variables.type#"
			password="#variables.password#"

			autogenmap=isNull(arguments.autogenmap) || isEmpty(arguments.autogenmap) ? existing.autogenmap : arguments.autogenmap
			eventHandling=isNull(arguments.eventHandling) || isEmpty(arguments.eventHandling) ? existing.eventHandling : arguments.eventHandling
			flushatrequestend=isNull(arguments.flushatrequestend) || isEmpty(arguments.flushatrequestend) ? existing.flushatrequestend : arguments.flushatrequestend
			logSQL=isNull(arguments.logSQL) || isEmpty(arguments.logSQL) ? existing.logSQL : arguments.logSQL
			savemapping=isNull(arguments.savemapping) || isEmpty(arguments.savemapping) ? existing.savemapping : arguments.savemapping
			useDBForMapping=isNull(arguments.useDBForMapping) || isEmpty(arguments.useDBForMapping) ? existing.useDBForMapping : arguments.useDBForMapping

			catalog=isNull(arguments.catalog) || isEmpty(arguments.catalog) ? existing.catalog : arguments.catalog
			cfclocation=isNull(arguments.cfclocation) || isEmpty(arguments.cfclocation) ? ArrayToList(existing.cfclocation) : arguments.cfclocation
			dbcreate=isNull(arguments.dbcreate) || isEmpty(arguments.dbcreate) ? existing.dbcreate : arguments.dbcreate
			schema=isNull(arguments.schema) || isEmpty(arguments.schema) ? existing.schema : arguments.schema

			sqlscript=isNull(arguments.sqlscript) || isEmpty(arguments.sqlscript) ? existing.sqlscript : arguments.sqlscript
			cacheconfig=isNull(arguments.cacheconfig) || isEmpty(arguments.cacheconfig) ? existing.cacheconfig : arguments.cacheconfig
			cacheProvider=isNull(arguments.cacheProvider) || isEmpty(arguments.cacheProvider) ? existing.cacheProvider : arguments.cacheProvider
			ormConfig=isNull(arguments.ormConfig) || isEmpty(arguments.ormConfig) ? existing.ormConfig : arguments.ormConfig
			secondarycacheenabled=isNull(arguments.secondarycacheenabled) || isEmpty(arguments.secondarycacheenabled) ? existing.secondarycacheenabled : arguments.secondarycacheenabled

			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the ORM settings
	*/
	public void function resetORMSetting(){
		admin
			action="resetORMSetting"
			type="#variables.type#"
			password="#variables.password#"
			remoteClients="#variables.remoteClients#";
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
	* @hint update the ORM engine details
	* @class specifies the class name of ORM engine to update
	* @bundleName specifies the bundle name of ORM
	* @bundleVersion specifies the bundle version of ORM
	*/
	public void function updateORMEngine(required string class, string bundleName="", string bundleVersion=""){
		admin
			action="updateORMEngine"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#"
			bundleName="#arguments.bundleName#"
			bundleVersion="#arguments.bundleVersion#";
	}

	/**
	* @hint removes the ORM engine details
	*/
	public void function removeORMEngine(){
		admin
			action="removeORMEngine"
			type="#variables.type#"
			password="#variables.password#";
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
	public void function updateComponent(string baseComponentTemplateCFML="", string baseComponentTemplateLucee="", string componentDumpTemplate="", string componentDataMemberDefaultAccess="public", boolean triggerDataMember=false, boolean useShadow=true, string componentDefaultImport="org.lucee.cfml.*", boolean componentLocalSearch=false, boolean componentPathCache=false, boolean componentDeepSearchDesc=false){
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
	* @primary type of mapping, resource/archive
	* @archive specifies file path to a components Lucee Archive (.lar).
	* @inspect checks for changes in the source file for an already loaded component
	*/
	public void function updateComponentMapping(required string virtual, required string physical, required string archive, string inspect="never"){
		admin
			action="updateComponentMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			primary="#arguments.primary#"
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
	* @hint verifies whether cache has connection or not
	* @name specifies the name of the cache to be verified
	*/
	public void function verifyCacheConnection( required string name ){
		admin
			action="verifyCacheConnection"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
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
		struct custom,
		string bundleName,
		string bundleVersion,
		string default,
		boolean readonly,
		boolean storage
	){
		var connections =  getCacheConnections()
		query name="existing" dbtype="query"{
			echo("SELECT * FROM connections WHERE class = '#arguments.class#' and name = '#arguments.name#' ")
		}

		admin
			action="updateCacheConnection"
			type="#variables.type#"
			password="#variables.password#"

			class="#arguments.class#"
			name="#arguments.name#"
			custom=isNull(arguments.custom) || isEmpty(arguments.custom) ? isEmpty(existing.custom)  ? {} : existing.custom  : arguments.custom
			bundleName=isNull(arguments.bundleName) || isEmpty(arguments.bundleName) ? existing.bundleName ?: "" : arguments.bundleName
			bundleVersion=isNull(arguments.bundleVersion) || isEmpty(arguments.bundleVersion) ? existing.bundleVersion ?: "" : arguments.bundleVersion
			default=isNull(arguments.default) || isEmpty(arguments.default) ? existing.default ?: false : arguments.default
			readonly=isNull(arguments.readonly) || isEmpty(arguments.readonly) ? existing.readonly ?: false : arguments.readonly
			storage=isNull(arguments.storage) || isEmpty(arguments.storage) ? existing.storage ?: false : arguments.storage

			remoteClients="#variables.remoteClients#";
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
	* @dotNotationUpperCase Convert all struct keys defined with "dot notation" to upper case or need to preserve case.
	* @nullSupport If set, lucee has complete support for null, otherwise a partial null support.
	* @suppressWSBeforeArg If set, Lucee suppresses whitespace defined between the "cffunction" starting tag and the last "cfargument" tag.
	* @handleUnquotedAttrValueAsString Handle unquoted tag attribute values as strings.
	* @externalizeStringGTE Externalize strings from generated class files to separate files.
	*/
	public void function updateCompilerSettings( required string templateCharset, required string dotNotationUpperCase, boolean nullSupport, boolean suppressWSBeforeArg, boolean handleUnquotedAttrValueAsString, numeric externalizeStringGTE){
		var dotNotUpper=true;
		if(isDefined('arguments.dotNotationUpperCase') and arguments.dotNotationUpperCase EQ "oc"){
			dotNotUpper=false;
		}
		var existing = getCompilerSettings();
		admin
			action="updateCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"

			templateCharset="#arguments.templateCharset#"
			dotNotationUpperCase="#dotNotUpper#"
			nullSupport=isNull(arguments.nullSupport) || isEmpty(arguments.nullSupport) ? existing.nullSupport  : arguments.nullSupport
			suppressWSBeforeArg=isNull(arguments.suppressWSBeforeArg) || isEmpty(arguments.suppressWSBeforeArg) ? existing.suppressWSBeforeArg : arguments.suppressWSBeforeArg
			handleUnquotedAttrValueAsString=isNull(arguments.handleUnquotedAttrValueAsString) || isEmpty(arguments.handleUnquotedAttrValueAsString) ? existing.handleUnquotedAttrValueAsString  : arguments.handleUnquotedAttrValueAsString
			externalizeStringGTE=isNull(arguments.externalizeStringGTE) || isEmpty(arguments.externalizeStringGTE) ? existing.externalizeStringGTE  : arguments.externalizeStringGTE
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the compiler settings to its defaults.
	*/
	public void function resetCompilerSettings(){
		admin
			action="updateCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"

			nullSupport=""
			dotNotationUpperCase=""
			suppressWSBeforeArg=""
			handleUnquotedAttrValueAsString=""
			templateCharset=""
			externalizeStringGTE=""
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
	public void function updatePerformanceSettings( required string inspectTemplate, boolean typeChecking){
		var existing = getPerformanceSettings();
		admin
			action="updatePerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"

			inspectTemplate="#arguments.inspectTemplate#"
			typeChecking=isNull(arguments.typeChecking) || isEmpty(arguments.typeChecking) ? existing.typeChecking : arguments.typeChecking

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
	public void function updateGatewayEntry( required string id, required string startupMode, string class, string cfcPath, string listenerCfcPath,  struct custom ){
		var getGatewayEntries = getGatewayEntries();
		query name="existing" dbtype="query"{
			echo("SELECT * FROM getGatewayEntries WHERE id = '#arguments.id#' and startupMode = '#arguments.startupMode#' ")
		}
		admin
			action="updateGatewayEntry"
			type="#variables.type#"
			password="#variables.password#"

			id="#trim(arguments.id)#"
			startupMode="#trim(arguments.startupMode)#"
			class=isNull(arguments.class) || isEmpty(arguments.class) ? existing.class ?: "" : arguments.class
			cfcPath=isNull(arguments.cfcPath) || isEmpty(arguments.cfcPath) ? existing.cfcPath ?: "" : arguments.cfcPath
			listenerCfcPath=isNull(arguments.listenerCfcPath) || isEmpty(arguments.listenerCfcPath) ? existing.listenerCfcPath ?: "" : arguments.listenerCfcPath
			custom=isNull(arguments.custom) || isEmpty(arguments.custom) ? isEmpty(existing.custom) ? {} : existing.custom  : arguments.custom

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
	public void function updateDebugEntry( required string label, string type, string ipRange, struct custom ){
		// load available drivers
		var driverNames=structnew("linked");
		driverNames=ComponentListPackageAsStruct("lucee-server.admin.debug",driverNames);
		driverNames=ComponentListPackageAsStruct("lucee.admin.debug",driverNames);
		driverNames=ComponentListPackageAsStruct("debug",driverNames);


		var drivers={};
		loop collection="#driverNames#" index="local.n" item="local.fn"{
			if(n EQ "Debug" or n EQ "Field" or n EQ "Group" ){
				continue;
			}
			tmp=createObject('component',fn);
			drivers[trim(tmp.getId())]=tmp;
		}

		var driver=drivers[trim(arguments.type)];
		var meta=getMetaData(driver);
		var debugEntry = getDebugEntry();
		query name="existing" dbtype="query"{
			echo("SELECT * FROM debugEntry WHERE label = '#arguments.label#' ");
		}
		admin
			action="updateDebugEntry"
			type="#variables.type#"
			password="#variables.password#"

			label="#arguments.label#"
			debugtype=isNull(arguments.type) || isEmpty(arguments.type) ? (existing.type ?: "lucee-classic") : arguments.type
			iprange=isNull(arguments.iprange) || isEmpty(arguments.iprange) ? (existing.iprange ?: "*") : arguments.iprange
			fullname="#meta.fullName#"
			path="#contractPath(meta.path)#"
			custom=isNull(arguments.custom) || isEmpty(arguments.custom) ? isEmpty(existing.custom)? {} : (existing.custom ?: {bgcolor:"white", color:"black", font:"Times New Roman, Times, serif", general:"true", highlight:"250000", minimal:"0", scopes:"Application,CGI,Client,Cookie,Form,Request,Server,Session,URL", size:"medium"}) : arguments.custom

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
	public void function updateDebug( boolean debug, boolean database, boolean queryUsage, boolean exception, boolean tracing, boolean dump, boolean timer, boolean implicitAccess ){
		var existing = getDebug();
		admin
			action="updateDebug"
			type="#variables.type#"
			password="#variables.password#"

			debug=isNull(arguments.debug) || isEmpty(arguments.debug) ? existing.debug : arguments.debug
			database=isNull(arguments.database) || isEmpty(arguments.database) ? existing.database : arguments.database
			exception=isNull(arguments.exception) || isEmpty(arguments.exception) ? existing.exception : arguments.exception
			tracing=isNull(arguments.tracing) || isEmpty(arguments.tracing) ? existing.tracing : arguments.tracing
			dump=isNull(arguments.dump) || isEmpty(arguments.dump) ? existing.dump : arguments.dump
			timer=isNull(arguments.timer) || isEmpty(arguments.timer) ? existing.timer : arguments.timer
			implicitAccess=isNull(arguments.implicitAccess) || isEmpty(arguments.implicitAccess) ? existing.implicitAccess : arguments.implicitAccess
			queryUsage=isNull(arguments.queryUsage) || isEmpty(arguments.queryUsage) ? existing.queryUsage : arguments.queryUsage

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
	public void function updateSSLCertificate( required string host, numeric port = 443 ){
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
	* @destination specifies the destination filename
	*/
	public void function updateContext( required string source, required string destination ){
		admin
			action="updateContext"
			type="#variables.type#"
			password="#variables.password#"
			source="#arguments.source#"
			destination="/lucee/admin/resources/language/#arguments.destination#";
	}

	/**
	* @hints updates the label for a web context
	* @label new label for the web context.
	* @hash hash for the web context to be updated.
	*/
	public void function updateLabel( required string label, required string hash ){
		admin
			action="updateLabel"
			type="#variables.type#"
			password="#variables.password#"

			label="#arguments.label#"
			hash="#arguments.hash#";
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
	* @hint returns the list of tasks
	*/
	public query function getTasks(){
		admin
			action="getSpoolerTasks"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint executes the task of given id
	* @id specifies the id of task to execute
	*/
	public void function executeTask(required string id){
		admin
			action="executeSpoolerTask"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#";
	}

	/**
	* @hint removes the task of given id
	* @id specifies the id of task to remove
	*/
	public void function removeTask(required string id){
		admin
			action="removeSpoolerTask"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#";
	}

	public void function removeAllTask(){
		admin
			action="removeAllSpoolerTask"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint removes all the task
	*/
	public void function removeAllTasks(){
		admin
			action="removeAllSpoolerTask"
			type="#variables.type#"
			password="#variables.password#";
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
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hints resets task settings
	*/
	public void function resetTaskSetting(){
		admin
			action="updateTaskSetting"
			type="#variables.type#"
			password="#variables.password#"
			maxThreads=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint verifies whether it is CFX tag or not
	* @name specifies the name of the tag to verify
	*/
	public void function verifyCFX( required string name ){
		admin
			action="verifyCFX"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
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
	public void function verifyJavaCFX( required string name, required string class, string bundleName, string bundleVersion ){
		admin
			action="verifyJavaCFX"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			class="#arguments.class#"
			bundleName="#arguments.bundleName#"
			bundleVersion="#arguments.bundleVersion#";
	}

	/**
	* @hint updates the CPPCFX tags
	* @name specifies the name of the javaCFX tag
	* @class specifies the class of javaCFX tag to update
	*/
	public void function updatejavacfx(required string name, required string class){
		admin
			action="updatejavacfx"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			class="#arguments.class#";
	}

	/**
	* @hint removes the CFX tags
	* @name specifies the name of CFXtag
	*/
	public void function removecfx(required string name){
		admin
			action="removecfx"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			remoteClients="#variables.remoteClients#";
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
	public void function updateLoginSettings( boolean rememberMe, boolean captcha, numeric delay ){
		var existing = getLoginSettings();
		admin
			action="updateLoginSettings"
			type="#variables.type#"
			password="#variables.password#"
			rememberme=isNull(arguments.rememberme) || isEmpty(arguments.rememberme) ? existing.rememberme : arguments.rememberme
			captcha=isNull(arguments.captcha) || isEmpty(arguments.captcha) ? existing.captcha : arguments.captcha
			delay=isNull(arguments.delay) || isEmpty(arguments.delay) ? existing.delay : arguments.delay;
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
	* @hint updates the log settings
	* @appenderClass specifies the appender class to display the log
	* @layoutClass specifies the layout format to display
	* @name specifies the name of the log to update
	* @appenderArgs specifies the structure of appender class
	* @layoutArgs specifies the structure of layout class
	*/
	public void function updateLogSettings(
		  required string name
		,          string level
		,          string appenderClass
		,          string layoutClass
		,          struct appenderArgs={}
		,          struct layoutArgs={}
	){
		var LogSettings = getLogSettings();
		query name="existing" dbtype="query"{
			echo("SELECT * FROM LogSettings WHERE name = '#arguments.name#' ");
		}

		admin
			action="updateLogSettings"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			level=isNull(arguments.level) || isEmpty(arguments.level) ? existing.level : arguments.level
			appenderClass=isNull(arguments.appenderClass) || isEmpty(arguments.appenderClass) ? existing.appenderClass : arguments.appenderClass
			appenderArgs=isNull(arguments.appenderArgs) || isEmpty(arguments.appenderArgs) ? isEmpty(existing.appenderArgs) ? {} : existing.appenderArgs : arguments.appenderArgs
			layoutClass=isNull(arguments.layoutClass) || isEmpty(arguments.layoutClass) ? existing.layoutClass : arguments.layoutClass
			layoutArgs=isNull(arguments.layoutArgs) || isEmpty(arguments.layoutArgs) ? isEmpty(existing.layoutArgs) ? {} : existing.layoutArgs : arguments.layoutArgs;
	}

	/**
	* @hint removes the Log settings
	* @name specifies the name of the log to remove
	*/
	public void function removeLogSetting(required string name){
		admin
			action="removeLogSetting"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			remoteClients="#variables.remoteClients#";
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
	* @type specifies the type of listener to update
	* @mode specifies the mode of the listener
	*/
	public void function updateApplicationListener( string type, string mode ){
		var existing = getApplicationListener();
		admin
			action="updateApplicationListener"
			type="#variables.type#"
			password="#variables.password#"
			listenerType=isNull(arguments.type) || isEmpty(arguments.type) ? existing.type : arguments.type
			listenerMode=isNull(arguments.mode) || isEmpty(arguments.mode) ? existing.mode : arguments.mode
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets application listener
	*/
	public void function resetApplicationListener(){
		admin
			action="updateApplicationListener"
			type="#variables.type#"
			password="#variables.password#"
			listenerType=""
			listenerMode=""
			remoteClients="#variables.remoteClients#";
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
	public void function updateScope( string scopeCascadingType, boolean allowImplicidQueryCall, boolean mergeFormAndUrl, boolean sessionManagement, boolean clientManagement, boolean domainCookies, boolean clientCookies, timespan clientTimeout, timespan sessionTimeout, string clientStorage, string sessionStorage, timespan applicationTimeout, string sessionType, string localMode, boolean cgiReadonly ){
		var existing = getScope();
		admin
			action="updateScope"
			type="#variables.type#"
			password="#variables.password#"
			scopeCascadingType=isNull(arguments.scopeCascadingType) || isEmpty(arguments.scopeCascadingType) ? existing.scopeCascadingType : arguments.scopeCascadingType
			allowImplicidQueryCall=isNull(arguments.allowImplicidQueryCall) || isEmpty(arguments.allowImplicidQueryCall) ? existing.allowImplicidQueryCall : arguments.allowImplicidQueryCall
			mergeFormAndUrl=isNull(arguments.mergeFormAndUrl) || isEmpty(arguments.mergeFormAndUrl) ? existing.mergeFormAndUrl : arguments.mergeFormAndUrl
			sessionManagement=isNull(arguments.sessionManagement) || isEmpty(arguments.sessionManagement) ? existing.sessionManagement : arguments.sessionManagement
			clientManagement=isNull(arguments.clientManagement) || isEmpty(arguments.clientManagement) ? existing.clientManagement : arguments.clientManagement
			domainCookies=isNull(arguments.domainCookies) || isEmpty(arguments.domainCookies) ? existing.domainCookies : arguments.domainCookies
			clientCookies=isNull(arguments.clientCookies) || isEmpty(arguments.clientCookies) ? existing.clientCookies : arguments.clientCookies
			clientTimeout=isNull(arguments.clientTimeout) || isEmpty(arguments.clientTimeout) ? existing.clientTimeout : arguments.clientTimeout
			sessionTimeout=isNull(arguments.sessionTimeout) || isEmpty(arguments.sessionTimeout) ? existing.sessionTimeout : arguments.sessionTimeout
			clientStorage=isNull(arguments.clientStorage) || isEmpty(arguments.clientStorage) ? existing.clientStorage : arguments.clientStorage
			sessionStorage=isNull(arguments.sessionStorage) || isEmpty(arguments.sessionStorage) ? existing.sessionStorage : arguments.sessionStorage
			applicationTimeout=isNull(arguments.applicationTimeout) || isEmpty(arguments.applicationTimeout) ? existing.applicationTimeout : arguments.applicationTimeout
			sessionType=isNull(arguments.sessionType) || isEmpty(arguments.sessionType) ? existing.sessionType : arguments.sessionType
			localMode=isNull(arguments.localMode) || isEmpty(arguments.localMode) ? existing.localMode : arguments.localMode
			cgiReadonly=isNull(arguments.cgiReadonly) || isEmpty(arguments.cgiReadonly) ? existing.cgiReadonly : arguments.cgiReadonly
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint reset scope settings
	*/
	public void function resetScope(){
		admin
			action="updateScope"
			type="#variables.type#"
			password="#variables.password#"
			scopeCascadingType=""
			allowImplicidQueryCall=""
			mergeFormAndUrl=""
			sessionManagement=""
			clientManagement=""
			domainCookies=""
			clientCookies=""
			clientTimeout=""
			sessionTimeout=""
			clientStorage=""
			sessionStorage=""
			applicationTimeout=""
			sessionType=""
			localMode=""
			cgiReadonly=""
			remoteClients="#variables.remoteClients#";
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
	public void function updateRestSettings( boolean list=false ){
		admin
			action="updateRestSettings"
			type="#variables.type#"
			password="#variables.password#"
			list="#arguments.list#"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets rest mapping settings
	*/
	public void function resetRestSettings(){
		admin
			action="updateRestSettings"
			type="#variables.type#"
			password="#variables.password#"
			list=""
			remoteClients="#variables.remoteClients#";
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
	public void function updateRestMapping( required string virtual, required string physical, boolean default=false ){
		admin
			action="updateRestMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			default="#arguments.default#";
	}

	/**
	* @hint updates a rest mapping
	* @virtual virtual name for the rest mapping to be removed
	*/
	public void function removeRestMapping( required string virtual ){
		admin
			action="removeRestMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#";
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
	public void function updateApplicationSetting( timespan requestTimeout, string scriptProtect, boolean allowURLRequestTimeout ){
		var existing = getApplicationSetting();
		admin
			action="updateApplicationSetting"
			type="#variables.type#"
			password="#variables.password#"

			scriptProtect=isNull(arguments.scriptProtect) || isEmpty(arguments.scriptProtect) ? existing.scriptProtect : arguments.scriptProtect
			allowURLRequestTimeout=isNull(arguments.allowURLRequestTimeout) || isEmpty(arguments.allowURLRequestTimeout) ? existing.AllowURLRequestTimeout : arguments.allowURLRequestTimeout
			requestTimeout=isNull(arguments.requestTimeout) || isEmpty(arguments.requestTimeout) ? existing.requestTimeout : arguments.requestTimeout
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the common application settings
	*/
	public void function resetApplicationSetting(){
		admin
			action="updateApplicationSetting"
			type="#variables.type#"
			password="#variables.password#"

			scriptProtect=""
			allowURLRequestTimeout=""
			requestTimeout=""

			remoteClients="#variables.remoteClients#";
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
	public void function updateQueueSetting( numeric max, numeric timeout, boolean enable ){
		var existing = getQueueSetting();
		admin
			action="updateQueueSetting"
			type="#variables.type#"
			password="#variables.password#"

			max=isNull(arguments.max) || isEmpty(arguments.max) ? existing.max : arguments.max
			timeout=isNull(arguments.timeout) || isEmpty(arguments.timeout) ? existing.timeout : arguments.timeout
			enable=isNull(arguments.enable) || isEmpty(arguments.enable) ? existing.enable : arguments.enable

			remoteClients="#variables.remoteClients#";
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
	public void function updateCustomTagSetting( required boolean deepSearch, required boolean localSearch, required boolean customTagPathCache, required string extensions ) {
		var existing = getCustomTagSetting();
		admin
			action="updateCustomTagSetting"
			type="#variables.type#"
			password="#variables.password#"

			deepSearch=isNull(arguments.deepSearch) || isEmpty(arguments.deepSearch) ? existing.customTagDeepSearch : arguments.deepSearch
			localSearch=isNull(arguments.localSearch) || isEmpty(arguments.localSearch) ? existing.customTagLocalSearch : arguments.localSearch
			customTagPathCache=isNull(arguments.customTagPathCache) || isEmpty(arguments.customTagPathCache) ? existing.customTagPathCache : arguments.customTagPathCache
			extensions=isNull(arguments.extensions) || isEmpty(arguments.extensions) ? arrayToList(existing.extensions) : arguments.extensions
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint update exiting custom tag
	* @virtual The name is used as identifier when you automatically import a Lucee Archive build based on this Mapping.
	* @physical Directory path where the custom tags are located.
	* @archive File path to a custom tag Lucee Archive (.lar).
	* @primary Defines where Lucee looks first for a requested custom tags
	* @inspect When does Lucee checks for changes in the source file for an already loaded custom tags.
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
	* @virtual The name is used as identifier when you automatically import a Lucee Archive build based on this Mapping.
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
	* @hint update the details about error
	* @template500 specifies template that will be invoked in case of an error
	* @template404 specifies template that will be invoked in case of a missing error
	* @statuscode specifies status code to enable or not
	*/
	public void function updateError( string template500, string template404, boolean statuscode ){
		var existing = getError();
		admin
			action="updateError"
			type="#variables.type#"
			password="#variables.password#"
			template500=isNull(arguments.template500) || isEmpty(arguments.template500) ? (existing.str.500 ?: existing.templates.500) : arguments.template500
			template404=isNull(arguments.template404) || isEmpty(arguments.template404) ? (existing.str.404 ?: existing.templates.404) : arguments.template404
			statuscode=isNull(arguments.statuscode) || isEmpty(arguments.statuscode) ? existing.doStatusCode : arguments.statuscode
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hint resets the error template settings
	*/
	public void function resetError(){
		admin
			action="updateError"
			type="#variables.type#"
			password="#variables.password#"
			template500=""
			template404=""
			statuscode=""
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hints checks whether the current user has access
	* @secType area for which access needs to be checked
	* @secvalue specifies the sec value
	*/
	public boolean function securityManager( required string secType, string secvalue="" ){
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			secType="#arguments.secType#"
			secvalue="#arguments.secvalue#"
			returnVariable="access";

		return access;
	}

	/**
	* @hints checks whether the current user has access
	* @id id of the web context
	*/
	public void function createsecuritymanager( required string id ) {
		admin
			action="createSecurityManager"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#";
	}

	/**
	* @hints checks whether the current user has access
	* @id id of the web context
	*/
	public struct function getSecurityManager( required string id ) {
		admin
			action="getSecurityManager"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			returnVariable="access";

		return access;
	}

	/**
	* @hints Update a particular security manager
	* @id id of the web context
	* @setting The settings (regional,component and scope) can be changed in the "web administrator"
	* @file Defines how Lucee can interact with the local filesystem in a web context.
	* @direct_java_access Allows access to Java methods and properties from the Lucee code
	* @mail The mail settings can be changed in the "web administrator"
	* @datasource Defines how many datasources can be added in the "web administrator".
	* @mapping Allows adding, removing and updating of mappings in the "web administrator".
	* @remote It allows the settings in the administrator to be synchronized with other Lucee contexts
	* @custom_tag The custom tag settings can be changed in the "web administrator"
	* @cfx_setting The settings for the cfx tags can be changed. The globally defined CFX tags defined in the "server administrator" can be used as well.
	* @cfx_usage CFX tags one can load Java classes which might have full access to the local hosts system.
	* @debugging The debugging settings can be changed in the "web administrator"
	* @tag_execute This tag is used to execute a process on the local hosts system
	* @tag_import This tag can be used to import JSP and Lucee tag libraries
	* @tag_object The tag CFObject and the function CreateObject you can load Java objects. If disabled, you only can create objects of type "component"
	* @tag_registry The tag CFRegistry you have full access to the registry of the local hosts system
	* @cache The cache settings can be changed in the "web administrator"
	* @gateway The gateway settings can be changed in the "web administrator"
	* @orm The ORM settings can be changed in the "web administrator"
	* @access_read define the access for reading data
	* @access_write define the access for writing data
	* @search The search settings can be changed in the "web administrator"
	* @scheduled_task The scheduled task settings can be changed in the "web administrator"
	* @file_access define additional directories where file access is allowed
	*/
	public void function updateSecurityManager(
		required string  id,
				 boolean setting,
				 string  file,
				 boolean direct_java_access,
				 boolean mail,
				 string  datasource,
				 boolean mapping,
				 boolean remote,
				 boolean custom_tag,
				 boolean cfx_setting,
				 boolean cfx_usage,
				 boolean debugging,
				 boolean tag_execute,
				 boolean tag_import,
				 boolean tag_object,
				 boolean tag_registry,
				 boolean cache,
				 boolean gateway,
				 boolean orm,
				 string  access_read,
				 string  access_write,
		         boolean search,
		         boolean scheduled_task,
		         array   file_access ) {
		var existing = getSecurityManager( arguments.id );
		admin
			action="updateSecurityManager"
			type="#variables.type#"
			password="#variables.password#"

			id="#arguments.id#"
			setting=isNull(arguments.setting) || isEmpty(arguments.setting) ? existing.setting : arguments.setting
			file=isNull(arguments.file) || isEmpty(arguments.file) ? existing.file : arguments.file
			file_access=isNull(arguments.file_access) || isEmpty(arguments.file_access) ? existing.file_access : arguments.file_access
			direct_java_access=isNull(arguments.direct_java_access) || isEmpty(arguments.direct_java_access) ? existing.direct_java_access : arguments.direct_java_access
			mail=isNull(arguments.mail) || isEmpty(arguments.mail) ? existing.mail : arguments.mail
			datasource=isNull(arguments.datasource) || isEmpty(arguments.datasource) ? existing.datasource : arguments.datasource
			mapping=isNull(arguments.mapping) || isEmpty(arguments.mapping) ? existing.mapping : arguments.mapping
			remote=isNull(arguments.remote) || isEmpty(arguments.remote) ? existing.remote : arguments.remote
			custom_tag=isNull(arguments.custom_tag) || isEmpty(arguments.custom_tag) ? existing.custom_tag : arguments.custom_tag
			cfx_setting=isNull(arguments.cfx_setting) || isEmpty(arguments.cfx_setting) ? existing.cfx_setting : arguments.cfx_setting
			cfx_usage=isNull(arguments.cfx_usage) || isEmpty(arguments.cfx_usage) ? existing.cfx_usage : arguments.cfx_usage
			debugging=isNull(arguments.debugging) || isEmpty(arguments.debugging) ? existing.debugging : arguments.debugging
			search=isNull(arguments.search) || isEmpty(arguments.search) ? existing.search : arguments.search
			scheduled_task=isNull(arguments.scheduled_task) || isEmpty(arguments.scheduled_task) ? existing.scheduled_task : arguments.scheduled_task
			tag_execute=isNull(arguments.tag_execute) || isEmpty(arguments.tag_execute) ? existing.tag_execute : arguments.tag_execute
			tag_import=isNull(arguments.tag_import) || isEmpty(arguments.tag_import) ? existing.tag_import : arguments.tag_import
			tag_object=isNull(arguments.tag_object) || isEmpty(arguments.tag_object) ? existing.tag_object : arguments.tag_object
			tag_registry=isNull(arguments.tag_registry) || isEmpty(arguments.tag_registry) ? existing.tag_registry : arguments.tag_registry
			cache=isNull(arguments.cache) || isEmpty(arguments.cache) ? existing.cache : arguments.cache
			gateway=isNull(arguments.gateway) || isEmpty(arguments.gateway) ? existing.gateway : arguments.gateway
			orm=isNull(arguments.orm) || isEmpty(arguments.orm) ? existing.orm : arguments.orm
			access_read=isNull(arguments.access_read) || isEmpty(arguments.access_read) ? existing.access_read : arguments.access_read
			access_write=isNull(arguments.access_write) || isEmpty(arguments.access_write) ? existing.access_write : arguments.access_write;
	}

	/**
	* @hints To remove a security manager for a context
	* @id id of the web context
	*/
	public void function removeSecurityManager( required string id ) {
		admin
			action="removeSecurityManager"
			type="#variables.type#"
			password="#variables.password#"

			id="#arguments.id#";
	}

	/**
	* @hints To get default security manager for a context
	*/
	public struct function getDefaultSecurityManager() {
		admin
			action="getDefaultSecurityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="access";

		return access;
	}

	/**
	* @hints Update a particular security manager
	* @setting The settings (regional,component and scope) can be changed in the "web administrator"
	* @file Defines how Lucee can interact with the local filesystem in a web context.
	* @direct_java_access Allows access to Java methods and properties from the Lucee code
	* @mail The mail settings can be changed in the "web administrator"
	* @datasource Defines how many datasources can be added in the "web administrator".
	* @mapping Allows adding, removing and updating of mappings in the "web administrator".
	* @remote It allows the settings in the administrator to be synchronized with other Lucee contexts
	* @custom_tag The custom tag settings can be changed in the "web administrator"
	* @cfx_setting The settings for the cfx tags can be changed. The globally defined CFX tags defined in the "server administrator" can be used as well.
	* @cfx_usage CFX tags one can load Java classes which might have full access to the local hosts system.
	* @debugging The debugging settings can be changed in the "web administrator"
	* @tag_execute This tag is used to execute a process on the local hosts system
	* @tag_import This tag can be used to import JSP and Lucee tag libraries
	* @tag_object The tag CFObject and the function CreateObject you can load Java objects. If disabled, you only can create objects of type "component"
	* @tag_registry The tag CFRegistry you have full access to the registry of the local hosts system
	* @cache The cache settings can be changed in the "web administrator"
	* @gateway The gateway settings can be changed in the "web administrator"
	* @orm The ORM settings can be changed in the "web administrator"
	* @access_read define the access for reading data
	* @access_write define the access for writing data
	* @search The search settings can be changed in the "web administrator"
	* @scheduled_task The scheduled task settings can be changed in the "web administrator"
	* @file_access define additional directories where file access is allowed
	*/
	public void function updateDefaultSecurityManager(
		boolean setting,
		string  file,
		boolean direct_java_access,
		boolean mail,
		string  datasource,
		boolean mapping,
		boolean remote,
		boolean custom_tag,
		boolean cfx_setting,
		boolean cfx_usage,
		boolean debugging,
		boolean tag_execute,
		boolean tag_import,
		boolean tag_object,
		boolean tag_registry,
		boolean cache,
		boolean gateway,
		boolean orm,
		string  access_read,
		string  access_write,
		boolean search,
		boolean scheduled_task,
		array   file_access ) {
		var existing = getDefaultSecurityManager();
		admin
			action="updateDefaultSecurityManager"
			type="#variables.type#"
			password="#variables.password#"

			setting=isNull(arguments.setting) || isEmpty(arguments.setting) ? existing.setting : arguments.setting
			file=isNull(arguments.file) || isEmpty(arguments.file) ? existing.file : arguments.file
			file_access=isNull(arguments.file_access) || isEmpty(arguments.file_access) ? existing.file_access : arguments.file_access
			direct_java_access=isNull(arguments.direct_java_access) || isEmpty(arguments.direct_java_access) ? existing.direct_java_access : arguments.direct_java_access
			mail=isNull(arguments.mail) || isEmpty(arguments.mail) ? existing.mail : arguments.mail
			datasource=isNull(arguments.datasource) || isEmpty(arguments.datasource) ? existing.datasource : arguments.datasource
			mapping=isNull(arguments.mapping) || isEmpty(arguments.mapping) ? existing.mapping : arguments.mapping
			remote=isNull(arguments.remote) || isEmpty(arguments.remote) ? existing.remote : arguments.remote
			custom_tag=isNull(arguments.custom_tag) || isEmpty(arguments.custom_tag) ? existing.custom_tag : arguments.custom_tag
			cfx_setting=isNull(arguments.cfx_setting) || isEmpty(arguments.cfx_setting) ? existing.cfx_setting : arguments.cfx_setting
			cfx_usage=isNull(arguments.cfx_usage) || isEmpty(arguments.cfx_usage) ? existing.cfx_usage : arguments.cfx_usage
			debugging=isNull(arguments.debugging) || isEmpty(arguments.debugging) ? existing.debugging : arguments.debugging
			search=isNull(arguments.search) || isEmpty(arguments.search) ? existing.search : arguments.search
			scheduled_task=isNull(arguments.scheduled_task) || isEmpty(arguments.scheduled_task) ? existing.scheduled_task : arguments.scheduled_task
			tag_execute=isNull(arguments.tag_execute) || isEmpty(arguments.tag_execute) ? existing.tag_execute : arguments.tag_execute
			tag_import=isNull(arguments.tag_import) || isEmpty(arguments.tag_import) ? existing.tag_import : arguments.tag_import
			tag_object=isNull(arguments.tag_object) || isEmpty(arguments.tag_object) ? existing.tag_object : arguments.tag_object
			tag_registry=isNull(arguments.tag_registry) || isEmpty(arguments.tag_registry) ? existing.tag_registry : arguments.tag_registry
			cache=isNull(arguments.cache) || isEmpty(arguments.cache) ? existing.cache : arguments.cache
			gateway=isNull(arguments.gateway) || isEmpty(arguments.gateway) ? existing.gateway : arguments.gateway
			orm=isNull(arguments.orm) || isEmpty(arguments.orm) ? existing.orm : arguments.orm
			access_read=isNull(arguments.access_read) || isEmpty(arguments.access_read) ? existing.access_read : arguments.access_read
			access_write=isNull(arguments.access_write) || isEmpty(arguments.access_write) ? existing.access_write : arguments.access_write;
	}

	/**
	* @hint returns information about storage details
	* @key specifies key name to get storage details
	*/
	public string function storageGet( required string key ){
		admin
			action="storageGet"
			type="#variables.type#"
			password="#variables.password#"
			key="#arguments.key#"
			returnVariable="local.rtn";
			return local.rtn;
	}

	/**
	* @hint update the storage details
	* @key specifies key name to update 
	* @value value to update for the specify key
	*/
	public void function storageSet( required string key, required string value ){
		admin
			action="storageSet"
			type="#variables.type#"
			password="#variables.password#"
			key="#arguments.key#"
			value="#arguments.value#";
	}

	/**
	* @hint returns API key
	*/
	public string function getAPIKey(){
		admin
			action="getAPIKey"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
		// if(isNull(local.rtn))
		// 	return;
		return local.rtn;
	}

	/**
	* @hint updates the API key
	* @key key to update
	*/
	public void function updateAPIKey( required string key ){
		admin
			action="updateAPIKey"
			type="#variables.type#"
			password="#variables.password#"
			key="#arguments.key#";
	}

	/**
	* @hint removes the API key
	*/
	public void function removeAPIKey(){
		admin
			action="removeAPIKey"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hints create component archive for the mappings
	* @virtual specifies the virtual name of the mapping to create archive
	* @file specifies the path of the file
	* @addCFMLFile Add all CFML Source Templates as well (.cfm,.cfc,.cfml).
	* @addNonCFMLFile Add all Non CFML Source Templates as well (.js,.css,.gif,.png ...)
	*/
	public void function createComponentArchive( required string virtual, required string file, required boolean addCFMLFile, required boolean addNonCFMLFile ){
		admin
			action="createComponentArchive"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			file="#arguments.file#"
			addCFMLFiles="#arguments.addCFMLFile#"
			addNonCFMLFiles="#arguments.addNonCFMLFile#"
			append="true"
			remoteClients="#variables.remoteClients#";
	}

	/**
	* @hints create CTarchive for the mappings
	* @virtual specifies the virtual name of the mapping to create archive
	* @file specifies the path of the file
	* @addCFMLFile Add all CFML Source Templates as well (.cfm,.cfc,.cfml).
	* @addNonCFMLFile Add all Non CFML Source Templates as well (.js,.css,.gif,.png ...)
	*/
	public void function createCTArchive( required string virtual, required string file, required boolean addCFMLFile, required boolean addNonCFMLFile ){
		admin
			action="createCTArchive"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			file="#arguments.file#"
			addCFMLFiles="#arguments.addCFMLFile#"
			addNonCFMLFiles="#arguments.addNonCFMLFile#"
			append="true"
			remoteClients="#variables.remoteClients#";
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
	* @hint updates the update details
	* @type specifies the type of the update
	* @location specifies the location to update
	*/
	public void function updateUpdate(required string type, required string location){
		admin
			action="updateUpdate"
			type="#variables.type#"
			password="#variables.password#"
			updatetype="#arguments.type#"
			updatelocation="#arguments.location#";
	}

	/**
	* @hint executes and run the update details
	*/
	public void function runUpdate(){
		admin
			action="runUpdate"
			type="#variables.type#"
			password="#variables.password#"
			remoteClients="#variables.remoteClients#";
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
	* @hint change the current version to specific version
	*/
	public void function changeVersionTo( required string version ){
		admin
			action="changeVersionTo"
			type="#variables.type#"
			password="#variables.password#"
			version="#arguments.version#";
	}

	/**
	* @hint reset the Security Key( In case this server is to be synchronized by another server, you have to enter the security key below in the distant definition of the remote client. )
	*/
	public void function resetId(){
		admin
			action="resetId"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint Restart the Lucee engine.
	*/
	public void function restart(){
		admin
			action="restart"
			type="#variables.type#"
			password="#variables.password#";
	}

	/**
	* @hint returns the list of min versions available
	*/
	public string function getMinVersion(){
		admin
			action="getMinVersion"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint restart the list of patches available
	*/
	public array function listPatches(){
		admin
			action="listPatches"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	/**
	* @hint specifies the access of admin
	*/
	public void function connect(){
		admin
			action="connect"
			type="#variables.type#"
			password="#variables.password#";
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

	private void function downloadFile( required string target ){
		var filename = listLast(listLast(arguments.target, "/"), "\");
		HEADER NAME="Content-Disposition" VALUE="inline; filename=#filename#"; content file="#arguments.target#" deletefile="yes" type="application/unknow";
	}
}