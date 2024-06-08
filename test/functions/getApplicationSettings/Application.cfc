component {
	this.name = "test-getApplicationSettings";
	this.useJavaAsRegexEngine="true";
	this.monitoring.showDebug = false;
	this.monitoring.showDoc = false;
	this.monitoring.showMetric = false;
	this.nonStandardSetting = true;

	this.myCustomUDF = function(){
		echo("hello world");
	}

	function onRequestStart() {
		var nothing = "else matters";
	}

}