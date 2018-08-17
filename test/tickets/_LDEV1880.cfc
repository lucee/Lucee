component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.base = GetDirectoryFromPath(getcurrentTemplatepath());
		variables.path = base&"LDEV1880\example.txt";
		if(!directoryExists(base&"LDEV1880")){
			directoryCreate(base&'LDEV1880');
		}
	}
	function afterAll(){
		if(directoryExists(base&"LDEV1880")){
			directoryDelete(base&"LDEV1880",true);
		}
	}
	function run( testResults , testBox ) {
		describe( "test suite for fileSetAttribute()", function() {
			beforeEach( function( currentSpec ) {
				if(!fileExists(path)){
					variables.myfile = FileOpen(path, "write");
					FileWrite(path,"This is a sample file content");
				}
			});
			afterEach( function( currentSpec ) {
				if(fileExists(path)){
					filedelete(path);
				}
			});

			it(title = "checking the file with Hidden Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Hidden');
				expect(getfileinfo(path).isHidden).toBe('true');
			});

			it(title = "checking the file with Archive Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Archive');
				expect(getfileinfo(path).isArchive).toBe('true');
			});

			it(title = "checking the file with System Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'System');
				expect(getfileinfo(path).isSystem).toBe('true');
			});

			it(title = "checking the file with readOnly Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'readOnly');
				expect(getfileinfo(path).canRead).toBe('true');
				expect(getfileinfo(path).canWrite).toBe('false');
			});

			it(title = "checking the file with Normal Attribute", body = function( currentSpec ) {
				fileSetAttribute(path,'Normal');
				expect(getfileinfo(path).canRead).toBe('true');
				expect(getfileinfo(path).canWrite).toBe('true');
				expect(getfileinfo(path).isHidden).toBe('false');
				expect(getfileinfo(path).isSystem).toBe('false');
				expect(getfileinfo(path).isArchive).toBe('false');
			});
		});
	}
}