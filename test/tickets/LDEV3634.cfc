component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{
	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3634",function(){
			it(title="seriliazeJSON() component has property with custom getter function", body =function( currentSpec ){
				customGetter = new LDEV3634.test();
				expect(serializeJSON(customGetter)).toBe('{"sum":"From custom getter function"}');
			});
			it(title="seriliazeJSON() component has property with getter false", body =function( currentSpec ){
				getterFalse = new LDEV3634.test2();
				expect(serializeJSON(getterFalse)).toBe('{"number2":20}');
			});
		});
	}
}
