component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageReadBase64/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults, testBox ) {
		describe( "test case for ImageReadBase64", function() {
			it(title = "Checking with ImageReadBase64", body = function( currentSpec ) {
				newImg = imageNew("",200,200,"rgb","red");
				getbase64 = imagewriteBase64(newImg,path&'readBase64.txt','jpg');
				cfimage( action="write", source = ImageReadBase64(getbase64), destination = path&'readbase64Img.png', overwrite = "yes");
			  	expect(fileExists(path&"readbase64Img.png")).tobe("true");
			});
			it(title = "Checking with Image.ImageReadBase64", body = function( currentSpec ) {
				getImg = imageRead("https://lucee.org/assets/img/logo.png");
				base64 = imagewriteBase64(getImg,path&"getbase64.txt","jpg");
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