<p> TestContent </p>

<cfsavecontent variable="addtohead">
	<meta charset="utf-8">
</cfsavecontent>
<cfhtmlhead text="#addtohead#"/>
<cfsilent>
	<cfset pageContents = getPageContext().getOut().getString() />
</cfsilent>
