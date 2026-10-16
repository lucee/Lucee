// implements="IDerivedFace" but only defines derivedMethod() — checkInterface must walk the interface chain.
component implements="IDerivedFace" {

	function derivedMethod() { return "only-the-derived-method"; }

}
