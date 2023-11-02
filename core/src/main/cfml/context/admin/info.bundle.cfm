<cfscript>
	hasAccess=true;

	stText.info.bundles.subject="Bundle";
	stText.info.bundles.version="Version";
	stText.info.bundles.vendor="Vendor";
	stText.info.bundles.type="Type";
	stText.info.bundles.state="State";
	stText.info.bundles.created="Created";
	stText.info.bundles.states.active="active";
	stText.info.bundles.states.installed="loaded";
	stText.info.bundles.states.notinstalled="not loaded";
	stText.info.bundles.states.resolved="resolved";
	stText.info.bundles.usedBy="Used by";
	stText.info.bundles.isFragment="Fragment?";
	stText.info.bundles.fileName="File name";
	stText.info.bundles.path="Path";
	stText.info.bundles.manifestHeaders="Manifest Headers";
	stText.bundles.introText="These are all the OSGi bundles (jars) available locally.";

	unix0=createDateTime(1970,1,1,0,0,0,0,"UTC");


	function toDateFromBundleHeader(headers) {
		if(structKeyExists(arguments.headers,"Bnd-LastModified"))
			return dateAdd("l", arguments.headers["Bnd-LastModified"], variables.unix0);
		else if(structKeyExists(arguments.headers,"Built-Date"))
			return parseDateTime(arguments.headers["Built-Date"]);
		try {}
		catch(e) {}
		return "";
	}

	function byteFormat(numeric bytes){

		var kb=arguments.bytes/1024;
		if(kb<1) return arguments.bytes&"b";

		var mb=kb/1024;
		if(mb<1) return variables.rround(kb)&"kb";

		var gb=mb/1024;
		if(gb<1) return variables.rround(mb)&"mb";

		var tb=gb/1024;
		if(tb<1) return variables.rround(gb)&"gb";

		return variables.rround(tb)&"tb";
	}

	function rround(nbr) {
		if(arguments.nbr>99) return round(arguments.nbr);
		if(arguments.nbr>9) return round(arguments.nbr*10)/10;
		return round(arguments.nbr*100)/100;
	}

	csss={
	active:"background-color:##cfc",
	installed:"background-color:##ffc",
	notinstalled:"background-color:##fcc",
	resolved:"background-color:##ff9"

	};
</cfscript>


<cfparam name="url.action2" default="list">
<cfparam name="form.mainAction" default="none">
<cfparam name="form.subAction" default="none">	

<cfswitch expression="#url.action2#">
	<cfcase value="list"><cfinclude template="info.bundle.list.cfm"/></cfcase>
	<cfcase value="create"><cfinclude template="info.bundle.create.cfm"/></cfcase>
</cfswitch>