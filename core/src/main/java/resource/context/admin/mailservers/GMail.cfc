component implements="MailServer" {
	
	/**
	* label of the mail server.
	* @return get the label of the mail server.
	*/
	public string function getLabel() {return "Google Mail (GMail)";}

	/**
	* description of the mail server.
	* @return get the description of the mail server.
	*/
	public string function getDescription() {return "Gmail is the Google approach to email and chat. Practically unlimited free online storage allows you to collect all your messages, and Gmail's simple but very smart interface lets you find mail precisely and see it in context without effort. POP and powerful IMAP access let you access your email with any email program or device.";}

	/**
	* host name of the mail server.
	* @return get the host name of the mail server.
	*/
	public string function getHost() {return "smtp.gmail.com";}
	
	/**
	* Port of the mail server.
	* @return get the Port of the mail server.
	*/
	public number function getPort() {return 587;}

	/**
	* Enable Transport Layer Security.
	* @return do enable Transport Layer Security.
	*/
	public boolean function useTLS() {return true;}

	/**
	* Enable secure connections via SSL.
	* @return do enable secure connections via SSL.
	*/
	public boolean function useSSL() {return false;}

	/**
	* Returns shortname for this mail server.
	* @return do return shortname for this mail server.
	* */
	public string function getShortName() {return "Gmail";}
}