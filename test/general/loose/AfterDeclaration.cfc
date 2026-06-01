component {

	function getAfterCounter() { return application.looseAfterCounter ?: 0; }
	function getAfterUuid()    { return application.looseAfterUuid    ?: ""; }

}

// Post-declaration code in script-syntax — silently discarded by the compiler. Pinned by ../LooseComponent.cfc.
application.looseAfterCounter = ( application.looseAfterCounter ?: 0 ) + 1;
application.looseAfterUuid    = createUUID();
