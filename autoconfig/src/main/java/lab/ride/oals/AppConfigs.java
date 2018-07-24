package lab.ride.oals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Author: cwz
 * Time: 2018/1/2
 * Description:
 */
public class AppConfigs {
    public static String command = "/home/cloud/HiBench-master/bin/workloads/ml/als/spark/run.sh";
    public static String resultPath = "";
    public static int timeLimit = 0;
    public static int K = 0;
    public static int B = 0;

    public static void initialConfig(String path) {
        try {
            Properties pro = new Properties();
            FileInputStream in = new FileInputStream(path);
            pro.load(in);
            in.close();
            command = pro.getProperty("command");
            resultPath = pro.getProperty("resultPath");
            timeLimit = Integer.parseInt(pro.getProperty("timeLimit")) * 1000;
            K = Integer.parseInt(pro.getProperty("K"));
            B = Integer.parseInt(pro.getProperty("B"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
