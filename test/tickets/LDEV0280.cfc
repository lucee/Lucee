component extends="org.lucee.cfml.test.LuceeTestCase" skip=true{
    function run( testResults , testBox ) {
        describe( "Test suite for LDEV-1827 & LDEV-280", function() {
            variables.obj = new LDEV0280.test();
            it( title='checking final variable', body=function( currentSpec ) {
                assertEquals("foo", obj.bar());
            });
            it( title='checking and trying to change final static variable', body=function( currentSpec ) {
                try {
                    hasError = false;
                    var staticVar = LDEV0280.test::staticProperty;
                    LDEV0280.test::staticProperty = "newValue";
                }
                catch(any e) {
                    hasError = true;
                }
                assertEquals("staticValue", staticVar);
                assertEquals(true, hasError);
            });
            it( title='checking and trying to change final instance variable', body=function( currentSpec ) {
                try {
                    hasError = false;
                    var insVar = obj.instanceProperty;
                    obj.instanceProperty = "newValue";
                }
                catch(any e) {
                    hasError = true;
                }
                assertEquals("Value", insVar);
                assertEquals(true, hasError);
            });
        });
    }
} 