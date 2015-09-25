package projet.odsig.com.moveasy.tables;

public class ParkingMeter
{
    private int id;
    private int numero;
    private int date;
    private String localisation;
    private String zone;
    private String alimentationType;
    private String pmType;
    private String geometry;

    public ParkingMeter(int id, int numero, int date, String localisation, String zone, String alimentationType, String pmType, String geometry) {
        this.id = id;
        this.numero = numero;
        this.date = date;
        this.localisation = localisation;
        this.zone = zone;
        this.alimentationType = alimentationType;
        this.pmType = pmType;
        this.geometry = geometry;
    }

    public String getPmType() {
        return pmType;
    }

    public String getZone() {
        return zone;
    }

    public String getAlimentationType() {
        return alimentationType;
    }

    public int getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public String getLocalisation() {
        return localisation;
    }

    public int getDate() {
        return date;
    }

    public String getGeometry() {
        return geometry;
    }
}
