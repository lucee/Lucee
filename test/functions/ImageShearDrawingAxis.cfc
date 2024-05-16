component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageShearDrawingAxis/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageShearDrawingAxis", function() {

			it(title = "Checking with ImageShearDrawingAxis", body = function( currentSpec ) {
				newimg = imageNew("",152,152,"rgb","A52A2A");
				ImageShearDrawingAxis(newimg,1.1,0.4);
				ImageDrawRect(newimg,40,50,70,50,"yes");
				cfimage( action = "write", source = newimg, destination = path&'sheardrawImg.jpg', overwrite = "yes");
			  	expect(fileExists(path&"sheardrawImg.jpg")).tobe("true");
			});

			it(title = "Checking with Image.ShearDrawingAxis()", body = function( currentSpec ) {
				imgObj = imageNew("",200,200,"rgb","149c82");
				imgObj.shearDrawingAxis(0.4,0.2);
				imgObj.drawRect(40,50,70,50,"yes");
				cfimage( action = "write", source = imgObj, destination = path&'sheardrawObj.png', overwrite = "yes" );
			  	expect( fileExists(path&"sheardrawObj.png") ).tobe("true");
			});

		});
	};

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path, true);
		}
	}
}