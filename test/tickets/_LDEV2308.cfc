component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.path = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/" &"LDEV2308/";
		if(!directoryExists(path)){
			directorycreate(path)
		}
		fr="<";
		br=">";
		filewrite(path&'Application.cfc',"component {#chr(10)##chr(9)#this.setClientCookies = false;#chr(10)#}");
		filewrite(path&'test.cfm',"#fr#cfscript#br##chr(10)##chr(9)#thread name = 'test';#chr(10)##chr(9)#sleep(1000);#chr(10)##fr#/cfscript#br#");
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV2308", function() {
			it( title='cookie JSessionID with cfthread', body=function( currentSpec ) {
			 	cfhttp( method="POST", url="#CGI.server_name##variables.path#/test.cfm", result="res");
			 	expect(res.cookies.name).tobe('');
			});
		});
	}

	function afterAll(){
		if(directoryExists(path)){
			directoryDelete(path,true);
		}
	}
}





