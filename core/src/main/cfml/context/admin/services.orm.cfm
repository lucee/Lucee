<cfset error.message="">
<cfset error.detail="">
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">

<cfadmin 
	action="securityManager"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="hasAccess"
	secType="orm"
	secValue="yes">
	
<cfadmin 
	action="getORMSetting"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="settings">
<cfadmin 
	action="getORMEngine"
	type="#request.adminType#"
	password="#session["password"&request.adminType]#"
	returnVariable="engine">
	

<cfinclude template="services.orm.list.cfm"/>