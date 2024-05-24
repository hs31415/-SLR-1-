import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProductionTable {
    private static File _tableFile;
    private static Workbook _workbook;
    private static FileInputStream _fileInputStream;
    public static void setTableFile(String filePath){
        try{
            _tableFile = new File(filePath);
            _fileInputStream = new FileInputStream(_tableFile);
            _workbook = WorkbookFactory.create(_fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
