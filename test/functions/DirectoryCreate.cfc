component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";	
	}
	function afterAll(){
		directorydelete(dir,true);
	}	
	function run( testResults , testBox ) {
		describe( "test case for directoryCreate", function() {
			it(title = "Checking with directoryCreate()", body = function( currentSpec ) {
				cflock(name="testdirectoryCreate" timeout="1" throwontimeout="no" type="exclusive"){
					_dir=dir&createUUID();
					assertEquals("#false#", "#DirectoryExists(_dir)#");
					directoryCreate(_dir);
					assertEquals("#true#", "#DirectoryExists(_dir)#");

					try{
						directoryCreate(_dir);
						fail("must throw:The specified directory ... could not be created.");
					}catch(Any e){}
					directorydelete(_dir);

					dir2=_dir&"/a/b/c/";
					assertEquals("#false#", "#DirectoryExists(dir2)#");
					directoryCreate(dir2);
					assertEquals("#true#", "#DirectoryExists(dir2)#");
					directorydelete(_dir,true);
				}

			<!--- end old test code --->
			});
		});	
	}
}