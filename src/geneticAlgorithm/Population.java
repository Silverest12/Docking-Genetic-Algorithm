package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.*;
import java.util.stream.Collectors;

public class Population {

    private final int popNumber;
    private final int elemLength;
    private final List<String> elemPool;
    private final List<String> postePool;
    private final HashMap<DNA, Integer> popDNAs;
    private final DockData dockData;

    public Population(int n, DockData dockData) {
        popNumber = n;
        this.elemPool = dockData.getNaviresList();
        this.postePool = dockData.getPostesList();
        this.dockData = dockData;
        this.elemLength = elemPool.size();
        popDNAs = new HashMap<>();
    }

    private class DNA {
        private final LinkedHashMap<String, List<Navire>> dnaMap;

        DNA () {
            dnaMap = generateDNA();
        }

        DNA (List<Navire> listeNavire) {
            dnaMap = checkSubmittedList(listeNavire);
            System.out.println(dnaMap);
            swapOutDups();
        }

        private LinkedHashMap<String, List<Navire>> checkSubmittedList (List<Navire> listeNavire) {
            LinkedHashMap<String, List<Navire>> dnaMap = new LinkedHashMap<>();

            for (String poste : postePool) {
                listeNavire.forEach(System.out::print);
                System.out.println("\n");
                ArrayList<Navire> list = new ArrayList<>();
                String lastId;
                do {
                    Navire n = listeNavire.get(0);
                    listeNavire.remove(n);
                    lastId = n.getIdNavire();
                    if (!lastId.equals("0")) {
                        n.setDureeServ(dockData.getDureeServ(lastId, poste));
                        list.add(n);
                    }
                } while (!lastId.equals("0"));
                dnaMap.putIfAbsent(poste, list);
            }

            return dnaMap;
        }

        public int calcFitness () {
            int sum = 0;
            for (String p : dnaMap.keySet()) {
                for (Navire n : dnaMap.get(p)) {
                    sum += n.getHeureDeb() - n.getHeureArr();
                }
            }
            return sum;
        }

        private List<String> identifyMissing (Set<String> setNvr) {
            List<String> elemPoolCopy = new ArrayList<>(elemPool);
            elemPoolCopy.removeAll(setNvr);
            return elemPoolCopy;
        }

        private void swapOutDups () {
            List<String> missingIds = identifyMissing(flatten().stream().map(Navire::getIdNavire).collect(Collectors.toSet()));
            List<String> dups = checkForDups();

            Random rand = new Random();

            for (String p : dnaMap.keySet()) {
                List<Navire> nvrList = dnaMap.get(p);
                List<Integer> toRem = new LinkedList<>();
                for (int i = 0; i < nvrList.size(); i++) {
                   if (dups.contains(nvrList.get(i).getIdNavire())) {
                       toRem.add(i);
                   }
                }
                for (int x: toRem) {
                    Navire backUpN = nvrList.get(x);
                    dups.remove(nvrList.get(x).getIdNavire());
                    nvrList.remove(x);
                    Navire n = generateRandomNav(missingIds, rand, p);
                    int heureDeb;
                    do {
                        heureDeb = rand.nextInt(1440 - n.getDureeServ());
                    } while (heureDeb < n.getHeureArr() || checkIfOverlapped(nvrList, heureDeb, n.getDureeServ()));

                    n.setHeureDeb(heureDeb);
                    missingIds.remove(n.getIdNavire());
                    if (n.getIdNavire().equals("0")) {
                        dnaMap.get(p).add(backUpN);
                    } else {
                        dnaMap.get(p).add(n);
                    }
                }
            }
        }

        private List<String> checkForDups () {
            Set<String> nvrSet = new HashSet<>();
            List<String> toChange = new LinkedList<>();

            for (String poste: dnaMap.keySet()) {
                for (Navire nvr: dnaMap.get(poste)) {
                    if(!nvrSet.add(nvr.getIdNavire())) {
                      toChange.add(nvr.getIdNavire());
                    }
                }
            }

            return toChange;
        }

        private boolean checkIfOverlapped (List<Navire> listNvr, int startTime, int dureeServ) {
            int finServ = startTime + dureeServ;
            for (Navire n : listNvr) {
                if (n.getHeureDeb() <= startTime && startTime < n.getHeureDeb() + n.getDureeServ()
                        && n.getHeureDeb() <= finServ && finServ < n.getHeureDeb() + n.getDureeServ())
                    return true;
            }

            return false;
        }

        private Navire generateRandomNav (List<String> elemPoolCopy, Random rand, String poste) {
            String addr;
            int heureArr;
            int dureeServ;

            int counter = 0;

            do {
                addr = elemPoolCopy.get(rand.nextInt(elemPoolCopy.size()));
                heureArr = dockData.getHeureArr(addr);
                dureeServ = dockData.getDureeServ(addr, poste);
                if (counter > 5) {
                    return new Navire();
                }
                counter ++;
            } while (dureeServ == 0);

            return new Navire(addr, heureArr, heureArr, dureeServ);
        }

        private LinkedHashMap<String, List<Navire>> firstGen () {
            LinkedHashMap<String, List<Navire>> dnaMap = new LinkedHashMap<>();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);
            Random rand = new Random();

            for (String poste : postePool) {
                Navire n = generateRandomNav(elemPoolCopy, rand, poste);
                dnaMap.put(poste, new ArrayList<>());
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(n.getIdNavire());
            }

            return dnaMap;
        }

        private LinkedHashMap<String, List<Navire>> generateDNA () {
            LinkedHashMap<String, List<Navire>> dnaMap = firstGen();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);

            for(String str : dnaMap.keySet()) {
                for (Navire n : dnaMap.get(str)) {
                    elemPoolCopy.remove(n.getIdNavire());
                }
            }

            Random rand = new Random();
            int mapSize = dnaMap.size();
            while (mapSize != elemLength) {
                String poste;
                Navire n;
                do {
                    poste = postePool.get(rand.nextInt(postePool.size()));
                    n = generateRandomNav(elemPoolCopy, rand, poste);
                } while (n.getIdNavire().equals("0"));

                int heureDeb;
                do {
                    heureDeb = rand.nextInt(1440 - n.getDureeServ());
                } while (heureDeb < n.getHeureArr() || checkIfOverlapped(dnaMap.get(poste), heureDeb, n.getDureeServ()));

                n.setHeureDeb(heureDeb);
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(n.getIdNavire());
                mapSize ++;
            }

            return dnaMap;
        }

        private String toHour(int time) {
            return time / 60 +  "h" + (time % 60 == 0? "" : time % 60);
        }

        private ArrayList<Navire> flatten() {
            ArrayList<Navire> flattenedMap = new ArrayList<>();

            for (String p : dnaMap.keySet()) {
                flattenedMap.addAll(dnaMap.get(p));
                flattenedMap.add(new Navire());
            }

            return flattenedMap;
        }

        @Override
        public String toString() {
            StringBuilder strBld = new StringBuilder();

            strBld.append(String.format("%14s | ", " "));
            for (String poste: dnaMap.keySet()) {
                int i = dnaMap.get(poste).size() * 6;
                strBld.append(String.format("%" + i + "s | ", poste));
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "idNavire"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");

                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%5s ", nav.getIdNavire()));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Heure arrivee"));

            for(String p: dnaMap.keySet()) {
                strBld.append(" ");

                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%5s ", toHour(nav.getHeureArr())));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Heure Debut"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");

                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%5s ", toHour(nav.getHeureDeb())));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Duree service"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");

                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%5d ", nav.getDureeServ()));
                }
                strBld.append("| ");
            }

            return strBld.toString();
        }
    }

    public DNA naturalSelection () {
        DNA chromElite = null;
        int fit = Integer.MAX_VALUE;

        for (DNA dna: popDNAs.keySet()) {
            if (popDNAs.get(dna) < fit) {
                chromElite = dna;
                fit = popDNAs.get(dna);
            }
        }

        return chromElite;
    }

    public void swapNChop(List<Navire> chrom1, List<Navire> chrom2, int i, int j) {
        List<Navire> newDna1 = new ArrayList<>(chrom1.subList(0, i));
        newDna1.addAll(chrom2.subList(i, j));
        newDna1.addAll(chrom1.subList(j, chrom1.size()));

        List<Navire> newDna2 = new ArrayList<>(chrom2.subList(0, i));
        newDna2.addAll(chrom1.subList(i, j));
        newDna2.addAll(chrom2.subList(j, chrom2.size()));

        DNA dna1 = new DNA(newDna1);
        DNA dna2 = new DNA(newDna2);

        popDNAs.put(dna1, dna1.calcFitness());
        popDNAs.put(dna2, dna2.calcFitness());
    }

    public void croisement() {
        Random rand = new Random();
        ArrayList<DNA> dnaPool = new ArrayList<>(popDNAs.keySet());
        dnaPool.remove(naturalSelection());
        DNA dna1 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));
        dnaPool.remove(dna1);
        DNA dna2 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));

        List<Navire> flattenedDna1 = dna1.flatten();
        List<Navire> flattenedDna2 = dna2.flatten();

        int len1 = flattenedDna1.size();

        int j;
        do {
            j = rand.nextInt(len1) / 2 + len1 / 2;
        } while (j == 0);

        int i;
        do {
            i = rand.nextInt(len1) / 2;
        } while (j == i || i == 0);

        popDNAs.remove(dna1);
        popDNAs.remove(dna2);

        swapNChop(flattenedDna1, flattenedDna2, i, j);
    }

    public void generatePop () {
        for (int i = 0; i < popNumber; i++) {
            var newDna = new DNA();
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
