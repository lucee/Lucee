package lucee.runtime.functions.other;

import java.security.KeyPair;

import lucee.runtime.PageContext;
import lucee.runtime.coder.RSA;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class GenerateRSAKeys extends BIF {

	private static final long serialVersionUID = 8436907807706520039L;

	public static Struct call(PageContext pc) throws PageException {
		return createKeyPair();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return createKeyPair();
		else throw new FunctionException(pc, "GenerateRSAKey", 0, 0, args.length);
	}

	private static Struct createKeyPair() throws PageException {
		try {
			KeyPair keyPair = RSA.createKeyPair();
			Struct sct = new StructImpl();
			sct.set("private", RSA.toString(keyPair.getPrivate()));
			sct.set("public", RSA.toString(keyPair.getPublic()));
			return sct;
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
}
