package com.sleepingbear.envocabulary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class VocabularyViewActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private VocabularyViewCursorAdapter adapter;
    public int mSelect = 0;

    private String kind = "";
    private String mMemorization = "ALL";
    private int mOrder = -1;

    private boolean isChange = false;
    private boolean isAllCheck = false;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //메뉴 선택 다이얼로그 생성
                final String[] kindCodeNames = new String[3];

                int idx = 0;
                kindCodeNames[idx++] = "직접 추가";
                kindCodeNames[idx++] = "영한사전에서 추가";
                kindCodeNames[idx++] = "Daum 단어장에서 추가";

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(VocabularyViewActivity.this);
                dlg.setTitle("선택");
                dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSelect = arg1;
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ( mSelect == 0 ) {
                            Bundle bundle = new Bundle();
                            bundle.putString("MODE", "ADD");
                            bundle.putString("KIND", kind);

                            Intent intent = new Intent(getApplication(), VocabularyEditActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, CommConstants.a_vocabularyEdit);
                        } else if ( mSelect == 1 ) {
                            Bundle bundle = new Bundle();
                            bundle.putString("KIND", CommConstants.dictionaryKind_f);
                            bundle.putString("CATEGORY_KIND", kind);

                            Intent intent = new Intent(getApplication(), DictionaryActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, CommConstants.a_dictionary);
                        } else if ( mSelect == 2 ) {
                            Bundle bundle = new Bundle();
                            bundle.putString("CATEGORY_KIND", kind);

                            Intent intent = new Intent(getApplication(), DaumVocabularyActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, CommConstants.a_daum);
                        }
                        DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                        adapter.dataChange();
                    }
                });
                dlg.show();
            }
        });

        Bundle b = this.getIntent().getExtras();
        kind = b.getString("kind");

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setTitle(b.getString("kindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        ((RelativeLayout) this.findViewById(R.id.my_c_rl_tool)).setVisibility(View.GONE);

        ((RadioButton)this.findViewById(R.id.my_a_voc_rb_all)).setOnClickListener(this);
        ((RadioButton)this.findViewById(R.id.my_a_voc_rb_m)).setOnClickListener(this);
        ((RadioButton)this.findViewById(R.id.my_a_voc_rb_m_not)).setOnClickListener(this);

        ((CheckBox) findViewById(R.id.my_cb_detail)).setOnClickListener(this);

        ((ImageView)this.findViewById(R.id.my_iv_all)).setOnClickListener(this);
        ((ImageView)this.findViewById(R.id.my_iv_delete)).setOnClickListener(this);
        ((ImageView)this.findViewById(R.id.my_iv_copy)).setOnClickListener(this);
        ((ImageView)this.findViewById(R.id.my_iv_move)).setOnClickListener(this);

        Spinner spinner = (Spinner) this.findViewById(R.id.my_a_voc_s_ord);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.dicOrderValue, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mOrder = parent.getSelectedItemPosition();

                if ( mOrder == 6 ) {
                    Toast.makeText(getApplicationContext(), "Random으로 조회시 암기여부를 체크할때 정렬이 다시 되기 때문에 보여지는 것이 틀려집니다.", Toast.LENGTH_LONG).show();
                }

                getListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        spinner.setSelection(0);

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_dictionary :
                getListView();

                break;
            case CommConstants.a_daum :
                getListView();

                break;
        }
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SEQ _id," + CommConstants.sqlCR);
        sql.append("       SEQ," + CommConstants.sqlCR);
        sql.append("       KIND," + CommConstants.sqlCR);
        sql.append("       WORD," + CommConstants.sqlCR);
        sql.append("       MEAN," + CommConstants.sqlCR);
        sql.append("       SPELLING," + CommConstants.sqlCR);
        sql.append("       SAMPLES," + CommConstants.sqlCR);
        sql.append("       MEMO," + CommConstants.sqlCR);
        sql.append("       MEMORIZATION," + CommConstants.sqlCR);
        sql.append("       INS_DATE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_MY_VOC" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        if ( mMemorization.length() == 1 ) {
            sql.append("   AND MEMORIZATION = '" + mMemorization + "' " + CommConstants.sqlCR);
        }
        if ( mOrder == 0 ) {
            sql.append(" ORDER BY INS_DATE DESC, WORD" + CommConstants.sqlCR);
        } else if ( mOrder == 1 ) {
            sql.append(" ORDER BY WORD DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 2 ) {
            sql.append(" ORDER BY MEAN DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 3 ) {
            sql.append(" ORDER BY INS_DATE, WORD" + CommConstants.sqlCR);
        } else if ( mOrder == 4 ) {
            sql.append(" ORDER BY WORD" + CommConstants.sqlCR);
        } else if ( mOrder == 5 ) {
            sql.append(" ORDER BY MEAN" + CommConstants.sqlCR);
        } else if ( mOrder == 6 ) {
            sql.append(" ORDER BY RANDOM()" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);

        ListView listView = (ListView) this.findViewById(R.id.my_lv_list);
        adapter = new VocabularyViewCursorAdapter(getApplicationContext(), cursor, this, db, ((CheckBox) findViewById(R.id.my_cb_detail)).isChecked());
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);

        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ( isEditing == false ) {
                Cursor cur = (Cursor) adapter.getItem(position);
                cur.moveToPosition(position);

                final String entryId = DicDb.getEntryIdForWord(db, cur.getString(cur.getColumnIndexOrThrow("WORD")));
                if ( !"".equals(entryId) ) {
                    Intent intent = new Intent(getApplication(), WordViewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("entryId", entryId);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("MODE", "UPDATE");
            bundle.putString("SEQ", cur.getString(cur.getColumnIndexOrThrow("SEQ")));
            bundle.putString("KIND", cur.getString(cur.getColumnIndexOrThrow("KIND")));
            bundle.putString("WORD", cur.getString(cur.getColumnIndexOrThrow("WORD")));
            bundle.putString("MEAN", cur.getString(cur.getColumnIndexOrThrow("MEAN")));
            bundle.putString("SPELLING", cur.getString(cur.getColumnIndexOrThrow("SPELLING")));
            bundle.putString("SAMPLES", cur.getString(cur.getColumnIndexOrThrow("SAMPLES")));
            bundle.putString("MEMO", cur.getString(cur.getColumnIndexOrThrow("MEMO")));

            Intent intent = new Intent(getApplication(), VocabularyEditActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, CommConstants.a_vocabularyEdit);

            return true;
        }
    };

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_cb_detail ) {
            getListView();
        } else if (v.getId() == R.id.my_a_voc_rb_all) {
            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_voc_rb_m) {
            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_voc_rb_m_not) {
            mMemorization = "N";
            getListView();
        } else if (v.getId() == R.id.my_iv_all ) {
            if ( isAllCheck ) {
                isAllCheck = false;
            } else {
                isAllCheck = true;
            }
            adapter.allCheck(isAllCheck);
        } else if (v.getId() == R.id.my_iv_delete ) {
            if ( !adapter.isCheck() ) {
                Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.delete(kind);

                                isChange = true;
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        } else if (v.getId() == R.id.my_iv_copy) {
            if ( !adapter.isCheck() ) {
                Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                //메뉴 선택 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getVocabularyKindMeExceptContextMenu(kind), null);

                if ( cursor.getCount() == 0 ) {
                    Toast.makeText(this, "등록된 단어장이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    final String[] kindCodes = new String[cursor.getCount()];
                    final String[] kindCodeNames = new String[cursor.getCount()];

                    int idx = 0;
                    while (cursor.moveToNext()) {
                        kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                        kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                        idx++;
                    }
                    cursor.close();

                    final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(VocabularyViewActivity.this);
                    dlg.setTitle("단어장 선택");
                    dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mSelect = arg1;
                        }
                    });
                    dlg.setNegativeButton("취소", null);
                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.copy(kind, kindCodes[mSelect]);

                            isChange = true;
                        }
                    });
                    dlg.show();
                }
            }
        } else if (v.getId() == R.id.my_iv_move ) {
            if (!adapter.isCheck()) {
                Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
            } else {
                //메뉴 선택 다이얼로그 생성
                Cursor cursor = db.rawQuery(DicQuery.getVocabularyKindMeExceptContextMenu(kind), null);

                if (cursor.getCount() == 0) {
                    Toast.makeText(this, "등록된 단어장이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    final String[] kindCodes = new String[cursor.getCount()];
                    final String[] kindCodeNames = new String[cursor.getCount()];

                    int idx = 0;
                    while (cursor.moveToNext()) {
                        kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                        kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                        idx++;
                    }
                    cursor.close();

                    final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(VocabularyViewActivity.this);
                    dlg.setTitle("단어장 선택");
                    dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mSelect = arg1;
                        }
                    });
                    dlg.setNegativeButton("취소", null);
                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.move(kind, kindCodes[mSelect]);

                            isChange = true;
                        }
                    });
                    dlg.show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_vocabulary, menu);

        if (isEditing) {
            ((MenuItem) menu.findItem(R.id.action_edit)).setVisible(false);
            ((MenuItem) menu.findItem(R.id.action_exit)).setVisible(true);
        } else {
            ((MenuItem) menu.findItem(R.id.action_edit)).setVisible(true);
            ((MenuItem) menu.findItem(R.id.action_exit)).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_edit) {
            isEditing = true;
            invalidateOptionsMenu();

            ((RelativeLayout) this.findViewById(R.id.my_c_rl_tool)).setVisibility(View.VISIBLE);
            ((RelativeLayout) this.findViewById(R.id.my_c_rl_condi)).setVisibility(View.GONE);

            adapter.editChange(isEditing);
            adapter.notifyDataSetChanged();
        } else if (id == R.id.action_exit) {
            isEditing = false;
            invalidateOptionsMenu();

            ((RelativeLayout) this.findViewById(R.id.my_c_rl_tool)).setVisibility(View.GONE);
            ((RelativeLayout) this.findViewById(R.id.my_c_rl_condi)).setVisibility(View.VISIBLE);

            adapter.editChange(isEditing);
            adapter.notifyDataSetChanged();

            if ( adapter.isChange ) {
                DicUtils.setDbChange(getApplicationContext()); //변경여부 체크
            }
        } else if (id == R.id.action_tts) {
            Cursor cur = (Cursor) adapter.getItem(0);

            String[] words  = new String[cur.getCount()];
            String[] means  = new String[cur.getCount()];
            for ( int i = 0; i < cur.getCount(); i++ ) {
                cur.moveToPosition(i);
                words[i] = DicUtils.getString(cur.getString(cur.getColumnIndexOrThrow("WORD")));
                means[i] = DicUtils.getString(cur.getString(cur.getColumnIndexOrThrow("MEAN")));
            }

            Intent ttsIntent = new Intent(getApplicationContext(), MySpeechService.class);
            stopService(ttsIntent);

            ttsIntent = new Intent(getApplicationContext(), MySpeechService.class);
            ttsIntent.putExtra("words", words);
            ttsIntent.putExtra("means", means);
            startService(ttsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent ttsIntent = new Intent(getApplicationContext(), MySpeechService.class);
        stopService(ttsIntent);

        Intent intent = new Intent(this.getApplication(), VocabularyViewActivity.class);
        intent.putExtra("isChange", (isChange ? "Y" : "N"));
        setResult(RESULT_OK, intent);

        finish();
    }
}

class VocabularyViewCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    private SQLiteDatabase mDb;
    private Cursor mCursor;

    private boolean isEditing = false;
    private boolean[] isCheck;
    private String[] word;

    public boolean isChange = false;
    public boolean isDetail = false;

    static class ViewHolder {
        protected CheckBox memorizationCheck;
        protected String kind;
        protected String word;
        protected int position;
        protected CheckBox cb;
    }

    public VocabularyViewCursorAdapter(Context context, Cursor cursor, Activity activity, SQLiteDatabase db, boolean _isDetail) {
        super(context, cursor, 0);
        mCursor = cursor;
        mDb = db;
        isDetail = _isDetail;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );

        isCheck = new boolean[cursor.getCount()];
        word = new String[cursor.getCount()];
        while ( cursor.moveToNext() ) {
            isCheck[cursor.getPosition()] = false;
            word[cursor.getPosition()] = cursor.getString(cursor.getColumnIndexOrThrow("WORD"));
        }
        cursor.moveToFirst();
    }

    public void dataChange() {
        mCursor.requery();

        isCheck = new boolean[mCursor.getCount()];
        word = new String[mCursor.getCount()];

        if ( mCursor.getCount() > 0 ) {
            mCursor.moveToFirst();
            isCheck[mCursor.getPosition()] = false;
            word[mCursor.getPosition()] = mCursor.getString(mCursor.getColumnIndexOrThrow("WORD"));
            while (mCursor.moveToNext()) {
                isCheck[mCursor.getPosition()] = false;
                word[mCursor.getPosition()] = mCursor.getString(mCursor.getColumnIndexOrThrow("WORD"));
            }

            mCursor.move(mCursor.getPosition());
        }

        //변경사항을 반영한다.
        notifyDataSetChanged();
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_vocabulary_view_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        //암기 체크
        viewHolder.memorizationCheck = (CheckBox) view.findViewById(R.id.my_cb_memory_check);
        viewHolder.memorizationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder)v.getTag();

                DicDb.updMyVocabularyMemory(mDb, viewHolder.kind, viewHolder.word, (((CheckBox) v).isChecked() ? "Y" : "N"));

                dataChange();
            }
        });

        viewHolder.cb = (CheckBox) view.findViewById(R.id.my_cb_check);
        viewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                ViewHolder viewHolder = (ViewHolder)buttonView.getTag();
                isCheck[viewHolder.position] = isChecked;
                notifyDataSetChanged();

                DicUtils.dicLog("onCheckedChanged : " + viewHolder.position);
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.kind = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
        viewHolder.word = cursor.getString(cursor.getColumnIndexOrThrow("WORD"));
        viewHolder.position = cursor.getPosition();
        viewHolder.memorizationCheck.setTag(viewHolder);
        viewHolder.cb.setTag(viewHolder);

        ((TextView) view.findViewById(R.id.my_tv_word)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("WORD"))));
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING"))));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE"))));
        ((TextView) view.findViewById(R.id.my_tv_mean)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("MEAN"))));
        ((TextView) view.findViewById(R.id.my_tv_samples)).setText(DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SAMPLES"))).replaceAll(":","\n - "));
        ((TextView) view.findViewById(R.id.my_tv_memo)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEMO")));

        //암기 체크박스
        if ( "Y".equals(cursor.getString(cursor.getColumnIndexOrThrow("MEMORIZATION"))) ) {
            ((CheckBox)view.findViewById(R.id.my_cb_memory_check)).setChecked(true);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_memory_check)).setChecked(false);
        }

        if ( isEditing ) {
            ((RelativeLayout) view.findViewById(R.id.my_rl_left)).setVisibility(View.VISIBLE);
        } else {
            ((RelativeLayout) view.findViewById(R.id.my_rl_left)).setVisibility(View.GONE);
        }

        ((CheckBox)view.findViewById(R.id.my_cb_check)).setChecked(isCheck[cursor.getPosition()]);
        if ( isCheck[cursor.getPosition()] ) {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_on_background);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_word)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_mean)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_samples)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_memo)).setTextSize(fontSize);

        if ( isDetail ) {
            if (DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SAMPLES"))).equals("")) {
                ((TextView) view.findViewById(R.id.my_tv_samples)).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.my_tv_samples)).setVisibility(View.VISIBLE);
            }
            if (DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("MEMO"))).equals("")) {
                ((TextView) view.findViewById(R.id.my_tv_memo)).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.my_tv_memo)).setVisibility(View.VISIBLE);
            }
        } else {
            ((TextView) view.findViewById(R.id.my_tv_samples)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.my_tv_memo)).setVisibility(View.GONE);
        }
    }

    public void allCheck(boolean chk) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            isCheck[i] = chk;
        }

        notifyDataSetChanged();
    }

    public void delete(String kind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.delMyVocabulary(mDb, kind, word[i]);
            }
        }

        isChange = true;
        dataChange();
    }

    public void copy(String kind, String copyKind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.copyMyVocabulary(mDb, kind, copyKind, word[i]);
            }
        }

        isChange = true;
        dataChange();
    }

    public void move(String kind, String copyKind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.moveMyVocabulary(mDb, kind, copyKind, word[i]);
            }
        }

        isChange = true;
        dataChange();
    }

    public boolean isCheck() {
        boolean rtn = false;
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                rtn = true;
                break;
            }
        }

        return rtn;
    }

    public void editChange(boolean isEditing) {
        this.isEditing = isEditing;
        notifyDataSetChanged();
    }
}