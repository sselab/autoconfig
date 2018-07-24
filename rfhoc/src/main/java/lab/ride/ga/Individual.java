package lab.ride.ga;


import lab.ride.model.RT;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;


public class Individual {
	private  static List<String> compressionList = Arrays.asList(new String[]{"none","lz4", "gzip", "snappy"});
	//-1 a,0 b,1 c
//	private  static List<String> acksList = Arrays.asList(new String[]{"a","b", "c"});
	static RT rt = new RT("log.csv");
	static int defaultGeneLength = 11;   //x?????????
	//????????
	private double[] genes = new double[defaultGeneLength];
	//?????????
	private double fitness=0;

	public Individual(){

	}
	public Individual(double fit){
		fitness=fit;
	}
	//??????????????????
	public void generateIndividual() {
        for (int i = 0; i < defaultGeneLength; i++) {
            double gene = Math.random();
//        	double gene = 0.1;
            genes[i] = gene;
        }
    }
	
	public double[] getGenes() {
		return genes;
	}
	public void setGenes(double[] genes) {
		this.genes = genes;
	}
	
	//????????fitness?
	public double getFitness(){
		if (fitness == 0) {
           //????????
			

			
			LinkedHashMap<String,String> parameter=transfromGene();
			
//			Iterator<Map.Entry<String, String>> iterator = parameter.entrySet().iterator();
//			while (iterator.hasNext()) {
//				Map.Entry<String, String> entry = iterator.next();
//				System.out.println(entry.getKey());
//				System.out.println(entry.getValue());
//			}
			
//			double memoryFraction= Double.parseDouble(parameter.get("spark.shuffle.memoryFraction"));
//			double safetyFraction=Double.parseDouble(parameter.get("spark.shuffle.safetyFraction"));
//			if(memoryFraction*safetyFraction<=0.45){
//
//				//�޸�Ϊ
////				WhatIfEstimate whatif=new WhatIfEstimate();
////				fitness=whatif.estimate(parameter,inputBytes,jarFileSize,exeMem,tasknums);
//			}else{
//				fitness=Double.MAX_VALUE;
//			}

			try {
				fitness = rt.predict(parameter);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
        return fitness;
	}
	
	public void setFitness(double fitness){
		this.fitness=fitness;
	}
	
	public double getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, double value) {
        genes[index] = value;
        fitness = 0;
    }

    //�����������
    public LinkedHashMap<String,String> transfromGene(){

		LinkedHashMap<String,String> parameters = new LinkedHashMap<>();
		//System.out.println("networkThreads" + genes[0]);
		//System.out.println( "compression" + genes[10]);
		double networkThreads = Math.ceil(genes[0] * 20);
		double ioThreads = Math.ceil(genes[1] * 24);
		double queuedRequests = Math.ceil(genes[2] * 100);
		double replica =  Math.ceil(genes[3] * 6);
		double receiveBuffer = Math.ceil(genes[4] * 20);
		double sendBuffer = Math.ceil(genes[5] * 20);
		double requestMax = Math.ceil(genes[6] * 30);
		double buffer = Math.ceil(genes[7] * 48);
		double batch = Math.ceil(genes[8] * 64);
		double linger=Math.ceil(genes[9] * 100);
		String compression = compressionList.get((int)Math.ceil(genes[10]*4)-1);
//		String acks = acksList.get((int)Math.ceil(genes[11]*3)-1);



		// spark.serializer
//		String serializer =  genes[9] < 0.5 ? "org.apache.spark.serializer.JavaSerializer" : "org.apache.spark.serializer.KryoSerializer";

		parameters.put("num.network.threads", String.valueOf(networkThreads));
		parameters.put("num.io.threads", String.valueOf(ioThreads));
		parameters.put("queued.max.requests", String.valueOf(queuedRequests));
		parameters.put("num.replica.fetchers", String.valueOf(replica));
		parameters.put("socket.receive.buffer.bytes", String.valueOf(receiveBuffer));
		parameters.put("socket.send.buffer.bytes", String.valueOf(sendBuffer));
		parameters.put("socket.request.max.bytes", String.valueOf(requestMax));
		parameters.put("buffer.memory", String.valueOf(buffer));
		parameters.put("batch.size", String.valueOf(batch));
		parameters.put("linger.ms", String.valueOf(linger));
		parameters.put("compression.type", String.valueOf(compression));
		//System.out.println(networkThreads);
		//System.out.println(compression);
//		parameters.put("acks", String.valueOf(acks));

    	return parameters;
    }

	public static void main(String[] args) {
	}

}
