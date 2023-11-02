component extends='super' {

	property name="name";

	function init( name="nothing" ){
		variables.id 	= createUUID();
		variables.name 	= arguments.name;

		return this;
	}

	function createUDF() {
		return function() {
			return superVariables & super.superThis;
		};
	}

	function getMemento(){
		var memento = getBaseMemento();

		memento[ "name" ] = variables.name;

		return memento;
	}
	
}