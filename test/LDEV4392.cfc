component extends = "org.lucee.cfml.test.LuceeTestCase" labels="wddx" {
    function run( testResults, textbox ) {
        describe("Testcase for LDEV-4392", function() {
        
            it(title="checking cfwddx tag with empty data", body=function( currentSpec ) {
                expect( function(){
                    cfwddx(action="wddx2cfml", input="<wddxPacket version='1.0'></wddxPacket>", output="variables.foo");
                }).notToThrow();

                expect( function(){
                    cfwddx(action="wddx2cfml", input="<wddxPacket version='1.0'><data/></wddxPacket>", output="variables.foo");
                }).notToThrow();
            });

            it(title="checking cfwddx tag with invalid root element", body=function( currentSpec ) {
                expect( function(){
                    cfwddx(action="wddx2cfml", input="<funPacket version='1.0'></funPacket>", output="variables.foo");
                }).toThrow(regex="Invalid WDDX packet: root element is not wddxPacket.");

                expect( function(){
                    cfwddx(action="wddx2cfml", input="<funPacket version='1.0'><data/></funPacket>", output="variables.foo");
                }).toThrow(regex="Invalid WDDX packet: root element is not wddxPacket.");
            });
            
        });
    }
}
