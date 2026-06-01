component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.cfcBody=new component {   
					function subTest() {
						return "inline"; 
					}  
				};   



	function run( testResults , testBox ) {
		describe( "simple test inline component", function() {
			it(title="test inline component", body=function() {
				var inline=new component {   
					function subTest() {
						return "inline"; 
					}  
				};   
				expect(inline.subTest()).toBe("inline");
				
				var md=getComponentMetaData(inline);
				expect(md.inline).toBe(true);
				expect(md.sub).toBe(false);
			});


			it(title="test correct linking of related to (LDEV-4884)", body=function() {
				
				function a(){
					return "a";
				}
				
				var inline=new component {   
					function b(){
						return "b";
					}  
					function c(){
						return "c";
					} 
				};   

				function d(){
					return "d";
				}

				expect(a()).toBe("a");
				expect(inline.b()).toBe("b");
				expect(inline.c()).toBe("c");
				expect(d()).toBe("d");
			});

			it(title="test inside function", body=function() {
				
				function func() {
					return new component {   
						function b(){
							return "b";
						}  
						function c(){
							return "c";
						} 
					}; 
				}

				expect(func().b()).toBe("b");
				expect(func().c()).toBe("c");
			});

			it(title="test component body loading", body=function() {
				expect(variables.cfcBody.subTest()).toBe("inline");
			});
			it(title="test inline component inside inline component", body=function() {

				var outer=new component {

					variables.inner=new component {
						function i() {
							return "i";
						}
					};

					function o(){
						return variables.inner.i();
					}
				};
				expect(outer.o()).toBe("i");
			});

		});

		describe( "inline metadata flag", function(){
			// `setInline()` flips ComponentProperties.inline = true once during
			// ComponentLoader.loadInline. The flag must surface via getMetaData and
			// must not leak to non-inline siblings of the same class.
			it( title="inline CFC reports inline=true in metadata", body=function( currentSpec ){
				var inlineCfc = new component {
					function hello() { return "inline-hi"; }
				};
				expect( getMetaData( inlineCfc ).inline ).toBeTrue();
			});
			it( title="regular file-backed CFC reports inline=false", body=function( currentSpec ){
				var c = new static.StaticHolder();
				expect( getMetaData( c ).inline ).toBeFalse();
			});
			it( title="duplicate of an inline CFC preserves inline=true", body=function( currentSpec ){
				var inlineCfc = new component {
					function hello() { return "inline-hi"; }
				};
				var d = duplicate( inlineCfc );
				expect( getMetaData( d ).inline ).toBeTrue();
				expect( d.hello() ).toBe( "inline-hi" );
			});
			it( title="creating a regular CFC after an inline CFC doesn't flip the regular's inline flag", body=function( currentSpec ){
				var inlineCfc = new component {
					function hello() { return "inline-hi"; }
				};
				expect( getMetaData( inlineCfc ).inline ).toBeTrue();
				var regular = new static.StaticHolder();
				expect( getMetaData( regular ).inline ).toBeFalse();
				expect( getMetaData( inlineCfc ).inline ).toBeTrue();
			});
		});
	}
}
