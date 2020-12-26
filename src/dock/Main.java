package dock;

import geneticAlgorithm.Population;
import utils.FileUtils;
import utils.TextParser;

import java.util.ArrayList;
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

        System.out.println("gen 0 :");
        pop.print();
        System.out.println();
        for (int i =1; i <= 4; i++ ) {
            pop.getNextGen();
            System.out.println("gen " + i + " : ");
            pop.print();
            System.out.println();
        }
    }
}
