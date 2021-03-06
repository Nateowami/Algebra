/*
    Solve4x - An algebra solver that shows its work
    Copyright (C) 2015  Nathaniel Paulus

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
package com.github.nateowami.solve4x.algorithm;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.nateowami.solve4x.config.RoundingRule;
import com.github.nateowami.solve4x.solver.*;
import com.github.nateowami.solve4x.solver.Number;

/**
 * @author Nateowami
 */
public class CombineLikeTerms extends Algorithm {
	
	private final RoundingRule round;
	
	/**
	 * Constructs a new CombineLikeTerms algorithm.
	 * @param r The rounding rule for arithmetic operations performed when combining terms.
	 */
	public CombineLikeTerms(RoundingRule r){
		super(Expression.class); // declare that this Algorithm works on expressions
		this.round = r;
	}
	
	@Override
	public Step execute(Algebra algebra) {
		//the expression to simplify
		Expression expr = (Expression) algebra;
		
		//a list of lists of like terms to combine
		ArrayList<ArrayList<AlgebraicParticle>> likeTerms = listCombinableTerms(expr);
		ArrayList<AlgebraicParticle> combined = combineLikeTerms(likeTerms);
		AlgebraicParticle out = constructExpression(expr.sign(), removeZeros(combined), expr.exponent());
		
		//construct the solving step
		Step step = new Step(out);
		
		//create the explanation
		step.explain("We need to combine like terms here, in the expression ").explain(expr).explain(".\n");
		for(int i = 0; i < likeTerms.size(); i++){
			if (likeTerms.get(i).size() > 1) //don't explain combining a single term with itself
					step.explain("Combine ").list(likeTerms.get(i)).explain(" to get ").explain(combined.get(i)).explain(".\n");
		}
		return step;
	}
	

	@Override
	public int smarts(Algebra algebra) {
		Expression expression = (Expression) algebra;
		int combinable = expression.length() - listCombinableTerms(expression).size();
		if(combinable == 0) return 0;
		else if(combinable == 1) return 7;
		else return 9;
	}
	
	/**
	 * Combines terms in a 2D (ArrayList of ArrayList) array, constructs and returns a new Expression.
	 * @param sign The sign of the Expression to create (should be the same as the sign of the 
	 * expression the terms came from).
	 * @param terms The terms (i.e. AlgebraicParticles) to combine. Since it's an ArrayList of an 
	 * ArrayList, the the terms in each of the inner ArrayLists will be combined to be a single term.
	 * @param exponent The exponent of the Expression to create (should be the same as the exponent of 
	 * the expression the terms came from).
	 * @return An expression with terms combined.
	 */
	protected  ArrayList<AlgebraicParticle> combineLikeTerms(ArrayList<ArrayList<AlgebraicParticle>> terms){
		ArrayList<AlgebraicParticle> combined = new ArrayList<AlgebraicParticle>(terms.size());
		for(ArrayList<AlgebraicParticle> a : terms){
			//combine everything into one
			AlgebraicParticle term = a.get(0);
			//loop over all remaining terms (all but first)
			for(int i = 1; i < a.size(); i++){
				term = combineTerms(term, a.get(i));
			}
			combined.add(term);
		}
		return combined;
	}
	
	/**
	 * Combines the given AlgebraicParticles a and b.
	 * @param a The first AlgebraicParticle.
	 * @param b The second AlgebraicParticle.
	 * @return a and b combined (added/subtracted, depending on signs).
	 * @throws IllegalArgumentException if a and b cannot be combined.
	 */
	protected AlgebraicParticle combineTerms(AlgebraicParticle a, AlgebraicParticle b){
		//if one is zero, return the other
		if(a.equals(Number.ZERO))return b; else if(b.equals(Number.ZERO))return a;
		
		//if they're numbers, mixed numbers, or fractions with numbers on top and bottom
		if(areCombinable(a,b)) return addConstants(a,b);
		
		//if they're identical variables
		else if(a instanceof Variable && b instanceof Variable){
			if(a.sign() != b.sign()) return Number.ZERO;
			//combine a and b to make a term with coefficient of 2 and sign the same as a and b 
			else return new Term(
					a.sign(), 
					new ArrayList<AlgebraicParticle>(Arrays.asList(new AlgebraicParticle[]{new Number(true, "2", null, null, 1), ((Variable)a).cloneWithNewSign(true)})),
					a.exponent());
		}
		
		//if they're terms, and they're like
		else if(a instanceof Term && b instanceof Term){
			//cast them to terms
			Term first = (Term) a, second = (Term) b;
			//build the new term
			ArrayList<AlgebraicParticle> termBuilder = new ArrayList<AlgebraicParticle>(first.length());
			
			//work with the coefficient
			AlgebraicParticle coeA = first.coefficient(), coeB = second.coefficient();
			if(!a.sign()) coeA = coeA.cloneWithNewSign(false);
			if(!b.sign()) coeB = coeB.cloneWithNewSign(false);
			AlgebraicParticle coe = addConstants(coeA, coeB);
			if(coe.equals(Number.ZERO)) return Number.ZERO;
			boolean sign = coe.sign();
			//the coefficient should always be positive, though the term may be negative 
			//e.g. -2x is a negative term, so the term, not the 2, should be negative
			if(!sign) coe = coe.cloneWithNewSign(true);
			if(coe.equals(Number.ONE)){
				//if the terms have just a coe and one other thing, return that other thing
				//for example, 2x and -x should just return x
				if(first.length() <= 2 && second.length() <= 2){
					AlgebraicParticle termWithoutCoefficient = first.hasCoefficient() ? first.get(1) : first.get(0);
					return termWithoutCoefficient.sign() == sign ? termWithoutCoefficient : termWithoutCoefficient.cloneWithNewSign(sign);
				}
			}
			else termBuilder.add(coe);
				
			//now add all the remaining parts that are the same
			//account for the possibility of one term having and exponent and the other not
			//find the longest term (if they're the same length, it doesn't matter which we get)
			Term t = first.length() > second.length() ? first : second;
			for(int i =  Util.constant(t.get(0)) ? 1 : 0; i < t.length(); i++){
				termBuilder.add(t.get(i));
			}
			return new Term(sign, termBuilder, first.exponent());
		}
		
		//take care of Expressions and roots
		else if(a instanceof Expression && b instanceof Expression || a instanceof Root && b instanceof Root){
			if(a.sign() != b.sign()) return Number.ZERO;
			else {
				//the exponent will be 2 (sign is independent of the exponent) so we need to make a term
				ArrayList<AlgebraicParticle> list = new ArrayList<AlgebraicParticle>(2);
				list.add(new Number("2"));
				list.add(a);
				return new Term(a.sign(), list, a.exponent());
			}
		}
		//take care terms with half-terms (e.g. x and 2x)
		if(a instanceof Term || b instanceof Term){
			//let t = the term and ap = the other one
			Term t = (Term) (a instanceof Term ? a : b);
			AlgebraicParticle ap = a instanceof Term ? b : a;
			
			//calculate the new coefficient
			AlgebraicParticle coe = addConstants(
					t.sign() ? t.coefficient() : t.coefficient().cloneWithNewSign(false),
					ap.sign() ? Number.ONE : Number.NEGATIVE_ONE
							);
			boolean sign = coe.sign();
						
			//if the coefficients remain
			if (!coe.equals(Number.ONE) && !coe.equals(Number.NEGATIVE_ONE)){
				//build the new term
				ArrayList<AlgebraicParticle> term = new ArrayList<AlgebraicParticle>();
				//add or subtract one to get the new coefficient
				term.add(coe.sign() ? coe : coe.cloneWithNewSign(true));
				term.add(ap.sign() ? ap : ap.cloneWithNewSign(true));
				return new Term(sign, term, 1);
			}
			//coefficients cancel
			else return ap.sign() == sign ? ap : ap.cloneWithNewSign(sign);
		}
		
		else throw new IllegalArgumentException();
	}
	
	/**
	 * Adds a and b, and returns the result. a and b must be instances of Number, 
	 * Fraction, or MixedNumber. In addition, fractions (or fractions in a MixedNumber)
	 * must have the same denominators, and have numbers in the numerator. There are a few
	 * more qualifications; basically the two should be combinable without rewriting 
	 * numerators or modifying multiple parts of a MixedNumber.
	 * @param a The first number to add. If null, it defaults to one.
	 * @param b The second number to add. If null, it defaults to one.
	 * @return a and b combined.
	 */
	protected AlgebraicParticle addConstants(AlgebraicParticle a, AlgebraicParticle b){
		if (a == null) a = Number.ONE; if (b == null) b = Number.ONE;
		if(a instanceof Number && b instanceof Number) return Number.add((Number)a, (Number)b, this.round);
		if(a instanceof Fraction && b instanceof Fraction){
			Fraction added = Fraction.add((Fraction)a, (Fraction)b, this.round);
			if(added.getTop().equals(Number.ZERO))return Number.ZERO;
			if(added.getTop().equals(added.getBottom()))return Number.ONE;
			else return added;
		}
		if(a instanceof MixedNumber && b instanceof MixedNumber) return MixedNumber.add((MixedNumber)a, (MixedNumber)b, this.round);
		
		//swap some values (e.g. turn Fraction-Number into Number-Fraction) so they can be used mor easily later
		AlgebraicParticle temp;
		if(a instanceof Fraction && b instanceof Number || a instanceof MixedNumber && b instanceof Number 
				|| a instanceof MixedNumber && b instanceof Fraction){temp = a; a = b; b = temp;}
		
		if(a instanceof Number && b instanceof Fraction) {
			Number num = (Number)a; Fraction frac = (Fraction)b;
			if(!num.sign()) num = num.cloneWithNewSign(true);
			if(!frac.sign()) frac = frac.cloneWithNewSign(true);
			return new MixedNumber(a.sign(), num, frac, 1);
		}
		if(a instanceof Number && b instanceof MixedNumber){
			Number added = Number.add((Number)a, ((MixedNumber)b).getNumeral(), this.round);
			boolean sign = added.sign();
			added = added.cloneWithNewSign(true);
			return new MixedNumber(sign, added, ((MixedNumber)b).getFraction(), 1);
		}
		if(a instanceof Fraction && b instanceof MixedNumber){
			Fraction frac = (Fraction)a; MixedNumber mn = (MixedNumber)b;
			Fraction added = Fraction.add(frac, mn.sign() ? mn.getFraction() : mn.getFraction().cloneWithNewSign(false), this.round);
			if(!added.sign()) added = added.cloneWithNewSign(true);
			
			if(added.getTop().equals(Number.ZERO)) return mn.getNumeral();
			else return new MixedNumber(mn.sign(), mn.getNumeral(), added, 1);
		}
		else throw new IllegalArgumentException();
	}
	
	/**
	 * Combines like terms* into ArrayLists.
	 * Given an expression like this:<br>
	 * 2x+4+((x)/(3))+x<br>
	 * Terms (or AlgebraicParticles) are added to ArrayLists to form a 2d array:<br>
	 * 2x&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;x<br>
	 * 4&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;6<br>
	 * ((x)/(3))<br>
	 * Now it can easily be seen that there are three types of terms. The first two 
	 * lines can then be combined to make a simpler expression:<br>
	 * 3x+10+((x)/(3))<br>
	 * *NOTE: If terms have fractional coefficients with different denominators, the terms
	 * will not be considered like terms. Hence the name "combinableTerms".
	 * @return A 2d ArrayList (ArrayList of ArrayList) containing like terms. Each row
	 * contains terms that are alike.
	 */
	protected ArrayList<ArrayList<AlgebraicParticle>> listCombinableTerms(Expression e){
		//this will be the list we return in the end
		ArrayList<ArrayList<AlgebraicParticle>> list = new ArrayList<ArrayList<AlgebraicParticle>>(e.length());
		//loop through the terms
		bigloop:
		for(int i = 0; i < e.length(); i++){
			//place this term in the correct ArrayList
			for(ArrayList<AlgebraicParticle> array : list){
				if(areCombinableTerms(array.get(0), e.get(i))){
					array.add(e.get(i));
					continue bigloop;
				}
			}
			//there is no like term, we must add it to a new arraylist
			ArrayList<AlgebraicParticle> tmp = new ArrayList<AlgebraicParticle>(1);
			tmp.add(e.get(i));
			list.add(tmp);
		}
		return list;
	}
	
	/**
	 * Tells if a and b are like terms (i.e., they are both numbers, mixed numbers, or fractions with
	 * numbers on top and bottom, or they are the same variables, or terms with like numerical 
	 * coefficients (if coefficients are fractions with different denominators, they are not considered
	 * like terms)).
	 * @param a The first algebraic particle.
	 * @param b The second algebraic particle.
	 * @return If a and b are like terms.
	 */
	protected boolean areCombinableTerms(AlgebraicParticle a, AlgebraicParticle b){
		if(a.exponent() == b.exponent() && a.almostEquals(b)) {
			return true;
		}
		else if(a.exponent() != 1 || b.exponent() != 1)return false;
		//if they're numbers, mixed numbers, or fractions with numbers on top and bottom
		else if(areCombinable(a, b))return true;
		//if they're identical variables
		else if(a instanceof Variable && b instanceof Variable) return ((Variable)a).getVar() == (((Variable)b).getVar());
		//if they're terms, and they're like
		else if(a instanceof Term && b instanceof Term){
			Term first = (Term) a, second = (Term) b;
			if(a.equals(b) || a.sign() != b.sign() && a.cloneWithNewSign(!a.sign()).equals(b))return true;
			if(!areCombinable(first.coefficient(), second.coefficient()))return false;
			//check if the terms are combinable
			if(Math.abs(first.length() - second.length()) > 1)return false;
			
			
			//if the they both have coefficients (or both don't) then their length must be the same
			if(first.hasCoefficient() == second.hasCoefficient() && first.length() != second.length())return false;
			
			//check that the terms are like. offset if necessary, for checking things like xy and 2xy
			for(int i = first.hasCoefficient() ? 1 : 0, j = second.hasCoefficient() ? 1 : 0; i < first.length() && j < second.length(); i++, j++){
				if(!first.get(i).equals(second.get(j)))return false;
			}
			return true;
		}
		else if(a instanceof Expression && b instanceof Expression || a instanceof Root && b instanceof Root) return a.equals(b);
		//account for situations like 2x and x -one is a variable or such, the other a term
		boolean aTerm = a instanceof Term, bTerm = b instanceof Term;
		if(aTerm != bTerm){
			Term term = (Term) (aTerm ? a : b);
			AlgebraicParticle nonterm = aTerm ? b : a;
			if(term.length() == 2 && term.hasCoefficient() && term.get(1).cloneWithNewSign(nonterm.sign()).equals(nonterm)) return true;
			else return false;
		}
		else return false;
	}
	
	/**
	 * Tells if a and b are combinable, i.e., at least one is a plain number, or if both have 
	 * fractions, check that the denominators are equal.
	 * @param a The first term to check.
	 * @param b The second term to check.
	 * @return If a and b can be combined without multiplying by the LCD, etc.
	 */
	protected boolean areCombinable(AlgebraicParticle a, AlgebraicParticle b){
		if(a == null) a = Number.ONE; if (b == null) b = Number.ONE;
		else if(!(Util.constant(a) && Util.constant(b)))return false;
		else if(a instanceof Number && b instanceof Number)return true;
		else if(a instanceof Fraction && b instanceof Fraction){
			Fraction fa = (Fraction)a, fb = (Fraction)b;
			return fa.getTop() instanceof Number && fb.getTop() instanceof Number && fa.like(fb);
		}
		else if(a instanceof MixedNumber && b instanceof MixedNumber){
			MixedNumber mna = (MixedNumber)a, mnb = (MixedNumber)b;
			//the dominant sign is the sign of the mixed number farthest from zero
			boolean dominantSign = Number.add(mna.getNumeral().cloneWithNewSign(mna.sign()), 
					mnb.getNumeral().cloneWithNewSign(mnb.sign()), 
					this.round).sign();
			return mna.getFraction().like(mnb.getFraction()) 
					//they have the same sign OR when combined the tops will stay positive
					&& (	mna.sign() == mnb.sign()
							|| Number.add((Number)mna.getFraction().getTop().cloneWithNewSign(mna.sign()), 
									(Number) mnb.getFraction().getTop().cloneWithNewSign(mnb.sign()),
									this.round).sign() == dominantSign
							);
		}
		
		//swap some values (e.g. turn Fraction-Number into Number-Fraction) so they can be used more easily later
		AlgebraicParticle temp;
		if(a instanceof Fraction && b instanceof Number || a instanceof MixedNumber && b instanceof Number
				|| a instanceof MixedNumber && b instanceof Fraction){temp = a; a = b; b = temp;}
		
		if(a instanceof Number && b instanceof Fraction && a.sign() == b.sign())return true;
		else if(a instanceof Number && b instanceof MixedNumber){
			Number n = ((MixedNumber)b).getNumeral();
			//if we add the number and mixed number will the sign change? if so, we shouldn't combine, because it's difficult
			return Number.add((Number)a, n.cloneWithNewSign(b.sign()), this.round).sign() == b.sign();
		}
		else if(a instanceof Fraction && b instanceof MixedNumber){
			Fraction f = (Fraction)a;
			MixedNumber mn = (MixedNumber)b;
			return f.like(mn.getFraction()) && Fraction.add(f, mn.getFraction().cloneWithNewSign(mn.sign()), this.round).sign() == mn.sign();
		}
		return false;
	}
	
	private AlgebraicParticle constructExpression(boolean sign, ArrayList<AlgebraicParticle> list, int exponent) {
		if(list.size() == 0) {
			//if the exponent isn't one then it needs to be carried over to the number
			if(exponent != 1) return Number.ZERO.cloneWithNewSignAndExponent(sign, exponent);
			return Number.ZERO;
		}
		else if(list.size() == 1) {
			AlgebraicParticle a = list.get(0);
			boolean resultingSign = sign == a.sign();
			//if sign and exponent are right already
			if(exponent == 1 && a.sign() == resultingSign) return a;
			else if((exponent == 1 || a.exponent() == 1)) {
				// Set the sign, and exponent. At least one exponent is 1, so multiplying gives us 
				// the one that isn't.
				return a.cloneWithNewSignAndExponent(resultingSign, exponent * a.exponent());
			}
			// Neither exponent is 1. 
			// Return a single-element term. Maybe a bad idea; not sure what to do.
			else {
				return new Term(resultingSign, list, exponent);
			}
		}
		else return new Expression(sign, list, exponent);
	}
	
	/**
	 * Removes zeros from an ArrayList of AlgebraicParticles. 
	 * @param combined The expression from which to remove zeros.
	 * @return expr with zeros removed.
	 */
	private ArrayList<AlgebraicParticle> removeZeros(ArrayList<AlgebraicParticle> list) {
		ArrayList<AlgebraicParticle> removed = new ArrayList<AlgebraicParticle>(list.size());
		for(AlgebraicParticle a : list) {
			if(!a.equals(Number.ZERO)) removed.add(a);
		}
		return removed;
	}
	
}
