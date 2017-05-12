<cfscript>
function getInterfaceCLName(clazz) {
	var interfaces=clazz.getinterfaces();
	return interfaces[1].getClassLoader().toString();
}

	obj =new HelloWorld();
	MyInterface=createObject('java','test.ldev1121.MyInterface');
	dynInstnace = createDynamicProxy(obj, ["test.ldev1121.MyInterface"]);	
	res=getInterfaceCLName(getMetaData(dynInstnace));


	// try it
	myTest=createObject('java','test.ldev1121.MyTest').init();
	res=myTest.test(dynInstnace);
	echo(res);
	echo('-');

	// check interface
	result = IsInstanceOf(dynInstnace , 'test.ldev1121.MyInterface');
	echo(result);
</cfscript>