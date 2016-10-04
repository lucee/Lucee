component extends="org.lucee.cfml.test.LuceeTestCase"	{


	public void function test(){

		public void function test(){

			local.http = new http();
			local.http.setMethod('put');
			local.http.setURL('http://www.getmura.com/formtest/');
			local.http.addParam(type="formfield",name='email',value='test@test.com');
			local.httpSendResult = local.http.send();
			local.httpResult = httpSendResult.getPrefix();

			var returnedJSON=isJson(local.httpResult.filecontent);

			expect(returnedJSON).toBeTrue();

			if(returnedJSON){
				returnedJSON=deserializeJSON(local.httpResult.filecontent);
				param name="returnedJSON.email" default="";
				expect(returnedJSON.email=='test@test.com').toBeTrue();
			}
		}
	}


}
