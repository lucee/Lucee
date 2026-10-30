component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ListToArray()", body=function() {
			it(title="checking ListToArray() function", body = function( currentSpec ) {
				<!--- begin old test code --->
				assertEquals("0", "#arrayLen(ListToArray(''))#");
				assertEquals("3", "#arrayLen(ListToArray('aaa,bbb,ccc'))#");
				assertEquals("1", "#arrayLen(ListToArray(',,xx,,'))#");
				assertEquals("3", "#arrayLen(ListToArray('xx,xx,xx'))#");
				assertEquals("3", "#arrayLen(ListToArray(',,xx,xx,xx'))#");
				assertEquals("3", "#ListToArray(',,xx,,,xx,xx').size()#");
				assertEquals("aa,a,bbb,c,cc", "#arrayToList(ListToArray('aayaxybbbxycxccx',"xy"))#");
				assertEquals("aaaUaaa", "#arrayToList(ListToArray('aaaUaaa',"u"))#");
				assertEquals("aaa,aaa", "#arrayToList(ListToArray('aaaUaaa',"U"))#");

				assertEquals("sasa,asaSa", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',","))#");
				assertEquals(",,sasa,,,asaSa,,", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',",",true))#");
				assertEquals("sasa,asaSa", "#arrayToList(ListToArray(',,sasa,,,asaSa,,',",",false))#");

				assertEquals("a*b*c", "#arrayToList(ListToArray('a:-b:-c',':-',false,false),'*')#");
				assertEquals("a*b*c", "#arrayToList(ListToArray('a-b:c',':-',false,false),'*')#");
				assertEquals("a*b*c", "#arrayToList(ListToArray('a:-b:-c',':-',false,true),'*')#");
				assertEquals("a-b:c", "#arrayToList(ListToArray('a-b:c',':-',false,true),'*')#");

				assertEquals("a*b*c", "#arrayToList(ListToArray(':-:-a:-b:-:-c:-:-',':-',false,true),'*')#");
				assertEquals("**a*b**c**", "#arrayToList(ListToArray(':-:-a:-b:-:-c:-:-',':-',true,true),'*')#");

				assertEquals("**a*b****", "#arrayToList(ListToArray(':-x:-xa:-xb:-x:-x:-x:-x',':-x',true,true),'*')#");

				assertEquals("0", "#arrayLen(ListToArray('',',',false))#");
				assertEquals("1", "#arrayLen(ListToArray('',',',true))#");
			});

			it( title="LDEV-4083 positive limit, rest of the list is the last element", body=function( currentSpec ) {
				expect( listToArray( "a,b,c,d", ",", false, false, 2 ) ).toBe( [ "a", "b,c,d" ] );
				expect( listToArray( "a,b,c,d", ",", false, false, 1 ) ).toBe( [ "a,b,c,d" ] );
				expect( listToArray( "a,b,c,d", ",", false, false, 4 ) ).toBe( [ "a", "b", "c", "d" ] );
				expect( listToArray( "a,b,c,d", ",", false, false, 10 ) ).toBe( [ "a", "b", "c", "d" ] );
				expect( listToArray( "data:image/png;base64,iVBORw0KGgo=", ",", false, false, 2 ) ).toBe( [ "data:image/png;base64", "iVBORw0KGgo=" ] );
				expect( listToArray( "Host: example.com:8443", ":", false, false, 2 ) ).toBe( [ "Host", " example.com:8443" ] );
			});

			it( title="LDEV-4083 positive limit with empty fields", body=function( currentSpec ) {
				expect( listToArray( "a,,b,c", ",", false, false, 2 ) ).toBe( [ "a", "b,c" ] );
				expect( listToArray( ",,a,b", ",", false, false, 2 ) ).toBe( [ "a", "b" ] );
				expect( listToArray( ",,a,b", ",", false, false, 1 ) ).toBe( [ "a,b" ] );
				expect( listToArray( "a,b,,", ",", false, false, 2 ) ).toBe( [ "a", "b,," ] );
				expect( listToArray( "a,", ",", false, false, 2 ) ).toBe( [ "a" ] );
				expect( listToArray( "a,,b,c", ",", true, false, 2 ) ).toBe( [ "a", ",b,c" ] );
				expect( listToArray( "a,", ",", true, false, 2 ) ).toBe( [ "a", "" ] );
				expect( listToArray( "", ",", true, false, 2 ) ).toBe( [ "" ] );
				expect( listToArray( "", ",", false, false, 2 ) ).toBe( [] );
			});

			it( title="LDEV-4083 negative limit, only the first n elements", body=function( currentSpec ) {
				expect( listToArray( "a,b,c,d", ",", false, false, -2 ) ).toBe( [ "a", "b" ] );
				expect( listToArray( "a,b,c,d", ",", false, false, -1 ) ).toBe( [ "a" ] );
				expect( listToArray( ",,a,,b,c", ",", false, false, -2 ) ).toBe( [ "a", "b" ] );
				expect( listToArray( ",,a", ",", true, false, -2 ) ).toBe( [ "", "" ] );
				expect( listToArray( "a,b,", ",", true, false, -10 ) ).toBe( [ "a", "b", "" ] );
			});

			it( title="LDEV-4083 limit with multiple and multi character delimiters", body=function( currentSpec ) {
				expect( listToArray( "a-b:c-d", ":-", false, false, 2 ) ).toBe( [ "a", "b:c-d" ] );
				expect( listToArray( "a-b:c-d", ":-", false, false, -3 ) ).toBe( [ "a", "b", "c" ] );
				expect( listToArray( "a:-b:-c", ":-", false, true, 2 ) ).toBe( [ "a", "b:-c" ] );
				expect( listToArray( ":-:-a:-b:-:-c", ":-", false, true, 2 ) ).toBe( [ "a", "b:-:-c" ] );
				expect( listToArray( ":-:-a:-b", ":-", true, true, -3 ) ).toBe( [ "", "", "a" ] );
				expect( listToArray( "abc", "", false, false, 2 ) ).toBe( [ "a", "bc" ] );
				expect( listToArray( "abc", "", false, false, -2 ) ).toBe( [ "a", "b" ] );
			});

			it( title="LDEV-4083 limit as named argument and member function", body=function( currentSpec ) {
				expect( listToArray( list="a,b,c", limit=2 ) ).toBe( [ "a", "b,c" ] );
				expect( "a,b,c".listToArray( ",", false, false, -2 ) ).toBe( [ "a", "b" ] );
				expect( listToArray( "a,b,c", ",", false, false, 0 ) ).toBe( [ "a", "b", "c" ] );
			});

			it( title="LDEV-4083 negative limit matches arraySlice of the full split", body=function( currentSpec ) {
				var lists = [ "a,b,c", ",,a,,b,c,,", "a", ",", "a,,", ",a" ];
				for ( var list in lists ) {
					for ( var includeEmpty in [ true, false ] ) {
						var full = listToArray( list, ",", includeEmpty );
						for ( var n = 1; n <= arrayLen( full ) + 2; n++ ) {
							var expected = arrayLen( full ) ? arraySlice( full, 1, min( n, arrayLen( full ) ) ) : [];
							expect( listToArray( list, ",", includeEmpty, false, -n ) ).toBe( expected, "list=[#list#] includeEmpty=#includeEmpty# limit=-#n#" );
						}
					}
				}
			});
		});
	}
}