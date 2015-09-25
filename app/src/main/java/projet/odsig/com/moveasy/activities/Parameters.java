package projet.odsig.com.moveasy.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import projet.odsig.com.moveasy.R;

/**
 * Created by ThierryMercier on 07/03/2015.
 */
public class Parameters extends Activity {

    SharedPreferences sharedPref;
    ImageButton closeButton;
    NumberPicker npDatas;
    String[] values = new String[60];
    Switch switchDatas;
    Switch switchCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parameters);

        createInterface();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = sharedPref.edit();

        // Toast.makeText(Parameters.this, values[npDatas.getValue()], Toast.LENGTH_SHORT).show();

        editor.putInt("data_interval", Integer.parseInt(values[npDatas.getValue()]));
        editor.commit();
    }

    public void createInterface() {

        sharedPref= getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);

        // Création du switch pour les données temps réel
        switchDatas = (Switch)findViewById(R.id.dataSwitch);
        switchDatas.setChecked(sharedPref.getBoolean("data_on", true));
        switchDatas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            SharedPreferences.Editor editor = sharedPref.edit();

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("data_on", switchDatas.isChecked());
                editor.commit();
            }
        });


        // Création du number picker pour l'intervalle des données temps réel
        npDatas = (NumberPicker) findViewById(R.id.dataNP);

        values[0] = "120";
        for (int i = 1; i < values.length; i++) {
            values[i] = Integer.toString(Integer.parseInt(values[i - 1]) + 10);
        }
        npDatas.setMaxValue(values.length - 1);
        npDatas.setMinValue(0);
        npDatas.setDisplayedValues(values);

        // Cette ligne évite l'affichage du clavier mais entraîne le non affichage des caractères
        // au 1er lancement après validation
        npDatas.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        for (int i=0; i < values.length; i++) {
            if (values[i].equals(Integer.toString(sharedPref.getInt("data_interval", 30)))) {
                npDatas.setValue(i);
            }
        }

        // Création du switch pour la position de la voiture
        switchCar = (Switch)findViewById(R.id.carSwitch);
        switch (sharedPref.getString("car_visible", "1")) {
            case "0":
                switchCar.setChecked(false);
                break;
            case "1":
                switchCar.setChecked(true);
                break;
        }
        switchCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            SharedPreferences.Editor editor = sharedPref.edit();

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (switchCar.isChecked()) {
                    editor.putString("car_visible", "1");
                    editor.commit();
                } else {
                    editor.putString("car_visible", "0");
                    editor.commit();
                }

            }
        });


        // Création du bouton de fermeture
        closeButton = (ImageButton) findViewById(R.id.closeButtonParam);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentParam = new Intent(Parameters.this, MainActivity.class);
                startActivity(intentParam);
                finish();
            }
        });
    }
}
