package lucee.runtime.functions.query;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.*;

import java.sql.SQLException;

public final class QueryClose extends BIF {

    private static final long serialVersionUID = 6778838386679577852L;

    public static boolean call(PageContext pc, Query qry) throws PageException {
        try {
            if(!qry.isClosed()){
                qry.close();
            }
        } catch (SQLException e) {
            // safe to ignore
        }
        return true;
    }


    @Override
    public Object invoke(PageContext pc, Object[] args) throws PageException {

        if (args.length == 1) return call(pc, Caster.toQuery(args[0]));
        throw new FunctionException(pc, "QueryClose", 1, 1, args.length);
    }
}
