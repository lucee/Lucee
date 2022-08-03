<cfcomponent hint="Parent Application">	
	<cfset this.name = "LDEV-3600">
	<cfset this.test = "this scope was available">
<cfscript>
	public function onRequestStart() {
		setting requesttimeout=10;
	}
</cfscript>
</cfcomponent>