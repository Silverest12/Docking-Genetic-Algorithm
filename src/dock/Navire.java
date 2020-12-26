package dock;

public class Navire {
    private final String idNavire;
    private final int heureArr;
    private int heureDeb;
    private int dureeServ;

    public Navire  () {
        idNavire = "0";
        heureArr = 0;
        heureDeb = 0;
        dureeServ = 0;
    }

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

    public void setHeureDeb(int heureDeb) {
        this.heureDeb = heureDeb;
    }

    public void setDureeServ(int dureeServ) {
        this.dureeServ = dureeServ;
    }

    @Override
    public String toString() {
        return idNavire + " ";
    }
}
