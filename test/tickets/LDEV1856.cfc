component extends="org.lucee.cfml.test.LuceeTestCase" labels="cfhttp"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1856", body=function() {
			it(title = "Checking cfhttp with charset attribute", body = function( currentSpec ) {
				cfhttp(url= "http://" &CGI.server_name &GetDirectoryFromPath( CGI.script_name ) &"LDEV1856/myfile.cfm", charset="iso-8859-1") { }
				expect(cfhttp.Filecontent.trim()).toBe(fileRead(GetDirectoryFromPath(GetCurrentTemplatePath())&"LDEV1856\myfile.cfm","iso-8859-1").trim());
			});
		});
	}

} 
