package lab.ride.oals;

import com.jcraft.jsch.*;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Author: cwz
 * Time: 2017/12/22
 * Description:
 */
public class SSHClient {
    private static String host = "192.168.0.123";
    private static int PORT = 22;
    private static String user = "root";
    private static String password = "123456";
    static {

        try {
            Properties pro = new Properties();
            FileInputStream in = new FileInputStream("env.properties");
            pro.load(in);
            in.close();
            host = pro.getProperty("host");
            user = pro.getProperty("user");
            password = pro.getProperty("password");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void scpFile(String filePath, String targetPath) {
        try {
            JSch jsch = new JSch();

            // connect session
            Session session = jsch.getSession(user, host, PORT);
            session.setConfig("StrictHostKeyChecking", "no");

            session.setPassword(password);
            session.connect();

            // sftp remotely
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            // put
            channel.put(new FileInputStream(filePath), targetPath);

            channel.disconnect();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int exeCommand(String command) {

        String out = null;
        int status = 1;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, PORT);
            session.setConfig("StrictHostKeyChecking", "no");

            session.setPassword(password);
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            InputStream in = channelExec.getInputStream();
            channelExec.setCommand(command);
            channelExec.connect();

            out = IOUtils.toString(in, "UTF-8");
            status = channelExec.getExitStatus();
            channelExec.disconnect();
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

    public static void main(String[] args) throws IOException, JSchException, SftpException {
//        exeCommand("ls /");
        scpFile("spark.conf", "/home/spark.conf");
    }
}
