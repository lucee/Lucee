component {
	
	// static
	static function staticUDF1(arg1){
		return arguments;
	}
 	
	// <access> static
	static function staticUDF21(arg1){
		return arguments;
	}
	static public function staticUDF22(arg1){
		return arguments;
	}

	// <access> static <modifier> <type>
	public static struct function staticUDF31(arg1){
		return arguments;
	}
	static public struct function staticUDF32(arg1){
		return arguments;
	}
	struct public static function staticUDF33(arg1){
		return arguments;
	}


	// instance
	function instanceUDF1(arg1){
		return arguments;
	}
	
}