component {
	variables.variable = "variables";
	variables.func = function (){
		return "variables";
	}

	public function init(){
		this.variable = "this";
		return this;
	}

	public function func (){
		return "this";
	}

	function echo(x){
		writeoutput(x);
	}

	function getFuncScope(){
		return func();
	}

	function getThisFuncScope(){
		return this.func();
	}
	
	function getVariableScope(){
		return variable;
	}

	function getThisVariableScope(){
		return this.variable;
	}
}
