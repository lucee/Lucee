component extends="org.lucee.cfml.test.LuceeTestCase"  {

	
	function run( testResults , testBox ) {
		describe( "test case for LDEV-4976", function() {
			it( title="issue ##1 ", body=function( currentSpec ) {
				var cfc=new LDEV4976.MyString();
			});
			it( title="issue ##2 ", body=function( currentSpec ) {
				var cfc=new LDEV4976.MyString2();
			});
			it( title="issue ##3 ", body=function( currentSpec ) {
				var cfc=new LDEV4976$Sub("four");
				expect( cfc.length() ).toBe(4);

			});
			it( title="inline component", body=function( currentSpec ) {
				var cfc=new component {
					variables.text="four";
					
					function init(text) {
						variables.text=text;
					}
				
					function length() {
						return len(variables.text);
					}
				};
				expect( cfc.length() ).toBe(4);

			});
		});
	}
}
component name="Sub" {
    variables.text="";
    
    function init(text) {
        variables.text=text;
    }

    function length() {
        return len(variables.text);
    }
}