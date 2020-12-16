package geneticAlgorithm;

import dock.DockData;
import dock.Navire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Population {

    private final int popNumber;
    private final int elemLength;
    private final List<String> elemPool;
    private final List<String> postePool;
    private ArrayList<DNA> popDNAs;
    private final DockData dockData;

    public Population(int n, DockData dockData) {
        popNumber = n;
        this.elemPool = dockData.getNaviresList();
        this.postePool = dockData.getPostesList();
        this.dockData = dockData;
        this.elemLength = elemPool.size();
        popDNAs = new ArrayList<>();
    }

    private class DNA {
        private final HashMap<String, ArrayList<Navire>> dnaMap;

        DNA () {
            dnaMap = generateDNA();
        }

        public HashMap<String, ArrayList<Navire>> getDNA() {
            return dnaMap;
        }

        private boolean checkIfOverlapped (ArrayList<Navire> listNvr, int startTime) {
            if (listNvr == null) return true;
            else {
                for (Navire n : listNvr) {
                    if (n.getHeureDeb() <= startTime && startTime < n.getHeureDeb() + n.getDureeServ())
                        return false;
                }
            }
            return true;
        }

        private HashMap<String, ArrayList<Navire>> generateDNA () {
            HashMap<String, ArrayList<Navire>> dnaMap = new HashMap<>();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);
            Random rand = new Random();
            int mapSize = 0;
            while (mapSize != elemLength) {
                String addr = elemPoolCopy.get(rand.nextInt(elemPoolCopy.size()));
                String poste = postePool.get(rand.nextInt(postePool.size()));
                int heureArr = dockData.getHeureArr(addr);
                int dureeServ = dockData.getDureeServ(addr, poste);
                int heureDeb = (int) (rand.nextDouble() * (1440 - dureeServ) + heureArr);
                if (dureeServ != 0 && checkIfOverlapped(dnaMap.get(poste), heureDeb)) {
                    Navire n = new Navire(addr, heureArr, heureDeb, dureeServ);
                    dnaMap.putIfAbsent(poste, new ArrayList<>());
                    dnaMap.get(poste).add(n);
                    elemPoolCopy.remove(addr);
                    mapSize ++;
                }
            }

            return dnaMap;
        }

        @Override
        public String toString() {
            StringBuilder strBld = new StringBuilder();

            strBld.append(String.format("%14s | ", " "));
            for (String poste: dnaMap.keySet()) {
                int i = dnaMap.get(poste).size() * 5;
                strBld.append(String.format("%" + i + "s | ", poste));
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "idNavire"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");
                if (dnaMap.get(p).size() == 1) {
                    strBld.append(" ");
                }
                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%4s ", nav.getIdNavire()));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Heure Debut"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");
                if (dnaMap.get(p).size() == 1) {
                    strBld.append(" ");
                }
                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%4d ", nav.getHeureDeb()));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Heure arrivee"));

            for(String p: dnaMap.keySet()) {
                strBld.append(" ");
                if (dnaMap.get(p).size() == 1) {
                    strBld.append(" ");
                }
                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%4d ", nav.getHeureArr()));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            strBld.append(String.format("%14s | ", "Duree service"));
            for(String p: dnaMap.keySet()) {
                strBld.append(" ");
                if (dnaMap.get(p).size() == 1) {
                    strBld.append(" ");
                }
                for (Navire nav: dnaMap.get(p)) {
                    strBld.append(String.format("%4d ", nav.getDureeServ()));
                }
                strBld.append("| ");
            }
            strBld.append("\n");

            return strBld.toString();
        }
    }

    public void generatePop () {
        for (int i = 0; i < popNumber; i++)
            popDNAs.add(new DNA());
    }

    public void print() {
        popDNAs.forEach(System.out::println);
    }
}
