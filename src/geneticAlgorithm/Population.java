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
    private final ArrayList<DNA> popDNAs;
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

        public int calcFitness () {
            int sum = 0;
            for (String p : dnaMap.keySet()) {
                for (Navire n : dnaMap.get(p)) {
                    sum += n.getHeureDeb() - n.getHeureArr();
                }
            }
            return sum;
        }

        private boolean checkIfOverlapped (ArrayList<Navire> listNvr, int startTime, int dureeServ) {
            int finServ = startTime + dureeServ;
            if (listNvr == null) return true;
            else {
                for (Navire n : listNvr) {
                    if (n.getHeureDeb() <= startTime && startTime < n.getHeureDeb() + n.getDureeServ()
                        && n.getHeureDeb() <= finServ && finServ < n.getHeureDeb() + n.getDureeServ())
                        return false;
                }
            }
            return true;
        }

        private HashMap<String, ArrayList<Navire>> firstGen () {
            HashMap<String, ArrayList<Navire>> dnaMap = new HashMap<>();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);
            Random rand = new Random();

            for (String poste : postePool) {
                String addr;
                int heureArr;
                int dureeServ;

                do {
                    addr = elemPoolCopy.get(rand.nextInt(elemPoolCopy.size()));
                    heureArr = dockData.getHeureArr(addr);
                    dureeServ = dockData.getDureeServ(addr, poste);
                } while (dureeServ == 0);

                Navire n = new Navire(addr, heureArr, heureArr, dureeServ);
                dnaMap.put(poste, new ArrayList<>());
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(addr);
            }

            return dnaMap;
        }

        private HashMap<String, ArrayList<Navire>> generateDNA () {
            HashMap<String, ArrayList<Navire>> dnaMap = firstGen();
            List<String> elemPoolCopy = new ArrayList<>(elemPool);

            for(String str : dnaMap.keySet()) {
                for (Navire n : dnaMap.get(str)) {
                    elemPoolCopy.remove(n.getIdNavire());
                }
            }

            Random rand = new Random();
            int mapSize = dnaMap.size();
            while (mapSize != elemLength) {
                String addr;
                String poste;
                int dureeServ;
                int heureDeb;

                do {
                    addr = elemPoolCopy.get(rand.nextInt(elemPoolCopy.size()));
                    poste = postePool.get(rand.nextInt(postePool.size()));
                    dureeServ = dockData.getDureeServ(addr, poste);
                } while (dureeServ == 0);

                int heureArr = dockData.getHeureArr(addr);

                do {
                    heureDeb = rand.nextInt(1440 - dureeServ);
                } while (heureDeb < heureArr || !checkIfOverlapped(dnaMap.get(poste), heureDeb, dureeServ));

                Navire n = new Navire(addr, heureArr, heureDeb, dureeServ);
                //dnaMap.putIfAbsent(poste, new ArrayList<>());
                dnaMap.get(poste).add(n);
                elemPoolCopy.remove(addr);
                mapSize ++;
            }

            return dnaMap;
        }

        private String toHour(int time) {
            return time / 60 +  "h" + (time % 60 == 0? "" : time % 60);
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
            strBld.append("\n")
                    .append("Fitness : ").append(calcFitness())
                    .append("\n");
            return strBld.toString();
        }
    }

    public void generatePop () {
        for (int i = 0; i < popNumber; i++)
            popDNAs.add(new DNA());
    }

    public void printFit () {
        popDNAs.stream().map(DNA::calcFitness).sorted().forEach(System.out::println);
    }

    public void print() {
        popDNAs.forEach(System.out::println);
    }
}
