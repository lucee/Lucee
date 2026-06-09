component extends="org.lucee.cfml.test.LuceeTestCase" {

	private any function claudeResponse( struct raw ) {
		return createObject( "java", "lucee.runtime.ai.anthropic.ClaudeResponse" ).init( raw, "UTF-8" );
	}

	function run( testResults, testBox ) {
		describe( title="ClaudeResponse multipart parsing", body=function() {

			it( title="getAnswer concatenates multiple text blocks", body=function() {
				var raw = {
					content: [
						{ type: "text", text: "Line one." },
						{ type: "text", text: " Line two." }
					]
				};
				expect( claudeResponse( raw ).getAnswer() ).toBe( "Line one. Line two." );
			});

			it( title="getAnswers returns one part per text block", body=function() {
				var raw = {
					content: [
						{ type: "text", text: "first" },
						{ type: "text", text: "second" }
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( arrayLen( parts ) ).toBe( 2 );
				expect( resp.isMultiPart() ).toBeTrue();
			});

			it( title="single text block is not multipart", body=function() {
				var raw = { content: [ { type: "text", text: "only one" } ] };
				var resp = claudeResponse( raw );
				expect( resp.isMultiPart() ).toBeFalse();
				expect( resp.getAnswer() ).toBe( "only one" );
			});

			it( title="handles plain string content", body=function() {
				var resp = claudeResponse( { content: "plain text reply" } );
				expect( resp.getAnswer() ).toBe( "plain text reply" );
				expect( resp.isMultiPart() ).toBeFalse();
			});

			it( title="parses code blocks as text parts", body=function() {
				var raw = {
					content: [
						{ type: "code", text: "print('hi')" },
						{ type: "text", text: " Done." }
					]
				};
				var resp = claudeResponse( raw );
				expect( resp.getAnswer() ).toBe( "print('hi') Done." );
				expect( arrayLen( resp.getAnswers() ) ).toBe( 2 );
			});

			it( title="parses thinking blocks", body=function() {
				var raw = {
					content: [
						{ type: "thinking", thinking: "Let me consider..." },
						{ type: "text", text: "Final answer." }
					]
				};
				var resp = claudeResponse( raw );
				expect( resp.getAnswer() ).toBe( "Let me consider...Final answer." );
				expect( arrayLen( resp.getAnswers() ) ).toBe( 2 );
			});

			it( title="parses base64 image blocks", body=function() {
				var raw = {
					content: [
						{
							type: "image",
							source: {
								type: "base64",
								media_type: "image/png",
								data: toBase64( "png-bytes" )
							}
						}
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( arrayLen( parts ) ).toBe( 1 );
				expect( parts[ 1 ].isImage() ).toBeTrue();
				expect( resp.isMultiPart() ).toBeTrue();
				expect( resp.getAnswer() ).toBeNull();
			});

			it( title="parses base64 document blocks", body=function() {
				var raw = {
					content: [
						{
							type: "document",
							source: {
								type: "base64",
								media_type: "application/pdf",
								data: toBase64( "pdf-bytes" )
							}
						}
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( arrayLen( parts ) ).toBe( 1 );
				expect( parts[ 1 ].getContentType() ).toBe( "application/pdf" );
				expect( resp.isMultiPart() ).toBeTrue();
			});

			it( title="parses url source data URLs", body=function() {
				var raw = {
					content: [
						{
							type: "image",
							source: {
								type: "url",
								url: "data:image/png;base64,#toBase64( 'png-bytes' )#"
							}
						}
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( parts[ 1 ].isImage() ).toBeTrue();
				expect( toString( parts[ 1 ].getAsBinary() ) ).toBe( "png-bytes" );
			});

			it( title="parses tool_use as structured JSON part", body=function() {
				var raw = {
					content: [
						{
							type: "tool_use",
							id: "toolu_01",
							name: "get_weather",
							input: { location: "Vienna" }
						}
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( arrayLen( parts ) ).toBe( 1 );
				expect( parts[ 1 ].isStructured() ).toBeTrue();
				expect( parts[ 1 ].getAsStruct().name ).toBe( "get_weather" );
			});

			it( title="parses tool_result blocks", body=function() {
				var raw = {
					content: [
						{
							type: "tool_result",
							tool_use_id: "toolu_01",
							content: "22C and sunny"
						}
					]
				};
				var resp = claudeResponse( raw );
				var parts = resp.getAnswers();
				expect( arrayLen( parts ) ).toBe( 1 );
				expect( parts[ 1 ].isStructured() ).toBeTrue();
			});

			it( title="parses mixed text and image blocks", body=function() {
				var raw = {
					content: [
						{ type: "text", text: "Caption:" },
						{
							type: "image",
							source: {
								type: "base64",
								media_type: "image/jpeg",
								data: toBase64( "jpeg-bytes" )
							}
						}
					]
				};
				var resp = claudeResponse( raw );
				expect( arrayLen( resp.getAnswers() ) ).toBe( 2 );
				expect( resp.getAnswer() ).toBe( "Caption:" );
				expect( resp.isMultiPart() ).toBeTrue();
			});

		});
	}

}
