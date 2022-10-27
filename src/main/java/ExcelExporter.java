import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExcelExporter {

    private final Workbook workbook = new XSSFWorkbook();
    private ArrayList<Sheet> sheets = new ArrayList<>();
    private String DatFile;

    public ExcelExporter(String DatFile) {
        this.DatFile = DatFile;
    }
    public void addSheet() {

        Sheet sheet = workbook.createSheet("TinyGP");
        sheets.add(sheet);
        sheet.setColumnWidth(0, 6000);
        sheet.setColumnWidth(1, 4000);
    }

    public void putDATColumns() throws IOException {
        Sheet sheet = sheets.get(0);
        Row row_header = sheet.createRow(0);
        row_header.createCell(0).setCellValue("X");
        row_header.createCell(1).setCellValue("f(X)");
        int lineNO = 0;

        try (LineIterator it = FileUtils.lineIterator(new File(DatFile), "UTF-8")) {

            while (it.hasNext()) {
                String line = it.nextLine();
                if (lineNO == 0) {
                    lineNO++;
                    continue;
                }
                Row row = sheet.createRow(lineNO);
                String[] lineContent = line.split(" ");
                row.createCell(0).setCellValue(lineContent[0]);
                row.createCell(1).setCellValue(lineContent[1]);
                lineNO++;
            }
        }
    }
    public void exportToFile(String filePath) {

        try {
//            File curDir = new File(".");
//            String path = curDir.getAbsolutePath();
//            String location = path.substring(0, path.length() - 1);
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
