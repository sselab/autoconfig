package lab.ride.record;

import lab.ride.crawler.UrlContentGetter;
import lab.ride.entity.ApplicationEnvironmentInfo;
import lab.ride.entity.ApplicationInfo;
import lab.ride.entity.Record;
import lab.ride.parameter.ParameterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
public class Generate {
    public static void main(String[] args) {
        //kmeans app-20171116023818-0000
        //kmeans app-20171120080333-0448
//        String startAppId = "app-20171114021230-0001"; //als
//        String startAppId = "app-20171110035307-0001"; //pca
//        String endAppId = "app-20171113071217-1001"; //pca
//        String startAppId = "app-20171116023818-0000"; //kmeans
//        String endAppId = "app-20171120080333-0448";    //kmeans
//        String startAppId = "app-20171121201542-0001";  //als-m
//        String endAppId = "app-20171122185621-1000";    //als-m
        String startAppId = "app-20171222064940-0041";
        String endAppId = "app-20171223114007-1040";
        List<String> paramNames = ParameterUtils.getParameterNames();
        List<ApplicationInfo> applicationInfos = UrlContentGetter.getAllApps(null);
        List<Record> records = new ArrayList<>();
        System.out.println("all apps size: " + applicationInfos.size());
        int i = 1;
        for(ApplicationInfo app : applicationInfos){
            if(app.getId().compareTo(startAppId) >= 0 && app.getId().compareTo(endAppId) <= 0 && !app.getId().equals("app-20171112015536-0611")){
                System.out.println("get app: " + i++);
//                if(UrlContentGetter.isAppFinished(app.getId())) {
                    ApplicationEnvironmentInfo env = UrlContentGetter.getAppEnv(app.getId());
                    Map<String, String> configs = new HashMap<>();
                    for (String[] config : env.getSparkProperties()) {
                        if (paramNames.contains(config[0])) {
                            configs.put(config[0], config[1]);
                        }
                    }
                    records.add(new Record(app.getAttempts().get(0).getDuration(), configs));
//                }

            }
        }

        CSVWriter.writeRaw("als-final.csv", records);
        CSVWriter.writeDummy("als-d-final.csv", records);
    }

}
