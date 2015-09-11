component extends="base"{
		
	variables.tagname = "http";
					
	public any function send(){
		return super.invokeTag();
	}
						
}
