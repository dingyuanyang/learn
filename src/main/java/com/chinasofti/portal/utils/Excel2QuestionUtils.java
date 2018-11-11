package com.chinasofti.portal.utils;

import com.mongodb.BasicDBObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class Excel2QuestionUtils {
    private static String exName(File file) {
        String fileName = file.getName().toLowerCase();
        int lastIdx = fileName.lastIndexOf(".");
        return fileName.substring(lastIdx + 1);
    }

    /**
     * 获取EXCEL中的试题数据
     *
     * @param excel 试题文件
     * @return 试题列表，试题的(题型、难度、方向、子方向字段需要二次处理)
     * @throws Exception
     */
    public static List<BasicDBObject> getQuestions(File excel) throws Exception {
        FileInputStream fileIn = new FileInputStream(excel);
        //根据指定的文件输入流导入Excel从而产生Workbook对象
        Workbook workbook = null;
        if ("xls".equals(exName(excel))) {
            workbook = new HSSFWorkbook(fileIn);
        } else if ("xlsx".equals(exName(excel))) {
            workbook = new XSSFWorkbook(fileIn);
        } else {
            throw new Exception("未知的Excel类型");
        }
        //获取Excel文档中的第一个表单
        Sheet sheet = workbook.getSheetAt(0);
        //对Sheet中的每一行进行迭代
        List<BasicDBObject> questions = new ArrayList<>(sheet.getLastRowNum());
        Row titleRow = sheet.getRow(sheet.getFirstRowNum());
        for (Row row : sheet) {
            int rowNum = row.getRowNum();
            //跳过标题行
            if (rowNum < 1) {
                continue;
            }
            //创建实体类
            BasicDBObject question = new BasicDBObject();
            //取出当前行
            short lastCell = row.getLastCellNum();
            for (int i = 0; i < lastCell; i++) {
                String title = titleRow.getCell(i).getStringCellValue();
                Cell cell = row.getCell(i);
                if (cell == null) {
                    continue;
                }
                String val = cell.getStringCellValue();
                switch (title) {
                    case "试题内容":
                        if (val == null) {
                            throw new Exception((rowNum + 1) + "试题未添加内容:" + excel);
                        }
                        question.put("content", val);
                        break;
                    case "选项":
                        question.put("options", val);
                        break;
                    case "答案":
                        if (val == null) {
                            throw new Exception((rowNum + 1) + "试题未设置试题答案:" + excel);
                        }
                        question.put("answers", val);
                        break;
                    case "题组类型":
                        if (val == null) {
                            throw new Exception((rowNum + 1) + "试题未设置试题类型:" + excel);
                        }
                        question.put("questionType", val);
                        break;
                    case "难度":
                        question.put("difficulty", val);
                        break;
                    case "技术方向":
                        question.put("techType", val);
                        break;
                    case "子技术方向":
                        question.put("subTechType", val);
                        break;
                }
            }
            progressQuestion(question, rowNum, excel.getPath());
            questions.add(question);
        }
        fileIn.close();
        return questions;
    }

    private static void progressQuestion(BasicDBObject question, int rowNum, String excel) throws Exception {
        String optionStr = question.getString("options");
        String answerStr = question.getString("answers");
        String questionType = question.getString("questionType");
        String[] options = new String[0];
        String[] answers = null;
        switch (questionType) {
            case "单选题":
            case "判断题":
            case "多选题":
                if (optionStr == null) {
                    throw new Exception((rowNum + 1) + "未设置答案选项" + excel);
                }
                options = optionStr.split("\n");
                List<BasicDBObject> opts = new ArrayList<>(options.length);
                for (int optChar = 0; optChar < options.length; optChar++) {
                    BasicDBObject opt = new BasicDBObject("char", (char) (optChar + 65) + "")
                            .append("content", options[optChar].substring(2));
                    opts.add(opt);
                }
                answers = answerStr.split("");
                question.put("options", opts);
                question.put("answers", answers);
                break;
            case "填空题":
                answers = answerStr.split("\n");
                List<BasicDBObject> answerOpts = new ArrayList<>(answers.length);
                for (int optChar = 0; optChar < answers.length; optChar++) {
                    BasicDBObject opt = new BasicDBObject("char", (char) (optChar + 65) + "")
                            .append("content", answers[optChar]);
                    answerOpts.add(opt);
                }
                question.put("options", answerOpts);
                question.put("answers", answers);
                break;
            case "简答题":
                question.put("options", options);
                question.put("details", answerStr);
                question.put("answers", new String[]{answerStr});
                break;
        }
    }

    public static void main(String[] args) {
        File excel = new File("E:\\迅雷下载\\试题导入模板.xlsx");
        try {
            List<BasicDBObject> ques = getQuestions(excel);
            System.out.println(ques);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
