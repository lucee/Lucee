component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2333", function() {
			it(title = "reFindNoCase() do not extract regular expression capture groups as subexpressions", body = function( currentSpec ) {
				var source = "lucee-5.3.3.56.jar";
				var RegExFileName = "Lucee-([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).jar";
				expect(refindnocase(RegExFileName,source,1)).toBe('1');
				expect(refindnocase(RegExFileName,source,5)).toBe('0');
				expect(refindnocase(RegExFileName,source,1,true).match[1]).toBe('lucee-5.3.3.56.jar');
				expect(refindnocase(RegExFileName,source,1,true).match[2]).toBe('5.3.3.56');
				expect(refindnocase(RegExFileName,source)).toBe('1');
				expect(refindnocase("[0-9]+",source,1,true).match[1]).toBe('5');
				expect(refindnocase("([0-9]+\.+)",source,1,true,"all")[1].match[1]).toBe('5.');
				expect(refindnocase("([0-9]+\.+)",source,1,true,"all")[2].match[2]).toBe('3.');
				expect(refindnocase("([0-9]+\.+)",source,1,true,"all")[3].match[2]).toBe('3.');
				expect(refindnocase("([0-9]+\.+)",source,1,true,"all")[4].match[2]).toBe('56.');

			});

			it(title = "reFind() do not extract regular expression capture groups as subexpressions", body = function( currentSpec ) {
				var source = "lucee-5.3.3.56.jar";
				var RegExFileName = "lucee-([0-9]+\.[0-9]+\.[0-9]+\.[0-9]+).jar";
				expect(refind(RegExFileName,source,1)).toBe('1');
				expect(refind(RegExFileName,source,5)).toBe('0');
				expect(refind(RegExFileName,"Lucee-5.3.3.56.jar",1,true).match[1]).toBe('');
				expect(refind(RegExFileName,source,1,true).match[1]).toBe('lucee-5.3.3.56.jar');
				expect(refind(RegExFileName,source,1,true).match[2]).toBe('5.3.3.56');
				expect(refind(RegExFileName,source)).toBe('1');
				expect(refind("[0-9]+",source,1,true).match[1]).toBe('5');
				expect(refind("([0-9]+\.+)",source,1,true,"all")[1].match[1]).toBe('5.');
				expect(refind("([0-9]+\.+)",source,1,true,"all")[2].match[2]).toBe('3.');
				expect(refind("([0-9]+\.+)",source,1,true,"all")[3].match[2]).toBe('3.');
				expect(refind("([0-9]+\.+)",source,1,true,"all")[4].match[2]).toBe('56.');
			});
		});
	}
}