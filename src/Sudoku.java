import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class allow us to load a sudoku from a file and print it
 * It's also used to merge the unsolved sudoku with the values from the CSP
 */
public class Sudoku {
    private int[][] values;
    public static final int SUDOKU_SIZE = 9;

    public Sudoku() {
        values = new int[SUDOKU_SIZE][SUDOKU_SIZE];
    }

    public void loadArrays(int[][] val, int[][] assignment) {
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                values[i][j] = assignment[i][j] == 0 ? val[i][j] : assignment[i][j];
            }
        }
    }

    public void loadFile(String filename) {
        try (InputStream in = Files.newInputStream(Paths.get(filename));
                BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); ++i)
                    values[lineNumber][i] = Integer.parseInt(line.charAt(i) + "");
                lineNumber++;
            }
        } catch (IOException x) {
            System.err.println(x);
        }
    }

    public void print() {
        for (int i = 0; i < SUDOKU_SIZE; ++i) {
            if (i % 3 == 0 && i != 0)
                System.out.println("|\n-------------------");
            else if (i % 3 == 0)
                System.out.println("\n-------------------");
            else
                System.out.println("|");
            for (int j = 0; j < SUDOKU_SIZE; ++j) {
                if (j % 3 == 0)
                    System.out.print("|");
                else
                    System.out.print(" ");
                System.out.print(values[i][j]);
            }
        }
        System.out.println("|\n-------------------");
    }

    public boolean isGood() {
        for (int i = 0; i < Sudoku.SUDOKU_SIZE; ++i) {
            for (int j = 0; j < Sudoku.SUDOKU_SIZE; ++j) {
                for (int line = 0; line < Sudoku.SUDOKU_SIZE; ++line) {
                    if (line != j && values[i][j] == values[i][line])
                        return false;
                }
                for (int column = 0; column < Sudoku.SUDOKU_SIZE; ++column) {
                    if (column != i && values[i][j] == values[column][j])
                        return false;
                }
                int minX = (i/3) * 3;
                int maxX = minX + 3;
                int minY = (j/3) * 3;
                int maxY = minY + 3;
                for (int squareX = minX; squareX < maxX; ++squareX) {
                    for (int squareY = minY; squareY < maxY; ++squareY) {
                        if (squareX != i && squareY != j && values[i][j] == values[squareX][squareY]) {
                            return false;
                        }
                    }
                }
            }
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
