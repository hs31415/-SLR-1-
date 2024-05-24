import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ProductionConst.testProductionMap();
        GrammarAnalysis.getFileContent("./test.txt");
        GrammarAnalysis grammarAnalysis = new GrammarAnalysis();
        grammarAnalysis.handleInput();
        grammarAnalysis.analysisInput();
    }
}