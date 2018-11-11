package com.chinasofti.portal.utils;

import com.mongodb.BasicDBObject;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.util.List;

public class ExportResourceUtils {

    private static final String FTP_EXPORT_PATH = "E:\\学习文件夹\\export";
    private static final String RESOURCE_PATH = "E:\\学习文件夹";

    private static boolean isEmpty(String string) {
        return true;
    }

    private static String exName(String fileName) {
        int lastIdx = fileName.lastIndexOf(".");
        return fileName.substring(lastIdx);
    }

    private static void copyFile(String resource, String export) throws Exception {
        if (!exists(resource)) {
            throw new Exception("导出的资源不存在");
        }
        File src = new File(RESOURCE_PATH, resource);
        File dest = new File(FTP_EXPORT_PATH, export);
        FileCopyUtils.copy(src, dest);
    }

    private static boolean activeList(List list) {
        return list != null && !list.isEmpty();
    }

    private static boolean exists(String path) {
        if (isEmpty(path)) {
            return false;
        }
        File file = new File(RESOURCE_PATH, path);
        return file.exists();
    }

    public static BasicDBObject exportTextbook(BasicDBObject textbook) throws Exception {
        if (textbook == null) {
            return null;
        }
        String cover = textbook.getString("textbookCover");
        String name = textbook.getString("textbookName");
        StringBuilder coverExport = new StringBuilder(name);
        coverExport.append("/");
        coverExport.append(name);
        coverExport.append(exName(cover));
        copyFile(cover, coverExport.toString());
        List<BasicDBObject> chapters = (List<BasicDBObject>) textbook.get("chapters");
        if (activeList(chapters)) {
            exportChapters(name, chapters);
        }
        // 导出完成，输出JSON到文件
        File resultJson = new File(FTP_EXPORT_PATH,"/"+name+"/"+name+".json");

        return textbook;
    }

    public static BasicDBObject exportCourse(BasicDBObject course) throws Exception {
        if (course == null) {
            return null;
        }
        String cover = course.getString("courseCover");
        String name = course.getString("courseName");
        StringBuilder coverExport = new StringBuilder(name);
        coverExport.append("/");
        coverExport.append(name);
        coverExport.append(exName(cover));
        copyFile(cover, coverExport.toString());
        List<BasicDBObject> chapters = (List<BasicDBObject>) course.get("chapters");
        if (activeList(chapters)) {
            exportChapters(name, chapters);
        }
        return course;
    }

    private static void exportChapters(String name, List<BasicDBObject> chapters) throws Exception {
        StringBuilder chapterPath = new StringBuilder();
        StringBuilder pointPath = new StringBuilder();
        //遍历章
        for (int chapIdx = 0; chapIdx < chapters.size(); chapIdx++) {
            BasicDBObject chapter = chapters.get(chapIdx);
            String pdf = chapter.getString("chapterPpt");
            if (!exists(pdf)) {
                throw new Exception("第" + (chapIdx + 1) + "章 pdf文件不不存在：" + pdf);
            }
            String chapName = chapter.getString("chapterName");
            chapterPath.delete(0, chapterPath.length());
            chapterPath.append("/");
            chapterPath.append(name);
            chapterPath.append("/第");
            chapterPath.append(chapIdx);
            chapterPath.append("章");
            chapterPath.append(chapName);
            chapterPath.append("/");
            String pdfSrc = chapterPath.toString() + chapName + exName(pdf);
            //copy 章节pdf
            copyFile(pdf, pdfSrc);
            chapter.put("src", pdfSrc);
            chapter.removeField("chapterPpt");
            List<BasicDBObject> sections = (List<BasicDBObject>) chapter.get("sections");
            if (activeList(sections)) {
                // 遍历节
                for (int sectionIdx = 0; sectionIdx < sections.size(); sectionIdx++) {
                    BasicDBObject section = sections.get(sectionIdx);
                    String sectionName = section.getString("sectionName");
                    List<BasicDBObject> points = (List<BasicDBObject>) section.get("points");
                    if (activeList(points)) {
                        // 遍历知识点
                        for (int pointIdx = 0; pointIdx < points.size(); pointIdx++) {
                            BasicDBObject point = points.get(pointIdx);
                            String pointName = point.getString("pointName");
                            BasicDBObject video = (BasicDBObject) point.get("video");
                            if (video != null) {
                                String path = video.getString("path");
                                if (!exists(path)) {
                                    throw new Exception("第" + (chapIdx + 1) + "章" + (sectionIdx + 1) + "节" + (pointIdx + 1) + "知识点视频不存在：" + path);
                                }
                                pointPath.delete(0, pointPath.length());
                                pointPath.append(chapterPath);
                                pointPath.append("第");
                                pointPath.append(sectionIdx);
                                pointPath.append("节");
                                pointPath.append(sectionName);
                                pointPath.append("/");
                                pointPath.append("第");
                                pointPath.append(pointIdx);
                                pointPath.append("知识点");
                                pointPath.append(pointName);
                                pointPath.append(exName(path));
                                String videoExport = pointPath.toString();
                                copyFile(path, videoExport);
                                point.put("src", videoExport);
                                point.removeField("path");
                            }
                            // end points 循环
                        }
                    }
                    //end sections 循环
                }
            }
            //遍历试题
            List<BasicDBObject> exercises = (List<BasicDBObject>) chapter.get("exercises");
            checkQuestions(chapIdx, exercises, "作业题", chapterPath.toString());
            //遍历练习题
            List<BasicDBObject> exams = (List<BasicDBObject>) chapter.get("exams");
            checkQuestions(chapIdx, exams, "考试题", chapterPath.toString());
            //遍历附件
            List<BasicDBObject> attachments = (List<BasicDBObject>) chapter.get("attachments");
            if (activeList(attachments)) {
                for (int i = 0; i < attachments.size(); i++) {
                    BasicDBObject attach = attachments.get(i);
                    String path = attach.getString("attachmentUrl");
                    if (!exists(path)) {
                        throw new Exception("第" + (chapIdx + 1) + "章 附件资料不存在：" + path);
                    }
                    String attachName = attach.getString("attachmentName");
                    String attachExport = chapterPath.toString() + "/附件/" + attachName;
                    copyFile(path, attachExport);
                    attach.put("src", attachExport);
                    attach.removeField("attachmentUrl");
                }
            }
            List<BasicDBObject> knowledgeBooks = (List<BasicDBObject>) chapter.get("knowledge");
            if(activeList(knowledgeBooks)){
                for (BasicDBObject knowledgeBook : knowledgeBooks) {
                    exportTextbook(knowledgeBook);
                }
            }
            //end chapters 循环
        }
    }

    private static void checkQuestions(int chapIdx, List<BasicDBObject> questions, String type, String chapterPath) throws Exception {
        if (activeList(questions)) {
            for (BasicDBObject ques : questions) {
                String src = ques.getString("src");
                if (!exists(src)) {
                    throw new Exception("第" + (chapIdx + 1) + "章 " + type + "附件不存在：" + src);
                }
                String srcName = ques.getString("attachmentName");
                String queExport = chapterPath + "/试题附件/" + srcName;
                copyFile(src, queExport);
            }
        }
    }
}
