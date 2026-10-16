component {
	function init( array items ) {
		variables.items = arguments.items;
		variables.pos = 0;
		return this;
	}
	function iterator() {
		return this;
	}
	function hasNext() {
		return variables.pos < arrayLen( variables.items );
	}
	function next() {
		variables.pos++;
		return variables.items[ variables.pos ];
	}
}
