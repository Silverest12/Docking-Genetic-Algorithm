package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.*;
public class Population {

    private final int popNumber;
    private final int elemLength;
    private final List<String> postePool;
    // Chrom -> Fitness
    private final HashMap<DNA, Integer> popDNAs;
    private final DockData dockData;
    private final Random rand;

    public Population(int n, DockData dockData) {
        this.popNumber = n;
        this.rand = new Random();
        this.postePool = dockData.getPostesList();
        this.dockData = dockData;
        this.elemLength = dockData.getNaviresList().size();
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

    public void getNextGen(double pc, double pm) {
        List<DNA> popKeys1 = new ArrayList<>(popDNAs.keySet());
        popKeys1.remove(naturalSelection());
        List<DNA> popKeys2 = new ArrayList<>(popKeys1);

        int cItr = iterCroisement(pc);
        int mItr = iterMut(pm);

        while (cItr-- >  0) croisement(popKeys1);

        while (mItr-- > 0) mutation(popKeys2);

        while (popDNAs.size() > popNumber) popDNAs.remove(maxFitnessDna());
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

        if (cross1.isValid() && cross2.isValid()) {
            System.out.println("Croisement de : \n" + chrom1 + "\n" + chrom2 + "\nen " + i + " " + j + "\n");
            System.out.println("Resultat : \n" + newDna1 + "\n" + newDna2 + "\n");
            popDNAs.put(cross1, cross1.calcFitness());
            popDNAs.put(cross2, cross2.calcFitness());
            System.out.println("Correction : \n" + cross1.flatten() + "\n" + cross2.flatten() + "\n");
        } else return false;

        return true;
    }

    public void croisement(List<DNA> dnaPool) {
        List<Navire> flattenedDna1, flattenedDna2;

        int i, j;
        DNA dna1, dna2;

        do {
            dna1 = dnaPool.get(rand.nextInt(dnaPool.size()));
            dnaPool.remove(dna1);
            dna2 = dnaPool.get(rand.nextInt(dnaPool.size()));
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
        dnaPool.remove(dna1);
        dnaPool.remove(dna2);
    }

    public void mutation(List<DNA> dnaPool) {
        DNA dna = dnaPool.get(rand.nextInt(dnaPool.size()));
        System.out.println("Mutation de : \n" + dna.flatten() + "\n");
        dnaPool.remove(dna);
        DNA mutatedDna = DNA.mutate(dna);

        if(mutatedDna.isValid() || mutatedDna.calcFitness() != dna.calcFitness()) {
            popDNAs.put(mutatedDna, mutatedDna.calcFitness());
        }

        System.out.println("Correction : \n" + mutatedDna.flatten() + "\n");
    }

    public int iterCroisement (double pc) {
        int n = (int) ((double) popNumber * pc); //4 * 0.8 = 3,.. => 3 | 8 * 0.8 = 6,.. => 6
        return Math.max(1, (int) ((double) (n - n % 2) / 2)); // 3 - 3 % 2 = 2 / 2 => 1 | 6 => 3
    }

    public int iterMut (double pm) {
        return Math.max(1, (int) ((double) popNumber * pm)); // 4 * 0.1 = 0.4 => 1
    }

    public void generatePop () {
        for (int i = 0; i < popNumber; i++) {
            DNA newDna = DNA.genRand(dockData);
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
