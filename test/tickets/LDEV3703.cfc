component extends = "org.lucee.cfml.test.LuceeTestCase" labels="regex" {
	function run( testResults, textbox ) {
		describe("testcase for LDEV-3703", function(){
			it(title="Checking reMatchNoCase() with pattern '.*' in regex perl engine", body=function( currentSpec ){
				expect(serializeJSON(reMatchNoCase(".*", ""))).toBe('[""]');
				expect(serializeJSON(reMatchNoCase(".*", " "))).toBe('[" "]');
				expect(serializeJSON(reMatchNoCase(".*", "123"))).toBe('["123"]');
			});
			it(title="Checking reMatchNoCase() with pattern '.*' in regex java engine", body=function( currentSpec ){
				application regex="#{type="java"}#";
				expect(serializeJSON(reMatchNoCase(".*", ""))).toBe('[""]');
				expect(serializeJSON(reMatchNoCase(".*", " "))).toBe('[" ",""]');
				expect(serializeJSON(reMatchNoCase(".*", "123"))).toBe('["123",""]');
			});
		});
	}

	function afterAll() {
		application regex="#{type="perl"}#"; // after the test again set regex type to perl 
	}
} 
