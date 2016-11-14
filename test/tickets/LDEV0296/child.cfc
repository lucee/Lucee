// C.cfc
component extends=parent initmethod=constructor {

	function init(){
		this.Const2 = 20;
		return this;
	}

	function constructor(){
		super.constructor(argumentCollection=arguments);
		this.Const3 = 30;
		return this;
	}
}