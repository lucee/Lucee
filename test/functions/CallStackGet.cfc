<!---
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){

		// ground-truth template paths from a real exception's cfcatch.tagContext.
		// throwing inside inner() yields the same call chain that callStackGet()
		// should report when invoked from inner() directly: inner <- outer <- wrapper
		// note: cfcatch.tagContext has {template, line, ...} but NOT function — function
		// names are only populated by CallStackGet._getTagContext.
		var templates = [];
		try {
			wrapper( "array", true );
		}
		catch ( any e ){
			for ( var i = 1; i <= 3; i++ ){
				templates.append( e.tagContext[ i ].template );
			}
		}
		variables.expectedTemplates = templates;
		variables.expectedFunctions = [ "inner", "outer", "wrapper" ];

		describe( "callStackGet()", function(){

			it( title="array: frames 1-3 match expected function names and tagContext templates", body=function(){
				var cs = wrapper( "array" );
				for ( var i = 1; i <= 3; i++ ){
					expect( cs[ i ].function ).toBeWithCase( variables.expectedFunctions[ i ] );
					expect( cs[ i ].template ).toBe( variables.expectedTemplates[ i ] );
				}
			});

			it( title="json: round-trips to same array shape", body=function(){
				var cs = deserializeJSON( wrapper( "json" ) );
				for ( var i = 1; i <= 3; i++ ){
					expect( cs[ i ].function ).toBeWithCase( variables.expectedFunctions[ i ] );
					expect( cs[ i ].template ).toBe( variables.expectedTemplates[ i ] );
				}
			});

			it( title="string: contains frames 1-3 as 'template.func():line;' entries", body=function(){
				var cs = wrapper( "string" );
				expect( cs ).toBeTypeOf( "string" );
				for ( var i = 1; i <= 3; i++ ){
					expect( cs ).toInclude( variables.expectedTemplates[ i ] & ".#variables.expectedFunctions[ i ]#():" );
				}
			});

			it( title="text is an alias for string", body=function(){
				expect( wrapper( "text" ) ).toBe( wrapper( "string" ) );
			});

			it( title="html: <ul class='-lucee-array'> contains frames 1-3", body=function(){
				var cs = wrapper( "html" );
				expect( cs ).toInclude( "<ul class='-lucee-array'>" );
				expect( cs ).toInclude( "</ul>" );
				for ( var i = 1; i <= 3; i++ ){
					expect( cs ).toInclude( variables.expectedTemplates[ i ] & ".#variables.expectedFunctions[ i ]#():" );
				}
			});

			it( title="throws for an unknown type", body=function(){
				expect( function(){ wrapper( "bogus" ); } ).toThrow();
			});
		});
	}

	// helpers: callStackGet  <-  inner  <-  outer  <-  wrapper
	// when doThrow is true, inner throws — used once at the top of run() to derive
	// ground-truth template paths from cfcatch.tagContext
	private function inner( f, boolean doThrow=false ){
		if ( arguments.doThrow ) throw( type="test", message="ground-truth stack" );
		return callStackGet( f );
	}
	private function outer( f, boolean doThrow=false ){
		return inner( f, arguments.doThrow );
	}
	private function wrapper( f, boolean doThrow=false ){
		return outer( f, arguments.doThrow );
	}
}
</cfscript>
