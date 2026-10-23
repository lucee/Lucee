component extends="org.lucee.cfml.test.LuceeTestCase" labels="multipart" {

	function beforeAll() {
		variables.nul = charsetEncode( binaryDecode( "00", "hex" ), "iso-8859-1" );
		variables.template = "/test/general/FormMultipartUpload/echo.cfm";
	}

	private string function allBytes( required numeric count ) {
		var s = createObject( "java", "java.lang.StringBuilder" ).init();
		for ( var i = 0; i < arguments.count; i++ ) s.append( ( i mod 256 ) == 0 ? variables.nul : chr( i mod 256 ) );
		return s.toString();
	}

	// the parts are built as UTF-8 text for the headers and as bytes for the content
	private binary function body( required array parts, string boundary="XyZ" ) {
		var out = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
		for ( var p in arguments.parts ) {
			var h = "--" & arguments.boundary & chr( 13 ) & chr( 10 ) & 'Content-Disposition: form-data; name="#p.name#"';
			if ( structKeyExists( p, "filename" ) ) h &= '; filename="#p.filename#"';
			if ( structKeyExists( p, "type" ) ) h &= chr( 13 ) & chr( 10 ) & "Content-Type: " & p.type;
			h &= chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 );
			out.write( charsetDecode( h, "utf-8" ) );
			out.write( structKeyExists( p, "bytes" ) ? p.bytes : charsetDecode( p.value, "utf-8" ) );
			out.write( charsetDecode( chr( 13 ) & chr( 10 ), "utf-8" ) );
		}
		out.write( charsetDecode( "--" & arguments.boundary & "--" & chr( 13 ) & chr( 10 ), "utf-8" ) );
		return out.toByteArray();
	}

	private struct function post( required binary body, string files="", string boundary="XyZ" ) {
		var res = _internalRequest(
			template: variables.template,
			urls: { files: arguments.files },
			method: "POST",
			headers: { "Content-Type": "multipart/form-data; boundary=" & arguments.boundary },
			body: arguments.body
		);
		return deserializeJSON( res.filecontent );
	}

	function run( testResults, testBox ) {
		describe( "multipart/form-data requests, the form scope and cffile upload", function() {

			it( title="form fields with umlauts", body=function( currentSpec ) {
				var umlauts = chr( 246 ) & chr( 228 ) & chr( 252 ) & chr( 233 ) & chr( 224 ) & chr( 232 );
				var result = post( body( [
					{ name: "plain", value: "hello" },
					{ name: "umlauts", value: umlauts },
					{ name: "name" & umlauts, value: "v" & umlauts },
					{ name: "empty", value: "" },
					{ name: "multi", value: "line1" & chr( 13 ) & chr( 10 ) & "line2" }
				] ) );
				expect( result.fields.plain ).toBe( "hello" );
				expect( result.fields.umlauts ).toBe( umlauts );
				expect( result.fields[ "name" & umlauts ] ).toBe( "v" & umlauts );
				expect( result.fields.empty ).toBe( "" );
				expect( result.fields.multi ).toBe( "line1" & chr( 13 ) & chr( 10 ) & "line2" );
			});

			it( title="a file with binary content, the name and the type are kept", body=function( currentSpec ) {
				var content = charsetDecode( allBytes( 70000 ), "iso-8859-1" );
				var result = post( body( [
					{ name: "title", value: "my file" },
					{ name: "f1", filename: "data.bin", type: "application/octet-stream", bytes: content }
				] ), "f1" );
				expect( result.fields.title ).toBe( "my file" );
				expect( result.files.f1.clientFile ).toBe( "data.bin" );
				expect( result.files.f1.size ).toBe( 70000 );
				expect( result.files.f1.hash ).toBe( hash( content, "MD5" ) );
				expect( result.files.f1.contentType ).toBe( "application/octet-stream" );
			});

			it( title="a file name with umlauts", body=function( currentSpec ) {
				var name = "M" & chr( 252 ) & "ller-" & chr( 233 ) & ".txt";
				var result = post( body( [ { name: "f1", filename: name, type: "text/plain", value: "x" } ] ), "f1" );
				expect( result.files.f1.clientFile ).toBe( name );
				expect( result.files.f1.hash ).toBe( hash( "x", "MD5" ) );
			});

			it( title="a very long field name and a long file name", body=function( currentSpec ) {
				var fieldName = repeatString( "f", 600 );
				var name = repeatString( "n", 200 ) & ".txt";
				var result = post( body( [
					{ name: fieldName, value: "long" },
					{ name: "f1", filename: name, type: "text/plain", value: "x" }
				] ), "f1" );
				expect( result.fields[ fieldName ] ).toBe( "long" );
				expect( result.files.f1.clientFile ).toBe( name );
			});

			it( title="several files and fields in a row", body=function( currentSpec ) {
				var a = charsetDecode( allBytes( 3000 ), "iso-8859-1" );
				var b = charsetDecode( repeatString( "b", 100000 ), "iso-8859-1" );
				var result = post( body( [
					{ name: "f1", filename: "a.bin", type: "application/octet-stream", bytes: a },
					{ name: "x", value: "between" },
					{ name: "f2", filename: "b.txt", type: "text/plain", bytes: b },
					{ name: "y", value: "after" }
				] ), "f1,f2" );
				expect( result.files.f1.hash ).toBe( hash( a, "MD5" ) );
				expect( result.files.f2.hash ).toBe( hash( b, "MD5" ) );
				expect( result.fields.x ).toBe( "between" );
				expect( result.fields.y ).toBe( "after" );
			});

			it( title="a file of several megabytes", body=function( currentSpec ) {
				var content = charsetDecode( repeatString( allBytes( 4096 ), 768 ), "iso-8859-1" ); // 3 MB
				var result = post( body( [ { name: "f1", filename: "big.bin", type: "application/octet-stream", bytes: content } ] ), "f1" );
				expect( result.files.f1.size ).toBe( arrayLen( content ) );
				expect( result.files.f1.hash ).toBe( hash( content, "MD5" ) );
			});

			it( title="a file name sent as filename*", body=function( currentSpec ) {
				var raw = createObject( "java", "java.io.ByteArrayOutputStream" ).init();
				raw.write( charsetDecode( "--XyZ" & chr( 13 ) & chr( 10 ) & "Content-Disposition: form-data; name=""f1""; filename*=UTF-8''M%C3%BCller.txt" & chr( 13 ) & chr( 10 ) & "Content-Type: text/plain" & chr( 13 ) & chr( 10 ) & chr( 13 ) & chr( 10 ) & "x" & chr( 13 ) & chr( 10 ) & "--XyZ--" & chr( 13 ) & chr( 10 ), "utf-8" ) );
				var result = post( raw.toByteArray(), "f1" );
				expect( result.files.f1.clientFile ).toBe( "M" & chr( 252 ) & "ller.txt" );
			});

			it( title="an upload that is cut off leaves nothing behind: empty form scope and no temp file", body=function( currentSpec ) {
				var content = charsetDecode( repeatString( "0123456789", 20000 ), "utf-8" ); // 200 KB
				var full = body( [ { name: "title", value: "my file" }, { name: "f1", filename: "data.bin", type: "application/octet-stream", bytes: content } ] );
				var before = directoryList( getTempDirectory(), false, "name", "tmp-*.upload" );
				var cut = binaryDecode( left( binaryEncode( full, "hex" ), 200000 ), "hex" ); // 100000 bytes, in the middle of the file
				var res = _internalRequest(
					template: variables.template,
					urls: { files: "" },
					method: "POST",
					headers: { "Content-Type": "multipart/form-data; boundary=XyZ" },
					body: cut
				);
				var result = deserializeJSON( res.filecontent );
				expect( result.fields ).toBeEmpty();
				var after = directoryList( getTempDirectory(), false, "name", "tmp-*.upload" );
				expect( after ).toHaveLength( arrayLen( before ) );
			});

			it( title="a boundary with special characters", body=function( currentSpec ) {
				var boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
				var result = post( body( [ { name: "a", value: "one" } ], boundary ), "", boundary );
				expect( result.fields.a ).toBe( "one" );
			});
		});
	}
}
