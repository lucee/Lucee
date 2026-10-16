component {
	// AnnotatedElement.getAnnotations() declares Annotation[]; each element must become an Annotation
	function getAnnotations() {
		return [ this ];
	}
	function annotationType() {
		return javaCast( "null", "" );
	}
}
