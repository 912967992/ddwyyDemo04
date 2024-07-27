package com.lu.ddwyydemo04.Service;

import com.lu.ddwyydemo04.controller.testManIndexController;
import com.lu.ddwyydemo04.dao.QuestDao;
import com.lu.ddwyydemo04.dao.SamplesDao;
import com.lu.ddwyydemo04.exceptions.ExcelOperationException;
import com.lu.ddwyydemo04.pojo.MergedCellInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Shape;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//此服务层主要用于填写测试报告页面的详细功能方法！
@Service
public class ExcelShowService {

    // 设置图片存放目录为根目录下的 imageDirectory
    // 图片存放目录的路径（C盘的imageDirectory文件夹）
    @Value("${file.storage.imagepath}")
    private String imagepath;

    private Path getImageLocationC(){
        return Paths.get(imagepath.replace("/","\\"));
    }

    private static final Logger logger = LoggerFactory.getLogger(testManIndexController.class);


    @Autowired
    private SamplesDao samplesDao;
    public List<String> getSheetNames(String file_path) {
        List<String> sheetNames = new ArrayList<>();
        try (InputStream is = new FileInputStream(file_path)) {
            Workbook workbook = WorkbookFactory.create(is);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sheetNames;
    }


    //这是未修改前的，用于点击工作表按钮之后获取该工作表的数据返回的
    public List<List<Object>> getSheetData(@RequestParam String sheetName,String file_path) {
        List<List<Object>> sheetData = new ArrayList<>();
        try (InputStream is = new FileInputStream(file_path)) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet != null) {
                // 获取合并区域的列表
                List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
                Drawing<?> drawing = sheet.createDrawingPatriarch();


                int lastRowNum = sheet.getLastRowNum();

                int col_width = 0;

                for (int rowNum = 0; rowNum <= lastRowNum; rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    List<Object> rowData = new ArrayList<>();
                    if (row != null) {
                        for (int colNum = 0; colNum < row.getLastCellNum(); colNum++) {
                            Cell cell = row.getCell(colNum, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            // 检查单元格的颜色
                            String colorStr = getColorAsString(cell);
                            String color = "";
                            if(getCellValue(cell).equals("初版234567890")){
                                System.out.println(colorStr);
                            }
                            // 检查颜色是否为蓝色,蓝色的为测试项
                            if ("0070C0".equals(colorStr)) {
                                // 检查单元格是否在合并区域内
                                CellRangeAddress mergedRegion = getMergedRegion(cell, mergedRegions);
                                color = "blue";

                                //获取cell的字符宽度
                                col_width = getCellWidth(cell,sheet,colNum);

                                getRowData(mergedRegion,cell,drawing,sheetName,rowNum,colNum,rowData,color,col_width,file_path);


                            }else if ("7030A0".equals(colorStr) || "FF0000".equals(colorStr)){ //检查颜色是否绿色或者红色，为测试结果
                                CellRangeAddress mergedRegion = getMergedRegion(cell, mergedRegions);
                                if("7030A0".equals(colorStr)){
                                    color = "purple";
                                }else{
                                    color = "red";
                                }

                                //获取cell的字符宽度
                                col_width = getCellWidth(cell,sheet,colNum);
//                                if(getCellValue(cell).equals("测试项目")){
//                                    System.out.println("测试项目"+col_width);
//                                }

                                getRowData(mergedRegion,cell,drawing,sheetName,rowNum,colNum,rowData,color,col_width,file_path);


                            }
                        }
                    }
                    // 即使行数据为空也添加到sheetData中
                    if (!rowData.isEmpty()) {
                        sheetData.add(rowData);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("读取文件失败", file_path, e);
            throw new ExcelOperationException(500, "读取文件失败");
        }
        return sheetData;
    }


    public List<List<List<Object>>> getAllSheetData(String filepath) {
        List<List<List<Object>>> allSheetData = new ArrayList<>();
        try (InputStream is = new FileInputStream(filepath)) {
            Workbook workbook = WorkbookFactory.create(is);
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
//                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = workbook.getSheetName(i); // 获取工作表名字
                List<List<Object>> sheetData = getSheetData(sheetName, filepath); // 调用 getSheetData 方法
                allSheetData.add(sheetData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allSheetData;
    }


    public int getCellWidth(Cell cell, Sheet sheet, int colNum) {

        // 默认情况下，获取单列的宽度
        double columnWidthInChars = sheet.getColumnWidth(colNum) / 256.0;
        int estimatedPixelWidth = (int) (columnWidthInChars * 6);

        // 检查单元格是否在合并单元格区域内
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(cell.getRowIndex(), colNum)) {
                columnWidthInChars = 0;
                for (int col = region.getFirstColumn(); col <= region.getLastColumn(); col++) {
                    columnWidthInChars += sheet.getColumnWidth(col) / 256.0;
                }
                estimatedPixelWidth = (int) (columnWidthInChars * 6);
                break;
            }
        }
//        System.out.println(getCellValue(cell)+estimatedPixelWidth);
        return estimatedPixelWidth;
    }



// 此方法只认第一列的图片，如果图片不是在第一列则解析不到
//    private List<Object> getRowData(CellRangeAddress mergedRegion,Cell cell,Drawing<?> drawing,String sheetName,int rowNum,int colNum,List<Object> rowData,String color,int col_width,String filepath){
//        if (mergedRegion != null) {
//            // 如果是合并区域的左上角单元格，则添加合并信息
//            if (cell.getRowIndex() == mergedRegion.getFirstRow() && cell.getColumnIndex() == mergedRegion.getFirstColumn()) {
//                if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == cell.getColumnIndex() && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
//                    //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
//                    String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, mergedRegion.getFirstColumn(),filepath);
//                    rowData.add(new MergedCellInfo(sheetName,imageLink,mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1, mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1,rowNum,colNum,color,col_width));
//                }else{
//                    rowData.add(new MergedCellInfo(sheetName,getCellValue(cell), mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1, mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1, rowNum, colNum,color,col_width));
//                }
//
//            }else if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == cell.getColumnIndex() && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
//                //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
//                String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, mergedRegion.getFirstColumn(),filepath);
//                MergedCellInfo lastItem = (MergedCellInfo) rowData.get(rowData.size()-1);
//                lastItem.setValue(imageLink);
//            }
//        } else {
//            // 非合并单元格，添加单元格值
//            if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == cell.getColumnIndex() && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
//                //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
//                String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, colNum,filepath);
//                rowData.add(new MergedCellInfo(sheetName,imageLink,1,1,rowNum,colNum,color,col_width));
//            }else{
//                rowData.add(new MergedCellInfo(sheetName,getCellValue(cell), 1, 1, rowNum, colNum,color,col_width));
//            }
//        }
//        return rowData;
//    }

    private List<Object> getRowData(CellRangeAddress mergedRegion,Cell cell,Drawing<?> drawing,String sheetName,int rowNum,int colNum,List<Object> rowData,String color,int col_width,String filepath){

        if (mergedRegion != null) {
            boolean imageFound = false;
            // 如果是合并区域的左上角单元格，则添加合并信息
            if (cell.getRowIndex() == mergedRegion.getFirstRow() && cell.getColumnIndex() == mergedRegion.getFirstColumn()) {

                for (int currentCol = mergedRegion.getFirstColumn(); currentCol <= mergedRegion.getLastColumn(); currentCol++) {
                    int finalCurrentCol = currentCol;
                    if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == finalCurrentCol && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
                        // 如果找到图片
                        imageFound = true;
                        //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
                        String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, mergedRegion.getFirstColumn(),filepath,finalCurrentCol);
                        rowData.add(new MergedCellInfo(sheetName,imageLink,mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1, mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1,rowNum,colNum,color,col_width));
                        break;
                    }
                }
                // 如果没有找到图片，则添加单元格信息
                if (!imageFound && cell.getRowIndex() == mergedRegion.getFirstRow() && cell.getColumnIndex() == mergedRegion.getFirstColumn()) {
                    rowData.add(new MergedCellInfo(sheetName, getCellValue(cell),
                            mergedRegion.getLastRow() - mergedRegion.getFirstRow() + 1,
                            mergedRegion.getLastColumn() - mergedRegion.getFirstColumn() + 1,
                            rowNum, colNum, color, col_width));
                }

            }
//            else if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == cell.getColumnIndex() && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
//                //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
//                String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, mergedRegion.getFirstColumn(),filepath,mergedRegion.getFirstColumn());
//                MergedCellInfo lastItem = (MergedCellInfo) rowData.get(rowData.size()-1);
//                lastItem.setValue(imageLink);
//            }
        } else {
            int finalCurrentCol = cell.getColumnIndex();
            // 非合并单元格，添加单元格值
            if (drawing instanceof XSSFDrawing && ((XSSFDrawing) drawing).getShapes().stream().anyMatch(s -> s instanceof XSSFPicture && ((XSSFPicture) s).getClientAnchor().getCol1() == cell.getColumnIndex() && ((XSSFPicture) s).getClientAnchor().getRow1() == cell.getRowIndex())) { // 检查单元格是否包含图片，并且单元格颜色标记为蓝色、绿色或红色
                //锚点：图片只认第一个最接近左边的第一个单元格，其他的统一认为不是图片的锚点！所以只有该图片的锚点（小的单元格）才能进这个方法
                String imageLink = saveImageFromCell(cell, drawing, sheetName, rowNum, colNum,filepath,finalCurrentCol);
                rowData.add(new MergedCellInfo(sheetName,imageLink,1,1,rowNum,colNum,color,col_width));
            }else{
                rowData.add(new MergedCellInfo(sheetName,getCellValue(cell), 1, 1, rowNum, colNum,color,col_width));
            }
        }
        return rowData;
    }






    private String getColorAsString(Cell cell) {
        if (cell != null && cell.getCellStyle() != null) {
            Workbook wb = cell.getSheet().getWorkbook();
            Font font = wb.getFontAt(cell.getCellStyle().getFontIndex());
            if (font instanceof XSSFFont) {
                XSSFFont xssfFont = (XSSFFont) font;
                XSSFColor color = xssfFont.getXSSFColor();
                if (color != null) {
                    byte[] rgb = color.getRGB();
                    if (rgb != null) {
                        return bytesToHex(rgb);
                    }
                }
            }
        }
        return "";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02X", b));
        }
        return builder.toString();
    }

    private CellRangeAddress getMergedRegion(Cell cell, List<CellRangeAddress> mergedRegions) {
        for (CellRangeAddress mergedRegion : mergedRegions) {
            if (mergedRegion.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                return mergedRegion;
            }
        }
        return null;
    }

    private String saveImageFromCell(Cell cell, Drawing<?> drawing, String sheetName, int rowIndex, int columnIndex,String filepath,Integer  finalCurrentCol) {
        try {
            // 检查单元格是否包含图片
            for (Shape shape : drawing) {
                if (shape instanceof Picture) {
                    Picture picture = (Picture) shape;
                    ClientAnchor anchor = picture.getClientAnchor();

//                    if (cell.getColumnIndex() == anchor.getCol1() && cell.getRowIndex() == anchor.getRow1()) {
                    if (finalCurrentCol == anchor.getCol1() && cell.getRowIndex() == anchor.getRow1()) {
//                    if (isImageInColumn && cell.getRowIndex() == anchor.getRow1()) {
                        // 获取图片数据
                        byte[] pictureData = picture.getPictureData().getData();

                        int lastIndex = filepath.lastIndexOf('\\');
                        String fileName = filepath.substring(lastIndex + 1);

                        // 生成图片文件名
                        String imageName = fileName + "_" + sheetName + "_" + rowIndex + "_" + columnIndex + ".png";

                        // 检查并创建目录
                        if (!Files.exists(getImageLocationC())) {
                            Files.createDirectories(getImageLocationC());
                        }

                        // 检查文件是否已存在
                        Path imagePathC = getImageLocationC().resolve(imageName);
                        if (!Files.exists(imagePathC)) {
                            // 保存图片到C盘目录
                            Files.write(imagePathC, pictureData);
                        }
                        // 返回图片文件路径（保持路径格式不变）
                        return "/imageDirectory/" + imageName;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        DecimalFormat df = new DecimalFormat("#");
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC://这里的处理是为了让解析的时候不自动转换为日期形式：2.0240527E7
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return df.format(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    public String saveEditedCell(String filePath, Map<String, Map<String, Object>> cellData) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {

            // 获取文件输入流并创建工作簿
            fileInputStream = new FileInputStream(filePath);
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

            // 处理单元格数据
            for (Map.Entry<String, Map<String, Object>> entry : cellData.entrySet()) {
                Map<String, Object> cellDetails = entry.getValue();
                String sheetName = (String) cellDetails.get("sheetName");
                int row = (int) cellDetails.get("row");
                int column = (int) cellDetails.get("column");
                String value = (String) cellDetails.get("value");

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    continue;
                }

                Row sheetRow = sheet.getRow(row);
                if (sheetRow == null) {
                    sheetRow = sheet.createRow(row);
                }

                Cell cell = sheetRow.getCell(column, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if (cell == null) {
                    cell = sheetRow.createCell(column);
                }

                if (value.startsWith("/imageDirectory/")) {
                    deleteImageFromCell(sheet, row, column);
                    insertImageToCell(workbook, sheet, value, row, column);
                    cell.setCellValue("");
                } else {
                    if ("NG".equals(value) || "FAIL".equals(value) || "×".equals(value)) {
                        setCellFontColor(cell, "FF0000");
                    } else {
                        setCellFontColor(cell, "7030A0");
                    }
                    cell.setCellValue(value);
                }

                if (cellDetails.containsKey("scrollHeight")) {
                    float scrollHeight = ((Number) cellDetails.get("scrollHeight")).floatValue();
                    sheetRow.setHeightInPoints(scrollHeight);
                }
            }

            // 关闭文件输入流
            fileInputStream.close();
            fileInputStream = null;

            // 使用文件输出流写入内容,如果有进程占用，则这里会报错，所以在这里要用try
            // 尝试获取文件输出流写入内容
            try {
                fileOutputStream = new FileOutputStream(filePath);
                workbook.write(fileOutputStream);

                // 关闭工作簿和文件输出流
                workbook.close();
                fileOutputStream.close();

                return "{\"status\": \"saved\"}";
            } catch (IOException e) {
                return "{\"status\": \"locked\", \"message\": \"文件正在被另一个进程使用\"}";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"status\": \"locked\", \"message\": \"文件正在被另一个进程使用\"}";
        } finally {
            try {

                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void deleteImageFromCell(Sheet sheet, int row, int column) {
        if (sheet instanceof XSSFSheet) {
            XSSFSheet xssfSheet = (XSSFSheet) sheet;
            XSSFDrawing drawing = xssfSheet.getDrawingPatriarch();

            // 检查绘图对象是否为空
            if (drawing == null) {
                return;
            }
            CTDrawing ctDrawing = drawing.getCTDrawing();
            List<CTTwoCellAnchor> anchors = new ArrayList<>(ctDrawing.getTwoCellAnchorList());

            // 获取点击单元格所属的合并区域
            CellRangeAddress mergedRegion = getMergedRegionForCell(sheet, row, column);

            // 创建一个新列表，用于保存需要删除的形状索引
            List<Integer> indicesToRemove = new ArrayList<>();

            // 遍历所有锚点（包含图片等形状）并检查它们的锚点
            for (int i = 0; i < anchors.size(); i++) {
                CTTwoCellAnchor anchor = anchors.get(i);

                // 打印图片的锚点信息
                System.out.println("图片锚点信息: Row1=" + anchor.getFrom().getRow() + ", Col1=" + anchor.getFrom().getCol() +
                        ", Row2=" + anchor.getTo().getRow() + ", Col2=" + anchor.getTo().getCol());

                boolean isInTargetCell = (anchor.getFrom().getRow() == row && anchor.getFrom().getCol() == column);

                // 检查图片锚点是否在当前单元格区域（合并或非合并）内
                if (mergedRegion != null) {
                    isInTargetCell = isInMergedRegion(anchor, mergedRegion);
                }

                if (isInTargetCell) {
                    indicesToRemove.add(i);
                }
            }

            // 删除所有需要删除的形状
            for (int index : indicesToRemove) {
                ctDrawing.removeTwoCellAnchor(index);
            }
        }
    }

    private void insertImageToCell(XSSFWorkbook workbook, Sheet sheet, String imagePath, int row, int column) {
        try {
            System.out.println("imagePath"+imagePath);

            // 将传入的路径解析为C盘imageDirectory目录下的绝对路径
            Path absoluteImagePath = getImageLocationC().resolve(imagePath.substring("/imageDirectory/".length())).normalize();
            System.out.println("imagePath (absolute): " + absoluteImagePath);

            InputStream is = new FileInputStream(absoluteImagePath.toFile());
            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
            is.close();

            Drawing<?> drawing = sheet.createDrawingPatriarch();
            CreationHelper helper = workbook.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();

            // 获取点击单元格所属的合并区域
            CellRangeAddress mergedRegion = getMergedRegionForCell(sheet, row, column);

            int firstCol = column;
            int lastCol = column;
            int numOfColumns = 0;

//            int numOfColumns = 0;

            // 计算合并单元格的列数和行数
            if(mergedRegion != null){
                firstCol = mergedRegion.getFirstColumn();
                lastCol = mergedRegion.getLastColumn();
                numOfColumns = lastCol + firstCol + 1;
            }
//            int firstCol = mergedRegion.getFirstColumn();
//            int lastCol = mergedRegion.getLastColumn();
//            numOfColumns = lastCol + firstCol + 1;

            anchor.setCol1(firstCol);

            if (mergedRegion != null) {
                anchor.setCol1(numOfColumns/2-1);
                anchor.setCol2(lastCol+1);
            }else{
                anchor.setCol1(firstCol);
                anchor.setCol2(lastCol);
            }


            // 设置图片插入的位置和大小

//            anchor.setCol1(firstCol);

            anchor.setRow1(row);
            anchor.setRow2(row+1);

//            anchor.setRow1(row);
//            anchor.setCol2(column + 1); // 图片占据一个单元格
//            anchor.setRow2(row + 1);


            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(); // 自动调整图片大小以适应单元格


            // 获取单元格的宽度和高度
            float cellWidth = sheet.getColumnWidthInPixels(column);
            float cellHeight = sheet.getRow(row).getHeightInPoints() / 72 * 96; // 将高度从点转换为像素

            // 获取图片的原始尺寸
            Dimension originalSize = pict.getImageDimension();
            float originalWidth = (float) originalSize.getWidth();
            float originalHeight = (float) originalSize.getHeight();

            float scaleWidth;
            float scaleHeight;
            float scale;
            scaleWidth = cellWidth / originalWidth;
            scaleHeight = cellHeight / originalHeight;

            if(mergedRegion!=null){

                scale  = (scaleWidth + scaleHeight) / 2;
            }else{

                scale = Math.min(scaleWidth, scaleHeight);
            }


            // 按照计算的缩放比例调整图片大小
            pict.resize(scale);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCellFontColor(Cell cell, String colorStr) {
        Workbook workbook = cell.getSheet().getWorkbook();
        CellStyle originalStyle = cell.getCellStyle(); // 保存当前单元格样式

        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setColor(new XSSFColor(hexToRgb(colorStr), null)); // 将十六进制颜色转换为RGB颜色并设置字体颜色
//        font.setFontHeightInPoints((short) 16);
        font.setFontName("宋体");

        CellStyle newStyle = workbook.createCellStyle();
        newStyle.cloneStyleFrom(originalStyle); // 复制当前单元格样式
        newStyle.setFont(font); // 设置新的字体颜色

        cell.setCellStyle(newStyle); // 应用新样式到单元格
    }

    private byte[] hexToRgb(String colorStr) {
        int r = Integer.valueOf(colorStr.substring(0, 2), 16);
        int g = Integer.valueOf(colorStr.substring(2, 4), 16);
        int b = Integer.valueOf(colorStr.substring(4, 6), 16);
        return new byte[]{(byte) r, (byte) g, (byte) b};
    }

    //    获取合并单元格区域
    private static CellRangeAddress getMergedRegionForCell(Sheet sheet, int row, int column) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(row, column)) {
                return region;
            }
        }
        return null;
    }


    private static boolean isInMergedRegion(CTTwoCellAnchor anchor, CellRangeAddress mergedRegion) {
        return anchor.getFrom().getRow() >= mergedRegion.getFirstRow() && anchor.getTo().getRow() <= mergedRegion.getLastRow() &&
                anchor.getFrom().getCol() >= mergedRegion.getFirstColumn() && anchor.getTo().getCol() <= mergedRegion.getLastColumn();
    }


    public Map<String, String> uploadImage(@RequestParam("image") MultipartFile imageFile,@RequestParam("row") int row,@RequestParam("column") int column,@RequestParam("sheetName") String sheetName,@RequestParam("filepath") String filepath) {

        Map<String, String> response = new HashMap<>();
        try {
            // 检查并创建目录
            if (!Files.exists(getImageLocationC())) {
                Files.createDirectories(getImageLocationC());
            }

            int lastIndex = filepath.lastIndexOf('\\');
            String fileName = filepath.substring(lastIndex + 1);

            // 获取图片文件名
            String imageName = fileName + "_" + sheetName + "_" + row + "_" + column + ".png";
            // 构建目标文件路径
            Path targetPath = getImageLocationC().resolve(imageName);
            System.out.println("targetPath: " + targetPath);
            // 将上传的图片保存到目标文件路径
            Files.copy(imageFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            // 构建图片访问路径
            String imageUrl = "/imageDirectory/" + imageName;

            // 返回上传成功的图片路径
            response.put("imageUrl", imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            logger.info("和文件保存起冲突，上传图片失败，请稍等5s后重试");
            // 返回上传失败的响应
            response.put("error", "和文件保存起冲突，上传图片失败，请稍等5s后重试");

        }
        return response;
    }



    public String insertRow(String sheetName,int rowNum,String file_path) {
        try (FileInputStream fileInputStream = new FileInputStream(file_path);
             XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(file_path)) {

            Sheet sheet = workbook.getSheet(sheetName);

            // 检查插入行的范围
            if (rowNum < 0 || rowNum > sheet.getLastRowNum()) {
                return "Invalid row number.";
            }

            // 向下移动行以腾出空间
            sheet.shiftRows(rowNum, sheet.getLastRowNum(), 1);

            // 创建新行
            Row newRow = sheet.createRow(rowNum);

            // 获取目标行
            Row targetRow = sheet.getRow(rowNum + 1);

            // 如果目标行存在，则复制格式
            Row sourceRow = sheet.getRow(rowNum + 1);
            if (sourceRow != null) {
                copyRowFormat(workbook, sourceRow, newRow);
                copyMergedRegions(sheet, sourceRow, newRow);

                // 检查目标行的单元格是否包含“上传图片”
                for (Cell cell : targetRow) {
                    if ("上传图片".equals(cell.getStringCellValue())) {
                        Cell newCell = newRow.createCell(cell.getColumnIndex());
                        newCell.setCellValue("上传图片");
                        newCell.setCellStyle(cell.getCellStyle()); // 复制单元格格式
                    }
                }

            }

            // 保存到文件
            workbook.write(fileOutputStream);
            logger.info("成功新增一行");
            return "成功新增一行";

        } catch (IOException e) {
            e.printStackTrace();
            return "新增一行有冲突，请稍等稍等5秒再试，谢谢！";
        }
    }

    private void copyRowFormat(XSSFWorkbook workbook, Row sourceRow, Row newRow) {
        // 复制行高
        newRow.setHeight(sourceRow.getHeight());

        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            Cell newCell = newRow.createCell(i);
            Cell sourceCell = sourceRow.getCell(i);

            if (sourceCell != null) {
                // 复制单元格样式
                CellStyle newCellStyle = workbook.createCellStyle();
                newCellStyle.cloneStyleFrom(sourceCell.getCellStyle());
                newCell.setCellStyle(newCellStyle);
            }
        }
    }

    private void copyMergedRegions(Sheet sheet, Row sourceRow, Row newRow) {
        int sourceRowNum = sourceRow.getRowNum();
        int newRowNum = newRow.getRowNum();
        List<CellRangeAddress> mergedRegions = new ArrayList<>();

        // 查找所有合并单元格
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
            if (mergedRegion.getFirstRow() == sourceRowNum && mergedRegion.getLastRow() == sourceRowNum) {
                mergedRegions.add(mergedRegion);
            }
        }

        // 复制合并单元格到新行
        for (CellRangeAddress mergedRegion : mergedRegions) {
            CellRangeAddress newMergedRegion = new CellRangeAddress(newRowNum,
                    newRowNum,
                    mergedRegion.getFirstColumn(),
                    mergedRegion.getLastColumn());
            sheet.addMergedRegion(newMergedRegion);
        }
    }

    public String deleteRow(String sheetName,int rowNum,String file_path) {
        try (FileInputStream fileInputStream = new FileInputStream(file_path)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
//            FileOutputStream fileOutputStream = new FileOutputStream(file_path);
            Sheet sheet = workbook.getSheet(sheetName);

            if(rowNum<0 || rowNum>sheet.getLastRowNum()){
                return "Invalid row number";
            }

            Row previousRow = sheet.getRow(rowNum - 1);
            if (isRowEffectivelyEmpty(previousRow)) {
                //为ture时证明上一行是空的，可以进行删除
                List<CellRangeAddress> mergedRegionsToRemove = getMergedRegions(sheet, rowNum - 1);
                for (CellRangeAddress region : mergedRegionsToRemove) {
                    sheet.removeMergedRegion(findMergedRegionIndex(sheet, region));
                }
                sheet.removeRow(previousRow);
                if (rowNum <= sheet.getLastRowNum()) {
                    sheet.shiftRows(rowNum, sheet.getLastRowNum(), -1);
                }
            }


            // 保存到文件
            try (FileOutputStream fileOutputStream = new FileOutputStream(file_path)) {
                workbook.write(fileOutputStream);
                logger.info("成功删除一行");
                return "成功删除一行";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "删除一行失败.";
        }
    }
    // 判断行是否实际为空（忽略“上传图片”单元格）
    private boolean isRowEffectivelyEmpty(Row row) {
        System.out.println("row:"+row);
        if (row == null) {
            return true;
        }
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK) {
                String cellValue = cell.getStringCellValue().trim();
                if (!"上传图片".equals(cellValue) && !cellValue.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
    private List<CellRangeAddress> getMergedRegions(Sheet sheet, int rowNum) {
        List<CellRangeAddress> mergedRegions = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.getFirstRow() == rowNum || region.getLastRow() == rowNum) {
                mergedRegions.add(region);
            }
        }
        return mergedRegions;
    }

    private int findMergedRegionIndex(Sheet sheet, CellRangeAddress region) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            if (sheet.getMergedRegion(i).equals(region)) {
                return i;
            }
        }
        return -1;
    }

    public int sampleCount(String model,String coding,String category,String version,int sample_frequency,String big_species,
                           String small_species,String high_frequency,String questStats){
        return samplesDao.sampleCount(model,coding,category,version,sample_frequency,big_species,small_species,high_frequency,questStats);
    }

    public int sampleOtherCount(String model,String coding,String high_frequency){
        return samplesDao.sampleOtherCount(model,coding,high_frequency);
    }


    public int insertSample(String tester, String filepath, String model, String coding, String category, String version, String sample_name, String  planfinish_time,String create_time,String sample_schedule,int sample_frequency,int sample_quantity,
                            String big_species,String small_species,String high_frequency,String questStats) {
        String full_model = model + " " + coding;
        return samplesDao.insertSample(tester,filepath,model,coding,full_model,category,version,sample_name,planfinish_time,create_time,sample_schedule,sample_frequency,sample_quantity,big_species,small_species,high_frequency,questStats);
    }

    public List<String> querySample(String model,String coding,String high_frequency){
        return samplesDao.querySample(model,coding,high_frequency);
    }


    public String getUUID(String filepath){
        return samplesDao.getUUID(filepath);
    }

    public int updateUUID(String filepath,String uuid){
        return samplesDao.updateUUID(filepath,uuid);
    }

}