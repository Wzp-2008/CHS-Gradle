package cn.wzpmc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLogger {
    public static final SimpleLogger INSTANCE = new SimpleLogger(new File(new File(System.getProperty("user.home"), ".gradle"), "chs.log"));
    private final File loggerFile;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy/MM/dd HH:mm:ss]");

    public SimpleLogger(File loggerFile) {
        this.loggerFile = loggerFile;
    }

    public void info(String msg) {
        this.appendToLogger(msg, "INFO");
    }

    public void info(Object msg) {
        this.info(msg.toString());
    }

    public void warn(String msg) {
        this.appendToLogger(msg, "WARN");
    }

    public void warn(Object msg) {
        this.warn(msg.toString());
    }

    public void error(String msg) {
        this.appendToLogger(msg, "ERROR");
    }

    public void error(Object msg) {
        this.error(msg.toString());
    }

    private void appendToLogger(String content, String tag) {
        try (FileOutputStream fos = new FileOutputStream(loggerFile, true)) {
            fos.write((dateFormat.format(new Date()) + "[" + tag + "] " + content + "\n").getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
