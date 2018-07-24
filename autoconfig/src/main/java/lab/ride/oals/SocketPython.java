package lab.ride.oals;


import com.alibaba.fastjson.JSON;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 *@Author xuziheng
 *@Date 2018/3/15 17:34
 */
public class SocketPython {
	public static double getResultFromPython(Map m){
		try {

			String str = JSON.toJSONString(m);
			Socket socket = new Socket("localhost",8889);
			socket.setKeepAlive(true);

			socket.setSoTimeout(9999999);
			//获取输出流，向服务器端发送信息
			OutputStream os=socket.getOutputStream();//字节输出流
			PrintWriter pw=new PrintWriter(os);//将输出流包装为打印流
			pw.write(str);
			pw.flush();
//			socket.shutdownOutput();//关闭输出流

			InputStream is=socket.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line=null;
			String info="0.1";
			while((line=in.readLine())!=null){
				System.out.println("reveive from Python "+line);
				info=line;
			}
			is.close();
			in.close();
			socket.close();
			return Double.parseDouble(info);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0.1;

	}
	public static void main(String args[])throws Exception {
		HashMap<String,String> m = new HashMap<>();
		m.put("a","b");
		m.put("1","2");
		System.out.println(new SocketPython().getResultFromPython(m));
	}
}
