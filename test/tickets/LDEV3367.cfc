component extends="org.lucee.cfml.test.LuceeTestCase" {

	// serializeJson over a concurrently-mutated List must not throw
	// ConcurrentModificationException. Original report (2021) was ColdBox's async
	// LogEvent.cfc — cfthread serialising a shared log buffer while the buffer was
	// being appended to. Race fires in JSONConverter._serializeList at it.next()
	// (fail-fast ListIterator) when any other thread bumps the List's modCount.

	public void function testSerializeJsonOverConcurrentlyMutatedList() {
		var sharedArr = createObject( "java", "java.util.ArrayList" ).init();
		for ( var i = 1; i <= 500; i++ ) sharedArr.add( "item-" & i );

		var cmeCount  = createObject( "java", "java.util.concurrent.atomic.AtomicLong" ).init( javaCast( "long", 0 ) );
		var otherErrs = createObject( "java", "java.util.concurrent.atomic.AtomicLong" ).init( javaCast( "long", 0 ) );

		var work = [];
		arraySet( work, 1, 2000, 0 );

		// Each parallel worker mutates the shared list then serialises it. With N concurrent
		// workers under arrayEach(parallel=true), add() and listIterator() races fire reliably.
		arrayEach( work, function( item, idx ) {
			sharedArr.add( "worker-" & arguments.idx );
			if ( sharedArr.size() > 700 ) sharedArr.remove( 0 );
			try {
				serializeJson( sharedArr );
			} catch ( any e ) {
				if ( findNoCase( "ConcurrentModificationException", e.type & " " & e.message & " " & ( e.stacktrace ?: "" ) ) ) {
					cmeCount.incrementAndGet();
				} else {
					otherErrs.incrementAndGet();
				}
			}
		}, true );

		assertEquals( 0, otherErrs.get() );
		assertEquals( 0, cmeCount.get() );
	}

}
