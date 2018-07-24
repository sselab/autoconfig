package lab.ride.record;

import lab.ride.crawler.UrlContentGetter;
import lab.ride.entity.ApplicationEnvironmentInfo;
import lab.ride.entity.ApplicationInfo;
import lab.ride.parameter.ParameterUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: cwz
 * Time: 2017/11/21
 * Description:
 */
public class ConfigWriter {
    public static void main(String[] args) {
        String startAppId = "app-20171114021230-0001";
        String endAppId = "app-20171115052220-1000";
        List<String> paramNames = ParameterUtils.getParameterNames();
        List<ApplicationInfo> applicationInfos = UrlContentGetter.getAllApps(null);
        List<String> configs = new ArrayList<>();
        for(ApplicationInfo app : applicationInfos){
            if(app.getId().compareTo(startAppId) >= 0 && app.getId().compareTo(endAppId) <= 0){
                ApplicationEnvironmentInfo env = UrlContentGetter.getAppEnv(app.getId());
                StringBuffer stringBuffer = new StringBuffer();
                for(String[] c : env.getSparkProperties()){
                    if(paramNames.contains(c[0])){
                        stringBuffer.append(c[0]).append(" ").append(c[1]);
                        stringBuffer.append(System.lineSeparator());
                    }
                }
                System.out.println(stringBuffer.toString());
                configs.add(stringBuffer.toString());
            }
        }

        System.out.println(configs.size());
        try {
            ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream("config.obj"));
            oo.writeObject(configs);
            oo.flush();
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
