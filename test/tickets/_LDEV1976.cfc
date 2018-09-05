component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test case for LDEV-1976", body=function(){
			it(title="Checking CFHTTP fileContent binary to string", body=function(){
				local.http = new Http(url = "https://apis.google.com/js/api.js", method="GET");
				local.result = local.http.send().getPrefix();
				result1 = local.result.fileContent.toString();
				result2 = CreateObject('java', 'java.lang.String').init(local.result.fileContent);
				expect(result1).tobe(result2);
			});
		});
	}
}