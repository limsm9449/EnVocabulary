package com.sleepingbear.envocabulary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.HashMap;

public class DaumVocabularyActivity extends AppCompatActivity {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    public DaumVocabularyCursorAdapter adapter;
    public Spinner s_daumKind;
    private int mOrder = -1;
    private String mOrderName = "";
    private String daumKind = "";
    private String vocName = "";
    private String categoryId = "";
    private int mSelect = 0;
    private Cursor cursor;
    private String mCategoryKind = "";

    DicCategoryTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_vocabulary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        Bundle b = getIntent().getExtras();
        if ( b != null ) {
            mCategoryKind = DicUtils.getString(b.getString("CATEGORY_KIND"));
        }

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery(DicQuery.getDaumKind(), null);
        String[] from = new String[]{"KIND_NAME"};
        int[] to = new int[]{android.R.id.text1};
        SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, cursor, from, to);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_daumKind = (Spinner) this.findViewById(R.id.my_s_daumKind);
        s_daumKind.setAdapter(mAdapter);
        s_daumKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                daumKind = ((Cursor) s_daumKind.getSelectedItem()).getString(1);

                //메뉴 갱신
                invalidateOptionsMenu();

                if ( "TOEIC,TOEFL,TEPS,수능영어,NEAT/NEPT,초중고영어,회화,기타".indexOf(daumKind) > -1 ) {
                    if (DicUtils.equalPreferencesDate(getApplicationContext(), daumKind)) {
                        changeListView();
                    } else {
                        if (DicUtils.isNetWork(DaumVocabularyActivity.this)) {
                            task = new DicCategoryTask();
                            task.execute();
                        } else {
                            changeListView();
                            Toast.makeText(getApplicationContext(), "인터넷에 연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    changeListView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //정렬
        Spinner spinner = (Spinner) this.findViewById(R.id.my_s_daumOrd);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.daumOrderValue, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mOrder = parent.getSelectedItemPosition();
                mOrderName = getResources().getStringArray(R.array.daumOrderValue)[mOrder];

                changeListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner.setSelection(3);



        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    public void changeListView() {
        //정렬 보여주기
        if ( "R1,R2,R3".indexOf(daumKind) < 0 ) {
            Spinner spinner = (Spinner) this.findViewById(R.id.my_s_daumOrd);
            spinner.setVisibility(View.VISIBLE);
        } else {
            Spinner spinner = (Spinner) this.findViewById(R.id.my_s_daumOrd);
            spinner.setVisibility(View.GONE);
        }

        cursor = db.rawQuery(DicQuery.getDaumSubCategoryCount(((Cursor) s_daumKind.getSelectedItem()).getString(1), mOrder), null);

        if ( cursor.getCount() == 0 ) {
            Toast.makeText(this, "검색된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
        }

        ListView listView = (ListView) this.findViewById(R.id.my_lv);
        adapter = new DaumVocabularyCursorAdapter(getApplicationContext(), cursor, 0);
        listView.setAdapter(adapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);
        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("KIND", cur.getString(cur.getColumnIndexOrThrow("KIND")));
            bundle.putString("CATEGORY_ID", cur.getString(cur.getColumnIndexOrThrow("CATEGORY_ID")));
            bundle.putString("CATEGORY_NAME", cur.getString(cur.getColumnIndexOrThrow("CATEGORY_NAME")));

            Intent intent = new Intent(getApplication(), DaumVocabularyViewActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final Cursor cur = (Cursor) adapter.getItem(position);
            categoryId = cur.getString(cur.getColumnIndexOrThrow("CATEGORY_ID"));

            if ( "R1,R2,R3".indexOf(daumKind) > -1 || DicDb.isExistDaumCategoryVocabulary(db, categoryId) ) {
                //layout 구성
                //메뉴 선택 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getVocabularyCategory(), null);
                final String[] kindCodes = new String[cursor.getCount()];
                final String[] kindCodeNames = new String[cursor.getCount()];

                int idx = 0;
                while (cursor.moveToNext()) {
                    if ( cursor.getString(cursor.getColumnIndexOrThrow("KIND")).equals(mCategoryKind) ) {
                        mSelect = idx;
                    }

                    kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                    kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                    idx++;
                }
                cursor.close();

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(DaumVocabularyActivity.this);
                dlg.setTitle("단어장 선택");
                dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSelect = arg1;
                    }
                });
                dlg.setNeutralButton("신규 단어장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final View dialog_layout = getLayoutInflater().inflate(R.layout.dialog_dic_category, null);

                        //dialog 생성..
                        AlertDialog.Builder builder = new AlertDialog.Builder(DaumVocabularyActivity.this);
                        builder.setView(dialog_layout);
                        final AlertDialog alertDialog = builder.create();

                        final EditText et_voc_name = ((EditText) dialog_layout.findViewById(R.id.my_et_voc_name));
                        et_voc_name.setText(cur.getString(cur.getColumnIndexOrThrow("CATEGORY_NAME")));

                        ((Button) dialog_layout.findViewById(R.id.my_b_download)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if ("".equals(et_voc_name.getText().toString())) {
                                    Toast.makeText(DaumVocabularyActivity.this, "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                                } else {
                                    alertDialog.dismiss();

                                    vocName = et_voc_name.getText().toString();

                                    String insCategoryCode = DicQuery.getInsCategoryCode(db);
                                    db.execSQL(DicQuery.getInsNewCategory(CommConstants.vocabularyCode, insCategoryCode, vocName));

                                    DicDb.insMyVocabularyFromDaumCategory(db, daumKind, insCategoryCode, categoryId);

                                    DicUtils.setDbChange(getApplicationContext()); //변경여부 체크

                                    Toast.makeText(getApplicationContext(), "단어장에 추가하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        ((Button) dialog_layout.findViewById(R.id.my_dc_b_close)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });

                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DicDb.insMyVocabularyFromDaumCategory(db, daumKind, kindCodes[mSelect], categoryId);

                        DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
                    }
                });
                dlg.show();
            } else {
                Toast.makeText(getApplicationContext(), "Daum 단어장과 동기화가 필요합니다.해당 단어장을 클릭해주세요.", Toast.LENGTH_SHORT).show();
            }

            return true;
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

    private class DicCategoryTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(DaumVocabularyActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HashMap hm = new HashMap();
            hm.put("TOEIC","1");
            hm.put("TOEFL","2");
            hm.put("TEPS","3");
            hm.put("수능영어","4");
            hm.put("NEAT/NEPT","5");
            hm.put("초중고영어","12");
            hm.put("회화","9");
            hm.put("기타","13");

            if ( hm.containsKey(daumKind) ) {
                ArrayList wordAl = DicUtils.gatherCategory(db, "http://wordbook.daum.net/open/wordbook/list.do?theme=" + hm.get(daumKind) + "&order=recent&dic_type=endic", daumKind);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            changeListView();

            super.onPostExecute(result);
        }
    }
}

class DaumVocabularyCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public DaumVocabularyCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_daum_vocabulary_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String categoryName = cursor.getString(cursor.getColumnIndexOrThrow("CATEGORY_NAME"));
        String kind = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
        String updDate = cursor.getString(cursor.getColumnIndexOrThrow("UPD_DATE"));
        if ( "R1,R2,R3".indexOf(DicUtils.getString(kind)) < 0 ) {
            categoryName += " [" + updDate + ", " + cursor.getString(cursor.getColumnIndexOrThrow("BOOKMARK_CNT")) + "]";
        }
        ((TextView) view.findViewById(R.id.my_c_dci_tv_category)).setText(categoryName);
        ((TextView) view.findViewById(R.id.my_c_dci_tv_cnt)).setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("WORD_CNT"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_c_dci_tv_category)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_c_dci_tv_cnt)).setTextSize(fontSize);
    }
}