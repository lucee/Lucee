package lucee.runtime.future;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.CatchBlockImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

public class Future implements Objects {

	private static final long serialVersionUID = -769725314696461494L;

	public static Object ARG_NULL = new Object();

	private final java.util.concurrent.Future<Object> future;
	private boolean hasError;
	private String error;
	private boolean hasCustomErrorHandler;
	private Exception exception;

	private final long timeout;

	private String[] names = new String[] { "cancel", "isCancelled", "isDone", "error", "get", "then" };
	private String[] rtns = new String[] { "boolean", "boolean", "boolean", "Future", "Future", "Future" };
	private String[][][] args = new String[][][] { new String[][] {}, new String[][] {}, new String[][] {}, new String[][] {},
			new String[][] { new String[] { "closure", "yes", "function" }, new String[] { "timezone", "no", "timespan" } },
			new String[][] { new String[] { "closure", "yes", "function" }, new String[] { "timezone", "no", "timespan" } } };

	public Future(java.util.concurrent.Future<Object> future, long timeout) {
		this.future = future;
		this.timeout = timeout;
	}

	public static Future _then(PageContext pc, UDF udf, long timeout) throws PageException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		return new Future(executor.submit(new CallableUDF(pc, udf, ARG_NULL)), timeout);
	}

	public Future then(PageContext pc, UDF udf, long timeout) throws PageException {
		if (this.hasError) return this;
		try {
			Object arg = get(pc, -1);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			return new Future(executor.submit(new CallableUDF(pc, udf, arg)), timeout);
		}
		catch (Exception e) {
			return handleExecutionError(pc, e);
		}
	}

	public Object get(PageContext pc, long timeout) throws PageException {
		if (timeout < 0) timeout = this.timeout;
		try {
			return timeout > 0 ? this.future.get(timeout, TimeUnit.MILLISECONDS) : this.future.get();
		}
		catch (Exception e) {
			setHasError(true);
			ThreadLocalPageContext.getLog(pc, "application").error("Async", e);
			throw Caster.toPageException(e);
		}
	}

	public Future error(PageContext pc, UDF udf, long timeout) {
		setHasCustomErrorHandler(true);
		if (this.hasError) {
			return executeErrorHandler(pc, udf, timeout, this.exception);
		}
		try {
			this.future.get();
			return this;
		}
		catch (Exception e) {
			return executeErrorHandler(pc, udf, timeout, e);
		}
	}

	private Future handleExecutionError(PageContext pc, Exception e) throws PageException {
		setHasError(true);
		this.exception = e;
		if (!this.hasCustomErrorHandler) {
			ThreadLocalPageContext.getLog(pc, "application").error("Async", e);
			throw Caster.toPageException(e);
		}
		return this;
	}

	private Future executeErrorHandler(PageContext pc, UDF udf, long timeout, Exception e) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		return new Future(executor.submit(new CallableUDF(pc, udf, new CatchBlockImpl(Caster.toPageException(e)))), timeout);
	}

	public boolean cancel() {
		return this.future.cancel(true);
	}

	public boolean isCancelled() {
		return this.future.isCancelled();
	}

	public boolean isDone() {
		return this.future.isDone();
	}

	boolean hasError() {
		return this.hasError;
	}

	void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	String getError() {
		return this.error;
	}

	void setError(String error) {
		this.error = error;
	}

	boolean hasCustomErrorHandler() {
		return this.hasCustomErrorHandler;
	}

	void setHasCustomErrorHandler(boolean hasCustomErrorHandler) {
		this.hasCustomErrorHandler = hasCustomErrorHandler;
	}

	Exception getException() {
		return this.exception;
	}

	void setException(Exception exception) {
		this.exception = exception;
	}

	@Override
	public Object call(PageContext pc, Key funcName, Object[] args) throws PageException {
		if ("cancel".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 0) return this.cancel();
			else throw new FunctionException(pc, "cancel", 0, 0, args.length);
		}
		if ("get".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 1) return get(pc, toTimeout(args[0]));
			else if (args.length == 0) return get(pc, -1);
			else throw new FunctionException(pc, "get", 0, 1, args.length);
		}
		if ("isCancelled".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 0) return this.isCancelled();
			else throw new FunctionException(pc, "isCancelled", 0, 0, args.length);
		}
		if ("isDone".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 0) return this.isDone();
			else throw new FunctionException(pc, "isDone", 0, 0, args.length);
		}
		if ("error".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 2) return this.error(pc, Caster.toFunction(args[0]), toTimeout(args[1]));
			else if (args.length == 1) return this.error(pc, Caster.toFunction(args[0]), 0);
			else throw new FunctionException(pc, "error", 1, 2, args.length);
		}
		if ("then".equalsIgnoreCase(funcName.getString())) {
			if (args.length == 2) return this.then(pc, Caster.toFunction(args[0]), toTimeout(args[1]));
			else if (args.length == 1) return this.then(pc, Caster.toFunction(args[0]), 0);
			else throw new FunctionException(pc, "then", 1, 2, args.length);
		}
		throw new ApplicationException("invalid function name [" + funcName + "], valid names are [" + ListUtil.arrayToList(names, ", ") + "]");
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key funcName, Struct args) throws PageException {
		if ("cancel".equalsIgnoreCase(funcName.getString())) {
			if (args.size() == 0) return this.cancel();
			else throw new FunctionException(pc, "cancel", 0, 0, args.size());
		}
		if ("get".equalsIgnoreCase(funcName.getString())) {
			if (args.size() == 0) return this.get(pc, extractTimeout(args));
			else if (args.size() == 0) return this.get(pc, -1);
			else throw new FunctionException(pc, "get", 0, 0, args.size());
		}
		if ("isCancelled".equalsIgnoreCase(funcName.getString())) {
			if (args.size() == 0) return this.isCancelled();
			else throw new FunctionException(pc, "isCancelled", 0, 0, args.size());
		}
		if ("isDone".equalsIgnoreCase(funcName.getString())) {
			if (args.size() == 0) return this.isDone();
			else throw new FunctionException(pc, "isDone", 0, 0, args.size());
		}
		if ("error".equalsIgnoreCase(funcName.getString())) {
			return this.error(pc, extractUDF(args), extractTimeout(args));
		}
		if ("then".equalsIgnoreCase(funcName.getString())) {
			return this.then(pc, extractUDF(args), extractTimeout(args));
		}
		throw new ApplicationException("invalid function name [" + funcName + "], valid names are [" + ListUtil.arrayToList(names, ", ") + "]");
	}

	private UDF extractUDF(Struct args) throws PageException {
		Object udf = args.get(KeyConstants._closure, null);
		if (udf == null) udf = args.get(KeyConstants._callback, null);
		if (udf == null) udf = args.get(KeyConstants._function, null);
		if (udf == null) udf = args.get(KeyConstants._udf, null);
		if (udf == null) throw new ApplicationException("argument [closure] is required but was not passed in");
		return Caster.toFunction(udf);
	}

	private long extractTimeout(Struct args) throws CasterException {
		Object obj = args.get(KeyConstants._timeout, null);
		if (obj == null) obj = args.get(KeyConstants._timespan, null);
		if (obj == null) return 0;

		return toTimeout(obj);
	}

	private long toTimeout(Object obj) throws CasterException {
		if (obj == null) return 0;

		TimeSpan ts = Caster.toTimespan(obj, null);
		if (ts != null) return ts.getMillis();

		Long l = Caster.toLong(obj, null);
		if (l != null) return l.longValue();

		throw new CasterException(obj, "timespan");
	}

	@Override
	public DumpData toDumpData(PageContext pc, int arg1, DumpProperties arg2) {
		DumpTable table = new DumpTable("component", "#77694f", "#c2baad", "#0099ff");
		table.setTitle("Future");
		DumpTable td;
		String[][] _arg;
		for (int i = 0; i < names.length; i++) {

			td = new DumpTable("component", "#77694f", "#c2baad", "#0099ff");
			td.setTitle("Function " + names[i]);

			// arguments
			DumpTable arg = new DumpTable("component", "#77694f", "#c2baad", "#0099ff");
			_arg = args[i];
			arg.appendRow(255, new SimpleDumpData("name"), new SimpleDumpData("required"), new SimpleDumpData("type"));
			for (String[] a: _arg) {
				arg.appendRow(0, new SimpleDumpData(a[0]), new SimpleDumpData(a[1]), new SimpleDumpData(a[2]));

			}

			td.appendRow(1, new SimpleDumpData("arguments"), arg);

			// label name required type default hint

			// return
			td.appendRow(1, new SimpleDumpData("return type"), new SimpleDumpData(rtns[i]));

			table.appendRow(1, new SimpleDumpData(names[i]), td);
		}
		return table;
	}

	@Override
	public Object get(PageContext pc, Key arg1) throws PageException {
		throw notSupported();
	}

	@Override
	public Object get(PageContext pc, Key arg1, Object arg2) {
		throw notSupported();
	}

	@Override
	public Object set(PageContext pc, Key arg1, Object arg2) throws PageException {
		throw notSupported();
	}

	@Override
	public Object setEL(PageContext pc, Key arg1, Object arg2) {
		throw notSupported();
	}

	private PageRuntimeException notSupported() {
		return new PageRuntimeException(new ApplicationException("this object only support calling functions."));
	}

	@Override
	public String castToString() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Query to String", "Use Built-In-Function \"serialize(Query):String\" to create a String from Query");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Query to a boolean value");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Query to a number value");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws ExpressionException {
		throw new ExpressionException("Can't cast Complex Object Type Query to a Date");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare Complex Object Type Query with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Query with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Query with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Query with a String");
	}
}
