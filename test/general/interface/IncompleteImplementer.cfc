// Declares implements="IFace" but does NOT define ifaceMethod() — checkInterface must reject this.
component implements="IFace" {

	function init() { return this; }

}
