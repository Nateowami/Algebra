/*
    Solve4x - A Java program to solve and explain algebra problems
    Copyright (C) 2013 Nathaniel Paulus

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

/**
 * Represents a solution for a given equation or expression by 
 * holding a Step ArrayList.
 * @author Nateowami
 */
public class Solution {
	
	private String equation;//the original equation or expression
	ArrayList <Step>steps = new ArrayList<Step>();//a list of steps
	
	/**
	 * Creates a new Solution from another Solution
	 * @param solution The Solution to copy
	 */
	public Solution(Solution solution){
		this.equation = solution.getEquation();
		this.steps = solution.getList();
	}
	
	/**
	 * Creates a new Solution
	 * @param equation The equation that is being solved
	 * @see Solution(String equation, Step step)
	 */
	public Solution(String equation){
		this.equation = equation;
	}
	
	/**
	 * Creates a new Solution
	 * @param equation The equation that is being solved
	 * @param step A Step to add to the begging of the 
	 * list of solving steps.
	 * @see Solution(String equation)
	 */
	public Solution(String equation, Step step){
		this.equation = equation;
		this.steps.add(step);
	}
	
	/**
	 * Gets the list of steps for solving
	 * @return The lsit of steps for solving
	 */
	private ArrayList<Step> getList() {
		return this.steps;
	}

	/**
	 * Gets the original equation that the user entered
	 * @return The original equation
	 */
	public String getEquation(){
		return this.equation;
	}
	
	/**
	 * Adds a Step to the current Solution
	 * @param step
	 */
	public void addStep(Step step){
		this.steps.add(step);
	}
	
	/**
	 * Finds the number of steps in this Solution
	 * @return The current number of steps
	 */
	public int getNumbOfSteps(){
		return this.steps.size();
	}
	
	/**
	 * Gets the Step at the specified index
	 * @param index The index of the Step you want
	 * @return The Step specified
	 */
	public Step getStepAt(int index){
		return this.steps.get(index);
	}
}
