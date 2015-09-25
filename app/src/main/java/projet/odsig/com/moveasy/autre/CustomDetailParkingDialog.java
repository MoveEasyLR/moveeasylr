package projet.odsig.com.moveasy.autre;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import projet.odsig.com.moveasy.R;
import projet.odsig.com.moveasy.activities.Help;
import projet.odsig.com.moveasy.activities.MainActivity;
import projet.odsig.com.moveasy.tables.ParkingEntrance;
import projet.odsig.com.moveasy.tables.ParkingEntranceTable;

public class CustomDetailParkingDialog extends Dialog
{
    //Buttons
    public Button driveMe;
    public ImageButton favoriSelectButton;

    //text fields
    public TextView name;
    public TextView placesAvailables;
    public TextView placesTotal;
    public TextView hourly;

    //data from the mainActivity
    Bundle data;
    MainActivity.JavaScriptInterface jsi;

    public CustomDetailParkingDialog(Context context, Bundle params) {
        super(context);
        this.data = params;
    }

    public CustomDetailParkingDialog(Context context, Bundle params, MainActivity.JavaScriptInterface jsi) {
        super(context);
        this.data = params;
        this.jsi = jsi;
    }

    public void init(Bundle data)
    {
        if(!data.getString("name").isEmpty())
        {
            name.setText(data.getString("name"));
        }

        if(!data.getString("placesAvailables").isEmpty())
            placesAvailables.setText(data.getString("placesAvailables"));
        if(!data.getString("hourly").isEmpty())
            hourly.setText(data.getString("hourly"));
        if(!data.getString("placeTotal").isEmpty())
            placesTotal.setText(data.getString("placeTotal"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_detail_parking_layout);

        ArrayList<ParkingEntrance> listParkingEntrance;
        ParkingEntranceTable parkingEntranceTable = new ParkingEntranceTable(getContext());
        listParkingEntrance = parkingEntranceTable.getParkingEntrance(getContext());

        name = (TextView) findViewById(R.id.name);
        placesAvailables = (TextView) findViewById(R.id.placesNumber);
        hourly = (TextView) findViewById(R.id.hourly);
        placesTotal = (TextView) findViewById(R.id.placesNumberTotal);
        driveMe = (Button) findViewById(R.id.driveMeB);
        favoriSelectButton = (ImageButton) findViewById(R.id.favoriSelectButton);

        for (ParkingEntrance pE : listParkingEntrance) {
            if (data.getInt("id")==pE.getId()) {
                if (pE.getParking_fa().equals("0")) {
                    favoriSelectButton.setSelected(false);

                } else {
                    favoriSelectButton.setSelected(true);
                }
            }
        }
        favoriSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ArrayList<ParkingEntrance> listParkingEntrance;
            ParkingEntranceTable parkingEntranceTable = new ParkingEntranceTable(getContext());
            listParkingEntrance = parkingEntranceTable.getParkingEntrance(getContext());

                for (ParkingEntrance pE : listParkingEntrance) {
                    if (data.getInt("id")==pE.getId()) {
                        if(pE.getParking_fa().equals("1")){
                            parkingEntranceTable.updateFavori(getContext(), Integer.toString(pE.getId()), "0");
                            favoriSelectButton.setSelected(false);
                            jsi.loadVecLayerParkings();
                        } else {
                            parkingEntranceTable.updateFavori(getContext(), Integer.toString(pE.getId()), "1");
                            favoriSelectButton.setSelected(true);
                            jsi.loadVecLayerParkings();
                        }
                    }
                }
            }
        });

        driveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsi.setupNavigation(data.getString("geometry"));
            }
        });
        init(data);
    }

}
