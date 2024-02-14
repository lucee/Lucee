component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults , testBox ) {
        describe( title = "Testcase for toScript function", body = function() {
            it( title = "checking toScript function for array", body = function( currentSpec ) {
                Array = [];
                Array[1] = "lucee";
                jsVar = serializeJson(Array);
                res = ToScript(Array, "jsVar");
                expect(res).toBe('jsVar=new Array();jsVar[0]="lucee";');
            });

            it( title = "checking toScript function for struct", body = function( currentSpec ) {
                Struct = {};
                Struct[1] = "lucee";
                jsVar = serializeJson(Struct);
                res = ToScript(Array, "jsVar");
                expect(res).toBe('jsVar=new Array();jsVar[0]="lucee";');
            });

            it( title = "checking toScript function for query", body = function( currentSpec ) {
                Query = queryNew( "name,age", "varchar,numeric", {name = "Susi", age = 20 } );
                res = ToScript(Query, "Query");
                expect(res).toBe('Query=new WddxRecordset();col0=new Array();col0[0]="Susi";Query["name"]=col0;col0=null;col1=new Array();col1[0]=20;Query["age"]=col1;col1=null;');
            });

            it( title = "checking toScript function for string", body = function( currentSpec ) {
                Str = "test";
                res = ToScript(Str, "Str");
                expect(res).toBe('Str="test";');
            });

            it( title = "checking toScript function for number", body = function( currentSpec ) {
                number = 10;
                res = ToScript(number, "number");
                expect(res).toBe('number=10;');
            });
        });
    }
}

