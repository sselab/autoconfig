package lab.ride.entity;

import lab.ride.parameter.ParameterUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private double throughput;
    private Map<String, String> configs;
    private ArrayList<Double> value;
    private Map<String,Double> valueMap;

    public Record(double throughput, Map<String, String> configs){
        this.throughput = throughput;
        this.configs = configs;
    }

    @Override
    public int compareTo(Record o) {
        return  new Double(o.throughput).compareTo(new Double(this.throughput));
    }

    public static Record toRecord(String s){
        String[] values = s.split(",");
        Map<String, String> configs = new LinkedHashMap<>();
        int i = 0;
        for(; i < values.length - 1; i++){
            configs.put(ParameterUtils.getParameterNames().get(i), values[i]);
        }
        return new Record(Long.parseLong(values[i]), configs);
    }
}
