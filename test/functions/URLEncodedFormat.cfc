component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for URLEncodedFormat()", body=function() {
			it(title="checking URLEncodedFormat() function", body = function( currentSpec ) {
				assertEquals("123","#URLEncode("123")#");
				assertEquals("123","#URLEncodedFormat("123")#");
				assertEquals("abcDEF123","#URLEncodedFormat("abcDEF123")#");

				assertEquals("%20", "#" ".URLEncodedFormat()#");
				assertEquals("+", "#" ".URLEncode()#");

				special=chr(246)&chr(228)&chr(252)&chr(233)&chr(224)&chr(232);
				plain=' 	+%&$,\|/:;=?@<>##{}()[]^`~-_.*''"#special#';

				encoded=URLEncodedFormat(plain);
				replain=URLDecode(encoded);

				assertEquals("%20%09%2B%25%26%24%2C%5C%7C%2F%3A%3B%3D%3F%40%3C%3E%23%7B%7D%28%29%5B%5D%5E%60%7E%2D%5F%2E%2A%27%22%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8","#encoded#");
				assertEquals("#replain#","#plain#");
				assertEquals("#replain#","#plain#");

				assertEquals("%20","#URLEncodedFormat(' ')#");
				assertEquals("%20","#URLEncodedFormat(' ','iso-8859-1')#");
				assertEquals("%20","#URLEncodedFormat(' ','utf-8')#");

				test=URLEncodedFormat(special);
				assertEquals("%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8","#test#");
				assertEquals("#special#","#URLDecode(test)#");

				test=URLEncodedFormat(special,'iso-8859-1');
				assertEquals("%F6%E4%FC%E9%E0%E8","#test#");
				assertEquals("#special#","#URLDecode(test,'iso-8859-1')#");

				test=URLEncodedFormat(special,'utf-8');
				assertEquals("%C3%B6%C3%A4%C3%BC%C3%A9%C3%A0%C3%A8","#test#");
				assertEquals("#special#","#URLDecode(test,'utf-8')#");

				assertEquals("123","#URLEncodedFormat("123")#");
				assertEquals("This%20is%20a%20test","#urlencodedformat('This is a test')#");
				assertEquals("%20%2B%2B%2D%2D%2E%2E%5F%5F%7E%2A","#urlencodedFormat(" ++--..__~*")#");
			});
		});
	}
}