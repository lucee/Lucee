<cfscript>
	hasAccess=true;

function byteFormat(numeric bytes){

	kb=bytes/1024;
	if(kb<1) return bytes&"b";

	mb=kb/1024;
	if(mb<1) return rround(kb)&"kb";

	gb=mb/1024;
	if(gb<1) return rround(mb)&"mb";

	tb=gb/1024;
	if(tb<1) return rround(gb)&"gb";

	return rround(tb)&"tb";
}

function rround(nbr) {
	if(nbr>99) return round(nbr);
	if(nbr>9) return round(nbr*10)/10;
	return round(nbr*100)/100;
}

</cfscript>

<cfset csss={
	active:"background-color:##cfc",
	installed:"background-color:##ffc",
	notinstalled:"background-color:##fcc",
	resolved:"background-color:##ff9"

	}>


<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">	

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="info.bundle.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="info.bundle.create.cfm"/></cfcase>
</cfswitch>