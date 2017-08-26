package com.sleepingbear.envocabulary;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Study3Activity extends AppCompatActivity implements View.OnClickListener {
    private String mVocKind;
    private String mMemorization;
    private String mSort = "QUESTION ASC";
    private boolean mIsPlay = false;

    private String mWordMean = "WORD";

    private DbHelper mDbbHelper;
    private SQLiteDatabase mDb;
    private Study2CursorAdapter adapter;

    private Cursor mCursor;
    private TextView tv_question;
    private TextView tv_spelling;
    private TextView tv_answer;
    private TextView tv_pos;
    private TextView tv_total;
    private SeekBar sb;

    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study3);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        mDbbHelper = new DbHelper(this);
        mDb = mDbbHelper.getWritableDatabase();

        Bundle b = this.getIntent().getExtras();
        mVocKind = b.getString("vocKind");
        mMemorization = b.getString("memorization");
        mWordMean = "WORD";

        ActionBar ab = getSupportActionBar();
        ab.setTitle(b.getString("studyKindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.my_a_study3_rb_all).setOnClickListener(this);
        findViewById(R.id.my_a_study3_rb_m).setOnClickListener(this);
        findViewById(R.id.my_a_study3_rb_m_not).setOnClickListener(this);
        findViewById(R.id.my_a_study3_rb_word).setOnClickListener(this);
        findViewById(R.id.my_a_study3_rb_mean).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_asc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_desc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_random).setOnClickListener(this);
        findViewById(R.id.my_a_study3_ib_first).setOnClickListener(this);
        findViewById(R.id.my_a_study3_ib_prev).setOnClickListener(this);
        findViewById(R.id.my_a_study3_ib_play).setOnClickListener(this);
        findViewById(R.id.my_a_study3_ib_next).setOnClickListener(this);
        findViewById(R.id.my_a_study3_ib_last).setOnClickListener(this);

        tv_question = (TextView) findViewById(R.id.my_a_study3_tv_question);
        tv_question.setText("");
        tv_spelling = (TextView) findViewById(R.id.my_a_study3_tv_spelling);
        tv_spelling.setText("");
        tv_answer = (TextView) findViewById(R.id.my_a_study3_tv_answer);
        tv_answer.setText("");
        tv_pos = (TextView) findViewById(R.id.my_a_study3_tv_pos);
        tv_pos.setText("0");
        tv_total = (TextView) findViewById(R.id.my_a_study3_tv_total);
        tv_total.setText("0");

        if ( "".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study3_rb_all)).setChecked(true);
        } else if ( "Y".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study3_rb_m)).setChecked(true);
        } else if ( "N".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study3_rb_m_not)).setChecked(true);
        }

        //UI 수정
        int fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        tv_question.setTextSize(fontSize);
        tv_spelling.setTextSize(fontSize);
        tv_answer.setTextSize(fontSize);

        sb = (SeekBar) findViewById(R.id.my_a_study3_sb);
        sb.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                  if ( progress < mCursor.getCount() ) {
                      mCursor.moveToPosition(progress);
                      tv_question.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("QUESTION")));
                      if ( "WORD".equals(mWordMean) ) {
                          tv_spelling.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("SPELLING")));
                          tv_answer.setText("");
                      } else {
                          tv_spelling.setText("");
                          tv_answer.setText("");
                      }

                      tv_pos.setText(Integer.toString(progress + 1));
                  }
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar) {
                  mThread.interrupt();
              }

              @Override
              public void onStopTrackingTouch(SeekBar seekBar) {
                  studyPlay();
              }
          }
        );

        getListView();

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_a_study3_rb_all) {
            mThread.interrupt();

            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_study3_rb_m) {
            mThread.interrupt();

            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_study3_rb_m_not) {
            mThread.interrupt();

            mMemorization = "N";
            getListView();
        } else if (v.getId() == R.id.my_a_study3_rb_word) {
            mThread.interrupt();

            mWordMean = "WORD";
            getListView();
        } else if (v.getId() == R.id.my_a_study3_rb_mean) {
            mThread.interrupt();

            mWordMean = "MEAN";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_asc) {
            mThread.interrupt();

            mSort = "QUESTION ASC";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_desc) {
            mThread.interrupt();

            mSort = "QUESTION DESC";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_random) {
            mThread.interrupt();

            mSort = "RANDOM_SEQ";
            mDb.execSQL(DicQuery.updMyVocabularyRandom(mVocKind));
            getListView();
        } else if (v.getId() == R.id.my_a_study3_ib_first) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            mThread.interrupt();

            mCursor.moveToFirst();
            studyPlay();
        } else if (v.getId() == R.id.my_a_study3_ib_prev) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( !mCursor.isFirst() ) {
                mThread.interrupt();

                mCursor.moveToPrevious();
                studyPlay();
            }
        } else if (v.getId() == R.id.my_a_study3_ib_play) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( mIsPlay ) {
                mIsPlay = false;
                ((ImageButton) findViewById(R.id.my_a_study3_ib_play)).setImageResource(android.R.drawable.ic_media_play);
            } else {
                mIsPlay = true;
                ((ImageButton) findViewById(R.id.my_a_study3_ib_play)).setImageResource(android.R.drawable.ic_media_pause);

                mThread.interrupt();

                studyPlay();
            }
        } else if (v.getId() == R.id.my_a_study3_ib_next) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( !mCursor.isLast() ) {
                mThread.interrupt();

                mCursor.moveToNext();
                studyPlay();
            }
        } else if (v.getId() == R.id.my_a_study3_ib_last) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            mThread.interrupt();

            mCursor.moveToLast();
            studyPlay();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if ( mThread != null ) {
                mThread.interrupt();
            }
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if ( mThread != null ) {
            mThread.interrupt();
        }
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id," + CommConstants.sqlCR);
        sql.append("       SEQ," + CommConstants.sqlCR);
        if ( "WORD".equals(mWordMean) ) {
            sql.append("       WORD QUESTION," + CommConstants.sqlCR);
            sql.append("       MEAN ANSWER," + CommConstants.sqlCR);
        } else {
            sql.append("       WORD ANSWER," + CommConstants.sqlCR);
            sql.append("       MEAN QUESTION," + CommConstants.sqlCR);
        }
        sql.append("       KIND," + CommConstants.sqlCR);
        sql.append("       WORD," + CommConstants.sqlCR);
        sql.append("       SPELLING," + CommConstants.sqlCR);
        sql.append("       MEMORIZATION" + CommConstants.sqlCR);
        sql.append("  FROM DIC_MY_VOC" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + mVocKind + "' " + CommConstants.sqlCR);
        if (mMemorization.length() == 1) {
            sql.append("   AND MEMORIZATION = '" + mMemorization + "' " + CommConstants.sqlCR);
        }
        sql.append(" ORDER BY " + mSort + CommConstants.sqlCR);
        mCursor = mDb.rawQuery(sql.toString(), null);
        if ( mCursor.moveToNext() ) {
            sb.setMax(mCursor.getCount() - 1);
            sb.setProgress(mCursor.getPosition());
            tv_total.setText(Integer.toString(mCursor.getCount()));

            mIsPlay = false;
            ((ImageButton) findViewById(R.id.my_a_study3_ib_play)).setImageResource(android.R.drawable.ic_media_play);

            studyPlay();
        } else {
            /*sb.setMax(0);
            sb.setProgress(0);
            tv_pos.setText("0");
            tv_total.setText("0");

            tv_question.setText("");
            tv_spelling.setText("");
            tv_answer.setText("");*/

            new android.app.AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("데이타가 없습니다.\n암기 여부, 일자 조건을 조정해 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //DicUtils.dicLog("Handler : " + msg.arg1 + " : " + mCursor.getPosition());
            if ( msg.arg1 == 0 ) {
                tv_question.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("QUESTION")));
                if ( "WORD".equals(mWordMean) ) {
                    tv_spelling.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("SPELLING")));
                    tv_answer.setText("");
                } else {
                    tv_spelling.setText("");
                    tv_answer.setText("");
                }
            } else if ( msg.arg1 == 1 ) {
                if ( "WORD".equals(mWordMean) ) {
                    tv_spelling.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("SPELLING")));
                    tv_answer.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER")).replaceAll("1.", "\n1.").replaceAll("2.","\n2.").replaceAll("3.","\n3.").replaceAll("4.","\n4.").replaceAll("5.","\n5."));
                } else {
                    tv_spelling.setText("");
                    tv_answer.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER")).replaceAll("1.", "\n1.").replaceAll("2.","\n2.").replaceAll("3.","\n3.").replaceAll("4.","\n4.").replaceAll("5.","\n5."));
                }
            } else if ( msg.arg1 == 2 ) {
                if ( !mCursor.isLast() ) {
                    mCursor.moveToNext();

                    sb.setProgress(mCursor.getPosition());
                } else {
                    mIsPlay = false;
                    ((ImageButton) findViewById(R.id.my_a_study3_ib_play)).setImageResource(android.R.drawable.ic_media_play);
                }
            }
        }
    };

    public void studyPlay() {

        sb.setProgress(mCursor.getPosition());
        //DicUtils.dicLog("studyPlay : " + mCursor.getPosition());

        mThread = new Thread(new Runnable() {
            public void run() {
                try {
                    int step = 0;
                    while (mIsPlay || step != 3) {
                        if (step == 0) {
                            Message msg = handler.obtainMessage();
                            msg.arg1 = step;
                            handler.sendMessage(msg);

                            step = 1;

                            Thread.sleep(2000);
                        } else if (step == 1) {
                            Message msg = handler.obtainMessage();
                            msg.arg1 = step;
                            handler.sendMessage(msg);

                            step = 2;

                            Thread.sleep(2000);
                        } else if (step == 2) {
                            if (mIsPlay) {
                                Message msg = handler.obtainMessage();
                                msg.arg1 = step;
                                handler.sendMessage(msg);

                                step = 0;

                                Thread.sleep(1000);
                            } else {
                                step = 3;
                            }
                        }
                    }

                    Log.d("aaa", "Thread stop");
                } catch ( InterruptedException e ) {
                    //interrupt 시 Thread 종료..
                } finally {
                    //DicUtils.dicLog("Thread InterruptedException Close");
                }
            }
        });
        mThread.start();
    }
}

