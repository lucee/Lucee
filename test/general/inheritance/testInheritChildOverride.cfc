component extends="testInheritParent" {
	// Override parent's auto-generated getter with manual one
	function getParentProp() {
		return "CHILD OVERRIDE";
	}
}
