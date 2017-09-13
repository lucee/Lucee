component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1484", function() {
			it( title='Checking structFilter() with arguments scope calling via UDF', body=function( currentSpec ) {
			    local.result = udfWithoutParameter('foo,bar');
				expect(local.result).toBe('foo,bar');
			});

			it( title='Checking structFilter() with arguments scope calling via UDF has declared parameters ', body=function( currentSpec ) {
				local.result = udfWithParameter('foo,bar');
				expect(local.result).toBe('foo,bar');
			});

			it( title='Checking structFilter() member function with arguments scope', body=function( currentSpec ) {
				local.result = structFilterMem('foo,bar');
				expect(local.result).toBe('foo,bar');
			});

		});
	}

	function udfWithoutParameter() {
		try{
		    var result = structFilter( arguments, function() {
		        return true;
		    });
		}catch(any e) {
			result = e.message;
		}
		return result;
 	}

	function udfWithParameter(foo,bar) {
		try{
		    var result =  structFilter( arguments, function() {
				return true;
			});
		}catch(any e) {
			result = e.message;
		}
		return result;

	}

	function structFilterMem() {
		var result = "";
		try{
			var test  = arguments.filter(function(){
	     		return true;
			});
			result = test.1;
		} catch ( any e ){
			result = e.message;
		}
		return result;
 	}
}