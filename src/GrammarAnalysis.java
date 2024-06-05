import org.apache.commons.math3.util.Pair;

import java.io.*;
import java.util.*;

public class GrammarAnalysis{
    private static final String BEGIN_SIGN = "s'";
    private static final String OUTPUT_FILE_NAME = "analyze.out";
    private static final String ERROR_FILE_NAME = "analyze.err";
    private static final String ANALYZER_FILE_NAME = "analyzer.xlsx";
    private static final String PRODUCTION_FILE_NAME = "production.txt";
    private File _outputFile;
    private File _errorFile;
    private FileWriter _outputFileWriter;
    private FileWriter _errorFileWriter;

    private static BufferedReader _bufferedReader;
    private static FileReader _fileReader;
    private String _textContent = "";
    private ArrayList<Symbol> _lexeme = new ArrayList<>();
    private Queue<Symbol> _lexemeQueue;
    private Queue<String> _actionMessageQueue;
    private Queue<String> _symbolMessageQueue;
    private Queue<String> _errorMessageQueue;
    private Queue<String> _stateMessageQueue;
    private Queue<String> _lexemeMessageQueue;
    private Stack<Symbol> _symbolStack;
    private Stack<Integer> _stateStack;
    private Integer _currentState;
    private Integer _tmpIndex = 0;
    private boolean _isError;
    private Boolean _isUsingExternalFile;
    private String _externalTablePath;
    private File _externalTableFile;

    public static void getFileContent(String filePath) throws FileNotFoundException {
        _fileReader = new FileReader(filePath);
        _bufferedReader = new BufferedReader(_fileReader);
    }
    public void handleInput(ArrayList<Symbol> symbolArrayList) throws IOException {
        for (Symbol symbol : symbolArrayList) {
            symbol.printSymbol();
        }
        for (Symbol symbol : symbolArrayList) {
            _lexeme.add(symbol);
        }
        if(_lexeme != null){
            Symbol end = new Symbol("$","");
            _lexeme.add(end);
            _lexemeQueue = new LinkedList<>(_lexeme);
        }
    }

    private void setOutput(String content, FileWriter fileWriter){
        if(null == fileWriter) throw new IllegalArgumentException("File writer is null!");
        else {
            System.out.print(content);
            try {
                fileWriter.write(content);
                fileWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void getOutputTable(){
        try{
            if(null != _outputFileWriter){
                _outputFileWriter.close();
            }
            _outputFileWriter = new FileWriter(_outputFile);
        } catch (IOException e){
            throw new RuntimeException(e);
        }

//        setOutput(String.format("%-50s%-50s%-50s%-50s\n", "State", "Input", "Symbol", "Action"), _outputFileWriter);
        setOutput(String.format("%-50s\t%-50s\n", "Symbol", "Action"), _outputFileWriter);
//        System.out.printf("%-25s%-25s%-25s%-25s\n", "State", "Input", "Symbol", "Action");

        int index = Math.min(_actionMessageQueue.size(), _symbolMessageQueue.size());
        for(int i = 0; i < index; i++){

            setOutput(String.format("%-50s\t\t\t\t%-50s\n", _symbolMessageQueue.peek(), _actionMessageQueue.peek()),
                    _outputFileWriter);
//            System.out.printf("%-25s%-25s%-25s%-25s\n", _stateMessageQueue.peek(), _lexemeMessageQueue.peek(), _symbolMessageQueue.peek(), _actionMessageQueue.peek());

            _actionMessageQueue.poll();
            _symbolMessageQueue.poll();
            _stateMessageQueue.poll();
            _lexemeMessageQueue.poll();
        }
        if(!_symbolMessageQueue.isEmpty()) setOutput(_symbolMessageQueue.peek(), _outputFileWriter);
    }

    private void writeContentToFile(String content){
        try{
            File file = new File(OUTPUT_FILE_NAME);
            FileWriter writer = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private String dumpStackState(){
        return "Dump: \nState: " + _stateStack.toString() + " Symbol: " + _symbolStack.toString() + " Input: " + _lexemeQueue.toString();
    }
    private void setErrorMessage(String message){
        _isError = true;
        _errorMessageQueue.add(message + "\n" + dumpStackState());
        _lexemeQueue.poll();
    }

    private void getErrorMessage(){
        try{
            if(null != _errorFileWriter){
                _errorFileWriter.close();
            }
            _errorFileWriter = new FileWriter(_errorFile);
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        while(!_errorMessageQueue.isEmpty()){
//            System.out.println(_errorMessageQueue.peek());
            setOutput(_errorMessageQueue.peek(), _errorFileWriter);
            _errorMessageQueue.poll();
        }
    }

    private void initStackAndQueue(){
        // 初始化堆栈状态
        _stateStack = new Stack<>();
        _symbolStack = new Stack<>();
        _actionMessageQueue = new LinkedList<>();
        _symbolMessageQueue = new LinkedList<>();
        _errorMessageQueue = new LinkedList<>();
        _stateMessageQueue = new LinkedList<>();
        _lexemeMessageQueue = new LinkedList<>();

        _stateStack.push(0);
        Symbol start = new Symbol("$","");
        _symbolStack.push(start);
    }


    private void initFileStatus(){
        if(null == _errorFile) _errorFile = new File(ERROR_FILE_NAME);
        if(null == _outputFile) _outputFile = new File(OUTPUT_FILE_NAME);
        if(_errorFile.exists()){
            Boolean hasDeleted = _errorFile.delete();
        }
        if(_outputFile.exists()){
            Boolean hasDeleted = _outputFile.delete();
        }
        try{
            boolean hasCreated = _errorFile.createNewFile();
            hasCreated = _outputFile.createNewFile();
        }catch (IOException e){
            System.out.println(e);
        }
    }
    private void initProductionConstAndTable(){
        ProductionTable.setTableFile(ANALYZER_FILE_NAME);
        try {
            ProductionTable.initSymbolMap();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ProductionConst.getProductionFromFile(PRODUCTION_FILE_NAME);
    }

    private void initHardcodedProductionConstAndTable(){
        ProductionConst.testProductionMap();

    }
    public void analysisInput() throws IOException{
        initFileStatus();
        initStackAndQueue();
        initHardcodedProductionConstAndTable();
        // 开始分析
        while(!_lexemeQueue.isEmpty()){
            _currentState = _stateStack.peek();
            // 头部不能为文法开始符号，因为文法开始符号一旦出现在symbolStack头部即规约成功
            _symbolMessageQueue.add(_symbolStack.toString());
            _stateMessageQueue.add(_stateStack.toString());
            _lexemeMessageQueue.add(_lexemeQueue.toString());
            if(!BEGIN_SIGN.equals(_symbolStack.peek().getType()) && !_lexemeQueue.isEmpty()) {
                analysisAction(_currentState, _lexemeQueue.peek().getType());
            }else{
                break;
            }
        }
        getOutputTable();
        if(_isError) getErrorMessage();
    }

    public void fAnalysisInput() throws IOException {
        initFileStatus();
        initStackAndQueue();
        initProductionConstAndTable();

        // 开始分析
        while(!_lexemeQueue.isEmpty()){
            _currentState = _stateStack.peek();
            // 头部不能为文法开始符号，因为文法开始符号一旦出现在symbolStack头部即规约成功
            _symbolMessageQueue.add(_symbolStack.toString());
            _stateMessageQueue.add(_stateStack.toString());
            _lexemeMessageQueue.add(_lexemeQueue.toString());
            if(!BEGIN_SIGN.equals(_symbolStack.peek().getType()) && !_lexemeQueue.isEmpty()) {
                fAnalysisAction(_currentState, _lexemeQueue.peek().getType());
            }else{
                break;
            }
        }
        getOutputTable();
        if(_isError) getErrorMessage();
    }

    private void shiftAction(int stateTransferTo){
        try{
            _symbolStack.push(_lexemeQueue.peek());
            _lexemeQueue.poll();
            _stateStack.push(stateTransferTo);

            _actionMessageQueue.add("SHIFT TO: " + stateTransferTo);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void fShiftAction(int stateTransferTo){
        try{
            _symbolStack.push(_lexemeQueue.peek());
            _lexemeQueue.poll();
            _stateStack.push(stateTransferTo);

            _actionMessageQueue.add("SHIFT TO: " + stateTransferTo);
        }catch(Exception e){
            System.out.println(e);
        }
    }

    private void fReduceAction(int ruleApplyTo){
        try{
            String tmp1 = "";
            String tmp2 = "";
            String tmp3 = "";
            Production p = ProductionConst.PRODUCTION_MAP.get(ruleApplyTo);
            // 获取产生式的右部
            ArrayList<String> rightPart = p.getRightPart();
            Integer rightPartLen = p.getRightPartLength();
            if(!rightPart.isEmpty() && (!"eps".equals(rightPart.get(0)))){
                for(int i = 0; i < rightPartLen; i++){
                    if(ruleApplyTo <= 40 && ruleApplyTo >= 37 && i == 0){
                        tmp1 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo <= 40 && ruleApplyTo >= 37 && i == 2){
                        tmp2 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 55){
                        tmp3 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 27 && i == 0){
                        tmp3 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 23 && i == 1){
                        tmp3 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 29 && i == 0){
                        tmp1 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 29 && i == 1){
                        tmp2 = _symbolStack.peek().getValue();
                    }else if(ruleApplyTo == 36 && i == 1){
                        tmp3 = _symbolStack.peek().getValue();
                    }
                    _symbolStack.pop();
                    _stateStack.pop();
                }
                if(ruleApplyTo == 37){
                    _tmpIndex++;
                    System.out.println("t" + _tmpIndex + " = " + tmp2 + " + " + tmp1);
                } else if (ruleApplyTo == 38) {
                    _tmpIndex++;
                    System.out.println("t" + _tmpIndex + " = " + tmp2 + " - " + tmp1);
                } else if(ruleApplyTo == 39){
                    _tmpIndex++;
                    System.out.println("t" + _tmpIndex + " = " + tmp2 + " * " + tmp1);
                } else if(ruleApplyTo == 40){
                    _tmpIndex++;
                    System.out.println("t" + _tmpIndex + " = " + tmp2 + " / " + tmp1);
                }
                else if (ruleApplyTo == 29) {
                    System.out.println(tmp2 + " = " + tmp1);
                }
                Symbol left = new Symbol("", "");
                if(ruleApplyTo == 55){
                    left = new Symbol(p.getLeftPart(),tmp3);
                }else if(ruleApplyTo <= 40 && ruleApplyTo >= 37){
                    left = new Symbol(p.getLeftPart(),"t" + _tmpIndex);
                }else if(ruleApplyTo == 27){
                    left = new Symbol(p.getLeftPart(),tmp3);
                }else if(ruleApplyTo == 23){
                    left = new Symbol(p.getLeftPart(),tmp3);
                }else if(ruleApplyTo == 29){
                    left = new Symbol(p.getLeftPart(),"");
                }else if(ruleApplyTo == 36){
                    left = new Symbol(p.getLeftPart(),tmp3);
                }
                else{
                    left = new Symbol(p.getLeftPart(),"");
                }
                // 将右部规约成左部
                _symbolStack.push(left);
                _currentState = _stateStack.peek();
                // 基于目前状态再分析一次
                if(!_symbolStack.peek().getType().equals(BEGIN_SIGN)) fAnalysisAction(_currentState, _symbolStack.peek().getType());
            }else if("eps".equals(rightPart.get(0))){
                Symbol left = new Symbol(p.getLeftPart(),"");
                _symbolStack.push(left);
                _currentState = _stateStack.peek();
                fAnalysisAction(_currentState,  _symbolStack.peek().getType());
            }

            _actionMessageQueue.add("REDUCE ACTION: " + p);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    private void fGotoAction(int stateTransferTo){
        try{
            _stateStack.push(stateTransferTo);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    private void gotoAction(int stateTransferTo){
        try{
            _stateStack.push(stateTransferTo);
//            _actionMessageQueue.add("GOTO: " + stateTransferTo);
        }catch (Exception e){
            System.out.println(e);
        }
    }
    private void reduceAction(int ruleApplyTo){
        try{
            Production p = ProductionConst.PRODUCTION_MAP.get(ruleApplyTo);
            // 获取产生式的右部
            ArrayList<String> rightPart = p.getRightPart();
            Integer rightPartLen = p.getRightPartLength();
            if(!rightPart.isEmpty() && (!"eps".equals(rightPart.get(0)))){
                for(int i = 0; i < rightPartLen; i++){
                    _symbolStack.pop();
                    _stateStack.pop();
                }
                Symbol left = new Symbol(p.getLeftPart(),"");
                // 将右部规约成左部
                _symbolStack.push(left);
                _currentState = _stateStack.peek();
                // 基于目前状态再分析一次
                if(!_symbolStack.peek().equals(BEGIN_SIGN)) analysisAction(_currentState, _symbolStack.peek().getType());
            }else if("eps".equals(rightPart.get(0))){
                Symbol left = new Symbol(p.getLeftPart(),"");
                _symbolStack.push(left);
                _currentState = _stateStack.peek();
                analysisAction(_currentState,  _symbolStack.peek().getType());
            }

            _actionMessageQueue.add("REDUCE ACTION: " + p);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    private String get2ndElementFromQueue(Queue<Symbol> q) throws Exception {
       if(q.size() < 2) throw new IllegalArgumentException("Queue size is not correct.");
       String ans = "";
       Queue<Symbol> p = new LinkedList<>();
       Symbol tmp = q.poll();
       p.offer(tmp);
       ans = q.peek().getType();
       while(!q.isEmpty()){
           p.offer(q.peek());
           q.poll();
       }
       _lexemeQueue=p;
       return ans;
    }

    private void fAnalysisAction(Integer currentState, String inputFront){
        try {
            Pair<String, Integer> actionPair = ProductionTable.getCellValue(inputFront, currentState);
            switch (actionPair.getFirst()){
                case "Goto":
                    fGotoAction(actionPair.getSecond());
                    break;
                case "Reduce":
                    fReduceAction(actionPair.getSecond());
                    break;
                case "Shift":
                    fShiftAction(actionPair.getSecond());
                    break;
                case "Predict":
                    String predict = get2ndElementFromQueue(_lexemeQueue);
                    if (predict.equals("function")) {
                        fShiftAction(26);
                    } else {
                        fReduceAction(26);
                    }
            }
            // 如果发生空指针异常说明对应单元格为空
        }catch(NullPointerException e){
            setErrorMessage("Error!");
        }catch(IllegalArgumentException e){
            setErrorMessage("QueueSize is not correct");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void analysisAction(Integer currentState, String inputFront) {
        switch(currentState){
            case 0:
                switch(inputFront){
                    case "(":
                        shiftAction(5);
                        break;
                    case "INTLITERAL":
                        shiftAction(7);
                        break;
                    case "FLOATLITERAL":
                        shiftAction(8);
                        break;
                    case "id":
                        shiftAction(6);
                        break;
                    case "expr":
                        gotoAction(2);
                        break;
                    case "factor":
                        gotoAction(3);
                        break;
                    case "expr_list":
                        gotoAction (1);
                        break;
                    case "primary":
                        gotoAction(4);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 1:
                switch(inputFront){
                    case "$":
                        reduceAction(0);
                        System.out.println("Successful!");
                        return;
                    default:
                        setErrorMessage("Error!");
                        break;
                };
                break;
            case 2:
                switch(inputFront){
                    case "$":
                        reduceAction(5);
                        break;
                    case ",":
                        shiftAction(10);
                        break;
                    case "expr_list_tail":
                        gotoAction(9);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 3:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                        reduceAction(15);
                        break;
                    case "+":
                        shiftAction(13);
                        break;
                    case "-":
                        shiftAction(14);
                        break;
                    case "addop":
                        gotoAction(12);
                        break;
                    case "expr_tail":
                        gotoAction(11);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 4:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                        reduceAction(17);
                        break;
                    case "*":
                        shiftAction(25);
                        break;
                    case "/":
                        shiftAction(26);
                        break;
                    case "mulop":
                        gotoAction(16);
                        break;
                    case "factor_tail":
                        gotoAction(15);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 5:
                switch(inputFront){
                    case "(":
                        shiftAction(5);
                        break;
                    case "INTLITERAL":
                        shiftAction(7);
                        break;
                    case "FLOATLITERAL":
                        shiftAction(8);
                        break;
                    case "id":
                        shiftAction(6);
                        break;
                    case "expr":
                        gotoAction(17);
                        break;
                    case "factor":
                        gotoAction(3);
                        break;
                    case "primary":
                        gotoAction(4);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 6:
                switch (inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        reduceAction(7);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 7:
                switch (inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        reduceAction(8);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 8:
                switch (inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        reduceAction(9);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 9:
                switch(inputFront){
                    case "$":
                        reduceAction(3);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 10:
                switch(inputFront){
                    case "$":
                        reduceAction(5);
                        break;
                    case ",":
                        shiftAction(10);
                        break;
                    case "(":
                        shiftAction(5);
                        break;
                    case "INTLITERAL":
                        shiftAction(7);
                        break;
                    case "FLOATLITERAL":
                        shiftAction(8);
                        break;
                    case "id":
                        shiftAction(6);
                        break;
                    case "expr":
                        gotoAction(18);
                        break;
                    case "factor":
                        gotoAction(3);
                        break;
                    case "primary":
                        gotoAction(4);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 11:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                        reduceAction(1);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 12:
                switch(inputFront){
                    case "(":
                        shiftAction(5);
                        break;
                    case "INTLITERAL":
                        shiftAction(7);
                        break;
                    case "FLOATLITERAL":
                        shiftAction(8);
                        break;
                    case "id":
                        shiftAction(6);
                        break;
                    case "factor":
                        gotoAction(19);
                        break;
                    case "primary":
                        gotoAction(4);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 13:
                switch(inputFront){
                    case "(":
                    case "INTLITERAL":
                    case "FLOATLITERAL":
                    case "id":
                        reduceAction(10);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 14:
                switch(inputFront){
                    case "(":
                    case "INTLITERAL":
                    case "FLOATLITERAL":
                    case "id":
                        reduceAction(11);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 15:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                        reduceAction(2);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 16:
                switch (inputFront){
                    case "(":
                        shiftAction(5);
                        break;
                    case "INTLITERAL":
                        shiftAction(7);
                        break;
                    case "FLOATLITERAL":
                        shiftAction(8);
                        break;
                    case "id":
                        shiftAction(6);
                        break;
                    case "primary":
                        gotoAction(20);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 17:
                switch(inputFront){
                    case ")":
                        shiftAction(21);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 18:
                switch(inputFront){
                    case ",":
                        shiftAction(10);
                        break;
                    case "expr_list_tail":
                        gotoAction(22);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 19:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                        reduceAction(15);
                        break;
                    case "+":
                        shiftAction(13);
                        break;
                    case "-":
                        shiftAction(14);
                        break;
                    case "addop":
                        gotoAction(12);
                        break;
                    case "expr_tail":
                        gotoAction(23);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 20:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                        reduceAction(17);
                        break;
                    case "*":
                        shiftAction(25);
                        break;
                    case "/":
                        shiftAction(26);
                        break;
                    case "mulop":
                        gotoAction(16);
                        break;
                    case "factor_tail":
                        gotoAction(24);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;

                }
                break;
            case 21:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                        reduceAction(6);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 22:
                switch(inputFront){
                    case "$":
                        reduceAction(4);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 23:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                        reduceAction(14);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 24:
                switch(inputFront){
                    case "$":
                    case ",":
                    case ")":
                    case "+":
                    case "-":
                        reduceAction(16);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 25:
                switch (inputFront){
                    case "(":
                    case "INTLITERAL":
                    case "FLOATLITERAL":
                    case "id":
                        reduceAction(12);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
            case 26:
                switch (inputFront){
                    case "(":
                    case "INTLITERAL":
                    case "FLOATLITERAL":
                    case "id":
                        reduceAction(13);
                        break;
                    default:
                        setErrorMessage("Error!");
                        break;
                }
                break;
        }
    }
}
