package dock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DockData {
    private final int nbrNvr;
    private final int nbrPst;
    private final HashMap<String, Integer> nvrTimeMap;
    private final HashMap<String, HashMap<String, Integer>> tmpService;

    private DockData (int nbrNvr, int nbrPst, HashMap<String, Integer> nvrTimeMap, HashMap<String, HashMap<String ,Integer>> tmpService) {
        this.nbrNvr = nbrNvr;
        this.nbrPst = nbrPst;
        this.nvrTimeMap = nvrTimeMap;
        this.tmpService = tmpService;
    }

    public static class DockBuilder {
        private int nbrNvr;
        private int nbrPst;
        private HashMap<String, Integer> nvrTimeMap;
        private HashMap<String, HashMap<String, Integer>> tmpService;

        public DockBuilder setNbrNvr(int nbrNvr) {
            this.nbrNvr = nbrNvr;
            return this;
        }

        public DockBuilder setNbrPst(int nbrPst) {
            this.nbrPst = nbrPst;
            return this;
        }

        public DockBuilder setNvrTimeMap(HashMap<String, Integer> nvrTimeMap) {
            this.nvrTimeMap = nvrTimeMap;
            return this;
        }

        public DockBuilder setTmpService(HashMap<String, HashMap<String, Integer>> tmpService) {
            this.tmpService = tmpService;
            return this;
        }

        public DockData build () {
            return new DockData(nbrNvr, nbrPst, nvrTimeMap, tmpService);
        }
    }

    public int getHeureArr (String idNavire) {
        return nvrTimeMap.get(idNavire);
    }

    public int getDureeServ (String idNavire, String idPoste) {
        return tmpService.get(idPoste).get(idNavire);
    }

    public List<String> getNaviresList () {
        return new ArrayList<>(nvrTimeMap.keySet());
    }

    public List<String> getPostesList () {
        return new ArrayList<>(tmpService.keySet());
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append("Nombre de navires : ").append(nbrNvr).append("\n")
                .append("Nombre de postes d'amarrage : ").append(nbrPst).append("\n")
                .append("\nDate d'arrivee:\n");

        for (String nom : nvrTimeMap.keySet()) {
            strBld.append(nom).append(" -> ").append(nvrTimeMap.get(nom)).append("\n");
        }

        strBld.append("\nDuree de service:\n");
        for (String poste : tmpService.keySet()) {
            strBld.append(poste).append("\n");
            for(String nom : tmpService.get(poste).keySet()) {
                strBld.append(nom).append(" -> ").append(tmpService.get(poste).get(nom)).append("\n");
            }
            strBld.append("\n");
        }

        return strBld.toString().trim();
    }
}
