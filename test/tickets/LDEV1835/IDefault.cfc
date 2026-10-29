interface {

	public string function required();

	public default string function greet( required string name ) {
		return "Hello " & arguments.name;
	}

	// a function with a body, without the keyword, is a default function as well
	public string function plain() {
		return "plain";
	}

	public default string function useRequired() {
		return "default uses " & this.required();
	}

}
