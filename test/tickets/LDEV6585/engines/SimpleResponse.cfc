component implementsJava="lucee.runtime.ai.Response" {

	function init( answer ) {
		variables.answer = arguments.answer;
		return this;
	}

	function getTotalTokenUsed() { return javaCast( "long", 0 ); }
	function getAnswer() { return variables.answer; }
	function getAnswers() { return createObject( "java", "java.util.ArrayList" ).init(); }
	function isMultiPart() { return false; }

}
