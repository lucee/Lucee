component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-652", function() {
			it(title="checking EXIF data from ImageGetExifMetadata function", body = function( currentSpec ) {
				var uri=createURI("LDEV0652/test.JPG");
				var EXIFdata = imageGetEXIFMetadata(imageRead(uri));
				expect(EXIFdata.Make).toBE('Apple');
				expect(EXIFdata.model).toBE('iPhone 5s');
				expect(EXIFdata["Exif Image Height"]).toBE('2448 pixels');
				expect(EXIFdata["Exif Image Width"]).toBE('3264 pixels');
				expect(EXIFdata["Exif version"]).toBE('2.21');
				expect(EXIFdata["Color Space"]).toBE('sRGB');
				/*expect(EXIFdata.Orientation).toBE('Top, left side (Horizontal / normal)');
				expect(EXIFdata["lens Make"]).toBE('Apple');
				expect(EXIFdata["lens Model"]).toBE('iPhone 5s back camera 4.15mm f/2.2');
				expect(EXIFdata["YCbCr Positioning"]).toBE('Center of pixel array');
				expect(EXIFdata["Lens Specification"]).toBE('83/20 83/20 11/5 11/5');*/
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}