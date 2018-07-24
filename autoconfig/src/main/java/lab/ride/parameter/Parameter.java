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
    /**
     * 参数名
     */
    private String name;
    /**
     * 类型
     */
    private String type;
    /**
     * 步长
     */
    private String step;
    /**
     * 范围
     */
    private String range;
    private String unit;

    private double low;
    private double high;
    private double currentLow;
    private double currentHigh;

    public Parameter(String name, String type, String step, String range, String unit) {
        this.name = name;
        this.type = type;
        this.step = step;
        this.range = range;
        this.unit = unit;

        switch (type) {
            case "bool":
                this.low = 0;
                this.high = 2;
                break;
            case "enum":
                this.low = 0;
                this.high = range.split(" ").length;
                break;
            case "series":
                this.low = Double.parseDouble(range.split("-")[0]) - 1;
                this.high = Double.parseDouble(range.split("-")[1]);
                break;
            case "real":
                this.low = Double.parseDouble(range.split("-")[0]);
                this.high = Double.parseDouble(range.split("-")[1]);
                break;
        }

        this.currentLow = this.low;
        this.currentHigh = this.high;
    }


}
