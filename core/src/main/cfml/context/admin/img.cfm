<cfif thistag.executionmode EQ "start"><cfsilent>

 <cfif (attributes.src CT "..")>
 	<cfthrow type="InvalidParam" message="[#attributes.src#] is not a valid parameter">
 </cfif>
<cfset width="">
<cfif isDefined("attributes.width")><cfset width=" width="""&attributes.width&""""></cfif>
<cfset height="">
<cfif isDefined("attributes.height")><cfset height=" height="""&attributes.height&""""></cfif>
<cfset title="">
<cfif isDefined("attributes.title")><cfset title=" title="""&attributes.title&""""></cfif>
<cfset hspace="">
<cfif isDefined("attributes.hspace")><cfset hspace=" hspace="""&attributes.hspace&""""></cfif>
<cfset vspace="">
<cfif isDefined("attributes.vspace")><cfset vspace=" vspace="""&attributes.vspace&""""></cfif>
<cfset valign="">
<cfif isDefined("attributes.valign")><cfset valign=" valign="""&attributes.valign&""""></cfif>
<cfset style="">
<cfif isDefined("attributes.style")><cfset style=" style="""&attributes.style&""""></cfif>
<cfset name="">
<cfif isDefined("attributes.name")><cfset name=" name="""&attributes.name&""""></cfif>

<cfparam name="attributes.border" default="0">
<cfparam name="attributes.type" default="img">


<cfset path="resources/img/#attributes.src#.cfm">
<cfparam name="application.adminimages" default="#{}#">
<cfif StructKeyExists(application.adminimages,path) and false>
	<cfset str=application.adminimages[path]>
<cfelse>
	<cfsavecontent variable="str" trim><cfinclude template="#path#"></cfsavecontent>
    <cfset application.adminimages[path]=str>
</cfif>

<cfif not structKeyExists(session,'oldStyle')>
	<cfset session.oldStyle=false>
	<cfif structKeyExists(cgi,'http_user_agent') and findNocase('MSIE',cgi.http_user_agent)>
    	<cfset session.oldStyle=true>
    </cfif>
</cfif> 
<cfif session.oldStyle><cfset str=path></cfif>

</cfsilent><cfoutput><cfif attributes.type EQ "css">#str#<cfelse><img src="#str#" #name##width##height##hspace##vspace##title##valign##style# border="#attributes.border#" /></cfif></cfoutput></cfif>