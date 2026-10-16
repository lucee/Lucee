// Closures defined in the pseudo-constructor body. Probed by ../BodyClosures.cfc.
component {

	variables.tag = "default";

	variables.greeter = function(){
		return "hello-" & variables.tag;
	};

	variables.tagReader = () => variables.tag;

	function init( string tag = "default" ){
		variables.tag = arguments.tag;
		return this;
	}

	function setTag( required string newTag ){ variables.tag = arguments.newTag; }
	function callGreeter()   { return variables.greeter(); }
	function callTagReader() { return variables.tagReader(); }
	function getGreeterRef() { return variables.greeter; }

}
