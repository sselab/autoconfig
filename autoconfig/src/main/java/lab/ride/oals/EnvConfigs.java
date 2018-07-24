package lab.ride.oals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Author: cwz
 * Time: 2017/12/22
 * Description:
 */
public class EnvConfigs {
    public static String templatePath = "spark1.conf";
    public static String targetPath = "/home/cloud/HiBench-master/conf/spark.conf";
    public static int ratio = 16;
    public static int cpu = 4;
    public static int maxMemory = 18432;
    public static int workers = 5;

    static {
        initialConfig("env.properties");
    }

    public static void initialConfig(String path){
        try {
            Properties pro = new Properties();
            FileInputStream in = new FileInputStream(path);
            pro.load(in);
            in.close();
            templatePath = pro.getProperty("templatePath");
            targetPath = pro.getProperty("targetPath");
            ratio = Integer.parseInt(pro.getProperty("ratio"));
            cpu = Integer.parseInt(pro.getProperty("cpu"));
            maxMemory = Integer.parseInt(pro.getProperty("maxMemory"));
            workers = Integer.parseInt(pro.getProperty("workers"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(maxMemory);
        System.out.println(ratio);
    }
}
