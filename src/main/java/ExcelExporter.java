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

        int lineNO = 0;

        try (LineIterator it = FileUtils.lineIterator(new File(DatFile), "UTF-8")) {
            String header = it.nextLine();
            String[] headerContent = header.split(" ");
            int variables = Integer.parseInt(headerContent[0]);
            for (int i = 0; i < variables; i++)
                row_header.createCell(i).setCellValue("X" + i);
            row_header.createCell(variables).setCellValue("f(X)");
            lineNO++;

            while (it.hasNext()) {
                String line = it.nextLine();

                Row row = sheet.createRow(lineNO);
                String[] lineContent = line.split(" ");
                for (int i = 0; i <= variables; i++)
                    row.createCell(i).setCellValue(lineContent[i]);
                lineNO++;
            }
        }
    }
    public void exportToFile(String filePath) {

        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
