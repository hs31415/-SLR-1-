import java.io.*;
import java.util.*;

public class GrammarAnalysis{
    public static final String BEGIN_SIGN = "expr_list1";
    private static BufferedReader _bufferedReader;
    private static FileReader _fileReader;
    private String _textContent = "";
    private ArrayList<String> _lexeme;
    private Queue<String> _lexemeQueue;
    private Queue<String> _actionMessageQueue;
    private Queue<String> _symbolMessageQueue;
    private Queue<String> _errorMessageQueue;
    private Stack<String> _symbolStack;
    private Stack<Integer> _stateStack;
    private Integer _currentState;
    private boolean _isError;
    private Boolean _isUsingExternalFile;
    private String _externalTablePath;
    private File _externalTableFile;

    public static void getFileContent(String filePath) throws FileNotFoundException {
        _fileReader = new FileReader(filePath);
        _bufferedReader = new BufferedReader(_fileReader);
    }
    public void handleInput() throws IOException {
        String lineInput;
        while(_bufferedReader.ready()){
            lineInput = _bufferedReader.readLine();
            _textContent += lineInput;
        }
        if(_textContent != null && !_textContent.isEmpty()){
            _lexeme = new ArrayList<>(Arrays.asList(_textContent.split(" ")));
        }
        if(_lexeme != null){
            _lexeme.add("$");
            _lexemeQueue = new LinkedList<>(_lexeme);
        }
    }

    private void getOutputTable(){
        System.out.printf("%-100s%-100s\n", "Symbol", "Action");
        int index = Math.min(_actionMessageQueue.size(), _symbolMessageQueue.size());
        for(int i = 0; i < index; i++){
            System.out.printf("%-100s%-100s\n",_symbolMessageQueue.peek(), _actionMessageQueue.peek());
            _actionMessageQueue.poll();
            _symbolMessageQueue.poll();
        }
        System.out.println(_symbolMessageQueue.peek());
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
        while(!_errorMessageQueue.isEmpty()){
            System.out.println(_errorMessageQueue.peek());
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

        _stateStack.push(0);
        _symbolStack.push("$");
    }
    public void analysisInput() throws IOException{
        initStackAndQueue();
        // 开始分析
        while(!_lexemeQueue.isEmpty()){
            _currentState = _stateStack.peek();
            // 头部不能为文法开始符号，因为文法开始符号一旦出现在symbolStack头部即规约成功
            _symbolMessageQueue.add(_symbolStack.toString());
            if(!BEGIN_SIGN.equals(_symbolStack.peek()) && !_lexemeQueue.isEmpty()) {
                if(!_isUsingExternalFile) analysisAction(_currentState, _lexemeQueue.peek());
                else analysisAction(_currentState, _lexemeQueue.peek(), _externalTablePath);
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

    }

    private void fReduceAction(int ruleApplyTo){

    }

    private void fGotoAction(int stateTransferTo){

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
                // 将右部规约成左部
                _symbolStack.push(p.getLeftPart());
                _currentState = _stateStack.peek();
                // 基于目前状态再分析一次
                if(!_symbolStack.peek().equals(BEGIN_SIGN))analysisAction(_currentState, _symbolStack.peek());
            }else if("eps".equals(rightPart.get(0))){
                _symbolStack.push(p.getLeftPart());
                _currentState = _stateStack.peek();
                analysisAction(_currentState,  _symbolStack.peek());
            }

            _actionMessageQueue.add("REDUCE ACTION: " + p);
        }catch(Exception e){
            System.out.println(e.toString());
        }
    }

    private void analysisAction(Integer currentState, String inputFront, String filePath){

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
