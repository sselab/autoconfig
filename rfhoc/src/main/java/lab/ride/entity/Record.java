package lab.ride.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Record implements Comparable<Record>{
    private long time;
    private Map<String, String> configs;

    @Override
    public int compareTo(Record o) {
        return (int)(this.time - o.time);
    }
}
