component extends="IterSelf" {
	function iterator() {
		return createDynamicProxy( this, [ "java.util.Iterator" ] );
	}
}
