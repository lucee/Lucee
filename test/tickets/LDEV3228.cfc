component extends = "org.lucee.cfml.test.LuceeTestCase" skip="true" {
	function run( testResults , textBox) {
		describe("Testcase for LDEV-3228", function(){
            it( title="tag cfcomponent as string in tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.tagComp()
                res = tagComponent.test();
                expect(encodeForHTML(res)).toBe('&lt;cfcomponent&gt;&lt;&##x2f;cfcomponent&gt;');
            });

            it( title="Use other tag like cflocation as string in tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.tagComp()
                res = tagComponent.otherTest();
                expect(encodeForHTML(res)).toBe('&lt;cflocation&gt;');
            });

            it( title="cftag as string in cfscript inside tag component", body=function( currentSpec ){
                tagComponent = new LDEV3228.mixedComp()
                resOne = tagComponent.test();
                resTwo = tagComponent.otherTest();
                expect(encodeForHTML(resOne)).toBe('&lt;cfcomponent&gt;&lt;&##x2f;cfcomponent&gt;');
                expect(encodeForHTML(resTwo)).toBe('&lt;cflocation&gt;');
            });

            it( title="tag cfcomponent as string in script component", body=function( currentSpec ){
                try{
                    scriptComponent = new LDEV3228.scriptComp()
                    res = scriptComponent.test();
                }
                catch(any e){
                    res = e.message;
                }
                expect(encodeForHTML(res)).toBe('&lt;cfcomponent&gt;&lt;&##x2f;cfcomponent&gt;');
            });

            it( title="Use other tag like cflocation as string in script component", body=function( currentSpec ){
                try{
                    scriptComponent = new LDEV3228.scriptCompTwo()
                    res = scriptComponent.otherTest();
                }
                catch(any e){
                    res = e.message;
                }
                expect(encodeForHTML(res)).toBe('&lt;cflocation&gt;');
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