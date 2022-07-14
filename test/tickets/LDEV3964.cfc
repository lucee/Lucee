component extends = "org.lucee.cfml.test.LuceeTestCase" skip=true{

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-3964", function() {	
			it( title="Checking the base64 value of image from ImageNew() with URL Image", body=function( currentSpec ) {
				var image = imageNew( "https://avatars1.githubusercontent.com/u/10973141?s=280&v=4" );
				var firstBase64 = toBase64( image ); 
				var secondBase64 = toBase64( image );
				expect(firstBase64 EQ secondBase64 ).tobeTrue();
			});
			it( title="Checking the base64 value of image from ImageNew()", body=function( currentSpec ) {
				var image = imageNew("",100,100,"rgb","yellow");
				var firstBase64 = toBase64( image ); 
				var secondBase64 = toBase64( image );
				expect(firstBase64 EQ secondBase64 ).tobeTrue();
			});
			it( title="Checking the base64 value of image from ImageNew() with bufferedImage", body=function( currentSpec ) {
				var imageIO = createObject( "java", "javax.imageio.ImageIO" );
				var imageUrl = createObject( "java", "java.net.URL" ).init( "https://avatars1.githubusercontent.com/u/10973141?s=280&v=4" );
				var bufferedImage = imageIO.read( imageUrl );
				var cfmlImage = imageNew( bufferedImage );
				var firstBase64 = toBase64( cfmlImage ); 
				var secondBase64 = toBase64( cfmlImage );
				expect(firstBase64 EQ secondBase64 ).tobeTrue();
			});
		});
	}
}