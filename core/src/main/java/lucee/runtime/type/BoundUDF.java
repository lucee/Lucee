package lucee.runtime.type;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;

/**
 * LDEV-6298 v2: bound-method wrapper around a shared {@link UDFGSProperty} flyweight.
 *
 * <p>Captures the calling component at extraction time so slow-path dispatch (UDF references,
 * bracket-key lookup, higher-order pass) reads scope from the right instance even when the inner
 * flyweight is shared across instances — both for {@code Duplicate(cfc)} (the LDEV-6298 v2 share)
 * and for the class-level static accessor pool that LDEV-3335 emits but doesn't yet consume. Both
 * threads converge on the same dispatch contract: {@code _call(pc, comp, args)} no longer trusts
 * {@code srcComponent}, so {@code srcComponent} can be the original owner (this share) or
 * {@code null} (LDEV-3335 Option 2 revival).
 *
 * <p>Allocated on every extraction through {@link lucee.runtime.ComponentImpl#get}; the fast path
 * ({@code obj.method()}) goes through {@code ComponentImpl._call} which dispatches via
 * {@link UDFGSProperty#_call} directly without allocating a wrapper.
 */
public final class BoundUDF implements UDFPlus {

	private static final long serialVersionUID = 1L;

	private final UDFGSProperty inner;
	private final Component callingComp;

	public BoundUDF(UDFGSProperty inner, Component callingComp) {
		this.inner = inner;
		this.callingComp = callingComp;
	}

	public UDFGSProperty getInner() {
		return inner;
	}

	// Wrapper-transparent equality. Two BoundUDFs with the same inner are equal regardless of
	// callingComp; a BoundUDF equals its raw inner. Required for Component equality (StructSupport
	// iterates accessor keys and compares values via UDF.equals — pre-WIP UDFGSProperty.equals
	// was signature-based, so cross-instance Component compares were equal). Hibernate's
	// HBMCreator.createFKColumnName depends on this contract (LDEV-6298 v2 testMany2Many).
	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other instanceof BoundUDF) other = ((BoundUDF) other).inner;
		return inner.equals(other);
	}

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	public Component getCallingComponent() {
		return callingComp;
	}

	// === Dispatch — uses callingComp instead of inner.srcComponent ===

	@Override
	public Object call(PageContext pc, Object[] args, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		UDF parent = pci.getActiveUDF();
		pci.setActiveUDF(inner);
		try {
			return inner._call(pc, callingComp, args);
		}
		finally {
			pci.setActiveUDF(parent);
		}
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Struct values, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		UDF parent = pci.getActiveUDF();
		pci.setActiveUDF(inner);
		try {
			return inner._callWithNamedValues(pc, callingComp, values);
		}
		finally {
			pci.setActiveUDF(parent);
		}
	}

	@Override
	public Object call(PageContext pc, Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		UDF parent = pci.getActiveUDF();
		Key parentName = pci.getActiveUDFCalledName();
		pci.setActiveUDF(inner);
		pci.setActiveUDFCalledName(calledName);
		try {
			return inner._call(pc, callingComp, args);
		}
		finally {
			pci.setActiveUDF(parent);
			pci.setActiveUDFCalledName(parentName);
		}
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key calledName, Struct values, boolean doIncludePath) throws PageException {
		PageContextImpl pci = (PageContextImpl) pc;
		UDF parent = pci.getActiveUDF();
		Key parentName = pci.getActiveUDFCalledName();
		pci.setActiveUDF(inner);
		pci.setActiveUDFCalledName(calledName);
		try {
			return inner._callWithNamedValues(pc, callingComp, values);
		}
		finally {
			pci.setActiveUDF(parent);
			pci.setActiveUDFCalledName(parentName);
		}
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return inner.implementation(pageContext);
	}

	// === Metadata — delegate to inner ===

	@Override
	public FunctionArgument[] getFunctionArguments() {
		return inner.getFunctionArguments();
	}

	@Override
	@Deprecated
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return inner.getDefaultValue(pc, index);
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return inner.getDefaultValue(pc, index, defaultValue);
	}

	@Override
	public int getIndex() {
		return inner.getIndex();
	}

	@Override
	public String getFunctionName() {
		return inner.getFunctionName();
	}

	@Override
	public boolean getOutput() {
		return inner.getOutput();
	}

	@Override
	public int getReturnType() {
		return inner.getReturnType();
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		return inner.getBufferOutput(pc);
	}

	@Override
	@Deprecated
	public int getReturnFormat() {
		return inner.getReturnFormat();
	}

	@Override
	public int getReturnFormat(int defaultFormat) {
		return inner.getReturnFormat(defaultFormat);
	}

	@Override
	public Boolean getSecureJson() {
		return inner.getSecureJson();
	}

	@Override
	public Boolean getVerifyClient() {
		return inner.getVerifyClient();
	}

	@Override
	public String getReturnTypeAsString() {
		return inner.getReturnTypeAsString();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public String getDisplayName() {
		return inner.getDisplayName();
	}

	@Override
	public String getHint() {
		return inner.getHint();
	}

	@Override
	public String getSource() {
		return inner.getSource();
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return inner.getMetaData(pc);
	}

	@Override
	public UDF duplicate() {
		return new BoundUDF(inner, callingComp);
	}

	@Override
	@Deprecated
	public Component getOwnerComponent() {
		return callingComp;
	}

	@Override
	public String id() {
		return inner.id();
	}

	@Override
	public PageSource getPageSource() {
		return inner.getPageSource();
	}

	// === Member ===

	@Override
	public int getAccess() {
		return inner.getAccess();
	}

	@Override
	public Object getValue() {
		return this;
	}

	@Override
	public int getModifier() {
		return inner.getModifier();
	}

	// === UDFPlus ===

	@Override
	public void setOwnerComponent(Component component) {
		// BoundUDF binding is captured at construction; the inner flyweight is shared so its
		// owner must not be mutated through this path. setOwnerComponent on the wrapper is a no-op.
	}

	@Override
	public void setAccess(int access) {
		inner.setAccess(access);
	}

	// === Dumpable ===

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return inner.toDumpData(pageContext, maxlevel, properties);
	}
}
