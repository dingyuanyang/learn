import java.io.*;

public class FileTest {
    public static void main(String[] args) throws IOException {
       randomAccessFileTest();

    }

    private static void fileTest() {
        File file = new File("E:" + File.separator + "test");
        System.out.println("file=" + file);
        System.out.println("file.getParent=" + file.getParent());
        System.out.println("file.isDirectory=" + file.isDirectory());
        System.out.println("file.isFile=" + file.isFile());
        System.out.println("file.length=" + file.length());
        String[] list = file.list();
        System.out.println("--------------------------------");
        for (String s : list) {
            System.out.println("file.list:" + s);
        }
        System.out.println("--------------------------------");

        File[] files = file.listFiles();
        System.out.println("--------------------------------");
        for (File dest : files) {
            System.out.println("file.list:" + dest + "\t isDir=" + dest.isDirectory() + "\tisFile=" + dest.isFile() + "\tlength=" + dest.length());
        }
        System.out.println("--------------------------------");
    }

    private static void outputStreamTest() throws IOException {
        File dist = new File("E:" + File.separator + "OutputStream.txt");
        String source = "hello FileOutputStream 中文了";
        byte[] bytes = source.getBytes();
        OutputStream out = new FileOutputStream(dist);
        out.write(bytes);
    }

    private static void fileWriterTest() throws IOException {
        File dist = new File("E" + File.separator + "FileWriter.txt");
        String source = "hello FileWriter 這個中文";
        FileWriter writer = new FileWriter(dist);
        writer.write(source);
        writer.flush();
        // writer.close();
    }

    private static void randomAccessFileTest() throws IOException {
        File dist = new File("E:" + File.separator + "OutputStream.txt");
        RandomAccessFile file = new RandomAccessFile(dist, "rw");
        String src = "這個世界怎麽了？";
    }
}
