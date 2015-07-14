package log;

import bot.AttackListRanker;
import bot.AttackSuperRegionRanker;
import bot.TroopMovePlanner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnunderwood on 7/11/15.
 */
public class LogConfig {
    private static LogConfig instance = null;
    private LogLevel rootLevel = LogLevel.INFO;
    private Map<String,LogLevel> loggerLevels = new HashMap<>();

    private LogConfig() {
        //loggerLevels.put(AttackSuperRegionRanker.class.getSimpleName(), LogLevel.DEBUG);
        loggerLevels.put(AttackListRanker.class.getSimpleName(), LogLevel.DEBUG);
        loggerLevels.put(TroopMovePlanner.class.getSimpleName(), LogLevel.TRACE);
    }

    public static synchronized LogConfig getInstance() {
        if(instance == null) {
            instance = new LogConfig();
        }

        return instance;
    }

    public LogLevel getLoggerLevel(String loggerName) {
        if(loggerLevels.containsKey(loggerName)) {
            return loggerLevels.get(loggerName);
        } else {
            return rootLevel;
        }
    }

    public void setLoggerLevel(String name, LogLevel level) {
        loggerLevels.put(name, level);
    }
}
