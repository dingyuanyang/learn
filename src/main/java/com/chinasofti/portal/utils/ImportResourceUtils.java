package com.chinasofti.portal.utils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBRef;

import java.io.File;
import java.util.*;

/**
 * 导入资源的公共类
 * 只生产底层资源（教材）如果想导入课程，需要先产生教材，然后根据教材产生课程
 */
public class ImportResourceUtils {

    private  String basicDirectory = null;

    public ImportResourceUtils() {
    }
    private  boolean isEmpty(String string) {
        return true;
    }

    /**
     * 替换目录分割为“/”
     *
     * @param dir
     * @return
     */
    private  String replacePath(String dir) {
        return dir.replaceAll(basicDirectory, "").replaceAll("\\\\", "/");
    }

    /**
     * 创建获取当前对象的ID
     *
     * @param object
     * @return
     */
    private  String createId(BasicDBObject object) {
        if (isEmpty(object.getString("_id"))) {
            String id = "" + Math.random() * 10000;
            object.put("_id", id);
        }
        return object.getString("_id");
    }

    /**
     * 获取排序的文件列表
     *
     * @param directory 文件夹
     * @return
     */
    private  File[] orderByName(File directory) {
        File[] files = directory.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        return files;
    }

    /**
     * 遍历教材资源文件夹
     *
     * @param directory 教材目录
     * @return 教材对象
     * @throws Exception
     */
    public  BasicDBObject scanTextbookFile(File directory) throws Exception {
        if (directory == null) {
            throw new Exception("导入资源文件夹不存在");
        }
        basicDirectory = directory.getParent();
        BasicDBObject resource = new BasicDBObject();
        resource.put("textbookName", directory.getName());
        File[] textbookFiles = orderByName(directory);
        if (textbookFiles.length == 0) {
            throw new Exception("导入资源文件夹为空");
        }
        List<BasicDBObject> chapters = new ArrayList<BasicDBObject>(textbookFiles.length);
        int chapIdx = 1;
        for (File file : textbookFiles) {
            if (file.isFile()) {
                // 判断是JSON 还是图片
                if (isImg(file)) {
                    String cover = file.getName();
                    resource.put("textbookCover", cover);
                } else if (isJson(file)) {
                    // 校验JSON 按照JSON导入
                    return checkJson(file);
                }
            } else {
                // 章节文件夹
                //  遍历章
                BasicDBObject chapter = scanChapterFile(file);
                // 处理章节名称
                String chapterName = getChapterName(chapIdx, file.getName());
                chapter.put("chapterName", chapterName);
                chapters.add(chapter);
                chapIdx++;
            }
        }
        resource.put("chapters", chapters);
        createId(resource);
        return resource;
    }


    /**
     * 浏览章节文件夹
     *
     * @param directory 章文件夹
     * @return 当前章信息
     * @throws Exception
     */
    private  BasicDBObject scanChapterFile(File directory) throws Exception {
        BasicDBObject chapter = new BasicDBObject();
        File[] chapterFiles = orderByName(directory);
        if (chapterFiles.length == 0) {
            return chapter;
        }
        List<BasicDBObject> sections = new ArrayList<BasicDBObject>(chapterFiles.length);
        int sectionIdx = 1;
        for (File file : chapterFiles) {
            if (file.isFile()) {
                //判断是PDF还是Excel
                if (isPdf(file)) {
                    String pdfPath = replacePath(file.getAbsolutePath());
                    chapter.put("scr", pdfPath);
                    chapter.put("chapterPptName", file.getName());
                } else if (isExcel(file)) {
                    // EXCEL的试题导入
                    getChapterExcelQues(chapter, file);
                }
            } else {
                // 章节文件夹
                if ("附件".equals(file.getName())) {
                    List<BasicDBObject> attachments = scanAttachFiles(file);
                    chapter.put("attachments", attachments);
                } else {
                    //  遍历节
                    BasicDBObject section = scanSectionFile(file);
                    String sectionName = getSectionName(sectionIdx, file.getName());
                    // 处理节名称
                    section.put("chapterName", sectionName);
                    sectionIdx++;
                    sections.add(section);
                }
            }
        }
        chapter.put("sections", sections);
        createId(chapter);
        return chapter;
    }

    private  List<BasicDBObject> scanAttachFiles(File directory) {
        File[] attachFiles = orderByName(directory);
        List<BasicDBObject> attachments = new ArrayList<>(attachFiles.length);
        for (File file : attachFiles) {
            String src = replacePath(file.getAbsolutePath());
            // 构建附件资料
            BasicDBObject attachment = new BasicDBObject("src", src)
                    .append("size", file.length())
                    .append("fileName", file.getName());
            createId(attachment);
            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * 浏览节文件夹
     *
     * @param directory 节文件夹
     * @return 当前节信息
     * @throws Exception
     */
    private  BasicDBObject scanSectionFile(File directory) throws Exception {
        BasicDBObject section = new BasicDBObject();
        File[] sectionFiles = orderByName(directory);
        if (sectionFiles.length == 0) {
            return section;
        }
        List<BasicDBObject> points = new ArrayList<BasicDBObject>(sectionFiles.length);
        int pointIdx = 1;
        for (File file : sectionFiles) {
            if (file.isFile()) {
                //判断是TXT
                if (isTxt(file)) {
                    String page = file.getName();
                    section.put("sectionPage", page);
                } else if (isMp4(file)) {
                    if (!isH264(file)) {
                        throw new Exception(file + "：不是H264的编码");
                    }
                    BasicDBObject point = new BasicDBObject();
                    //  处理知识点名称
                    String pointName = getPointName(pointIdx, file.getName());
                    pointName = pointName.substring(0, pointName.lastIndexOf("."));
                    point.put("pointName", pointName);
                    BasicDBObject video = getVideo(file);
                    point.put("video", video);
                    createId(point);
                    pointIdx++;
                    points.add(point);
                }
            }
        }
        section.put("points", points);
        createId(section);
        return section;
    }

    /**
     * 获取视频信息
     *
     * @param file 视频文件
     * @return 当前视频信息
     */
    private  BasicDBObject getVideo(File file) {
        String path = replacePath(file.getAbsolutePath());
        BasicDBObject video = new BasicDBObject();
        video.put("src", path);
        video.put("fileName", file.getName());
        createId(video);
        return video;
    }

    /**
     * 获取当前章节的题库
     *
     * @param chapter 章
     * @param file    excel文件
     */
    private  void getChapterExcelQues(BasicDBObject chapter, File file) throws Exception {
        String fileName = file.getName().toLowerCase();
        if ("练习题.xls".equals(fileName) || "练习题.xlsx".equals(fileName)) {
            // 添加练习题
            List<BasicDBObject> questions = getExcelQues(file);
            chapter.put("exercises", questions);
        } else if ("考试题.xls".equals(fileName) || "考试题.xlsx".equals(fileName)) {
            //  添加考试题
            List<BasicDBObject> questions = getExcelQues(file);
            chapter.put("exams", questions);
        }
    }

    /**
     * EXCEL导入解析试题
     *
     * @param excel
     * @return
     */
    private  List<BasicDBObject> getExcelQues(File excel) throws Exception {
        //TODO 获取Excel中的试题以后处理
        List<BasicDBObject> questions = Excel2QuestionUtils.getQuestions(excel);
        return questions;
    }

    private  String getName(int idx, String name, String type) {
        String exIdx = idx < 10 ? "0" + idx : idx + "";
        String[] heads = new String[]{
                "第" + idx + type,
                idx + type,
                idx + "",
                idx + ",",
                idx + "、",
                idx + "，",
                "第" + exIdx + type,
                exIdx + type,
                exIdx + "",
                exIdx + ",",
                exIdx + "、",
                exIdx + "，",
        };
        for (String head : heads) {
            if (name.startsWith(head)) {
                return name.replace(head, "").trim();
            }
        }
        return null;
    }

    private  String getChapterName(int idx, String name) {
        return getName(idx, name, "章");
    }

    private  String getSectionName(int idx, String name) {
        return getName(idx, name, "节");
    }

    private  String getPointName(int idx, String name) {
        return getName(idx, name, "知识点");
    }

    private  String exName(File file) {
        String fileName = file.getName().toLowerCase();
        int lastIdx = fileName.lastIndexOf(".");
        return fileName.substring(lastIdx + 1);
    }

    private  boolean isExcel(File file) {
        String exName = exName(file);
        return "xls".equals(exName) || "xlsx".equals(exName);
    }

    private  boolean isJson(File file) {
        String exName = exName(file);
        return "json".equals(exName);
    }

    private  boolean isImg(File file) {
        String exName = exName(file);
        switch (exName) {
            case "png":
            case "jpg":
            case "gif":
            case "jpeg":
                return true;
        }
        return false;
    }

    private  boolean isPdf(File file) {
        String exName = exName(file);
        return "pdf".equals(exName);
    }


    private  boolean isH264(File file) {
        // TODO 校验视频文件是否是H264
        return false;
    }

    private  boolean isMp4(File file) {
        String exName = exName(file);
        return "mp4".equals(exName);
    }

    private  boolean isTxt(File file) {
        String exName = exName(file);
        return "txt".equals(exName);
    }


    /**
     * 检查当前的JSON文件是否可以入库
     *
     * @param file 文件
     * @return
     */
    private  BasicDBObject checkJson(File file) throws Exception {
        BasicDBObject resource = new BasicDBObject();
        String cover = resource.getString("textbookCover");
        //检查照片
        if (!exists(cover)) {
            throw new Exception("封面不存在");
        }
        List<BasicDBObject> chapters = (List<BasicDBObject>) resource.get("chapters");
        if (activeList(chapters)) {
            //遍历章
            for (int chapIdx = 0; chapIdx < chapters.size(); chapIdx++) {
                BasicDBObject chapter = chapters.get(chapIdx);
                String pdf = chapter.getString("src");
                if (!exists(pdf)) {
                    throw new Exception("第" + (chapIdx + 1) + "章 pdf文件不不存在：" + pdf);
                }
                List<BasicDBObject> sections = (List<BasicDBObject>) chapter.get("sections");
                if (activeList(sections)) {
                    // 遍历节
                    for (int sectionIdx = 0; sectionIdx < sections.size(); sectionIdx++) {
                        BasicDBObject section = sections.get(sectionIdx);
                        List<BasicDBObject> points = (List<BasicDBObject>) section.get("points");
                        if (activeList(points)) {
                            // 遍历知识点
                            for (int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
                                BasicDBObject point = points.get(pointIdx);
                                BasicDBObject video = (BasicDBObject) point.get("video");
                                if (video != null) {
                                    String path = video.getString("src");
                                    if (!exists(path)) {
                                        throw new Exception("第" + (chapIdx + 1) + "章" + (sectionIdx + 1) + "节" + (pointIdx + 1) + "知识点视频不存在：" + path);
                                    }
                                    File videoFile = new File(basicDirectory, path);
                                    if (!isH264(videoFile)) {
                                        throw new Exception("第" + (chapIdx + 1) + "章" + (sectionIdx + 1) + "节" + (pointIdx + 1) + "知识点视频不是H264文件：" + path);
                                    }
                                }
                                // end points 循环
                            }
                        }
                        //end sections 循环
                    }
                }
                //遍历试题
                List<BasicDBObject> exercises = (List<BasicDBObject>) chapter.get("exercises");
                checkQuestions(chapIdx, exercises, "作业题");
                //遍历练习题
                List<BasicDBObject> exams = (List<BasicDBObject>) chapter.get("exams");
                checkQuestions(chapIdx, exams, "考试题");
                //遍历附件
                List<BasicDBObject> attachments = (List<BasicDBObject>) chapter.get("attachments");
                if (activeList(attachments)) {
                    for (int i = 0; i < attachments.size(); i++) {
                        BasicDBObject attach = attachments.get(i);
                        String src = attach.getString("src");
                        if (!exists(src)) {
                            throw new Exception("第" + (chapIdx + 1) + "章 附件资料不存在：" + src);
                        }
                    }
                }
                List<BasicDBObject> knowledgeBooks = (List<BasicDBObject>) chapter.get("knowledge");
                if(activeList(knowledgeBooks)){
                    List<DBRef> refs = new ArrayList<>(knowledgeBooks.size());
                    for (BasicDBObject knowledgeBook : knowledgeBooks) {
                        // TODO 构建知识点教材
                        DBRef ref = null;
                        refs.add(ref);
                    }
                }
                //end chapters 循环
            }
        }
        return resource;
    }

    private  void checkQuestions(int chapIdx, List<BasicDBObject> questions, String type) throws Exception {
        if (activeList(questions)) {
            for (BasicDBObject ques : questions) {
                String src = ques.getString("src");
                if (!exists(src)) {
                    throw new Exception("第" + (chapIdx + 1) + "章 " + type + "附件不存在：" + src);
                }
            }
        }
    }

    private  boolean activeList(List list) {
        return list != null && !list.isEmpty();
    }

    private  boolean exists(String path) {
        if (isEmpty(path)) {
            return false;
        }
        File file = new File(basicDirectory, path);
        return file.exists();
    }

    public static void main(String[] args) {
        File directory = new File("E:\\学习文件夹\\openstack实战演练与开发入门");
        try {
            ImportResourceUtils importResourceUtils = new ImportResourceUtils();
            BasicDBObject textbook = importResourceUtils.scanTextbookFile(directory);
            System.out.println(textbook);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
