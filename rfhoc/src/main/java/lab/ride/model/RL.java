package lab.ride.model;

import lab.ride.crawler.UrlContentGetter;
import lab.ride.entity.ApplicationEnvironmentInfo;
import lab.ride.entity.ApplicationInfo;
import lab.ride.entity.Record;
import lab.ride.parameter.ParameterUtils;
import lab.ride.process.ChangeFiles;
import lab.ride.record.CSVWriter;
import lab.ride.record.ObjectRW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toList;


/**
 * Author: cwz
 * Time: 2017/11/30
 * Description:
 */
public class RL {
    private static final Logger logger = LoggerFactory.getLogger(RL.class);
    private static int configSize = 10;
    private static int randomConfigSize = 1119744;
    private static String historyConfigsPath = "history_configs.obj";

    public static void main(String[] args) throws Exception{
        String baseDirPath = args[0];
        long startTime = Long.parseLong(args[1]) * 1000 - 8 * 60 * 60 * 1000;
//        if(args.length > 2){
//            configSize = Integer.parseInt(args[2]);
//            randomConfigSize = Integer.parseInt(args[3]);
//        }
        File baseDir = new File(baseDirPath);
        if(! baseDir.exists()){
            baseDir.mkdirs();
        }

        String csvFile = baseDirPath + "/" + "record.csv";
        historyConfigsPath = baseDirPath + "/" + historyConfigsPath;
        if(args.length == 4){
            initial(args[2], csvFile, Integer.parseInt(args[3]));
            return;
        }



        List<String> historyConfigs = new ArrayList<>();
        if(new File(historyConfigsPath).exists()) {
            historyConfigs = ObjectRW.readObj(historyConfigsPath);
        }
        logger.info("history config size: {}", historyConfigs.size());

        List<String> paramNames = ParameterUtils.getParameterNames();
        Thread.sleep(10000);
        logger.info("sleep: {}", 10000);

        updateRecords(startTime, paramNames, csvFile);


        RT rt = new RT(csvFile);
        List<Record> randomRecords = new LinkedList<>();
        for(int i = 0; i < randomConfigSize; i++){
            LinkedHashMap<String, String> config = ParameterUtils.randomConfigMap();
            long time = (long)rt.predict(config);
            randomRecords.add(new Record(time, config));
        }

        Collections.sort(randomRecords);

        List<String> configGroup = new ArrayList<>();
        for(Record record : randomRecords){
            String config = ParameterUtils.mapToString(record.getConfigs());
            if(historyConfigs.contains(config)){
                continue;
            }else {
                logger.info("config: {}, time: {}", record.getConfigs(), record.getTime());
                historyConfigs.add(config);
                configGroup.add(config);
                if(configGroup.size() >= configSize){
                    break;
                }
            }
        }



        for(int i = 0; i < configGroup.size(); i++){
            ChangeFiles.changeConfigs(configGroup.get(i), baseDirPath + "/" + i + ".conf");
        }

        ObjectRW.writeObj(historyConfigs, historyConfigsPath);
    }

    public static void updateRecords(long startTime, List<String> paramNames, String csvFile){
        List<ApplicationInfo> apps = UrlContentGetter.getAppsFromTime(startTime);

        List<Record> records = new ArrayList<>();
        for(ApplicationInfo app : apps){
            ApplicationEnvironmentInfo env = UrlContentGetter.getAppEnv(app.getId());
            Map<String, String> configs = new HashMap<>();
            for (String[] config : env.getSparkProperties()) {
                if (paramNames.contains(config[0])) {
                    configs.put(config[0], config[1]);
                }
            }
            records.add(new Record(app.getAttempts().get(0).getDuration(), configs));
        }

        CSVWriter.write(csvFile, records);
    }
    public static void initial(String inputPath, String recordPath, int size) throws IOException {
        List<String> records = Files.lines(Paths.get(inputPath)).collect(toList());
        String title = records.remove(0);
        String[] cols = title.split(",");
        Collections.shuffle(records);
        List<String> historyConfigs = new ArrayList<>();

        BufferedWriter bw = new BufferedWriter(new FileWriter(recordPath));
        bw.write(title);

        for(int i = 0; i < size; i++){
            LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap();
            String[] c = records.get(i).split(",");
            for(int j = 0; j < cols.length - 2; j++){
                linkedHashMap.put(cols[j], c[j]);
            }
            historyConfigs.add(ParameterUtils.mapToString(linkedHashMap));
            bw.newLine();
            bw.write(records.get(i));
        }
        bw.flush();
        bw.close();

        ObjectRW.writeObj(historyConfigs, historyConfigsPath);
    }


}
