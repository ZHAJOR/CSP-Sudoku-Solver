import java.util.*;

/**
 * This class respects the modelisation of a csp
 * with the handling of the constrains and domains for each variables (ie values)
 */
public class CSP {
    private int[][] values;
    private ArrayList<Constraint>[][] constraints = new ArrayList[Sudoku.SUDOKU_SIZE][Sudoku.SUDOKU_SIZE];
    private ArrayList<Integer>[][] domains = new ArrayList[Sudoku.SUDOKU_SIZE][Sudoku.SUDOKU_SIZE];

    /**
     * A CSP need a sudoku to be initialize
     * Then all the constrains are generate here
     * Each variable has 20 binary constraints (8 of line, 8 of column and 4 of square)
     * No need to generate domains here, the AC-3 will do the work later
     */
    public CSP(Sudoku sudo) {
        values = sudo.getValues();
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                constraints[i][j] = new ArrayList<>();
            }
        }
        // Add constraints for each variable
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                for (int line = 0; line < Sudoku.SUDOKU_SIZE; ++line) {
                    if (line != j)
                        constraints[i][j].add(new Constraint(i, line));
                }
                for (int column = 0; column < Sudoku.SUDOKU_SIZE; ++column) {
                    if (column != i)
                        constraints[i][j].add(new Constraint(column, j));
                }
                int minX = (i/3) * 3;
                int maxX = minX + 3;
                int minY = (j/3) * 3;
                int maxY = minY + 3;
                for (int squareX = minX; squareX < maxX; ++squareX) {
                    for (int squareY = minY; squareY < maxY; ++squareY) {
                        if (squareX != i && squareY != j) {
                            constraints[i][j].add(new Constraint(squareX, squareY));
                        }
                    }
                }
            }
        }
    }

    /**
     * Launch the backtracking algorithm and return the solved sudoku
     */
    public Sudoku doBackTracking() {
        BackTracking bt = new BackTracking(this);
        if (bt.backtrackingSearch()) {
            Sudoku result = new Sudoku();
            result.loadArrays(values, bt.getSolution());
            return result;
        }
        return null;
    }

    /**
     * Used for backtracking
     * Check if it's solved
     */
    public boolean isComplete(int[][] assignment) {
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                if (assignment[i][j] == 0 && values[i][j] == 0)
                    return false;
            }
        }
        return true;
    }

    /**
     * Used for backtracking
     * Return the variable
     * First we do an AC-3 then MRV, degree heuristic and finally least constraining value
     */
    public Variable getUnassignedVariable(int[][] assignment) {
        ArrayList<Variable> best = getAvailaibleVariables(assignment);
        AC3(assignment, best);
        best = MRV(best);
        if (best.size() == 0)
            return null;
        else if (best.size() == 1)
            return best.get(0);
        best = degreeHeuristic(assignment, best);
        if (best.size() == 1)
            return best.get(0);
        best = leastConstrainingValue(assignment, best);
        return best.get(0);
    }

    /**
     * Return a list of variables not set yet
     */
    public ArrayList<Variable> getAvailaibleVariables(int[][] assignment) {
        ArrayList<Variable> result = new ArrayList<>();
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                if (values[i][j] == 0 && assignment[i][j] == 0)
                    result.add(new Variable(i, j));
            }
        }
        return result;
    }

    /**
     * The MRV algorithm
     * As the AC-3 has been called we use the domains values pre-computed
     */
    public ArrayList<Variable> MRV(ArrayList<Variable> available) {
        int minValues = -1;
        ArrayList<Variable> result = new ArrayList<>();
        for (Variable var : available) {
            int localMax = domains[var.getX()][var.getY()].size();
            if (minValues == -1 || minValues > localMax) {
                result  = new ArrayList<>();
                minValues = localMax;
                result.add(new Variable(var.getX(), var.getY()));
            }
            else if (minValues == localMax)
                result.add(new Variable(var.getX(), var.getY()));
        }
        return result;
    }

    /**
     * This method was first used for the MRV algorithm before the implementation of AC-3
     * Now used for leastConstrainingValueMaxValuesPossible
     */
    private int MRVMaxValues(int[][] assignment, int x, int y) {
        int max = 9;
        for (int line = 0; line < Sudoku.SUDOKU_SIZE; ++line) {
            if (line != y && (values[x][line] != 0 || assignment[x][line] != 0))
                max--;
        }
        for (int column = 0; column < Sudoku.SUDOKU_SIZE; ++column) {
            if (column != x && (values[column][y] != 0 || assignment[column][y] != 0))
                max--;
        }
        int minX = (x/3) * 3;
        int maxX = minX + 3;
        int minY = (y/3) * 3;
        int maxY = minY + 3;
        for (int squareX = minX; squareX < maxX; ++squareX) {
            for (int squareY = minY; squareY < maxY; ++squareY) {
                if (squareX != x && squareY != y && (values[squareX][squareX] != 0 || assignment[squareX][squareX] != 0))
                    max--;
            }
        }

        return max;
    }

    /**
     * The degree heuristic algorithm
     */
    public ArrayList<Variable> degreeHeuristic(int[][] assignment, ArrayList<Variable> MRVSelection) {
        int maxConstraints = -1;
        ArrayList<Variable> result = new ArrayList<>();
        for (Variable var : MRVSelection) {
            int local = degreeHeuristicMaxConstraints(assignment, var.getX(), var.getY());
            if (maxConstraints < local) {
                maxConstraints = local;
                result = new ArrayList<>();
                result.add(var);
            }
            else if (maxConstraints == local)
                result.add(var);
        }
        return result;
    }

    /**
     * Used by degreeHeuristic
     * return the number of current state constraints
     */
    public int degreeHeuristicMaxConstraints(int[][] assignment, int x, int y) {
        ArrayList<Constraint> localContraint = constraints[x][y];
        int max = localContraint.size();
        for (Constraint local : localContraint) {
            if (values[local.getX()][local.getY()] == 0 && assignment[local.getX()][local.getY()] == 0)
                max--;
        }
        return max;
    }

    /**
     * The least constraining value algorithm
     * We look for the variable with the most values possible for the remaining variables
     */
    public ArrayList<Variable> leastConstrainingValue(int[][] assignment, ArrayList<Variable> heuristicSelection) {
        int maxValues = -1;
        ArrayList<Variable> result = new ArrayList<>();
        for (Variable var : heuristicSelection) {
            int local = leastConstrainingValueMaxValuesPossible(assignment, var.getX(), var.getY(), heuristicSelection);
            if (maxValues == -1 || maxValues > local) {
                result = new ArrayList<>();
                maxValues = local;
                result.add(var);
            }
            else if (maxValues == local)
                result.add(var);
        }
        return result;
    }

    /**
     * Used by leastConstrainingValue
     * Set a value for the assignment which can be anything but 0
     * (cf MRVMaxValues implementation)
     */
    public int leastConstrainingValueMaxValuesPossible(int[][] assignment, int x, int y, ArrayList<Variable> heuristicSelection) {
        // As we made an MRV first all the Variable have the same MaxValue
        int max = domains[x][y].size() * heuristicSelection.size();
        assignment[x][y] = 10;
        for (Variable var : heuristicSelection) {
            if (var.getX() != x && var.getY() != y)
                max -= MRVMaxValues(assignment, var.getX(), var.getY());
        }
        assignment[x][y] = 0;
        return max;
    }

    /**
     * The AC-3 algorithm
     * Here we use the array of constraints to create the queue
     */
    public void AC3(int[][] assignment, ArrayList<Variable> available) {
        Queue<Arc> queue = new LinkedList<>();
        for (Variable var : available) {
            domains[var.getX()][var.getY()] = computeDomains(assignment, var.getX(), var.getY());
            // The constraints represents in this case all the arcs
            for (Constraint localConst: constraints[var.getX()][var.getY()]) {
                queue.add(new Arc(var, new Variable(localConst.getX(), localConst.getY())));
                domains[localConst.getX()][localConst.getY()] = computeDomains(assignment, localConst.getX(), localConst.getY());
            }
        }
        while(!queue.isEmpty()) {
            Arc arc = queue.remove();
            if (removeInconsistentValues(arc)) {
                for (Constraint localConst: constraints[arc.getX().getX()][arc.getX().getY()]) {
                    queue.add(new Arc(new Variable(localConst.getX(), localConst.getY()), arc.getX()));
                }
            }
        }
    }

    /**
     * Used by the AC-3
     * Update the domains list of a variable
     */
    public boolean removeInconsistentValues(Arc arc) {
        boolean result = false;
        ArrayList<Integer> savingX = new ArrayList<>(domains[arc.getX().getX()][arc.getX().getY()]);
        for (Integer domain : domains[arc.getX().getX()][arc.getX().getY()]) {
            ArrayList<Integer> yDomains = domains[arc.getY().getX()][arc.getY().getY()];
            // No more values only if there is one value in the y domains which is the same as domain var
            if (yDomains.size() == 1 && yDomains.get(0).equals(domain)) {
                savingX.remove(domain);
                result = true;
            }
        }
        domains[arc.getX().getX()][arc.getX().getY()] = savingX;
        return result;
    }

    /**
     * Compute the possible domains for a variable
     * using the current state of the grid
     */
    public ArrayList<Integer> computeDomains(int[][] assignment, int x, int y) {
        ArrayList<Integer> result = new ArrayList<>(9);
        for(int i = 1; i <= 9; ++i)
            result.add(i);
        for (int line = 0; line < Sudoku.SUDOKU_SIZE; ++line) {
            if (line != y && (assignment[x][line] != 0 || values[x][line] != 0))
                result.remove(new Integer(assignment[x][line] + values[x][line]));
        }
        for (int column = 0; column < Sudoku.SUDOKU_SIZE; ++column) {
            if (column != x && (assignment[column][y] != 0 || values[column][y] != 0))
                result.remove(new Integer(assignment[column][y] + values[column][y]));
        }
        int minX = (x/3) * 3;
        int maxX = minX + 3;
        int minY = (y/3) * 3;
        int maxY = minY + 3;
        for (int squareX = minX; squareX < maxX; ++squareX) {
            for (int squareY = minY; squareY < maxY; ++squareY) {
                if (squareX != x && squareY != y && (assignment[squareX][squareY] != 0 || values[squareX][squareY] != 0)) {
                    result.remove(new Integer(assignment[squareX][squareY] + values[squareX][squareY]));
                }
            }
        }
        return result;
    }

    public ArrayList<Integer> getDomains(int x, int y) {
        return domains[x][y];
    }

    /**
     * Used for backtracking
     * Check if all the constraints are good
     */
    public boolean checkConsistent(int[][] assignment, Variable var) {
        for (Constraint localConstraint : constraints[var.getX()][var.getY()]) {
            if (assignment[localConstraint.getX()][localConstraint.getY()] == var.getValue()
                    || values[localConstraint.getX()][localConstraint.getY()] == var.getValue())
                return false;
        }
        return true;
    }

    public int[][] getValues() {
        return values;
    }

    public void setValues(int[][] values) {
        this.values = values;
    }
}
