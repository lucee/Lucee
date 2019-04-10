component {
	this.name = "test123" ;

	function onRequestEnd(){
		pageContents = getPageContext().getOut().getString() ;
	}
}
