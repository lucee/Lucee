component extends = "org.lucee.cfml.test.LuceeTestCase" {
    function run( testResults, textbox ) {
        describe("testcase for LDEV-3658", function(){
            it(title="duplicate() with java.util.hashMap", body=function( currentSpec ){
                try {
                    res = "duplicate hashmap finished";
                    duplicate(createObject( "java", "java.lang.System" ).getEnv());
                }
                catch(any e) {
                    res = e.message;
                }
                expect(res).toBe("duplicate hashmap finished");
            });
            it(title="duplicate() with java.util.Collections", body=function( currentSpec ){
                try {
                    res = "duplicate java.util.Collections finished"
                    x = [ "a", "b", "c" ];
                    y = x.subList(1, 2); // returns java.util.Collections$SynchronizedRandomAccessList
                    z = duplicate(y); 
                }
                catch(any e) {
                    res = e.message;
                }
                expect(res).toBe("duplicate java.util.Collections finished");
            });
            it(title="Struct.Clone() with java.util.hashMap as child", body=function( currentSpec ){
                try {
                    res = "Struct.Clone() with java.util.hashMap worked";
                    ok["hashMapChild"] = createObject( "java", "java.lang.System" ).getenv();
                    cloned = ok.clone();
                }
                catch(any e) {
                    res = e.message;
                }
                expect(res).toBe("Struct.Clone() with java.util.hashMap worked");
            });
        });
    }
}