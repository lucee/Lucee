component extends="org.lucee.cfml.test.LuceeTestCase" skip=true	{
	
	public function beforeTests(){
		ScriptEngineManager=createObject('java','javax.script.ScriptEngineManager');
		variables.manager=ScriptEngineManager.init();
		
	}
	

	public void function testGetEngineByName(){

		local.engine =manager.getEngineByName("CFML");
		assertEquals("cfml",engine.factory.languageName);
	}

	public void function testGetEngineByExtension(){
		local.engine =manager.getEngineByExtension("cfm");
		assertEquals("cfml",engine.factory.languageName);

		if (getJavaVersion() lte 11){
			// reference
			local.engine =manager.getEngineByExtension("js");
			assertEquals("ECMAScript",engine.factory.languageName);
		}
	}

	public void function testGetEngineByMimeType(){
		local.engine =manager.getEngineByMimeType("application/cfml");
		assertEquals("cfml",engine.factory.languageName);

		if (getJavaVersion() lte 11) {
			// reference
			local.engine =manager.getEngineByMimeType("application/javascript");
			assertEquals("ECMAScript",engine.factory.languageName);
		}
	}


	public void function testGetEngineFactories(){
		local.names=[];
		loop array="#manager.getEngineFactories()#" item="local.factory" {
			names.append(factory.languageName);
		}
		assertTrue(names.contains("cfml"));
		if (getJavaVersion() lte 11)
			assertTrue(names.contains("ECMAScript"));
	}

	public void function testPutGet(){
		local.testValue="Susi #now()#";
		local.engine =manager.getEngineByName("CFML");
		engine.put('test',testValue);
		assertEquals(testValue,engine.get('test'));
	}

	public void function testEvalReturnValue(){
		_testEvalReturnValue(manager.getEngineByName("CFML"));
	}
	private void function _testEvalReturnValue(required engine){
		engine.put('cont',false);
		assertEquals("456",engine.eval('if(cont){x=123;}else {y=456;}'));
		
		engine.put('cont',true);
		assertEquals("123",engine.eval('if(cont){x=123;}else {y=456;}'));
	
	}

	public void function testEvalScopes() skip=true{
		_testEvalScopes(manager.getEngineByName("CFML"));
	}
	private void function _testEvalScopes(required engine){
		engine.eval('url.test=1;');
		assertEquals('{"test":1}',serialize(engine.get('url')));// im getting this from the binding object, so it is in the engine scope
		assertEquals(1,engine.eval('getVariable("url.test");'));// im getting this from the binding object, so it is in the engine scope
	}


	public void function testEvalInterpreter(){
		_testEvalInterpreter(manager.getEngineByName("CFML"));
	}
	private void function _testEvalInterpreter(required engine){
		engine.eval('susi.sorglos=1;');
		assertEquals(1,engine.eval('getVariable("susi.sorglos");'));
		assertEquals(1,engine.eval('evaluate("susi.sorglos");'));
	}

	public void function testEvalFunction(){
		_testEvalFunction(manager.getEngineByName("CFML"));
	}
	private void function _testEvalFunction(required engine){
		// udf
		assertEquals("udf",engine.eval('function test(){return "udf";}; x=test();'));
		// closure
		assertEquals("closure",engine.eval('test=function (){return "closure";}; x=test();'));
		// lambda
		// assertEquals("lambda",engine.eval('test=()->"lambda"; x=test();'));
	}

	private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if(arr[1]==1) // version 1-9
			return arr[2];
		return arr[1];
	}

} 



