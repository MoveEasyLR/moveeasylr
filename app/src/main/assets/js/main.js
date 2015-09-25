var lon = 379500;
var lat = 6570000;
var map;

// Variable utilisée pour stocker fond de carte et layer actuels
var mapType;
var activeLayer;

// Les différents fonds de carte disponibles
var mapOSM, wmsOrtho2013;

// Layers
var horodateurLayer, tousParkingsLayer, parkingsGratuitsLayer, parkingsPayantsLayer, parkingsRelaisLayer, favorisLayer;
var gpsLayer, carLayer;

//Features
var featuresSqlHoro, featuresSqlParkings;
var pointFeatureCar, pointFeatureGPS;
var selectedFeature;
var selectCtrl, dragCarLayer;

// Variables liées au GPS
var gpsMarker;
var gpsLat;
var gpsLon;

// Statut de l'affichage de la voiture
var carMarker;

// Coordonnées de La Rochelle
var lonLR = -1.1532;
var latLR = 46.1558;

// Définition des niveaux de zoom utilisés en fonction du type de carte
var zoomOSM = 13;
var zoomOrtho = 8;

var wkt;


// Fonction appelée au chargement de map.html
function init0(){

    jsi.init0();
}


// Fonction appelée par jsi.init0() à la première ouverture de la map
function initMap(){

    Proj4js.defs['EPSG:2154'] = '+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 '+'+lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 '+'+units=m +no_defs';

    var mapOptions={
        projection: new OpenLayers.Projection("EPSG:2154"),
        units: "m",
        maxExtent: new OpenLayers.Bounds(-370000, 6550000, 385000, 6650000),
        displayProjection: new OpenLayers.Projection("EPSG:4326")
    };

    map = new OpenLayers.Map("map", mapOptions);

    // OSM Layer
    mapOSM = new OpenLayers.Layer.OSM();
    map.addLayer(mapOSM);

    // Ortho Layer
    wmsOrtho2013 = new OpenLayers.Layer.WMS( "Ortho 2013", "http://portail-sig.ville-larochelle.fr/opendata/carteWS.php?", {layers: 'ortho_2013',format: 'image/jpeg'},{isBaseLayer:true});
    map.addLayer(wmsOrtho2013);

    wkt = new OpenLayers.Format.WKT();

    // GPS Layer
    initGPSLayer();
    map.addLayer(gpsLayer);
    map.setLayerIndex(gpsLayer, 0)
    //gpsLayer.setZIndex(999);


    // Car Layer
    initCarLayer();
    map.addLayer(carLayer);
    //carLayer.setZIndex( 999 );

    // Initialisation couche horodateurs
    initHoroLayer();
    map.addLayer(horodateurLayer);

    // Initialisation couche tous parkings
    initTousParkingsLayer();
    map.addLayer(tousParkingsLayer);
    tousParkingsLayer.redraw()

    // Initialisation couche parkings gratuits
    initParkingsGratuitsLayer();
    map.addLayer(parkingsGratuitsLayer);

    // Initialisation couche parkings payants
    initParkingsPayantsLayer();
    map.addLayer(parkingsPayantsLayer);

    // Initialisation couche parkings relais
    initParkingsRelaisLayer();
    map.addLayer(parkingsRelaisLayer);

    // Initialisation couche favoris
    initFavorisLayer();
    map.addLayer(favorisLayer);

    // Initialisation du controle pour pouvoir bouger la position de la voiture
    dragCarLayer = new OpenLayers.Control.DragFeature(carLayer,{onComplete: function(){}});
    map.addControl(dragCarLayer);
    //dragCarLayer.activate();*/


    // Control des couches selectionnées
    selectCtrl = new OpenLayers.Control.SelectFeature (
        [horodateurLayer,tousParkingsLayer,parkingsGratuitsLayer,parkingsGratuitsLayer,parkingsPayantsLayer,parkingsRelaisLayer,favorisLayer, carLayer, gpsLayer],{
            //hover : true,
            clickout : true,
            clickoutFeature : function() {
                if (selectedFeature) map.removePopup(selectedFeature.popup);
            },
            multiple: false
        }
    );

    /*carLayer.events.on({
        "featureselected":function(event){
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });*/



    horodateurLayer.events.on({
        "featureselected":function(event) {
            var cluster_bounds = new OpenLayers.Bounds();
            event.feature.cluster.forEach(function(feature){
                cluster_bounds.extend(feature.geometry);
            })
            map.zoomToExtent(cluster_bounds)
        }
    });


    tousParkingsLayer.events.on({
        "featureselected":function(event){
            if (selectedFeature) map.removePopup(selectedFeature.popup);
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });

    parkingsGratuitsLayer.events.on({
        "featureselected":function(event){
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });

    parkingsPayantsLayer.events.on({
        "featureselected":function(event){
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });

    parkingsRelaisLayer.events.on({
        "featureselected":function(event){
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });

    favorisLayer.events.on({
        "featureselected":function(event){
            selectCtrl.unselectAll();
            feature = event.feature;
            selectedFeature = feature;
            text_popup();
            popup.autoSize = false;
            feature.popup = popup;
            map.addPopup(popup);
        }
    });

    map.addControl(selectCtrl);
    selectCtrl.activate();

    // Car Layer
    initCarLayer();
    map.addLayer(carLayer);
    //carLayer.setZIndex( 999 );

    mapChoice();
}


// Fonction appelant la fonction correspondant au fond de carte choisi
function mapChoice(){

    switch (mapType) {
		case 0:
			initOSM();
		break;
		case 1:
			initOrtho();
		break;
	}
}


// Définition du fond de carte actuel : OSM (avec maintien de l'affichage de la position GPS et de la voiture)
function initOSM() {

    map.setBaseLayer(mapOSM);
    removeAll();

    switch(activeLayer) {
            case 0:
                jsi.loadVecLayerParkings();
                jsi.loadVecLayerParkings();
                break;
            case 1:
                jsi.loadVecLayerParkings();
                break;
            case 2:
                jsi.loadVecLayerParkings();
                jsi.loadVecLayerParkings();
                break;
            case 3:
                jsi.loadVecLayerParkings();
                break;
            case 4:
                jsi.loadVecLayerHoro();
                break;
            case 5:

                break;
            case 6:
                jsi.loadVecLayerParkings();
                break;
        }


    centerMap(lonLR, latLR, zoomOSM);
}


// Définition du fond de carte actuel : Ortho2013 (avec maintien de l'affichage de la position GPS et de la voiture)
function initOrtho() {

    map.setBaseLayer(wmsOrtho2013);
    removeAll();

    // Chargement des couches si on ne vient pas du onCreate() du Java
    // (pour maintien des couches au changement de fond de carte)
    switch(activeLayer) {
            case 0:
                jsi.loadVecLayerParkings();
                jsi.loadVecLayerParkings();
                break;
            case 1:
                jsi.loadVecLayerParkings();
                break;
            case 2:
                jsi.loadVecLayerParkings();
                jsi.loadVecLayerParkings();
                break;
            case 3:
                jsi.loadVecLayerParkings();
                break;
            case 4:
                jsi.loadVecLayerHoro();
                break;
            case 5:

                break;
            case 6:
                jsi.loadVecLayerParkings();
                break;
        }


    centerMap(lonLR, latLR, zoomOrtho);

}


// Définition du style de la couche horodateurs
function initHoroLayer() {
    // Les 3 variables suivantes servent à définir le style de la couche horodateur

    var symbolizer;
    var styleHoro;
    var cluster_horo;

    //couleurs selon la densité des clusters
    var colors ={
        lowDensity: "#2980ff",
        midDensity: "#0000ff",
        highDensity: "#000064"
        };

    // déclaration de règles permettant de mettre en valeur la densité des clusters
    var oneRule = new OpenLayers.Rule({
        filter : new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.EQUAL_TO,
            property: "count",
            value: 1
        }),
        symbolizer:{
            externalGraphic : "${getGraphic}",
            pointRadius : 15,
            fillOpacity : 1,
        }
    });

    // règle pour les regroupements sup. à 2
    var lowRule = new OpenLayers.Rule({
        filter : new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.BETWEEN,
            property : "count",
            lowerBoundary : 2,
            upperBoundary : 10
        }),
        symbolizer: {
            fillColor : colors.lowDensity,
            fillOpacity: 0.9,
            strokeColor : colors.lowDensity,
            strokeOpacity: 0.5,
            strokeWidth: 12,
            pointRadius: 10,
            label: "${getCount}",
            labelOutlineWidth:1,
            fontColor: "#FFFFFF",
            fontOpacity: 0.8,
            fontSize:"12px"
        }
    });

    var midRule = new OpenLayers.Rule({
        filter : new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.BETWEEN,
            property : "count",
            lowerBoundary : 11,
            upperBoundary : 29
        }),
        symbolizer: {
            fillColor: colors.midDensity,
            fillOpacity: 0.9,
            strokeColor : colors.midDensity,
            strokeOpacity: 0.5,
            strokeWidth: 12,
            pointRadius: 15,
            label: "${getCount}",
            labelOutlineWidth:1,
            fontColor: "#FFFFFF",
            fontOpacity: 0.8,
            fontSize:"12px"
        }
    });

    var highRule = new OpenLayers.Rule({
        filter : new OpenLayers.Filter.Comparison({
            type: OpenLayers.Filter.Comparison.GREATER_THAN,
            property : "count",
            value : 30
        }),
        symbolizer: {
            fillColor :colors.highDensity,
            fillOpacity: 0.9,
            strokeColor : colors.highDensity,
            strokeOpacity: 0.5,
            strokeWidth: 12,
            pointRadius: 20,
            label: "${getCount}",
            labelOutlineWidth:1,
            fontColor: "#FFFFFF",
            fontOpacity: 0.8,
            fontSize:"12px"
        }
    });

    //context permet d alimenter le label des règles et la représentation par une image si pas de cluster
    var context = {
        getGraphic: function(feature) {
            //console.log(feature.cluster[0].attributes.hor_libell);
            if(feature.cluster[0].attributes.hor_libell === "Zone Verte") {return "js/img/horodateurvert.png"}
            if(feature.cluster[0].attributes.hor_libell === "Zone Orange") {return "file:///android_asset/js/img/horodateurorange.png"}
            if(feature.cluster[0].attributes.hor_libell === "Zone Rouge") {return "file:///android_asset/js/img/horodateurrouge.png"}
            if(feature.cluster[0].attributes.hor_libell === "") {return "file:///android_asset/js/img/horodateurvert.png"}
            },
        getCount: function(feature){
            if (feature.cluster && feature.cluster.length > 1) {return feature.cluster.length}
            else {return ""}
        }
    };

    wstyleMapRule = new OpenLayers.Style(null, {
        rules : [oneRule, lowRule, midRule, highRule],
        context : context
    });

    styleHoro = new OpenLayers.StyleMap({"default":wstyleMapRule, "select": {pointRadius:40}});
    cluster_horo = new OpenLayers.Strategy.Cluster();
    cluster_horo.distance = 30;

    if (selectCtrl) {
        selectCtrl.deactivate();
        map.removeControl(selectCtrl)
        selectCtrl = null;
    }

    if (horodateurLayer) {

        horodateurLayer.destroyFeatures;
        map.removeLayer(horodateurLayer);
        horodateurLayer = null;
    }

    // Couche horo

    featuresSqlHoro = [];
    horodateurLayer = new OpenLayers.Layer.Vector("Horodateur", {
        projection: new OpenLayers.Projection("EPSG:2154"),
        protocol: new OpenLayers.Protocol.Script({
            format: new OpenLayers.Format.JSON({
                extractStyles: true,
                extractAttributes: true
            })
        }),
        styleMap: styleHoro,
        strategies : [cluster_horo],
    });

}


// Définition du style de la couche tous parkings
function initTousParkingsLayer() {

        var context = {
            getGraphic: function(feature) {
                if(feature.attributes.parking_fa==="1") {
                    {var taux_disponibilite = parseFloat(feature.attributes.taux_disponibilite)}
                    if(taux_disponibilite>0.2) {return "file:///android_asset/js/img/parkingpayantvert_favori.png"};
                    if((taux_disponibilite<=0.2)&&(taux_disponibilite>0.0)) {return "file:///android_asset/js/img/parkingpayantorange_favori.png"};
                    if(taux_disponibilite===0.0) {return "file:///android_asset/js/img/parkingpayantrouge_favori.png"};
                    if(feature.attributes.parking_ty==="Parking gratuit") {return "file:///android_asset/js/img/parkinggratuit_favori.png"};
                    if(feature.attributes.parking_ty==="Parking relais") {return "file:///android_asset/js/img/parkingrelais_favori.png"};
                    if(feature.attributes.taux_disponibilite==="NA" && feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="Parking relais" ) {return "file:///android_asset/js/img/parkingpayant_favori.png"};
                }
                else {
                    var taux_disponibilite = parseFloat(feature.attributes.taux_disponibilite);
                    if(taux_disponibilite>0.2) {return "file:///android_asset/js/img/parkingpayantvert.png"};
                    if((taux_disponibilite<=0.2)&&(taux_disponibilite>0.0)) {return "file:///android_asset/js/img/parkingpayantorange.png"};
                    if(taux_disponibilite===0.0) {return "file:///android_asset/js/img/parkingpayantrouge.png"};
                    if(feature.attributes.taux_disponibilite==="NA" && feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="Parking relais" ) {return "file:///android_asset/js/img/parkingpayant.png"};
                    if(feature.attributes.parking_ty==="Parking gratuit") {return "file:///android_asset/js/img/parkinggratuit.png"};
                    if(feature.attributes.parking_ty==="Parking relais") {return "file:///android_asset/js/img/parkingrelais.png"};
                }
            }
        };

        var template = {
            externalGraphic: "${getGraphic}",
            graphicWidth:30,
            graphicHeight:30,
            graphicYOffset:-15
        };
        var styleParkings = new OpenLayers.Style(template, {context: context});

        // Couche tous les parkings
        featuresSqlParkings = [];
        tousParkingsLayer = new OpenLayers.Layer.Vector("Tous Parkings", {
            projection: new OpenLayers.Projection("EPSG:2154"),
            protocol: new OpenLayers.Protocol.Script({
                format: new OpenLayers.Format.JSON({
                    extractStyles: true,
                    extractAttributes: true
                })
            }),
            styleMap: new OpenLayers.StyleMap(styleParkings)
        });
        tousParkingsLayer.redraw({ force: true });

}

// Définition du style de la couche parkings gratuits
function initParkingsGratuitsLayer() {

        var context = {
            getGraphic: function(feature) {
                if(feature.attributes.parking_ty==="Parking gratuit") {
                    if(feature.attributes.parking_fa==="1") {return "file:///android_asset/js/img/parkinggratuit_favori.png"}
                    else {return "file:///android_asset/js/img/parkinggratuit.png"}
               }
               else return "";
            }

        };

        var template = {
            externalGraphic: "${getGraphic}",
            graphicWidth:30,
            graphicHeight:30,
            graphicYOffset:-15
        };
        var styleParkings = new OpenLayers.Style(template, {context: context});


        // Couche parkings gratuits
        featuresSqlParkings = [];
        parkingsGratuitsLayer = new OpenLayers.Layer.Vector("Parkings Gratuits", {
            projection: new OpenLayers.Projection("EPSG:2154"),
            protocol: new OpenLayers.Protocol.Script({
                format: new OpenLayers.Format.JSON({
                    extractStyles: true,
                    extractAttributes: true
                })
            }),
            styleMap: new OpenLayers.StyleMap(styleParkings)
        });
}

// Définition du style de la couche parkings payants
function initParkingsPayantsLayer() {

        var context = {
            getGraphic: function(feature) {
                if (feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="Parking relais"){
                    if(feature.attributes.parking_fa==="1") {
                        var taux_disponibilite = parseFloat(feature.attributes.taux_disponibilite);
                        if(taux_disponibilite>0.2) {return "file:///android_asset/js/img/parkingpayantvert_favori.png"};
                        if((taux_disponibilite<=0.2)&&(taux_disponibilite>0.0)) {return "file:///android_asset/js/img/parkingpayantorange_favori.png"};
                        if(taux_disponibilite===0.0) {return "file:///android_asset/js/img/parkingpayantrouge_favori.png"};
                        if(feature.attributes.taux_disponibilite==="NA" && feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="parking relais" ) {return "file:///android_asset/js/img/parkingpayant_favori.png"};
                    }
                    else if (feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="Parking relais") {
                         var taux_disponibilite = parseFloat(feature.attributes.taux_disponibilite);
                         if(taux_disponibilite>0.2) {return "file:///android_asset/js/img/parkingpayantvert.png"};
                         if((taux_disponibilite<=0.2)&&(taux_disponibilite>0.0)) {return "file:///android_asset/js/img/parkingpayantorange.png"};
                         if(taux_disponibilite===0.0) {return "file:///android_asset/js/img/parkingpayantrouge.png"};
                         if(feature.attributes.taux_disponibilite==="NA" && feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="Parking relais" ) {return "file:///android_asset/js/img/parkingpayant.png"};
                    }
                }
                else return "";
            }
        };

        var template = {
            externalGraphic: "${getGraphic}",
            graphicWidth:30,
            graphicHeight:30,
            graphicYOffset:-15
        };
        var styleParkings = new OpenLayers.Style(template, {context: context});


        // Couche horo
        featuresSqlParkings = [];
        parkingsPayantsLayer = new OpenLayers.Layer.Vector("Parkings Payants", {
            projection: new OpenLayers.Projection("EPSG:2154"),
            protocol: new OpenLayers.Protocol.Script({
                format: new OpenLayers.Format.JSON({
                    extractStyles: true,
                    extractAttributes: true
                })
            }),
            styleMap: new OpenLayers.StyleMap(styleParkings)
        });
}

// Définition du style de la couche parkings relais
function initParkingsRelaisLayer() {

    var context = {
        getGraphic: function(feature) {
            if(feature.attributes.parking_ty==="Parking relais"){
                if(feature.attributes.parking_fa==="1") {return "file:///android_asset/js/img/parkingrelais_favori.png"}
                else{return "file:///android_asset/js/img/parkingrelais.png"};
            }
            else return "";
        }
    };

    var template = {
        externalGraphic: "${getGraphic}",
        graphicWidth:30,
        graphicHeight:30,
        graphicYOffset:-15
    };
    var styleParkings = new OpenLayers.Style(template, {context: context});


    // Couche parkings relais
    featuresSqlParkings = [];
    parkingsRelaisLayer = new OpenLayers.Layer.Vector("Parkings Relais", {
        projection: new OpenLayers.Projection("EPSG:2154"),
        protocol: new OpenLayers.Protocol.Script({
            format: new OpenLayers.Format.JSON({
                extractStyles: true,
                extractAttributes: true
            })
        }),
        styleMap: new OpenLayers.StyleMap(styleParkings)
    });
}


// Définition du style de la couche favoris
function initFavorisLayer() {

    var context = {
        getGraphic: function(feature) {
            if(feature.attributes.parking_fa==="1") {
                if (feature.attributes.taux_disponibilite!="NA") {
                        var taux_disponibilite = parseFloat(feature.attributes.taux_disponibilite);
                }
                if(taux_disponibilite>0.2) {return "file:///android_asset/js/img/parkingpayantvert_favori.png"};
                if((taux_disponibilite<=0.2)&&(taux_disponibilite>0.0)) {return "file:///android_asset/js/img/parkingpayantorange_favori.png"};
                if(taux_disponibilite===0.0) {return "file:///android_asset/js/img/parkingpayantrouge_favori.png"};
                if(feature.attributes.taux_disponibilite==="NA" && feature.attributes.parking_ty!="Parking gratuit" && feature.attributes.parking_ty!="parking relais" ) {return "file:///android_asset/js/img/parkingpayant_favori.png"};
                if(feature.attributes.parking_ty==="Parking gratuit") {return "file:///android_asset/js/img/parkinggratuit_favori.png"};
                if(feature.attributes.parking_ty==="parking relais") {return "file:///android_asset/js/img/parkingrelais_favori.png"};
            }
            else {
                return"";
            }
        }
    };

    var template = {
        externalGraphic: "${getGraphic}",
        graphicWidth:30,
        graphicHeight:30,
        graphicYOffset:-15
    };
    var styleFavoris = new OpenLayers.Style(template, {context: context});

    //couche favoris
    featuresSqlParkings = [];
    favorisLayer = new OpenLayers.Layer.Vector("Favoris", {
        projection: new OpenLayers.Projection("EPSG:2154"),
        protocol: new OpenLayers.Protocol.Script({
            format: new OpenLayers.Format.JSON({
                extractStyles: true,
                extractAttributes: true
            })
        }),
        styleMap: new OpenLayers.StyleMap(styleFavoris)
    });
}


// Style de la couche gpsLayer (position GPS)
function initGPSLayer() {

    var context = {
        getGraphic: function(feature) {
             return "file:///android_asset/js/img/ic_place_red_24dp.png";
        }
    };

    var template = {
        externalGraphic: "${getGraphic}",
        graphicWidth:50,
        graphicHeight:50,
        graphicYOffset:-25
    };

    var styleGPS = new OpenLayers.Style(template, {context: context});

    gpsLayer = new OpenLayers.Layer.Vector("GPS", {
        projection: map.getProjectionObject(),
        styleMap: new OpenLayers.StyleMap(styleGPS)//,
        //strategies : [new OpenLayers.Strategy.Box()]
    });
}


// Style de la couche carLayer (position voiture)
function initCarLayer() {
    var context = {
        getGraphic: function(feature) {
             return "file:///android_asset/js/img/ic_directions_car_blue_24dp.png";
             }
    };

    var template = {
        externalGraphic: "${getGraphic}",
        graphicWidth:50,
        graphicHeight:50,
        graphicYOffset:-25
    };
    var styleCar = new OpenLayers.Style(template, {context: context});

    carLayer = new OpenLayers.Layer.Vector("Car", {
        projection: new OpenLayers.Projection("EPSG:2154"),
        protocol: new OpenLayers.Protocol.Script({
            format: new OpenLayers.Format.JSON({
                extractStyles: true,
                extractAttributes: true
            })
        }),
        styleMap: new OpenLayers.StyleMap(styleCar)
    });
}



// Affichage des horodateurs
function displayHoro(hor_id, hor_libell,horogeom){
    wkt.internalProjection = map.getProjectionObject();
    wkt.externalProjection = new OpenLayers.Projection("EPSG:2154");
    var geom = wkt.read(horogeom);
    //geom.geometry.transform(new OpenLayers.Projection("EPSG:2154"), map.getProjectionObject());
    geom.attributes = {'hor_id':hor_id,'hor_libell':hor_libell};
    featuresSqlHoro.push(geom);

    map.removePopup();

    if (selectedFeature) map.removePopup(selectedFeature.popup);
}


// Affichage des parkings
function displayParkings(parking_id, parking_no, parking_nb, parking_ty, parking_ho, dp_place_disponible,taux_disponibilite, dp_date, parking_fa, parkEGeom){

    var geom = wkt.read(parkEGeom);
    //console.log(parking_no+" "+parking_fa);

    wkt.internalProjection = map.getProjectionObject();
    wkt.externalProjection = new OpenLayers.Projection("EPSG:2154");

    if (parking_nb === "0") {
     parking_nb = "NA";
    }


    if (parking_ty === "parking gratuit") {
         parking_ty = "Parking gratuit";
    }

    if (parking_ty === "parking relais") {
             parking_ty = "Parking relais";
    }

    if (parking_ty === "parking souterrain") {
                 parking_ty = "Parking souterrain";
    }

    if (parking_ty === "parking en enclos") {
                 parking_ty = "Parking en enclos";
    }

    if (parking_ty === "parking abonnés") {
                     parking_ty = "Parking abonnés";
    }

    geom.attributes = {'parking_id':parking_id, 'parking_no':parking_no, 'parking_nb':parking_nb, 'parking_ty':parking_ty, 'parking_ho':parking_ho,
                        'dp_place_disponible':dp_place_disponible, 'taux_disponibilite':taux_disponibilite, 'dp_date':dp_date, 'parking_fa':parking_fa};
    featuresSqlParkings.push(geom);

    if (selectedFeature) map.removePopup(selectedFeature.popup);
}


// Affichage de la position GPS
function displayGPSPosition(gpsLatStr, gpsLonStr) {

    gpsLat = parseFloat(gpsLatStr);
    gpsLon = parseFloat(gpsLonStr);
    console.log(gpsLat);
    pointFeatureGPS = new OpenLayers.Geometry.Point(gpsLon,gpsLat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
}

// Affichage de la position de la voiture
function displayCarPosition(carLatStr, carLonStr) {

    var carLat = parseFloat(carLatStr);
    var carLon = parseFloat(carLonStr);

    pointFeatureCar = new OpenLayers.Geometry.Point(carLon,carLat).transform(new OpenLayers.Projection("EPSG:4326"), map.getProjectionObject());
}


// Centrage de la carte sur La Rochelle
function centerMap(lon, lat, zoom) {

    if ((typeof lon) === "string") {
        lon = parseFloat(lon);
        lat = parseFloat(lat);
    }

    if (zoom==="") {
        map.setCenter(new OpenLayers.LonLat(lon,lat) // Centre de la carte
                          .transform(
                            new OpenLayers.Projection("EPSG:4326"), // transformation de WGS 1984
                            map.getProjectionObject() // en projection Mercator sphérique
                          ), map.getZoom()
                        );
    } else {
        map.setCenter(new OpenLayers.LonLat(lon,lat) // Centre de la carte
                          .transform(
                            new OpenLayers.Projection("EPSG:4326"), // transformation de WGS 1984
                            map.getProjectionObject() // en projection Mercator sphérique
                          ), zoom
                        );
    }
}




// Suppression de toutes les features des couches de la carte
function removeAll() {
    featuresSqlHoro = [];
    featuresSqlParkings = [];
    horodateurLayer.removeAllFeatures();
    tousParkingsLayer.removeAllFeatures();
    parkingsGratuitsLayer.removeAllFeatures();
    parkingsPayantsLayer.removeAllFeatures();
    parkingsRelaisLayer.removeAllFeatures();
    favorisLayer.removeAllFeatures();
}

function drawTousParkings(){
    tousParkingsLayer.drawFeature();
}

// Suppression des features du GPS seulement
function removeGPS() {
    //pointFeatureGPS = [];
    gpsLayer.destroyFeatures();
    gpsLayer.removeAllFeatures();
}

// Suppression des features de la voiture seulement
function removeCar() {
    pointFeatureCar = [];
    carLayer.removeAllFeatures();
}

//Supression des popups
function onPopupClose() {
    selectCtrl.unselect(selectedFeature);
    map.removePopup(selectedFeature.popup);
    //selectedFeature.popup.destroy();
    //selectedFeature.popup = null;
}

//renvoi de l'id du parking au JAVA pour fenêtre info
function getInfo(parking_id) {
    jsi.displayDetails(parking_id);
}

//renvoie la position de la destination au JAVA pour lancer la navigation Maps
function setupNavigation(lat, lon) {
    console.log(lat);
    console.log(lon);
    pointDestination = new OpenLayers.Geometry.Point(lon, lat).
    transform(new OpenLayers.Projection("EPSG:2154"), new OpenLayers.Projection("EPSG:4326"));
    jsi.launchNavigation(pointDestination.toString());
}


//Gestion de l'affichage des popups
function getNomParking(nomParking,TypeParking) {
    if (nomParking == "") {
        if (TypeParking == "Parking gratuit" ) {
                return "<div style='font-size:14px'>"+ TypeParking +"</div>" ;
        }
    } else {
        return "<div style='font-size:14px'>"+ nomParking +"</div>";
    }
}

function getTypeParking(TypeParking, nomParking) {
    if (TypeParking == "Parking gratuit" ) {
        if (nomParking != "") {
            return "<br>" + TypeParking ;
        }
        else {
            return "";
        }
    }
    else {
        return "<br>" + TypeParking;
    }
}

function getNbPlaces(nbPlaces) {
    if (nbPlaces == "NA") {
        return "";
    } else {
        return "<br> Nombre de place : " + nbPlaces;
    }
}

function getPlacesDispo(PlacesDispo) {
    if (PlacesDispo == "NA") {
        return "";
    } else {
        return "<br> Places disponibles : " + PlacesDispo;
    }
}

//fonction text_popup
function text_popup() {
    if (feature.attributes.parking_no == ""){y_popup = 80}
    else {y_popup = 80};
    if (feature.attributes.parking_no.length > 22) {y_popup = 100};

    if (feature.attributes.parking_ty == "Parking gratuit" ) {
        if (feature.attributes.parking_nb > 0) {
            y_popup = y_popup + 20;
        }
    }
    else {y_popup = y_popup + 20};

    if (feature.attributes.parking_nb > 0) {
        y_popup = y_popup + 20;
    };

    if (feature.attributes.dp_place_disponible != "NA") {
        y_popup = y_popup + 20;
    };

    //y_popup = 100;
    popup = new OpenLayers.Popup.FramedCloud("chicken",
        feature.geometry.getBounds().getCenterLonLat(),
        new OpenLayers.Size(150,y_popup),
        //popup(feature.attributes.parking_no, feature.attributes.parking_ty, feature.attributes.parking_nb, feature.attributes.dp_place_disponible)
        "<div style='font-size:12px'>"+ getNomParking(feature.attributes.parking_no,feature.attributes.parking_ty) + getTypeParking(feature.attributes.parking_ty, feature.attributes.parking_no) + getNbPlaces(feature.attributes.parking_nb) + getPlacesDispo(feature.attributes.dp_place_disponible)+"</div><br><img src='js/img/info.png' ALIGN=left WIDTH=40px HEIGHT=40px onclick='getInfo("+feature.attributes.parking_id+")'><img src='js/img/voiture.png' ALIGN=right WIDTH=40px HEIGHT=40px onclick='setupNavigation("+feature.geometry.getBounds().getCenterLonLat().lat+","+feature.geometry.getBounds().getCenterLonLat().lon+ ")'>",
        null,true,onPopupClose)
    console.log(feature.attributes.parking_id);

}




