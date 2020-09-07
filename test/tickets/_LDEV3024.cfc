component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV3024", function() {
			it(title = "imageAddBorder with other bordertypes", body = function( currentSpec ) {
				img=imageRead("https://avatars1.githubusercontent.com/u/10973141?s=280&v=4")
				try {
					imageAddBorder(img,10,"red","wrap");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") OR structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

				img=imageRead("https://avatars1.githubusercontent.com/u/10973141?s=280&v=4")
				try {
					imageAddBorder(img,10,"red","reflect");
					res = (structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

			});
				
			it(title = "imageAddBorder with bordertype 'copy'", body = function( currentSpec ) {

				img = imageNew("",200,200,"rgb","blue");
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"colormodel") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");
				
				img=imageRead("https://thumbor.forbes.com/thumbor/960x0/https%3A%2F%2Fblogs-images.forbes.com%2Fjonathanocallaghan%2Ffiles%2F2019%2F10%2FHubble-Borisov-main.jpg");
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

				img=imageRead("https://avatars1.githubusercontent.com/u/10973141?s=280&v=4")
				try {
					imageAddBorder(img,10,"red","copy");
					res = (structkeyexists(img,"component 1") && structkeyexists(img,"component 2") && structkeyexists(img,"component 3") && structkeyexists(img,"width")); 
				}
				catch (any e) {
					res = e.message;
				}
				expect(res).toBe("true");

			});

		});
	}
}