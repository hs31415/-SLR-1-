import java.util.ArrayList;
import java.util.List;

public class Production {
    private String leftPart;
    private ArrayList<String> rightPart;

    public Production(String leftPart, ArrayList<String> rightPart){
        this.leftPart = leftPart;
        this.rightPart = rightPart;
    }

    public Integer getRightPartLength(){
        return rightPart.size();
    }

    public String getLeftPart() {
        return leftPart;
    }

    public ArrayList<String> getRightPart() {
        return rightPart;
    }

    @Override
    public String toString() {
        return leftPart + "->" + rightPart;
    }
}
