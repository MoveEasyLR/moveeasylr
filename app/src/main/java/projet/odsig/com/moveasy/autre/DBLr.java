package projet.odsig.com.moveasy.autre;
import java.io.File;

import jsqlite.Exception;

import android.content.Context;

public class DBLr {
	private jsqlite.Database bdd;
	
	public DBLr(Context context){
		//if (Utils.existFile(new File("/data/data/com.vlr.test/databases/dblr.sqlite")) == true){
		if (Utils.existFile(new File(Globals.wPathFiles + "databases/opendata_sqlite.sqlite")) == true){
            System.out.println(Globals.wPathFiles + "databases/opendata_sqlite.sqlite");
			bdd = new jsqlite.Database();

		} else {
			bdd = null;
		}
	}
 
	public void open(){
		try {
			//bdd.open("/data/data/com.vlr.test/databases/dblr.sqlite", jsqlite.Constants.SQLITE_OPEN_READWRITE);
			bdd.open(Globals.wPathFiles + "databases/opendata_sqlite.sqlite", jsqlite.Constants.SQLITE_OPEN_READWRITE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	public String getPath(){
		//return "/data/data/com.vlr.test/databases/dblr.sqlite";
		return Globals.wPathFiles + "databases/opendata_sqlite.sqlite";
	}
	
	public void close(){
		try {
			bdd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public jsqlite.Database getBDD(){
		return bdd;
	}
}