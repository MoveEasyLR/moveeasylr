package projet.odsig.com.moveasy.tables;

import android.content.Context;
import java.util.ArrayList;
import jsqlite.*;
import projet.odsig.com.moveasy.autre.DBLr;

public class DefibrillatorTable extends DBLr
{
    final static String TABLE_NAME = "defibrillateur";
    final static String ID = "id";
    final static String ADRESS = "adresse";
    final static String STRUCTURE = "structure";
    final static String GEOMETRY = "Geometry";

    public DefibrillatorTable(Context context) {
        super(context);
    }

    //Defibrillators
    public ArrayList<Defibrillator> getDefibrillators(Context c)
    {
        ArrayList<Defibrillator> defibrillators = new ArrayList<Defibrillator>();
        try {
            DBLr db = new DBLr(c);
            db.open();
            Stmt stmt = db.getBDD().prepare("select "+ID+", "+ADRESS+", "+STRUCTURE+", "+"AsText("+GEOMETRY+")"+" from "+TABLE_NAME+";");
            while(stmt.step()) {
                defibrillators.add(new Defibrillator(stmt.column_string(2), stmt.column_string(1), stmt.column_int(0), stmt.column_string(3)));
            }
            stmt.close();
            db.close();
        } catch (jsqlite.Exception e) {
            e.printStackTrace();
        }
        return defibrillators;
    }
}
