component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageReadBase64/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults, testBox ) {
		describe( "test case for ImageReadBase64", function() {

			it(title = "Checking with ImageReadBase64", body = function( currentSpec ) {
				var newImg = imageNew("",200,200,"rgb","red");
				var getbase64 = imagewriteBase64(newImg,path&'readBase64.txt','jpg');
				cfimage( action="write", source = ImageReadBase64(getbase64), destination = path&'readbase64Img.png', overwrite = "yes");
			  	expect(fileExists(path&"readbase64Img.png")).tobe("true");
			});

			it(title = "Checking with Image.ImageReadBase64", body = function( currentSpec ) {
				var getImg = imageNew("",200,200,"rgb","red");
				var base64 = getImg.writeBase64(path&"getbase64.txt","jpg");
				cfimage( action = "write", source = ImageReadBase64(base64), destination = path&'base64Img.png', overwrite = "yes" );
			  	expect( fileExists(path&"base64Img.png") ).tobe("true");
			});
		});
	};

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path, true);
		}
	}
}