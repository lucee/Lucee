<cfcomponent>
	<cfset this.name = hash( getCurrentTemplatePath() )>
	<cfset request.baseURL="http://#cgi.HTTP_HOST##GetDirectoryFromPath(cgi.SCRIPT_NAME)#">
	<cfset request.currentPath=GetDirectoryFromPath(getCurrentTemplatePath())>

	<cfset this.cache.connections["test"] = {
		class: 'lucee.runtime.cache.ram.RamCache'
		, storage: false
		, custom: {'timeToIdleSeconds':'0','timeToLiveSeconds':'0'}
		, default: ''
	}>

	<cfset this.cache["test1"] = {
		class: 'lucee.runtime.cache.ram.RamCache'
		, storage: false
		, custom: {'timeToIdleSeconds':'0','timeToLiveSeconds':'0'}
		, default: ''
	}>
<cfscript>
	public function onRequestStart() {
		setting requesttimeout=10;
	}
</cfscript>
</cfcomponent>