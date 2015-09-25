package projet.odsig.com.moveasy.tables;
public class Defibrillator
{
    private int id;
    private String adress;
    private String structure;
    private String geometry;

    public Defibrillator(String structure, String adress, int id, String geometry) {
        this.structure = structure;
        this.adress = adress;
        this.id = id;
        this.geometry = geometry;
    }

    public int getId() {
        return id;
    }

    public String getAdress() {
        return adress;
    }

    public String getStructure() {
        return structure;
    }

    public String getGeometry() {
        return geometry;
    }
}
