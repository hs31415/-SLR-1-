import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductionConst {
    public static Map<Integer, Production> PRODUCTION_MAP = new HashMap<>();
    public static void testProductionMap(){
        PRODUCTION_MAP.put(0, new Production("expr_list1", new ArrayList<>(List.of(new String[]{"expr_list"}))));
        PRODUCTION_MAP.put(1, new Production("expr", new ArrayList<>(List.of(new String[]{"factor", "expr_tail"}))));
        PRODUCTION_MAP.put(2, new Production("factor", new ArrayList<>(List.of(new String[]{"primary", "factor_tail"}))));
        PRODUCTION_MAP.put(3, new Production("expr_list", new ArrayList<>(List.of(new String[]{"expr", "expr_list_tail"}))));
        PRODUCTION_MAP.put(4, new Production("expr_list_tail", new ArrayList<>(List.of(new String[]{",", "expr", "expr_list_tail"}))));
        PRODUCTION_MAP.put(5, new Production("expr_list_tail", new ArrayList<>(List.of(new String[]{"eps"}))));
        PRODUCTION_MAP.put(6, new Production("primary", new ArrayList<>(List.of(new String[]{"(", "expr", ")"}))));
        PRODUCTION_MAP.put(7, new Production("primary", new ArrayList<>(List.of(new String[]{"id"}))));
        PRODUCTION_MAP.put(8, new Production("primary", new ArrayList<>(List.of(new String[]{"INTLITERAL"}))));
        PRODUCTION_MAP.put(9, new Production("primary",new ArrayList<>(List.of(new String[]{"FLOATLITERAL"}))));
        PRODUCTION_MAP.put(10, new Production("addop", new ArrayList<>(List.of(new String[]{"+"}))));
        PRODUCTION_MAP.put(11, new Production("addop", new ArrayList<>(List.of(new String[]{"-"}))));
        PRODUCTION_MAP.put(12, new Production("mulop", new ArrayList<>(List.of(new String[]{"*"}))));
        PRODUCTION_MAP.put(13, new Production("mulop", new ArrayList<>(List.of(new String[]{"/"}))));
        PRODUCTION_MAP.put(14, new Production("expr_tail", new ArrayList<>(List.of(new String[]{"addop", "factor", "expr_tail"}))));
        PRODUCTION_MAP.put(15, new Production("expr_tail", new ArrayList<>(List.of(new String[]{"eps"}))));
        PRODUCTION_MAP.put(16, new Production("factor_tail", new ArrayList<>(List.of(new String[]{"mulop", "primary", "factor_tail"}))));
        PRODUCTION_MAP.put(17, new Production("factor_tail", new ArrayList<>(List.of(new String[]{"eps"}))));
    }

    // 0  1  2      3    4
    // 3　exp -> Repeat exp

    // 0  1  2      3
    // x a   ->     b
    public static void getProductionFromFile(String filePath){
        try{
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while(bufferedReader.ready()){
                String content = bufferedReader.readLine();
                ArrayList<String> dividedLine = new ArrayList<>(List.of(content.split(" ")));

                if(dividedLine.size() < 4) throw new Exception("Invalid input of productions: +" + content);
                ArrayList<String> rightPart = new ArrayList<>();
                for(int i = 3; i < dividedLine.size(); i++){
                    rightPart.add(dividedLine.get(i));
                }
                PRODUCTION_MAP.put(Integer.parseInt(dividedLine.get(0)), new Production(dividedLine.get(1), rightPart));
            }
        }catch(IOException e){
            System.out.println(e.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
