<cfscript>
	test2=createObject(type:'java',class:'org.lucee.mockup2.osgi.Test2'
		,bundlename:"lucee.mockup2",bundleversion:"1.0.0.0");
	
	meta2=getMetaData(test2);
	bi2=bundleInfo(test2);


	sct={
		"bundle":{"name":bi2.name,"version":bi2.version}
	};

	echo(serializeJson(sct));
</cfscript>