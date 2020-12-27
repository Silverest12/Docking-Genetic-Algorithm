package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.*;
import java.util.stream.Collectors;

public class DNA {

    public static DNA fromListToDNA (List<Navire> listNavire, List<String> postePool, DockData dockData) {
        Map<String, List<Navire>> dnaMap = new LinkedHashMap<>();
        List<Navire> listeNavire = new ArrayList<>(listNavire);

        for (String poste : postePool) {
            List<Navire> list = new ArrayList<>();

            String lastId;
            do {
                Navire n = listeNavire.get(0);
                listeNavire.remove(0);
                lastId = n.getIdNavire();
                if (!lastId.equals("0")) {
                    n.setDureeServ(dockData.getDureeServ(lastId, poste));
                    list.add(n);
                }
            } while (!lastId.equals("0"));

            dnaMap.put(poste, list);
        }

        return new DNA(dockData, dnaMap);
    }

    public static DNA copyOf(DNA dna) {
        return new DNA(dna.getDockData(), new LinkedHashMap<>(dna.getDnaMap()));
    }

    public static DNA genRand(DockData dockData) {
        return new DNA(dockData);
    }

    public static DNA empty() {
        return null;
    }

    public static DNA mutate (DNA dna) {
        DNA dnaCopy = DNA.copyOf(dna);
        List<String> postePool = dnaCopy.postePool;
        Random rand = new Random();
        Map<String, List<Navire>> dnaMap = dnaCopy.getDnaMap();
        List<Navire> f;

        do {
            String p1 = postePool.get(rand.nextInt(postePool.size()));
            postePool.remove(p1);
            String p2 = postePool.get(rand.nextInt(postePool.size()));
            postePool.add(p1);

            Navire nvr1 = dnaMap.get(p1).get(rand.nextInt(dnaMap.get(p1).size()));
            Navire nvr2 = dnaMap.get(p2).get(rand.nextInt(dnaMap.get(p2).size()));

            dnaCopy.swapNvr(nvr1, nvr2, p1, p2);

            f = dnaCopy.flatten();
            dnaCopy.fixDna();
        } while (!dnaCopy.isValid() || dnaCopy.isSame(f, dnaCopy.flatten()));

        return dnaCopy;
    }

    private Map<String, List<Navire>> dnaMap;
    private final List<String> elemPool;
    private final List<String> postePool;
    private final DockData dockData;
    private final Random rand = new Random();
    
    private DNA (DockData dockData) {
        this.dockData = dockData;
        elemPool = dockData.getNaviresList();
        postePool = dockData.getPostesList();
        dnaMap = generateDNA();
    }

    private DNA (DockData dockData, Map<String, List<Navire>> dnaMap) {
        this.dockData = dockData;
        elemPool = dockData.getNaviresList();
        postePool = dockData.getPostesList();
        this.dnaMap = dnaMap;
    }

    public DockData getDockData() {
        return dockData;
    }

    public Map<String, List<Navire>> getDnaMap() {
        return dnaMap;
    }

    public void check() {
        DNA dna;

        do {
            dna = DNA.copyOf(this);
            dna.fixDna();
        } while (!dna.isValid());

        this.dnaMap = dna.dnaMap;
    }

    private boolean isSame (List<Navire> f1, List<Navire> f2) {
        for (int i = 0; i < f1.size(); i++) {
            if (!f1.get(i).getIdNavire().equals(f2.get(i).getIdNavire()))
                return false;
        }

        return true;
    }

    private LinkedHashMap<String, List<Navire>> generateDNA () {
        LinkedHashMap<String, List<Navire>> dnaMap = firstGen();
        List<String> elemPoolCopy = new ArrayList<>(elemPool);

        for(String str : dnaMap.keySet()) {
            for (Navire n : dnaMap.get(str)) {
                elemPoolCopy.remove(n.getIdNavire());
            }
        }

        int elemLen = elemPool.size();
        int mapSize = dnaMap.size();

        while (mapSize != elemLen) {
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

    private void setJobTimer () {
        for (String poste: dnaMap.keySet()) {
            List<Navire> nvrList = dnaMap.get(poste);
            List<Navire> cp = List.copyOf(nvrList);
            for (Navire nvr: cp) {
                nvr.setDureeServ(dockData.getDureeServ(nvr.getIdNavire(), poste));
                nvrList.remove(nvr);
                while (nvr.getHeureDeb() < nvr.getHeureArr() || isOverlapped(nvrList, nvr.getHeureDeb(), nvr.getDureeServ())) {
                    nvr.setHeureDeb(rand.nextInt(1440 - nvr.getDureeServ()));
                }
                nvrList.add(nvr);
            }
        }
    }

    private Navire putRandomNvr (String poste, String idNvr) {
        int elemLen = elemPool.size();
        Navire nvr;

        do {
            String id = elemPool.get(rand.nextInt(elemLen));
            nvr = new Navire(id, dockData.getHeureArr(id), rand.nextInt(1440 - dockData.getDureeServ(id, poste)), dockData.getDureeServ(id, poste));
        } while (idNvr.equals(nvr.getIdNavire()) && dockData.getDureeServ(nvr.getIdNavire(), poste) != 0);
        return nvr;
    }

    private void swapNvr (Navire nvr1, Navire nvr2, String poste1, String poste2){
        dnaMap.get(poste1).remove(nvr1);
        dnaMap.get(poste2).remove(nvr2);

        nvr1.setDureeServ(dockData.getDureeServ(nvr1.getIdNavire(), poste2));
        nvr2.setDureeServ(dockData.getDureeServ(nvr2.getIdNavire(), poste1));

        dnaMap.get(poste1).add(nvr2);
        dnaMap.get(poste2).add(nvr1);
    }

    private void clearDups () {
        while (hasDups()) {
            Set<String> visited = new HashSet<>();
            for (String poste : dnaMap.keySet()) {
                for (Navire nvr : dnaMap.get(poste)) {
                    if (visited.contains(nvr.getIdNavire())) {
                        Navire newNvr = putRandomNvr(poste, nvr.getIdNavire());
                        int i = dnaMap.get(poste).indexOf(nvr);
                        dnaMap.get(poste).set(i, newNvr);
                        visited.add(newNvr.getIdNavire());
                    } else visited.add(nvr.getIdNavire());
                }
            }
        }
    }

    private void swapFalseServTime () {
        for (String p: dnaMap.keySet()) {

            for (int i = 0; i < dnaMap.get(p).size(); i++) {
                Navire nvr = dnaMap.get(p).get(i);
                if (nvr.getDureeServ() == 0) {
                    postePool.remove(p);
                    String poste = postePool.get(rand.nextInt(postePool.size()));
                    postePool.add(p);

                    List<Navire> nvrList = dnaMap.get(poste);
                    Navire swpNvr = nvrList.get(rand.nextInt(nvrList.size()));

                    swapNvr(nvr, swpNvr, p, poste);
                }
            }
        }
    }

    private boolean hasDups () {
        return flatten().stream()
                .map(Navire::getIdNavire)
                .collect(Collectors.toSet())
                .size() - 1 != elemPool.size();
    }

    public void fixDna () {
        List<Navire> tmp = new LinkedList<>();

        for (String poste: dnaMap.keySet()) {
            if (!checkTimeDiff(dnaMap.get(poste)) && dnaMap.get(poste).size() == 1) {
                tmp.add(dnaMap.get(poste).get(0));
            }
        }

        tmp.forEach(x -> x.setHeureDeb(x.getHeureArr()));

        if(hasDups()) clearDups();
        swapFalseServTime();
        setJobTimer();
    }

    public boolean isValid() {
        for (String p: dnaMap.keySet()) {
            if(dnaMap.get(p).isEmpty())
                return false;
            for(Navire nvr: dnaMap.get(p)) {
                if(nvr.getHeureDeb() < nvr.getHeureArr()
                        || isOverlapped(dnaMap.get(p), nvr.getHeureDeb(), nvr.getDureeServ())
                        || nvr.getIdNavire().equals("0"))
                    return false;
            }
        }

        List<Navire> tmpList = flatten();

        return tmpList.size() - postePool.size() == elemPool.size() && dnaMap.keySet().size() == postePool.size() && !hasDups();
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

    private boolean isOverlapped (List<Navire> listNvr, int startTime, int dureeServ) {
        int finServ = startTime + dureeServ;
        for (Navire n : listNvr) {
            if ((n.getHeureDeb() < startTime && startTime < n.getHeureDeb() + n.getDureeServ())
                    || (n.getHeureDeb() < finServ && finServ < n.getHeureDeb() + n.getDureeServ()))
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

    public ArrayList<Navire> flatten() {
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
