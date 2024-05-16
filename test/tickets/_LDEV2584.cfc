component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe("Test case for LDEV2584", function(){
			it(title = "Lucee doesn't convert decimal number to integer",body = function( currentSpec ){
				testQuery = queryNew("id,firstname,value", "integer,varchar,integer", [
					{ "id":1,"firstname":"VEN","value":4.767 },
					{ "id":2,"firstname":"SEL","value":1672.34 },
					{ "id":3,"firstname":"RNK","value":13.3456},
					{ "id":5,"firstname":"TMK","value":0.3456},
					{ "id":4,"firstname":"MTU","value":10}
				]);
				expect(testQuery.value[1]).toBe(4);
				expect(testQuery.value[2]).toBe(1672);
				expect(testQuery.value[3]).toBe(13);
				expect(testQuery.value[4]).toBe(10);
				expect(testQuery.value[4]).toBe(0);
			});
		});
	}
}