component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath(getcurrentTemplatepath());
		variables.path = base&"LDEV2410\example.txt";
		if(!directoryExists(base&"LDEV2410")){
			directoryCreate(base&'LDEV2410');
		}
	}

	function run( testResults, testBox ){
		describe( "test case for LDEV-2410", function() {
			it(title = "checking the file with READONLY Attribute", body = function( currentSpec ) {
				FileWrite(path,"I am in readonly file");
				fileSetAttribute(path,'readonly');
				expect(getfileinfo(path).canRead).toBe(true);
				expect(getfileinfo(path).canWrite).toBe(false);
			});
			it(title = "checking the file with NORMAL Attribute", body = function( currentSpec ) {
				try{
					fileSetAttribute(path,'normal');
					FileWrite(path,"I am in normal file");
					res = getfileinfo(path).canWrite;
				}
				catch(any e){
					res = e.message;
				}
				expect(res).toBe(true);
			});
		});
	}

	function afterAll(){
		if(directoryExists(base&"LDEV2410")){
			directoryDelete(base&"LDEV2410",true);
		}
	}
}