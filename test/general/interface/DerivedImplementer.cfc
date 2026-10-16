// Implements only the derived interface — must satisfy methods from both IDerivedFace and IFace.
component implements="IDerivedFace" {

	function ifaceMethod()   { return "ifaceMethod-from-derived-impl"; }
	function derivedMethod() { return "derivedMethod-result"; }

}
