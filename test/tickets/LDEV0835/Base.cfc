component {
	static {		
		static.base="Base";
		static.all="Base";
	}

	public static string function getData(){
		return static.base & ":" & static.all;
	}
}