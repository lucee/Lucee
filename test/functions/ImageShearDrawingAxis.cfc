component extends="org.lucee.cfml.test.LuceeTestCase"{
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

			it(title = "Checking with Image.ImageShearDrawingAxis with URL image", body = function( currentSpec ) {
				urlImg = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				ImageShearDrawingAxis(urlImg,1.1,0.3);
				ImageDrawRect(urlImg,40,50,70,50,"yes");
				cfimage( action = "write", source = urlImg, destination = path&'sheardrawLogo.jpg', overwrite = "yes");
			  	expect(fileExists(path&"sheardrawLogo.jpg")).tobe("true");
			});

			it(title = "Checking with Image.ShearDrawingAxis()", body = function( currentSpec ) {
				imgObj = imageNew("",200,200,"rgb","149c82");
				imgObj.shearDrawingAxis(0.4,0.2);
				imgObj.drawRect(40,50,70,50,"yes");
				cfimage( action = "write", source = imgObj, destination = path&'sheardrawObj.png', overwrite = "yes" );
			  	expect( fileExists(path&"sheardrawObj.png") ).tobe("true");
			});

			it(title = "Checking with Image.ShearDrawingAxis() with URL image", body = function( currentSpec ) {
				urlimgObj = imageRead("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				urlimgObj.shearDrawingAxis(0.4,0.9);	
				urlimgObj.drawRect(40,50,70,50,"yes");
				cfimage( action = "write", source = urlimgObj, destination = path&'sheardrawlogoObj.png', overwrite = "yes" );
			  	expect( fileExists(path&"sheardrawlogoObj.png") ).tobe("true");
			});
		});
	};
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path, true);
		}
	}
}