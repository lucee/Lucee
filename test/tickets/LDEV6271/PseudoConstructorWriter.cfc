component {

	variables.greet = "hello";

	public string function greet(){
		return "from-method";
	}

	public string function readGreetViaVariables(){
		return variables.greet;
	}

}
