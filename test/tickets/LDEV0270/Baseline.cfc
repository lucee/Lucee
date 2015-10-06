component {

	echo("-body-constructor-");
	static {
		echo("-static-constructor-");
		//static.myVar = 17;
	}

	static function f1(){
		echo("-static1-method-");
	}
	static function f2(){
		echo("-static2-method-");
	}
	static function f3() {
		echo("-static3-method-");
	}

	function init(){
		echo("-function-constructor-")
	}

}