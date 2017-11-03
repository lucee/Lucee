<cfscript>
component extends="testbox.system.compat.framework.TestSuite" {
	
	/**
	* adding n testcases by defining a package that hold testcases 
	* for example: "org.lucee.cfml.test"
	* @packageName name of the package that holds testcases
	*/
	public void function addPackage(required string packageName){
		var cfcs=getTestcasesFromPackage(packageName,true);
		loop collection="#cfcs#" index="local.name" item="local.cfc" {
			addAll(name, cfc);
        }
	}
	
	private struct function getTestcasesFromPackage(required string packageName, boolean loaded=true){
		var results={};
		try {
        	var names=componentListPackage(packageName);
        }
        catch(e){
        	
        	// try the map relative to the callers path
        	try {
	        	var templ=getTemplatePath();
	        	if(templ.len()>1) {
	        		packageName=ListCompact(replace(replace(getDirectoryfrompath(contractPath(templ[templ.len()-1])),'/','.','all'),'\','.','all'),'.')&"."&packageName;
	        		var names=componentListPackage(packageName);
	        	}
        	}
        	catch(ee){}
        	if(isNull(names)) rethrow;
        }
        
        
		var cfc='';
		loop array="#names#" item="name" {
			// check if it is a Testcase
			cfc=createObject('component',packageName&"."&name);
			if(isTestCase(cfc)) {
                if(loaded) results[packageName&"."&name]=cfc;
				//if(loaded) results[packageName&"."&name&"("&meta.path&")"]=cfc;
				else results[packageName&"."&	name]=packageName&"."&name;
            }
        }
		return results;
    }
    
    /* *
    * Primary method for running TestSuites and individual tests.
    * @results The TestResult collecting parameter.
    * @testMethod A single test method to run.
    * /
    remote function run(TestResult results,string testMethod="") {
    	systemOutput("run",true,true);
    	if(isNull(results))results=createObject("component","mxunit.framework.TestResult").TestResult();
    	var testRunner = createObject("component", "LuceeTestSuiteRunner");
		testRunner.setMockingFramework(this.mockingFramework);
		testRunner.setDataProviderHandler(this.dataProviderHandler);
		if(variables.requestScopeDebuggingEnabled OR structKeyExists(url,"requestdebugenable"))
			testRunner.enableRequestScopeDebugging();
		return testRunner.run(this.suites(), results, testMethod);
	}*/
    
    private boolean function isTestCase(required component cfc) {
    	return isInstanceof(cfc,'mxunit.framework.TestCase') 
    	|| isInstanceof(cfc,'testbox.system.testing.compat.framework.TestCase')
    	|| isInstanceof(cfc,'testbox.system.compat.framework.TestCase');
    }

}
</cfscript>