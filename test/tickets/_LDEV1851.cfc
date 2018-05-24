component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV-1851", function() {
			it( title='Checking contains() function returns boolean', body=function( currentSpec ) {
				var a = {};
				var b = {};
				var x = [ a ];
				assertEquals("false", isnumeric(x.contains(b)));
			});

			it( title='Checking contains() function returns boolean while without value', body=function( currentSpec ) {
				var a = {};
				var b = {};
				var x = [ a ];
				assertEquals("false", isnumeric(x.contains("")));
			});

			it( title='Checking equals() function returns boolean', body=function( currentSpec ) {
				var a = {};
				var b = {};
				var x = [ a ];
				assertEquals("false", isnumeric(a.equals(b)));
			});

			it( title='Checking collection interface while using createObject()', body=function( currentSpec ) {
				var a = {};
				var b = {};
				var x = [ a ];
				var result = createObject("java", "java.util.HashSet").init([ a, b ]).size();
				assertEquals('1', result);
			});
		});
	}
}
