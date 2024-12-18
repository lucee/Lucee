component {
	this.name='LDEV-5206';
	function onRequestStart(){
		if ( structKeyExists( url, "pagePoolClear" ) )
			pagePoolClear();
	}
}
