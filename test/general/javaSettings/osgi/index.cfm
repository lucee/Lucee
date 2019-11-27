<cfscript>
	test1=createObject(type:'java',class:'org.lucee.mockup.osgi.Test');
	meta1=getMetaData(test1);
	// bi1=bundleInfo(test1); only loaded as an OSGi bundle when using bundle info
	
	test2=createObject(type:'java',class:'org.lucee.mockup.osgi.Test'
		,bundlename:"lucee.mockup",bundleversion:"1.0.0.0");
	
	meta2=getMetaData(test2);
	bi2=bundleInfo(test2);

	

	sct={
		"bundle1":{"name":bi1.name,"version":bi1.version},
		"bundle2":{"name":bi1.name,"version":bi1.version}
	};

	echo(serializeJson(sct));
</cfscript>