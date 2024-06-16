component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {

    function run( testResults, testBox ) {
        describe( "Testcase for LDEV-4899", function() {
            it( title="testing elvis with default closure", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: function(){ return "" };
            });
            it( title="testing elvis with default lambda", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: () => "susi";
            });

            it( title="testing elvis with default array", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [1,2,3];
            });

            it( title="testing elvis with default struct", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: {a:1};
            });

            it( title="testing elvis with default component", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: new Query();
            });

            it( title="testing elvis with default inline component", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: new component {};
            });


            it( title="testing elvis with default array of closure", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [function(){ return "" }];
            });
            it( title="testing elvis with default  array of lambda", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [() => "susi"];
            });

            it( title="testing elvis with default  array of struct", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [{a:1}];
            });

            it( title="testing elvis with default  array of component", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [new Query()];
            });

            it( title="testing elvis with default  array of inline component", skip="true", body=function( currentSpec ) {
                var prop = a.b.c   ?: [new component {}];
            });



        });
    }
}
