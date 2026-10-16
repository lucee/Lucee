// Pseudo-constructor body — runs per new/createObject. Statements interspersed between function declarations.
component {

	function getBetweenUuid() { return variables.betweenUuid; }

	variables.betweenUuid    = createUUID();
	variables.betweenCounter = ( application.looseBetweenCounter ?: 0 ) + 1;
	application.looseBetweenCounter = variables.betweenCounter;

	function getBetweenCounter() { return variables.betweenCounter; }

}
