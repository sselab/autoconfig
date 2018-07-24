package lab.ride.oals;/**
 * Created by Administrator on 2018/3/29.
 *
 * @
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author xuziheng
 * @Date 2018/3/29 9:47
 */
public class LHS {
	/**
	 * 相关系数
	 */
	static double a = 0.1;
	/**
	 * start A
	 */
	static double A = 0;
	/**
	 * end B
	 */
	static double B = 1;
	static double C = 1;
	static int K = 10;
	static double d = a * C / (Math.exp(-a * A * C) - Math.exp(-a * B * C));

	//	public static double funcD(double x) {
//		return a*C / (Math.exp(-a * A * C) - Math.exp(-a * B * C));
//	}
	public static double getRandom() {
		Random r = new Random();
		return r.nextDouble();
	}

	public static double funcZi(double j) {
		double d = C * a / (Math.exp(-a * C * A) - Math.exp(-a * C * B));
		return -Math.log(Math.exp(-a * C * A) - a * C * j / (d * K)) / a * C;
	}

	public static double getPoint(double zj, double zj1) {
		double h = d * (Math.exp(-a * C * zj) - Math.exp(-a * C * zj1)) / a * C;
		return -Math.log(Math.exp(-a * C * zj) - getRandom() * (Math.exp(-a * C * zj) - Math.exp(-a * C * zj1))) / a * C;
	}
	public static ArrayList<Double> getPoints(double a, int K,double low,double high){
		LHS.a = a;
		LHS.K = K;
		ArrayList<Double> l = new ArrayList<>();
		double[] zarray = new double[K + 1];
		zarray[0] = A;
//		double[] pointArray = new double[K];
		for (int j = 1; j <= K; j++) {
			zarray[j] = funcZi(j);
			l.add(getPoint(zarray[j-1],zarray[j])*(high-low)+low);
//			System.out.println("point " + pointArray[j-1]);
		}
		return l;
	}
	public static void main(String[] args) {
//		getPoints(0.1,10);
	}
}
