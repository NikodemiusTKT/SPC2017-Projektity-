package harjoitustyo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author tkt
 * 
 *
 */
public class LogEvent {

    private static int lastId;
    private int logId;
    private String logTime;
    private String logType;
    private String desciption;

    private final String timeFormat = "dd.MM.yyyy HH:mm:ss";

    /**
     * @param logId
     * @param logTime
     * @param logType
     * @param desciption
     */
    public LogEvent(int logId, String logTime, String logType, String desciption) {
        this.logId = logId;
        this.logTime = logTime;
        this.logType = logType;
        this.desciption = desciption;
        lastId = this.logId;
    }
    
    /**
     * @param logId
     * @param logTime
     * @param logType
     * @param desciption
     */
    public LogEvent(String logType, String desciption) {
        this.logType = logType;
        this.desciption = desciption;
        this.logId = lastId++;
    }

    /**
     * @return the logId
     */
    public int getLogId() {
        return logId;
    }

    /**
     * @param logId the logId to set
     */
    public void setLogId(int logId) {
        this.logId = logId;
    }

    /**
     * @return the logTime
     */
    public String getLogTime() {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime input = LocalDateTime.parse(this.logTime,inputFormat);
        return input.format(DateTimeFormatter.ofPattern(timeFormat));
    }

    /**
     * @param logTime the logTime to set
     */
    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    /**
     * @return the logType
     */
    public String getLogType() {
        return logType;
    }

    /**
     * @param logType the logType to set
     */
    public void setLogType(String logType) {
        this.logType = logType;
    }

    /**
     * @return the desciption
     */
    public String getDesciption() {
        return desciption;
    }

    /**
     * @param desciption the desciption to set
     */
    public void setDesciption(String desciption) {
        this.desciption = desciption;
    }

}

