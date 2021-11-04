import java.io.*;

class Main {
    static String premain;

    public static void main(String[] args) throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("main"), "UTF-8");
        out.write("main привет\r\n");
        out.write(args[0] + "\r\n");
        out.close();
        System.out.println(args[0].equals(premain));
    }
}
