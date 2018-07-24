package lab.ride.entity;

import lombok.Data;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
@Data
public class ApplicationAttemptInfo {
    private String attemptId;
    private String startTime;
    private String endTime;
    private String lastUpdated;
    private long duration;
    private String sparkUser;
    private boolean completed;
}
