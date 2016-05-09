component extends="Base" {
	static {
		static.b="B";
	}

	public static string function getData(){
		return static.b&":"&static.all;
	}
}