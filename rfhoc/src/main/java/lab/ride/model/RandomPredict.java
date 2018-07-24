package lab.ride.model;

import lab.ride.entity.Record;
import lab.ride.parameter.ParameterUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Author: cwz
 * Time: 2017/12/4
 * Description:
 */
public class RandomPredict {
    private static int randomConfigSize = 1119744;
    public static void main(String[] args) throws Exception {
        RT rt = new RT("als/records.csv");
        List<Record> randomRecords = new LinkedList<>();
        for(int i = 0; i < randomConfigSize; i++){
            LinkedHashMap<String, String> config = ParameterUtils.randomConfigMap();
            long time = (long)rt.predict(config);
            randomRecords.add(new Record(time, config));
        }
        Collections.sort(randomRecords);
        System.out.println(randomRecords.get(0).getConfigs());

    }
}
