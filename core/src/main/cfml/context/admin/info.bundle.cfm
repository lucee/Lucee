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

		
			if(structKeyExists(headers,"Bnd-LastModified"))
				return dateAdd("l",headers["Bnd-LastModified"],unix0);
			else if(structKeyExists(headers,"Built-Date"))
				return parseDateTime(headers["Built-Date"]);
		try {}
		catch(e) {}
		return "";
	}

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