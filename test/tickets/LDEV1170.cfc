component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1170", body=function(){
			it(title="Checking generate3DesKey hidden ACF function", body=function(){
				expect(generate3DesKey('Test')).toBe("VGVzdA==");
			});
		});
	}

	// private function generate3DesKey( string fromString ) {
	// 	if( !structKeyExists( arguments, 'fromString' ) ){
	// 		return generateSecretKey( 'DESEDE' );
	// 	}
	// 	var secretKeySpec = createObject( 'java', 'javax.crypto.spec.SecretKeySpec' ).init( arguments.fromString.getBytes(), 'DESEDE' );
	// 	return toBase64( secretKeySpec.getEncoded() );
	// }
}