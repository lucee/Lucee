<p> TestContent </p>

<cfsavecontent variable="addtohead">
	<meta charset="utf-8">
</cfsavecontent>
<cfhtmlhead text="#addtohead#"/>
<head>
	<cfset pageContents = getPageContext().getOut().getString() />
</head>
