package com.sleepingbear.envocabulary;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class HelpActivity extends AppCompatActivity {
    private int fontSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        //ab.setHomeButtonEnabled(true);
        //ab.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        StringBuffer allSb = new StringBuffer();
        StringBuffer CurrentSb = new StringBuffer();
        StringBuffer tempSb = new StringBuffer();

        String screen = b.getString("SCREEN");
        if ( screen == null ) {
            screen = "";
        }
        String kind = b.getString("KIND");
        if ( kind == null ) {
            kind = "";
        }

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 어플 사용팁" + CommConstants.sqlCR);
        tempSb.append("1. 하단 + 버튼으로 단어장을 추가하세요." + CommConstants.sqlCR);
        tempSb.append("2. 단어장으로 들어가서 하단 + 버튼으로 단어를 등록하세요." + CommConstants.sqlCR);
        tempSb.append("  - 직접 추가 : 내가 단어, 뜻, 스펠링, 예제, 메모등을 입력합니다." + CommConstants.sqlCR);
        tempSb.append("  - 엑셀파일에서 추가 : 엑셀에 단어, 뜻, 스펠링, 예제, 메모를 입력한후에 엑셀로 업로드 합니다." + CommConstants.sqlCR);
        tempSb.append("  - 영한사전에서 추가 : 사전에서 필요한 단어를 검색해서 입력을 합니다." + CommConstants.sqlCR);
        tempSb.append("  - Daum 단어장에서 추가 : Daum 사이트에 있는 단어장을 선택해서 등록합니다." + CommConstants.sqlCR);
        tempSb.append("  - 단어목록으로 추가(엑셀) : 엑셀에 단어를 입력한후에 엑셀로 업로드 합니다. 뜻, 스펠링은 사전에 있는 정보를 찾습니다." + CommConstants.sqlCR);
        tempSb.append("  - 단어목록으로 추가(직접입력) : 등록할 단어를 ','를 붙여서 넣어주면 사전에서 뜻, 스펠링을 찾아서 등록을 합니다." + CommConstants.sqlCR);
        tempSb.append("3. 단어장 상세에서 등록한 단어를 확인하고 학습해보세요." + CommConstants.sqlCR);
        tempSb.append("  - 메뉴의 '단어 예제 생성'으로 예제를 자동 등록한 후에 '상세' 체크박스를 클릭해서 단어와 예제를 같이 보면서 학습해보세요." + CommConstants.sqlCR);
        tempSb.append("4. 단어학습에서 6가지 방법으로 단어 학습을 해보세요." + CommConstants.sqlCR);
        tempSb.append("5. 내가 정리한 단어장을 엑셀로 받아서 친구들과 공유를 해보세요." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장" + CommConstants.sqlCR);
        tempSb.append("- 단어장 목록을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .하단의 + 버튼을 클릭해서 신규 단어장을 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .기존 단어장을 길게 클릭하시면 수정, 삭제,  내보내기, 가져오기를 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어장을 클릭하시면 등록된 단어를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어장 상세" + CommConstants.sqlCR);
        tempSb.append("- 단어 목록 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 수정 버튼능 클릭하면 단어장을 편집(삭제,복사,이동) 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상단 TTS 버튼을 클릭하면 단어,뜻을 들을 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .상세를 클릭하면 단어와 예제를 같이 보실 수 있습니다. 예제가 없을 경우 메뉴의'단어 예제 생성'으로 예제를 자동 등록할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .메뉴의 '전체 암기','전체 미암기'로 암기 여부를 변경할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 영한 사전" + CommConstants.sqlCR);
        tempSb.append(" .단어를 조회한 후에 단어를 길게 클릭해서 단어장에 단어를 등록할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .또는 Naver, Daum 웹으로 단어 검색을 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 Naver 사전, Daum 사전, 예문을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* Daum 단어장" + CommConstants.sqlCR);
        tempSb.append(" .Daum 사이트에 있는 단어장을 통해서 단어를 공부 할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .Toeic, Toefl, Teps...  카테고리를 선택한후에 단어장을 클릭하면 단어 목록을 볼 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .검색을 통해서 원하는 단어장을 찾을 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어장을 길게 클릭해서 기존 단어장이나 신규 단어장에 단어를 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("    - 단, 처음에는 단어장의 단어가 동기화 되어있지 않기 때문에 단어장을 클릭해서 단어를 동기화 해야 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* Daum 단어장 상세" + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭해서 기존 단어장에 단어를 추가할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단답 학습" + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 뜻을 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭해서 암기여부를 표시합니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 정답 보기/ 단어 보기/전체 정답 보기를 선택하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 학습입니다." + CommConstants.sqlCR);
        tempSb.append(" .하단 Play 버튼을 클릭하시면 영어를 보여주고 잠시후에 뜻이 보여집니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 OX 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 OX 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 학습" + CommConstants.sqlCR);
        tempSb.append("- 카드형 4지선다 학습입니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 카드형 4지선다 TTS 학습" + CommConstants.sqlCR);
        tempSb.append("- TTS를 이용하여 학습을 합니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 단어 상세" + CommConstants.sqlCR);
        tempSb.append("- 상단 콤보 메뉴를 선택하시면 네이버 사전, 다음 사전, 예제를 보실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        tempSb.delete(0, tempSb.length());
        tempSb.append("* 문장 상세" + CommConstants.sqlCR);
        tempSb.append("- 문장의 발음 및 관련 단어를 조회하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 클릭하시면 단어 보기 및 등록할 단어장을 선택 하실 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .단어를 길게 클릭하시면 등록할 단어장을 선택할 수 있습니다." + CommConstants.sqlCR);
        tempSb.append(" .별표를 클릭하시면 Default 단어장에 추가 됩니다." + CommConstants.sqlCR);
        tempSb.append("" + CommConstants.sqlCR);
        allSb.append(tempSb.toString());

        ((TextView) this.findViewById(R.id.my_c_help_tv1)).setText(allSb.toString());

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        ((TextView) this.findViewById(R.id.my_c_help_tv1)).setTextSize(fontSize);

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
