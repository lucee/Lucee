component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for astFromString", body = function() {

			it( title = 'Test basic variable assignment parsing', body = function( currentSpec ) {
				var result = astFromString("<cfset x = 1>");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});

			it( title = 'Test AstUtil.astFromString method', body = function( currentSpec ) {
				var astUtil = new lucee.runtime.util.AstUtil();
				var result = astUtil.astFromString("<cfset test = 'util'>");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});


			it( title = 'echo literal string', body = function( currentSpec ) {
				var result = astFromString("Susi");
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"ExpressionStatement","expression":{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":4,"offset":4},"type":"StringLiteral","value":"Susi","raw":"\"Susi\""}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test loop tag', body = function( currentSpec ) {
				var result = astFromString('<cfloop from="1" to="10" index="i"></cfloop>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":44,"offset":44},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":44,"offset":44},"type":"CFMLTag","isBuiltIn":true,"name":"loop","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfloop","attributes":[{"name":"from","type":"Attribute","value":{"start":{"line":1,"column":13,"offset":13},"end":{"line":1,"column":16,"offset":16},"type":"NumberLiteral","raw":"1","value":1}},{"name":"to","type":"Attribute","value":{"start":{"line":1,"column":20,"offset":20},"end":{"line":1,"column":24,"offset":24},"type":"NumberLiteral","raw":"10","value":10}},{"name":"index","type":"Attribute","value":{"start":{"line":1,"column":31,"offset":31},"end":{"line":1,"column":34,"offset":34},"type":"StringLiteral","value":"i","raw":"\"i\""}}],"body":{"type":"BlockStatement","body":[]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test variable assignment with single data member', body = function( currentSpec ) {
				var result = astFromString('<cfscript>a.b.c=d;</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":29,"offset":29},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":29,"offset":29},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":17,"offset":17},"type":"AssignmentExpression","operator":"ASSIGN","left":{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":11,"offset":11},"type":"MemberExpression","computed":false,"object":{"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"A"},"property":{"type":"Identifier","name":"B"}},"property":{"type":"Identifier","name":"C"}},"right":{"start":{"line":1,"column":16,"offset":16},"end":{"line":1,"column":17,"offset":17},"type":"Identifier","name":"D"}}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test variable assignment with 2 data member', body = function( currentSpec ) {
				var result = astFromString('<cfscript>a.b.c=d.e;</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":31,"offset":31},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":31,"offset":31},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":19,"offset":19},"type":"AssignmentExpression","operator":"ASSIGN","left":{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":11,"offset":11},"type":"MemberExpression","computed":false,"object":{"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"A"},"property":{"type":"Identifier","name":"B"}},"property":{"type":"Identifier","name":"C"}},"right":{"start":{"line":1,"column":16,"offset":16},"end":{"line":1,"column":17,"offset":17},"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"D"},"property":{"type":"Identifier","name":"E"}}}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test variable assignment with function call', body = function( currentSpec ) {
				var result = astFromString('<cfscript>a.b.c=d(1,true,"");</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":40,"offset":40},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":40,"offset":40},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":28,"offset":28},"type":"AssignmentExpression","operator":"ASSIGN","left":{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":11,"offset":11},"type":"MemberExpression","computed":false,"object":{"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"A"},"property":{"type":"Identifier","name":"B"}},"property":{"type":"Identifier","name":"C"}},"right":{"start":{"line":1,"column":16,"offset":16},"end":{"line":1,"column":28,"offset":28},"type":"CallExpression","callee":{"type":"Identifier","name":"D"},"arguments":[{"start":{"line":1,"column":18,"offset":18},"end":{"line":1,"column":19,"offset":19},"type":"NumberLiteral","raw":"1","value":1},{"start":{"line":1,"column":20,"offset":20},"end":{"line":1,"column":24,"offset":24},"type":"BooleanLiteral","value":true},{"start":{"line":1,"column":25,"offset":25},"end":{"line":1,"column":27,"offset":27},"type":"StringLiteral","value":"","raw":"\"\""}]}}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test variable assignment with scopes', body = function( currentSpec ) {
				var result = astFromString('<cfscript>variables.a=url.a;</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":39,"offset":39},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":39,"offset":39},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":27,"offset":27},"type":"AssignmentExpression","operator":"ASSIGN","left":{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":19,"offset":19},"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"VARIABLES"},"property":{"type":"Identifier","name":"A"}},"right":{"start":{"line":1,"column":22,"offset":22},"end":{"line":1,"column":25,"offset":25},"type":"MemberExpression","computed":false,"object":{"type":"Identifier","name":"URL"},"property":{"type":"Identifier","name":"A"}}}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});
			it( title = 'test positional arguments', body = function( currentSpec ) {
				var result = astFromString('<cfscript>whatever(1,true,"abc");</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":44,"offset":44},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":44,"offset":44},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":32,"offset":32},"type":"CallExpression","callee":{"type":"Identifier","name":"WHATEVER"},"arguments":[{"start":{"line":1,"column":19,"offset":19},"end":{"line":1,"column":20,"offset":20},"type":"NumberLiteral","raw":"1","value":1},{"start":{"line":1,"column":21,"offset":21},"end":{"line":1,"column":25,"offset":25},"type":"BooleanLiteral","value":true},{"start":{"line":1,"column":26,"offset":26},"end":{"line":1,"column":31,"offset":31},"type":"StringLiteral","value":"abc","raw":"\"abc\""}]}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
					);
			});

			it( title = 'test named arguments', body = function( currentSpec ) {
				var result = astFromString('<cfscript>whatever(arg1=1, arg2=true, arg3="abc");</cfscript>');
				assertEquals(
					'{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":61,"offset":61},"type":"Program","body":[{"start":{"line":1,"column":0,"offset":0},"end":{"line":1,"column":61,"offset":61},"type":"CFMLTag","isBuiltIn":true,"name":"script","nameSpace":"cf","nameSpaceSeparator":"","fullname":"cfscript","attributes":[],"body":{"type":"BlockStatement","body":[{"start":{"line":1,"column":10,"offset":10},"end":{"line":1,"column":49,"offset":49},"type":"CallExpression","callee":{"type":"Identifier","name":"WHATEVER"},"arguments":[{"type":"NamedArgument","name":{"start":{"line":1,"column":19,"offset":19},"end":{"line":1,"column":23,"offset":23},"type":"Identifier","name":"ARG1"},"value":{"start":{"line":1,"column":24,"offset":24},"end":{"line":1,"column":25,"offset":25},"type":"NumberLiteral","raw":"1","value":1}},{"type":"NamedArgument","name":{"start":{"line":1,"column":27,"offset":27},"end":{"line":1,"column":31,"offset":31},"type":"Identifier","name":"ARG2"},"value":{"start":{"line":1,"column":32,"offset":32},"end":{"line":1,"column":36,"offset":36},"type":"BooleanLiteral","value":true}},{"type":"NamedArgument","name":{"start":{"line":1,"column":38,"offset":38},"end":{"line":1,"column":42,"offset":42},"type":"Identifier","name":"ARG3"},"value":{"start":{"line":1,"column":43,"offset":43},"end":{"line":1,"column":48,"offset":48},"type":"StringLiteral","value":"abc","raw":"\"abc\""}}]}]}}],"sourceType":"tag","dotNotationUpperCase":true,"handleUnquotedAttrValueAsString":true}',
					serializeJSON(var:result,compact:true)
				);
			});

			it( title = 'test unknown tag self-closing without slash', body = function( currentSpec ) {
				var pre='<cf';
				var result = astFromString(pre&'Unknown susi="1">');
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("unknown", result.body[1].name);
				assertEquals("cf", result.body[1].nameSpace);
				assertEquals("cfUnknown", result.body[1].fullname);
				assertEquals(1, arrayLen(result.body[1].attributes));
				assertEquals("susi", result.body[1].attributes[1].name);
			});

			it( title = 'test unknown tag self-closing with slash', body = function( currentSpec ) {
				// we cannot define a cf tag in the string, Lucee will still parse this
				var prefix="cf";
				var result = astFromString('<#prefix#Unknown susi="1"/>');
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("unknown", result.body[1].name);
				assertEquals("cf", result.body[1].nameSpace);
				assertEquals("cfUnknown", result.body[1].fullname);
				assertEquals(1, arrayLen(result.body[1].attributes));
				assertEquals("susi", result.body[1].attributes[1].name);
			});

			it( title = 'test unknown tag with body content', body = function( currentSpec ) {
				var prefix="cf";
				var result = astFromString('<#prefix#Unknown susi="1">ddd</#prefix#Unknown>');
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("unknown", result.body[1].name);
				assertEquals("cf", result.body[1].nameSpace);
				assertEquals("cfUnknown", result.body[1].fullname);
				assertEquals(1, arrayLen(result.body[1].attributes));
				assertEquals("susi", result.body[1].attributes[1].name);
				assertTrue(structKeyExists(result.body[1], "body"));
			});

			it( title = 'test unknown tag with string content', body = function( currentSpec ) {
				var prefix="cf";
				var result = astFromString('<#prefix#script>
```
<#prefix#Unknown susi="1">ddd</#prefix#Unknown>
``` 
</#prefix#script>');
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("script", result.body[1].name);
			});

			it( title = 'test known script tag in legacy style', body = function( currentSpec ) {
				var result = astFromString('loop from=1 to=10 index="x" {echo("ddd");}',"script");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("loop", result.body[1].name);
			});

			it( title = 'test known script tag in function style', body = function( currentSpec ) {
				var result = astFromString('cfloop(from=1, to=10, index="x") {echo("ddd");}',"script");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("loop", result.body[1].name);
			});

			it( title = 'test mode=tag works explicitly', body = function( currentSpec ) {
				var result = astFromString("<cfset x = 1>", "tag");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
			});

			it( title = 'test invalid mode throws error', body = function( currentSpec ) {
				var errorThrown = false;
				try {
					astFromString("x = 1;", "banana");
				}
				catch( any e ) {
					errorThrown = true;
					assertTrue( e.message contains "mode" || e.detail contains "mode", "Error should mention 'mode'" );
				}
				assertTrue( errorThrown, "Expected an error for invalid mode 'banana'" );
			});

			xit( title = 'test unknown script tag in legacy style', body = function( currentSpec ) {
				var result = astFromString('unknown susi="1" {echo("ddd");}',"script");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("unknown", result.body[1].name);
				assertEquals("cf", result.body[1].nameSpace);
				assertEquals("cfUnknown", result.body[1].fullname);
				assertEquals(1, arrayLen(result.body[1].attributes));
				assertEquals("susi", result.body[1].attributes[1].name);
				assertTrue(structKeyExists(result.body[1], "body"));
			});
			xit( title = 'test unknown script tag in function style', body = function( currentSpec ) {
				var result = astFromString('cfUnknown(susi="1") {echo("ddd");}',"script");
				assertEquals("Program", result.type);
				assertTrue(arrayLen(result.body) > 0);
				assertEquals("CFMLTag", result.body[1].type);
				assertEquals("unknown", result.body[1].name);
				assertEquals("cf", result.body[1].nameSpace);
				assertEquals("cfUnknown", result.body[1].fullname);
				assertEquals(1, arrayLen(result.body[1].attributes));
				assertEquals("susi", result.body[1].attributes[1].name);
				assertTrue(structKeyExists(result.body[1], "body"));
			});

		});
	}
}