<cfif request.admintype EQ "server"><cflocation url="#request.self#" addtoken="no"></cfif>

<cfset error.message="">
<cfset error.detail="">
<cfif request.adminType EQ "web">
<!--- 
Defaults --->
<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">
<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="services.schedule.list.cfm"/></cfcase>
	<cfcase value="edit"><cfinclude template="services.schedule.edit.cfm"/></cfcase>
	<cfcase value="create,#stText.Buttons.Create#"><cfinclude template="services.schedule.create.cfm"/></cfcase>
</cfswitch>
</cfif>