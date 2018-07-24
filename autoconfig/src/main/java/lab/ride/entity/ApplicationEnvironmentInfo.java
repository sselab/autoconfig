package lab.ride.entity;

import lombok.Data;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
@Data
public class ApplicationEnvironmentInfo {
    private RuntimeInfo runtime;
    private String[][] sparkProperties;
    private String[][]  systemProperties;
    private String[][]  classpathEntries;
}
