package lab.ride.record;/**
 * Created by Administrator on 2018/4/2.
 *
 * @
 */

import java.io.*;
import java.util.ArrayList;

/**
 *@Author xuziheng
 *@Date 2018/4/2 22:27
 */
public class CSVLoader {
	public static ArrayList<String> readContent(String path){
		ArrayList<String> result = new ArrayList<>();
		String line;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(path)));
			while((line = br.readLine())!=null){
				result.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}
