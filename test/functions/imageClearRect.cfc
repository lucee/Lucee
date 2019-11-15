component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#zipTest";
		directoryCreate(variables.Dir);
		if(!directoryexists(dir)) {
			directorycreate(dir);
		}
	}
	function run() {
		describe( title="Test suite for imageClearRect", body=function() {
			it( title='Test case for imageClearRect',body=function( currentSpec ) {
				img = imageread("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				ImageClearRect(img,100,100,100,100);
				cfimage(action="write",source=img,destination=dir&".\rect.png",overwrite="yes");
				assertEquals(fileexists(dir&".\rect.png"),"true");
				img1 = imageread("https://dev.lucee.org/uploads/default/original/2X/1/140e7bb0f8069e4f7f073b6d01f55c496bbd42e3.png");
				img1.ClearRect(100,100,100,100);
				cfimage(action="write",source=img,destination=dir&".\rect1.png",overwrite="yes");
				assertEquals(fileexists(dir&".\rect1.png"),"true");
			});
		});
	}
	function afterAll() {
		if(directoryexists(dir)) {
			directorydelete(dir,true);
		}
	}
}