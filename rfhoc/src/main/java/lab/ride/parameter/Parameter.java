package lab.ride.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: cwz
 * Time: 2017/11/9
 * Description:
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Parameter {
    private String name;
    private String type;
    private String step;
    private String range;
    private String unit;
}
