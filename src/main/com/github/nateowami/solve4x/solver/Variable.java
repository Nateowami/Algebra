/*
    Solve4x - An algebra solver that shows its work
    Copyright (C) 2014 Solve4x project

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.nateowami.solve4x.solver;

import java.nio.charset.MalformedInputException;

/**
 * Represents a variable, A-Z or a-z
 * @author Nateowami
 */
public class Variable extends AlgebraicParticle{
	private char var;
	
	/**
	 * Constructs a new Variable.
	 * @param s The string to parse.
	 * @throws If s is null, doesn't contain exactly one char, or the one char is not A-Z or a-z.
	 */
	protected Variable(String s) throws MalformedInputException{
		if(s == null || s.length() != 1 || !Util.isLetter(s.charAt(0))){
			throw new MalformedInputException(0);
		}
		else var = s.charAt(0);
	}
	
	public char getVar(){
		return var;
	}

	/* (non-Javadoc)
	 * @see com.github.nateowami.solve4x.solver.AlgebraicParticle#getAsString()
	 */
	@Override
	public String getAsString() {
		return (sign()? "" : "-") + var + (exponent() == 0? "" : exponent());
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Variable [var=" + var + ", sign()=" + sign() + ", exponent()="
				+ exponent() + "]";
	}

	
	/**
	 * Tells if 
	 * @param s
	 * @return
	 */
	public static boolean parseable(String s) {
		if(s == null || s.length() != 1 || !Util.isLetter(s.charAt(0))){
			return false;
		}
		else return true;
	}
	
}