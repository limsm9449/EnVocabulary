package com.sleepingbear.envocabulary;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MainCursorAdapter adapter;


    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View dialog_layout = inflater.inflate(R.layout.dialog_category_add, null);

                //dialog 생성..
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(dialog_layout);
                final AlertDialog alertDialog = builder.create();

                ((TextView) dialog_layout.findViewById(R.id.my_d_category_add_tv_title)).setText("단어장 추가");
                final EditText et_ins = ((EditText) dialog_layout.findViewById(R.id.my_d_category_add_et_ins));
                ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_ins)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if ("".equals(et_ins.getText().toString())) {
                            Toast.makeText(getApplication(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();

                            String insCategoryCode = DicQuery.getInsCategoryCode(db);
                            db.execSQL(DicQuery.getInsNewCategory(CommConstants.vocabularyCode, insCategoryCode, et_ins.getText().toString()));

                            changeListView();

                            DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                            Toast.makeText(getApplicationContext(), "단어장에 추가하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_close)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        System.out.println("=============================================== App Start ======================================================================");
        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //DB가 새로 생성이 되었으면 이전 데이타를 DB에 넣고 Flag를 N 처리함
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if ( "Y".equals(prefs.getString("db_new", "N")) ) {
            DicUtils.dicLog("backup data import");

            DicUtils.readExcelBackup(this, db, null);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("db_new", "N");
            editor.commit();
            Toast.makeText(getApplicationContext(), "DB 변경되었습니다.\n기존 데이타를 복구하였습니다.", Toast.LENGTH_LONG).show();
        }

        checkPermission();

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);

        changeListView();

        // 사용법 Dialog...
        String appHintDialog = "20170813";
        int appHintDialogCount = prefs.getInt("appHintDialogCount", 3);
        //DicUtils.dicLog("appHintDialogCount : " + appHintDialogCount);
        boolean showDialog = false;
        if ( !appHintDialog.equals(prefs.getString("appHintDialog", "N")) ) {
            showDialog = true;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("appHintDialog", appHintDialog);
            editor.putInt("appHintDialogCount", 2);
            appHintDialogCount = 2;
            editor.commit();
        } else if ( appHintDialogCount == 1 || appHintDialogCount == 2 || appHintDialogCount == 3 ) {
            showDialog = true;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("appHintDialog", appHintDialog);
            editor.putInt("appHintDialogCount", --appHintDialogCount);
            editor.commit();
        }
        if ( showDialog ) {
            String msg = "1. 학습할 단어를 엑셀로 정리하여 단어장으로 등록합니다..\n";
            msg += "2. Daum 단어장에서 학습할 단어장을 다운로드하여 등록합니다.\n";
            msg += "3. 단어학습에서 6가지 모드로 단어를 학습할 수 있습니다.\n";

            new AlertDialog.Builder(this)
                    .setTitle("알림" + (appHintDialogCount >= 0 ? " - " + ++appHintDialogCount : ""))
                    .setMessage(msg)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    public boolean checkPermission() {
        Log.d(CommConstants.tag, "checkPermission");
        boolean isCheck = false;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            Log.d(CommConstants.tag, "권한 없음");
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                //Toast.makeText(this, "(중요)파일로 내보내기, 가져오기를 하기 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            Log.d(CommConstants.tag, "2222");
        } else {
            Log.d(CommConstants.tag, "권한 있음");
            isCheck = true;
        }

        return isCheck;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(CommConstants.tag, "권한 허가");
                } else {
                    Log.d(CommConstants.tag, "권한 거부");
                    Toast.makeText(this, "파일 권한이 없기 때문에 파일 내보내기, 가져오기를 할 수 없습니다.\n만일 권한 팝업이 안열리면 '다시 묻지 않기'를 선택하셨기 때문입니다.\n어플을 지우고 다시 설치하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            //} else {
            //    super.onBackPressed();
        }

        //종료 시점에 변경 사항을 기록한다.
        if ( "Y".equals(DicUtils.getDbChange(getApplicationContext())) ) {
            DicUtils.writeExcelBackup(this, db, "");
            DicUtils.clearDbChange(this);
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();

            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_dictionary) {
            Bundle bundle = new Bundle();
            bundle.putString("KIND", CommConstants.dictionaryKind_f);

            Intent dictionaryIntent = new Intent(getApplication(), DictionaryActivity.class);
            dictionaryIntent.putExtras(bundle);
            startActivityForResult(dictionaryIntent, CommConstants.a_dictionary);
        } else if (id == R.id.nav_daum) {
            startActivityForResult(new Intent(getApplication(), DaumVocabularyActivity.class), CommConstants.a_daum);
        } else if (id == R.id.nav_study) {
            startActivity(new Intent(getApplication(), StudyActivity.class));
        } else if (id == R.id.nav_setting) {
            startActivityForResult(new Intent(getApplication(), SettingsActivity.class), CommConstants.a_setting);
        } else if (id == R.id.nav_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "최고의 영어 단어장 어플");
            msg.putExtra(Intent.EXTRA_TEXT, "영어.. 참 어렵죠? '최고의 영어 단어장' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.envocabulary");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));
        } else if (id == R.id.nav_review) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } else if (id == R.id.nav_other_app) {
            String url ="http://blog.naver.com/limsm9449/221031416154";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (id == R.id.nav_mail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            intent.putExtra(Intent.EXTRA_TEXT, "어플관련 문제점을 적어 주세요.\n빠른 시간 안에 수정을 하겠습니다.\n감사합니다.");
            intent.setData(Uri.parse("mailto:limsm9449@gmail.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_vocabulary :
                changeListView();

                break;
            case CommConstants.a_dictionary :
                changeListView();

                break;
            case CommConstants.a_daum :
                changeListView();

                break;
            case CommConstants.a_setting :
                changeListView();

                break;
        }
    }

    public void changeListView() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        Cursor cursor = db.rawQuery(DicQuery.getMyVocabularyCategoryCount(), null);

        ListView listView = (ListView) findViewById(R.id.my_a_vocabulary_note_lv);
        adapter = new MainCursorAdapter(this, cursor, 0);
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
            bundle.putString("kind", cur.getString(cur.getColumnIndexOrThrow("KIND")));
            bundle.putString("kindName", cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

            Intent intent = new Intent(getApplication(), VocabularyViewActivity.class);
            intent.putExtras(bundle);

            startActivityForResult(intent, CommConstants.a_vocabulary);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final Cursor cur = (Cursor) adapter.getItem(position);

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            //layout 구성
            final View dialog_layout = inflater.inflate(R.layout.dialog_category_iud, null);

            final String kind = cur.getString(cur.getColumnIndexOrThrow("KIND"));

            //dialog 생성..
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setView(dialog_layout);
            final AlertDialog alertDialog = builder.create();

            ((TextView) dialog_layout.findViewById(R.id.my_d_category_tv_category)).setText("단어장 관리");

            final EditText et_upd = ((EditText) dialog_layout.findViewById(R.id.my_et_upd));
            et_upd.setText(cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

            ((Button) dialog_layout.findViewById(R.id.my_b_upd)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("VOC0001".equals(kind)) {
                        Toast.makeText(getApplicationContext(), "기본 단어장은 수정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        if ("".equals(et_upd.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();

                            db.execSQL(DicQuery.getUpdCategory(CommConstants.vocabularyCode, kind, et_upd.getText().toString()));
                            DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                            changeListView();

                            Toast.makeText(getApplicationContext(), "단어장 이름을 수정하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_b_del)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ("VOC0001".equals(kind)) {
                        Toast.makeText(getApplicationContext(), "기본 단어장은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("알림")
                                .setMessage("삭제된 데이타는 복구할 수 없습니다. 삭제하시겠습니까?")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();

                                        DicDb.delCategory(db, CommConstants.vocabularyCode, kind);
                                        DicDb.delMyVocabularyAll(db, kind);

                                        DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                                        changeListView();

                                        Toast.makeText(getApplicationContext(), "단어장을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
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
            });

            final EditText et_saveName = ((EditText) dialog_layout.findViewById(R.id.my_et_voc_name));
            et_saveName.setText(cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")) + ".xls");
            ((Button) dialog_layout.findViewById(R.id.my_b_download)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String saveFileName = et_saveName.getText().toString();
                    if ("".equals(saveFileName)) {
                        Toast.makeText(getApplicationContext(), "저장할 파일명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else if (saveFileName.indexOf(".") > -1 && !"xls".equals(saveFileName.substring(saveFileName.length() - 3, saveFileName.length()).toLowerCase())) {
                        Toast.makeText(getApplicationContext(), "확장자는 xls 입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        String fileName = DicUtils.getFileName(saveFileName, "xls");

                        Cursor cursor = db.rawQuery(DicQuery.getSaveMyVocabulary(kind), null);
                        boolean isSave = DicUtils.writeExcelVocabulary(fileName, cursor);
                        if ( isSave ) {
                            Toast.makeText(getApplicationContext(), "단어장을 정상적으로 내보냈습니다.", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        }
                    }
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_b_upload)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileChooser filechooser = new FileChooser(MainActivity.this);
                    filechooser.setFileListener(new FileChooser.FileSelectedListener() {
                        @Override
                        public void fileSelected(final File file) {
                            boolean isSave = DicUtils.readExcelVocabulary(db, file, kind, false);
                            if ( isSave ) {
                                Toast.makeText(getApplicationContext(), "단어장을 정상적으로 가져왔습니다.", Toast.LENGTH_SHORT).show();
                                alertDialog.dismiss();
                                changeListView();
                            }
                        }
                    });
                    filechooser.setExtension("xls");
                    filechooser.showDialog();
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_b_close)).setOnClickListener(new View.OnClickListener() {
                                                                                                     @Override
                                                                                                     public void onClick(View v) {
                                                                                                         alertDialog.dismiss();
                                                                                                     }
                                                                                                 }
            );

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            return true;
        }
    };
}



class MainCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public MainCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_main_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_category)).setText(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")));
        ((TextView) view.findViewById(R.id.my_tv_cnt)).setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("CNT"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_category)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_cnt)).setTextSize(fontSize);
    }
}
