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

	public static double getRandom() {
		Random r = new Random();
		return r.nextDouble();
	}

	public static double funcZi(double a,double C,double A,double B,double j,double d,int K) {
		return -Math.log(Math.exp(-a * C * A) - (a * C * j )/ (d * K)) / a * C;
	}

	public static double getPoint(double zj, double zj1,double d,double a,double C) {
		double h = d * (Math.exp(-a * C * zj) - Math.exp(-a * C * zj1)) / a * C;
		return -Math.log(Math.exp(-a * C * zj) - getRandom() * (Math.exp(-a * C * zj) - Math.exp(-a * C * zj1))) / a * C;
	}

	/**
	 *
	 * @param a 参数权重
	 * @param K 样本点采集个数
	 * @param C 超参数
	 * @param A 区间起始
	 * @param B 区间末尾
	 * @return
	 */
	public static ArrayList<Double> getPoints(double a, int K,double C,double A,double B){
		ArrayList<Double> l = new ArrayList<>();
		double[] zarray = new double[K + 1];
		zarray[0] = A;
		zarray[K] = B;
		double d = (a * C) / (Math.exp(-a * A * C) - Math.exp(-a * B * C));
		for (int j = 1; j < K; j++) {
			zarray[j] = funcZi(a,C,A,B,j,d,K);
			l.add(getPoint(zarray[j-1],zarray[j],d,a,C));//*(high-low)+low
		}
		return l;
	}

	public static void main(String[] args) {
//		getPoints(0.1,10);
	}
}
