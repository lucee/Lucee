component extends="org.lucee.cfml.test.LuceeTestCase" labels="guard" {
	function run( testResults , testBox ) {
		describe( "test case for EncodeForLDAP", function() {
			it(title = "Checking with EncodeForLDAP", body = function( currentSpec ) {
				var enc=EncodeForLDAP('<script>');
				assertEquals('<script>',enc);
			});
			it(title = "Checking with EncodeForLDAPMember", body = function( currentSpec ) {
				var enc='<script>'.EncodeForLDAP();
				assertEquals('<script>',enc);
			});
		});	
	}
}