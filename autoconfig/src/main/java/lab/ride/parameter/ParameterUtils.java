package lab.ride.parameter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Author: cwz
 * Time: 2017/11/9
 * Description:
 */
public class ParameterUtils {
    private final static Random random = new Random();
    private final static DecimalFormat format = new DecimalFormat("0.00");
    public static Map<String, Parameter> parameterMap = new LinkedHashMap<>();
    public final static List<Parameter> parameters = readParameterFile("configs.csv");


    public static List<Parameter> getParameters() {
        return parameters;
    }

    public static List<String> getParameterNames(){
        List<String> names = new ArrayList<>();
        for(Parameter parameter : parameters){
            names.add(parameter.getName());
        }
        return names;
    }
    /**
     * 从参数配置文件中读取可优化配置参数，类型和取值范围
     * @param filePath
     * @return
     */
    public static List<Parameter> readParameterFile(String filePath){
        List<Parameter> result = new ArrayList<>();
        final String[] FILE_HEADER = {"property","type","step","range","unit"};
        CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER).withSkipHeaderRecord();
        try(Reader in = new FileReader(filePath)) {
            Iterable<CSVRecord> records = format.parse(in);
            String property;
            String type;
            String step;
            String range;
            String unit;
            for (CSVRecord record : records) {
                property = record.get("property");
                type = record.get("type");
                step = record.get("step");
                range = record.get("range");
                unit = record.get("unit");
                Parameter parameter = new Parameter(property, type, step, range, unit);
                result.add(parameter);
                parameterMap.put(property, parameter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 随机产生输入配置的参数值
     * @param parameter
     * @return
     */
    public static String randomValue(Parameter parameter, boolean unit) {
        String value;
        switch (parameter.getType()) {
            case "bool":
                value = random.nextBoolean() == true ? "true" : "false";
                break;
            case "enum":
                String[] values = parameter.getRange().split(" ");
                value = values[random.nextInt(values.length)];
                break;
            case "series":
                double step = Double.parseDouble(parameter.getStep());
                int low = Integer.parseInt(parameter.getRange().split("-")[0]);
                int high = Integer.parseInt(parameter.getRange().split("-")[1]);
                value = format.format(step * (random.nextInt(high - low + 1) + low)) + "";
                value = subZeroAndDot(value);
                if(unit) {
                    value = value + parameter.getUnit();
                }
                break;
            case "real":
                double rLow = Double.parseDouble(parameter.getRange().split("-")[0]);
                double rHigh = Double.parseDouble(parameter.getRange().split("-")[1]);
                value = String.valueOf(random.nextDouble() * (rHigh - rLow) + rLow);
                if(unit) {
                    value = value + parameter.getUnit();
                }
                break;
            default:
                value = "";
        }

        return value;
    }

//    /**
//     * 获取随机配置
//     * @return
//     */
//    public static String randomConfig(){
//        StringBuffer sb = new StringBuffer();
//        for(Parameter parameter : parameters){
//            sb.append(parameter.getName())
//                    .append(" ")
//                    .append(randomValue(parameter, true)).append(System.lineSeparator());
//        }
//        return sb.toString();
//    }
    public static String randomConfig(){
        return (mapToString(randomConfigMap()));
    }

    /**
     * 获取随机配置Map
     * @return
     */

    public static LinkedHashMap<String, String> randomConfigMap(){
        LinkedHashMap<String, String> config = new LinkedHashMap<>();
        for(Parameter parameter : parameters){
            config.put(parameter.getName(), randomValue(parameter, false));
        }
        return config;
    }

    public static String mapToString(Map<String, String> configs){
        StringBuffer sb = new StringBuffer();
        for(Parameter parameter : parameters){
            String name = parameter.getName();
            sb.append(name)
                    .append(" ")
                    .append(configs.get(name))
                    .append(parameter.getUnit())
                    .append(System.lineSeparator());
        }
        sb.append("spark.cores.max")
                .append(" ")
                .append(configs.get("spark.cores.max"));
        return sb.toString();
    }


    /**
     * 去除小数后面的多余0
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s){
        if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    public static void main(String[] args) {
//        Map m = randomConfigMap();
//        System.out.println(m);
        System.out.println(getParameterNames());
    }

}
