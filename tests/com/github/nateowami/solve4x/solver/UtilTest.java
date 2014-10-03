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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * @author Nateowami
 */
public class UtilTest {

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isEq(java.lang.String)}.
	 */
	@Test
	public void testIsEq() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isNumeral(char)}.
	 */
	@Test
	public void testIsNumeral() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isInteger(java.lang.String)}.
	 */
	@Test
	public void testIsInteger() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#removePar(java.lang.String)}.
	 */
	@Test
	public void testRemovePar() {
		assertEquals(Util.removePar("4"), "4");
		assertEquals(Util.removePar("xy+6z)"), "xy+6z)");
		assertEquals(Util.removePar("(asdfa + z 4"), "(asdfa + z 4");
		assertEquals(Util.removePar("(4)"), "4");
		assertEquals(Util.removePar("(xy+6/3)"), "xy+6/3");
		assertEquals(Util.removePar("((asdfa)asdkjflaskdfj)"), "(asdfa)asdkjflaskdfj");
		assertEquals(Util.removePar("(z6+4)"), "z6+4");
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isOpenPar(char)}.
	 */
	@Test
	public void testIsOpenPar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isClosePar(char)}.
	 */
	@Test
	public void testIsClosePar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isLetter(char)}.
	 */
	@Test
	public void testIsLetterChar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isLetter(java.lang.String)}.
	 */
	@Test
	public void testIsLetterString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isFraction(java.lang.String)}.
	 */
	@Test
	public void testIsFraction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isFracSide(java.lang.String)}.
	 */
	@Test
	public void testIsFracSide() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isFullySimplified(java.lang.String)}.
	 */
	@Test
	public void testIsFullySimplified() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#getGCF(int, int)}.
	 */
	@Test
	public void testGetGCF() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#getCommonFactors(int, int)}.
	 */
	@Test
	public void testGetCommonFactors() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#factors(int)}.
	 */
	@Test
	public void testFactors() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isSuperscript(char)}.
	 */
	@Test
	public void testIsSuperscriptChar() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isSuperscript(java.lang.String)}.
	 */
	@Test
	public void testIsSuperscriptString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#superscriptToInt(java.lang.String)}.
	 */
	@Test
	public void testSuperscriptToInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#intToSuperscript(int)}.
	 */
	@Test
	public void testIntToSuperscript() {
		assertEquals(Util.intToSuperscript(6), "⁶");
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#isSubscript(char)}.
	 */
	@Test
	public void testIsSubscript() {
		assertTrue(Util.isSubscript('₀'));
		assertTrue(Util.isSubscript('₄'));
		assertTrue(Util.isSubscript('₉'));
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#subscriptToInt(java.lang.String)}.
	 */
	@Test
	public void testSubscriptToInt() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link com.github.nateowami.solve4x.solver.Util#intToSubscript(int)}.
	 */
	@Test
	public void testIntToSubscript() {
		assertEquals("₄₄", Util.intToSubscript(44));//TODO change "something"
	}
	
}
