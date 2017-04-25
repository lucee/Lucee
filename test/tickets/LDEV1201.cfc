component extends="org.lucee.cfml.test.LuceeTestCase"{

	private function test() {
		return {a:1};
	}

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




			it(title="a.b.c.d?:'DF'", body=function(){
				a.b.c.d=1;
				var result = "";
				try{
					result = a.b.c.d?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe(1);
			});
			it(title="a1.b1.c1.d1?:'DF'", body=function(){
				var result = "";
				try{
					result = a1.b1.c1.d1?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe("DF");
			});

			it(title="test().a?:'DF'", body=function(){
				var result = "";
				try{
					result = test().a?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe(1);
			});

			it(title="test().notexisting?:'DF'", body=function(){
				var result = "";
				try{
					result = test().notexisting?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe("DF");
			});

			it(title="notexisting()?:'DF'", body=function(){
				var result = "";
				try{
					result = notexisting()?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe("DF");
			});

			it(title="notexisting().a?:'DF'", body=function(){
				var result = "";
				try{
					result = notexisting().a?:'DF';
				}catch(any e){
					result = e.message;
				}
				expect( result ).toBe("DF");
			});

		});
	}
	
	function testElvis() {
		return {};
	}
}