component {
	this.name = 'LDEV982';
	this.applicationTimeout = createTimespan( 0, 0, 0, 1 );

	function onApplicationStart(){
		systemOutput("application started", true);
		application.configured = true;
	}

	function onApplicationEnd(){
		systemOutput("application ended", true);
	}
}