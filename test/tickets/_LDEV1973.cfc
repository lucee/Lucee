component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test case for LDEV-1973", body=function(){
			it(title="Checking deserializeJSON with 8 zeros", body=function(){
				jsonText1 = '{"number":1234.00000000}';
				result1 = deserializeJSON(jsonText1);
				expect(result1.number).toHaveLength(4);
			});
			it(title="Checking deserializeJSON with 4 zeros", body=function(){
				jsonText2 = '{"number":1234.0000}';
				result2 = deserializeJSON(jsonText2);
				expect(result2.number).toHaveLength(4);
			});
			it(title="Checking deserializeJSON with above 5 zeros", body=function(){
				jsonText3 = deserializeJSON('{"number":1234.000000}');
				expect(jsonText3.number).toHaveLength(4);
			});
		});
	}
}