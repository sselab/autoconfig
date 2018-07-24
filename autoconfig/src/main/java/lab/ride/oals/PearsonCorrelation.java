package lab.ride.oals;



import lab.ride.record.CSVLoader;

import java.util.*;

/**
 * Created by Administrator on 2018/4/2.
 */
public class PearsonCorrelation {
	public static void main(String[] args) {
		test();
	}
	private static void test(){
		/*用于测试*/
		ArrayList<String> l = CSVLoader.readContent("./log.csv");
//		ArrayList<Double> x = new ArrayList<>();
//		ArrayList<Double> y= new ArrayList<>();
//		String[] titile = l.get(0).split(",");
//		for(int p=0;p<10;p++){
//			for(int i=1;i<l.size();i++){
//				String[] line = l.get(i).split(",");
//				x.add(Double.parseDouble(line[p]));
//				y.add(Double.parseDouble(line[11]));
//			}
//			double score = getPearsonCorrelationScore(x, y);
//			System.out.println(titile[p]+" "+p+" "+score*10);//0.6350393282549671
//			for(double d : LHS.getPoints(-score*10,5)){
//				System.out.println(d*100);
//			}
//		}
		ArrayList<Integer> ll = new ArrayList<>();
		ll.add(1);
		double[] x = new double[] {2, 2,1, 1};
		double[] y = new double[] { 13,1, 11, 9};
		double score = getPearsonCorrelationScore(x, y);
		System.out.println(score);//0.6350393282549671
	}

	public static double getPearsonCorrelationScore(List x, List y) {
		if (x.size() != y.size())
			throw new RuntimeException("数据不正确！");
		double[] xData = new double[x.size()];
		double[] yData = new double[x.size()];
		for (int i = 0; i < x.size(); i++) {
			xData[i] = (double)x.get(i);
			yData[i] = (double)y.get(i);
		}
		return getPearsonCorrelationScore(xData,yData);
	}

	public static double getPearsonCorrelationScore(double[] xData, double[] yData) {
		if (xData.length != yData.length)
			throw new RuntimeException("数据不正确！");
		double xMeans;
		double yMeans;
		double numerator = 0;// 求解皮尔逊的分子
		double denominator = 0;// 求解皮尔逊系数的分母

		double result = 0;
		// 拿到两个数据的平均值
		xMeans = getMeans(xData);
		yMeans = getMeans(yData);
		// 计算皮尔逊系数的分子
		numerator = generateNumerator(xData, xMeans, yData, yMeans);
		// 计算皮尔逊系数的分母
		denominator = generateDenomiator(xData, xMeans, yData, yMeans);
		// 计算皮尔逊系数
		result = numerator / denominator;
		return result;
	}

	/**
	 * 计算分子
	 *
	 * @param xData
	 * @param xMeans
	 * @param yData
	 * @param yMeans
	 * @return
	 */
	private static double generateNumerator(double[] xData, double xMeans, double[] yData, double yMeans) {
		double numerator = 0.0;
		for (int i = 0; i < xData.length; i++) {
			numerator += (xData[i] - xMeans) * (yData[i] - yMeans);
		}
		return numerator;
	}

	/**
	 * 生成分母
	 *
	 * @param yMeans
	 * @param yData
	 * @param xMeans
	 * @param xData
	 * @return 分母
	 */
	private static double generateDenomiator(double[] xData, double xMeans, double[] yData, double yMeans) {
		double xSum = 0.0;
		for (int i = 0; i < xData.length; i++) {
			xSum += (xData[i] - xMeans) * (xData[i] - xMeans);
		}
		double ySum = 0.0;
		for (int i = 0; i < yData.length; i++) {
			ySum += (yData[i] - yMeans) * (yData[i] - yMeans);
		}
		return Math.sqrt(xSum) * Math.sqrt(ySum);
	}

	/**
	 * 根据给定的数据集进行平均值计算
	 *
	 * @param
	 * @return 给定数据集的平均值
	 */
	private static double getMeans(double[] datas) {
		double sum = 0.0;
		for (int i = 0; i < datas.length; i++) {
			sum += datas[i];
		}
		return sum / datas.length;
	}
}