package lab.ride.record;

import java.io.*;
import java.util.List;

/**
 * Author: cwz
 * Time: 2017/11/30
 * Description:
 */
public class ObjectRW {

    public static void writeObj(Object o, String path){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path));
            out.writeObject(o);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readObj(String path){
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            Object obj = in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> readObjString(String path){
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
            List<String> obj = (List<String>) in.readObject();
            in.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
