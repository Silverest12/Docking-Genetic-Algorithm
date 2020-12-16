package dock;

public class Navire {
    private final String idNavire;
    private final int heureArr;
    private final int heureDeb;
    private final int dureeServ;
    //private int finServ;

    public Navire (String id, int heureArr, int heureDeb, int dureeServ) {
       this.idNavire = id;
       this.heureArr = heureArr;
       this.heureDeb = heureDeb;
       this.dureeServ = dureeServ;
    }

    public String getIdNavire() {
        return idNavire;
    }

    public int getHeureArr() {
        return heureArr;
    }

    public int getHeureDeb() {
        return heureDeb;
    }

    public int getDureeServ() {
        return dureeServ;
    }
}
