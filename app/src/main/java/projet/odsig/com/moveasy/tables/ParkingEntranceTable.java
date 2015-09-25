package projet.odsig.com.moveasy.tables;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

import jsqlite.Stmt;
import projet.odsig.com.moveasy.autre.DBLr;

public class ParkingEntranceTable extends DBLr
{
    final static String TABLE_NAME = "parc_stationnement_entre";

    final static String ID = "parking_id";
    final static String NAME = "parking_li";
    final static String SHORT_NAME = "parking_no";
    final static String NB_PLACES = "parking_nb";
    final static String TYPE = "parking_ty";
    final static String HOURLY = "parking_ho";
    final static String SURFACE_AREA = "parking_ha";
    final static String PARKING_VL = "parking_vl";
    final static String PARKING_VE = "parking_ve";
    final static String PARKING_PM = "parking_pm";
    final static String PARKING_VI = "parking_vi";
    final static String PARKING_MO = "parking_mo";
    final static String PARKING_FA = "parking_fa";
    final static String GEOMETRY = "Geometry";

    public ParkingEntranceTable(Context context)
    {
        super(context);
    }

    public ArrayList<ParkingEntrance> getParkingEntrance(Context c)
    {
        ArrayList<ParkingEntrance> parkingsEntrances = new ArrayList<ParkingEntrance>();
        try {
            DBLr db = new DBLr(c);
            db.open();
            String sql = "select "+ID+", "+NAME+", "+SHORT_NAME+", "+NB_PLACES+", "+TYPE+", "+HOURLY+", "+SURFACE_AREA+", "+PARKING_VL+", "+PARKING_VE+", "+PARKING_PM+", "+PARKING_VI+", "+PARKING_MO+", "+PARKING_FA+", "+"AsText("+GEOMETRY+")"+" from "+TABLE_NAME+";";
            Stmt stmt = db.getBDD().prepare(sql);
            while(stmt.step()) {
                parkingsEntrances.add(new ParkingEntrance(stmt.column_int(0), stmt.column_string(1), stmt.column_string(2), stmt.column_int(3), stmt.column_string(4), stmt.column_string(5), stmt.column_double(6),stmt.column_int(7), stmt.column_int(8),stmt.column_int(9),stmt.column_int(10),stmt.column_int(11), stmt.column_string(12), stmt.column_string(13)));
            }
            stmt.close();
            db.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }

        return parkingsEntrances;
    }

    // !! Problème ici lors de l'inscription des données
    public ArrayList<ParkingEntrance> getSimpleParkingEntrance(Context c)
    {
        ArrayList<ParkingEntrance> parkingsEntrances = new ArrayList<ParkingEntrance>();

        try {
            DBLr db = new DBLr(c);
            db.open();
            String sql = "select "+ID+", "+NAME+", "+SHORT_NAME+", "+NB_PLACES+", "+TYPE+", "+HOURLY+", "+SURFACE_AREA+", "+PARKING_FA+", "+"AsText("+GEOMETRY+")"+" from "+TABLE_NAME+";";
            Stmt stmt = db.getBDD().prepare(sql);
            while(stmt.step()) {
                parkingsEntrances.add(new ParkingEntrance(stmt.column_int(0), stmt.column_string(1), stmt.column_string(2), stmt.column_int(3), stmt.column_string(4), stmt.column_string(5), stmt.column_double(6), stmt.column_string(7), stmt.column_string(8)));
            }
            stmt.close();
            db.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        return parkingsEntrances;
    }

    // !! Problème ici lors de l'inscription des données
    public void updateFavori(Context c, String id, String favori)
    {
        try {
            DBLr db = new DBLr(c);
            db.open();
            String sql = "UPDATE "+TABLE_NAME+ " SET "+PARKING_FA+ "="+favori+" WHERE "+ID+"="+Integer.parseInt(id)+";";
            db.getBDD().exec(sql, null);
            db.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
            Log.e("titi", e.getMessage());
        }
    }



}
