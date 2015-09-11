component extends="base"{
		
	variables.tagname = "mail";
						
	public any function send(){
		return super.invokeTag();
	}
						
}
