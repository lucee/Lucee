<cfscript>
    test()

    private void function test()
    {
        var myArray = [1, 2, 3]
        local.dance = "tango"

        myArray.each(function(x) {
        })

        [1, 2, 3].each( function(i){ echo( i )  } ) 
    }

</cfscript>
