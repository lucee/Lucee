component accessors="true" persistent="true" table="LDEV4067" {
	property name="id" type="string" persistent="true";
	property name="name" type="string" persistent="true";
	
	public component function init(){
		return this;
	}
	
	this.testClosureThis = {
		mappers = {
			"theName" : function(){
				return this.getName();
			}
		}
	}
	
	this.testClosureVar = {
		mappers = {
			"theName" : () => variables.getName()
		}
	}

	this.testLambdaThis = {
		mappers = {
			"theName" : function(){
				return this.getName();
			}
		}
	}
	
	this.testLambdaVar = {
		mappers = {
			"theName" : () => variables.getName()
		}
	}
}