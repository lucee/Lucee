component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	public function beforeAll(){
		variables.ts=getTimeZone();
	}
	
	public function afterAll(){
		setTimezone(variables.ts);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4772", body=function() {
			it(title = "checking if we can load a component from an old lar file", body = function( currentSpec ) {
				
				// set old archive as mapping
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var parent=getDirectoryFromPath(mid(curr,1,len(curr)-1));
				var art=parent&"artifacts/lars/lucee-5.lar"; 
				expect( fileExists(art) ).toBeTrue();	
				admin 
						action="updateComponentMapping"
						type="web"
						password=request.WEBADMINPASSWORD
						virtual="/test4772"
						primary="archive"
						physical=""
						archive=art;
				
				
				var test=new org.lucee.test.Test();
				expect( test.foo() ).toBe("bar");	
			});

		});
	}
} 