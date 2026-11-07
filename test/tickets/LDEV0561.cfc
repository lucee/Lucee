component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.salt = 'A41n9t0Q';
		variables.passphrase = 'passphrase';
		variables.iterations = 100000;
		variables.keysize = 2048;
	}

	function run( testResults , testBox ) {

		describe( 'LDEV-561' , function() {

			// TODO LDEV-6627: skipped until the OSGi extension exists, the specs load org.apache.oro by bundle name which needs the OSGi service. Enable them (remove skip=true) as soon as the OSGi extension is available.
			describe( 'CreateObject loads java classes by bundle name and value' , function() {
				it( title='for createObject("java",<class-name>)', skip=true, body=function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( title='for createObject("java",<class-name>,<bundle-name>)', skip=true, body=function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern","org.apache.oro")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( title='for createObject(type:"java",clas:<class-name>,name:<bundle-name>)', skip=true, body=function() {
					 
					expect(
						createObject(type:"java",class:"org.apache.oro.text.regex.Pattern",name:"org.apache.oro")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 


				it( title='for createObject("java",<class-name>,<bundle-name>,<bundle-version>)', skip=true, body=function() {
					 
					expect(
						createObject("java","org.apache.oro.text.regex.Pattern","org.apache.oro","2.0.8")
						.getClass().getName()
					).toBe(
						'org.apache.oro.text.regex.Pattern'
					);
				}); 

				it( title='for createObject(type:"java",clas:<class-name>,name:<bundle-name>,version:<bundle-version>)', skip=true, body=function() {
					 
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
