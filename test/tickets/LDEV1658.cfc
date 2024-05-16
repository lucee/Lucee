component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1658", body=function() {
			it( title='Checking arrayAppend member function without any params',body=function( currentSpec ) {
				try{
					var myArr = [];
					var result = myArr.Append();
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("too few arguments for function [ArrayAppend] call");
			});

			it( title='Checking arrayPrepend member function without any params',body=function( currentSpec ) {
				try{
					var myArr = [];
					var result = myArr.prepend();
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("too few arguments for function [ArrayPrepend] call");
			});

			it( title='Checking arraySort member function without any params',body=function( currentSpec ) {
				try{
					var myArr = [];
					var result = myArr.sort();
				} catch( any e ){
					var result = e.message;
				}
				expect(result).toBe("too few arguments for function [ArraySort] call");
			});
		});
	}
}
