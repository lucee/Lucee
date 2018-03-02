component output="false" accessors="true"{
	/**
	 * @hint Performs equal opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/
	static function eq(required string columnName, required string value){
		var result = "";
		result = " #arguments.columnName# = '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs Not equal opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function neq(required string columnName, required string value){
		var result = "";
		result = "#arguments.columnName# != '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs GreaterThan opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function gt(string columnName, string value){
		var result ="";
		result = "#arguments.columnName# > '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs Lessthan opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function lt(string columnName, string value){
		var result ="";
		result = "#arguments.columnName# < '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs not opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function not(string columnName, string value){
		result = " NOT #arguments.columnName# = '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs IN opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function in(required string columnName, required string value){
		var arr = listToArray(arguments.value);
		var list = "";
		var i = 1;
		for(str in arr){
			if(arrayLEn(arr) != i){
				list &= " '#str#', ";
			} else {
				list &= " '#str#' ";
			}
			i++;
		}
		result = " #arguments.columnName# In (#list#) ";
		return result;
	}

	/**
	 * @hint Performs Not IN opertion for where clause
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to filter out from the table
	*/

	static function notIn(required string columnName, required string value){
		var arr = listToArray(arguments.value);
		var list = "";
		var i = 1;
		for(str in arr){
			if(arrayLEn(arr) != i){
				list &= " '#str#', ";
			} else {
				list &= " '#str#' ";
			}
			i++;
		}
		result = " #arguments.columnName# NOT IN (#list#) ";
		return result;
	}

	/**
	 * @hint Performs And opertion for where clause
	*/

	static function and(){
		var result = " ";
		var i= 1;
		for(str in arguments){
			if( i!= 1){
				result &= "and #arguments[str]#";
			} else {
				result &= "#arguments[str]#";
			}
			i++;
		}
		return result;
	}

	/**
	 * @hint Performs OR opertion for where clause
	*/

	static function or(){
		var result = "";
		var i= 1;
		for(str in arguments){
			if( i!= 1){
				result &= " OR #arguments[str]#";
			} else {
				result &= " #arguments[str]#";
			}
			i++;
		}
		return result;
	}

	/**
	 * @hint Performs Search opertion
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to search out from the table
	*/

	static function like(required string columnName, required string value){
		var result ="";
		result = "#arguments.columnName# LIKE '#arguments.value#' ";
		return result;
	}

	/**
	 * @hint Performs Between opertion
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to search out from the table
	*/

	static function between(required string columnName, required string value){
		var result ="";
		result = "#arguments.columnName# Between '#ListFirst(arguments.value)#' AND  '#ListLast(arguments.value)#' ";
		return result;
	}

	/**
	 * @hint Performs on opertion
	 * @columnName columnName which ou want to filter out
	 * @Value value which want to search out from the table
	*/

	static function on(required string columnName, required string value){
		var result = "";
		result = " ON #arguments.columnName# = #arguments.value# ";
		return result;
	}

}
