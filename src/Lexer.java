import consts.ConstKeywords;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
// import java.util.Stack;

class Symbol {
    private String type;
    private String value;
    private String arrTmp;

    public Symbol(String type, String value,String arrTmp) {
        this.type = type;
        this.value = value;
        this.arrTmp = arrTmp;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getArrTmp(){return arrTmp;}

    public void printSymbol() {
        System.out.println(type + " : " + value);
    }

    @Override
    public String toString() {
        return type + ":" + value + ":" + arrTmp;
    }
}

public class Lexer {
    private int index = 0;
    private int syn;
    private int sum;
    private String token;
    private int tag = 1;
    private boolean hasDecimalPoint = false;
    private boolean isReal = false;
    private ArrayList<String> errorList = new ArrayList<String>();
    private ArrayList<String> outList = new ArrayList<String>();
    private HashSet<String> symSet = new HashSet<String>();
    public ArrayList<Symbol> symbolArrayList = new ArrayList<>();
    // private Stack<String> closeToken = new Stack<String>();
    // private Stack<Integer> closeLineIndex = new Stack<Integer>();

    public Lexer() { };

    private Boolean IsDigit(char c){
        return Character.isDigit(c);
    }

    private Boolean IsLetter(char c){
        return Character.isLetter(c);
    }

    private int GetKeywordIndex(String s) {
        for(int i = 0; i < ConstKeywords.KEYWORDS.length; i++){
            if(s.equals(ConstKeywords.KEYWORDS[i])){
                return i;
            }
        }
        return -1;
    }

    public void GetInput(String inPath, String outPath, String symPath, String errorPath){
        try{
            int lineIndex = 0;
            Scanner sc = new Scanner(new FileReader(inPath));
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                System.out.println(line.length());
                index = 0;
                while(index < line.length()){
                    ScanLine(line, lineIndex);
                    switch(syn){
                        case -1:
                            System.out.println(errorList.get(errorList.size() - 1) );
                            syn = 0;
                            break;
                        case -2:
                            break;
                        default:
                            if(syn != 0){
                                Symbol symObj = new Symbol("", "","");
                                String outString = "";
                                if(syn == 10){
                                    symObj = new Symbol("id", token,"");
                                    String symString = "IDENTIFIER " + token + "\n";
                                    if (!symSet.contains(symString)) {
                                        symSet.add(symString);
                                    }
                                    outString = "id" + ":" + token;
                                }else if(syn == 20){
                                    symObj = new Symbol("int", token,"");
                                    outString = "int" + ":" + token;
                                }else if(syn == 21){
                                    symObj = new Symbol("real", token,"");
                                    outString = "float" + ":" + token;
                                }else if(syn == 22){
                                    symObj = new Symbol("int", token,"");
                                    outString = "num" + ":" + token;
                                }
                                else{
                                    symObj = new Symbol(token, "","");
                                    outString = syn + token ;
                                }
                                symbolArrayList.add(symObj);
                                symObj.printSymbol();
                                outList.add(outString + "\n");
                            }
                    }
                    if(index >= line.length()) break;
                }
                lineIndex++;
            }
            sc.close();
            WriteError(errorPath);
            WriteOut(outPath);
            WriteSym(symPath);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void WriteOut(String outPath) throws Exception {
        FileWriter fw = new FileWriter(outPath);
        for (String string : outList) {
            fw.write(string);
        }
        fw.close();
    }

    private void WriteSym(String symPath) throws Exception {
        FileWriter fw = new FileWriter(symPath);
        for (String string : ConstKeywords.KEYWORDS) {
            fw.write("KEYWORD " + string + "\n");
        }
        for (String string : symSet) {
            fw.write(string);
        }
        fw.close();
    }

    private void WriteError(String errorPath) throws Exception {
        FileWriter fw = new FileWriter(errorPath);
        for (String string : errorList) {
            fw.write(string);
        }
        fw.close();
    }

    private void ScanLine(String s, int lineIndex) throws Exception {
        final int lineLen = s.length();
        if(index >= lineLen) return;
        if(s.charAt(index) == ' '||s.charAt(index) =='\t'){
            syn = -2;
            index++;
        }else if(s.charAt(index) == '.'){
            hasDecimalPoint = true;
            index++;
        }
        else{
            token = "";
            if(s.charAt(index) == '#'){
                index = lineLen;
                return;
            }
            // 1. 判断字符是否为数字
            if(IsDigit(s.charAt(index))){
                token = "";
                sum = 0;
                while(index < lineLen && IsDigit(s.charAt(index))){
                    sum = (sum * 10) + (s.charAt(index) - '0');
                    index++;
                    if(index >= lineLen) break;
                    syn = 20;
                    if(s.charAt(index) == '.'){
                        index ++;
                        token += sum + ".";
                        sum = 0;

                        isReal = true;
                    }
                }
                if(isReal){
                    syn = 21;
                    isReal = false;
                }else if(sum == 0){
                    syn = 22;
                }
                if(hasDecimalPoint){
                    syn = 21;
                    isReal = false;
                    String sumStr = String.valueOf(sum);
                    token += "0."+sum;
                    if(!isReal){
                        syn = 21;
                    }
                    hasDecimalPoint = false;
                }else{
                    token += sum;
                }
            }
            // 2. 符号为字符串
            else if(IsLetter(s.charAt(index))){
                token = "";
                while(index < lineLen && (IsDigit(s.charAt(index)) || IsLetter(s.charAt(index)))){
                    token = token + s.charAt(index);
                    index++;
                }
                syn = (GetKeywordIndex(token) == -1 ? 10 : GetKeywordIndex(token) + 1);
            }
            else{
                token = "";
                switch(s.charAt(index)){
                    case '=':
                        syn = 31;
                        index++;
                        token = "=";
                        break;
                    case '+':
                        syn = 32;
                        index++;
                        token = "+";
                        break;

                    case '-':
                        syn = 33;
                        index++;
                        token = "-";
                        break;
                    case '*':
                        syn = 34;
                        index++;
                        token = "*";
                        break;

                    case '/':
                        syn = 35;
                        index++;
                        token = "/";
                        break;

                    case '(':
                        syn = 36;
                        index++;
                        token = "(";
                        break;

                    case ')':
                        syn = 37;
                        index++;
                        token = ")";
                        break;

                    case '[':
                        syn = 38;
                        index++;
                        token = "[";
                        break;

                    case ']':
                        syn = 39;
                        index++;
                        token = "]";
                        break;

                    case '{':
                        syn = 40;
                        index++;
                        token = "{";
                        break;

                    case '}':
                        syn = 41;
                        index++;
                        token = "}";
                        break;

                    case ',':
                        syn = 42;
                        index++;
                        token = ",";
                        break;

                    case ':':
                        syn = 43;
                        index++;
                        token = ":";
                        break;
                    case ';':
                        syn = 44;
                        index++;
                        token = ";";
                        break;
                    case '>':
                        syn = 45;
                        index++;
                        token = ">";
                        break;

                    case '<':
                        syn = 46;
                        index++;
                        token = "<";
                        break;
                    case '!':
                        syn = 50;
                        index++;
                        break;
                    case '|':
                        syn = 51;
                        index++;
                        break;
                    case '&':
                        syn = 52;
                        index++;
                        token = "&";
                        break;
//                    case '\t':
//                        syn = -1;
//                        index ++;
//                        break;
                    case '%':
                        syn = 43;
                        index++;
                        token = "%";
                        break;
                    case '"':
                        syn = -1;
                        token += s.charAt(index);
                        index++;
                        while(index < lineLen && s.charAt(index) != '"'){
                            token += s.charAt(index);
                            index++;
                            tag = 1;
                        }
                        if(index == lineLen){
                            tag = 0;
                            index++;
                        }
                        if(tag == 1){
                            token += s.charAt(index);
                            index++;
                            syn = 60;
                            break;
                        }
                        else{
                            errorList.add("Line: " + (lineIndex + 1) + " Quote didn't match."+ "\n");
                            outList.add("Line: " + (lineIndex + 1) + " Quote didn't match."+ "\n");
                        }
                    case '\''://单引号只能声明单字符
                        syn = -1;
                        token += s.charAt(index);
                        index++;
                        if(index<lineLen && s.charAt(index) != '\''){
                            token += s.charAt(index);
                            index++;
                        }else if(s.charAt(index) == '\''){
                            token = "' '";
                            index++;
                            syn = 60;
                            break;
                        }
                        if(index<lineLen && s.charAt(index) != '\''){
                            token += s.charAt(index);
                            index = lineLen;
                        }else if(s.charAt(index) == '\''){
                            tag = 1;
                        }
                        if(index == lineLen){
                            tag = 0;
                            index++;
                        }
                        if(tag == 1){
                            token += s.charAt(index);
                            index++;
                            syn = 60;
                            break;
                        }
                        else{
                            errorList.add("Line: " + (lineIndex + 1) + " Single Quote didn't match."+ "\n");
                            outList.add("Line: " + (lineIndex + 1) + " Single Quote didn't match."+ "\n");
                        }
                    default:
                        if(index > lineLen) break;
                        syn = -1;
                        errorList.add("Line: " + (lineIndex + 1) + " Invalid token."+ "\n");
                        outList.add("Line: " + (lineIndex + 1) + " Invalid token."+ "\n");
                        index++;
                }
            }
        }
    }
}
