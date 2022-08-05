component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1770", function() {
			var getArt= QueryNew( "ARTID,ARTNAME,DESCRIPTION,ISSOLD,MEDIATYPE" );
			QueryAddRow( getArt, 3 );

			QuerySetCell( getArt, "ARTID", "1", 1 );
			QuerySetCell( getArt, "DESCRIPTION", "Test desc 1", 1 );
			QuerySetCell( getArt, "ARTNAME", "Picaso 1", 1 );

			QuerySetCell( getArt, "ARTID", "2", 2 );
			QuerySetCell( getArt, "DESCRIPTION", "Test desc 2", 2 );
			QuerySetCell( getArt, "ARTNAME", "Picaso 2", 2 );

			QuerySetCell( getArt, "ARTID", "3", 3 );
			QuerySetCell( getArt, "DESCRIPTION", "Test desc 3", 3 );
			QuerySetCell( getArt, "ARTNAME", "Picaso 3", 3 );

			var feedMeta = structNew();
			var feedMeta.description = "ColdFusion Art Gallery XML Feed";
			var feedMeta.link = "http://coldfusionexamples.com/";
			var feedMeta.title = "Art";
			var feedMeta.version = "rss_2.0";

			var colMap = structNew();
			var colMap.content = "DESCRIPTION";
			var colMap.rsslink = "ARTID";
			var colMap.title = "ARTNAME";

			cffeed (action="create" query="#getArt#" columnMap="#colMap#" properties="#feedMeta#" xmlVar="feedXML");
			var testContent = xmlParse(feedXML);

			it( title="Checking 'item' with 'title' node as empty ", body=function( currentSpec ) {
				local.result = testContent.XmlRoot.XmlChildren[1].XmlChildren[4].XmlChildren[1].XmlText;
				expect(local.result).toBe('Picaso 1');
			});

			it( title="Checking 'item' with 'desc' node as empty", body=function( currentSpec ) {
				local.result = testContent.XmlRoot.XmlChildren[1].XmlChildren[4].XmlChildren[2].XmlText;
				expect(local.result).toBe('1');
			});

			it( title="Checking 'item' contains 'link' node", body=function( currentSpec ) {
				local.result = testContent.XmlRoot.XmlChildren[1].XmlChildren[4].XmlChildren[2].xmlName; 
				expect(local.result).toBe('link');
			});

			it( title="Checking 'item' contains three nodes", body=function( currentSpec ) {
				local.result = arrayLen(testContent.XmlRoot.XmlChildren[1].XmlChildren[4].XmlChildren);
				expect(local.result).toBe('3');
			});
		});
	}
}