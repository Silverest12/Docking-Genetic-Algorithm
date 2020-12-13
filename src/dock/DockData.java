package dock;

import java.util.HashMap;

public class DockData {
    private int nbrNvr;
    private int nbrPst;
    private HashMap<String, String> nvrTimeMap;
    private HashMap<String, HashMap<String, Integer>> tmpService;

    private DockData (int nbrNvr, int nbrPst, HashMap<String, String> nvrTimeMap, HashMap<String, HashMap<String ,Integer>> tmpService) {
        this.nbrNvr = nbrNvr;
        this.nbrPst = nbrPst;
        this.nvrTimeMap = nvrTimeMap;
        this.tmpService = tmpService;
    }

    public static class DockBuilder {
        private int nbrNvr;
        private int nbrPst;
        private HashMap<String, String> nvrTimeMap;
        private HashMap<String, HashMap<String, Integer>> tmpService;

        public DockBuilder setNbrNvr(int nbrNvr) {
            this.nbrNvr = nbrNvr;
            return this;
        }

        public DockBuilder setNbrPst(int nbrPst) {
            this.nbrPst = nbrPst;
            return this;
        }

        public DockBuilder setNvrTimeMap(HashMap<String, String> nvrTimeMap) {
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

    public void setNbrNvr(int nbrNvr) {
        this.nbrNvr = nbrNvr;
    }

    public void setPostes(int nbrPst) {
        this.nbrPst = nbrPst;
    }

    public void setNvrTimeMap(HashMap<String, String> nvrTimeMap) {
        this.nvrTimeMap = nvrTimeMap;
    }

    public void setTmpService(HashMap<String, HashMap<String, Integer>> tmpService) {
        this.tmpService = tmpService;
    }

    public int getNbrNvr() {
        return nbrNvr;
    }

    public int getPostes() {
        return nbrPst;
    }

    public HashMap<String, String> getNvrTimeMap() {
        return nvrTimeMap;
    }

    public HashMap<String, HashMap<String, Integer>> getTmpService() {
        return tmpService;
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
