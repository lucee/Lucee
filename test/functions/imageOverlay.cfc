component extends="org.lucee.cfml.test.LuceeTestCase" labels="image" {

	function run( testResults , testBox ) {
		describe( "test case for imageOverlay", function() {

			it(title = "Checking with image.Overlay", body = function( currentSpec ) {
				var myImg = imageNew("",152,152,"rgb","40739e");
				var topImg = imageNew("",80,80,"rgb","fbc531");
				myImg.overlay(topImg);
				expect( isImage(myImg) ).tobe("true");
			});

			it(title = "Checking with imageOverlay", body = function( currentSpec ) {
				var myImg = imageNew("",152,152,"rgb","40739e");
				var topImg = imageNew("",80,80,"rgb","fbc531");
				ImageOverlay( myImg, topImg );
				expect( isImage( myImg ) ).tobe("true");
			});

		});
	};

}