package com.sleepingbear.envocabulary;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class DicUtils {
    public static String getString(String str) {
        if (str == null)
            return "";
        else
            return str.trim();
    }
    public static String getString(HSSFCell cell) {
        if (cell == null)
            return "";
        else
            return cell.toString().trim();
    }



    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return year + "" + (month + 1 > 9 ? "" : "0") + (month + 1) + "" + (day > 9 ? "" : "0") + day;
    }

    public static String getAddDay(String date, int addDay) {
        String mDate = date.replaceAll("[.-/]", "");

        int year = Integer.parseInt(mDate.substring(0, 4));
        int month = Integer.parseInt(mDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(mDate.substring(6, 8));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day + addDay);

        return c.get(Calendar.YEAR) + "" + (c.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (c.get(Calendar.MONTH) + 1) + "" + (c.get(Calendar.DAY_OF_MONTH) > 9 ? "" : "0") + c.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDelimiterDate(String date, String delimiter) {
        if (getString(date).length() < 8) {
            return "";
        } else {
            return date.substring(0, 4) + delimiter + date.substring(4, 6) + delimiter + date.substring(6, 8);
        }
    }

    public static String getYear(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(0, 4);
        }
    }

    public static String getMonth(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(4, 6);
        }
    }

    public static String getDay(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(6, 8);
        }
    }

    public static void dicSqlLog(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(CommConstants.tag + " ====>", str);
        }
    }

    public static void dicLog(String str) {
        if (BuildConfig.DEBUG) {
            Calendar cal = Calendar.getInstance();
            String time = cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초";

            Log.d(CommConstants.tag + " ====>", time + " : " + str);
        }
    }

    public static String lpadding(String str, int length, String fillStr) {
        String rtn = "";

        for (int i = 0; i < length - str.length(); i++) {
            rtn += fillStr;
        }
        return rtn + (str == null ? "" : str);
    }

    public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        if ( sentence != null ) {
            String tmpSentence = sentence + " ";

            int startPos = 0;
            for (int i = 0; i < tmpSentence.length(); i++) {
                if (CommConstants.sentenceSplitStr.indexOf(tmpSentence.substring(i, i + 1)) > -1) {
                    if (i == 0) {
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    } else {
                        if (i != startPos) {
                            al.add(tmpSentence.substring(startPos, i));
                        }
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    }
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }

    public static String getSentenceWord(String[] sentence, int kind, int position) {
        String rtn = "";
        if ( kind == 1 ) {
            rtn = sentence[position];
        } else if ( kind == 2 ) {
            if ( position + 2 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2];
                }
            }
        } else if ( kind == 3 ) {
            if ( position + 4 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) && " ".equals(sentence[position + 3]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2] + sentence[position + 3] + sentence[position + 4];
                }
            }
        }

        //dicLog(rtn);
        return rtn;
    }

    public static String getOneSpelling(String spelling) {
        String rtn = "";
        String[] str = spelling.split(",");
        if ( str.length == 1 ) {
            rtn = spelling;
        } else {
            rtn = str[0] + "(" + str[1] + ")";
        }

        return rtn;
    }

    public static void readInfoFromFile(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile start, " + fileName);

        //데이타 복구
        FileInputStream fis = null;
        try {
            //데이타 초기화
            DicDb.initMyVocabulary(db);

            if ( "".equals(fileName) ) {
                fis = ctx.openFileInput(CommConstants.infoFileName);
            } else {
                fis = new FileInputStream(new File(fileName));
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(isr);

            //출력...
            String readString = buffreader.readLine();
            while (readString != null) {
                dicLog(readString);

                String[] row = readString.split("[/^]");
                if ( row[0].equals(CommConstants.tag_code_ins) ) {
                    DicDb.insCode(db, row[1], row[2], row[3]);
                } else if ( row[0].equals(CommConstants.tag_voc_ins) ) {
                    //DicDb.insMyVocabulary(db, row[1], row[2], row[3], row[4]);
                }

                readString = buffreader.readLine();
            }

            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile end");
    }

    /**
     * 데이타 기록
     * @param ctx
     * @param db
     */
    public static void writeInfoToFile(Context ctx, SQLiteDatabase db, String fileName) {
        System.out.println("writeNewInfoToFile start");

        try {
            FileOutputStream fos = null;

            if ( "".equals(fileName) ) {
                fos = ctx.openFileOutput(CommConstants.infoFileName, Context.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            Cursor cursor = db.rawQuery(DicQuery.getWriteData(), null);
            while (cursor.moveToNext()) {
                String writeData = cursor.getString(cursor.getColumnIndexOrThrow("WRITE_DATA"));
                DicUtils.dicLog(writeData);
                if ( writeData != null ) {
                    fos.write((writeData.getBytes()));
                    fos.write("\n".getBytes());
                }
            }
            cursor.close();

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }

        System.out.println("writeNewInfoToFile end");
    }

    public static boolean isHangule(String pStr) {
        boolean isHangule = false;
        String str = (pStr == null ? "" : pStr);
        try {
            isHangule = str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }

        return isHangule;
    }

    public static Document getDocument(String url) throws Exception {
        Document doc = null;
        //while (true) {
        //    try {
                doc = Jsoup.connect(url).timeout(60000).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US;   rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").get();
        //        break;
        //    } catch (Exception e) {
        //        System.out.println(e.getMessage());
        //    }
        //}

        return doc;
    }

    public static Element findElementSelect(Document doc, String tag, String attr, String value) throws Exception {
        Elements es = doc.select(tag);
        for (Element es_r : es) {
            if (value.equals(es_r.attr(attr))) {
                return es_r;
            }
        }

        return null;
    }

    public static Element findElementForTag(Element e, String tag, int findIdx) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i);
                } else {
                    idx++;
                }
            }
        }

        return null;
    }

    public static Element findElementForTagAttr(Element e, String tag, String attr, String value) throws Exception {
        if (e == null) {
            return null;
        }

        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName()) && value.equals(e.child(i).attr(attr))) {
                return e.child(i);
            }
        }

        return null;
    }

    public static String getAttrForTagIdx(Element e, String tag, int findIdx, String attr) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i).attr(attr);
                } else {
                    idx++;
                }
            }
        }

        return "";
    }

    public static String getElementText(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.text();
        }
    }

    public static String getElementHtml(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.html();
        }
    }

    public static String getUrlParamValue(String url, String param) throws Exception {
        String rtn = "";

        if (url.indexOf("?") < 0) {
            return "";
        }
        String[] split_url = url.split("[?]");
        String[] split_param = split_url[1].split("[&]");
        for (int i = 0; i < split_param.length; i++) {
            String[] split_row = split_param[i].split("[=]");
            if (param.equals(split_row[0])) {
                rtn = split_row[1];
            }
        }

        return rtn;
    }

    public static Boolean isNetWork(AppCompatActivity context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        return (isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect);
    }

    public static String getBtnString(String word){
        String rtn = "";

        if ( word.length() == 1 ) {
            rtn = "  " + word + "  ";
        } else if ( word.length() == 2 ) {
            rtn = "  " + word + " ";
        } else if ( word.length() == 3 ) {
            rtn = " " + word + " ";
        } else if ( word.length() == 4 ) {
            rtn = " " + word;
        } else {
            rtn = " " + word + " ";
        }

        return rtn;
    }

    public static void setDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "Y");
        editor.commit();

        dicLog(DicUtils.class.toString() + " setDbChange : " + "Y");
    }

    public static String getDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(CommConstants.flag_dbChange, "N");
    }

    public static void clearDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "N");
        editor.commit();
    }

    public static String getPreferencesValue(Context context, String preference) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String rtn = sharedPref.getString( preference, "" );
        if ( "".equals( rtn ) ) {
            if ( preference.equals(CommConstants.preferences_font) ) {
                rtn = "17";
            } else if ( preference.equals(CommConstants.preferences_wordView) ) {
                rtn = "0";
            } else if ( preference.equals(CommConstants.preferences_webViewFont) ) {
                rtn = "3";
            } else {
                rtn = "";
            }
        }

        DicUtils.dicLog(rtn);

        return rtn;
    }

    public static ArrayList gatherCategory(SQLiteDatabase db, String url, String codeGroup) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            boolean isBreak = false;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Element table_e = findElementSelect(doc, "table", "class", "tbl_wordbook");
                Element tbody_e = findElementForTag(table_e, "tbody", 0);
                for (int m = 0; m < tbody_e.children().size(); m++) {
                    HashMap row = new HashMap();

                    Element category = findElementForTag(tbody_e.child(m), "td", 1);

                    String categoryId = getUrlParamValue(category.child(0).attr("href"), "id").replace("\n", "");
                    String categoryName = category.text();
                    String wordCnt = findElementForTag(tbody_e.child(m), "td", 3).text();
                    String bookmarkCnt = findElementForTag(tbody_e.child(m), "td", 4).text();
                    String updDate = findElementForTag(tbody_e.child(m), "td", 5).text();
                    dicLog(codeGroup + " : " + categoryName + " : " + categoryId + " : " + categoryName + " : " + wordCnt + " : " + bookmarkCnt + " : " + updDate) ;
                    Cursor cursor = db.rawQuery(DicQuery.getDaumCategory(categoryId), null);
                    if (cursor.moveToNext()) {
                        if ( categoryId.equals(cursor.getString(cursor.getColumnIndexOrThrow("CATEGORY_ID"))) && updDate.equals(cursor.getString(cursor.getColumnIndexOrThrow("UPD_DATE"))) ) {
                            isBreak = true;
                            break;
                        } else {
                            //수정
                            DicDb.updDaumCategoryInfo(db, categoryId, categoryName, updDate, bookmarkCnt);
                        }
                    } else {
                        //입력
                        DicDb.insDaumCategoryInfo(db, codeGroup, categoryId, categoryName, updDate, wordCnt, bookmarkCnt);
                    }
                }

                if ( isBreak ) {
                    break;
                }

                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    dicLog("cnt : " + cnt);
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static ArrayList gatherCategoryWord(String url) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Elements es = doc.select("div.wrap_word");
                for (int i = 0; i < es.size(); i++) {
                    HashMap row = new HashMap();

                    row.put("WORD", es.get(i).select("div.txt_word div.f_l a.link_wordbook").text().replaceAll("'","''"));
                    row.put("SPELLING", es.get(i).select("div.txt_word div.f_l span.pron_wordbook").text().replaceAll("'","''"));
                    row.put("MEAN", es.get(i).select("div.mean_info p span.link_mean").text().replaceAll("'","''"));

                    String samples = "";
                    Elements ses = es.get(i).select("div.mean_info div.desc_example");
                    for (int si = 0; si < ses.size(); si++) {
                        samples += ( "".equals(samples) ? "" : "\n" ) + ses.get(si).select("em").text() + ":" + ses.get(si).select("p").text();
                    }
                    row.put("SAMPLES", samples.replaceAll("'","''"));
                    row.put("MEMO", es.get(i).select("div.mean_info div.wrap_memo p.txt_memo").text().replaceAll("'","''"));

                    wordAl.add(row);
                }

                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static void getNovelList0(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList1(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.titlelist li");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).child(0).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList2(SQLiteDatabase db, String url, String kind) {
        dicLog("getNovelList2 : " + url);
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li.menu-li-bottom p.paginate-bar");
            String pageStr = es.get(0).text().trim().replaceAll("Page ","").replaceAll("of ","").split(" ")[1];
            int page = Integer.parseInt(pageStr);

            ArrayList al = new ArrayList();
            for ( int i = 1; i <= page; i++ ) {
                String pageUrl = url;
                if ( i > 1 ) {
                    doc = getDocument(url + "&page=" + i);
                }
                Elements es2 = doc.select("li.list-li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a.list-link");
                    Elements esImg = es2.get(m).select("img");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", esImg.get(0).attr("alt"));
                        al.add(hm);
                    }
                }
                es2 = doc.select("ul#s-list-ul li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", es2.get(m).text().replaceAll("[:]", ""));
                        al.add(hm);
                    }
                }
            }

            DicDb.delNovel(db, kind);

            for (int i = 0; i < al.size(); i++) {
                DicDb.insNovel(db, kind, (String)((HashMap)al.get(i)).get("title"), (String)((HashMap)al.get(i)).get("url"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static int getNovelPartCount0(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static int getNovelPartCount1(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.chapter-list li");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static String getNovelContent0(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td font");
            rtn = contents.get(1).html().replaceAll("<br /> <br />", "\n").replaceAll("&quot;","\"").replaceAll("<br />","");
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent1(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td.chapter-text span.chapter-heading");
            if ( contents.size() > 0 ) {
                rtn += contents.get(0).text() + "\n\n\n";
            }

            contents = doc.select("td.chapter-text p");
            for ( int i = 0; i < contents.size(); i++ ) {
                rtn += contents.get(i).text() + "\n\n";
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent2(String url) {
        StringBuffer rtn = new StringBuffer();
        try {
            Document doc = getDocument(url);
            Elements esA = doc.select("ul#book-ul a");
            for ( int i = 0; i < esA.size(); i++ ) {
                if ( esA.get(i).attr("href").indexOf(".txt") >= 0 ) {
                    InputStream inputStream = new URL("http://www.loyalbooks.com" + esA.get(i).attr("href")).openStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while((line = rd.readLine()) != null) {
                        rtn.append(line);
                        rtn.append('\n');
                    }
                    rd.close();
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn.toString();
    }

    public static File getFIle(String folderName, String fileName) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName + "/" + fileName);

        return saveFile;
    }

    public static String getHtmlString(String title, String contents, int fontSize) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!doctype html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<script src='https://code.jquery.com/jquery-1.11.3.js'></script>");
        sb.append("<script>");
        sb.append("$( document ).ready(function() {");
        sb.append("    $('#news_title,#news_contents').html(function(index, oldHtml) {");
        sb.append("        return oldHtml.replace(/<[^>]*>/g, '').replace(/(<br>)/g, '\\n').replace(/\\b(\\w+?)\\b/g,'<span class=\"word\">$1</span>').replace(/\\n/g, '<br>')");
        sb.append("    });");
        sb.append("    $('.word').click(function(event) {");
        sb.append("        window.android.setWord(event.target.innerHTML)");
        sb.append("    });");
        sb.append("});");
        sb.append("</script>");

        sb.append("<body>");
        sb.append("<h3 id='news_title'>");
        sb.append(title);
        sb.append("</h3>");
        sb.append("<font size='" + fontSize + "' face='돋움'><div id='news_contents'>");
        sb.append(contents);
        sb.append("</div></font></body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static String getMyNovelContent(String path) {
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                content += temp + "\n";
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        return content;
    }

    public static String getFilePageContent(String path, int pageSize, int page) {
        //dicLog("getFilePageContent : " + pageSize + " : " + page);
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            int getContentSize = 0;
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
                if ( getContentSize > ( page - 1 ) * pageSize && getContentSize < page * pageSize ) {
                    content += temp + "\n";
                } else if ( getContentSize > page * pageSize ) {
                    break;
                }
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        //dicLog("content length : " + content.length());
        return content;
    }

    public static int getFilePageCount(String path, int pageSize) {
        int getContentSize = 0;
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        int pageCount = (int)Math.ceil(getContentSize / pageSize);
        if ( getContentSize - pageCount * pageSize > 0 ) {
            pageCount++;
        }
        //dicLog("content page : " + getContentSize + " : " + pageSize + " : " + pageCount);
        return pageCount;
    }

    public static void setMainNews(Context mContext, String news) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_mainNews, news);
        editor.commit();
    }

    public static String getQueryParam(String str) {
        return str.replaceAll("\"","`").replaceAll("'","`");
    }

    public static void setPreferences(Context mContext, String pref, String val) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pref, val);
        editor.commit();
    }

    public static String getPreferences(Context mContext, String pref, String defaultVal) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String val = prefs.getString(pref, defaultVal);

        return val;
    }

    public static boolean equalPreferencesDate(Context mContext, String pref) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String date = prefs.getString(pref, "");
        dicLog(pref + " : " + date);

        if ( date.equals(getCurrentDate()) ) {
            return true;
        } else {
            setPreferences(mContext, pref, getCurrentDate());
            return false;
        }
    }

    public static String[] getSentencesArray(String str) {
        ArrayList al = new ArrayList();
        Pattern re = Pattern.compile("[^.!?\\s][^.!?]*(?:[.!?](?!['\"]?\\s|$)[^.!?]*)*[.!?]?['\"]?(?=\\s|$)", Pattern.MULTILINE | Pattern.COMMENTS);
        Matcher reMatcher = re.matcher(str);
        while (reMatcher.find()) {
            dicLog(reMatcher.group());
            al.add(reMatcher.group());
        }

        String[] rtn = new String[al.size()];
        for ( int i = 0; i < al.size(); i++ ) {
            rtn[i] = (String)al.get(i);
        }
        return rtn;
    }

    public static String getFileName(String saveFileName, String extension) {
        String fileName = "";

        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }

        if (saveFileName.indexOf(".") > -1) {
            fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
        } else {
            fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + "." + extension;
        }

        return fileName;
    }

    public static boolean writeExcelVocabulary(String fileName, Cursor cursor) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w(CommConstants.tag, "Storage not available or read only");
            return false;
        }

        boolean success = false;

        // 워크북 생성
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 워크시트 생성
        HSSFSheet sheet = workbook.createSheet("aaa");

        int rowIdx = 0;
        int cellIdx = 0;

        // Generate column headings
        HSSFRow row = sheet.createRow(rowIdx++);

        HSSFCell c = null;
        c = row.createCell(0);
        c.setCellValue("단어");
        sheet.setColumnWidth(0, (10 * 500));

        c = row.createCell(1);
        c.setCellValue("뜻");
        sheet.setColumnWidth(1, (15 * 500));

        c = row.createCell(2);
        c.setCellValue("스펠링");
        sheet.setColumnWidth(2, (15 * 500));

        c = row.createCell(3);
        c.setCellValue("예제");
        sheet.setColumnWidth(3, (30 * 500));

        c = row.createCell(4);
        c.setCellValue("메모");
        sheet.setColumnWidth(4, (30 * 500));

        // Create a path where we will place our List of objects on external storage
        File file = new File(fileName);
        FileOutputStream os = null;

        try {
            while (cursor.moveToNext()) {
                row = sheet.createRow(rowIdx++);

                cellIdx = 0;
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("SAMPLES")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("MEMO")));
            }

            os = new FileOutputStream(file);
            workbook.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }

        return success;
    }

    public static boolean readExcelVocabulary(SQLiteDatabase db, File file, String kind, boolean isOnlyWord) {
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return false;
        }

        try{
            FileInputStream myInput = new FileInputStream(file);

            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            HSSFWorkbook workbook = new HSSFWorkbook(myFileSystem);
            HSSFSheet mySheet = workbook.getSheetAt(0);

            Iterator<Row> rowIter = mySheet.rowIterator();
            while ( rowIter.hasNext() ) {
                HSSFRow myRow = (HSSFRow) rowIter.next();

                if ( isOnlyWord ) {
                    String word = getString(myRow.getCell(0).toString());
                    if (!"단어".equals(word) && !"".equals(word)) {
                        HashMap wordInfo = DicDb.getWordInfo(db, word);
                        if ( wordInfo.containsKey("WORD") ) {
                            String mean = (String)wordInfo.get("MEAN");
                            String spelling = DicUtils.getString((String)wordInfo.get("SPELLING")).replace("[","").replace("]","");
                            //String samples = DicDb.getWordSamples(db, word);
                            String samples = "";
                            String memo = "";

                            DicDb.insMyVocabulary(db, kind, word, mean, spelling, samples, "");
                        }
                    }
                } else {
                    int idx = 0;
                    String word = getString(myRow.getCell(idx++));
                    String mean = getString(myRow.getCell(idx++));
                    String spelling = getString(myRow.getCell(idx++));
                    String samples = getString(myRow.getCell(idx++));
                    String memo = getString(myRow.getCell(idx++));

                    if (!"단어".equals(word)) {
                        if (!"".equals(word) && !"".equals(mean)) {
                            DicDb.insMyVocabulary(db, kind, word, mean, spelling, samples, memo);
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return true;
    }

    public static void putCell(HSSFRow row, int cellIdx, String data) {
        HSSFCell c = row.createCell(cellIdx);
        c.setCellValue(data);
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean writeExcelBackup(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog("writeExcelBackup start");
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            dicLog("Storage not available or read only");
            return false;
        }

        boolean success = false;

        try {
            FileOutputStream fos = null;

            if ( "".equals(fileName) ) {
                fos = ctx.openFileOutput(CommConstants.systemBackupFile, Context.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("backup");
            int rowIdx = 0;
            int cellIdx = 0;
            HSSFCell c = null;

            StringBuffer sql = new StringBuffer();

            //단어 카테고리 저장
            sql.append("SELECT A.CODE_GROUP, A.CODE, A.CODE_NAME" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
            sql.append(" WHERE CODE_GROUP IN ('MY_VOC','C01','C02')" + CommConstants.sqlCR);
            sql.append("   AND CODE NOT IN ('VOC0001','C010001')" + CommConstants.sqlCR);
            Cursor cursor = db.rawQuery(sql.toString(), null);
            while (cursor.moveToNext()) {
                HSSFRow row = sheet.createRow(rowIdx++);

                cellIdx = 0;
                putCell(row, cellIdx++, CommConstants.tag_code_ins);
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("CODE_GROUP")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("CODE")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("CODE_NAME")));
            }
            cursor.close();

            //단어장 저장
            sql.setLength(0);
            sql.append("SELECT KIND, WORD, MEAN, SPELLING, SAMPLES, MEMO, MEMORIZATION, INS_DATE" + CommConstants.sqlCR);
            sql.append("  FROM DIC_MY_VOC" + CommConstants.sqlCR);
            cursor = db.rawQuery(sql.toString(), null);
            while (cursor.moveToNext()) {
                HSSFRow row = sheet.createRow(rowIdx++);

                cellIdx = 0;
                putCell(row, cellIdx++, CommConstants.tag_voc_ins);
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("KIND")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("SAMPLES")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("MEMO")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("MEMORIZATION")));
                putCell(row, cellIdx++, cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));
            }
            cursor.close();

            workbook.write(fos);

            success = true;

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("writeExcelBackup 에러=" + e.toString());
        }

        System.out.println("writeExcelBackup end");

        return success;
    }

    public static boolean readExcelBackup(Context ctx, SQLiteDatabase db, File file) {
        dicLog("readExcelBackup start");
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            dicLog("Storage not available or read only");
            return false;
        }

        boolean success = false;

        try{
            //데이타 초기화
            DicDb.initMyVocabulary(db);

            FileInputStream fis = null;
            if ( file == null ) {
                fis = ctx.openFileInput(CommConstants.systemBackupFile);
            } else {
                fis = new FileInputStream(file);
            }

            POIFSFileSystem myFileSystem = new POIFSFileSystem(fis);
            HSSFWorkbook workbook = new HSSFWorkbook(myFileSystem);
            HSSFSheet mySheet = workbook.getSheetAt(0);

            Iterator<Row> rowIter = mySheet.rowIterator();
            while ( rowIter.hasNext() ) {
                HSSFRow myRow = (HSSFRow) rowIter.next();

                int idx = 0;

                String kind = getString(myRow.getCell(idx++).toString());
                if ( kind.equals(CommConstants.tag_code_ins) ) {
                    DicDb.insCode(db
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString()));
                } else if ( kind.equals(CommConstants.tag_voc_ins) ) {
                    DicDb.insMyVocabulary(db
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString())
                            , getString(myRow.getCell(idx++).toString()));
                }
            }

            success = true;
            fis.close();
        } catch (Exception e) {
            DicUtils.dicLog("readExcelBackup 에러=" + e.toString());
        }

        System.out.println("readExcelBackup end");

        return success;
    }

    public static void setAdView(AppCompatActivity app) {
        AdView av = (AdView)app.findViewById(R.id.adView);
        if ( CommConstants.isFreeApp ) {
            AdRequest adRequest = new  AdRequest.Builder().build();
            av.loadAd(adRequest);
        } else {
            av.setVisibility(View.GONE);
        }
    }
}