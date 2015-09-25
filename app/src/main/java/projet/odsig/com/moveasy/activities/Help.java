package projet.odsig.com.moveasy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;


import com.viewpagerindicator.CirclePageIndicator;

import java.util.List;
import java.util.Vector;

import projet.odsig.com.moveasy.R;
import android.app.Activity;

// Classe définissant l'Activité pour l'affichage de l'aide en tant que FragmentActivity
// (avec ajout des fragments pour chaque page de l'aide)
public class Help extends Activity {
    ImageButton closeButton;
    WebView wv;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.aide);

        ImageView btnBack = (ImageView)findViewById(R.id.closeButtonHelp);
        btnBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        wv = (WebView) findViewById(R.id.webViewAide);
        //WebSettings webSettings = wv.getSettings();
        //webSettings.setJavaScriptEnabled(true);
        //wv.addJavascriptInterface(new JavaScriptInterface(this), "jb");

        wv.loadUrl("file:///android_asset/aide.html");

        closeButton = (ImageButton) findViewById(R.id.closeButtonHelp);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentParam = new Intent(Help.this, MainActivity.class);
                startActivity(intentParam);
                finish();
            }
        });
    }

    /*public void fullScreenCall() {

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }*/

    /*public void closeHelp() {
        Intent intent = new Intent(Help.this, MainActivity.class);
        startActivity(intent);
        finish();
    }*/

}
