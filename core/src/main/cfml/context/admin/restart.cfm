<!--- create no output here!!! --->
<cfsetting showdebugoutput="false">
<cfadmin 
	action="restart"
	type="#url.adminType#"
	password="#session["password"&url.adminType]#">
	
