component extends="org.lucee.cfml.test.LuceeTestCase"{
	private function udf1(){
		return "inner";
	}
	private function udf2(){
		return udf1;
	}
	private function udf3(){
		return udf2;
	}
	private function udf4(){
		return udf3;
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1182", function() {
			it( title='checking UDF', body=function( currentSpec ) {
				
				result=udf2();  
				expect(isCustomFunction(result)).toBe(true);

				result=udf2()();  
				expect(result).toBe('inner');
			});
			it( title='checking UDF on multiple levels', body=function( currentSpec ) {
				
				result=udf4();  
				expect(isCustomFunction(result)).toBe(true);

				result=udf4()()()();  
				expect(result).toBe('inner');
			});

			it( title='Checking Closure', body=function( currentSpec ) {
				result=function(){return udf1;}()();
				expect(result).toBe('inner');
			});


		});
	}
}