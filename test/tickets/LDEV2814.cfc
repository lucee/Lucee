component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&"LDEV2814/";
		if(!directoryExists(dir))directoryCreate(dir);
		variables.StringUtil=createObject("java","lucee.commons.lang.StringUtil");
	}

	function afterAll(){
		variables.dir=getDirectoryFromPath(getCurrentTemplatePath())&"LDEV2814/";
		if(directoryExists(dir))directoryDelete(dir,true);
		
	}
	function run( testResults , testBox ) {

		

		describe( "test suite for LDEV2814", function() {
			it(title = "unwrap wrong decoded 8220 and 8221", body = function( currentSpec ) {
				fileWrite(variables.dir&'test.txt',"#chr(8220)#abc#chr(8221)#",'UTF-8');
				var str=fileRead(variables.dir&'test.txt','Windows-1252');
				var res=variables.StringUtil.unwrap(str);
				expect("abc").toBe(res);
			});
			it(title = "unwrap wrong decoded 8220", body = function( currentSpec ) {
				fileWrite(variables.dir&'test.txt',"#chr(8220)#abc""",'UTF-8');
				var str=fileRead(variables.dir&'test.txt','Windows-1252');
				var res=variables.StringUtil.unwrap(str);
				expect("abc").toBe(res);
			});
			it(title = "unwrap wrong decoded 8221", body = function( currentSpec ) {
				fileWrite(variables.dir&'test.txt',"""abc#chr(8221)#",'UTF-8');
				var str=fileRead(variables.dir&'test.txt','Windows-1252');
				var res=variables.StringUtil.unwrap(str);
				expect("abc").toBe(res);
			});
			it(title = "unwrap ' ", body = function( currentSpec ) {
				fileWrite(variables.dir&'test.txt',"'abc'",'UTF-8');
				var str=fileRead(variables.dir&'test.txt','Windows-1252');
				var res=variables.StringUtil.unwrap(str);
				expect("abc").toBe(res);
			});
			it(title = "unwrap 8220 8221 ", body = function( currentSpec ) {
				fileWrite(variables.dir&'test.txt',"“abc”",'UTF-8');
				var str=fileRead(variables.dir&'test.txt','Windows-1252');
				var res=variables.StringUtil.unwrap(str);
				expect("abc").toBe(res);
			});

		});
	}
} 