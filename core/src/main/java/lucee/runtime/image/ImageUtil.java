package lucee.runtime.image;

import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.osgi.framework.Version;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.Identification;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;

public class ImageUtil {

	private static Class getImageClass() {
		try {
			Config config = ThreadLocalPageContext.getConfig();
			Identification id = config == null ? null : config.getIdentification();
			return ClassUtil.loadClassByBundle("org.lucee.extension.image.Image", "image.extension", (Version) null, id, null);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static Class getImageUtilClass() {
		try {
			Config config = ThreadLocalPageContext.getConfig();
			Identification id = config == null ? null : config.getIdentification();
			return ClassUtil.loadClassByBundle("org.lucee.extension.image.ImageUtil", "image.extension", (Version) null, id, null);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static Object toImage(PageContext pc, Object obj, boolean checkForVariables, Object defaultValue) {
		try {
			return toImage(pc, obj, checkForVariables);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	private static Object toImage(PageContext pc, Object obj, boolean checkForVariables) throws PageException {
		try {
			Class clazz = getImageClass();
			if (clazz != null) {
				Method m = clazz.getMethod("toImage", new Class[] { PageContext.class, Object.class, boolean.class });
				return m.invoke(null, new Object[] { pc, obj, checkForVariables });
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		throw new ApplicationException("Cannot convert Object to an Image, you need to install the Image Extension to do so.");
	}

	public static byte[] getImageBytes(Object o, String format) throws PageException {
		try {
			Method m = o.getClass().getMethod("getImageBytes", new Class[] { String.class, boolean.class });
			return (byte[]) m.invoke(o, new Object[] { format, false });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public static byte[] getImageBytes(BufferedImage bi) throws PageException {
		try {
			Class clazz = getImageClass();
			if (clazz != null) {
				Constructor c = clazz.getConstructor(new Class[] { BufferedImage.class });
				Object o = c.newInstance(new Object[] { bi });
				return getImageBytes(o, "png");
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		throw new ApplicationException("Cannot convert BufferedImage to a byte array, you need to install the Image Extension to do so.");
	}

	public static BufferedImage toBufferedImage(Resource file, String format) throws PageException {
		try {
			Class clazz = getImageUtilClass();
			if (clazz != null) {
				Method m = clazz.getMethod("toBufferedImage", new Class[] { Resource.class, String.class });
				return (BufferedImage) m.invoke(null, new Object[] { file, format });
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
		throw new ApplicationException("Cannot convert Object to a BufferedImage, you need to install the Image Extension to do so.");
	}

	/*
	 * public static Type getImageType() { return Type.getType("Lorg/lucee/extension/image/Image;"); }
	 */

	public static boolean isCastableToImage(PageContext pc, Object obj) {
		try {
			Class clazz = ImageUtil.getImageClass();
			if (clazz != null) {
				Method m = clazz.getMethod("isCastableToImage", new Class[] { PageContext.class, Object.class });
				return (boolean) m.invoke(null, new Object[] { pc, obj });
			}
		}
		catch (Exception e) {}
		return false;
	}

	public static boolean isImage(Object obj) {
		try {
			Class clazz = ImageUtil.getImageClass();
			if (clazz != null) {
				Method m = clazz.getMethod("isImage", new Class[] { Object.class });
				return (boolean) m.invoke(null, new Object[] { obj });
			}
		}
		catch (Exception e) {}
		return false;
	}
}
