<p> TestContent </p>

<cfsavecontent variable="addtohead">
	<meta charset="utf-8">
</cfsavecontent>
<cfhtmlhead text="#addtohead#"/>
<cfset pageContents = getPageContext().getOut().getString() />