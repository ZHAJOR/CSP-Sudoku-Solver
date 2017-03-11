public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("You must give the path of the sudoku file!");
            return;
        }
        Sudoku sudo = new Sudoku();
        sudo.loadFile(args[0]);
        sudo.print();
        CSP csp = new CSP(sudo);
        long startTime = System.nanoTime();
        Sudoku result = csp.doBackTracking();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;
        result.print();
        System.out.println("Done in " + duration + " ms");
    }
}
