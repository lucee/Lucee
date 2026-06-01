// Pre-declaration code in script-syntax — silently discarded by the compiler. Pinned by ../LooseComponent.cfc.
application.looseBeforeCounter = ( application.looseBeforeCounter ?: 0 ) + 1;
application.looseBeforeUuid    = createUUID();

component {

	function getBeforeCounter() { return application.looseBeforeCounter; }
	function getBeforeUuid()    { return application.looseBeforeUuid; }

}
