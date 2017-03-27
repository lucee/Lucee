component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1201", body=function(){
			it(title="Checking elvis operator", body=function(){
				var testStruct = {};
				var testStruct1 = { bar:"bar" };
				expect( testStruct.foo ?: 'bar' ).toBe('bar');			
				expect( testStruct1.foo ?: 'bar' ).toBe('bar');
			});

			it(title="Checking elvis operator value return by a function", body=function(){
				var result = "";
				try{
					result = testElvis().foo ?: 'bar';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe('bar');
			});

			it(title="Checking elvis operator value return by a function with isNull()", body=function(){
				var result = "";
				try{
					result = isNull( testElvis().foo );
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe('True');
			});
		});
	}
	
	function testElvis() {
		return {};
	}
}