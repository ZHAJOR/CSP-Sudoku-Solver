import java.util.ArrayList;

/**
 * The implementation of the BackTracking algorithm
 * The solution is stored in the solution array (ie sudoku grid),
 * it's then used to make the solved sudoku and print it
 */
public class BackTracking {
    private CSP csp;
    private int[][] solution;

    public BackTracking(CSP csp) {
        this.csp = csp;
    }

    public boolean backtrackingSearch() {
        return recursiveBackTracking(new int[Sudoku.SUDOKU_SIZE][Sudoku.SUDOKU_SIZE]);
    }

    private boolean recursiveBackTracking(int[][] assignment) {
        if (csp.isComplete(assignment)) {
            solution = assignment;
            return true;
        }
        Variable var = csp.getUnassignedVariable(assignment);
        ArrayList<Integer> domains = csp.getDomains(var.getX(), var.getY());
        for (int i = 0; i < domains.size(); ++i) {
            var.setValue(domains.get(i));
            if (csp.checkConsistent(assignment, var)) {
                assignment[var.getX()][var.getY()] = var.getValue();
                boolean result = recursiveBackTracking(assignment);
                if (result)
                    return result;
                assignment[var.getX()][var.getY()] = 0;
            }
        }
        return false;
    }

    public int[][] getSolution() {
        return solution;
    }
}
