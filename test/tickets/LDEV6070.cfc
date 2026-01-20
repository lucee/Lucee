component extends="org.lucee.cfml.test.LuceeTestCase" labels="ajax,form,url" {

	function beforeAll() {
		variables.uri = createURI( "LDEV6070" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6070: Support bracket notation in form/URL parameters", function() {

			it( "should parse simple bracket notation into nested struct", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user[name]": "John",
						"user[age]": "30"
					}
				);
				expect( result.filecontent ).toInclude( "user.name=John" );
				expect( result.filecontent ).toInclude( "user.age=30" );
			});

			it( "should parse deep bracket notation", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user[address][city]": "Sydney",
						"user[address][country]": "Australia"
					}
				);
				expect( result.filecontent ).toInclude( "user.address.city=Sydney" );
				expect( result.filecontent ).toInclude( "user.address.country=Australia" );
			});

			it( "should parse mixed dot and bracket notation", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user.address[city]": "Sydney",
						"user.address[zip]": "2000"
					}
				);
				expect( result.filecontent ).toInclude( "user.address.city=Sydney" );
				expect( result.filecontent ).toInclude( "user.address.zip=2000" );
			});

			it( "should handle array notation with brackets", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"tags[]": [ "java", "cfml", "lucee" ]
					}
				);
				expect( result.filecontent ).toInclude( "tags=java,cfml,lucee" );
			});

			it( "should handle complex nested structures", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"order[customer][name]": "Jane",
						"order[customer][email]": "jane@example.com",
						"order[items][0][product]": "Widget",
						"order[items][0][qty]": "2",
						"order[items][1][product]": "Gadget",
						"order[items][1][qty]": "5"
					}
				);
				expect( result.filecontent ).toInclude( "order.customer.name=Jane" );
				// Numeric indices create struct keys, not array indices
				expect( result.filecontent ).toInclude( "order.items.0.product=Widget" );
				expect( result.filecontent ).toInclude( "order.items.1.qty=5" );
			});

			// Remote CFC calls need different test approach - internalRequest doesn't work same way
			xit( "should work with remote CFC calls", function() {
				var result = _internalRequest(
					template: "#uri#/TestService.cfc?method=testMethod",
					forms: {
						"userData[name]": "John",
						"userData[sellerID]": "12345"
					}
				);
				var data = deserializeJSON( result.filecontent );
				expect( data ).toHaveKey( "userData" );
				expect( data.userData.name ).toBe( "John" );
				expect( data.userData.sellerID ).toBe( "12345" );
			});

			it( "should respect formUrlAsStruct=false setting", function() {
				var result = _internalRequest(
					template: "#uri#/test-disabled/test-disabled.cfm",
					forms: {
						"user[name]": "John"
					}
				);
				// Should have literal key, not nested
				expect( result.filecontent ).toInclude( "user[name]=John" );
			});

			it( "should handle malformed brackets - missing closing bracket", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user[name": "John"
					}
				);
				// Should treat as literal key when malformed
				expect( result.filecontent ).toInclude( "user[name=John" );
			});

			it( "should handle malformed brackets - missing opening bracket", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"username]": "John"
					}
				);
				// Should treat as literal key when malformed
				expect( result.filecontent ).toInclude( "username]=John" );
			});

			it( "should skip empty bracket segments", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user[]name": "John"
					}
				);
				// Empty brackets should be skipped: user[]name -> user.name
				expect( result.filecontent ).toInclude( "user.name=John" );
			});

			it( "should skip empty dot segments", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user..name": "John"
					}
				);
				// Empty dot segments should be skipped: user..name -> user.name
				expect( result.filecontent ).toInclude( "user.name=John" );
			});

			it( "should skip trailing dots and brackets", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user.": "John"
					}
				);
				// Trailing dot should be skipped: user. -> user
				expect( result.filecontent ).toInclude( "user=John" );
			});

			it( "should skip leading dots", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						".name": "John"
					}
				);
				// Leading dot should be skipped: .name -> name
				expect( result.filecontent ).toInclude( "name=John" );
			});

			it( "should handle special characters in bracket keys", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"user[first-name]": "John",
						"user[last_name]": "Doe"
					}
				);
				expect( result.filecontent ).toInclude( "user.first-name=John" );
				expect( result.filecontent ).toInclude( "user.last_name=Doe" );
			});

			it( "should handle numeric string keys vs array indices", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"items[0]": "first",
						"items[1]": "second",
						"items[foo]": "bar"
					}
				);
				// Numeric indices create struct keys, not array
				expect( result.filecontent ).toInclude( "items.0=first" );
				expect( result.filecontent ).toInclude( "items.1=second" );
				expect( result.filecontent ).toInclude( "items.foo=bar" );
			});

			it( "should handle deeply nested brackets", function() {
				var result = _internalRequest(
					template: "#uri#/test.cfm",
					forms: {
						"a[b][c][d][e][f]": "deep"
					}
				);
				expect( result.filecontent ).toInclude( "a.b.c.d.e.f=deep" );
			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
