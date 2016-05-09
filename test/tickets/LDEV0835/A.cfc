component extends="Base" {
	static {
		static.a="A";
		static.all="A";
	}

	public static string function getData(){
		return static.a&":"&static.all;
	}
}