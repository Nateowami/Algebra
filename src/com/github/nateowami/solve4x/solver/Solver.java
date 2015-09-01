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
package com.github.nateowami.solve4x.solver;

import java.util.ArrayList;

import com.github.nateowami.solve4x.Solve4x;
import com.github.nateowami.solve4x.algorithm.*;
import com.github.nateowami.solve4x.config.RoundingRule;

/**
 * Solves equations and simplifies expressions
 * @author Nateowami
 */

public class Solver {
	
	/**
	 * Represents the possible different ways to solve. For example, maybe the user wants
	 * the expression simplified, or maybe they want it put in a certain form. Or maybe they
	 * only need to factor the expression. The user's selection is represented by this enum.
	 * @author Nateowami
	 */
	public enum SolveFor {
		SOLVE, SIMPLIFY, FACTOR
	}
	
	//the final solution
	private Solution finalSolution;
	//A list of algorithms that can be used for solving
	private final ArrayList <Algorithm> algorithms;
	
	/**
	 * Creates a new Solver so you can call getSolution().
	 * @param input The equation or expression to solve, simplify, 
	 * factor, multiply, etc.
	 * @param solveFor The user's selection regarding what to solve for. Do they want this to be 
	 * factored, solved, simplified, or what? See {@link Solver.SolveFor}.
	 * @param round A RoundingRule for rounding arithmetic operations.
	 * @throws IllegalArgumentException If solveFor is SOLVE but input was not an equation, or 
	 * input was an equation, but solveFor was not SOLVE.
	 * @throws ParsingException If the input cannot be parsed as algebra.
	 */
	public Solver(String input, SolveFor solveFor, RoundingRule round) throws IllegalArgumentException, ParsingException {
		
		//remove spaces TODO move this to the GUI level; this shouldn't be the solver's concern
		input = input.replaceAll(" ", "");
		//remove any commas that may be in numbers
		input = input.replaceAll(",", "");
		
		Algebra parsedInput = null;
		//if input doesn't have an equals sign
		if(input.indexOf('=') == -1) {
			if(AlgebraicParticle.parsable(input)) {
				parsedInput = AlgebraicParticle.getInstance(input);
				//if solving was requested, but it wasn't an equation
				if(solveFor == SolveFor.SOLVE) throw new IllegalArgumentException("Solving requested but input was not an equation.");
			}
			else throw new ParsingException("Invalid input: " + input);
		}
		else {
			parsedInput = new Equation(input);
			if(solveFor != SolveFor.SOLVE) throw new IllegalArgumentException("Input was requested, but something other than solving was requested.");
		}
		
		this.algorithms = getAlgorithms(solveFor, round);
		
		//create a list of solutions
		ArrayList<Solution> currentSolutions = new ArrayList<Solution>();
		currentSolutions.add(new Solution(parsedInput));
		
		// Loop until one of the following conditions is met:
		// - No solutions have survived
		// - A complete solution has been found
		// - We've looped 25 times already
		for(int i = 0; currentSolutions.size() > 0 && findSolution(currentSolutions, solveFor) == null && i < 25; i++){
			//take out/copy all the solutions and remove them from the list
			ArrayList<Solution> previousSolutions = new ArrayList<Solution>(currentSolutions);
			currentSolutions = new ArrayList<Solution>();
			
			//loop through the solutions
			for(Solution solution : previousSolutions){
				//now loop through the algorithms
				currentSolutions.addAll(dispatchAlgorithms(solution, solveFor, round));
			}
		}
				
		//set finalSolution
		finalSolution = findSolution(currentSolutions, solveFor);
	}
	
	/**
	 * @return A Solution object that contains all the steps for solving
	 */
	public Solution getSolution(){
		return finalSolution;
	}
	
	/**
	 * Tells if an expression is fully simplified. Examples: 23(2)/(3)
	 * Works best for fractions. If it's complicated it's unreliable and will return false
	 * @param expr The expression to check
	 * @return If the expr is fully simplified
	 */
	private boolean isSimplified(AlgebraicParticle expr){
		if(expr.exponent() != 1)return false;
		else if(expr instanceof Number) return true;
		else if(expr instanceof Variable && expr.sign()) return true;
		else if(expr instanceof Fraction && ((Fraction)expr).isSimplified()) return true;
		else if(expr instanceof MixedNumber && ((MixedNumber)expr).isSimplified()) return true;
		else return false;
	}
	
	/**
	 * Tells if a given expression is first degree. A first degree expression must have exactly two  
	 * terms, be positive, and have no exponent. Of its two terms, one must number, fraction, etc 
	 * (which must be simplified), and the other must be a single variable, which may have a 
	 * coefficient. The variable must not be raised to any power, and its coefficient must be 
	 * fully simplified.
	 * @param expr The expression to check.
	 * @return If expr is a first degree expression (generally will look like 7x+6).
	 */
	protected static boolean isFirstDegreeExpression(Expression expr) {
		if(expr.length() != 2 || !expr.sign() || expr.exponent() != 1) return false;
		
		AlgebraicParticle a = expr.get(0), b = expr.get(1);
		//if one is a variable or variable with exponent, and the other is a number/mixed number/simplified fraction
		if(Util.constant(a) && b instanceof Term && isVariableWithOptionalCoefficient((Term)b)) return true;
		if(Util.constant(b) && a instanceof Term && isVariableWithOptionalCoefficient((Term)a)) return true;
		else return false;
	}
	
	/**
	 * Tells if given algebra is a variable with (optionally) a simplified coefficient. There are 
	 * two conditions under which this method will return true: the input is a variable with no 
	 * exponent, or the input is a term with no exponent, containing a positive variable with no 
	 * exponent, and a positive and simplified number, mixed number, or fraction (which can't have 
	 * an exponent either). Examples: 2x -5.7y
	 * @param algebra The term or variable to check.
	 * @return If algebra is a variable with an optional coefficient.
	 */
	private static boolean isVariableWithOptionalCoefficient(AlgebraicParticle algebra) {
		if(algebra.exponent() != 1)return false;
		//if it's just a simple variable
		if(algebra instanceof Variable) return true;
		
		//or it's just a simple term
		Term t = (Term) algebra;
		if(t.length() != 2) return false;
		AlgebraicParticle a = t.get(0), b = t.get(1);
		//if either has a negative sign or an exponent, return false
		if(!a.sign() || !b.sign() || a.exponent() != 1 && b.exponent() != 1) return false;
		//if one is a variable and the other is a number, MixedNumber, simplified fraction, etc.
		if(Util.constant(a) && b instanceof Variable) return true;
		if(Util.constant(b) && a instanceof Variable) return true;
		else return false;
	}
	
	/**
	 * Tells if a given equation is solved. This means that either the left side is equal to the 
	 * right side (an identity), or both sides are simple variables, numbers, mixed numbers, or 
	 * simplified fractions.
	 * @param eq The equation to check.
	 * @return True if eq is fully solved, otherwise false.
	 */
	private boolean isSolved(Equation eq){
		//identity
		if(eq.left().equals(eq.right()))return true;
		//both sides simplified
		else return isSimplified(eq.left()) && isSimplified(eq.right());
	}
	
	/**
	 * Tells if algebra is in the state specified by solveFor. For example, if solveFor is SOLVE, 
	 * this method will return true if algebra is fully solved.
	 * @param algebra The algebra to check.
	 * @param solveFor What we're doing (solving/simplifying/factoring).
	 * @return If algebra is done being worked on, as specified by solveFor.
	 */
	private boolean isFinished(Algebra algebra, SolveFor solveFor) {
		switch(solveFor) {
		case SOLVE:
			return algebra instanceof Equation && isSolved((Equation) algebra);
		case SIMPLIFY:
			return  isSimplified((AlgebraicParticle)algebra) || (isFirstDegreeExpression((Expression) algebra));
		case FACTOR:
			return algebra instanceof Term;
		default: return false;
		}
	}
	
	/**
	 * Tells which Solution is the best.
	 * @param solutions An ArrayList of Solutions to search.
	 * @param solveFor What we're supposed to solve for (e.g. solve, simplify, factor).
	 * @return The Solution that is solved with fewest steps, or null if no solutions are given, or 
	 * none are completely solved/simplified.
	 */
	private Solution findSolution(ArrayList<Solution> solutions, SolveFor solveFor) {
		//the best-so-far solution
		Solution workingSolution = null;
		boolean firstIteration = true;
		
		//iterate over the solutions and find the shortest one
		for(Solution solution : solutions){
			if(isFinished(solution.getLastAlgebraicExpression(), solveFor) 
					&& (firstIteration || solution.length() < workingSolution.length())){
				workingSolution = solution;
				firstIteration = false;
			}
		}
		return workingSolution;
	}
	
	/**
	 * Returns the applicable solving algorithms for the what is being solved for, e.g.,
	 * algorithms for solving, simplifying, factoring, etc.
	 * @param selection The user's selection regarding what to solve for.
	 * @param round A RoundingRule for rounding arithmetic operations.
	 * @return An ArrayList of algorithms applicable for what's being solved for.
	 */
	private ArrayList<Algorithm> getAlgorithms(SolveFor solveFor, RoundingRule round) {
		ArrayList<Algorithm> algorList = new ArrayList<Algorithm>();
		
		//this switch is deliberately fall-through (not using break)
		switch (solveFor){
		case SOLVE:
			algorList.add(new ChangeSides());
			algorList.add(new DivideBothSides());
			algorList.add(new MultiplyBothSides());
		case FACTOR:
		case SIMPLIFY:
			algorList.add(new CombineLikeTerms(round));
			algorList.add(new Multiply(round));
			algorList.add(new Distribute(round));
			algorList.add(new CancelFactors());
		}
		return algorList;
	}
	
	/**
	 * Dispatches an algorithm to work on equation s. This method will choose the proper algorithms 
	 * to use, and if more than one is applicable, it will
	 * @param e A Solution to work on solving.
	 * @return An array of Solutions that that continue solving s.
	 */
	private ArrayList<Solution> dispatchAlgorithms(Solution s, SolveFor solveFor, RoundingRule round){
		ArrayList<Solution> solutions = new ArrayList<Solution>();
		for(Algorithm a : this.algorithms) solutions.addAll(dispatchAlgorithm(s, a, round));
		return solutions;
	}
	
	/**
	 * Dispatches a given algorithm with a given rounding rule and a partial solution. The solution 
	 * is given so new solutions may be constructed off of it.
	 * @param s The (unfinished) solution that we are extending by adding a step.
	 * @param a The algorithm to use in solving.
	 * @param round The rounding rule to use for arithmetic operations.
	 * @return A list of solutions, each having one more step than the given solution. This list 
	 * may be empty.
	 */
	private ArrayList<Solution> dispatchAlgorithm(Solution s, Algorithm a, RoundingRule round) {
		ArrayList<Solution> list = new ArrayList<Solution>();
		for(Algebra resource : resourcesForAlgorithm(s.getLastAlgebraicExpression(), a)) {
			if(a.smarts(resource) > 3){
				Solution solution = new Solution(s);
				solution.addStep(dispatchAlgorithmWithResource(a, resource, s.getLastAlgebraicExpression()));
				list.add(solution);
			}
		}
		return list;
	}
	
	/**
	 * Dispatches an algorithm with a given resource and top-level expression. In other words, it 
	 * takes an algorithm, a piece of algebra to feed to that algorithm, and the piece of algebra 
	 * that the first piece belongs to. For example, if we wanted to combine like terms in x(2+4), 
	 * algorithm would be an instance of CombineLikeTerms, the resource would be 2+4, and the 
	 * top-level algebra would be x(2+4) (Unless, of course, the x(2+4) was part of another 
	 * expression or equation, in which case it would be that).
	 * @param algorithm The algorithm to dispatch.
	 * @param resource The resource to give to the algorithm.
	 * @param topLevel The top-level ancestor of resource (e.g. an equation that resource is a part 
	 * of). It could even be the resource itself.
	 * @return A new solving step which uses the algorithm and resource.
	 */
	//protected so it can be tested
	protected static Step dispatchAlgorithmWithResource(Algorithm algorithm, Algebra resource, Algebra topLevel){
		Step step = algorithm.execute(resource);
		
		//figure out what algebraic particle was modified, and what it was modified to
		Algebra out = resource, in = step.getChange();
		//the final top-level equation, expression, term, etc.
		Algebra result = null;
		
		//if the top level expression was changed
		if(resource == topLevel) result = step.getChange();
		//it was nested then
		else {
			if(topLevel instanceof Equation) result = ((Equation)topLevel).replace((AlgebraicParticle)out, (AlgebraicParticle)in);
			else if(topLevel instanceof AlgebraicCollection) result = ((AlgebraicCollection)topLevel).replace((AlgebraicParticle)out, (AlgebraicParticle)in);
			else throw new IllegalArgumentException("Unknown something-a-rather error.");
		}
		
		step.setAlgebraicExpression(result);
		
		return step;
	}
	
	/**
	 * Calculates the Algebra to be sent to a given algorithm 
	 * @param e
	 * @param algorithm
	 * @return
	 */
	private ArrayList<? extends Algebra> resourcesForAlgorithm(Algebra algebra, Algorithm algorithm){
		ArrayList<Algebra> list = new ArrayList<Algebra>();
		//if we're working with equations
		if(algebra instanceof Equation && algorithm.ALGORITHM_LEVEL.equals(Equation.class)) {
			list.add(algebra);
			return list;
		}
		
		if(algebra instanceof AlgebraicCollection){
			list.addAll(((AlgebraicCollection)algebra).flattenAndLimitByClass((Class<AlgebraicParticle>) algorithm.ALGORITHM_LEVEL));
			return list;
		}
		else if(algebra instanceof Equation) {
			return ((Equation)algebra).flattenAndLimitByClass((Class<AlgebraicParticle>) algorithm.ALGORITHM_LEVEL);
		}
		return list;
	}
	
}
