component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function afterAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "test suite for the safe navigation operator", function() {

			it(title="test not existing variable", body=function() {
				expect(not_.existing_.var_?:'NotExisting').toBe("NotExisting");
				expect(not_existing_var?:"NotExisting").toBe("NotExisting");
			});


			it(title="test smple address", body=function() {
				expect(isNull(myvar?.firstlevel())).toBeTrue();
				expect(isNull(myvar?.firstlevel?.nextlevel())).toBeTrue();
				expect(isNull(myvar?.firstlevel?.nextlevel?.udf())).toBeTrue();
			});

			it(title="test assign function call", body=function() {
				x=myvar?.firstlevel();
				expect(isNull(x)).toBeTrue();
				
				x=myvar?.firstlevel?.nextlevel();
				expect(isNull(x)).toBeTrue();
				
				x=myvar?.firstlevel?.nextlevel?.udf();
				expect(isNull(x)).toBeTrue();
			});

			it(title="test assign variable", body=function() {
				x=myvar?.firstlevel;
				expect(isNull(x)).toBeTrue();
				x=myvar?.firstlevel?.nextlevel;
				expect(isNull(x)).toBeTrue();
				x=myvar?.firstlevel?.nextlevel?.udf;
				expect(isNull(x)).toBeTrue();
			});

		});
	}
}
