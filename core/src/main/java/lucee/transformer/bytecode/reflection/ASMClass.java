/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.transformer.bytecode.reflection;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericSignatureFormatError;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public final class ASMClass implements java.io.Serializable {

	private String name;
	private Map<String, ASMMethod> methods;

	public ASMClass(String name, Map<String, ASMMethod> methods) {
		this.name = name;
		this.methods = methods;
	}

	/**
	 * Converts the object to a string. The string representation is the string "class" or "interface",
	 * followed by a space, and then by the fully qualified name of the class in the format returned by
	 * {@code getName}. If this {@code Class} object represents a primitive type, this method returns
	 * the name of the primitive type. If this {@code Class} object represents void this method returns
	 * "void".
	 *
	 * @return a string representation of this class object.
	 */
	@Override
	public String toString() {
		return (isInterface() ? "interface " : (isPrimitive() ? "" : "class ")) + getName();
	}

	/**
	 * Creates a new instance of the class represented by this {@code Class} object. The class is
	 * instantiated as if by a {@code new} expression with an empty argument list. The class is
	 * initialized if it has not already been initialized.
	 *
	 * <p>
	 * Note that this method propagates any exception thrown by the nullary constructor, including a
	 * checked exception. Use of this method effectively bypasses the compile-time exception checking
	 * that would otherwise be performed by the compiler. The
	 * {@link java.lang.reflect.Constructor#newInstance(java.lang.Object...) Constructor.newInstance}
	 * method avoids this problem by wrapping any exception thrown by the constructor in a (checked)
	 * {@link java.lang.reflect.InvocationTargetException}.
	 *
	 * @return a newly allocated instance of the class represented by this object.
	 * @exception IllegalAccessException if the class or its nullary constructor is not accessible.
	 * @exception InstantiationException if this {@code Class} represents an abstract class, an
	 *                interface, an array class, a primitive type, or void; or if the class has no
	 *                nullary constructor; or if the instantiation fails for some other reason.
	 * @exception ExceptionInInitializerError if the initialization provoked by this method fails.
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies creation of new instances of this
	 *                class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 */
	public Object newInstance() throws InstantiationException, IllegalAccessException {
		_throw();
		return null;
	}

	/**
	 * Determines if the specified {@code Class} object represents an interface type.
	 *
	 * @return {@code true} if this object represents an interface; {@code false} otherwise.
	 */
	public boolean isInterface() {
		_throw();
		return false;
	}

	/**
	 * Determines if this {@code Class} object represents an array class.
	 *
	 * @return {@code true} if this object represents an array class; {@code false} otherwise.
	 * @since JDK1.1
	 */
	public boolean isArray() {
		_throw();
		return false;
	}

	/**
	 * Determines if the specified {@code Class} object represents a primitive type.
	 *
	 * <p>
	 * There are nine predefined {@code Class} objects to represent the eight primitive types and void.
	 * These are created by the Java Virtual Machine, and have the same names as the primitive types
	 * that they represent, namely {@code boolean}, {@code byte}, {@code char}, {@code short},
	 * {@code int}, {@code long}, {@code float}, and {@code double}.
	 *
	 * <p>
	 * These objects may only be accessed via the following public static final variables, and are the
	 * only {@code Class} objects for which this method returns {@code true}.
	 *
	 * @return true if and only if this class represents a primitive type
	 *
	 * @see java.lang.Boolean#TYPE
	 * @see java.lang.Character#TYPE
	 * @see java.lang.Byte#TYPE
	 * @see java.lang.Short#TYPE
	 * @see java.lang.Integer#TYPE
	 * @see java.lang.Long#TYPE
	 * @see java.lang.Float#TYPE
	 * @see java.lang.Double#TYPE
	 * @see java.lang.Void#TYPE
	 * @since JDK1.1
	 */
	public boolean isPrimitive() {
		_throw();
		return false;
	}

	/**
	 * Returns the name of the entity (class, interface, array class, primitive type, or void)
	 * represented by this {@code Class} object, as a {@code String}.
	 *
	 * <p>
	 * If this class object represents a reference type that is not an array type then the binary name
	 * of the class is returned, as specified by the Java Language Specification, Second Edition.
	 *
	 * <p>
	 * If this class object represents a primitive type or void, then the name returned is a
	 * {@code String} equal to the Java language keyword corresponding to the primitive type or void.
	 *
	 * <p>
	 * If this class object represents a class of arrays, then the internal form of the name consists of
	 * the name of the element type preceded by one or more '{@code [}' characters representing the
	 * depth of the array nesting. The encoding of element type names is as follows:
	 *
	 * <blockquote>
	 * <table summary="Element types and encodings">
	 * <tr>
	 * <th>Element Type
	 * <th>&nbsp;&nbsp;&nbsp;
	 * <th>Encoding
	 * <tr>
	 * <td>boolean
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>Z
	 * <tr>
	 * <td>byte
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>B
	 * <tr>
	 * <td>char
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>C
	 * <tr>
	 * <td>class or interface
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>L<i>classname</i>;
	 * <tr>
	 * <td>double
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>D
	 * <tr>
	 * <td>float
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>F
	 * <tr>
	 * <td>int
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>I
	 * <tr>
	 * <td>long
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>J
	 * <tr>
	 * <td>short
	 * <td>&nbsp;&nbsp;&nbsp;
	 * <td align=center>S
	 * </table>
	 * </blockquote>
	 *
	 * <p>
	 * The class or interface name <i>classname</i> is the binary name of the class specified above.
	 *
	 * <p>
	 * Examples: <blockquote>
	 * 
	 * <pre>
	 * String.class.getName()
	 *     returns "java.lang.String"
	 * byte.class.getName()
	 *     returns "byte"
	 * (new Object[3]).getClass().getName()
	 *     returns "[Ljava.lang.Object;"
	 * (new int[3][4][5][6][7][8][9]).getClass().getName()
	 *     returns "[[[[[[[I"
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @return the name of the class or interface represented by this object.
	 */
	public String getName() {
		_throw();
		return name;
	}

	/**
	 * Returns the class loader for the class. Some implementations may use null to represent the
	 * bootstrap class loader. This method will return null in such implementations if this class was
	 * loaded by the bootstrap class loader.
	 *
	 * <p>
	 * If a security manager is present, and the caller's class loader is not null and the caller's
	 * class loader is not the same as or an ancestor of the class loader for the class whose class
	 * loader is requested, then this method calls the security manager's {@code checkPermission} method
	 * with a {@code RuntimePermission("getClassLoader")} permission to ensure it's ok to access the
	 * class loader for the class.
	 *
	 * <p>
	 * If this object represents a primitive type or void, null is returned.
	 *
	 * @return the class loader that loaded the class or interface represented by this object.
	 * @throws SecurityException if a security manager exists and its {@code checkPermission} method
	 *             denies access to the class loader for the class.
	 * @see java.lang.ClassLoader
	 * @see SecurityManager#checkPermission
	 * @see java.lang.RuntimePermission
	 */
	public ClassLoader getClassLoader() {
		_throw();
		return null;
	}

	/**
	 * Returns the {@code Class} representing the superclass of the entity (class, interface, primitive
	 * type or void) represented by this {@code Class}. If this {@code Class} represents either the
	 * {@code Object} class, an interface, a primitive type, or void, then null is returned. If this
	 * object represents an array class then the {@code Class} object representing the {@code Object}
	 * class is returned.
	 *
	 * @return the superclass of the class represented by this object.
	 */
	public Class getSuperclass() {
		_throw();
		return null;
	}

	/**
	 * Returns the {@code Type} representing the direct superclass of the entity (class, interface,
	 * primitive type or void) represented by this {@code Class}.
	 *
	 * <p>
	 * If the superclass is a parameterized type, the {@code Type} object returned must accurately
	 * reflect the actual type parameters used in the source code. The parameterized type representing
	 * the superclass is created if it had not been created before. See the declaration of
	 * {@link java.lang.reflect.ParameterizedType ParameterizedType} for the semantics of the creation
	 * process for parameterized types. If this {@code Class} represents either the {@code Object}
	 * class, an interface, a primitive type, or void, then null is returned. If this object represents
	 * an array class then the {@code Class} object representing the {@code Object} class is returned.
	 *
	 * @throws GenericSignatureFormatError if the generic class signature does not conform to the format
	 *             specified in the Java Virtual Machine Specification, 3rd edition
	 * @throws TypeNotPresentException if the generic superclass refers to a non-existent type
	 *             declaration
	 * @throws MalformedParameterizedTypeException if the generic superclass refers to a parameterized
	 *             type that cannot be instantiated for any reason
	 * @return the superclass of the class represented by this object
	 * @since 1.5
	 */
	public Type getGenericSuperclass() {
		_throw();
		return null;
	}

	/**
	 * Gets the package for this class. The class loader of this class is used to find the package. If
	 * the class was loaded by the bootstrap class loader the set of packages loaded from CLASSPATH is
	 * searched to find the package of the class. Null is returned if no package object was created by
	 * the class loader of this class.
	 *
	 * <p>
	 * Packages have attributes for versions and specifications only if the information was defined in
	 * the manifests that accompany the classes, and if the class loader created the package instance
	 * with the attributes from the manifest.
	 *
	 * @return the package of the class, or null if no package information is available from the archive
	 *         or codebase.
	 */
	public Package getPackage() {
		_throw();
		return null;
	}

	/**
	 * Determines the interfaces implemented by the class or interface represented by this object.
	 *
	 * <p>
	 * If this object represents a class, the return value is an array containing objects representing
	 * all interfaces implemented by the class. The order of the interface objects in the array
	 * corresponds to the order of the interface names in the {@code implements} clause of the
	 * declaration of the class represented by this object. For example, given the declaration:
	 * <blockquote> {@code class Shimmer implements FloorWax, DessertTopping { ... }} </blockquote>
	 * suppose the value of {@code s} is an instance of {@code Shimmer}; the value of the expression:
	 * <blockquote> {@code s.getClass().getInterfaces()[0]} </blockquote> is the {@code Class} object
	 * that represents interface {@code FloorWax}; and the value of: <blockquote>
	 * {@code s.getClass().getInterfaces()[1]} </blockquote> is the {@code Class} object that represents
	 * interface {@code DessertTopping}.
	 *
	 * <p>
	 * If this object represents an interface, the array contains objects representing all interfaces
	 * extended by the interface. The order of the interface objects in the array corresponds to the
	 * order of the interface names in the {@code extends} clause of the declaration of the interface
	 * represented by this object.
	 *
	 * <p>
	 * If this object represents a class or interface that implements no interfaces, the method returns
	 * an array of length 0.
	 *
	 * <p>
	 * If this object represents a primitive type or void, the method returns an array of length 0.
	 *
	 * @return an array of interfaces implemented by this class.
	 */
	public Class[] getInterfaces() {
		_throw();
		return null;
	}

	/**
	 * Returns the {@code Class} representing the component type of an array. If this class does not
	 * represent an array class this method returns null.
	 *
	 * @return the {@code Class} representing the component type of this class if this class is an array
	 * @see java.lang.reflect.Array
	 * @since JDK1.1
	 */
	public Class getComponentType() {
		_throw();
		return null;
	}

	/**
	 * Returns the Java language modifiers for this class or interface, encoded in an integer. The
	 * modifiers consist of the Java Virtual Machine's constants for {@code public}, {@code protected},
	 * {@code private}, {@code final}, {@code static}, {@code abstract} and {@code interface}; they
	 * should be decoded using the methods of class {@code Modifier}.
	 *
	 * <p>
	 * If the underlying class is an array class, then its {@code public}, {@code private} and
	 * {@code protected} modifiers are the same as those of its component type. If this {@code Class}
	 * represents a primitive type or void, its {@code public} modifier is always {@code true}, and its
	 * {@code protected} and {@code private} modifiers are always {@code false}. If this object
	 * represents an array class, a primitive type or void, then its {@code final} modifier is always
	 * {@code true} and its interface modifier is always {@code false}. The values of its other
	 * modifiers are not determined by this specification.
	 *
	 * <p>
	 * The modifier encodings are defined in <em>The Java Virtual Machine Specification</em>, table 4.1.
	 *
	 * @return the {@code int} representing the modifiers for this class
	 * @see java.lang.reflect.Modifier
	 * @since JDK1.1
	 */
	public int getModifiers() {
		_throw();
		return 0;
	}

	/**
	 * If the class or interface represented by this {@code Class} object is a member of another class,
	 * returns the {@code Class} object representing the class in which it was declared. This method
	 * returns null if this class or interface is not a member of any other class. If this {@code Class}
	 * object represents an array class, a primitive type, or void,then this method returns null.
	 *
	 * @return the declaring class for this class
	 * @since JDK1.1
	 */
	public Class getDeclaringClass() {
		_throw();
		return null;
	}

	/**
	 * Returns an array containing {@code Class} objects representing all the public classes and
	 * interfaces that are members of the class represented by this {@code Class} object. This includes
	 * public class and interface members inherited from superclasses and public class and interface
	 * members declared by the class. This method returns an array of length 0 if this {@code Class}
	 * object has no public member classes or interfaces. This method also returns an array of length 0
	 * if this {@code Class} object represents a primitive type, an array class, or void.
	 *
	 * @return the array of {@code Class} objects representing the public members of this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} method denies access to the classes
	 *                within this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Class[] getClasses() {
		_throw();
		return null;
	}

	/**
	 * Returns an array containing {@code Field} objects reflecting all the accessible public fields of
	 * the class or interface represented by this {@code Class} object. The elements in the array
	 * returned are not sorted and are not in any particular order. This method returns an array of
	 * length 0 if the class or interface has no accessible public fields, or if it represents an array
	 * class, a primitive type, or void.
	 *
	 * <p>
	 * Specifically, if this {@code Class} object represents a class, this method returns the public
	 * fields of this class and of all its superclasses. If this {@code Class} object represents an
	 * interface, this method returns the fields of this interface and of all its superinterfaces.
	 *
	 * <p>
	 * The implicit length field for array class is not reflected by this method. User code should use
	 * the methods of class {@code Array} to manipulate arrays.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
	 *
	 * @return the array of {@code Field} objects representing the public fields
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the fields within this
	 *                class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Field[] getFields() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns an array containing {@code Method} objects reflecting all the public <em>member</em>
	 * methods of the class or interface represented by this {@code Class} object, including those
	 * declared by the class or interface and those inherited from superclasses and superinterfaces.
	 * Array classes return all the (public) member methods inherited from the {@code Object} class. The
	 * elements in the array returned are not sorted and are not in any particular order. This method
	 * returns an array of length 0 if this {@code Class} object represents a class or interface that
	 * has no public member methods, or if this {@code Class} object represents a primitive type or
	 * void.
	 *
	 * <p>
	 * The class initialization method {@code <clinit>} is not included in the returned array. If the
	 * class declares multiple public member methods with the same parameter types, they are all
	 * included in the returned array.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
	 *
	 * @return the array of {@code Method} objects representing the public methods of this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the methods within this
	 *                class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public ASMMethod[] getMethods() throws SecurityException {
		_throw();
		return methods.values().toArray(new ASMMethod[methods.size()]);
	}

	/**
	 * Returns an array containing {@code Constructor} objects reflecting all the public constructors of
	 * the class represented by this {@code Class} object. An array of length 0 is returned if the class
	 * has no public constructors, or if the class is an array class, or if the class reflects a
	 * primitive type or void.
	 *
	 * Note that while this method returns an array of {@code
	 * Constructor<T>} objects (that is an array of constructors from this class), the return type of
	 * this method is {@code
	 * Constructor<?>[]} and <em>not</em> {@code Constructor<T>[]} as might be expected. This less
	 * informative return type is necessary since after being returned from this method, the array could
	 * be modified to hold {@code Constructor} objects for different classes, which would violate the
	 * type guarantees of {@code Constructor<T>[]}.
	 *
	 * @return the array of {@code Constructor} objects representing the public constructors of this
	 *         class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the constructors within
	 *                this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Constructor[] getConstructors() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Field} object that reflects the specified public member field of the class or
	 * interface represented by this {@code Class} object. The {@code name} parameter is a
	 * {@code String} specifying the simple name of the desired field.
	 *
	 * <p>
	 * The field to be reflected is determined by the algorithm that follows. Let C be the class
	 * represented by this object:
	 * <OL>
	 * <LI>If C declares a public field with the name specified, that is the field to be reflected.</LI>
	 * <LI>If no field was found in step 1 above, this algorithm is applied recursively to each direct
	 * superinterface of C. The direct superinterfaces are searched in the order they were
	 * declared.</LI>
	 * <LI>If no field was found in steps 1 and 2 above, and C has a superclass S, then this algorithm
	 * is invoked recursively upon S. If C has no superclass, then a {@code NoSuchFieldException} is
	 * thrown.</LI>
	 * </OL>
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
	 *
	 * @param name the field name
	 * @return the {@code Field} object of this class specified by {@code name}
	 * @exception NoSuchFieldException if a field with the specified name is not found.
	 * @exception NullPointerException if {@code name} is {@code null}
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the field
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Field getField(String name) throws NoSuchFieldException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Method} object that reflects the specified public member method of the class or
	 * interface represented by this {@code Class} object. The {@code name} parameter is a
	 * {@code String} specifying the simple name of the desired method. The {@code parameterTypes}
	 * parameter is an array of {@code Class} objects that identify the method's formal parameter types,
	 * in declared order. If {@code parameterTypes} is {@code null}, it is treated as if it were an
	 * empty array.
	 *
	 * <p>
	 * If the {@code name} is "{@code <init>};"or "{@code <clinit>}" a {@code NoSuchMethodException} is
	 * raised. Otherwise, the method to be reflected is determined by the algorithm that follows. Let C
	 * be the class represented by this object:
	 * <OL>
	 * <LI>C is searched for any <I>matching methods</I>. If no matching method is found, the algorithm
	 * of step 1 is invoked recursively on the superclass of C.</LI>
	 * <LI>If no method was found in step 1 above, the superinterfaces of C are searched for a matching
	 * method. If any such method is found, it is reflected.</LI>
	 * </OL>
	 *
	 * To find a matching method in a class C:&nbsp; If C declares exactly one public method with the
	 * specified name and exactly the same formal parameter types, that is the method reflected. If more
	 * than one such method is found in C, and one of these methods has a return type that is more
	 * specific than any of the others, that method is reflected; otherwise one of the methods is chosen
	 * arbitrarily.
	 *
	 * <p>
	 * Note that there may be more than one matching method in a class because while the Java language
	 * forbids a class to declare multiple methods with the same signature but different return types,
	 * the Java virtual machine does not. This increased flexibility in the virtual machine can be used
	 * to implement various language features. For example, covariant returns can be implemented with
	 * {@linkplain java.lang.reflect.Method#isBridge bridge methods}; the bridge method and the method
	 * being overridden would have the same signature but different return types.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and 8.4.
	 *
	 * @param name the name of the method
	 * @param parameterTypes the list of parameters
	 * @return the {@code Method} object that matches the specified {@code name} and
	 *         {@code parameterTypes}
	 * @exception NoSuchMethodException if a matching method is not found or if the name is
	 *                "&lt;init&gt;"or "&lt;clinit&gt;".
	 * @exception NullPointerException if {@code name} is {@code null}
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the method
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Constructor} object that reflects the specified public constructor of the class
	 * represented by this {@code Class} object. The {@code parameterTypes} parameter is an array of
	 * {@code Class} objects that identify the constructor's formal parameter types, in declared order.
	 *
	 * If this {@code Class} object represents an inner class declared in a non-static context, the
	 * formal parameter types include the explicit enclosing instance as the first parameter.
	 *
	 * <p>
	 * The constructor to reflect is the public constructor of the class represented by this
	 * {@code Class} object whose formal parameter types match those specified by
	 * {@code parameterTypes}.
	 *
	 * @param parameterTypes the parameter array
	 * @return the {@code Constructor} object of the public constructor that matches the specified
	 *         {@code parameterTypes}
	 * @exception NoSuchMethodException if a matching method is not found.
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.PUBLIC)} denies access to the constructor
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Constructor getConstructor(Class... parameterTypes) throws NoSuchMethodException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns an array of {@code Class} objects reflecting all the classes and interfaces declared as
	 * members of the class represented by this {@code Class} object. This includes public, protected,
	 * default (package) access, and private classes and interfaces declared by the class, but excludes
	 * inherited classes and interfaces. This method returns an array of length 0 if the class declares
	 * no classes or interfaces as members, or if this {@code Class} object represents a primitive type,
	 * an array class, or void.
	 *
	 * @return the array of {@code Class} objects representing all the declared members of this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared classes
	 *                within this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Class[] getDeclaredClasses() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns an array of {@code Field} objects reflecting all the fields declared by the class or
	 * interface represented by this {@code Class} object. This includes public, protected, default
	 * (package) access, and private fields, but excludes inherited fields. The elements in the array
	 * returned are not sorted and are not in any particular order. This method returns an array of
	 * length 0 if the class or interface declares no fields, or if this {@code Class} object represents
	 * a primitive type, an array class, or void.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, sections 8.2 and 8.3.
	 *
	 * @return the array of {@code Field} objects representing all the declared fields of this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared fields
	 *                within this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Field[] getDeclaredFields() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns an array of {@code Method} objects reflecting all the methods declared by the class or
	 * interface represented by this {@code Class} object. This includes public, protected, default
	 * (package) access, and private methods, but excludes inherited methods. The elements in the array
	 * returned are not sorted and are not in any particular order. This method returns an array of
	 * length 0 if the class or interface declares no methods, or if this {@code Class} object
	 * represents a primitive type, an array class, or void. The class initialization method
	 * {@code <clinit>} is not included in the returned array. If the class declares multiple public
	 * member methods with the same parameter types, they are all included in the returned array.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, section 8.2.
	 *
	 * @return the array of {@code Method} objects representing all the declared methods of this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared methods
	 *                within this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Method[] getDeclaredMethods() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns an array of {@code Constructor} objects reflecting all the constructors declared by the
	 * class represented by this {@code Class} object. These are public, protected, default (package)
	 * access, and private constructors. The elements in the array returned are not sorted and are not
	 * in any particular order. If the class has a default constructor, it is included in the returned
	 * array. This method returns an array of length 0 if this {@code Class} object represents an
	 * interface, a primitive type, an array class, or void.
	 *
	 * <p>
	 * See <em>The Java Language Specification</em>, section 8.2.
	 *
	 * @return the array of {@code Constructor} objects representing all the declared constructors of
	 *         this class
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared
	 *                constructors within this class
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Constructor[] getDeclaredConstructors() throws SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Field} object that reflects the specified declared field of the class or
	 * interface represented by this {@code Class} object. The {@code name} parameter is a
	 * {@code String} that specifies the simple name of the desired field. Note that this method will
	 * not reflect the {@code length} field of an array class.
	 *
	 * @param name the name of the field
	 * @return the {@code Field} object for the specified field in this class
	 * @exception NoSuchFieldException if a field with the specified name is not found.
	 * @exception NullPointerException if {@code name} is {@code null}
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared field
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Method} object that reflects the specified declared method of the class or
	 * interface represented by this {@code Class} object. The {@code name} parameter is a
	 * {@code String} that specifies the simple name of the desired method, and the
	 * {@code parameterTypes} parameter is an array of {@code Class} objects that identify the method's
	 * formal parameter types, in declared order. If more than one method with the same parameter types
	 * is declared in a class, and one of these methods has a return type that is more specific than any
	 * of the others, that method is returned; otherwise one of the methods is chosen arbitrarily. If
	 * the name is "&lt;init&gt;"or "&lt;clinit&gt;" a {@code NoSuchMethodException} is raised.
	 *
	 * @param name the name of the method
	 * @param parameterTypes the parameter array
	 * @return the {@code Method} object for the method of this class matching the specified name and
	 *         parameters
	 * @exception NoSuchMethodException if a matching method is not found.
	 * @exception NullPointerException if {@code name} is {@code null}
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared method
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Method getDeclaredMethod(String name, Class... parameterTypes) throws NoSuchMethodException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Returns a {@code Constructor} object that reflects the specified constructor of the class or
	 * interface represented by this {@code Class} object. The {@code parameterTypes} parameter is an
	 * array of {@code Class} objects that identify the constructor's formal parameter types, in
	 * declared order.
	 *
	 * If this {@code Class} object represents an inner class declared in a non-static context, the
	 * formal parameter types include the explicit enclosing instance as the first parameter.
	 *
	 * @param parameterTypes the parameter array
	 * @return The {@code Constructor} object for the constructor with the specified parameter list
	 * @exception NoSuchMethodException if a matching method is not found.
	 * @exception SecurityException If a security manager, <i>s</i>, is present and any of the following
	 *                conditions is met:
	 *
	 *                <ul>
	 *
	 *                <li>invocation of {@link SecurityManager#checkMemberAccess
	 *                s.checkMemberAccess(this, Member.DECLARED)} denies access to the declared
	 *                constructor
	 *
	 *                <li>the caller's class loader is not the same as or an ancestor of the class
	 *                loader for the current class and invocation of
	 *                {@link SecurityManager#checkPackageAccess s.checkPackageAccess()} denies access to
	 *                the package of this class
	 *
	 *                </ul>
	 *
	 * @since JDK1.1
	 */
	public Constructor getDeclaredConstructor(Class... parameterTypes) throws NoSuchMethodException, SecurityException {
		_throw();
		return null;
	}

	/**
	 * Finds a resource with a given name. The rules for searching resources associated with a given
	 * class are implemented by the defining {@linkplain ClassLoader class loader} of the class. This
	 * method delegates to this object's class loader. If this object was loaded by the bootstrap class
	 * loader, the method delegates to {@link ClassLoader#getSystemResourceAsStream}.
	 *
	 * <p>
	 * Before delegation, an absolute resource name is constructed from the given resource name using
	 * this algorithm:
	 *
	 * <ul>
	 *
	 * <li>If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>), then the absolute name
	 * of the resource is the portion of the {@code name} following the {@code '/'}.
	 *
	 * <li>Otherwise, the absolute name is of the following form:
	 *
	 * <blockquote> {@code modified_package_name/name} </blockquote>
	 *
	 * <p>
	 * Where the {@code modified_package_name} is the package name of this object with {@code '/'}
	 * substituted for {@code '.'} (<tt>'&#92;u002e'</tt>).
	 *
	 * </ul>
	 *
	 * @param name name of the desired resource
	 * @return A {@link java.io.InputStream} object or {@code null} if no resource with this name is
	 *         found
	 * @throws NullPointerException If {@code name} is {@code null}
	 * @since JDK1.1
	 */
	public InputStream getResourceAsStream(String name) {
		_throw();
		return null;
	}

	/**
	 * Finds a resource with a given name. The rules for searching resources associated with a given
	 * class are implemented by the defining {@linkplain ClassLoader class loader} of the class. This
	 * method delegates to this object's class loader. If this object was loaded by the bootstrap class
	 * loader, the method delegates to {@link ClassLoader#getSystemResource}.
	 *
	 * <p>
	 * Before delegation, an absolute resource name is constructed from the given resource name using
	 * this algorithm:
	 *
	 * <ul>
	 *
	 * <li>If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>), then the absolute name
	 * of the resource is the portion of the {@code name} following the {@code '/'}.
	 *
	 * <li>Otherwise, the absolute name is of the following form:
	 *
	 * <blockquote> {@code modified_package_name/name} </blockquote>
	 *
	 * <p>
	 * Where the {@code modified_package_name} is the package name of this object with {@code '/'}
	 * substituted for {@code '.'} (<tt>'&#92;u002e'</tt>).
	 *
	 * </ul>
	 *
	 * @param name name of the desired resource
	 * @return A {@link java.net.URL} object or {@code null} if no resource with this name is found
	 * @since JDK1.1
	 */
	public java.net.URL getResource(String name) {
		_throw();
		return null;
	}

	private static void _throw() {
		throw new RuntimeException("not supported!");
	}
}