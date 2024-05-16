component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-1871", function() {
			it( title='Checking cffeed entry.content.type', body=function( currentSpec ) {
				var theFeed = {
					version='atom_1.0',
					entry: [
						{
							content: {
								type: 'text/plain',
								value: 'Some text'
							}
						}
					]
				}
				feed action='create' name=theFeed xmlVar='feedXML';
				assertEquals(true, isxml(feedXML));
				var ParseXml = xmlParse(feedXML);
				var result = xmlSearch( ParseXml,'//*[ local-name()=''feed'' ]')[1].XmlChildren[1].XmlChildren[4].XmlAttributes.type;
				assertEquals('text/plain', result);;
			});
		});
	}
}