package lab.ride.oals;

import lab.ride.entity.Record;
import lab.ride.model.RT2;
import lab.ride.parameter.Parameter;
import lab.ride.parameter.ParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static lab.ride.oals.ConfigGenerator.*;

/**
 * Author: cwz
 * Time: 2017/12/22
 * Description:
 *
 * 1.初始化K个参数取值,进行实验
 * 2.选取前M个高吞吐量的参数
 * 3
 */
public class AutoConfig {
    static final Logger LOGGER = LoggerFactory.getLogger(AutoConfig.class);
    static long startTime = System.currentTimeMillis();
    static RT2 rt;
    public static void main(String[] args) throws Exception {
        String appPath = args[0];
        AppConfigs.initialConfig(appPath);
        run();
    }

    public static void run() throws Exception {
//        new File(fileName).getParentFile().mkdirs();
//        Thread.sleep(11800000);

        startTime = System.currentTimeMillis();
        rt = new RT2("./log.csv");
        PriorityQueue<Record> randomBestRecord = new PriorityQueue<Record>();
        //取初始k个参数集
        List<ArrayList<Double>> initialK = getK(AppConfigs.K, true);

        List<Record> records = runNewGroup(initialK);
//      RT rt = new RT(fileName);
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
                 * 在分支限界的范围内取10个点
                 */
                List<ArrayList<Double>> newNearK = getK(AppConfigs.K, false,records);
                //选择最好的2个点
                List<ArrayList<Double>> best = findBest(newNearK);
                List<Record> predictRecords = runNewGroup(best);

                initialK.addAll(best);
                records.addAll(predictRecords);
                bestMRecords.addAll(predictRecords);
            }//end for (int i = 0; i < AppConfigs.M ; i++)

            /**
             * 在选取的10个子节点中找出最好的M个
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

    public static List<ArrayList<Double>> findBest(List<ArrayList<Double>> configs){
//        int[] weight = new int[configs.size()];
        Map<ArrayList<Double>,Integer> m = new HashMap<>();
        for(int i=0;i<configs.size();i++){
            m.put(configs.get(i),0);
        }
        for (int i = 0; i < configs.size(); i++) {
            for (int j = 0; j < configs.size(); j++) {
                if(i!=j){
                    double value = 0;
                    try {
                        value = rt.predict(toConfig(configs.get(i)),toConfig(configs.get(j)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(Math.round(value)==1){
                        Integer count = m.get(configs.get(i));
                        m.put(configs.get(i),++count);
                    }
                }
            }
        }
        List<ArrayList<Double>> ans = new ArrayList<ArrayList<Double>>();

        List<Map.Entry<ArrayList<Double>,Integer>> l = new ArrayList<Map.Entry<ArrayList<Double>,Integer>>();
        for(Map.Entry<ArrayList<Double>,Integer> e : m.entrySet()){
            l.add(e);
        }
        Collections.sort(l, new Comparator<Map.Entry<ArrayList<Double>, Integer>>() {
            @Override
            public int compare(Map.Entry<ArrayList<Double>, Integer> o1, Map.Entry<ArrayList<Double>, Integer> o2) {
                return o1.getValue()>o2.getValue()?-1:1;
            }
        });
        for(int i=0;i<1;i++){
            ans.add(l.get(i).getKey());
        }
        return ans;
    }

}
