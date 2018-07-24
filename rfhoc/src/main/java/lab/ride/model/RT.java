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
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Author: cwz
 * Time: 2017/11/12
 * Description:
 */
public class RT {
    private static final Logger logger = LoggerFactory.getLogger(RT.class);
    private RandomForest randomForest;
    private Instances instances;
    public RT(String filePath){
        try {
            CSVLoader loader = new CSVLoader();
            loader.setSource(new File(filePath));
            instances = loader.getDataSet();
            instances.setClassIndex(instances.numAttributes() - 1);
            randomForest = new RandomForest();
            Evaluation eval=new Evaluation(instances);
            eval.crossValidateModel(randomForest, instances, 10, new Random(1));
            logger.info("model rae: {}", eval.relativeAbsoluteError());
            logger.info("instances size rae: {}", eval.numInstances());
            randomForest = new RandomForest();
            randomForest.buildClassifier(instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double predict(LinkedHashMap<String, String> param)throws Exception{
        Instance inst = new DenseInstance(12);
        inst.setDataset(instances);
        int i = 0;
        for(String key : param.keySet()){
           //System.out.println("key: "+key);
           // System.out.println("value: "+param.get(key));
        }
        for(String value : param.values()) {
            if (instances.attribute(i).type() == 0) {
                inst.setValue(i++, Double.parseDouble(value));
            } else {
                System.out.println("value: "+value);
                inst.setValue(i++, value);
            }
        }
        double value = randomForest.classifyInstance(inst);
        return value;
    }

    public static void main(String[] args) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File("log.csv"));
        Instances instances = loader.getDataSet();

//        for(Instance i : instances){
//            System.out.println(i.value(11));
//
//            if(i.value(11)==1){
//                i.attribute(11).setStringValue("A");
//            }else if(i.value(11)==0){
//                i.attribute(11).setStringValue("0");
//            }else if(i.value(11)==-1){
//                i.attribute(11).setStringValue("-1");
//            }
//
//        }
        instances.setClassIndex(instances.numAttributes() - 1);
        for(int i = 0 ; i < instances.numAttributes(); i++){
            System.out.println(instances.attribute(i).toString());
        }
        RandomForest randomForest = new RandomForest();
//        randomForest.setCalcOutOfBag(true);
//        randomForest.buildClassifier(instances);

//        randomForest.setNumIterations(1000);
//        System.out.println(Arrays.toString(randomForest.getOptions()));
//        randomForest.buildClassifier(instances);
//        Instance inst = new DenseInstance(10);
//        inst.setDataset(instances);
//
//        System.out.println(instances.attribute(0).type());
//        System.out.println(instances.attribute(3).type());
//        System.out.println(inst.toString());


        Evaluation eval=new Evaluation(instances);
        eval.crossValidateModel(randomForest, instances, 10, new Random(4));

        System.out.println(eval.toSummaryString());
    }
}
