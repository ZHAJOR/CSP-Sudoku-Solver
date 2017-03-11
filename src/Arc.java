/**
 * This class describe the link between two variables
 * It's used for the AC-3
 */
public class Arc {
    private Variable X;
    private Variable Y;

    public Arc(Variable x, Variable y) {
        X = x;
        Y = y;
    }

    public Variable getX() {
        return X;
    }

    public void setX(Variable x) {
        X = x;
    }

    public Variable getY() {
        return Y;
    }

    public void setY(Variable y) {
        Y = y;
    }
}
