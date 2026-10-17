component extends="org.lucee.cfml.test.LuceeTestCase" {

    function beforeAll(){
		variables.uri = createURI("LDEV3672");
	}

	function run( testResults , testBox ) { 

		describe( title='LDEV-3672' , body=function(){

			it( title='dumping a cfc errors with final' , body=function() {
				local.result = _InternalRequest(
					template:"#variables.uri#/LDEV3672.cfm"
				);
				expect(local.result.filecontent.trim()).toBe('success');
			});

			it( title='final this member is public and stays final' , body=function() {
				var cfc = new LDEV3672.LDEV3672_final();
				expect( cfc.test ).toBe( "test" );
				expect( function(){ cfc.test = "changed"; } ).toThrow();
				expect( cfc.test ).toBe( "test" );
			});
		});
	}

    private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatepath()),"\/")#/";
		return baseURI & "" & calledName;
	}
}
