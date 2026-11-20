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
			// LDEV-6627: oro is not on Maven Central as an OSGi bundle (only on the Lucee S3 bucket), so the specs use commons-net, a valid OSGi bundle from the Maven mapping.
				it( title='for createObject("java",<class-name>)', body=function() {
					 
					expect(
						createObject("java","org.apache.commons.net.util.SubnetUtils")
						.getClass().getName()
					).toBe(
						'org.apache.commons.net.util.SubnetUtils'
					);
				}); 

				it( title='for createObject("java",<class-name>,<bundle-name>)', body=function() {
					 
					expect(
						createObject("java","org.apache.commons.net.util.SubnetUtils","org.apache.commons.commons-net")
						.getClass().getName()
					).toBe(
						'org.apache.commons.net.util.SubnetUtils'
					);
				}); 

				it( title='for createObject(type:"java",clas:<class-name>,name:<bundle-name>)', body=function() {
					 
					expect(
						createObject(type:"java",class:"org.apache.commons.net.util.SubnetUtils",name:"org.apache.commons.commons-net")
						.getClass().getName()
					).toBe(
						'org.apache.commons.net.util.SubnetUtils'
					);
				}); 


				it( title='for createObject("java",<class-name>,<bundle-name>,<bundle-version>)', body=function() {
					 
					expect(
						createObject("java","org.apache.commons.net.util.SubnetUtils","org.apache.commons.commons-net","3.11.1")
						.getClass().getName()
					).toBe(
						'org.apache.commons.net.util.SubnetUtils'
					);
				}); 

				it( title='for createObject(type:"java",clas:<class-name>,name:<bundle-name>,version:<bundle-version>)', body=function() {
					 
					expect(
						createObject(type:"java",class:"org.apache.commons.net.util.SubnetUtils",name:"org.apache.commons.commons-net",version:"3.11.1")
						.getClass().getName()
					).toBe(
						'org.apache.commons.net.util.SubnetUtils'
					);
				}); 

			});

		});

	}
	
} 
