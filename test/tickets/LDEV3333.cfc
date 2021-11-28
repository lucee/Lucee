component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3333", function() {
            it( title="Circular references with struct", body=function(){
                var x = {};
                x.circular = x;

                try {
                    // note:
                    // acf behavior is to recursively expand this up to some limit, to `{circular: {circular: {circular: ... {circular: {} }}}}`
                    // lucee writes out {circular: <indicator-of-circularity>}
                    var didThrow = false;
                    savecontent variable="_" { writedump(x); } 
                }
                catch (any e) {
                    didThrow = true;
                }
                expect(didThrow).toBe(false, "Did not throw during `writeDump(<circular-struct>)`");

                try {
                    // acf behavior is to recursively expand this up to some limit, to `{circular: {circular: {circular: ... {circular: null}}}}`
                    // lucee just writes out {circular: null}
                    var didThrow = false;
                    serializeJSON(x);
                }
                catch (any e) {
                    var didThrow = true;
                }
                expect(didThrow).toBe(false, "Did not throw during `serializeJSON(<circular-struct>)`");
            });
            it( title="Circular references with array", body=function(){
                // note:
                // acf appends a copy
                // lucee appends a reference
                var x = [];
                x.append(x);

                try {
                    // acf has no problem with this, there is no recursion
                    // lucee should write out similar to a circular struct reference, i.e. `[<indicator-of-circularity>]`
                    var didThrow = false;
                    savecontent variable="_" { writedump(x); }
                }
                catch (any e) {
                    didThrow = true;
                }

                expect(didThrow).toBe(false, "Did not throw during `writeDump(<circular-array>)`");

                try {
                    // acf has no problem with this, there is no recursion
                    // for lucee, we have a circularity; current behavior is to serialize it to JSON as `null`
                    var didThrow = false;
                    serializeJSON(x);
                }
                catch (any e) {
                    didThrow = true;
                }

                expect(didThrow).toBe(false, "Did not throw during `serializeJSON(<circular-array>)`")
            });
            it ( title="CF engine reflective call to `System.identityHashCode` for 'arrays that contain themselves'", body = function() {
                x = createObject("java", "java.util.ArrayList").init();
                y = createObject("java", "java.util.ArrayList").init();
                x.add(y);
                y.add(x);
                System = createObject("java", "java.lang.System");
                hc = System.identityHashCode(x);
                expect(isValid("integer", hc)).toBe(true);
            });


        });
    }
}