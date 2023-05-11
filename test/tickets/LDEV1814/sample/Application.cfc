component {
	this.name = "test123" ;

	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

	function onRequestEnd(){
		pageContents = getPageContext().getOut().getString() ;
	}
}
