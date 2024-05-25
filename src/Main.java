import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
//        ProductionTable.setTableFile("analyzer.xlsx");
//        ProductionTable.initSymbolMap();
//        ProductionConst.getProductionFromFile("production.txt");
        GrammarAnalysis.getFileContent("./test.txt");
        GrammarAnalysis grammarAnalysis = new GrammarAnalysis();
        grammarAnalysis.handleInput();
//        grammarAnalysis.fAnalysisInput();
        grammarAnalysis.analysisInput();
    }
}