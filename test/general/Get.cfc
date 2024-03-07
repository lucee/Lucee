component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "Test bytecode generated get", function() {
			it( title='get collection vs get', body=function( currentSpec ) {

				var qry=query("name":["Susi","Peter"]);
				expect(isQuery(qry)).toBeTrue();
				expect(qry.name).toBe("Susi");
				expect(valueList(qry.name)).toBe("Susi,Peter");
				
				var data.sub=qry;
				expect(isStruct(data)).toBeTrue();
				expect(isQuery(data.sub)).toBeTrue();
				expect(data.sub.name).toBe("Susi");
				expect(valueList(data.sub.name)).toBe("Susi,Peter");
				
				var outer.data.sub=data.sub;
				expect(isStruct(outer)).toBeTrue();
				expect(isStruct(outer.data)).toBeTrue();
				expect(isQuery(outer.data.sub)).toBeTrue();
				expect(outer.data.sub.name).toBe("Susi");
				expect(valueList(outer.data.sub.name)).toBe("Susi,Peter");
			});

			it( title='safe navigated', body=function( currentSpec ) {
				var x=myvar?.firstlevel;
				expect(isNull(x)).toBeTrue();
			});
		});
	}
	
}