package lucee.runtime.util;

import java.util.HashSet;

// LDEV-3333 / LDEV-3731
// this is just a partial implementation of HashSet<Object>, using System.identityHashCode
// instead of the default virtually dispatched <object-impl>.hashCode; this avoids the problem
// of "hashing arrays which contain themselves causing a stackoverflow" 

public class ObjectIdentityHashSet {
    private HashSet<Integer> elements = new HashSet<Integer>();

    public boolean contains(Object object) {
        return elements.contains(System.identityHashCode(object));
    }
    public boolean add(Object object) {
        return elements.add(System.identityHashCode(object));
    }
    public boolean remove(Object object) {
        return elements.remove(System.identityHashCode(object));
    }
}