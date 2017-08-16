package com.sleepingbear.envocabulary;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class VocabularyEditActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private String kind;
    private String seq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = this.getIntent().getExtras();
        if ( "ADD".equals(b.getString("MODE")) ) {
            kind = b.getString("kind");

        } else {
            seq = b.getString("SEQ");
            kind = b.getString("kind");

            ((EditText) this.findViewById(R.id.my_et_word)).setText(b.getString("WORD"));
            ((EditText) this.findViewById(R.id.my_et_mean)).setText(b.getString("MEAN"));
            ((EditText) this.findViewById(R.id.my_et_spelling)).setText(b.getString("SPELLING"));
            ((EditText) this.findViewById(R.id.my_et_samples)).setText(b.getString("SAMPLES"));
            ((EditText) this.findViewById(R.id.my_et_memo)).setText(b.getString("MEMO"));
        }

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

}
