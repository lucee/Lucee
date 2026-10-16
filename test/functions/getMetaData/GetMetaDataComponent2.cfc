component {

	function getOtherMessage() {
		return variables.otherMessage ?: "";
	}

	function setOtherMessage( required string otherMessage ) {
		variables.otherMessage = arguments.otherMessage;
	}

}
