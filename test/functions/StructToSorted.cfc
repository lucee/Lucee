component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.myStr = StructNew();
		myStr.dd="dd";
		myStr.cc="cc";
		myStr.aa="aa";
		myStr.bb="bb";

		variables.myNumb = StructNew();
		myNumb.4="4";
		myNumb.3="3";
		myNumb.2="2";
		myNumb.1="1";
	}

	function run( testResults , testBox ) {

		describe( "test case for StructToSorted BIF", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = StructToSorted( duplicate( myStr ),"text", "asc" );
				expect( res.keyList() ).toBe( 'AA,BB,CC,DD', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'AA', 'BB', 'CC', 'DD' ] );
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = StructToSorted(duplicate(myStr),"text","desc");
				expect( res.keyList() ).toBe( 'DD,CC,BB,AA', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'DD','CC','BB','AA' ] );
			});

			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = StructToSorted( duplicate( myNumb ), "numeric", "asc" );
				expect( res.keyList() ).toBe( '1,2,3,4', res.toJson());
				expect( res.valueArray() ).toBe( [ 1, 2, 3, 4 ] );
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = StructToSorted( duplicate( myNumb ), "numeric", "desc" );
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});
		});

		describe( "test case StructToSorted - member functions", function() {
			it(title = "Checking string type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = mystr.ToSorted("text","asc");
				expect( res.keyList() ).toBe( 'AA,BB,CC,DD', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'AA', 'BB', 'CC', 'DD' ] );
			});
			it(title = "Checking string type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = mystr.ToSorted("text","desc");
				expect( res.keyList() ).toBe( 'DD,CC,BB,AA', res.toJson() );
				expect( res.valueArray() ).toBe( [ 'DD','CC','BB','AA' ] );
			});

			it(title = "Checking Numeric type with StructToSorted() in ASC order", body = function( currentSpec ) {
				var res = myNumb.ToSorted("numeric","asc");
				expect( res.keyList() ).toBe( '1,2,3,4', res.toJson());
				expect( res.valueArray() ).toBe( [ 1, 2, 3, 4 ] );
			});
			it(title = "Checking Numeric type with StructToSorted() in DESC order", body = function( currentSpec ) {
				var res = myNumb.ToSorted("numeric","desc");
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});
		});

		describe( "test case StructToSorted - nested struct", function() {
			it(title = "Checking nested struct", body = function( currentSpec ) {
				var nestedStruct = {
					"apple" = { "price" = 3, "quantity" = 10 },
					"banana" = { "price" = 1, "quantity" = 5 },
					"cherry" = { "price" = 2, "quantity" = 8 },
					"date" = { "price" = 4, "quantity" = 12 }
				};
				var sortedNestedStruct = nestedStruct.toSorted("text", "desc");
				var keys = structKeyArray( sortedNestedStruct );
				expect( keys[ 1 ] ).toBe( "date" );
				expect( keys[ 4 ] ).toBe( "apple" );
				expect( sortedNestedStruct[ keys[ 1 ] ].quantity ).toBe( 12 );
				expect( sortedNestedStruct[ keys[ 4 ] ].price ).toBe( 3 );
			});
		});

		describe( "test case StructToSorted - with callback by key", function() {
			it(title = "structSort BIF Callback by key", body = function( currentSpec ) {
				var cb = function( value1, value2, key1, key2 ){
					if (arguments.key1 < arguments.key2 ) // i.e. desc
						return 1;
					else
						return -1;
				};
				var res = StructToSorted( duplicate( myNumb ), cb );
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});

			it(title = "structSort member function with callback by key", body = function( currentSpec ) {
				var cb = function( value1, value2, key1, key2 ){
					if (arguments.key1 < arguments.key2 ) // i.e. desc
						return 1;
					else
						return -1;
				};
				var res = myNumb.ToSorted( cb );
				expect( res.keyList() ).toBe( '4,3,2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 4, 3, 2, 1 ] );
			});
		});

		describe( "test case StructToSorted - with callback by value", function() {
			it(title = "structSort BIF Callback by value", body = function( currentSpec ) {
				var cb = function( value1, value2, key1, key2 ){
					if (arguments.value1 > arguments.value2 )
						return 1;
					else
						return -1;
				};
				var st = {
					1: 2,
					2: 1
				};
				var res = StructToSorted( st, cb );
				expect( res.keyList() ).toBe( '2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 1, 2 ] );
			});

			it(title = "structSort member function with callback by value", body = function( currentSpec ) {
				var cb = function( value1, value2, key1, key2 ){
					if (arguments.value1 > arguments.value2 )
						return 1;
					else
						return -1;
				};
				var st = {
					1: 2,
					2: 1
				};
				var res = st.ToSorted( cb );
				expect( res.keyList() ).toBe( '2,1', res.toJson() );
				expect( res.valueArray() ).toBe( [ 1, 2 ] );
			});
		});
		// LDEV-6302: toSorted() required a sort direction
		describe("test case for LDEV-6302: struct.toSorted/StructToSorted", function() {
			it( title="member toSorted('text') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(myStr).toSorted("text"), ",")
				).toBe("aa,bb,cc,dd");
			} );

			it( title="member toSorted('textNoCase') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(myStr).toSorted("textNoCase"), ",")
				).toBe("aa,bb,cc,dd");
			} );

			it( title="member toSorted('numeric') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(duplicate(myNumb).toSorted("numeric"), ",")
				).toBe("1,2,3,4");
			} );

			it( title="StructToSorted(base,'text') with one argument defaults to ascending order", body=function( currentSpec ) {
				expect(
					structKeyList(StructToSorted(duplicate(myStr), "text"), ",")
				).toBe("aa,bb,cc,dd");
			} );
		} );
		
	}
}

