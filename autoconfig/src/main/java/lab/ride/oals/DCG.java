package lab.ride.oals;

import lab.ride.homo.IndexTime;
import lab.ride.model.Node;
import weka.core.Instance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Author: cwz
 * Time: 2018/1/9
 * Description:
 */
public class DCG {
    private String inputPath;
    private int reduceTimes;
    private List<ArrayList<IndexTime>> rawDatas;
    private String outputPath;
    private StringBuilder result = new StringBuilder("type,TP,TN,FP,FN,Accuracy,Precision,Recall,F1\n");

    public DCG(String inputPath) throws IOException {
        this.inputPath = inputPath;
        initialData();
    }

    public void initialData() throws IOException {
        List<String> lines =  Files.lines(Paths.get(inputPath)).collect(Collectors.toList());
        if(lines.size() <= 0){
            System.out.println(lines.size());
            System.exit(1);
        }else{
            rawDatas = new ArrayList<>();
            reduceTimes = lines.get(0).split(",").length;
            for(int i = 0; i < reduceTimes; i++){
                rawDatas.add(new ArrayList<>());
            }

            for(int i = 0; i < lines.size(); i++){
                for(int j = 0; j < reduceTimes; j++){
                    String[] cols = lines.get(i).split(",");
                    rawDatas.get(j).add(new IndexTime(i, Long.parseLong(cols[j])));
                }
            }
        }

    }

    public static int getValue(int i, int j,int n){
        double percent = 1.0*Math.abs(i -j) / (n-1);
//        System.out.println("percent "+percent);
        if(percent <= 0.1){
            return 31;
        }else if(percent <= 0.25){
            return 15;
        }else if(percent <= 0.55){
            return 7;
        }else if(percent <= 0.9){
            return 3;
        }else {
            return 0;
        }
    }

//    public double computeDCG(List<Integer> sortedIndex, List<Integer> otherIndex){
//        double dcg = 0;
//        for(int i = 0; i < otherIndex.size(); i++){
//            int index = otherIndex.get(i);
////            System.out.println(getValue(sortedIndex, index));
////            System.out.println(getValue(sortedIndex, index) / Math.log(i + 2));
//            dcg += getValue(sortedIndex, index) / Math.log(i + 2);
//        }
//        return dcg;
//    }

    public static double computeDCGr(List<Instance> origin){
        double dcg = 0;
        for(int i = 0; i < origin.size(); i++){

//            dcg =dcg+  31 / Math.log(i + 2);
            dcg =dcg+  31 / origin.size();
        }

        return dcg/origin.size();
    }
    public static double computeDCG(List<Instance> origin,List<Node> predict){
        double dcg = 0;
        for(int i = 0; i < predict.size(); i++){
            Node curNode = predict.get(i);
            for(int j=0;j<origin.size();j++){
                if(origin.get(j).equals(curNode.in)){
//                    dcg =dcg+  getValue(i, j,origin.size()) / Math.log(i + 2);
                    dcg =dcg+  getValue(i, j,origin.size()) / origin.size();
                }
            }

        }

        return dcg/origin.size();
    }



    public static void main(String[] args) throws IOException {
//        for(double percent = 0.02; percent <= 0.1; percent += 0.02) {
//            new ComputeEI("homo-csv/sort.csv", percent).compute();
//        }
        String dir = "dcg-result/gbt";
        String[] algs = {"all"};
        String[] models = {"rt", "gdbt", "svr"};
        StringBuilder writeResult = new StringBuilder("");

        for(int weight = 1; weight <= 3; weight++){
            StringBuilder tBuilder = new StringBuilder(weight + "");
            StringBuilder rBuilder = new StringBuilder(weight + "");
            for(int fold = 10; fold <= 10; fold++){
                File parent = new File(dir,   weight + "-" + fold);
                for(String alg : algs) {
                    for (String model : models) {
                        double real = 0;
                        double test = 0;
                        int count = 0;
                        for (File file : parent.listFiles()) {

                            if (file.getName().contains(alg) && file.getName().contains(model)) {
                                String result = new lab.ride.homo.DCG(file.getPath()).computeDCG();
                                real += Double.parseDouble(result.split(" ")[0]);
                                test += Double.parseDouble(result.split(" ")[1]);
                                count++;
                            }
                        }
                        tBuilder.append(",").append("" + test/count);
                        rBuilder.append(",").append("" + real/count);
                    }
                }

            }
            writeResult.append(tBuilder.toString());
            writeResult.append("\n");
            writeResult.append(rBuilder.toString());
            writeResult.append("\n");
        }

        Files.write(Paths.get("dcg.csv"), writeResult.toString().getBytes());

    }
}
