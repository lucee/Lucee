component extends="org.lucee.cfml.test.LuceeTestCase" {

	private any function claudeResponse( struct raw ) {
		return createObject( "java", "lucee.runtime.ai.anthropic.ClaudeResponse" ).init( raw, "UTF-8" );
	}

	function run( testResults, testBox ) {
		describe( title="LDEV-6389 Claude multipart response parsing", body=function() {

			it( title="getAnswer concatenates multiple text blocks", body=function() {
				var raw = {
					content: [
						{ type: "text", text: "Line one." },
						{ type: "text", text: " Line two." }
					]
				};
				var resp = claudeResponse( raw );
				expect( resp.getAnswer() ).toBe( "Line one. Line two." );
			});

			it( title="getAnswers returns a part per text block", body=function() {
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

			it( title="getAnswers parses base64 image blocks", body=function() {
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

			it( title="getAnswers parses tool_use as structured JSON part", body=function() {
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

			it( title="handles plain string content", body=function() {
				var raw = { content: "plain text reply" };
				var resp = claudeResponse( raw );
				expect( resp.getAnswer() ).toBe( "plain text reply" );
				expect( resp.isMultiPart() ).toBeFalse();
			});

		});
	}

}
