package dock;

import geneticAlgorithm.Population;
import utils.FileUtils;
import utils.TextParser;

import java.util.Scanner;

public class Main {
    public static void main(String[] arr) {
        Scanner in = new Scanner(System.in);
        System.out.print("Entrez le nom du fichier : ");
        String fileName = in.nextLine();
        System.out.print("Entrez le nombre de chromosome dans une population : ");
        int n = in.nextInt();
        System.out.print("Entrez Pc : ");
        double pc = in.nextDouble();
        System.out.print("Entrez Pm : ");
        double pm = in.nextDouble();
        in.close();
        DockData dock = TextParser.convertTextToDoc(FileUtils.readFile(fileName));
        Population pop = new Population(n, dock);
        pop.generatePop();

        System.out.println("gen 0 :");
        pop.print();
        System.out.println();
        for (int i =1; i <= 10; i++ ) {
            pop.getNextGen(pc, pm);
            System.out.println("gen " + i + " : ");
            pop.print();
            System.out.println();
        }
    }
}
