// Probe: pre/post-declaration variables.x writes are compile-time dead.
variables.probePre = "pre-set";

component {

	function getSelfPre() {
		return ( variables.probePre ?: "missing-in-component" );
	}

}

variables.probePost = "post-set";
