package experiment2;

/**
 * @author 李天翔
 * @date 2022/05/20
 **/
public class Step {
    int numStep;
    String stackString;
    String inString;
    String productString;

    public Step(int numStep, String stackString, String inString, String productString) {
        this.numStep = numStep;
        this.stackString = stackString;
        this.inString = inString;
        this.productString = productString;
    }

    @Override
    public String toString() {
        return  numStep +
                "\t\t" + stackString +
                "\t\t" + inString +
                "\t\t" + productString + "\t\t";
    }
}
