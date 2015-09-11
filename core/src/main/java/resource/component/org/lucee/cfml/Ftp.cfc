component extends="Base" accessors="true"{
		
	variables.tagname = "ftp";
					
	public Result function open(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('open');
		return super.invokeTag();
	}

	public Result function close(){
		this.setAttributes(argumentCollection=arguments);
		this.setAction('close');
		return super.invokeTag();
	}

	public Result function quote(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('quote');
		return super.invokeTag();
	}

	public Result function site(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('site');
		return super.invokeTag();
	}

	public Result function allo(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('allo');
		return super.invokeTag();
	}
		
	public Result function acct(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('acct');
		return super.invokeTag();
	}

	public Result function changeDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('changeDir');
		return super.invokeTag();
	}
	
	public Result function createDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('createDir');
		return super.invokeTag();
	}

	public Result function listDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('listDir');
		return super.invokeTag();
	}

	public Result function removeDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('removeDir');
		return super.invokeTag();
	}
	
	public Result function getFile(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('getFile');
		return super.invokeTag();
	}

	public Result function putFile(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('putFile');
		return super.invokeTag();
	}

	public Result function rename(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('rename');
		return super.invokeTag();
	}

	public Result function remove(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('remove');
		return super.invokeTag();
	}

	public Result function getCurrentDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('getCurrentDir');
		return super.invokeTag();
	}

	public Result function getCurrentUrl(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('getCurrentUrl');
		return super.invokeTag();
	}

	public Result function existsDir(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('existsDir');
		return super.invokeTag();
	}
	
	public Result function existsFile(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('existsFile');
		return super.invokeTag();
	}

	public Result function existsFile(){
		this.setAttributes(argumentCollection=arguments);	
		this.setAction('existsFile');
		return super.invokeTag();
	}
	
						
}
