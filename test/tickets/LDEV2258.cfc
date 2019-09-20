component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){

		variables.path = getDirectoryFromPath(getCurrentTemplatePath())&"LDEV2258/logo";
	}
	function run( testResults , testBox ) {
		describe( "Test case for LDEV2204", function() {
			it( title='imageNew(String)', body=function( currentSpec ) {
			 	local.result=imageNew(variables.path);
				expect(isStruct(local.result)).tobe(true);
			});
			it( title='imageNew(File)', body=function( currentSpec ) {
			 	local.result=imageNew(createObject("java", "java.io.File").init(variables.path));
				expect(isStruct(local.result)).tobe(true);
			});
			it( title='imageNew(BufferedImage)', body=function( currentSpec ) {
			 	local.img = createObject("java", "javax.imageio.ImageIO")
			 		.read(createObject("java", "java.io.File").init(variables.path));
			 	local.result=imageNew(img);
				expect(isStruct(local.result)).tobe(true);
			});
			it( title='imageNew(Image)', body=function( currentSpec ) {
			 	local.result=imageNew(imageNew(variables.path));
				expect(isStruct(local.result)).tobe(true);
			});
			it( title='imageNew(Binary)', body=function( currentSpec ) {
			 	var barr=fileReadBinary(variables.path);
				local.result=imageNew(barr);
				expect(isStruct(local.result)).tobe(true);
			});

		});
	}
}