package lab.ride.model;

import lab.ride.parameter.ParameterUtils;
import lab.ride.process.ChangeFiles;
import lab.ride.record.ObjectRW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: cwz
 * Time: 2017/11/30
 * Description:
 */
public class Initial {
    private static final Logger logger = LoggerFactory.getLogger(Initial.class);
    private static String historyConfigsPath = "history_configs.obj";
    public static void main(String[] args) {
        String baseDirPath = args[0];
        File baseDir = new File(baseDirPath);
        if(! baseDir.exists()){
            baseDir.mkdirs();
        }
        historyConfigsPath = baseDirPath + "/" + historyConfigsPath;

        List<String> historyConfigs = new ArrayList<>();
        if(new File(historyConfigsPath).exists()) {
            historyConfigs =  ObjectRW.readObj(historyConfigsPath);
        }
        logger.info("history config size: {}", historyConfigs.size());

        String randomConfig = ParameterUtils.randomConfig();
        while(historyConfigs.contains(randomConfig)){
            randomConfig = ParameterUtils.randomConfig();
        }

        logger.info("config: {}", randomConfig);
        ChangeFiles.changeConfigs(randomConfig, "/home/cloud/HiBench-master/conf/spark.conf");
        historyConfigs.add(randomConfig);
        ObjectRW.writeObj(historyConfigs, historyConfigsPath);
    }
}
