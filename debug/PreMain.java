import java.lang.instrument.Instrumentation;
import java.io.*;

class PreMain {
    public static void premain(String args, Instrumentation inst) throws Exception {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("premain"), "UTF-8");
        out.write("premain привет\r\n");
        out.write(args + "\r\n");
        Main.premain = args;
        out.close();
    }
}
