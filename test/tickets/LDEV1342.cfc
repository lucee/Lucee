component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {
	function beforeAll(){
		variables.uri = createURI("LDEV1342");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1342", function() {
			it( title='Checking imageWrite produce missing huffman code error', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=1}
				);
				expect(local.result.filecontent.trim()).toBe("success");
			});

			it( title='Checking imageWrite produce missing huffman code error 2', body=function( currentSpec ) {
				try {
					var uploadImage ="./LDEV1342/assets/images/testImage1.jpg";
					var img = imageRead(uploadImage);
					var imgNew = "./LDEV1342/assets/images/newTestImage1.jpg";
					imageWrite(img, imgNew);
				}
				finally {
					if(!isNull(imgNew) && fileExists(imgNew))fileDelete(imgNew);
				}
			});

			it( title='Checking imageWrite not producing any error', body=function( currentSpec ) {
				local.result = _InternalRequest(
					template:"#variables.uri#/test.cfm",
					forms:{Scene=2}
				);
				expect(local.result.filecontent.trim()).toBe("success");
			});

			it( title='Checking imageWrite not producing any error 2', body=function( currentSpec ) {
				try{
					var uploadImage ="./LDEV1342/assets/images/testImage2.jpg";
					var img = imageRead(uploadImage);
					var imgNew = "./LDEV1342/assets/images/newTestImage2.jpg";
					imageWrite(img, imgNew);
				}
				finally {
					if(!isNull(imgNew) && fileExists(imgNew))fileDelete(imgNew);
				}
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}