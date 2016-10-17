component {  
 
	// constructor should be called only once
	static {
		static.constructed = randRange(0, 1000000);
	}

	function init(name) {
		this.name = name;
		return this;
	}
	
	function details() {
		return static.constructed;
	}
}