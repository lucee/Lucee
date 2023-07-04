<cfscript>
    try {
        throw "a";
    }
    finally {
        try {
            try {
                throw "b";
            }
            finally {}
        }
        finally {}
    }
</cfscript>