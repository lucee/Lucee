component implementsJava="lucee.runtime.ai.Response" {

	function init( answer ) {
		variables.answer = arguments.answer;
		return this;
	}

	function getTotalTokenUsed() { return 0; }
	function getAnswer() { return variables.answer; }
	function getAnswers() { return []; }
	function isMultiPart() { return false; }

}
