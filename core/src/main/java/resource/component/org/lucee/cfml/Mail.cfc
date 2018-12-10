component extends="HelperBase"{
		
	variables.tagname = "mail";
						
	public any function send(){
		return super.invokeTag();
	}
						
}
