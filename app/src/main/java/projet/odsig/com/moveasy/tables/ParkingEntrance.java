package projet.odsig.com.moveasy.tables;

public class ParkingEntrance
{
    private int id;
    private String name;
    private String shortName;
    private int nbPlaces;
    //parking relais|souterrain|enclos|gratuit|abonnés
    private String type;
    //Horaires
    private String hourly;
    //superficie ?
    private Double surface_area;
    //?
    private int parking_vl;
    //?
    private int parking_ve;
    //Nb place handicapés ?
    private int parking_pm;
    //?
    private int parking_vi;
    //?
    private int parking_mo;
    //?
    private String parking_fa;

    private String geometry;

    private String dpPlaceDisponible;

    private String dpDate;

    private String tauxDisponibilite;

    public ParkingEntrance(int id, String name, String shortName, int nbPlaces, String type, String hourly, Double surface_area, int parking_vl, int parking_ve, int parking_pm, int parking_vi, int parking_mo, String parking_fa, String geometry) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.nbPlaces = nbPlaces;
        this.type = type;
        this.hourly = hourly;
        this.surface_area = surface_area;
        this.parking_vl = parking_vl;
        this.parking_ve = parking_ve;
        this.parking_pm = parking_pm;
        this.parking_vi = parking_vi;
        this.parking_mo = parking_mo;
        this.parking_fa = parking_fa;
        this.geometry = geometry;
        dpPlaceDisponible = "NA";
        dpDate = "NA";
        tauxDisponibilite = "NA";
    }

    public ParkingEntrance(int id, String name, String shortName, int nbPlaces, String type, String hourly, Double surface_area, String parking_fa, String geometry) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.nbPlaces = nbPlaces;
        this.type = type;
        this.hourly = hourly;
        this.surface_area = surface_area;
        this.geometry = geometry;
        dpPlaceDisponible = "NA";
        dpDate = "NA";
        tauxDisponibilite = "NA";
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public String getType() {
        return type;
    }

    public String getHourly() {
        return hourly;
    }

    public Double getSurface_area() {
        return surface_area;
    }

    public int getParking_vl() {
        return parking_vl;
    }

    public int getParking_ve() {
        return parking_ve;
    }

    public int getParking_pm() {
        return parking_pm;
    }

    public int getParking_vi() {
        return parking_vi;
    }

    public int getParking_mo() {
        return parking_mo;
    }

    public String getParking_fa() {
        return parking_fa;
    }

    public void setParking_fa(String parking_fa) {
        this.parking_fa = parking_fa;
    }

    public String getGeometry() {
        return geometry;
    }

    public String getDpPlaceDisponible() {
        return dpPlaceDisponible;
    }

    public void setDpPlaceDisponible(String dpPlaceDisponible) {
        this.dpPlaceDisponible = dpPlaceDisponible;
    }

    public String getDpDate() {
        return dpDate;
    }

    public void setDpDate(String dpDate) {
        this.dpDate = dpDate;
    }

    public String getTauxDisponibilite() {
        return tauxDisponibilite;
    }

    public void setTauxDisponibilite(String tauxDisponibilite) {
        this.tauxDisponibilite = tauxDisponibilite;
    }
}
