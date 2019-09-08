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
package lucee.runtime.op;

import java.util.Date;

import lucee.runtime.exp.PageException;
import lucee.runtime.util.Operation;

/**
 * oimplementation of interface Operation
 */
public final class OperationImpl implements Operation {

	private static OperationImpl singelton;

	@Override
	public int compare(boolean left, boolean right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(boolean left, Date right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(boolean left, double right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(boolean left, Object right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(boolean left, String right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Date left, boolean right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Date left, Date right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Date left, double right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Date left, Object right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Date left, String right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(double left, boolean right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(double left, Date right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(double left, double right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(double left, Object right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(double left, String right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Object left, boolean right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Object left, Date right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Object left, double right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Object left, Object right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(Object left, String right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(String left, boolean right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(String left, Date right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(String left, double right) {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(String left, Object right) throws PageException {
		return Operator.compare(left, right);
	}

	@Override
	public int compare(String left, String right) {
		return Operator.compare(left, right);
	}

	@Override
	public String concat(String left, String right) {
		return left.concat(right);
	}

	@Override
	public boolean ct(Object left, Object right) throws PageException {
		return Operator.ct(left, right);
	}

	@Override
	public double divide(double left, double right) {
		return Operator.divide(left, right);
	}

	@Override
	public boolean equals(Object left, Object right, boolean caseSensitive) throws PageException {
		return Operator.equals(left, right, caseSensitive);
	}

	@Override
	public boolean eqv(Object left, Object right) throws PageException {
		return Operator.eqv(left, right);
	}

	@Override
	public double exponent(Object left, Object right) throws PageException {
		return Operator.exponent(left, right);
	}

	@Override
	public boolean imp(Object left, Object right) throws PageException {
		return Operator.imp(left, right);
	}

	@Override
	public double minus(double left, double right) {
		return Operator.minus(left, right);
	}

	@Override
	public double modulus(double left, double right) {
		return Operator.modulus(left, right);
	}

	@Override
	public double multiply(double left, double right) {
		return Operator.multiply(left, right);
	}

	@Override
	public boolean nct(Object left, Object right) throws PageException {
		return Operator.nct(left, right);
	}

	@Override
	public double plus(double left, double right) {
		return Operator.plus(left, right);
	}

	public static Operation getInstance() {
		if (singelton == null) singelton = new OperationImpl();
		return singelton;
	}

	@Override
	public boolean equalsComplexEL(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) {
		return Operator.equalsComplexEL(left, right, caseSensitive, checkOnlyPublicAppearance);
	}

	@Override
	public boolean equalsComplex(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) throws PageException {
		return Operator.equalsComplex(left, right, caseSensitive, checkOnlyPublicAppearance);
	}

}