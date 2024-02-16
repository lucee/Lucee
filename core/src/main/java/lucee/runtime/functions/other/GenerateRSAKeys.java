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
		return createKeyPair(Caster.toIntValue(RSA.KEY_SIZE));
	}
	public static Struct call(PageContext pc, double keySize) throws PageException {
		return createKeyPair(Caster.toIntValue(keySize));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return createKeyPair(RSA.KEY_SIZE);
		else if (args.length == 1) return createKeyPair(Caster.toIntValue(args[1]));
		else throw new FunctionException(pc, "GenerateRSAKey", 2, 2, args.length);
	}

	private static Struct createKeyPair(int keySize) throws PageException {
		try {
			KeyPair keyPair = RSA.createKeyPair(keySize);
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
