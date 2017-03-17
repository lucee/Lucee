component extends="comp1"{
	public string function myUCase1InComp2( required string str ){
		return myUCase(arguments.str);
	}
	public string function myUCase2InComp2( required string str ){
		return comp1::myUCase(arguments.str);
	}
}