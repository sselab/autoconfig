package lab.ride.entity;

import lombok.Data;

import java.util.List;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
@Data
public class ApplicationInfo {
    private String id;
    private String name;
    private List<ApplicationAttemptInfo> attempts;
}
