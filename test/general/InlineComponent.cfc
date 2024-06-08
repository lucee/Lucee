component extends="org.lucee.cfml.test.LuceeTestCase" {

	variables.cfcBody=new component {   
					function subTest() {
						return "inline"; 
					}  
				};   



	function run( testResults , testBox ) {
		describe( "simple test inline component", function() {
			it(title="test inline component", body=function() {
				inline=new component {   
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
				
				inline=new component {   
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
				
				outer=new component {

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
	}
}
