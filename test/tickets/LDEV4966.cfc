component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

    function test(){
        var a = [];
        ArraySet( a, 1, 10000, true );
        
        arrayEach(a, function(){
            pagePoolClear();
            sleep(2);
            getComponentMetadata( "test.tickets.LDEV4966" );
            sleep(3);
        }, true );
    }

}
