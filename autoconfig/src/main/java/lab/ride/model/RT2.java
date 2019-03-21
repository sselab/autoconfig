package lab.ride.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Author: cwz
 * Time: 2017/11/12
 * Description:
 */
public class RT2 {
    private static final Logger logger = LoggerFactory.getLogger(RT2.class);
    private static RandomForest randomForest = new RandomForest();
    private static  Instances instances;
    public RT2(String filePath){
        try {
            for(int i=0;i<10;i++){
                trainSimple(filePath);
                if(train()>0.8){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double predict(LinkedHashMap<String, String> param1,LinkedHashMap<String, String> param2)throws Exception{
        Instance inst = new DenseInstance(param1.size()+param2.size());
        inst.setDataset(instances);
        int i = 0;
        for(String value : param1.values()) {
            if (instances.attribute(i).type() == 0) {
                inst.setValue(i++, Double.parseDouble(value));
            } else {
                inst.setValue(i++, value);
            }
        }
        for(String value : param2.values()) {
            if (instances.attribute(i).type() == 0) {
                inst.setValue(i++, Double.parseDouble(value));
            } else {
                inst.setValue(i++, value);
            }
        }
        double value = randomForest.classifyInstance(inst);
        return value;
    }

    /**
     * 生成训练样本
     * @param filepath
     * @throws Exception
     */
    public  void trainSimple(String filepath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filepath));//150.csv
        Instances instances = loader.getDataSet();
        instances.randomize(new Random());
        //取前50个样本，两两组合，一共49*50共2450个样本
        int TRAIN_NUM =50;
        List<List<String>> ans = new ArrayList<List<String>>();
        for(int i=0;i<TRAIN_NUM;i++){
            for(int j=0;j<TRAIN_NUM;j++){
                if(i!=j){
                    List<String> l = new ArrayList<>();
                    Instance n1 = instances.get(i);
                    Instance n2 = instances.get(j);
                    for(int at=0;at<n1.numAttributes()-1;at++){
                        l.add(n1.toString(at));
                    }
                    for(int at=0;at<n2.numAttributes()-1;at++){
                        l.add(n2.toString(at));
                    }
                    if(n1.value(n1.numAttributes()-1)>n2.value(n2.numAttributes()-1)){
                        l.add("1");
                    }else{
                        l.add("0");
                    }
                    ans.add(l);
                }
            }
        }
        //再次取前50个样本，两两组合，一共49*50共2450个样本
        for(int i=TRAIN_NUM;i<TRAIN_NUM+50;i++){
            for(int j=TRAIN_NUM;j<TRAIN_NUM+50;j++){
                if(i!=j){
                    List<String> l = new ArrayList<>();
                    Instance n1 = instances.get(i);
                    Instance n2 = instances.get(j);
                    for(int at=0;at<n1.numAttributes()-1;at++){
                        l.add(n1.toString(at));
                    }
                    for(int at=0;at<n2.numAttributes()-1;at++){
                        l.add(n2.toString(at));
                    }
                    if(n1.value(n1.numAttributes()-1)>n2.value(n2.numAttributes()-1)){
                        l.add("1");
                    }else{
                        l.add("0");
                    }
                    ans.add(l);
                }
            }
        }
        File f = new File("./random.csv");
        if(f.exists()){
            f.delete();
        }
        FileWriter fw = new FileWriter(f);
        fw.write("num.network.threads,num.io.threads,queued.max.requests,num.replica.fetchers," +
                "socket.receive.buffer.bytes,socket.send.buffer.bytes,socket.request.max.bytes," +
                "buffer.memory,batch.size,linger.ms,compression.type,num.network.threads1,num.io.threads1,queued.max.requests1," +
                "num.replica.fetchers1,socket.receive.buffer.bytes1,socket.send.buffer.bytes1,socket.request.max.bytes1,buffer.memory1," +
                "batch.size1,linger.ms1,compression.type1,T\r\n");
        for(List<String> t : ans){
            for(int i=0;i<t.size()-1;i++){
                fw.write(t.get(i)+",");
            }
            fw.write(t.get(t.size()-1));
            fw.write("\r\n");
        }
        fw.close();
    }
    public  double train() throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("random.csv"));
        Instances allins = loader.getDataSet();
//        instances.randomize(new Random());
        Instances test = new Instances(allins);
        Instances train = new Instances(allins);
        allins.setClassIndex(allins.numAttributes() - 1);
        train.setClassIndex(train.numAttributes() - 1);
        instances = allins;

        Iterator<Instance> it = test.iterator();
        //删除前2450个,留下测试集
        for(int i=0;i<2450;i++){//3540
            it.next();
            it.remove();
        }
        //取前2450个样本，留下训练集
        Iterator<Instance> trainIt = train.iterator();
        List<Instance> test9900= new ArrayList<>();
        //取前2450个样本，存入test9900中
        for(int i=0;i<2450;i++){//3540
            Instance in = trainIt.next();
            test9900.add(in);
        }
        //删除集合中后2450个样本，留下训练集
        while(trainIt.hasNext()){
            trainIt.remove();
            trainIt.next();
        }
        //复制测试集合
        List<Instance> test2450= new ArrayList<>();
        for(int i=0;i<test.size();i++){
            test2450.add(test.get(i));
        }
        Evaluation eval = new Evaluation(train);
        randomForest = new RandomForest();
        eval.crossValidateModel(randomForest, train, 10, new Random(1));
//      CVParameterSelection ps = new CVParameterSelection();
//      ps.setClassifier(randomForest);
//      ps.setNumFolds(5);
//      ps.buildClassifier(instances);
        randomForest.setBatchSize("100");
        randomForest.setNumIterations(500);
        randomForest.setMaxDepth(5);
        randomForest.setCalcOutOfBag(true);
        randomForest.setNumFeatures(8);
        randomForest.buildClassifier(train);

        List<Long> predictTrain = new ArrayList<>();
        List<Double> predictDoubleTrain = new ArrayList<>();
        //测试训练集正确率
        for(Instance in : test9900){
            Instance inst = new DenseInstance(in.numAttributes()-1);
            inst.setDataset(instances);
            for(int i = 0;i<in.numAttributes()-1 ;i++) {
//                System.out.println(in.attribute(i).type());
//                System.out.println(in.value(i));
//                System.out.println(in.toString());
//                System.out.println(in.toString(i));
                if(in.attribute(i).type()==0){
                    inst.setValue(i, in.value(i));
                }else{
                    inst.setValue(i, in.toString(i));
                }
            }
            double value = randomForest.classifyInstance(inst);
            predictDoubleTrain.add(value);
            predictTrain.add(Math.round(value));
        }
        //测试集正确率
        List<Long> predict = new ArrayList<>();
        List<Double> predictDouble = new ArrayList<>();
        for(Instance in : test2450){
            Instance inst = new DenseInstance(in.numAttributes()-1);
            inst.setDataset(instances);
            int i = 0;
            for(;i<in.numAttributes()-1 ;i++) {
                if(in.attribute(i).type()==0){
                    inst.setValue(i, in.value(i));
                }else{
                    inst.setValue(i, in.toString(i));
                }
            }
            double value = randomForest.classifyInstance(inst);
            predictDouble.add(value);
            predict.add(Math.round(value));
        }
        int count1=0;
        int count2=0;
        int targetIndex = allins.numAttributes() - 1;
        int tn =0;//实际负:->预测负
        int tp =0;//实际正:->预测正
        int fn =0;//实际负:->预测正
        int fp =0;//实际正:->预测负
        double precesion =0; //查准率 TP/(TP+FP) 即在检索后返回的结果中，真正正确的个数占整个结果的比例。
        double recall = 0;//查全率 recall = TP/(TP+FN) 即在检索结果中真正正确的个数 占整个数据集（检索到的和未检索到的）中真正正确个数的比例。
        FileWriter fw = new FileWriter("./pr.csv");
        for(Double in : predictDouble){
             tn =0;//实际负:->预测负
             tp =0;//实际正:->预测正
             fn =0;//实际负:->预测正
             fp =0;//实际正:->预测负
            for (int i = 0; i < predictDouble.size(); i++) {

                if(predictDouble.get(i)>in&&test2450.get(i).value(targetIndex)==1){
                    tp++;
                }
                if(predictDouble.get(i)>in&&test2450.get(i).value(targetIndex)==0){
                    fp++;
                }
                if(predictDouble.get(i)<in&&test2450.get(i).value(targetIndex)==0){
                    tn++;
                }
                if(predictDouble.get(i)<in&&test2450.get(i).value(targetIndex)==1){
                    fn++;
                }
            }
            fw.write(1.0*tp/(tp+fp)+","+1.0*tp/(tp+fn));
            fw.write("\r\n");
        }
        fw.close();
        tn =0;//实际负:->预测负
        tp =0;//实际正:->预测正
        fn =0;//实际负:->预测正
        fp =0;//实际正:->预测负
        for (int i = 0; i < predict.size(); i++) {
            if(Math.round(test2450.get(i).value(targetIndex)) == predict.get(i)){
                if(predict.get(i)==0){
                    tn++;
                }else{
                    tp++;
                }
//                System.out.println(test2450.get(i).value(targetIndex) +" --- " + predict.get(i) +" --- "+predictDouble.get(i));
                count1++;
            }else if(Math.round(test2450.get(i).value(targetIndex))==0){
                fp++;
            }else{
                fn++;
            }
        }
        for (int i = 0; i < predictTrain.size(); i++) {
            if(Math.round(test9900.get(i).value(targetIndex)) == predictTrain.get(i)){
                if(predictTrain.get(i)==0){
//                    tn++;
                }else{
//                    tp++;
                }
                count2++;
            }else if(Math.round(test9900.get(i).value(targetIndex))==0){
//                fn++;
            }else{
//                fp++;
            }
        }
//        System.out.println(tn);
//        System.out.println(tp);
//        System.out.println(fn);
//        System.out.println(fp);
        precesion = 1.0*tp/(tp+fp);
        recall = 1.0*tp/(tp+fn);
        System.out.println(precesion);
        System.out.println(recall);
        System.out.println(1.0*count1/test2450.size());
        System.out.println(1.0*count2/test9900.size());
        return 1.0*count1/test2450.size();
    }
    public static void main(String[] args) throws Exception {
//      graph1();
//      graph2_2();
//      graph2_1();
//        String filePath = "./random.csv";
//        train(filePath);
//        for(int i=0;i<10;i++){
//            train(filepath);
//            if(graph3_2()>0.8){
//                break;
//            }
//        }

         RT2 r = new RT2("./1500.csv");
//         r.trainSimple(filePath);
//         r.train();
//         r.predict();

    }

}
