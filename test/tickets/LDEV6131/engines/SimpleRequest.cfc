component implementsJava="lucee.runtime.ai.Request" {

	function init( question ) {
		variables.question = arguments.question;
		return this;
	}

	function getQuestion() { return variables.question; }
	function getQuestions() { return []; }
	function isMultiPart() { return false; }

}
