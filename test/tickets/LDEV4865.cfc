component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {
		describe( "Test case for LDEV-4865", function() {

			it( title="dump html contains the path of struct keys and array indexes", body=function( currentSpec ) {
				var data = { "items": [ { "name": "a" }, { "name": "b" } ], "title": "t" };
				var paths = htmlPaths( data, "data" );

				expect( paths ).toInclude( "data.items" );
				expect( paths ).toInclude( "data.items[1]" );
				expect( paths ).toInclude( "data.items[1].name" );
				expect( paths ).toInclude( "data.items[2]" );
				expect( paths ).toInclude( "data.items[2].name" );
				expect( paths ).toInclude( "data.title" );
			});

			it( title="dump html uses bracket notation for keys that are not plain identifiers", body=function( currentSpec ) {
				var data = { "my key": 1, "a.b": 2, "it's": 3, "1": 4, "a-b": 5, "say ""hi""": 6, "x": { "y z": 7 } };
				var paths = htmlPaths( data, "data" );

				expect( paths ).toInclude( "data['my key']" );
				expect( paths ).toInclude( "data['a.b']" );
				expect( paths ).toInclude( "data['it''s']" );
				expect( paths ).toInclude( "data['1']" );
				expect( paths ).toInclude( "data['a-b']" );
				expect( paths ).toInclude( "data['say ""hi""']" );
				expect( paths ).toInclude( "data.x" );
				expect( paths ).toInclude( "data.x['y z']" );
			});

			it( title="dump html contains the path of query cells", body=function( currentSpec ) {
				var data = { "q": queryNew( "id,name,my col", "integer,varchar,varchar", [ [ 1, "x", "z" ], [ 2, "y", "w" ] ] ) };
				var paths = htmlPaths( data, "data" );

				expect( paths ).toInclude( "data.q" );
				expect( paths ).toInclude( "data.q.id[1]" );
				expect( paths ).toInclude( "data.q.name[1]" );
				expect( paths ).toInclude( "data.q['my col'][1]" );
				expect( paths ).toInclude( "data.q.id[2]" );
				expect( paths ).toInclude( "data.q.name[2]" );
				expect( paths ).toInclude( "data.q['my col'][2]" );
			});

			it( title="dump html contains the path of the members of a component", body=function( currentSpec ) {
				var cmp = new LDEV4865.Sample();
				var paths = htmlPaths( cmp, "cmp" );

				expect( paths ).toInclude( "cmp.hello" );
				expect( paths ).toInclude( "cmp.pub" );
				expect( paths ).toInclude( "variables.firstName" );
			});

			it( title="dump html contains the path of nested components, structs and queries within a component", body=function( currentSpec ) {
				var data = { "obj": new LDEV4865.Sample(), "list": [ queryNew( "a", "varchar", [ [ "x" ] ] ) ] };
				var paths = htmlPaths( data, "data" );

				expect( paths ).toInclude( "data.obj" );
				expect( paths ).toInclude( "data.obj.hello" );
				expect( paths ).toInclude( "data.obj.pub" );
				expect( paths ).toInclude( "data.list[1]" );
				expect( paths ).toInclude( "data.list[1].a[1]" );
			});

			it( title="dump html contains relative paths when the name of the variable is not known", body=function( currentSpec ) {
				var data = { "items": [ { "name": "a" } ] };
				var paths = htmlPaths( data );
				expect( paths ).toInclude( "items" );
				expect( paths ).toInclude( "items[1].name" );

				paths = htmlPaths( [ { "a": 1 } ] );
				expect( paths ).toInclude( "[1]" );
				expect( paths ).toInclude( "[1].a" );
			});

			it( title="dump html with a label keeps the paths", body=function( currentSpec ) {
				var data = { "items": [ 1 ] };
				var paths = htmlPaths( data, "data", "my label" );
				expect( paths ).toInclude( "data.items" );
				expect( paths ).toInclude( "data.items[1]" );
			});

			it( title="dump html escapes the path in the attribute", body=function( currentSpec ) {
				var data = { "<b>x</b>": 1 };
				var html = renderDump( data, "html", "data" );
				expect( html ).toInclude( 'data-path="data[''&lt;b&gt;x&lt;/b&gt;'']"' );
				expect( html ).notToInclude( 'data-path="data[''<b>' );
			});

			it( title="dump html writes the click handler only once per request", body=function( currentSpec ) {
				var hadHeader = structKeyExists( request, "__Lucee_dump" );
				var saved = hadHeader ? request.__Lucee_dump : nullValue();
				try {
					structDelete( request, "__Lucee_dump" );
					var first = renderDump( { "a": 1 }, "html" );
					var second = renderDump( { "a": 1 }, "html" );
				}
				finally {
					if ( hadHeader ) request.__Lucee_dump = saved;
					else structDelete( request, "__Lucee_dump" );
				}

				expect( first ).toInclude( "navigator.clipboard" );
				expect( find( "window.__Lucee = ", first ) ).toBeGT( 0 );
				expect( find( "window.__Lucee = ", second ) ).toBe( 0 );
				expect( second ).toInclude( 'data-path="a"' );
			});

			it( title="dump text is not changed by the paths", body=function( currentSpec ) {
				var data = { "items": [ { "name": "a" } ], "q": queryNew( "id", "integer", [ [ 1 ] ] ), "obj": new LDEV4865.Sample() };
				var text = renderDump( data, "text", "data" );

				expect( text ).notToInclude( "data-path" );
				expect( text ).notToInclude( "__Lucee" );
				expect( text ).toInclude( "struct" );
				expect( text ).toInclude( "items" );
				expect( text ).toInclude( "name" );
			});

			it( title="dump classic and simple are not changed by the paths", body=function( currentSpec ) {
				var data = { "items": [ { "name": "a" } ] };
				for ( var format in [ "classic", "simple" ] ) {
					var html = renderDump( data, format, "data" );
					expect( html ).notToInclude( "data-path" );
					expect( html ).notToInclude( "__Lucee" );
					expect( html ).toInclude( "items" );
				}
			});

			it( title="dumpStruct does not contain paths", body=function( currentSpec ) {
				var meta = dumpStruct( { "a": [ 1 ] } );
				expect( serializeJSON( meta ) ).notToInclude( "path" );
			});

		});
	}

	private string function renderDump( required any data, required string format, string eval="", string label="" ) {
		var result = "";
		try {
			savecontent variable="result" {
				if ( len( arguments.eval ) ) {
					// eval is evaluated in the variables scope of this component, it names the root of the paths
					variables[ arguments.eval ] = arguments.data;
					if ( len( arguments.label ) ) dump( eval=arguments.eval, label=arguments.label, format=arguments.format );
					else dump( eval=arguments.eval, format=arguments.format );
				}
				else dump( var=arguments.data, format=arguments.format );
			}
		}
		finally {
			if ( len( arguments.eval ) ) structDelete( variables, arguments.eval );
		}
		return result;
	}

	private array function htmlPaths( required any data, string eval="", string label="" ) {
		// the header is only written once per request, the paths do not depend on it
		var html = renderDump( arguments.data, "html", arguments.eval, arguments.label );
		var matches = reMatch( 'data-path="[^"]*"', html );
		var paths = [];
		for ( var m in matches ) {
			var p = mid( m, 12, len( m ) - 12 );
			p = replace( p, "&lt;", "<", "all" );
			p = replace( p, "&gt;", ">", "all" );
			p = replace( p, "&quot;", '"', "all" );
			p = replace( p, "&amp;", "&", "all" );
			arrayAppend( paths, p );
		}
		return paths;
	}

}
