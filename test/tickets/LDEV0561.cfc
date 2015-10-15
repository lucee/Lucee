component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.salt = 'A41n9t0Q';
		variables.passphrase = 'passphrase';
		variables.iterations = 100000;
		variables.keysize = 2048;
	}

	function run( testResults , testBox ) {

		describe( 'LDEV-561' , function() {

			describe( 'CreateObject loads java classes by bundle name and value' , function() {
				it( 'for createObject("java",<class-name>)' , function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( 'for createObject("java",<class-name>,<bundle-name>)' , function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern","org.apache.oro")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( 'for createObject(type:"java",clas:<class-name>,name:<bundle-name>)' , function() {
					 
					expect(
						createObject(type:"java",class:"org.apache.oro.text.regex.Pattern",name:"org.apache.oro")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 


				it( 'for createObject("java",<class-name>,<bundle-name>,<bundle-version>)' , function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern","org.apache.oro","2.0.8")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( 'for createObject(type:"java",clas:<class-name>,name:<bundle-name>,version:<bundle-version>)' , function() {
					 
					expect(
						createObject(type:"java",class:"org.apache.oro.text.regex.Pattern",name:"org.apache.oro",version:"2.0.8")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

			});

		});

	}
	
} 
