component extends = "org.lucee.cfml.test.LuceeTestCase" {

	variables.isModernHibernate=left(bundleInfo(createObject("java","org.lucee.extension.orm.hibernate.HibernateORMEngine")).version,1)>=5;



	function run( testResults , testBox ) {

		var uri=createURI("ORMExecuteQuery");


		describe( "test case for the function ORMQueryExecute", function() {
			it(title="test inline parameter",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					forms : {Scene = "inline"}
				);
				debug(result);
				expect(isValid("uuid",trim(result.filecontent))).toBe(true);
			});

			it(title="test struct parameter",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					forms : {Scene = "struct"}
				);
				debug(result);
				expect(isValid("uuid",trim(result.filecontent))).toBe(true);
			});

			// this only works for Hibernate >=5
			it(title="test array parameter", skip=!variables.isModernHibernate, body=function( currentSpec ) {
				if(!variables.isModernHibernate) return;
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					forms : {Scene = "array"}
				);
				debug(result);
				expect(isValid("uuid",trim(result.filecontent))).toBe(true);
			});

			it(title="test legacy parameter",  body=function( currentSpec ) {
				local.result = _InternalRequest(
					template : "#uri#/index.cfm",
					forms : {Scene = "legacy"}
				);
				debug(result);
				expect(isValid("uuid",trim(result.filecontent))).toBe(true);
			});
		});
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}




