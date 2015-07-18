package log;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by johnunderwood on 7/11/15.
 */
public class Logger {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private final String prefixFormat = "%s [%s] %s %s : ";
    private final String errorFormat = "%s %s: %s";
    private final String name;
    private final LogConfig logConfig;
    private final PrintStream out;

    public Logger(String name) {
        this(name, System.err);
    }

    public Logger(String name, PrintStream out) {
        this.name = name;
        this.logConfig = LogConfig.getInstance();
        this.out = out;
    }

    public void trace(String format, Object... args) {
        log(LogLevel.TRACE, format, args);
    }

    public void debug(String format, Object... args) {
        log(LogLevel.DEBUG, format, args);
    }

    public void info(String format, Object... args) {
        log(LogLevel.INFO, format, args);
    }

    public void warn(String format, Object... args) {
        log(LogLevel.WARN, format, args);
    }

    public void error(String format, Object... args) {
        log(LogLevel.ERROR, format, args);
    }

    public void warn(String text, Throwable t) {
        log(LogLevel.WARN, text, t);
    }

    public void error(String text, Throwable t) {
        log(LogLevel.ERROR, text, t);
    }

    public synchronized void log(LogLevel level, String text, Throwable t) {
        if (logLevelEnabled(level)) {
            String messagePrefix = String.format(prefixFormat,
                    dateFormat.format(new Date()), Thread.currentThread().getName(), level, name);
            String messageBody = String.format(errorFormat, text, t.getClass().getName(), t.getMessage());
            out.println(messagePrefix + messageBody);
            t.printStackTrace(out);
        }
    }

    public synchronized void log(LogLevel level, String format, Object... args) {
        if (logLevelEnabled(level)) {
            String messagePrefix = String.format(prefixFormat,
                    dateFormat.format(new Date()), Thread.currentThread().getName(), level, name);
            String messageBody = String.format(format, args);
            out.println(messagePrefix + messageBody);
        }
    }

    private boolean logLevelEnabled(LogLevel level) {
        return logConfig.getLoggerLevel(name).ordinal() <= level.ordinal();
    }
}
