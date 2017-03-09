component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1192( Generic test cases for Custom JAVA exception with additional keys )", body=function(){
			it(title="Checking additional key available or not", body=function(){
				try{
					var jar=getDirectoryFromPath(getCurrentTemplatePath())&"LDEV1192/CustomerService.jar";
					var account = createobject("java", "com.mkyong.examples.CustomerService", [jar]);
					account.findByName("");
				} catch( any e ){
					expect(e.returnCode).toBe(100);
					expect(structKeyExists(e, "returnCode")).toBeTrue();
				}
			});
		});
	}
}