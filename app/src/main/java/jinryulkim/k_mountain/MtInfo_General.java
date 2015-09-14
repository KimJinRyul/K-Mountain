package jinryulkim.k_mountain;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import jinryulkim.k_mountain.result.CardView;

/**
 * Created by jinryulkim on 15. 8. 21..
 */

public class MtInfo_General {
    public String code = null;            // 산코드
    public String name = null;            // 산이름
    public String sname = null;           // 부재
    public String address = null;         // 소재지
    public String high = null;            // 높이
    public String admin = null;           // 관리주체
    public String adminNum = null;        // 관리자 전화번호

    public String summary = null;         // 산정보 개관
    public String detail = null;          // 산정보 상세
    public ArrayList<String> imagePaths = null;  // 산이미지
    public MtInfo_Named namedInfo = null;        // 100대 명산 정보
    public CardView cardview = null;      // CardView
    public boolean animated = false;      // 최초 생성시 1번만 aniamtion
    public boolean downloaded = false;
    private boolean loading = false;

    public boolean admin_expanded = false;
    public boolean summary_expanded = false;
    public boolean detail_expanded = false;
    public boolean reason_expanded = false;
    public boolean overview_expanded = false;
    public boolean transport_expanded = false;
    public boolean tourism_expanded = false;
    public boolean etccource_expanded = false;

    public void initExpands() {
        admin_expanded = false;
        summary_expanded = false;
        detail_expanded = false;
        reason_expanded = false;
        overview_expanded = false;
        transport_expanded = false;
        tourism_expanded = false;
        etccource_expanded = false;
    }

    /**
     * File Path를 생성하여 준다.
     */
    public String makeImagePath(Context context, int i) {

        if(imagePaths != null && i < imagePaths.size()) {
            String url = imagePaths.get(i);
            String ext = url.substring(url.lastIndexOf("."));
            return context.getCacheDir() + "/" + code + "_" + i + ext;
        }
        return null;
    }
    /**
     * imagePaths에 해당하는 이미지들이 모두 다운로드 되었는지 확인한다
     */
    public boolean checkDownloaded(Context context) {
        if(imagePaths != null) {
            String path = null;
            for (int i = 0; i < imagePaths.size(); i++) {
                path = makeImagePath(context, i);
                if(path != null) {
                    File file = new File(path);
                    if(file.exists() == false || file.length() <= 0) {
                        downloaded = false;
                        return false;
                    }
                } else {
                    downloaded = false;
                    return false;
                }
            }
        }
        downloaded = true;
        return true;
    }

    public void requestDownloadImage(final Context context) {

        if(loading == true) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                loading = true;

                InputStream in = null;
                FileOutputStream out = null;
                URL url = null;
                try {
                    for(int i = 0; i < imagePaths.size(); i++) {

                        url = new URL(imagePaths.get(i));

                        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                        urlConn.setConnectTimeout(5000);
                        urlConn.setRequestProperty("User-Agent", "anything");
                        urlConn.setRequestMethod("GET");
                        urlConn.connect();

                        if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            InputStream is = urlConn.getInputStream();
                            if ("gzip".equals(urlConn.getContentEncoding())) {
                                is = new GZIPInputStream(is);
                            }

                            FileOutputStream fos = new FileOutputStream(makeImagePath(context, i));

                            int byteRead = -1;
                            byte[] buffer = new byte[2048];
                            while ((byteRead = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, byteRead);
                            }
                            is.close();
                            fos.close();
                        }
                        urlConn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (out != null)
                            out.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }

                checkDownloaded(context);

                if(cardview != null) {
                    cardview.downloadCompleted();
                }

                loading = false;
            }
        }.start();
    }
}
