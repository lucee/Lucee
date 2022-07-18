component extends="HelperBase"{
		
	variables.tagname = "http";
					
	public any function send(){
		this.setAttributes(argumentCollection=arguments);
		return super.invokeTag();
	}
						
}
