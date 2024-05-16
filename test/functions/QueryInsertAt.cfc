component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for QueryInsertAt", body = function() {

		///////////// QUERY /////////////////
			it( title = 'query insert at beginning',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				var res=QueryInsertAt(qry1,qry2,1);
				
				assertEquals("4,5,1,2,3",valueList(qry1.id));
				assertEquals("d,e,a,b,c",valueList(res.name));
			});
			it( title = 'query insert in middle',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				var res=QueryInsertAt(qry1,qry2,2);
				
				assertEquals("1,4,5,2,3",valueList(qry1.id));
				assertEquals("a,d,e,b,c",valueList(res.name));
			});
			it( title = 'query insert at the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				var res=QueryInsertAt(qry1,qry2,3);
				
				assertEquals("1,2,4,5,3",valueList(qry1.id));
				assertEquals("a,b,d,e,c",valueList(res.name));
			});
			it( title = 'query insert after the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				var res=QueryInsertAt(qry1,qry2,4);
				
				assertEquals("1,2,3,4,5",valueList(qry1.id));
				assertEquals("a,b,c,d,e",valueList(res.name));
			});
			it( title = 'query insert invalid 0',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				
				var error=false;
				try {
					QueryInsertAt(qry1,qry2,0);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});
			it( title = 'query insert invalid ',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var qry2=queryNew("id,name", "integer,varchar",[
			        [4,"d"],
			        [5,"e"]
			    ]);
				
				var error=false;
				try {
					QueryInsertAt(qry1,qry2,5);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});

		///////////// STRUCT /////////////////
			it( title = 'struct insert at beginning',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				var res=QueryInsertAt(qry1,sct,1);
				
				assertEquals("4,1,2,3",valueList(qry1.id));
				assertEquals("d,a,b,c",valueList(res.name));
			});
			it( title = 'struct insert in middle',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				var res=QueryInsertAt(qry1,sct,2);
				
				assertEquals("1,4,2,3",valueList(qry1.id));
				assertEquals("a,d,b,c",valueList(res.name));
			});
			it( title = 'struct insert at the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				var res=QueryInsertAt(qry1,sct,3);
				
				assertEquals("1,2,4,3",valueList(qry1.id));
				assertEquals("a,b,d,c",valueList(res.name));
			});
			it( title = 'struct insert after the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				var res=QueryInsertAt(qry1,sct,4);
				
				assertEquals("1,2,3,4",valueList(qry1.id));
				assertEquals("a,b,c,d",valueList(res.name));
			});
			it( title = 'struct insert invalid 0',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				
				var error=false;
				try {
					QueryInsertAt(qry1,sct,0);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});
			it( title = 'struct insert invalid ',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var sct={'id':4,'name':"d"};
				
				var error=false;
				try {
					QueryInsertAt(qry1,sct,5);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});


		///////////// ARRAY /////////////////
			it( title = 'array insert at beginning',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				var res=QueryInsertAt(qry1,arr,1);
				
				assertEquals("4,1,2,3",valueList(qry1.id));
				assertEquals("d,a,b,c",valueList(res.name));
			});
			it( title = 'array insert in middle',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				var res=QueryInsertAt(qry1,arr,2);
				
				assertEquals("1,4,2,3",valueList(qry1.id));
				assertEquals("a,d,b,c",valueList(res.name));
			});
			it( title = 'array insert at the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				var res=QueryInsertAt(qry1,arr,3);
				
				assertEquals("1,2,4,3",valueList(qry1.id));
				assertEquals("a,b,d,c",valueList(res.name));
			});
			it( title = 'array insert after the end',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				var res=QueryInsertAt(qry1,arr,4);
				
				assertEquals("1,2,3,4",valueList(qry1.id));
				assertEquals("a,b,c,d",valueList(res.name));
			});
			it( title = 'array insert invalid 0',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				
				var error=false;
				try {
					QueryInsertAt(qry1,arr,0);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});
			it( title = 'array insert invalid ',body = function( currentSpec ) {
				
				var qry1=queryNew("id,name", "integer,varchar",[
			    [1, "a"],
			    [2, "b"],
			    [3, "c"]
			    ]);
			    var arr=[4,"d"];
				
				var error=false;
				try {
					QueryInsertAt(qry1,arr,5);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});

		});

	}
}