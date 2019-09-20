<cfscript>
	test1=createObject(type:'java',class:'org.lucee.mockup.classic.Test');
	meta1=getMetaData(test1);
	

	try {
		bi1=bundleInfo(test1);
		isBundle=true;
	}
	catch(e) {
		isBundle=false;
	}
	
	

	sct={
		"bundle":isBundle
	};

	echo(serializeJson(sct));
</cfscript>