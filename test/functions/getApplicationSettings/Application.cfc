component {
	this.name = "test-getApplicationSettings";
	this.useJavaAsRegexEngine="true";

	this.nonStandardSetting = true;

	this.myCustomUDF = function(){
		echo("hello world");
	}

	function onRequestStart() {
		var nothing = "else matters";
	}

}