

component	{
	
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
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="setting"
			secValue="yes";
		if(local.hasAccess){
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
	}

	/**
	* @hint resets output settings for this context
	*/
	public void function resetOutputSetting() {
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="setting"
			secValue="yes";
		if(local.hasAccess){
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
	* @hint updates the Preserve single quotes setting from datasource page
	* @psq an argument for psq enabled or not
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
	* @allowed_select allow database permission for select
	* @allowed_insert allow database permission for insert
	* @allowed_update allow database permission for update
	* @allowed_delete allow database permission for delete
	* @allowed_alter allow database permission for alter
	* @allowed_drop allow database permission for drop
	* @allowed_revoke allow database permission for revoke
	* @allowed_create allow database permission for create
	* @allowed_grant allow database permission for grant
	* @storage Allow to use this datasource as client/session storage.
	* @custom_useUnicode Should the driver use Unicode character encodings when handling strings?
	* @custom_characterEncoding Should only be used when the driver can't determine the character set mapping, or you are trying to 'force' the driver to use a character set that MySQL either doesn't natively support (such as UTF-8)If it is set to true, what character encoding should the driver use when dealing with strings?
	* @custom_useOldAliasMetadataBehavior Should the driver use the legacy behavior for "AS" clauses on columns and tables, and only return aliases (if any) rather than the original column/table name? In 5.0.x, the default value was true.
	* @custom_allowMultiQueries Allow the use of ";" to delimit multiple queries during one statement
	* @custom_zeroDateTimeBehavior What should happen when the driver encounters DATETIME values that are composed entirely of zeroes (used by MySQL to represent invalid dates)? Valid values are "exception", "round" and "convertToNull"
	* @custom_autoReconnect Should the driver try to re-establish stale and/or dead connections?
	* @custom_jdbcCompliantTruncation If set to false then values for table fields are automatically truncated so that they fit into the field.
	* @custom_tinyInt1isBit if set to "true" (default) tinyInt(1) is converted to a bit value otherwise as integer.
	* @custom_useLegacyDatetimeCode Use code for DATE/TIME/DATETIME/TIMESTAMP handling in result sets and statements
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

		boolean allowed_select=false,
		boolean allowed_insert=false,
		boolean allowed_update=false,
		boolean allowed_delete=false,
		boolean allowed_alter=false,
		boolean allowed_drop=false,
		boolean allowed_revoke=false,
		boolean allowed_create=false,
		boolean allowed_grant=false,

		boolean custom_useUnicode=false,
		string custom_characterEncoding=false,
		boolean custom_useOldAliasMetadataBehavior=false,
		boolean custom_allowMultiQueries=false,
		string custom_zeroDateTimeBehavior=false,
		boolean custom_autoReconnect=false,
		boolean custom_jdbcCompliantTruncation=false,
		boolean custom_tinyInt1isBit=false,
		boolean custom_useLegacyDatetimeCode=false
	){

		var driverNames=structnew("linked");
		driverNames=ComponentListPackageAsStruct("lucee-server.admin.dbdriver",driverNames);
		driverNames=ComponentListPackageAsStruct("lucee.admin.dbdriver",driverNames);
		driverNames=ComponentListPackageAsStruct("dbdriver",driverNames);

		var driver=createObject("component", drivernames[ arguments.type ]);
		var custom=structNew();
		loop collection="#arguments#" item="key"{
			if(findNoCase("custom_",key) EQ 1){
				l=len(key);
				custom[mid(key,8,l-8+1)]=arguments[key];
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

			allowed_select="#getArguments('allowed_select',false)#"
			allowed_insert="#getArguments('allowed_insert',false)#"
			allowed_update="#getArguments('allowed_update',false)#"
			allowed_delete="#getArguments('allowed_delete',false)#"
			allowed_alter="#getArguments('allowed_alter',false)#"
			allowed_drop="#getArguments('allowed_drop',false)#"
			allowed_revoke="#getArguments('allowed_revoke',false)#"
			allowed_create="#getArguments('allowed_create',false)#"
			allowed_grant="#getArguments('allowed_grant',false)#"
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
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="mail"
			secValue="yes";
		if(local.hasAccess){
			var ms = getMailservers();
			admin
				action="updateMailServer"
				type="#variables.type#"
				password="#variables.password#"

				hostname="#arguments.host#"
				dbusername="#arguments.username#"
				dbpassword="#toPassword(arguments.host,arguments.password, ms)#"
				life="#arguments.life#"
				idle="#arguments.idle#"

				port="#arguments.port#"
				id="new"
				tls="#arguments.tls#"
				ssl="#arguments.ssl#"
				remoteClients="#variables.remoteClients#";
		}
	}

	/**
	* @hint removes a specific mailserver defined for this context.
	* @host hostname for the mail server to be removed.
	* @username username of the mail server to be removed.
	*/
	public void function removeMailServer(){
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="mail"
			secValue="yes";
		if(local.hasAccess){
			admin
				action="removeMailServer"
				type="#variables.type#"
				password="#variables.password#"

				hostname="#arguments.host#"
				username="#arguments.username#"
				remoteClients="#variables.remoteClients#";
		}
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
	public void function updateMailSetting(){
		admin
			action="updateMailSetting"
			type="#variables.type#"
			password="#variables.password#"

			spoolEnable="#arguments.spoolenable#"
			timeout="#arguments.timeout#"
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
	* @hint updates/inserts a specific mapping for this context
	* @virtual virtual name for the mapping
	* @physical physical path for the mapping
	* @archive archive path for the mapping, if needed.
	* @primary type of mapping ( physical/archive )
	* @inspect type of inspection for the mapping(never/once/always/"").
	* @toplevel 
	*/
	public void function updateMapping(required string virtual, required string physical, required string archive, required string primary, required string inspect, required boolean toplevel) {
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="mapping"
			secValue="yes";
		if(local.hasAccess){
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
	}

	/**
	* @hint removes a mapping defined in this context
	* @virtual virtual name for the mapping to be removed.
	*/
	public void function removeMapping(required string virtual){
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="mapping"
			secValue="yes";
		if(local.hasAccess){
			admin
				action="removeMapping"
				type="#variables.type#"
				password="#variables.password#"

				virtual="#arguments.virtual#"
				remoteClients="#variables.remoteClients#";
		}
	}

	/**
	* @hint compiles the mapping for any errors
	* @virtual virtual name for the mapping to be compiled.
	*/
	public void function compileMapping(required string virtual){
		admin
			action="securityManager"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.hasAccess"
			secType="mapping"
			secValue="yes";
		if(local.hasAccess){
			admin
				action="compileMapping"
				type="#variables.type#"
				password="#variables.password#"

				virtual="#arguments.virtual#"
				stoponerror="yes"
				remoteClients="#variables.remoteClients#";
		}
	}

	/**
	* @hint creates new archieve for the mapping
	* @virtual virtual name for the mapping.
	* @addCFMLFile Add all CFML Source Templates as well (.cfm,.cfc,.cfml).
	* @addNonCFMLFile Add all Non CFML Source Templates as well (.js,.css,.gif,.png ...)
	* @doDownload Whether need to download the archive or not.
	*/
	public void function createArchiveFromMapping(required string virtual, required boolean addCFMLFile, required boolean addNonCFMLFile, required boolean doDownload){
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

			file="#target#"
			virtual="#arguments.virtual#"
			addCFMLFiles="#arguments.addCFMLFile#"
			addNonCFMLFiles="#arguments.addNonCFMLFile#"
			append="#not arguments.doDownload#"
			remoteClients="#variables.remoteClients#";
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

	//@getInfo()
 
	public query function getInfo(){
		admin
			action="getinfo"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}


	//@surveillance()
	public query function surveillance(){
		admin
			action="surveillance"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	
	// is MonitorEnabled

	public boolean function isMonitorEnabled(){
		admin
			action="isMonitorEnabled"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	public void function resetORMSetting(){
		admin
			action="resetORMSetting"
			type="#variables.type#"
			password="#variables.password#"
	}

	//getORMSetting 
	public query function getORMSetting(){
		admin
			action="getORMSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getORMEngine 

	public struct function getORMEngine(){
		admin
			action="getORMEngine"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getApplicationListener

	public struct function getApplicationListener(){
		admin
			action="getApplicationListener"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getProxy

	public struct function getProxy(){
		admin
			action="getProxy"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}
	
	//getComponent

	public Struct function getComponent(){
		admin
			action="getComponent"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getScope

	public struct function getScope(){
		admin
			action="getScope"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getApplicationSetting	

	public struct function getApplicationSetting(){
		admin
			action="getApplicationSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getQueueSetting

	public struct function getQueueSetting(){
		admin
			action="getQueueSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getCustomTagSetting

	public struct function getCustomTagSetting(){
		admin
			action="getCustomTagSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getDatasource 

	public struct function getDatasource( required string name ){
		admin
			action="getDatasource"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			returnVariable="local.rtn";
			return rtn;
	}


	//getJDBCDrivers 

	public query function getJDBCDrivers(){
		admin
			action="getJDBCDrivers"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getCacheConnections

	public query function getCacheConnections(){
		admin
			action="getCacheConnections"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getCacheConnection 

	public query function getCacheConnection( required string name ){
		admin
			action="getCacheConnection"
			type="#variables.type#"
			password="#variables.password#"
			name = "#arguments.name#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getCacheDefaultConnection

	public struct function getCacheDefaultConnection(){
		admin
			action="getCacheDefaultConnection"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getRemoteClients

	public query function getRemoteClients(){
		admin
			action="getRemoteClients"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getRemoteClient

	public query function getRemoteClient( required string url ){
		admin
			action="getRemoteClient"
			type="#variables.type#"
			password="#variables.password#"
			url = "#trim(arguments.url)#"
			returnVariable="local.rtn";
			return rtn;
	}

	//hasRemoteClientUsage

	public boolean function hasRemoteClientUsage(){
		admin
			action="hasRemoteClientUsage"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getRemoteClientUsage

	public query function getRemoteClientUsage(){
		admin
			action="getRemoteClientUsage"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getSpoolerTasks

	public query function getSpoolerTasks(){
		admin
			action="getSpoolerTasks"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}


	//getPerformanceSettings

	public struct function getPerformanceSettings(){
		admin
			action="getPerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getLogSettings

	public query function getLogSettings(){
		admin
			action="getLogSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getCompilerSettings 

	public struct function getCompilerSettings(){
		admin
			action="getCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//updatePerformanceSettings

	
	public query function updatePerformanceSettings( required string template, boolean typeChecking=false){
		admin

			action="updatePerformanceSettings"
			type="#variables.type#"
			password="#variables.password#"
			typeChecking="#getArguments('typeChecking',false)#"
			inspectTemplate="#arguments.template#";
	}

	//updateCompilerSettings


	
	public query function updateCompilerSettings(){
		admin
			action="updateCompilerSettings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getGatewayentries 

	public query function getGatewayentries( type ){
		admin
			action="getGatewayentries"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getGatewayentry
	
	public struct function getGatewayentry( required string id){
		admin
			action="getGatewayentry"
			type="#variables.type#"
			password="#variables.password#"
			id = "#arguments.id#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getRunningThreads

	public query function getRunningThreads(){
		admin
			action="getRunningThreads"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getMonitors

	public query function getMonitors(){
		admin
			action="getMonitors"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getMonitor

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

	//getBundles

	public query function getBundles(){
		admin
			action="getBundles"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}


	//getBundle
	
	public strct function getBundle( required string name ){
		admin
			action="getBundle"
			type="#variables.type#"
			password="#variables.password#"
			symbolicName = "#arguments.name#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getExecutionLog


	public strct function getExecutionLog(){
		admin
			action="getExecutionLog"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//gateway
	
	public strct function gateway( required string id, required string gatewayAction ){
		admin
			action="gateway"
			type="#variables.type#"
			password="#variables.password#"
			id="#arguments.id#"
			gatewayAction="#arguments.gatewayAction#"
			returnVariable="local.rtn";
			return rtn;
	}


	//alias for getSpoolerTasks

	//getRemoteClientTasks 

	public query function getRemoteClientTasks(){
		admin
			action="getRemoteClientTasks"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getDatasourceDriverList

	public query function getDatasourceDriverList(){
		admin
			action="getDatasourceDriverList"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getDebuggingList
	
	public query function getDebuggingList(){
		admin
			action="getDebuggingList"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getDebugSetting

	public struct function getDebugSetting(){
		admin
			action="getDebugSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getSSLCertificate
	
	public struct function getSSLCertificate( required string host ){
		admin
			action="getSSLCertificate"
			type="#variables.type#"
			password="#variables.password#"
			host="#arguments.host#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getPluginDirectory

	public string function getPluginDirectory(){
		admin
			action="getPluginDirectory"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	//getPlugins

	public query function getPlugins(){
		admin
			action="getPlugins"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
	}

	
	public struct function updatePlugin( required string source ){
		admin
			action="updatePlugin"
			type="#variables.type#"
			password="#variables.password#"
			source="#arguments.source#";
	}
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

	//getJars

	public query function getJars(){
		admin
			action="getJars"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
			
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

	public query function getLocalExtension( required string id  ){
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


	//getTaskSetting

	public struct function getTaskSetting(){
		admin
			action="getTaskSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.rtn";
			return rtn;
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

	//getCustomTagMappings

	public query function getCustomTagMappings(){
		admin
			action="getCustomTagMappings"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
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

	//getDebugEntry

	public query function getDebugEntry(){
		admin
			action="getDebugEntry"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.providers";
			return providers;
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

	//verifyremoteclient

	public void function verifyremoteclient( required string label, required string url, required string adminPassword, required string securityKey, required string usage ){
		admin
			action="verifyremoteclient"
			type="#variables.type#"
			password="#variables.password#"
			label="#arguments.label#"
			url="#arguments.url#"
			adminPassword="#arguments.adminPassword#"
			securityKey="#arguments.securityKey#"
			usage="#arguments.usage#";
	}

	//verifyDatasource

	public void function verifyDatasource( required string name, required string dbusername, required string dbpassword ){
		admin
			action="verifyDatasource"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			dbusername="#arguments.dbusername#"
			dbpassword="#arguments.dbpassword#";
	}

	//verifyCacheConnection

	public void function verifyCacheConnection( required string name ){
		admin
			action="verifyCacheConnection"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
	}

	//verifyMailServer

	public struct function verifyMailServer( required string hostname, required string port, required string mailusername, required string mailpassword ){
		admin
			action="verifyMailServer"
			type="#variables.type#"
			password="#variables.password#"
			hostname="#arguments.hostname#"
			port="#arguments.port#"
			mailusername="#arguments.mailusername#"
			mailpassword="#arguments.mailpassword#";
	}

	//verifyExtensionProvider

	public void function verifyExtensionProvider( required string url ){
		admin
			action="verifyExtensionProvider"
			type="#variables.type#"
			password="#variables.password#"
			url="#arguments.url#"
	}

	//verifyJavaCFX

	public query function verifyJavaCFX( required string name, required string class ){
		admin
			action="verifyJavaCFX"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#"
			class="#arguments.class#";
	}

	//verifyCFX

	public void function verifyCFX( required string name ){
		admin
			action="verifyCFX"
			type="#variables.type#"
			password="#variables.password#"
			name="#arguments.name#";
	}

	//resetId

	public void function resetId(){
		admin
			action="resetId"
			type="#variables.type#"
			password="#variables.password#";
	}

	//updateLoginSettings

	public void function updateLoginSettings( boolean rememberme=false, boolean captcha=false, numeric delay=0  ){
		admin
			action="updateLoginSettings"
			type="#variables.type#"
			password="#variables.password#"
			rememberme="#arguments.rememberme#"
			captcha="#arguments.captcha#"
			delay="#arguments.delay#";
	}

	//updateLogSettings

	public void function updateLogSettings( required string level, required string appenderClass, required string layoutClass, required string name, struct appenderArgs={}, struct layoutArgs={} ){
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

	//updateMonitorEnabled

	public query function updateMonitorEnabled(){
		admin
			action="updateMonitorEnabled"
			type="#variables.type#"
			password="#variables.password#"
	}

	//updateTLD

	public query function updateTLD(){
		admin
			action="updateTLD"
			type="#variables.type#"
			password="#variables.password#"
	}

	//updateFLD

	public void function updateFLD(){
		admin
			action="updateFLD"
			type="#variables.type#"
			password="#variables.password#"
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

	//updateCachedWithin->
	
	public void function updateCachedWithin( required string cachedWithinType, required string cachedWithin ){
		admin
			action="updateCachedWithin"
			type="#variables.type#"
			password="#variables.password#"
			cachedWithinType="#arguments.cachedWithinType#"
			cachedWithin="#arguments.cachedWithin#";
	}

	//updateproxy

	public void function updateproxy( boolean proxyenabled=false, string proxyserver="testProxy", numeric proxyport=443, string proxyusername="admin", string proxypassword="password" ){
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

	//updateCharset

	public query function updateCharset( string resourceCharset, string templateCharset, string webCharset ){
		admin
			action="updateCharset"
			type="#variables.type#"
			password="#variables.password#"
			resourceCharset="#arguments.resourceCharset#"
			templateCharset="#arguments.templateCharset#"
			webCharset="#arguments.webCharset#";
	}

	//updatecomponent

	public query function updatecomponent(){
		admin
			action="updatecomponent"
			type="#variables.type#"
			password="#variables.password#"
	}

	//updatescope

	public query function updatescope( required string scopeCascadingType, required boolean allowImplicidQueryCall, required boolean mergeFormAndUrl, required boolean sessionManagement, required boolean clientManagement, required boolean domainCookies, required boolean clientCookies, required date clientTimeout, required date sessionTimeout, required string clientStorage, required string sessionStorage, required date applicationTimeout, required string sessionType, required string localMode, required boolean cgiReadonly ){
		admin
			action="updatescope"
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
	}

	//updateRestSettings

	public void function updateRestSettings(){
		admin
			action="updateRestSettings"
			type="#variables.type#"
			password="#variables.password#"
	}

	//updateRestMapping

	public void function updateRestMapping( required string virtual, required string physical, required string default ){
		admin
			action="updateRestMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#"
			physical="#arguments.physical#"
			default="#arguments.default#";
	}

	//removeRestMapping

	public void function removeRestMapping( required string virtual ){
		admin
			action="removeRestMapping"
			type="#variables.type#"
			password="#variables.password#"
			virtual="#arguments.virtual#";
	}

	//updateApplicationSetting

	public void function updateApplicationSetting( required date requestTimeout, required string scriptProtect, required boolean allowURLRequestTimeout ){
		admin
			action="updateApplicationSetting"
			type="#variables.type#"
			password="#variables.password#"
			requestTimeout="#arguments.requestTimeout#"
			scriptProtect="#arguments.scriptProtect#"
			allowURLRequestTimeout="#arguments.allowURLRequestTimeout#";
	}

	//updateOutputSetting

	public void function updateOutputSetting( required string cfmlWriter, required boolean suppressContent, required boolean allowCompression, required string contentLength, required string bufferOutput ){
		admin
			action="updateOutputSetting"
			type="#variables.type#"
			password="#variables.password#"
			cfmlWriter="#arguments.cfmlWriter#"
			suppressContent="#arguments.suppressContent#"
			allowCompression="#arguments.allowCompression#"
			contentLength="#arguments.contentLength#"
			bufferOutput="#arguments.bufferOutput#";
	}	

	//updateQueueSetting

	public void function updateQueueSetting( required numeric max, required numeric timeout, required boolean enable ){
		admin
			action="updateQueueSetting"
			type="#variables.type#"
			password="#variables.password#"
			max="#arguments.max#"
			timeout="#arguments.timeout#"
			enable="#arguments.enable#";
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

	//updateCacheDefaultConnection-> doUpdateCacheDefaultConnection(); need to check

	public void function updateCacheDefaultConnection( string object, string template, string query, string resource, string function, string include, string http, string file, string webservice ){
		admin
			action="updateRestSettings"
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
			webservice="#arguments.webservice#";
	}

	//updateCacheConnection

	public void function updateCacheConnection( required string class, required string name, required struct custom, string bundleName, string bundleVersion, boolean readonly=false, boolean storage=false ){
		admin
			action="updateCacheConnection"
			type="#variables.type#"
			password="#variables.password#"
			class="#arguments.class#"
			name="#arguments.name#"
			custom="#arguments.custom#"
			bundleName="#arguments.bundleName#"
			bundleVersion="#arguments.bundleVersion#"
			readonly="#arguments.readonly#"
			storage="#arguments.storage#";
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

	private function toPassword(host, pw, ms){
		variables.stars = "*********";
		var i=1;
		if(arguments.pw EQ variables.stars){
			for(i=arguments.ms.recordcount;i>0;i--){
				if(arguments.host EQ arguments.ms.hostname[i])
					return arguments.ms.password[i];
			}
		}
		return arguments.pw;
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
}
