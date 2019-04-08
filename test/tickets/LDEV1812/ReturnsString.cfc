component implements="I"{

	Person function returnsAny(){

		obj1 = createObject("component", "Person");
		return obj1;
	}

	void function acceptAny(required string a,required numeric b,required numeric c){
	 WriteOutput(a);
	 WriteOutput(b);
	 WriteOutput(c);
	 
	}

}