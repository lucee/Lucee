

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
	public any function resetOutputSetting() {
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

				remoteClients="#request.getRemoteClients()#";
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
	public boolean function getDatasourceSetting() {
		admin
			action="getDatasourceSetting"
			type="#variables.type#"
			password="#variables.password#"
			returnVariable="local.dbSetting";

		return local.dbSetting;
	}
}