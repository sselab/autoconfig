package lab.ride.oals;

import lab.ride.entity.Record;
import lab.ride.expr3.ParameterWeight;
import lab.ride.parameter.Parameter;
import lab.ride.parameter.ParameterUtils;

import java.util.*;

import static lab.ride.oals.EnvConfigs.cpu;
import static lab.ride.oals.EnvConfigs.maxMemory;
import static lab.ride.oals.EnvConfigs.ratio;

/**
 * Author: cwz
 * Time: 2017/12/22
 * Description:
 */
public class ConfigGenerator {
    private static Random random = new Random();

    public static double randomBetween(double low, double high){
        return random.nextDouble() * (high - low) + low;
    }

    public static List<ArrayList<Double>> getRandom(int n){
        List<Parameter> parameters = ParameterUtils.getParameters();
        List<ArrayList<Double>> pn = new ArrayList<>();
        for(Parameter parameter : parameters){
            ArrayList<Double> nRandom = new ArrayList<>();
            for(int i = 0; i < n; i++){
                nRandom.add(randomBetween(parameter.getLow(), parameter.getHigh()));
            }
            pn.add(nRandom);
        }

        List<ArrayList<Double>> np = new ArrayList<>();
        for(int i = 0 ; i < n; i++){
            ArrayList<Double> config = new ArrayList<>();
            for(ArrayList<Double> kRandom : pn){
                config.add(kRandom.get(i));
            }
            np.add(config);
        }

        return np;
    }

    public static List<ArrayList<ArrayList<Double>>> getAll(int k){
        List<Parameter> parameters = ParameterUtils.getParameters();
        List<ArrayList<Integer>> nk = new ArrayList<>();
        for(int i = 0; i < parameters.size(); i++){
            nk.add(getRandomSequence(k));
        }

        List<ArrayList<Integer>> kn = new ArrayList<>();
        for(int i = 0; i < k; i++){
            ArrayList<Integer> config = new ArrayList<>();
            for(ArrayList<Integer> kRandom : nk){
                config.add(kRandom.get(i));
            }
            kn.add(config);
        }


        List<ArrayList<ArrayList<Integer>>> allConfigs = new ArrayList<>();
        List<ArrayList<ArrayList<Double>>> allValueConfigs = new ArrayList<>();
        for(int i = 0; i < parameters.size(); i ++){
            ArrayList<Integer> otherNoChange = kn.get(i % k);
            ArrayList<ArrayList<Integer>> ithChangeConfigs = new ArrayList<>();
            ArrayList<ArrayList<Double>> doubleConfigs = new ArrayList<>();
            for(int j = 0; j < k; j++){
                ArrayList<Integer> enumConfigs = (ArrayList<Integer>)otherNoChange.clone();
                enumConfigs.set(i, j);
                ithChangeConfigs.add(enumConfigs);
                doubleConfigs.add(intToDoubleConfig(enumConfigs, k));
            }
            allValueConfigs.add(doubleConfigs);
            allConfigs.add(ithChangeConfigs);
        }

        System.out.println(allValueConfigs);
        return allValueConfigs;
    }

    public static ArrayList<Integer> getRandomSequence(int k){
        ArrayList<Integer> sequence = new ArrayList<>();
        for(int i = 0; i < k; i++){
            sequence.add(i);
        }
        Collections.shuffle(sequence);
        return sequence;
    }

    public static ArrayList<Double> intToDoubleConfig(ArrayList<Integer> config, int k){
        List<Parameter> parameters = ParameterUtils.getParameters();
        ArrayList<Double> result = new ArrayList<>();
        for(int i = 0; i < parameters.size(); i ++){
            Parameter parameter = parameters.get(i);
            double low = parameter.getLow();
            double high  = parameter.getHigh();
            double interval = (high - low) / k;
            int step = config.get(i);
            result.add(randomBetween(low + step * interval, low + (step + 1) * interval));
        }
        return result;
    }



    public static List<ArrayList<Double>> getWeightedK(int k, List<ParameterWeight> parameterWeights){
        List<Parameter> parameters = ParameterUtils.getParameters();

        double[][] values = new double[parameters.size()][];

        for(int i = 0; i < parameterWeights.size(); i ++){
            ParameterWeight weight = parameterWeights.get(i);
            Parameter parameter = parameters.get(weight.getIndex());
            int step = (int) (2 * k / (i + 1));
            step = step > 0 ? step : 1;
            double low = parameter.getCurrentLow();
            double high  = parameter.getCurrentHigh();
            double interval = (high - low) / step;
            double[] paramValues = new double[step];
            for(int j = 0; j < step; j++){
               paramValues[j] = randomBetween(low + j * interval, low + (j + 1) * interval);
            }
            values[weight.getIndex()] = paramValues;
        }

        int sum = 1;
        for(int i = 0; i < values.length; i ++){
            sum *= values[i].length;
        }
        System.out.println(sum);
        return getAllPossibility(values);
    }

    public static List<ArrayList<Double>> getAllPossibility(double[][] allArray){
        int arrayNum = allArray.length;
        List<ArrayList<Double>> resultList = new LinkedList<>();

        for(int n=0 ; n<allArray[0].length ; n++){
            ArrayList<Double> initList = new ArrayList<>();
            initList.add(allArray[0][n]);
            resultList.add(initList);
        }

        for(int i = 1 ; i < arrayNum ; i++){
            int arrayLength = allArray[i].length;
            List<ArrayList<Double>> tempResultList = new ArrayList<>();
            for(List<Double> existList : resultList) {
                for (int j = 0; j < arrayLength; j++){
                    ArrayList<Double> newList = new ArrayList<>();
                    newList.addAll(existList);
                    newList.add(allArray[i][j]);
                    tempResultList.add(newList);
                }
            }
            resultList.clear();
            resultList.addAll(tempResultList);
        }

        System.out.println(resultList.size());
        return resultList;
    }


    public static List<ArrayList<Double>> getK(int k, boolean isInitial){
        List<Parameter> parameters = ParameterUtils.getParameters();
        List<ArrayList<Double>> nk = new ArrayList<>();
        for(Parameter parameter : parameters){
            ArrayList<Double> kRandom = new ArrayList<>();
            double low;
            double high;
            if(isInitial) {
                low = parameter.getLow();
                high = parameter.getHigh();
            }else{
                low = parameter.getCurrentLow();
                high = parameter.getCurrentHigh();
            }
            double interval = (high - low) / k;

            for(int i = 0; i < k; i++){
                kRandom.add(randomBetween(low + i * interval, low + (i + 1) * interval));
            }

            Collections.shuffle(kRandom);
            nk.add(kRandom);
        }

        List<ArrayList<Double>> kn = new ArrayList<>();
        for(int i = 0 ; i < k; i++){
            ArrayList<Double> config = new ArrayList<>();
            for(ArrayList<Double> kRandom : nk){
                config.add(kRandom.get(i));
            }
            kn.add(config);
        }

        return kn;
    }
    public static List<ArrayList<Double>> getK(int k, boolean isInitial,List<Record> records){
        List<Parameter> parameters = ParameterUtils.getParameters();
        List<ArrayList<Double>> nk = new ArrayList<>();
        List<Double> y  = new ArrayList<>();
        for(Record r : records){
            y.add(r.getThroughput());
        }

        for(Parameter parameter : parameters){
            List<Double> x  = new ArrayList<>();
            for(Record r:records){
                x.add(r.getValueMap().get(parameter.getName()));
            }

            ArrayList<Double> kRandom = new ArrayList<>();
            double low;
            double high;
            if(isInitial) {
                low = parameter.getLow();
                high = parameter.getHigh();
            }else{
                low = parameter.getCurrentLow();
                high = parameter.getCurrentHigh();
            }
//            double interval = (high - low) / k;

            double d =  PearsonCorrelation.getPearsonCorrelationScore(x,y);
            List<Double> points = LHS.getPoints(d,k,low,high);
            for(int i = 0; i < k; i++){
                kRandom.add(points.get(i));
            }

            Collections.shuffle(kRandom);
            nk.add(kRandom);
        }

        List<ArrayList<Double>> kn = new ArrayList<>();
        for(int i = 0 ; i < k; i++){
            ArrayList<Double> config = new ArrayList<>();
            for(ArrayList<Double> kRandom : nk){
                config.add(kRandom.get(i));
            }
            kn.add(config);
        }

        return kn;
    }

    public static LinkedHashMap<String, String> toConfig(List<Double> configValue){
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        List<Parameter> parameters = ParameterUtils.getParameters();
        for(int i = 0; i < parameters.size(); i++){
            Parameter parameter  = parameters.get(i);
            double value = configValue.get(i);
            String v = "";
            switch (parameter.getType()){
                case "bool":
                    v += (int) Math.floor(value) == 0 ? "true" : "false";
                    break;
                case "enum":
                    v += parameter.getRange().split(" ")[(int)Math.floor(value)];
//                    v += value;
                    break;
                case "series":
                    v += (int)Math.ceil(value);
                    break;
                case "real":
                    v += value;
                    break;
            }
            config.put(parameter.getName(), v);
        }

        return config;
    }
    public static LinkedHashMap<String, Double> toValueMap(List<Double> configValue){
        LinkedHashMap<String, Double> config = new LinkedHashMap<>();
        List<Parameter> parameters = ParameterUtils.getParameters();
        for(int i = 0; i < parameters.size(); i++){
            Parameter parameter  = parameters.get(i);
            double value = configValue.get(i);
//            String v = "";
//            switch (parameter.getType()){
//                case "bool":
//                    v += (int) Math.floor(value) == 0 ? "true" : "false";
//                    break;
//                case "enum":
//                    v += parameter.getRange().split(" ")[(int)Math.floor(value)];
//                    break;
//                case "series":
//                    v += (int)Math.ceil(value);
//                    break;
//                case "real":
//                    v += value;
//                    break;
//            }
            config.put(parameter.getName(), value);
        }

        return config;
    }


    public static String mapToString(Map<String, String> configs, int ratio){
        List<Parameter> parameters = ParameterUtils.getParameters();

        constrict(configs, ratio);

        StringBuffer sb = new StringBuffer();
        for(Parameter parameter : parameters){
            String name = parameter.getName();
            sb.append(name)
                    .append(" ")
                    .append(configs.get(name))
                    .append(parameter.getUnit())
                    .append(System.lineSeparator());
        }


        sb.append("spark.cores.max")
                .append(" ")
                .append(configs.get("spark.cores.max"))
                .append(System.lineSeparator());


        sb.append("hibench.default.shuffle.parallelism")
                .append(" ")
                .append(configs.get("spark.default.parallelism"));

        return sb.toString();
    }

    public static void constrict(Map<String, String> configs, int ratio){
        int mem = Integer.parseInt(configs.get("spark.executor.memory"));
        int core = Integer.parseInt(configs.get("spark.executor.cores"));

        int nCExec = cpu / core;
        int nMExec = maxMemory / mem;
        int n = Math.min(nCExec, nMExec);

        mem = (mem - 300) / ratio + 300;
        configs.put("spark.executor.cores", core + "");
        configs.put("spark.executor.memory", mem + "");
        configs.put("spark.cores.max", core * n * EnvConfigs.workers + "");
    }

    public static void constrict(Map<String, String> configs){
        int mem = Integer.parseInt(configs.get("spark.executor.memory"));
        int core = Integer.parseInt(configs.get("spark.executor.cores"));

        int nCExec = cpu / core;
        int nMExec = EnvConfigs.maxMemory / mem;
        int n = Math.min(nCExec, nMExec);

        mem = (mem - 300) / ratio + 300;
        configs.put("spark.executor.cores", core + "");
        configs.put("spark.executor.memory", mem + "");
        configs.put("spark.cores.max", core * n * EnvConfigs.workers + "");
    }

    public static String mapToString(Map<String, String> configs){
        List<Parameter> parameters = ParameterUtils.getParameters();

        constrict(configs);

        StringBuffer sb = new StringBuffer();
        for(Parameter parameter : parameters){
            String name = parameter.getName();
            sb.append(name)
                    .append(" ")
                    .append(configs.get(name))
                    .append(parameter.getUnit())
                    .append(System.lineSeparator());
        }


        sb.append("spark.cores.max")
                .append(" ")
                .append(configs.get("spark.cores.max"))
                .append(System.lineSeparator());


        sb.append("hibench.default.shuffle.parallelism")
                .append(" ")
                .append(configs.get("spark.default.parallelism"));

        return sb.toString();
    }




    public static void main(String[] args) {
        List<Parameter> parameters = ParameterUtils.getParameters();
        int i = 0;
        int j = 13;
        List<ParameterWeight> parameterWeights = new ArrayList<>();
        for(Parameter parameter : parameters){
            parameterWeights.add(new ParameterWeight(j--, i++));
        }
//        System.out.println(parameterWeights);
        Collections.sort(parameterWeights);
        getWeightedK(15, parameterWeights);
//        getRandom(10000)
//                .stream()
//                .map(ConfigGenerator::toConfig)
//                .map(ConfigGenerator::mapToString)
//                .forEach(System.out::println);
//        ArrayList<Double> config = getK(EnvConfigs.K, true)
//                .get(0);
//
//        ArrayList<ArrayList<Double>> configs = new ArrayList<>();
//        configs.add(config);
//        System.out.println(Oals.runNewGroup(configs));


//        System.out.println(1 ^ 2 ^ 1 ^ 2 ^ 5);

//        ArrayList<ArrayList<Double>> list = new ArrayList<>();
//        ArrayList<Double> result = new ArrayList<>();
//        list.add(result);
//        if(list.contains(result)){
//            System.out.println("hhh");
//        }
    }
}
