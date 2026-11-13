component extends = "org.lucee.cfml.test.LuceeTestCase" labels="syntax" {
	// built by concatenation so this test component itself does not contain the tags
	variables.cfcomponentTags = "<" & "cfcomponent><" & "/cfcomponent>";
	variables.cflocationTag = "<" & "cflocation>";

	function run( testResults , textBox) {
		describe("Testcase for LDEV-3228", function(){
            it( title="tag cfcomponent as string in tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.tagComp()
                res = tagComponent.test();
                expect(res).toBe(variables.cfcomponentTags);
            });

            it( title="Use other tag like cflocation as string in tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.tagComp()
                res = tagComponent.otherTest();
                expect(res).toBe(variables.cflocationTag);
            });

            it( title="cftag as string in cfscript inside tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.mixedComp()
                resOne = tagComponent.test();
                resTwo = tagComponent.otherTest();
                expect(resOne).toBe(variables.cfcomponentTags);
                expect(resTwo).toBe(variables.cflocationTag);
            });

            it( title="tag cfcomponent as string in script component", body=function( currentSpec ){
                try{
                    scriptComponent = new LDEV3228.scriptComp()
                    res = scriptComponent.test();
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe(variables.cfcomponentTags);
            });

            it( title="Use other tag like cflocation as string in script component", body=function( currentSpec ){
                try{
                    scriptComponent = new LDEV3228.scriptCompTwo()
                    res = scriptComponent.otherTest();
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe(variables.cflocationTag);
            });

            it( title="cftag as comment line in script component", body=function( currentSpec ){
                try{
                    scriptComponent = new LDEV3228.commentComp();
                    res = isObject(scriptComponent);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe(true);
            });
        });
    }
}