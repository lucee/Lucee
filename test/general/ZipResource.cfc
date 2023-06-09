component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		this.virtual="/lucee";
        this.relPath="/doc.cfm";
        this.fullPath=virtual&relPath;
	}

	function run( testResults , testBox ) {
		describe( "Test suite for the zip resource", function() {


			it( title='test if the zip resource works with expandPath', body=function( currentSpec ) {
				var path=expandPath(fullPath);
				expect(fileExists(path)).toBeTrue();
				expect(left(path,6)=="zip://").toBeTrue();
				expect(find("!",path)>0).toBeTrue();
			});
			it( title='test if the zip resource works with the mapping directly', body=function( currentSpec ) {
				var pc = getPageContext();
				var config = pc.getConfig();
				
				// get the mapping for /lucee
				loop array=config.getMappings() item="m" {
					if(m.getVirtual()==virtual) local.mapping=m;
				}
				// substract the mapping part
				ps = mapping.getPageSource(relPath);
				var path=ps.getDisplayPath();

				expect(fileExists(path)).toBeTrue();
				expect(left(path,6)=="zip://").toBeTrue();
				expect(find("!",path)>0).toBeTrue();
			});

		});
	}

}