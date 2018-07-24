package lab.ride.record;

import lab.ride.entity.Record;
import lab.ride.parameter.Parameter;
import lab.ride.parameter.ParameterUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: cwz
 * Time: 2017/11/10
 * Description:
 */
public class CSVWriter {
    public static void write(String filePath, List<Record> records){
        File outputFile = new File(filePath);
        List<String> names = ParameterUtils.getParameterNames();
        try{
            BufferedWriter bw = null;
            if (!outputFile.exists()) {
                bw = new BufferedWriter(new FileWriter(outputFile, true));
                StringBuilder title = new StringBuilder();
                for (String name : names) {
                    title.append(name).append(",");
                }
                title.append("time");
                bw.write(title.toString());
            }

            if(bw == null){
                bw = new BufferedWriter(new FileWriter(outputFile, true));
            }
            for(Record record : records){
                StringBuilder line = new StringBuilder();
                for(int i = 0; i < names.size(); i++){
                    String name = names.get(i);
                    if(name.equals("spark.memory.fraction") || name.equals("spark.memory.storageFraction")){
                        line.append(record.getConfigs().get(name));
                    }else {
                        line.append(getNumbers(record.getConfigs().get(name)));
                    }
                    line.append(",");
                }
                line.append(record.getThroughput());
                bw.newLine();
                bw.write(line.toString());
            }
            bw.flush();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void writeRaw(String filePath, List<Record> records){
        List<String> names = ParameterUtils.getParameterNames();
        final String[] FILE_HEADER = new String[names.size() + 1];
        int i = 0;
        for(String name : names){
            FILE_HEADER[i++] = name;
        }
        FILE_HEADER[i] = "time";


        CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER);
        try(Writer out = new FileWriter(filePath)){
            CSVPrinter printer = new CSVPrinter(out, format);
            for (Record record : records) {
                List<String> cols = new ArrayList<>();
                for(String name : names){
                    if(name.equals("spark.memory.fraction") || name.equals("spark.memory.storageFraction")){
                        cols.add(record.getConfigs().get(name));
                    }else {
                        cols.add(getNumbers(record.getConfigs().get(name)));
                    }
                }
                cols.add(record.getThroughput() + "");
                printer.printRecord(cols);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeDummy(String filePath, List<Record> records){
        String spilt = "_";
        List<Parameter> parameters = ParameterUtils.parameters;
        List<String> fileHeader = new ArrayList<>();
        for(Parameter parameter : parameters){
            String type = parameter.getType();
            switch (type){
                case "bool":
                    fileHeader.add(parameter.getName() + spilt + "true");
                    fileHeader.add(parameter.getName() + spilt + "false");
                    break;
                case "enum":
                    if(parameter.getName().equals("spark.task.cpus")){
                        fileHeader.add(parameter.getName());
                    }else{
                        String[] values = parameter.getRange().split(" ");
                        for(String value : values){
                            fileHeader.add(parameter.getName() + spilt + value);
                        }
                    }
                    break;
                case "series":
                    fileHeader.add(parameter.getName());
                    break;
            }
        }
        fileHeader.add("time");

        final String[] FILE_HEADER = fileHeader.toArray(new String[]{});
        CSVFormat format = CSVFormat.DEFAULT.withHeader(FILE_HEADER);
        try(Writer out = new FileWriter(filePath)){
            CSVPrinter printer = new CSVPrinter(out, format);
            for (Record record : records) {
                List<String> cols = new ArrayList<>();
                for(String headerName : fileHeader){
                    String[] header = headerName.split(spilt);
                    String value = record.getConfigs().get(header[0]);
                    if(header.length == 1){
                        if(!header[0].equals("time")){
                            if(header[0].equals("spark.memory.fraction") || header[0].equals("spark.memory.storageFraction")){
                                cols.add(value);
                            }else {
                                cols.add(getNumbers(value));
                            }
                        }
                    }else{
                        if(header[1].equals(value)){
                            cols.add("1");
                        }else{
                            cols.add("0");
                        }
                    }
                }

                cols.add(record.getThroughput() + "");
                printer.printRecord(cols);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNumbers(String content) {
        Pattern pattern = Pattern.compile("^\\d+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            return matcher.group(0);
        }
        return content;
    }

    public static void main(String[] args) {
        System.out.println(getNumbers("96.0k"));
    }
}
