component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path ="#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1388\";
		if(not directoryExists("#path#uploads")){
			Directorycreate("#path#uploads");
		}
	}

	function afterAll(){
		variables.path ="#getDirectoryFromPath(getCurrenttemplatepath())#LDEV1388\";
		if(directoryExists("#path#uploads")){
			DirectoryDelete("#path#uploads",true);
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1388", function() {
			it(title="checking CMYK image using cfimage tag, with action='resize' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					cfimage (action="resize", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#path#uploads/resize#indx#.jpg", height="333",  width="500" , overwrite="yes");
					expect(IsImageFile( "#path#uploads/resize#indx#.jpg" )).toBe("true");
				}
			});

			it(title="checking CMYK image using cfimage tag, with action='convert' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					cfimage (action="convert", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#path#uploads/convert#indx#.png", format="png" , overwrite="yes");
					expect(IsImageFile( "#path#uploads/convert#indx#.png" )).toBe("true");
				}
			});

			it(title="checking CMYK image using cfimage tag, with action='write' ", body = function( currentSpec ) {
				loop from=1 to=4 index="local.indx" {
					cfimage (action="convert", 
						source="#path#/cmyk_#indx#.jpg", 
						destination="#path#uploads/write#indx#.jpg", overwrite="yes");
					expect(IsImageFile( "#path#uploads/write#indx#.jpg" )).toBe("true");
				}
			});

		});
	}
}