package utils;

import java.io.*;

public class FileUtils {
    public static String readFile(String fileName) {
       File f = new File(fileName);
       StringBuilder strBld = new StringBuilder();

       try (BufferedReader br = new BufferedReader(new FileReader(f))) {
           String str;
           while ((str = br.readLine()) != null) {
               strBld.append(str).append("\n");
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
       return strBld.toString();
    }

    public static void writeToFile(String fileName, String outPut) throws IOException {
        File f = new File(fileName);

        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(outPut);
        bw.close();
    }
}
