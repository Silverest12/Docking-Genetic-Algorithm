package dock;

import utils.FileUtils;
import utils.TextParser;

import java.util.Scanner;

public class Main {
    public static void main(String[] arr) {
        Scanner in = new Scanner(System.in);
        String fileName = in.nextLine();
        DockData dock = TextParser.convertTextToDoc(FileUtils.readFile(fileName));
        System.out.println(dock);
        in.close();
    }
}
