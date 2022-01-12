component extends="org.lucee.cfml.test.LuceeTestCase"	{

	function beforeAll() {
			employees = queryNew( 'name,age,email,department,isContract,yearsEmployed,sickDaysLeft,hireDate,isActive,empID,favoriteColor', 'varchar,integer,varchar,varchar,boolean,integer,integer,date,boolean,varchar,varchar', [
				[ 'John Doe',28,'John@company.com','Acounting',false,2,4,createDate(2010,1,21),true,'sdf','red' ],
				[ 'Jane Doe',22,'Jane@company.com','Acounting',false,0,8,createDate(2011,2,21),true,'hdfg','blue' ],
				[ 'Bane Doe',28,'Bane@company.com','Acounting',true,3,2,createDate(2012,3,21),true,'sdsfsff','green' ],
				[ 'Tom Smith',25,'Tom@company.com','Acounting',false,6,4,createDate(2013,4,21),false,'HDFG','yellow' ],
				[ 'Harry Johnson',38,'Harry@company.com','IT',false,8,6,createDate(2014,5,21),true,'4ge','purple' ],
				[ 'Jason Wood',37,'Jason@company.com','IT',false,19,4,createDate(2015,6,21),true,'ShrtDF','Red' ],
				[ 'Doris Calhoun',67,'Doris@company.com','IT',true,3,6,createDate(2016,7,21),true,'sgsdg','Blue' ],
				[ 'Mary Root',17,'Mary@company.com','IT',false,8,2,createDate(2017,8,21),true,'','Green' ],
				[ 'Aurthur Duff',23,'Aurthur@company.com','IT',false,4,0,createDate(2018,9,21),true,nullValue(),'Yellow' ],
				[ 'Luis Hake',29,'Luis@company.com','IT',true,9,5,createDate(2019,10,21),true,nullValue(),'Purple' ],
				[ 'Gavin Bezos',46,'Gavin@company.com','HR',false,2,5,createDate(2020,11,21),false,nullValue(),'RED' ],
				[ 'Nancy Garmon',57,'Nancy@company.com','HR',false,14,9,createDate(2005,12,21),true,nullValue(),'BLUE' ],
				[ 'Tom Zuckerburg',27,'Tom@company.com','HR',true,16,10,createDate(2006,1,21),true,nullValue(),'GREEN' ],
				[ 'Richard Gates',62,'Richard@company.com','Executive',false,11,1,createDate(2007,2,21),true,nullValue(),'YELLOW' ],
				[ 'Amy Merryweather',58,'Amy@company.com','Executive',false,12,2,createDate(2008,3,21),true,nullValue(),'PURPLE' ]
			] );

	}

	function run( testResults , testBox ) {

		describe( 'QofQ' , function(){

			it( 'Can select *' , function() {				
				actual = QueryExecute(
					sql = "SELECT * FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( employees.recordcount );
			});

			it( 'Can select with extra space in multi-word clause' , function() {				
				actual = QueryExecute(
					sql = "SELECT count(1) from employees where empID is 	 null or empID is 	 not 	 null and isActive not  	 like 'test' and isactive not 	   in ('test')",
					options = { dbtype: 'query' }
				);
				expect( actual ).toBeQuery();
				expect( actual.recordcount ).toBe( 1 );
			});

			it( 'Can select with functions' , function() {				
				actual = QueryExecute(
					sql = "SELECT upper(name), lower(email), coalesce( null, age ) FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( employees.recordcount );
			});
			
				
			it( 'Can select with math operations' , function() {				
				actual = QueryExecute(
					sql = "SELECT yearsEmployed/sickDaysLeft as calc1,
								yearsEmployed*sickDaysLeft as calc2,
								yearsEmployed-sickDaysLeft as calc3,
								yearsEmployed+sickDaysLeft as calc4
							from employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
			});

			it( 'Can select with functions that are aliased' , function() {				
				actual = QueryExecute(
					sql = "SELECT upper(name) as name, lower(email) email, coalesce( null, age ) as foo FROM employees",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( employees.recordcount );
			});
				
			it( 'Can select with order bys' , function() {				
				actual = QueryExecute(
					sql = "SELECT * from employees ORDER BY department, isActive desc, name, email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
				expect( actual.email[1] ).toBe( 'Bane@company.com' );
				expect( actual.email[15] ).toBe( 'Mary@company.com' );
			});
				
			it( 'Can order by alias' , function() {				
				actual = QueryExecute(
					sql = "SELECT department as dept from employees ORDER BY dept",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
				expect( actual.dept[1] ).toBe( 'Acounting' );
				expect( actual.dept[15] ).toBe( 'IT' );
			});
				
			it( 'Can order by literal' , function() {				
				actual = QueryExecute(
					sql = "SELECT department as dept from employees ORDER BY name,'test', 'foo', 7, false",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
				expect( actual.dept[1] ).toBe( 'Acounting' );
				expect( actual.dept[15] ).toBe( 'Executive' );
			});
				
			it( 'Can order by columns not in select' , function() {				
				actual = QueryExecute(
					sql = "SELECT department from employees ORDER BY department, isActive desc, name, email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 15 );
				expect( actual.department[1] ).toBe( 'Acounting' );
				expect( actual.department[15] ).toBe( 'IT' );
			});
				
			it( 'Can have extra whitespace in group by and order by clauses' , function() {
				actual = QueryExecute(
					sql = "SELECT department from employees group       by department ORDER       BY department",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 4 );
				expect( actual.department[1] ).toBe( 'Acounting' );
				expect( actual.department[4] ).toBe( 'IT' );
			});
				
			it( 'Can filter on date column' , function() {
				actual = QueryExecute(
					sql = "SELECT * from employees where hireDate = '2019-10-21 00:00:00.000'",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 1 );
				expect( actual.name).toBe( 'Luis Hake' );
			});
				
			it( 'Can handle isnull' , function() {
				actual = QueryExecute(
					sql = "SELECT empid, isNull( empID, 'default' ) as empIDNonNull from employees where email in ( 'Doris@company.com','Mary@company.com','Aurthur@company.com' ) order by email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 3 );
				expect( actual.empid[1] ).toBe( '' );
				expect( actual.empidNonNull[1] ).toBe( 'default' );
				expect( actual.empid[2] ).toBe( 'sgsdg' );
				expect( actual.empidNonNull[2] ).toBe( 'sgsdg' );
				expect( actual.empid[3] ).toBe( '' );
				expect( actual.empidNonNull[3] ).toBe( '' );
			});
				
			it( 'Can handle isnull with full null support' , function() {
				application nullsupport=true action='update';
				actual = QueryExecute(
					sql = "SELECT empid, isNull( empID, 'default' ) as empIDNonNull from employees where email in ( 'Doris@company.com','Mary@company.com','Aurthur@company.com' ) order by email",
					options = { dbtype: 'query' }
				);
				expect( actual.recordcount ).toBe( 3 );
				expect( actual.empid[1] ).toBeNull();
				expect( actual.empidNonNull[1] ).toBe( 'default' );
				expect( actual.empid[2] ).toBe( 'sgsdg' );
				expect( actual.empidNonNull[2] ).toBe( 'sgsdg' );
				expect( actual.empid[3] ).toBe( '' );
				expect( actual.empidNonNull[3] ).toBe( '' );
				
				application nullsupport=false action='update';
			});

			describe( 'Distinct' , function(){
		
				it( 'Can select distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.department[1] ).toBe( 'Acounting' );
					expect( actual.department[2] ).toBe( 'Executive' );
					expect( actual.department[3] ).toBe( 'HR' );
					expect( actual.department[4] ).toBe( 'IT' );
				});
		
				it( 'Can select distinct with order by' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
				});
	
				it( 'Can select distinct with top' , function() {				
					actual = QueryExecute(
						sql = "SELECT top 2 distinct department as foo FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
	
				it( 'Can select distinct with maxrows' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department FROM employees order by department",
						options = { dbtype: 'query', maxrows: 2 }
					);
					expect( actual.recordcount ).toBe( 2 );
					expect( actual.department[1] ).toBe( 'Acounting' );
					expect( actual.department[2] ).toBe( 'Executive' );
				});
	
				it( 'Can select distinct with *' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct * FROM employees",
						options = { dbtype: 'query'}
					);
					expect( actual.recordcount ).toBe( employees.recordcount );
				});
				
			});

			describe( 'Query Union' , function(){
				
					it( 'Can union' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union
							SELECT * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 15 );
				});
				
				it( 'Can union all' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union all
							SELECT * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 30 );
				});
				
				it( 'Can union distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT upper( favoriteColor ) as favoriteColor FROM employees
							union distinct
							SELECT upper( favoriteColor ) FROM employees
							order by favoriteColor",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 5 );
					expect( actual.favoriteColor[1] ).toBeWithCase( 'BLUE' );
					expect( actual.favoriteColor[2] ).toBeWithCase( 'GREEN' );
					expect( actual.favoriteColor[3] ).toBeWithCase( 'PURPLE' );
					expect( actual.favoriteColor[4] ).toBeWithCase( 'RED' );
					expect( actual.favoriteColor[5] ).toBeWithCase( 'YELLOW' );
				});
				
				it( 'Can union with top' , function() {				
					actual = QueryExecute(
						sql = "SELECT top 2 * FROM employees
							union all
							SELECT top 3 * FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 5 );
				});
				
				it( 'Can union with maxrows' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union all
							SELECT * FROM employees",
						options = { dbtype: 'query', maxrows: 2  }
					);
					expect( actual.recordcount ).toBe( 2 );
				});
				
				it( 'Can union with order' , function() {				
					actual = QueryExecute(
						sql = "SELECT * FROM employees
							union
							SELECT * FROM employees
							order by department, name desc",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 15 );
					expect( actual.email[1] ).toBe( 'Tom@company.com' );
					expect( actual.email[15] ).toBe( 'Aurthur@company.com' );
				});
				
				it( 'Can union with group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as thing, count(1) as count, max(age) as age FROM employees
							GROUP BY department
							union all
							SELECT age, count(*), min(sickDaysLeft) FROM employees
							GROUP BY age
							ORDER BY thing, age",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 18 );
					expect( actual.thing[1] ).toBe( 17 );
					expect( actual.count[1] ).toBe( 1 );
					expect( actual.age[1] ).toBe( 2 );
					expect( actual.thing[18] ).toBe( 'IT' );
					expect( actual.count[18] ).toBe( 6 );
					expect( actual.age[18] ).toBe( 67 );
				});
				
				it( 'Can union with literals' , function() {				
					actual = QueryExecute(
						sql = "SELECT TOP 1 'brad' as firstname, 'wood' as lastname FROM employees
							union all SELECT TOP 1 'Scott', 'Steinbeck' FROM employees
							union all SELECT TOP 1 'Gavin', 'Pickin' FROM employees
							union all SELECT TOP 1 'Luis', 'Majano' FROM employees
							",
						options = { dbtype: 'query'  }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.firstname[1] ).toBe( 'brad' );
					expect( actual.firstname[4] ).toBe( 'Luis' );
				});
				
			});

			describe( 'Query grouping' , function(){
				
				it( 'Can use aggregates with no group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT avg(age) as avgAge, count(1) as totalEmps, max(hireDate) as mostRecentHire, min(sickDaysLeft) as fewestSickDays FROM employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					expect( actual.avgAge ).toBe( 37.6 );
					expect( actual.totalEmps ).toBe( 15 );
					expect( actual.mostRecentHire ).toBe( "{ts '2020-11-21 00:00:00'}" );
					expect( actual.fewestSickDays ).toBe( 0 );
				});
				
				it( 'Can use count all and count distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT count(1) as cNum, 
									count(*) as cStar, 
									count('asdf') as cLiteral, 
									count(name) as cColumn ,  
									count( all department ) as cDeptAll,
									count( distinct department) as cDept, 
									count( distinct empID ) as cEmpID, 
									count( distinct favoriteColor ) as cfavoriteColor,
									count( distinct upper( favoriteColor ) ) as cUpperfavoriteColor 
								from employees",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					expect( actual.cNum ).toBe( 15 );
					expect( actual.cStar ).toBe( 15 );
					expect( actual.cLiteral ).toBe( 15 );
					expect( actual.cColumn ).toBe( 15 );
					expect( actual.cDeptAll ).toBe( 15 );
					expect( actual.cDept ).toBe( 4 );
					expect( actual.cEmpID ).toBe( 8 );
					expect( actual.cfavoriteColor ).toBe( 15 );
					expect( actual.cUpperfavoriteColor ).toBe( 5 );
				});
				
				it( 'Can use aggregates with no group by and where' , function() {				
					actual = QueryExecute(
						sql = "SELECT sum(age) sumAge FROM employees where department = 'IT'",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					expect( actual.sumAge ).toBe( 211 );
				});
				
				it( 'Can use group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as dept FROM employees GROUP BY department ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.dept[1] ).toBe( 'Acounting' );
					expect( actual.dept[2] ).toBe( 'Executive' );
					expect( actual.dept[3] ).toBe( 'HR' );
					expect( actual.dept[4] ).toBe( 'IT' );
				});
				
				it( 'Can use group by with distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT distinct department as dept FROM employees GROUP BY department ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.dept[1] ).toBe( 'Acounting' );
					expect( actual.dept[2] ).toBe( 'Executive' );
					expect( actual.dept[3] ).toBe( 'HR' );
					expect( actual.dept[4] ).toBe( 'IT' );
				});
				
				it( 'Can use group by with aggregates' , function() {				
					actual = QueryExecute(
						sql = "SELECT department as dept, max(hireDate) as mostRecentHire, min(age) as youngestAge, max( email ) FROM employees GROUP BY department order by mostRecentHire desc",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.dept[1] ).toBe( 'HR' );
					expect( actual.mostRecentHire[1] ).toBe( '2020-11-21 00:00:00' );
					expect( actual.dept[4] ).toBe( 'Executive' );
					expect( actual.mostRecentHire[4] ).toBe( '2008-03-21 00:00:00' );
				});
				
				it( 'Can use group by with more than one group by' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, isContract, isActive FROM employees GROUP BY department, isContract, isActive ORDER BY department, isContract, isActive",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 9 );
					expect( actual.department[1] ).toBe( 'Acounting' );
					expect( actual.department[5] ).toBe( 'HR' );
					expect( actual.department[9] ).toBe( 'IT' );
				});
				
				it( 'Can use group by with having clause' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
					expect( actual.department[1] ).toBe( 'Executive' );
					expect( actual.department[2] ).toBe( 'HR' );
					expect( actual.department[3] ).toBe( 'IT' );
					expect( actual.maxAge[1] ).toBe( 62 );
					expect( actual.maxAge[2] ).toBe( 57 );
					expect( actual.maxAge[3] ).toBe( 67 );
				});
				
				it( 'Can use group by with having clause and distinct' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY department",
						options = { dbtype: 'query' }
					)
					expect( actual.recordcount ).toBe( 3 );
					expect( actual.department[1] ).toBe( 'Executive' );
					expect( actual.department[2] ).toBe( 'HR' );
					expect( actual.department[3] ).toBe( 'IT' );
					expect( actual.maxAge[1] ).toBe( 62 );
					expect( actual.maxAge[2] ).toBe( 57 );
					expect( actual.maxAge[3] ).toBe( 67 );
				});
				
				it( 'Can use group by with operations' , function() {				
					actual = QueryExecute(
						sql = "SELECT lower(department) as lowerDept from employees GROUP BY upper(department) HAVING max(age) > 30 ORDER BY lower(department)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
					expect( actual.lowerDept[1] ).toBeWithCase( 'executive' );
					expect( actual.lowerDept[2] ).toBeWithCase( 'hr' );
					expect( actual.lowerDept[3] ).toBeWithCase( 'it' );
				});
				
				it( 'Can use group by with columns not in select' , function() {				
					actual = QueryExecute(
						sql = "SELECT 'test' as val from employees GROUP BY department, age, lower(email) ORDER BY upper(department)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 15 );
					expect( actual.val[1] ).toBe( 'test' );
					expect( actual.val[15] ).toBe( 'test' );
				});
				
				it( 'Can order by aggregate columns' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max(age) as maxAge from employees GROUP BY department HAVING max(age) > 30 ORDER BY max(age)",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 3 );
					expect( actual.department[1] ).toBe( 'HR' );
					expect( actual.department[2] ).toBe( 'Executive' );
					expect( actual.department[3] ).toBe( 'IT' );
					expect( actual.maxAge[1] ).toBe( 57 );
					expect( actual.maxAge[2] ).toBe( 62 );
					expect( actual.maxAge[3] ).toBe( 67 );
				});
				
				it( 'Can reference more than one column in aggregate function' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, sum(yearsEmployed * sickDaysLeft) as calc from employees GROUP BY department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 38 );
					expect( actual.calc[2] ).toBe( 35 );
					expect( actual.calc[3] ).toBe( 296 );
					expect( actual.calc[4] ).toBe( 203 );
				});
				
				it( 'Can wrap aggregate function in scalar function' , function() {				
					actual = QueryExecute(
						sql = "SELECT floor(sum(yearsEmployed * sickDaysLeft)) as calc from employees group by department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 38 );
					expect( actual.calc[2] ).toBe( 35 );
					expect( actual.calc[3] ).toBe( 296 );
					expect( actual.calc[4] ).toBe( 203 );
				});
				
				it( 'Can nest scalar functions inside of aggregates inside of scalar functions and use more than aggregate in a single operation' , function() {				
					actual = QueryExecute(
						sql = "SELECT department, max( yearsEmployed ) as max, count(1) as count, ceiling( max( floor( yearsEmployed ) )+count(1) )  as calc from employees group by department order by department",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 4 );
					expect( actual.calc[1] ).toBe( 10 );
					expect( actual.calc[2] ).toBe( 14 );
					expect( actual.calc[3] ).toBe( 19 );
					expect( actual.calc[4] ).toBe( 25 );
				});
				
				it( 'Aggregate select with no group by against empty query returns 1 row of empty strings' , function() {
					var qry = queryNew( 'col', 'varchar' );				
					actual = QueryExecute(
						sql = "SELECT 'const' as const, count(1) as count, avg(col) as avg, min(col) as min, max(col) as max, isNull( max(col), 'test' ) as max2 from qry",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					// Count always returns a number
					expect( actual.count[1] ).toBe( 0 );
					// Other aggregates return empty
					expect( actual.avg[1] ).toBe( '' );
					expect( actual.min[1] ).toBe( '' );
					expect( actual.max[1] ).toBe( '' );
					// Constant value is just passed through
					expect( actual.const[1] ).toBe( 'const' );
					// Scalar function still processes
					expect( actual.max2[1] ).toBe( 'test' );
				});
				
				it( 'Aggregate select with no group by and a where clause against empty query returns 1 row of empty strings' , function() {
					var qry = queryNew( 'col', 'varchar' );				
					actual = QueryExecute(
						sql = "SELECT 'const' as const, count(1) as count, avg(col) as avg, min(col) as min, max(col) as max, isNull( max(col), 'test' ) as max2 from qry where col = ''",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					// Count always returns a number
					expect( actual.count[1] ).toBe( 0 );
					// Other aggregates return empty
					expect( actual.avg[1] ).toBe( '' );
					expect( actual.min[1] ).toBe( '' );
					expect( actual.max[1] ).toBe( '' );
					// Constant value is just passed through
					expect( actual.const[1] ).toBe( 'const' );
					// Scalar function still processes
					expect( actual.max2[1] ).toBe( 'test' );
				});
				
				it( 'Aggregate select with group by against empty query returns 0 rows' , function() {
					var qry = queryNew( 'col', 'varchar' );				
					actual = QueryExecute(
						sql = "SELECT count(1) as count from qry group by col",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 0 );
				});
				
				it( 'Aggregates ignore null values' , function() {
					var qry = queryNew( 'col,col2', 'integer,integer', [
						[ 0, nullValue() ],
						[ nullValue(), nullValue() ],
						[ 100, nullValue() ],
					] );				
					actual = QueryExecute(
						sql = "SELECT sum(col) as sum, avg(col) as avg, min(col) as min, max(col) as max, sum(col2) as sum2, avg(col2) as avg2, min(col2) as min2, max(col2) as max2 from qry",
						options = { dbtype: 'query' }
					);
					expect( actual.recordcount ).toBe( 1 );
					
					// When some column values are null, aggregates should ignore the null values
					expect( actual.sum[1] ).toBe( 100 );
					expect( actual.avg[1] ).toBe( 50 );
					expect( actual.min[1] ).toBe( 0 );
					expect( actual.max[1] ).toBe( 100 );
					
					// When all column values are null, aggregates should return nothing
					expect( actual.sum2[1] ).toBe( '' );
					expect( actual.avg2[1] ).toBe( '' );
					expect( actual.min2[1] ).toBe( '' );
					expect( actual.max2[1] ).toBe( '' );
				});
								
			});

			
		});

	}
	
	
} 