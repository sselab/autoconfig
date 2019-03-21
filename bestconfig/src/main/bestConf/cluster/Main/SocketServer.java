package bestConf.cluster.Main;/**
 * Created by Administrator on 2018/3/15.
 *
 * @
 */

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *@Author xuziheng
 *@Date 2018/3/15 23:14
 */
public class SocketServer {
	public static void main(String[] args) {
		try {
			ServerSocket ss = new ServerSocket(8888);
			System.out.println("启动服务器....");
			Socket s = ss.accept();
			System.out.println("客户端:"+s.getInetAddress().getLocalHost()+"已连接到服务器");

			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//读取客户端发送来的消息
			String mess = br.readLine();
			System.out.println("客户端："+mess);
			Thread.sleep(180000);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			bw.write(mess+"\n");
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
