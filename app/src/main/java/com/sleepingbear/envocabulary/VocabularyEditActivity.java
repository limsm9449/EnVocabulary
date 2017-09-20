package com.sleepingbear.envocabulary;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

public class VocabularyEditActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private VocabularyEditwCursorAdapter adapter;
    private String mode;
    private String kind;
    private String seq;
    private String word;
    private String mean;
    private String spelling;
    private String samples;
    private String memo;
    private InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle b = this.getIntent().getExtras();
        mode = b.getString("MODE");
        kind = b.getString("KIND");

        if ( "UPDATE".equals(mode) ) {
            ab.setTitle("단어 수정");
        } else {
            ab.setTitle("단어 등록");
        }

        if ( "UPDATE".equals(b.getString("MODE")) ) {
            seq = b.getString("SEQ");
            word = DicUtils.getString(b.getString("WORD"));
            mean = DicUtils.getString(b.getString("MEAN"));
            spelling = DicUtils.getString(b.getString("SPELLING")).replace("[","").replace("]","");
            samples = DicUtils.getString(b.getString("SAMPLES"));
            memo = DicUtils.getString(b.getString("MEMO"));
        } else {
            seq = "";
            word = "";
            mean = "";
            spelling = "";
            samples = "";
            memo = "";
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        ((ImageView)this.findViewById(R.id.my_iv_save)).setOnClickListener(this);

        getListView();

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 _id, 1 SEQ, '단어' KIND, '" + word.replaceAll("'","''") + "' INPUT" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 2 _id, 2 SEQ, '뜻' KIND, '" + mean.replaceAll("'","''") + "' INPUT" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 3 _id, 3 SEQ, '스펠링' KIND, '" + spelling.replaceAll("'","''") + "' INPUT" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 4 _id, 4 SEQ, '예제' KIND, '" + samples .replaceAll("'","''")+ "' INPUT" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 5 _id, 5 SEQ, '메모' KIND, '" + memo.replaceAll("'","''") + "' INPUT" + CommConstants.sqlCR);

        Cursor cursor = db.rawQuery(sql.toString(), null);

        ListView listView = (ListView) this.findViewById(R.id.my_lv_list);
        adapter = new VocabularyEditwCursorAdapter(getApplicationContext(), cursor);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);

        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            final String seq = cur.getString(cur.getColumnIndexOrThrow("SEQ"));
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            final View dialog_layout;

            if ( "1".equals(seq) || "3".equals(seq) ) {
                dialog_layout = inflater.inflate(R.layout.dialog_vocabulary_iu, null);
            } else {
                dialog_layout = inflater.inflate(R.layout.dialog_vocabulary_iu2, null);
            }

            //dialog 생성..
            AlertDialog.Builder builder = new AlertDialog.Builder(VocabularyEditActivity.this);
            builder.setView(dialog_layout);
            final AlertDialog alertDialog = builder.create();

            final EditText et_input = ((EditText) dialog_layout.findViewById(R.id.my_et_input));

            if ( "1".equals(seq) ) {
                ((TextView) dialog_layout.findViewById(R.id.my_tv_label)).setText("단어를 입력하세요.");
                et_input.setText(word);
            } else if ( "2".equals(seq) ) {
                ((TextView) dialog_layout.findViewById(R.id.my_tv_label)).setText("뜻을 입력하세요.");
                et_input.setText(mean);
            } else if ( "3".equals(seq) ) {
                ((TextView) dialog_layout.findViewById(R.id.my_tv_label)).setText("스펠링을 입력하세요.");
                et_input.setText(spelling);
            } else if ( "4".equals(seq) ) {
                ((TextView) dialog_layout.findViewById(R.id.my_tv_label)).setText("예제를 입력하세요.");
                et_input.setText(samples);
            } else {
                ((TextView) dialog_layout.findViewById(R.id.my_tv_label)).setText("메모를 입력하세요.");
                et_input.setText(memo);
            }

            ((Button) dialog_layout.findViewById(R.id.my_b_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( ("1".equals(seq) || "2".equals(seq) ) && "".equals(et_input.getText().toString())) {
                        Toast.makeText(getApplication(), "내용을 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        alertDialog.dismiss();

                        if ( "1".equals(seq) ) {
                            word = et_input.getText().toString().toLowerCase();
                            HashMap wordInfo = DicDb.getWordInfo(db, word);
                            if ( wordInfo.containsKey("WORD") ) {
                                mean = (String)wordInfo.get("MEAN");
                                spelling = DicUtils.getString((String)wordInfo.get("SPELLING")).replace("[","").replace("]","");
                                samples = DicDb.getWordSamples(db, word);
                            }
                        } else if ( "2".equals(seq) ) {
                            mean = et_input.getText().toString();
                        } else if ( "3".equals(seq) ) {
                            spelling = et_input.getText().toString().replace("[","").replace("]","");
                        } else if ( "4".equals(seq) ) {
                            samples = et_input.getText().toString();
                        } else {
                            memo = et_input.getText().toString();
                        }

                        getListView();

                        imm.hideSoftInputFromWindow(et_input.getWindowToken(),0);

                    }
                }
            });
            ((Button) dialog_layout.findViewById(R.id.my_b_close)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_iv_save ) {
            if ( "".equals(word) ) {
                Toast.makeText(getApplicationContext(), "단어를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if ( "".equals(mean) ) {
                Toast.makeText(getApplicationContext(), "뜻을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("단어를 저장하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ( "ADD".equals(mode) ) {
                                DicDb.insMyVocabulary(db, kind, word, mean, spelling, samples, memo);
                                Toast.makeText(getApplicationContext(), "단어를 등록하였습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                DicDb.updMyVocabulary(db, seq, kind, word, mean, spelling, samples, memo);
                                Toast.makeText(getApplicationContext(), "단어를 수정하였습니다.", Toast.LENGTH_SHORT).show();
                            }

                            Intent intent = new Intent();
                            intent.putExtra("MSG", CommConstants.msgSave);
                            setResult(RESULT_OK, intent);

                            finish();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

}


class VocabularyEditwCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public VocabularyEditwCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_vocabulary_edit_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_kind)).setText(cursor.getString(cursor.getColumnIndexOrThrow("KIND")));
        ((TextView) view.findViewById(R.id.my_tv_input)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INPUT")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_kind)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_input)).setTextSize(fontSize);
    }
}