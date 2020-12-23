package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.*;
import java.util.stream.Collectors;

public class Population {

    private class DNA {
        private final LinkedHashMap<String, List<Navire>> dnaMap;

        DNA () {
            dnaMap = generateDNA();
        }

        DNA (List<Navire> listeNavire) {
            dnaMap = checkSubmittedList(listeNavire);
            swapOutDups();
            fixDna();
        }

        public void mutate () {
            boolean mutated;

            do {
                String p1 = postePool.get(rand.nextInt(postePool.size()));
                Navire nvr1 = dnaMap.get(p1).get(rand.nextInt(dnaMap.get(p1).size()));
                String p2;
                do {
                    p2 = postePool.get(rand.nextInt(postePool.size()));
                } while (p2.equals(p1));

                int dureeSrv1 = dockData.getDureeServ(nvr1.getIdNavire(), p2);
                if (dureeSrv1 != 0) {
                    Navire nvr2 = dnaMap.get(p2).get(rand.nextInt(dnaMap.get(p2).size()));
                    int dureeSrv2 = dockData.getDureeServ(nvr2.getIdNavire(), p1);
                    if (dureeSrv2 != 0) {
                        dnaMap.get(p1).remove(nvr1);
                        dnaMap.get(p2).remove(nvr2);
                        nvr1.setDureeServ(dureeSrv1);
                        nvr2.setDureeServ(dureeSrv2);
                        dnaMap.get(p1).add(nvr2);
                        dnaMap.get(p2).add(nvr1);
                        mutated = true;
                    } else mutated = false;
                } else mutated = false;
            } while (!mutated || !isValid());
        }

        private void swapOutDups () {

            List<String> missingIds = identifyMissing(flatten().stream().map(Navire::getIdNavire).collect(Collectors.toSet()));
            List<String> dups = checkForDups();

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
                    Navire n = generateRandomNav(missingIds, p);
                    int heureDeb;
                    do {
                        heureDeb = rand.nextInt(1440 - n.getDureeServ());
                    } while (heureDeb < n.getHeureArr() || isOverlapped(nvrList, heureDeb, n.getDureeServ()));

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

        private LinkedHashMap<String, List<Navire>> generateDNA () {
            LinkedHashMap<String, List<Navire>> dnaMap = firstGen();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);

            for(String str : dnaMap.keySet()) {
                for (Navire n : dnaMap.get(str)) {
                    elemPoolCopy.remove(n.getIdNavire());
                }
            }

            int mapSize = dnaMap.size();
            while (mapSize != elemLength) {
                String poste;
                Navire n;
                do {
                    poste = postePool.get(rand.nextInt(postePool.size()));
                    n = generateRandomNav(elemPoolCopy, poste);
                } while (n.getIdNavire().equals("0"));

                int heureDeb;
                do {
                    heureDeb = rand.nextInt(1440 - n.getDureeServ());
                } while (heureDeb < n.getHeureArr() || isOverlapped(dnaMap.get(poste), heureDeb, n.getDureeServ()));

                n.setHeureDeb(heureDeb);
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(n.getIdNavire());
                mapSize ++;
            }

            return dnaMap;
        }

        private boolean checkTimeDiff (List<Navire> nvrList) {
            for (Navire nvr: nvrList) {
                if (nvr.getHeureArr() - nvr.getHeureDeb() == 0) {
                    return true;
                }
            }
            return false;
        }

        private void fixDna () {
            List<Navire> tmp = new LinkedList<>();
            for (String poste: dnaMap.keySet()) {
                if (!checkTimeDiff(dnaMap.get(poste)) && !dnaMap.get(poste).isEmpty()) {
                    tmp.add(dnaMap.get(poste).get(0));
                }
            }

            tmp.forEach(x -> x.setHeureDeb(x.getHeureArr()));

            for (String poste: dnaMap.keySet()) {
                List<Navire> nvrList = dnaMap.get(poste);
                for (Navire nvr: nvrList) {
                    while (nvr.getHeureDeb() < nvr.getHeureArr() || isOverlapped(nvrList, nvr.getHeureDeb(), nvr.getDureeServ())) {
                        nvr.setHeureDeb(rand.nextInt(1440 - nvr.getDureeServ()));
                    }
                }
            }
        }

        private boolean isValid() {
            for (String p: dnaMap.keySet()) {
                for(Navire nvr: dnaMap.get(p)) {
                     if(nvr.getHeureDeb() < nvr.getHeureArr()
                             || isOverlapped(dnaMap.get(p), nvr.getHeureDeb(), nvr.getDureeServ())
                             || nvr.getIdNavire().equals("0"))
                         return false;
                }
            }

            Set<Navire> tmpList = new HashSet<>(flatten());
            return tmpList.size() - 4 == elemLength && dnaMap.keySet().size() == postePool.size();
        }

        private LinkedHashMap<String, List<Navire>> checkSubmittedList (List<Navire> listeNavire) {
            LinkedHashMap<String, List<Navire>> dnaMap = new LinkedHashMap<>();

            int itr = 0;
            for (String poste : postePool) {
                ArrayList<Navire> list = new ArrayList<>();

                if(itr >= listeNavire.size()) {
                    break;
                }

                String lastId;
                do {
                    Navire n = listeNavire.get(itr++);
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

        private boolean isOverlapped (List<Navire> listNvr, int startTime, int dureeServ) {
            int finServ = startTime + dureeServ;
            for (Navire n : listNvr) {
                if (n.getHeureDeb() <= startTime && startTime < n.getHeureDeb() + n.getDureeServ()
                        && n.getHeureDeb() <= finServ && finServ < n.getHeureDeb() + n.getDureeServ())
                    return true;
            }

            return false;
        }

        private Navire generateRandomNav (List<String> elemPoolCopy, String poste) {
            String addr;
            int heureArr;
            int dureeServ;

            int counter = 0;

            do {
                if (!elemPoolCopy.isEmpty()) {
                    int id = rand.nextInt(elemPoolCopy.size());
                    addr = elemPoolCopy.get(id);
                    heureArr = dockData.getHeureArr(addr);
                    dureeServ = dockData.getDureeServ(addr, poste);
                    if (counter > 5) {
                        return new Navire();
                    }
                    counter++;
                } else {
                    return new Navire();
                }
            } while (dureeServ == 0);

            return new Navire(addr, heureArr, heureArr, dureeServ);
        }

        private LinkedHashMap<String, List<Navire>> firstGen () {
            LinkedHashMap<String, List<Navire>> dnaMap = new LinkedHashMap<>();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);

            for (String poste : postePool) {
                Navire n = generateRandomNav(elemPoolCopy, poste);
                dnaMap.put(poste, new ArrayList<>());
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(n.getIdNavire());
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
                strBld.append(String.format("%" + Math.max(i, 1) + "s | ", poste));
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

            strBld.append("\n").append(isValid());
            return strBld.toString();
        }
    }

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
            if (popDNAs.get(dna) < fit) {
                chromElite = dna;
                fit = popDNAs.get(dna);
            }
        }

        return chromElite;
    }

    public boolean swapNChop(DNA dna1, DNA dna2, int i, int j) {
        List<Navire> chrom1 = dna1.flatten();
        List<Navire> chrom2 = dna2.flatten();
        List<Navire> newDna1 = new ArrayList<>(chrom1.subList(0, i));
        newDna1.addAll(chrom2.subList(i, j));
        newDna1.addAll(chrom1.subList(j, chrom1.size()));

        List<Navire> newDna2 = new ArrayList<>(chrom2.subList(0, i));
        newDna2.addAll(chrom1.subList(i, j));
        newDna2.addAll(chrom2.subList(j, chrom2.size()));

        if (newDna1.size() != chrom1.size() || newDna2.size() != chrom2.size()) {
            return false;
        }

        DNA cross1 = new DNA(newDna1);
        DNA cross2 = new DNA(newDna2);

        if (cross1.isValid() && cross1.calcFitness() >= dna1.calcFitness()) {
            popDNAs.put(cross1, cross1.calcFitness());
            popDNAs.remove(dna1);
        } else if (!cross1.isValid()) return false;

        if (cross2.isValid() && cross2.calcFitness() >= dna2.calcFitness()) {
                popDNAs.put(cross2, cross2.calcFitness());
                popDNAs.remove(dna2);
        } else if (!cross2.isValid()) return false;

        return true;
    }

    public void croisement() {
        List<DNA> dnaPool = new ArrayList<>(popDNAs.keySet());
        dnaPool.remove(naturalSelection());
        DNA dna1, dna2;
        int i, j;
        do {
            dna1 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));
            dnaPool.remove(dna1);
            dna2 = new ArrayList<>(dnaPool).get(rand.nextInt(dnaPool.size()));
            dnaPool.add(dna1);
            List<Navire> flattenedDna1 = dna1.flatten();

            int len1 = flattenedDna1.size();

            do {
                j = rand.nextInt(len1 / 2) + len1/2;
            } while (j == 0 || flattenedDna1.get(j).getIdNavire().equals("0"));

            do {
                i = rand.nextInt(j);
            } while (j == i || i == 0 || flattenedDna1.get(i).getIdNavire().equals("0"));

        } while (!swapNChop(dna1, dna2, i, j));
    }

    public void mutation() {
        List<DNA> dnaPool = new ArrayList<>(popDNAs.keySet());
        dnaPool.remove(naturalSelection());
        DNA dna = dnaPool.get(rand.nextInt(dnaPool.size()));
        popDNAs.remove(dna);
        dna.mutate();
        popDNAs.put(dna, dna.calcFitness());
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
