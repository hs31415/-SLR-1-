import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProductionTable {
    private static File _tableFile;
    private static Workbook _workbook;
    private static Sheet _sheet;
    private static FileInputStream _fileInputStream;
    private static Map<String, Integer> _symbolMap;
    public static void setTableFile(String filePath){
        try{
            _tableFile = new File(filePath);
            _fileInputStream = new FileInputStream(_tableFile);
            _workbook = WorkbookFactory.create(_fileInputStream);
            _sheet = _workbook.getSheetAt(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void initSymbolMap() throws IOException {
        if(null == _workbook) throw new IOException("Workbook is not properly set!");
        if(null == _symbolMap) _symbolMap = new HashMap<>();

        Row row = _sheet.getRow(1);
        int column = row.getLastCellNum();
        for(int i = 1; i < column; i++){
            Cell cell = row.getCell(i);
            String cellValue = cell.getStringCellValue();

            _symbolMap.put(cellValue, i);
//            System.out.println(cellValue);
        }
    }

    public static Pair<String, Integer> cellValueParser(String s){
        if("".equals(s)) throw new NullPointerException("S is Empty!");
        int num ;
        String action = "";
        try{
            num = Integer.parseInt(s);
            return new Pair<>("Goto", num);
        }catch(Exception e){
            if(s.length() > 1) num = Integer.parseInt(s.substring(1));
            else num = -1;
            action = "";
            switch(s.charAt(0)){
                case 's':
                    action = "Shift";
                    break;
                case 'r':
                    action = "Reduce";
                    break;
                case 'p':
                    action = "Predict";
                    break;
            }
        }
        return new Pair<>(action, num);
    }
    public static Pair<String, Integer> getCellValue(String symbol, int state){
        Row row = null;
        row = _sheet.getRow(2 + state);
        Cell cell = row.getCell(_symbolMap.get(symbol));

//        return cellValueParser(cell.getStringCellValue());
        return cellValueParser(new DataFormatter().formatCellValue(cell));
    }

    public static void closeFileStream(){
        try{
            _workbook.close();
            _fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
