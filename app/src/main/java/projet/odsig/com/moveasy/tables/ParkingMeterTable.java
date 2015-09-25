package projet.odsig.com.moveasy.tables;

import android.content.Context;

import java.util.ArrayList;

import jsqlite.Stmt;
import projet.odsig.com.moveasy.autre.DBLr;

public class ParkingMeterTable extends DBLr
{
    final static String TABLE_NAME = "horodateur";
    final static String ID = "hor_id";
    final static String NUMERO = "hor_num";
    final static String DATE = "hor_date_m";
    final static String LOCALISATION = "hor_voie_l";
    final static String PRICE_ZONE = "hor_libell";
    final static String ALIMENTATION_TYPE = "hor_alimen";
    final static String PM_TYPE = "hor_type";
    final static String GEOMETRY = "Geometry";

    public ParkingMeterTable(Context c)
    {super(c);}

    public ArrayList<ParkingMeter> getParkingMeters(Context c)
    {
        ArrayList<ParkingMeter> parkingmeters = new ArrayList<ParkingMeter>();
        try {
            DBLr db = new DBLr(c);
            db.open();
            Stmt stmt = db.getBDD().prepare("select "+ID+", "+NUMERO+", "+DATE+", "+LOCALISATION+", "+PRICE_ZONE+", "+ALIMENTATION_TYPE+", "+PM_TYPE+", "+" AsText"+"("+GEOMETRY+")"+" FROM "+TABLE_NAME +";");
            while(stmt.step()) {
                parkingmeters.add(new ParkingMeter(stmt.column_int(0), stmt.column_int(1), stmt.column_int(2), stmt.column_string(3), stmt.column_string(4), stmt.column_string(5), stmt.column_string(6), stmt.column_string(7)));
            }
            stmt.close();
            db.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        return parkingmeters;
    }
}
