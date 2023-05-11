component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run(testResults, textbox) {
        describe( title="Test suite for LDEV0-4423", body=function() {
            it( title='Test case for structdelete() with non existing key', body=function( currentSpec ) {
                var world = {"save":"water","clean":"wastes"};
                expect(structdelete(world,"earth", true)).toBeFalse();
                expect(structdelete(world,"earth", false)).toBeTrue();
            });
            it( title='Test case for Struct.delete() member function with non existing key', body=function( currentSpec ) {
                var world1 = {"save":"water","clean":"wastes"};
                expect(function() {
                    world1.delete("tree",true)
                }).toThrow();
                expect( function () { 
                    world1.delete("tree",false) 
                }).NotToThrow();
            });
        });
    }
}
