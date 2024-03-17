package lucee.runtime.functions.system;

import java.util.Date;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Castable;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.SimpleValue;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;

public class ValueRef extends BIF {

	private static final long serialVersionUID = 1195300110252570841L;

	public static Object call(PageContext pc, UDF ref) throws PageException {
		return new UDFValue(ref);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toFunction(args[0]));
		else throw new FunctionException(pc, "LuceeValueRef", 1, 1, args.length);
	}

	private static class UDFValue implements Castable, SimpleValue, Dumpable {
		private static final long serialVersionUID = -5028960028109980321L;
		private UDF udf;

		public UDFValue(UDF udf) {
			this.udf = udf;
		}

		@Override
		public Boolean castToBoolean(Boolean defaultValue) {
			try {
				return Caster.toBoolean(udf.call(ThreadLocalPageContext.get(), new Object[0], true), defaultValue);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		@Override
		public boolean castToBooleanValue() throws PageException {
			return Caster.toBooleanValue(udf.call(ThreadLocalPageContext.get(), new Object[0], true));
		}

		@Override
		public DateTime castToDateTime() throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return Caster.toDatetime(udf.call(pc, new Object[0], true), ThreadLocalPageContext.getTimeZone(pc));
		}

		@Override
		public DateTime castToDateTime(DateTime defaultValue) {
			PageContext pc = ThreadLocalPageContext.get();
			try {
				return Caster.toDatetime(udf.call(pc, new Object[0], true), ThreadLocalPageContext.getTimeZone(pc));
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		@Override
		public double castToDoubleValue() throws PageException {
			return Caster.toDoubleValue(udf.call(ThreadLocalPageContext.get(), new Object[0], true));
		}

		@Override
		public double castToDoubleValue(double defaultValue) {
			try {
				return Caster.toDoubleValue(udf.call(ThreadLocalPageContext.get(), new Object[0], true), false, defaultValue);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		@Override
		public String castToString() throws PageException {
			return Caster.toString(udf.call(ThreadLocalPageContext.get(), new Object[0], true));
		}

		@Override
		public String castToString(String defaultValue) {
			try {
				return Caster.toString(udf.call(ThreadLocalPageContext.get(), new Object[0], true), defaultValue);
			}
			catch (PageException e) {
				return defaultValue;
			}
		}

		@Override
		public int compareTo(String other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, udf.call(pc, new Object[0], true), other);
		}

		@Override
		public int compareTo(boolean other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, udf.call(pc, new Object[0], true), other);
		}

		@Override
		public int compareTo(double other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, udf.call(pc, new Object[0], true), other);
		}

		@Override
		public int compareTo(DateTime other) throws PageException {
			PageContext pc = ThreadLocalPageContext.get();
			return OpUtil.compare(pc, udf.call(pc, new Object[0], true), (Date) other);
		}

		@Override
		public String toString() {
			Object obj = null;
			try {
				obj = udf.call(ThreadLocalPageContext.get(), new Object[0], true);
			}
			catch (PageException e) {
				throw new RuntimeException(e);
			}

			try {
				return Caster.toString(obj);
			}
			catch (PageException e) {
				return obj.toString();
			}

		}

		@Override
		public DumpData toDumpData(PageContext pc, int level, DumpProperties props) {
			try {
				DumpTable data = (DumpTable) DumpUtil.toDumpData(udf.call(ThreadLocalPageContext.get(), new Object[0], true), pc, level, props);
				data.setTitle("UDF Reference");
				data.setHighLightColor("#9b1ab0");
				data.setNormalColor("#e129ff");
				data.setBorderColor("#000000");
				return data;
			}
			catch (PageException e) {
				throw new RuntimeException(e);
			}
		}
	}
}