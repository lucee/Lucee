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
package lucee.commons.lang.types;

/**
 * Integer Type that can be modified
 */
public class RefIntegerImpl implements RefInteger {

    private int value;

    /**
     * @param value
     */
    public RefIntegerImpl(int value) {
        this.value=value;
    }
    public RefIntegerImpl() {
    }
    
    /**
     * @param value
     */
    @Override
	public synchronized void setValue(int value) {
        this.value = value;
    }
    
    /**
     * operation plus
     * @param value
     */
    @Override
	public synchronized void plus(int value) {
        this.value+=value;
    }
    
    /**
     * operation minus
     * @param value
     */
    @Override
	public synchronized void minus(int value) {
        this.value-=value;
    }

    /**
     * @return returns value as integer
     */
    @Override
	public synchronized Integer toInteger() {
        return Integer.valueOf(value);
    }
    /**
     * @return returns value as integer
     */
    @Override
	public synchronized Double toDouble() {
        return new Double(value);
    }
    

	@Override
	public synchronized double toDoubleValue() {
		return value;
	}
	
	@Override
	public synchronized int toInt() {
		return value;
	}
    
    
    @Override
    public synchronized String toString() {
        return String.valueOf(value);
    }
}