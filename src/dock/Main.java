package dock;

import geneticAlgorithm.Population;
import utils.FileUtils;
import utils.TextParser;

import java.util.Scanner;

public class Main {
    public static void main(String[] arr) {
        //Scanner in = new Scanner(System.in);
        //String fileName = in.nextLine();
        //in.close();
        String fileName = "inputTest.txt";
        DockData dock = TextParser.convertTextToDoc(FileUtils.readFile(fileName));

        Population pop = new Population(4, dock);

        pop.generatePop();
        pop.print();
    }
}
