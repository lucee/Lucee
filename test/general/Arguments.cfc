component extends="org.lucee.cfml.test.LuceeTestCase"	{

    private function checkArgs(a, b, c){
        expect( arguments.a ).toBe( "aa" );
        expect( arguments.1 ).toBe( "aa" );
        expect( arguments[ 1 ] ).toBe( "aa" );

        expect( arguments.b ).toBe( "bb" );
        expect( arguments.2 ).toBe( "bb" );
        expect( arguments[ 2 ] ).toBe( "bb" );

        expect( arguments.c ).toBe( "cc" );
        expect( arguments.3 ).toBe( "cc" );
        expect( arguments[ 3 ] ).toBe( "cc" );
    }

    public function testArgs() {
        checkArgs( "aa" , "bb" , "cc" );
    }
}