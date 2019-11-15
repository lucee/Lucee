component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"ImageWriteBase64/";
		if(!directoryExists(path)){
			directorycreate(path);
		}
	}
	function run( testResults , testBox ) {
		describe( "test case for ImageWriteBase64", function() {
			it(title = "Checking with ImageWriteBase64", body = function( currentSpec ) {
				newImg = imageNew("",200,200,"rgb","red");
				imagewriteBase64(newImg,path&'funBase64.txt','jpg');
			  	expect(fileread(path&"funBase64.txt")).tobestring();
			});
			it(title = "Checking with Image.WriteBase64()", body = function( currentSpec ) {
				imgbase = imageNew("",200,200,"rgb","blue");
				imgbase.writeBase64(path&'objBase64.txt','jpg','true');
			  	expect(fileread(path&"objBase64.txt")).tobestring();
			});
		});
	}
	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path, true);
		}
	}
}