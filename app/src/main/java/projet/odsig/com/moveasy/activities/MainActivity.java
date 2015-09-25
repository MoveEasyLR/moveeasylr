package projet.odsig.com.moveasy.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import projet.odsig.com.moveasy.autre.CustomDetailParkingDialog;
import projet.odsig.com.moveasy.autre.Globals;
import projet.odsig.com.moveasy.autre.JSONRequest;
import projet.odsig.com.moveasy.R;
import projet.odsig.com.moveasy.autre.Utils;
import projet.odsig.com.moveasy.tables.ParkingEntrance;
import projet.odsig.com.moveasy.tables.ParkingEntranceTable;
import projet.odsig.com.moveasy.tables.ParkingMeter;
import projet.odsig.com.moveasy.tables.ParkingMeterTable;

public class MainActivity extends Activity {

    //Déclaration des éléments de l'interface
    ImageView chevronGauche;
    ImageView chevronDroite;
    public ExpandableListView drawerListLeft;
    public FrameLayout drawerListRight;
    public static DrawerLayout drawerLayout;
    private String[] layerItems;
    private String[] mapItems;
    android.widget.ExpandableListAdapter listAdapterR;
    android.widget.ExpandableListAdapter listAdapterL;
    List<String> listDataHeaderL;
    HashMap<String, List<String>> listDataChildL;
    public TextView titre;
    public WebView webView;
    ImageButton btnPutCar;
    ImageButton btnShowLocation;
    ImageButton btnParkings;
    ImageButton btnFavoris;
    ImageButton btnCenterMap;

    // Progress Dialog
    private ProgressDialog Dialog;

    // Parameters Dialog
    private Dialog parametersDialog;

    // TimerTask
    Timer timer;
    TimerTask timerTask;
    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    // Retrievedata sert à récupérer les données de disponibilité temps réel
    Retrievedata retrievedata;

    // Déclaration du GPSTracker et des coordonnées nécessaires voiture/GPS
    GPSTracker gps;
    public String latitudeGPS;
    public String longitudeGPS;
    public String carLatGPS;
    public String carLonGPS;

    // Déclaration de la JSI
    JavaScriptInterface jsi;

    // Coordonnées de La Rochelle
    String lonLR = "-1.1532";
    String latLR = "46.1558";
    String zoomOSM = "13";
    String zoomOrtho = "8";

    // Déclaration des éléments servant aux requêtes et à leur manipulation
    String jsonTempsReelStr = "";
    String requeteTempsReel;
    String requeteParkings;
    JSONRequest jsonRequest = new JSONRequest();
    JSONArray jsonArray;

    // Etat de l'affichage
    public String mapType;
    public String activeLayer;
    public String gpsVisible;
    public String carVisible;
    public String centerMap;

    // Paramètres
    public boolean dataOn;
    public int dataInterval;

    final Context currentContext = this;

    // Pour la sauvegarde des paramètres
    public SharedPreferences sharedPref;

    // Tableau servant à la préparation de l'envoi des données au main.js
    ArrayList<ParkingMeter> listParkingMeter = new ArrayList<ParkingMeter>();
    ArrayList<ParkingEntrance> listParkingEntrance = new ArrayList<ParkingEntrance>();

    // Permet l'accès au Javascript
    @SuppressLint("SetJavaScriptEnabled")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération des données sauvegardées
        sharedPref= getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE);
        activeLayer = sharedPref.getString("saved_layer", "5");

        mapType = sharedPref.getString("saved_map_type", "0");
        carVisible = sharedPref.getString("car_visible", "0");
        carLatGPS = sharedPref.getString("saved_car_lat", latLR);
        carLonGPS = sharedPref.getString("saved_car_lon", lonLR);
        gpsVisible = sharedPref.getString("saved_gps_status", "0");
        latitudeGPS = sharedPref.getString("saved_gps_lat", latLR);
        longitudeGPS = sharedPref.getString("saved_gps_lon", lonLR);
        dataOn = sharedPref.getBoolean("data_on", true);
        dataInterval = sharedPref.getInt("data_interval", 90);
        centerMap = sharedPref.getString("center_map", "0");

        titre = (TextView) findViewById(R.id.titre);
        Globals.wPathFiles = currentContext.getFilesDir().getPath() + "/";
        if (Utils.existFile(new File(Globals.wPathFiles + "databases/opendata_sqlite.sqlite")) == false){
            Toast.makeText(getApplicationContext(), "Initialisation ...",Toast.LENGTH_SHORT).show();
            AssetManager assetManager = this.getAssets();
            Utils.copyFileOrDirDB("opendata_sqlite", assetManager);
        }

        // Définition des différentes requêtes utilisées
        defineRequests();

        // Création de la WebView et création de ls JSI
        webView =(WebView) findViewById(R.id.map);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        jsi = new JavaScriptInterface(this);
        webView.addJavascriptInterface(jsi, "jsi");

        // Récupération du type de carte choisi et de la couche active
        webView.loadUrl("javascript:mapType="+mapType+";");
        webView.loadUrl("javascript:activeLayer="+activeLayer+";");

        // Ouverture et initialisation de la map dans la WebView
        webView.loadUrl("file:///android_asset/map.html");

        // Création des boutons de raccourcis
        createButtons();
        // Préparation de la liste des éléments du Menu Principal
        prepareMenus();
        // Création du Drawer Layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        createDrawers();


        // Affichage de la position GPS et de la position de la voiture
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {

                // Affichage de la couche en cours
                if ((activeLayer.equals("0")) || (activeLayer.equals("2")) || (activeLayer.equals("6"))) {
                    if (dataOn) {
                        //Toast.makeText(MainActivity.this, Integer.toString(dataInterval),Toast.LENGTH_SHORT).show();
                        startTimer();
                    }
                }
                if ((activeLayer.equals("1")) || (activeLayer.equals("3"))) {
                    stopTimerTask();
                    jsi.loadVecLayerParkings();
                }
                if (activeLayer.equals("4")) {
                    stopTimerTask();
                    jsi.loadVecLayerHoro();
                }
                //Toast.makeText(MainActivity.this, "GPS "+gpsVisible, Toast.LENGTH_SHORT).show();

                if (gpsVisible.equals("1")) {
                    jsi.displayGPSPositionJava();
                }
                //Toast.makeText(MainActivity.this, "CAR "+carVisible, Toast.LENGTH_SHORT).show();

                if (carVisible.equals("1")) {
                    jsi.displayCarPositionJava();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTimerTask();

        gps = null;
        if (retrievedata!=null) retrievedata.cancel(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopTimerTask();

        gps = null;
        if (retrievedata!=null) retrievedata.cancel(true);
    }

    // Sauvegarde des données de positionnement si nouvel affichage de l'activité (changement d'orientation)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Classe qui permet d'effectuer les requêtes an AsyncTask
    public class Retrievedata extends AsyncTask<String,String,String> {

        String collectedDatas;

        @Override
        protected String doInBackground(String... arg0) {
            JSONObject jsonObj = null;
            collectedDatas = arg0[1];

            // Requête sur le Web Service
            try {
                jsonObj = jsonRequest.makeHttpRequest(arg0[0]);
            } catch (Exception e){
                Toast.makeText(MainActivity.this, getString(R.string.pb_reception), Toast.LENGTH_SHORT).show();
            }

            // Conversion en objet String (pour réutilisabilité en fonction du format en entrée, on retourne une String)
            String jsonStr = jsonObj.toString();

            return jsonStr;
        }
        @Override
        protected void onPreExecute() {
            // Progress Dialog
            Dialog = new ProgressDialog(MainActivity.this);
            Dialog.setMessage(getString(R.string.reception));
            Dialog.show();
        }
        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
        @Override
        protected void onPostExecute(String jsonString)
        {
            Dialog.dismiss();

            // Appel de la bonne méthode de la JSI en fonction du type de données récupérées
            switch(collectedDatas)
            {
                case "tempsreel":
                    jsonTempsReelStr = jsonString;
                    break;
                default:
            }
            jsi.loadVecLayerParkings();
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    // Préparation de la liste des éléments et sous-éléments du Menu Principal (à gauche)
    private void prepareMenus() {

        //Préparation Left Drawer
        String[] menu_principal_array = getResources().getStringArray(R.array.menu_principal_array);
        String[] legend_array= getResources().getStringArray(R.array.legend_array);

        listDataHeaderL = new ArrayList<String>();
        for(int i = 0; i<menu_principal_array.length; i++) {
            listDataHeaderL.add(menu_principal_array[i]);
        }

        List<String> legende = new ArrayList<String>();
        for(int i = 0; i<legend_array.length; i++) {
            legende.add(legend_array[i]);
        }
        List<String> parametres = new ArrayList<String>();
        List<String> aide = new ArrayList<String>();
        List<String> quitter = new ArrayList<String>();

        listDataChildL = new HashMap<String, List<String>>();
        listDataChildL.put(listDataHeaderL.get(0), legende);
        listDataChildL.put(listDataHeaderL.get(1), parametres);
        listDataChildL.put(listDataHeaderL.get(2), aide);
        listDataChildL.put(listDataHeaderL.get(3), quitter);
    }


    // Création des deux menus en tant que drawers et listeners associés
    public void createDrawers() {

        layerItems = getResources().getStringArray(R.array.layers_array);
        mapItems = getResources().getStringArray(R.array.maps_array);

        drawerListRight = (FrameLayout) findViewById(R.id.right_drawer);
        ListView drawerListRightLayers = (ListView)findViewById(R.id.right_drawer_layers);
        drawerListRightLayers.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, layerItems));
        ListView drawerListRightMaps = (ListView)findViewById(R.id.right_drawer_maps);
        drawerListRightMaps.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item, mapItems));

        // Surlignage du fond de carte actif
        switch(mapType) {
            case "0":drawerListRightMaps.setItemChecked(Integer.parseInt(mapType), true);
                break;
            case "1":drawerListRightMaps.setItemChecked(Integer.parseInt(mapType), true);
        }

        // Surlignage de la couche active
        drawerListRightLayers.setItemChecked(Integer.parseInt(activeLayer), true);

        // Listener pour le Menu de Sélection des couches
        drawerListRightLayers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position)
                {
                    case 0:
                        titre.setText(getString(R.string.app_name)+" > "+layerItems[position]);
                        activeLayer = Integer.toString(position);
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        if (dataOn) {
                            startTimer();
                        } else {
                            jsi.loadVecLayerParkings();
                            //jsi.loadVecLayerParkings();
                        }
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 1:
                        titre.setText(getString(R.string.app_name)+" > "+layerItems[position]);
                        activeLayer = Integer.toString(position);
                        stopTimerTask();
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        jsi.loadVecLayerParkings();
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 2:
                        titre.setText(getString(R.string.app_name)+" > "+layerItems[position]);
                        activeLayer = Integer.toString(position);
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        if (dataOn) {
                            startTimer();
                        } else {
                            jsi.loadVecLayerParkings();
                            jsi.loadVecLayerParkings();
                        }
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 3:
                        titre.setText(getString(R.string.app_name)+" > "+layerItems[position]);
                        activeLayer = Integer.toString(position);
                        stopTimerTask();
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        jsi.loadVecLayerParkings();
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 4:
                        titre.setText(getString(R.string.app_name)+" > "+layerItems[position]);
                        activeLayer = Integer.toString(position);
                        stopTimerTask();
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        jsi.loadVecLayerHoro();
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 5:
                        titre.setText(getString(R.string.app_name));
                        activeLayer = Integer.toString(position);
                        stopTimerTask();
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("saved_layer", activeLayer);
                        editor.commit();
                        webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                        webView.loadUrl("javascript:removeAll();");
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    default:
                }
            }
        });

        // Listener pour le Menu de Sélection des fonds de carte
        drawerListRightMaps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SharedPreferences.Editor editor = sharedPref.edit();

                switch(position)
                {
                    case 0:
                        mapType = Integer.toString(position);
                        editor.putString("saved_map_type", mapType);
                        editor.commit();
                        jsi.initOSM();
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    case 1:
                        mapType = Integer.toString(position);
                        editor.putString("saved_map_type", mapType);
                        editor.commit();
                        jsi.initOrtho();
                        drawerLayout.closeDrawer(drawerListRight);
                        break;
                    default:
                }
            }
        });

        // Création du Menu Principal et Listener associé
        drawerListLeft = (ExpandableListView) findViewById(R.id.left_drawer);

        listAdapterL = new ExpandableListAdapter(this, listDataHeaderL, listDataChildL);
        drawerListLeft.setAdapter(listAdapterL);

        drawerListLeft.setOnGroupClickListener( new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                switch (groupPosition) {
                    case 1:
                        Intent intentParam = new Intent(MainActivity.this, Parameters.class);
                        stopTimerTask();
                        if (retrievedata!=null) retrievedata.cancel(true);
                        startActivity(intentParam);
                        finish();
                        break;
                    case 2:
                        Intent intentHelp = new Intent(MainActivity.this, Help.class);
                        stopTimerTask();
                        if (retrievedata!=null) retrievedata.cancel(true);
                        startActivity(intentHelp);
                        finish();
                        break;
                    case 3:
                        stopTimerTask();
                        finish();
                        break;
                }
                return false;
            }
        });
        drawerListLeft.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                return false;
            }
        });

        // Assignation de listeners aux chevrons pour ouvrir les menus en plus du swipe
        chevronDroite = (ImageView)findViewById(R.id.chevronDroite);
        chevronGauche = (ImageView)findViewById(R.id.chevronGauche);

        chevronDroite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerListRight);
            }
        });

        chevronGauche.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerListLeft);
            }
        });
    }


    // Création des boutons de raccourcis
    public void createButtons() {

        // Bouton de positionnement Voiture et dialogues associés
        btnPutCar = (ImageButton)findViewById(R.id.btnPutCar);
        btnPutCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create class object
                gps = new GPSTracker(MainActivity.this);

                final android.app.Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_car);

                final Button dialogButtonValider = (Button) dialog.findViewById(R.id.dialogButtonValider);

                dialogButtonValider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        SharedPreferences.Editor editor = sharedPref.edit();

                        // check if GPS enabled
                        if(gps.canGetLocation()){
                            carLatGPS = Double.toString(gps.getLatitude());
                            carLonGPS = Double.toString(gps.getLongitude());
                            carVisible = "1";
                            webView.loadUrl("javascript:carPositionVisible="+carVisible+";");
                            editor.putString("saved_car_lat", carLatGPS);
                            editor.putString("saved_car_lon", carLonGPS);
                            editor.putString("car_visible", carVisible);
                            editor.commit();
                            gps.stopUsingGPS();
                            jsi.displayCarPositionJava();
                            jsi.displayCarPositionJava();
                        } else {
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }
                        dialog.dismiss();
                    }
                });

                Button dialogButtonAnnuler = (Button) dialog.findViewById(R.id.dialogButtonAnnuler);
                // if button is clicked, close the custom dialog
                dialogButtonAnnuler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        // Bouton de positionnement GPS et dialogue associé
        btnShowLocation = (ImageButton) findViewById(R.id.btnShowLocation);
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // create class object
                gps = new GPSTracker(MainActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();

                // check if GPS enabled
                if(gps.canGetLocation()){

                    if (gpsVisible == "1"){
                        webView.loadUrl("javascript:removeGPS()");
                        gpsVisible = "0";
                        editor.putString("saved_gps_status", gpsVisible);
                        editor.commit();
                        webView.loadUrl("javascript:gpsPositionVisible="+gpsVisible+";");
                    } else {
                        latitudeGPS = Double.toString(gps.getLatitude());
                        longitudeGPS = Double.toString(gps.getLongitude());
                        gpsVisible = "1";
                        webView.loadUrl("javascript:gpsPositionVisible="+gpsVisible+";");
                        editor.putString("saved_gps_lat", latitudeGPS);
                        editor.putString("saved_gps_lon", longitudeGPS);
                        editor.putString("saved_gps_status", gpsVisible);
                        editor.commit();
                        jsi.displayGPSPositionJava();
                        gps.stopUsingGPS();
                    }
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    if (gpsVisible == "0") {
                        gps.showSettingsAlert();
                    } else {
                        webView.loadUrl("javascript:removeGPS()");
                        gpsVisible = "0";
                        editor.putString("saved_gps_status", gpsVisible);
                        editor.commit();
                        webView.loadUrl("javascript:gpsPositionVisible="+gpsVisible+";");
                    }
                }
            }
        });

        // Bouton d'affichage des favoris
        btnFavoris = (ImageButton)findViewById(R.id.btnFavoris);
        btnFavoris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeLayer.equals("6")) {
                    titre.setText(getString(R.string.app_name));
                    activeLayer = "5";
                    ListView drawerListRightLayers = (ListView)findViewById(R.id.right_drawer_layers);
                    drawerListRightLayers.setItemChecked(Integer.parseInt(activeLayer), true);
                    stopTimerTask();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("saved_layer", activeLayer);
                    editor.commit();
                    webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                    webView.loadUrl("javascript:removeAll();");
                } else {
                    titre.setText(getString(R.string.app_name)+" > "+"Favoris");
                    activeLayer = "6";
                    ListView drawerListRightLayers = (ListView)findViewById(R.id.right_drawer_layers);
                    drawerListRightLayers.setItemChecked(Integer.parseInt(activeLayer), true);
                    webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                    if (dataOn) {
                        startTimer();
                    } else {
                        jsi.loadVecLayerParkings();
                    }
                }
            }
        });

        btnFavoris.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final android.app.Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_favoris);

                final Button dialogButtonValider = (Button) dialog.findViewById(R.id.dialogButtonValider);

                dialogButtonValider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<ParkingEntrance> listParkingEntrance;
                        ParkingEntranceTable parkingEntranceTable = new ParkingEntranceTable(MainActivity.this);
                        listParkingEntrance = parkingEntranceTable.getParkingEntrance(MainActivity.this);

                        for (ParkingEntrance pE : listParkingEntrance) {
                            parkingEntranceTable.updateFavori(MainActivity.this, Integer.toString(pE.getId()), "0");
                            Log.e("FAVORIS", Integer.toString(pE.getId())+ pE.getParking_fa());
                        }
                        jsi.loadVecLayerParkings();
                        dialog.dismiss();
                    }
                });

                Button dialogButtonAnnuler = (Button) dialog.findViewById(R.id.dialogButtonAnnuler);
                // if button is clicked, close the custom dialog
                dialogButtonAnnuler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                return false;
            }
        });

        // Bouton de centrage de la carte
        btnParkings = (ImageButton)findViewById(R.id.btnParkings);
        btnParkings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layerItems = getResources().getStringArray(R.array.layers_array);

                if (activeLayer.equals("0")) {
                    titre.setText(getString(R.string.app_name));
                    activeLayer = "5";
                    ListView drawerListRightLayers = (ListView)findViewById(R.id.right_drawer_layers);
                    drawerListRightLayers.setItemChecked(Integer.parseInt(activeLayer), true);
                    stopTimerTask();
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("saved_layer", activeLayer);
                    editor.commit();
                    webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                    webView.loadUrl("javascript:removeAll();");
                } else {
                    activeLayer = "0";
                    titre.setText(getString(R.string.app_name)+" > "+layerItems[Integer.parseInt(activeLayer)]);
                    ListView drawerListRightLayers = (ListView)findViewById(R.id.right_drawer_layers);
                    drawerListRightLayers.setItemChecked(Integer.parseInt(activeLayer), true);
                    webView.loadUrl("javascript:activeLayer="+activeLayer+";");
                    if (dataOn) {
                        startTimer();
                    } else {
                        jsi.loadVecLayerParkings();
                    }
                }
            }
        });

        // Bouton de centrage de la carte
        btnCenterMap = (ImageButton)findViewById(R.id.btnCenter);
        btnCenterMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (centerMap.equals("0")) {
                if (mapType.equals("0")){
                    webView.loadUrl("javascript:centerMap("+lonLR+","+latLR+","+zoomOSM+")");
                }
                if (mapType.equals("1")){
                    webView.loadUrl("javascript:centerMap("+lonLR+","+latLR+","+zoomOrtho+")");
                }
            }
            if (centerMap.equals("1")) {
                if (mapType.equals("0")){
                    webView.loadUrl("javascript:centerMap("+carLonGPS+","+carLatGPS+",''"+")");
                }
                if (mapType.equals("1")){
                    webView.loadUrl("javascript:centerMap("+carLonGPS+","+carLatGPS+",''"+")");
                }
            }
            if (centerMap.equals("2")) {
                if (mapType.equals("0")){
                    webView.loadUrl("javascript:centerMap("+longitudeGPS+","+latitudeGPS+",''"+")");
                }
                if (mapType.equals("1")){
                    webView.loadUrl("javascript:centerMap("+longitudeGPS+","+latitudeGPS+",''"+")");
                }
            }
            }
        });

        btnCenterMap.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final android.app.Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_choice_center);
                dialog.show();

                Button dialogButtonCenterMap = (Button) dialog.findViewById(R.id.dialogButtonCenterMap);
                dialogButtonCenterMap.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        centerMap = "0";
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("center_map", centerMap);
                        editor.commit();
                        webView.loadUrl("javascript:centerMap("+lonLR+","+latLR+",10)");
                        dialog.dismiss();
                    }
                });

                Button dialogButtonCenterCar = (Button) dialog.findViewById(R.id.dialogButtonCenterCar);
                dialogButtonCenterCar.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        centerMap = "1";
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("center_map", centerMap);
                        editor.commit();
                        webView.loadUrl("javascript:centerMap("+carLonGPS+","+ carLatGPS+")");
                        dialog.dismiss();
                    }
                });

                Button dialogButtonCenterSelf = (Button) dialog.findViewById(R.id.dialogButtonCenterSelf);
                dialogButtonCenterSelf.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        centerMap = "2";
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("center_map", centerMap);
                        editor.commit();
                        webView.loadUrl("javascript:centerMap("+longitudeGPS+","+ latitudeGPS+")");
                        dialog.dismiss();
                    }
                });
                return false;
            }
        });
    }

    // Définition des différentes requêtes sur le Web Service
    public void defineRequests() {
        requeteParkings = "http://www.opendata.larochelle.fr/webservice/?service=getData&key=CL3SXJxGrCv525oK&db=stationnement&table=sta_parking&format=json";
        requeteTempsReel = "http://www.opendata.larochelle.fr/webservice/?service=getData&key=CL3SXJxGrCv525oK&db=stationnement&table=disponibilite_parking&format=json";
    }


    // JavaScriptInterface utilisée pour communiquer avec le main.js
    public class JavaScriptInterface {
        Context context;

        public JavaScriptInterface(Context context) {
            this.context = context;
        }

        // Méthode servant à la 1ère ouverture de la map
        @JavascriptInterface
        public void init0() {
            runOnUiThread(new Runnable() {
                public void run() {
                    webView.loadUrl("javascript:mapType="+mapType+";");
                    webView.loadUrl("javascript:initMap()");
                }
            });
        }

        // Méthode utilisée pour appeler l'affichage de la carte OSM
        @JavascriptInterface
        public void initOSM() {
            runOnUiThread(new Runnable() {
                public void run() {
                    mapType = "0";
                    webView.loadUrl("javascript:mapType="+mapType+";");
                    webView.loadUrl("javascript:mapChoice()");
                }
            });
        }

        // Méthode utilisée pour appeler l'affichage de la carte Ortho
        @JavascriptInterface
        public void initOrtho() {
            runOnUiThread(new Runnable() {
                public void run() {
                    mapType = "1";
                    webView.loadUrl("javascript:mapType="+mapType+";");
                    webView.loadUrl("javascript:mapChoice()");
                }
            });
        }

        @JavascriptInterface
        public void loadVecLayerHoro() {

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("saved_layer", activeLayer);
            editor.commit();

            runOnUiThread(new Runnable() {
                public void run() {
                    ParkingMeterTable parkingMeterTable = new ParkingMeterTable(currentContext);
                    listParkingMeter = parkingMeterTable.getParkingMeters(currentContext);
                    webView.loadUrl("javascript:removeAll();");
                    for(ParkingMeter pM : listParkingMeter) {
                        webView.loadUrl("javascript:displayHoro(\"" + Integer.toString(pM.getId()) + "\",\"" + pM.getZone() + "\",\"" + pM.getGeometry() + "\");");
                    }

                    webView.loadUrl("javascript:horodateurLayer.addFeatures(featuresSqlHoro);");
                }
            });
        }

        @JavascriptInterface
        public void loadVecLayerParkings() {

            JSONObject jsonObject;
            JSONObject openData;
            JSONObject answer;

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("saved_layer", activeLayer);
            editor.commit();

            if (!jsonTempsReelStr.equals("")) {
                try {
                    jsonObject = new JSONObject(jsonTempsReelStr);
                    openData = jsonObject.getJSONObject("opendata");
                    answer = openData.getJSONObject("answer");
                    jsonArray = answer.getJSONArray("data");
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Problème de récupération du tableau JSON", Toast.LENGTH_LONG).show();
                }

                runOnUiThread(new Runnable() {
                    public void run() {

                        ParkingEntranceTable parkingEntranceTable = new ParkingEntranceTable(currentContext);
                        listParkingEntrance = parkingEntranceTable.getParkingEntrance(currentContext);
                        webView.loadUrl("javascript:removeAll();");

                        for (ParkingEntrance pE : listParkingEntrance) {
                            for (int i=0; i < jsonArray.length();i++) {
                                try {
                                    if (pE.getId() == Integer.parseInt(jsonArray.getJSONObject(i).getString("dp_parc_id"))) {
                                        pE.setDpPlaceDisponible(jsonArray.getJSONObject(i).getString("dp_place_disponible"));
                                        pE.setDpDate(jsonArray.getJSONObject(i).getString("dp_date"));
                                        Float placesDispo = Float.parseFloat(pE.getDpPlaceDisponible());
                                        pE.setTauxDisponibilite((Float.toString(placesDispo/pE.getNbPlaces())));
                                    }
                                    //Toast.makeText(MainActivity.this, pE.getParking_fa(), Toast.LENGTH_LONG).show();

                                } catch (JSONException e){
                                    Toast.makeText(MainActivity.this, "Problème d'affectation des données temps réel", Toast.LENGTH_LONG).show();
                                }
                            }
                            webView.loadUrl("javascript:displayParkings(\"" + Integer.toString(pE.getId()) + "\",\"" + pE.getName() + "\",\"" + pE.getNbPlaces() + "\",\"" + pE.getType() + "\",\"" + pE.getHourly() +  "\",\"" + pE.getDpPlaceDisponible() + "\",\"" + pE.getTauxDisponibilite() + "\",\"" + pE.getDpDate() + "\",\""+ pE.getParking_fa() +"\",\""+ pE.getGeometry() + "\");");
                            //break;
                        }
                        switch (activeLayer) {
                            case "0":
                                webView.loadUrl("javascript:tousParkingsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "1": webView.loadUrl("javascript:parkingsGratuitsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "2": webView.loadUrl("javascript:parkingsPayantsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "3": webView.loadUrl("javascript:parkingsRelaisLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "6": webView.loadUrl("javascript:favorisLayer.addFeatures(featuresSqlParkings);");
                                break;
                        }
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    public void run() {
                        ParkingEntranceTable parkingEntranceTable = new ParkingEntranceTable(currentContext);
                        listParkingEntrance = parkingEntranceTable.getParkingEntrance(currentContext);
                        webView.loadUrl("javascript:removeAll();");

                        for (ParkingEntrance pE : listParkingEntrance) {
                            webView.loadUrl("javascript:displayParkings(\"" + Integer.toString(pE.getId()) + "\",\"" + pE.getName() + "\",\"" + pE.getNbPlaces() + "\",\"" + pE.getType() + "\",\"" + pE.getHourly() +  "\",\"" + pE.getDpPlaceDisponible() + "\",\"" + pE.getTauxDisponibilite() + "\",\"" + pE.getDpDate()+ "\",\"" + pE.getParking_fa() + "\",\"" + pE.getGeometry() + "\");");
                        }

                        switch (activeLayer) {
                            case "0": webView.loadUrl("javascript:tousParkingsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "1": webView.loadUrl("javascript:parkingsGratuitsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "2": webView.loadUrl("javascript:parkingsPayantsLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "3": webView.loadUrl("javascript:parkingsRelaisLayer.addFeatures(featuresSqlParkings);");
                                break;
                            case "6": webView.loadUrl("javascript:favorisLayer.addFeatures(featuresSqlParkings);");
                                break;
                        }
                    }
                });
            }
        }

        @JavascriptInterface
        public void displayCarPositionJava() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:displayCarPosition(" + carLatGPS + "," + carLonGPS + ")");
                    webView.loadUrl("javascript:carLayer.addFeatures([new OpenLayers.Feature.Vector(pointFeatureCar)]);");
                    //webView.loadUrl("javascript:dragCarLayer.activate();");
                }
            });
        }

        @JavascriptInterface
        public void displayGPSPositionJava() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:displayGPSPosition(" + latitudeGPS + "," + longitudeGPS + ")");
                    webView.loadUrl("javascript:gpsLayer.addFeatures([new OpenLayers.Feature.Vector(pointFeatureGPS)]);");
                }
            });
        }

        // Méthode appelée par le main.js pour lancer l'affichage des détails d'un parking
        @JavascriptInterface
        public void displayDetails(final String parking_id) {
            runOnUiThread(new Runnable() {
                public void run() {
                    final int id = Integer.parseInt(parking_id);
                    ParkingEntrance pe = getParkingEntranceById(id);
                    Bundle data = new Bundle();
                    data.putString("name", pe.getName());
                    data.putString("shortName", pe.getShortName());
                    data.putString("hourly", pe.getHourly());
                    data.putString("placesAvailables", pe.getDpPlaceDisponible());
                    data.putString("placeTotal", Integer.toString(pe.getNbPlaces()));
                    data.putString("favori", pe.getParking_fa());
                    data.putInt("id", id);
                    data.putString("geometry", pe.getGeometry());
                    CustomDetailParkingDialog cdd=new CustomDetailParkingDialog(currentContext, data, jsi);
                    cdd.requestWindowFeature(Window.FEATURE_NO_TITLE);

                    // This is line that does all the magic
                    cdd.getWindow().setBackgroundDrawableResource(
                            R.drawable.layout_bg_details);
                    cdd.show();
                }
            });
        }

        @JavascriptInterface
        public void setupNavigation(final String destinationGeom) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String latLon = Utils.strPointToLatLon(destinationGeom);
                    webView.loadUrl("javascript:setupNavigation(" + latLon + ");");
                }
            });
        }

        @JavascriptInterface
        public void launchNavigation(final String pointDestination) {
            String latLon = Utils.strPointToLatLon(pointDestination);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + latLon));
            startActivity(intent);
        }
    }

    // Méthode pour démarrer le timer pour la mise à jour auto des données temps réel
    public void startTimer() {

        stopTimerTask();

        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer (ms to start, ms interval)
        timer.schedule(timerTask, 1000, dataInterval*1000); //
    }

    // Arrêt du timer
    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    // Création de la timertask
    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

            handler.post(new Runnable() {
                public void run() {
                    retrievedata = new Retrievedata();
                    retrievedata.execute(requeteTempsReel, "tempsreel");
                }
            });
            }
        };
    }

    public ParkingEntrance getParkingEntranceById(int id)
    {
        ParkingEntrance pe = null;
        for(ParkingEntrance parkingEntrance : listParkingEntrance)
        {
            if(parkingEntrance.getId() == id)
                pe = parkingEntrance;
        }
        return pe;
    }

    // Méthode pour affichage de l'interface en plein écran (pas utilisée car saute à l'affichage des fenêtres de dialogue
    /*public void fullScreenCall() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }*/
}
