component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
	variables.typeList = 'BIGINT,BINARY,BIT,BOOLEAN,CHAR,DATE,DECIMAL,DOUBLE,INTEGER,LONGVARBINARY,LONGVARCHAR,NCHAR,NUMERIC,NVARCHAR,REAL,SMALLINT,TIME,TIMESTAMP,TINYINT,VARBINARY,VARCHAR';

	function beforeAll() {
		variables.employees = queryNew( 'name,age,email,department,isContract,yearsEmployed,sickDaysLeft,hireDate,isActive,empID,favoriteColor', 'varchar,integer,varchar,varchar,boolean,integer,integer,date,boolean,varchar,varchar' );
		// 1 Million records
		loop from=1 to=62500 index='local.i' {
			employees.addRow( [
				[ 'John Doe#i#',28,'John#i#@company.com','Accounting',false,2,4,createDate(2010,1,21),true,'sdf','red' ],
				[ 'Jane Doe#i#',22,'Jane#i#@company.com','Accounting',false,0,8,createDate(2011,2,21),true,'hdfg','blue' ],
				[ 'Bane Doe#i#',28,'Bane#i#@company.com','Accounting',true,3,2,createDate(2012,3,21),true,'sdsfsff','green' ],
				[ 'Tom Smith#i#',25,'Tom#i#@company.com','Accounting',false,6,4,createDate(2013,4,21),false,'HDFG','yellow' ],
				[ 'Harry Johnson#i#',38,'Harry#i#@company.com','IT',false,8,6,createDate(2014,5,21),true,'4ge','purple' ],
				[ 'Jason Wood#i#',37,'Jason#i#@company.com','IT',false,19,4,createDate(2015,6,21),true,'ShrtDF','Red' ],
				[ 'Doris Calhoun#i#',67,'Doris#i#@company.com','IT',true,3,6,createDate(2016,7,21),true,'sgsdg','Blue' ],
				[ 'Mary Root#i#',17,'Mary#i#@company.com','IT',false,8,2,createDate(2017,8,21),true,'','Green' ],
				[ 'Aurthur Duff#i#',23,'Aurthur#i#@company.com','IT',false,4,0,createDate(2018,9,21),true,nullValue(),'Yellow' ],
				[ 'Luis Hake#i#',29,'Luis#i#@company.com','IT',true,9,5,createDate(2019,10,21),true,nullValue(),'Purple' ],
				[ 'Gavin Bezos#i#',46,'Gavin#i#@company.com','HR',false,2,5,createDate(2020,11,21),false,nullValue(),'RED' ],
				[ 'Nancy Garmon#i#',57,'Nancy#i#@company.com','HR',false,14,9,createDate(2005,12,21),true,nullValue(),'BLUE' ],
				[ 'Tom Zuckerburg#i#',27,'Tom#i#@company.com','HR',true,16,10,createDate(2006,1,21),true,nullValue(),'GREEN' ],
				[ 'Richard Gates#i#',62,'Richard#i#@company.com','Executive',false,11,1,createDate(2007,2,21),true,nullValue(),'YELLOW' ],
				[ 'Amy Merryweather#i#',58,'Amy#i#@company.com','Executive',false,12,2,createDate(2008,3,21),true,nullValue(),'PURPLE' ],
				[ 'Bob Smith#i#',78,'Bob#i#@company.com','HR',true,30,40,createDate(1990,1,1),true,'test','verde' ]
			] );
		}

		// Every known SQL type so we can test all possible sorting options
		variables.qrySort = queryNew(
			'type,#typeList#,unicode',
			'string,#typeList#,string' );
		qrySort.addRow( [
			[ 'lower',12341234,javaCast( 'byte[]', [0,0,0] ),0,false,'A',createDate(2021,1,1),1.1,100,100,javaCast( 'byte[]', [0,0,0] ),'A','A',10,'A',1.1,1,
				createTime(1,1,1),createObject( 'java', 'java.sql.Timestamp' ).init( createDate(2021,1,1).getTime() ),5,javaCast( 'byte[]', [0,0,0] ),'A', chr(196)&chr(246)&chr(252)&chr(223)&chr(22823)&'A' ],
			[ 'higher',12341235,javaCast( 'byte[]', [8,8,8] ),1,true,'a',createDate(2022,1,1),1.2,200,200,javaCast( 'byte[]', [8,8,8] ),'a','a',20,'a',1.2,2,
				createTime(2,2,2),createObject( 'java', 'java.sql.Timestamp' ).init( createDate(2022,1,1).getTime() ),50,javaCast( 'byte[]', [8,8,8] ),'a', chr(196)&chr(246)&chr(252)&chr(223)&chr(22823)&'a' ]
		] );

		variables.qryNullSort = queryNew(
			'type,string,integer',
			'string,string,integer' );
		qryNullSort.addRow( [
			[ 'lower', 'A', 10 ],
			[ 'null', nullValue(), nullValue() ],
			[ 'null', nullValue(), nullValue() ],
			[ 'higher', 'a', 100 ]
		] );
	}

	function run( testResults , testBox ) {

		describe( 'Parallel QoQ' , () =>{

			it( 'can handle simple non-aggregate ordered select' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT name, age, email, department, age
						FROM employees
						WHERE sickDaysLeft > 8
							AND isActive = true
							AND favoriteColor != 'verde'
							AND email like '%@company.com'
						ORDER BY department desc, name",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 125000 );
				expect( actual.name[1] ).toBe( 'Nancy Garmon1' );
				expect( actual.email[1] ).toBe( 'Nancy1@company.com' );
				expect( actual.age[1] ).toBe( 57 );

			});

			it( 'can handle simple non-aggregate ordered select with TOP' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT TOP 5 name, age, email, department, age
						FROM employees
						WHERE sickDaysLeft > 8
							AND isActive = true
							AND favoriteColor != 'verde'
							AND email like '%@company.com'
						ORDER BY department desc, name",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 5 );
				expect( actual.name[1] ).toBe( 'Nancy Garmon1' );
				expect( actual.email[1] ).toBe( 'Nancy1@company.com' );
				expect( actual.age[1] ).toBe( 57 );

			});

			it( 'can handle simple non-aggregate non-ordered select with TOP' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT TOP 5 name, age, email, department, age
						FROM employees
						WHERE sickDaysLeft > 8
							AND isActive = true
							AND favoriteColor != 'verde'
							AND email like '%@company.com'",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 5 );

			});

			it( 'can handle simple non-grouped aggregate' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT max( name ) as name, min( age ) as age, count( 1 ) as count
						FROM employees
						WHERE sickDaysLeft > 8
							AND isActive = true
							AND favoriteColor != 'verde'
							AND email like '%@company.com'",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.name[1] ).toBe( 'Tom Zuckerburg9999' );
				expect( actual.age[1] ).toBe( 27 );
				expect( actual.count[1] ).toBe( 125000 );

			});

			it( 'can handle simple grouped aggregate' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT department, isActive, count(1) as count, min( age ) as minAge, max( age ) as maxAge
						FROM employees
						WHERE sickDaysLeft > 2
							AND favoriteColor != 'verde'
							AND email like '%@company.com'
						GROUP BY department, isActive
						HAVING min( age ) > 22
						ORDER BY department, isActive, count",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 4 );
				expect( actual.department[1] ).toBe( 'Accounting' );
				expect( actual.isActive[1] ).toBe( false );
				expect( actual.count[1] ).toBe( 62500 );
				expect( actual.minAge[1] ).toBe( 25 );
				expect( actual.maxAge[1] ).toBe( 25 );

				expect( actual.department[2] ).toBe( 'HR' );
				expect( actual.isActive[2] ).toBe( false );
				expect( actual.count[2] ).toBe( 62500 );
				expect( actual.minAge[2] ).toBe( 46 );
				expect( actual.maxAge[2] ).toBe( 46	 );

				expect( actual.department[3] ).toBe( 'HR' );
				expect( actual.isActive[3] ).toBe( true );
				expect( actual.count[3] ).toBe( 125000 );
				expect( actual.minAge[3] ).toBe( 27 );
				expect( actual.maxAge[3] ).toBe( 57 );

				expect( actual.department[4] ).toBe( 'IT' );
				expect( actual.isActive[4] ).toBe( true );
				expect( actual.count[4] ).toBe( 250000 );
				expect( actual.minAge[4] ).toBe( 29 );
				expect( actual.maxAge[4] ).toBe( 67 );

			});

			it( 'can handle UNION ALL' , ()=>{
				var actual = QueryExecute(
					sql = "
						SELECT name
						FROM employees
						WHERE department = 'IT'
							AND sickDaysLeft > 2
							AND favoriteColor != 'verde'
							AND email like '%@company.com'
						UNION ALL
						SELECT name
						FROM employees
						WHERE department = 'IT'
							AND sickDaysLeft > 2
							AND favoriteColor != 'verde'
							AND email like '%@company.com'
						ORDER BY name",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 500000 );
				expect( actual.name[1] ).toBe( 'Doris Calhoun1' );
				expect( actual.name[2] ).toBe( 'Doris Calhoun1' );
			});

			it( 'can handle UNION DISTINCT' , ()=>{
				var actual = QueryExecute(
					sql = "
					SELECT name
					FROM employees
					WHERE department = 'IT'
						AND sickDaysLeft > 2
						AND favoriteColor != 'verde'
						AND email like '%@company.com'
					UNION
					SELECT name
					FROM employees
					WHERE department = 'IT'
						AND sickDaysLeft > 2
						AND favoriteColor != 'verde'
						AND email like '%@company.com'
					ORDER BY name",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 250000 );
				expect( actual.name[1] ).toBe( 'Doris Calhoun1' );
			});

			describe( 'Sorting' , ()=>{

				loop list="#typeList#,unicode" index="local.datatype" {
					it( title='can sort #datatype#', data={ datatype : datatype } , body=function(data) {
						var actual = QueryExecute(
							sql = "
								SELECT type, #data.datatype#
								FROM qrySort
								ORDER BY #data.datatype# desc",
							options = { dbtype: 'query' }
						);
						expect( actual.type[1] ).toBe( 'higher', '#data.datatype# did not sort correctly' );
						expect( actual.type[2] ).toBe( 'lower', '#data.datatype# did not sort correctly'  );
					});
				}

				it( 'can sort strings with nulls', ()=>{
					var actual = QueryExecute(
						sql = "
							SELECT type, string
							FROM qryNullSort
							ORDER BY string desc",
						options = { dbtype: 'query' }
					);
					expect( actual.type[1] ).toBe( 'higher' );
					expect( actual.type[2] ).toBe( 'lower' );
					expect( actual.type[3] ).toBe( 'null' );
					expect( actual.type[3] ).toBe( 'null' );
				});

				it( 'can sort numbers with nulls', ()=>{
					var actual = QueryExecute(
						sql = "
							SELECT type, integer
							FROM qryNullSort
							ORDER BY integer desc",
						options = { dbtype: 'query' }
					);
					expect( actual.type[1] ).toBe( 'higher' );
					expect( actual.type[2] ).toBe( 'lower' );
					expect( actual.type[3] ).toBe( 'null' );
					expect( actual.type[3] ).toBe( 'null' );
				});

			});

		});

	}


}