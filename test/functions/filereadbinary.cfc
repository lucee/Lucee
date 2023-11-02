component extends = "org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"FileReadBinary/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}

	function run( testResults , testBox ) {
		describe( "test case for FileReadBinary", function() {
			it(title = "Checking with FileReadBinary with local image", body = function( currentSpec ) {
				imgBinary = imageNew("",200,200,"rgb","red");
				cfimage(action = "write", source = imgBinary, destination = path&"filebinaryImg.jpg", overwrite="yes");
			  	expect(isbinary(FileReadBinary(path&'filebinaryImg.jpg'))).tobe("true");
			});

			it(title = "Checking with FileReadBinary with URL", body = function( currentSpec ) {
			  	expect(isbinary(FileReadBinary("https://lucee.org/assets/img/logo.png"))).tobe("true");
			});
			
			it(title = "Checking with FileReadBinary with file", body = function( currentSpec ) {
				filewrite(path&'FileReadBinary.txt'," I love lucee");
			  	expect(isbinary(FileReadBinary(path&'FileReadBinary.txt'))).tobe("true");
			});
		});
	}

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}