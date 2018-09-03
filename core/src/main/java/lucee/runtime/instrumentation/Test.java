package lucee.runtime.instrumentation;
/*
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jdk.internal.misc.Unsafe;
import jdk.internal.misc.VM;
import lucee.print;
*/
public class Test {
   /* private static Unsafe unsafe;
	private static Field constructorModifiers;
	private static long constructorModifiersOffset;
	private static Field methodModifiers;
	private static long methodModifiersOffset;
	private static Field fieldModifiers;
	private static long fieldModifiersOffset;
	private static Method setAccessible;

	static
    {
		try {
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            unsafe = unsafeConstructor.newInstance();
            constructorModifiers = Constructor.class.getDeclaredField("modifiers");
            constructorModifiersOffset = unsafe.objectFieldOffset(constructorModifiers);
            methodModifiers = Method.class.getDeclaredField("modifiers");
            methodModifiersOffset = unsafe.objectFieldOffset(methodModifiers);
            fieldModifiers = Field.class.getDeclaredField("modifiers");
            fieldModifiersOffset = unsafe.objectFieldOffset(fieldModifiers);
            setAccessible = AccessibleObject.class.getDeclaredMethod("setAccessible0", boolean.class);
            setForceAccessible(setAccessible);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
    }*/
    /*private static boolean setForceAccessible(AccessibleObject accessibleObject)
    {
        try
        {
            if (accessibleObject instanceof Constructor)
            {
                Constructor<?> object = (Constructor<?>) accessibleObject;
                unsafe.getAndSetInt(object, constructorModifiersOffset, addPublicModifier(object.getModifiers()));
                return true;
            }
            if (accessibleObject instanceof Method)
            {
                Method object = (Method) accessibleObject;
                unsafe.getAndSetInt(object, methodModifiersOffset, addPublicModifier(object.getModifiers()));
                return true;
            }
            if (accessibleObject instanceof Field)
            {
                Field object = (Field) accessibleObject;
                unsafe.getAndSetInt(object, fieldModifiersOffset, addPublicModifier(object.getModifiers()));
                return true;
            }
            return false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private static int addPublicModifier(int mod)
    {
        mod &= ~ (Modifier.PRIVATE);
        mod &= ~ (Modifier.PROTECTED);
        mod |= (Modifier.PUBLIC);
        return mod;
    }
    
    public static void main(String[] args) {
    	//print.e(VM.getSystemProperties());
	print.e(VM.getSavedProperty("jdk.attach.allowAttachSelf"));
	}*/
}
