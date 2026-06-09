component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "OpenAI multipart response parsing", function() {

			it( title="parses plain text content", body=function() {
				var raw = {
					"choices": [ {
						"message": {
							"role": "assistant",
							"content": "Hello world"
						}
					} ]
				};
				var rsp = createResponse( raw );

				expect( rsp.getAnswer() ).toBe( "Hello world" );
				expect( rsp.isMultiPart() ).toBeFalse();
				expect( rsp.getAnswers().size() ).toBe( 1 );
				expect( rsp.getAnswers()[ 1 ].getAsString() ).toBe( "Hello world" );
			});

			it( title="parses multipart text content blocks", body=function() {
				var raw = {
					"choices": [ {
						"message": {
							"role": "assistant",
							"content": [
								{ "type": "text", "text": "Line one." },
								{ "type": "text", "text": " Line two." }
							]
						}
					} ]
				};
				var rsp = createResponse( raw );

				expect( rsp.getAnswer() ).toBe( "Line one. Line two." );
				expect( rsp.isMultiPart() ).toBeTrue();
				expect( rsp.getAnswers().size() ).toBe( 2 );
			});

			it( title="parses refusal content blocks", body=function() {
				var raw = {
					"choices": [ {
						"message": {
							"role": "assistant",
							"content": [
								{ "type": "refusal", "refusal": "Sorry, I can't help with that." }
							]
						}
					} ]
				};
				var rsp = createResponse( raw );

				expect( rsp.getAnswer() ).toBe( "Sorry, I can't help with that." );
				expect( rsp.getAnswers().size() ).toBe( 1 );
			});

			it( title="parses image_url data URI content", body=function() {
				// 1x1 red pixel PNG
				var pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
				var raw = {
					"choices": [ {
						"message": {
							"role": "assistant",
							"content": [
								{ "type": "text", "text": "Here is an image:" },
								{
									"type": "image_url",
									"image_url": {
										"url": "data:image/png;base64,#pngBase64#"
									}
								}
							]
						}
					} ]
				};
				var rsp = createResponse( raw );
				var parts = rsp.getAnswers();

				expect( rsp.getAnswer() ).toBe( "Here is an image:" );
				expect( rsp.isMultiPart() ).toBeTrue();
				expect( parts.size() ).toBe( 2 );
				expect( parts[ 2 ].isImage() ).toBeTrue();
				expect( parts[ 2 ].getContentType() ).toBe( "image/png" );
				expect( len( parts[ 2 ].getAsBinary() ) ).toBeGT( 0 );
			});

		});
	}

	private function createResponse( struct raw ) {
		var json = serializeJSON( raw );
		var interpreter = createObject( "java", "lucee.runtime.interpreter.JSONExpressionInterpreter" );
		var javaRaw = interpreter.interpret( javacast( "null", 0 ), json );
		return createObject( "java", "lucee.runtime.ai.openai.OpenAIResponse" ).init( javaRaw, "UTF-8" );
	}

}
