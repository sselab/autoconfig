package lab.ride.oals;

import lab.ride.entity.Record;
import lab.ride.model.RT;
import lab.ride.parameter.Parameter;
import lab.ride.parameter.ParameterUtils;
import lab.ride.process.ChangeFiles;
import lab.ride.record.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static lab.ride.oals.AppConfigs.command;
import static lab.ride.oals.AppConfigs.resultPath;
import static lab.ride.oals.ConfigGenerator.*;
import static lab.ride.oals.EnvConfigs.targetPath;
import static lab.ride.oals.EnvConfigs.templatePath;

/**
 * Author: cwz
 * Time: 2017/12/22
 * Description:
 *
 * 1.初始化K个参数取值,进行实验
 * 2.选取前M个高吞吐量的参数
 * 3
 */
public class Oals8 {
    static final Logger LOGGER = LoggerFactory.getLogger(Oals8.class);
    static long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {
        String appPath = args[0];
        AppConfigs.initialConfig(appPath);
//        String[] temp = command.split("/");
//        String appName = temp[temp.length - 3];
//        LOGGER.info("oals8 run app {}, K:{}, M:{}, time:{}", appName, AppConfigs.K, AppConfigs.M, AppConfigs.timeLimit);
//        run(resultPath + appName + "/oals.csv");
        run();
    }

    public static void run() throws Exception {
//        new File(fileName).getParentFile().mkdirs();
        Thread.sleep(19000000);
        startTime = System.currentTimeMillis();
        PriorityQueue<Record> randomBestRecord = new PriorityQueue<Record>();
        //取初始k个参数集
        List<ArrayList<Double>> initialK = getK(AppConfigs.K, true);

        List<Record> records = runNewGroup(initialK);
//        RT rt = new RT(fileName);
        // 更新区域
        Collections.sort(records);

        double initialBestTime = records.get(0).getThroughput();
        LOGGER.info("INITIAL BEST TIME: {}",  initialBestTime);

        /**
         * 上一次最好的M个参数取值组合
         */
        List<Record> lastNearRecords = new ArrayList<>();
        for(int i = 0; i < AppConfigs.B ; i++) {
            lastNearRecords.add(records.get(i));
        }
        /**
         * 最近最好的记录
         */
        List<Record>  nearRecords = new ArrayList<>();

        int currentIterTime = 1;
        while(System.currentTimeMillis() - startTime < AppConfigs.timeLimit){
            List<Record> bestMRecords = new ArrayList<>();
            for(int i = 0; i < AppConfigs.B ; i++){
                if(nearRecords.size()>=AppConfigs.B) {
                    // 如果搜索到的比上次的好，更新范围。
                    if(nearRecords.get(i).getThroughput() > lastNearRecords.get(i).getThroughput()) {
                        updateRange(nearRecords.get(i), initialK);
                        lastNearRecords.set(i, nearRecords.get(i));
                    }else {
                        if(randomBestRecord.peek() != null){
                            lastNearRecords.set(i, randomBestRecord.peek());
                            updateRange(randomBestRecord.poll(), initialK);
                        }else {
                            resetRange();
                        }
                    }
                }else{
                    /**
                     * 更新总的取值范围的大小
                     */
                    updateRange(lastNearRecords.get(i), initialK);
                }
                /**
                 * 在分支限界的范围内取5个点
                 */
                List<ArrayList<Double>> newNearK = getK(AppConfigs.B, false,records);
                List<Record> predictRecords = runNewGroup(newNearK);
                initialK.addAll(newNearK);
                records.addAll(predictRecords);
                bestMRecords.addAll(predictRecords);
            }//end for (int i = 0; i < AppConfigs.M ; i++)

            /**
             * 在选取的25个子节点中找出最好的K个
             */
            List<ArrayList<Double>> bestM = new ArrayList<>();
            Collections.sort(bestMRecords);
            nearRecords.clear();
            for(int i = 0; i < AppConfigs.B ; i ++){
                bestM.add(bestMRecords.get(i).getValue());
                nearRecords.add(bestMRecords.get(i));
            }

            //在初始区间选择最好的K个节点
            List<ArrayList<Double>> newRandomK = getK(AppConfigs.K, true);
            List<Record> newRandomRecords = runNewGroup( newRandomK);
            for(Record record : newRandomRecords){
                if(record.getThroughput() > initialBestTime){
                    randomBestRecord.add(record);
                }
            }
            initialK.addAll(newRandomK);
            records.addAll(newRandomRecords);

            Collections.sort(records);
            LOGGER.info("{} BEST TIME: {}", currentIterTime, records.get(0).getThroughput());
            currentIterTime++;
//            rt = new RT(fileName);
        }

    }


    public static void resetRange(){
        List<Parameter> parameters = ParameterUtils.getParameters();
        for(Parameter parameter : parameters){
            parameter.setCurrentLow(parameter.getLow());
            parameter.setCurrentHigh(parameter.getHigh());
        }
    }

    public static void updateRange(Record record,  List<ArrayList<Double>> configs){
        List<Double> bestValue = record.getValue();
        List<Parameter> parameters = ParameterUtils.getParameters();

        for(int i = 0; i < parameters.size(); i++){
            double value = bestValue.get(i);
            double low = Double.MIN_VALUE;
            double high = Double.MAX_VALUE;
            boolean lowChange = false;
            boolean highChange= false;

            for(int j = 0; j < configs.size(); j ++){
                double currentValue = configs.get(j).get(i);
                if(currentValue - value > 0 && currentValue < high){
                    high = currentValue;
                    highChange = true;
                }else if(currentValue - value < 0 && currentValue > low){
                    low = currentValue;
                    lowChange = true;
                }
            }

            if(!lowChange){
                low = value;
            }

            if(!highChange){
                high = value;
            }

            parameters.get(i).setCurrentLow(low);
            parameters.get(i).setCurrentHigh(high);
        }
    }


    public static List<Record> runNewGroup(List<ArrayList<Double>> configs){
        List<Record> records = new ArrayList<>();
        Random r = new Random();
        for(ArrayList<Double> config : configs){
            if(System.currentTimeMillis() - startTime >= AppConfigs.timeLimit){
                System.exit(1);
            }

            LinkedHashMap<String, String> m = toConfig(config);
            LinkedHashMap socketMap = new LinkedHashMap();
            for(Map.Entry<String,String> e : m.entrySet()){
                if(e.getKey().equals("compression.type")){
                    socketMap.put(e.getKey(),e.getValue());
                }else{
                    socketMap.put(e.getKey(),Double.parseDouble(e.getValue()));
                }

            }
            double execTime  = SocketPython.getResultFromPython(socketMap);
            Record record = new Record(execTime, toConfig(config), config,toValueMap(config));
            records.add(record);
            ArrayList<Record> writeRecords = new ArrayList<>();
            writeRecords.add(record);
        }
        return records;
    }

    /**
     * 在指定时间内运行
     * @TODO 待完成
     * @param configs
     * @param timelimit
     * @return
     */
    public static List<Record> runNewGroup(List<ArrayList<Double>> configs,int timelimit){
        List<Record> records = new ArrayList<>();
        Random r = new Random();
        long startTime = System.currentTimeMillis();


        for(ArrayList<Double> config : configs){
            if(System.currentTimeMillis() - startTime >= timelimit){
                return records;
            }
            LinkedHashMap m = toConfig(config);
            double execTime = r.nextDouble();
            Record record = new Record(execTime, toConfig(config), config,toValueMap(config));
            records.add(record);
            ArrayList<Record> writeRecords = new ArrayList<>();
            writeRecords.add(record);

        }
        return records;
    }

}
