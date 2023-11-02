component accessors="true"{

	property name="id";

	variables.superVariables = 'foo';
	this.superThis = 'bar';

	function getBaseMemento(){
		return {
			id = variables.id
		};
	}
		
}