package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.*;
public class Population {

    private final int popNumber;
    private final int elemLength;
    private final List<String> elemPool;
    private final List<String> postePool;
    private final HashMap<DNA, Integer> popDNAs;
    private final DockData dockData;
    private final Random rand;

    public Population(int n, DockData dockData) {
        this.popNumber = n;
        this.rand = new Random();
        this.elemPool = dockData.getNaviresList();
        this.postePool = dockData.getPostesList();
        this.dockData = dockData;
        this.elemLength = elemPool.size();
        this.popDNAs = new HashMap<>();
    }

    public DNA naturalSelection () {
        DNA chromElite = null;
        int fit = Integer.MAX_VALUE;

        for (DNA dna: popDNAs.keySet()) {
            if(fit > dna.calcFitness()) {
                chromElite = dna;
                fit = popDNAs.get(dna);
            }
        }

        return chromElite;
    }

    private boolean checkSeps (List<Navire> dna) {
        return dna.stream().filter(x -> x.getIdNavire().equals("0")).count() != postePool.size()
                && dna.stream().filter(x -> !x.getIdNavire().equals("0")).count() != elemLength;
    }

    public boolean swapNChop (List<Navire> chrom1, List<Navire> chrom2, int i, int j) {
        List<Navire> newDna1 = new ArrayList<>(chrom1.subList(0, i));
        newDna1.addAll(chrom2.subList(i, j));
        newDna1.addAll(chrom1.subList(j, chrom1.size()));

        List<Navire> newDna2 = new ArrayList<>(chrom2.subList(0, i));
        newDna2.addAll(chrom1.subList(i, j));
        newDna2.addAll(chrom2.subList(j, chrom2.size()));

        if (checkSeps(newDna1)
                || checkSeps(newDna2))
            return false;

        DNA cross1 = DNA.fromListToDNA(newDna1, postePool, dockData);
        cross1.check();

        DNA cross2 = DNA.fromListToDNA(newDna2, postePool, dockData);
        cross2.check();

        if (cross1.isValid()) {
            popDNAs.put(cross1, cross1.calcFitness());
        } else return false;

        if (cross2.isValid()) {
            popDNAs.put(cross2, cross2.calcFitness());
        } else return false;

        return true;
    }

    private DNA maxFitnessDna() {
        DNA maxDna = DNA.empty();
        int max = Integer.MIN_VALUE;
        for (DNA dna: popDNAs.keySet()) {
            if(max < popDNAs.get(dna)) {
                max = popDNAs.get(dna);
                maxDna = dna;
            }
        }
        return maxDna;
    }

    public void getNextGen() {
        croisement();
        mutation();
        while (popDNAs.size() > 4) {
            popDNAs.remove(maxFitnessDna());
        }
    }

    public void croisement() {
        List<DNA> dnaPool = new ArrayList<>(popDNAs.keySet());
        dnaPool.remove(naturalSelection());
        List<Navire> flattenedDna1, flattenedDna2;

        int i, j;

        do {
            DNA dna1 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));
            dnaPool.remove(dna1);
            DNA dna2 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));
            dnaPool.add(dna1);

            flattenedDna1 = dna1.flatten();
            flattenedDna2 = dna2.flatten();

            int len1 = flattenedDna1.size();

            do {
                j = rand.nextInt(len1 / 2 - 1) + len1/2 - 1;
            } while (flattenedDna1.get(j).getIdNavire().equals("0")
                    || flattenedDna2.get(j).getIdNavire().equals("0"));

            do {
                i = rand.nextInt(j);
            } while (j == i || flattenedDna1.get(i).getIdNavire().equals("0")
                    || flattenedDna2.get(i).getIdNavire().equals("0") );

        } while (!swapNChop(flattenedDna1, flattenedDna2, i, j));
        System.out.println("Croisement de : \n" + flattenedDna1 + "\n" + flattenedDna2 + "\nen " + i + " " + j);
    }

    public void mutation() {
        List<DNA> dnaPool = new ArrayList<>(popDNAs.keySet());
        dnaPool.remove(naturalSelection());
        DNA dna = dnaPool.get(rand.nextInt(dnaPool.size()));
        DNA newDna = DNA.copyOf(dna);
        do {
            newDna.mutate();
            popDNAs.put(newDna, newDna.calcFitness());
        } while (!newDna.isValid());
    }

    public void generatePop () {
        for (int i = 0; i < popNumber; i++) {
            var newDna = DNA.genRand(dockData);
            newDna.fixDna();
            popDNAs.put(newDna, newDna.calcFitness());
        }
    }

    public void print() {
        for(DNA chromosome: popDNAs.keySet()) {
            System.out.println (chromosome);
            System.out.println ("Fitness : " + popDNAs.get(chromosome));
        }
    }
}
