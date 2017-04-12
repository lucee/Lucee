component extends="comp1"{

	public static string function myUCase2( required string str ){
		return uCase(arguments.str);
	}


	public string function func1( required string str ){
		return myUCase(arguments.str);
	}

	public string function func2( required string str ){
		return myUCase2(arguments.str);
	}

	public string function func3( required string str ){
		return comp1::myUCase(arguments.str);
	}
	public string function func4( required string str ){
		return comp2::myUCase2(arguments.str);
	}
	public string function func5( required string str ){
		return static.myUCase2(arguments.str);
	}
	public string function func6( required string str ){
		return static.myUCase(arguments.str);
	}
	public string function func7( required string str ){
		return super::myUCase(arguments.str);
	}
}