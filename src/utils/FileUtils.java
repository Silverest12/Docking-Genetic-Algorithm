package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
}
