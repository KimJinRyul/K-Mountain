package jinryulkim.k_mountain;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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

    public class Weather {
        public Weather() {
            clear();
        }
        public void clear() {
            tempDay = tempMorn = tempEve = tempNight = tempMin = tempMax = null;
            pressure = null;
            humidity = null;
            windSpeed = null;
            clouds = null;
            rain = null;
            snow = null;
            id = -1;
            windDegree = -1;
            dt = -1;
            sunrise = sunset = 0;
        }
        public int id;                 // 날씨 코드
        public long dt;                // 예보시간UTC
        public String tempDay, tempMorn, tempEve, tempNight, tempMin, tempMax; // C
        public String pressure;        // hPa
        public String humidity;        // 습도%
        public String windSpeed;       // m/sec
        public int windDegree;         // 0~360
        public String clouds;          // 0~100%
        public String rain;            // mm
        public String snow;            // mm
        public long sunrise;           // 일출시간UTC
        public long sunset;            // 일몰시간UTC
    }

    public Weather todaysWeather = new Weather();
    public ArrayList<Weather> arrWeathers = new ArrayList<Weather>();

    public boolean weatherinfo = false;
    private boolean weatherloading = false;

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

    private final static String todayURL = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";
    private final static String forecastURL = "http://api.openweathermap.org/data/2.5/forecast/daily?lat=%s&lon=%s&cnt=5&units=metric";

    public void requestWeatherInfo(final Context context) {
        if(weatherloading == true || weatherinfo == true) {
            return;
        }

        new Thread() {
            @Override public void run() {

                boolean success = false;
                try {
                    weatherloading = true;
                    todaysWeather.clear();
                    arrWeathers.clear();

                    String cityLine;
                    String[] citySplit = {"0", "0"};
                    boolean bMatched = false;
                    for (int i = 0; i < CommonUtils.cityToGeo.length; i++) {
                        cityLine = CommonUtils.cityToGeo[i];
                        citySplit = cityLine.split("_");
                        if (citySplit.length == 3) {
                            if (address.indexOf(citySplit[0]) >= 0) {
                                bMatched = true;
                                break;
                            }
                        }
                    }

                    if (bMatched == true) {

                        // 오늘 예보
                        String addr = String.format(todayURL, citySplit[1], citySplit[2]);
                        URL url = new URL(addr);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                        String line;
                        String JSON = "";
                        while((line = reader.readLine()) != null) {
                            JSON += line + "\n";
                        }
                        reader.close();
                        JSON = JSON.trim();

                        JSONObject jsonObj = new JSONObject(JSON);
                        JSONArray jsonArr;
                        JSONObject objTemp;
                        Iterator<String> keys, mainKeys;
                        String key = "", mainKey = "";

                        mainKeys = jsonObj.keys();
                        while(mainKeys.hasNext()) {
                            mainKey = mainKeys.next();
                            if("weather".equals(mainKey)) {
                                jsonArr = jsonObj.getJSONArray(mainKey);
                                if(jsonArr != null && jsonArr.length() > 0) {
                                    objTemp = jsonArr.getJSONObject(0);
                                    keys = objTemp.keys();
                                    while(keys.hasNext()) {
                                        key = keys.next();
                                        if("id".equals(key)) {
                                            todaysWeather.id = objTemp.getInt(key);
                                            break;
                                        }
                                    }
                                }
                            } else if("main".equals(mainKey)) {
                                objTemp = jsonObj.getJSONObject(mainKey);
                                keys = objTemp.keys();
                                while(keys.hasNext()) {
                                    key = keys.next();
                                    if("temp".equals(key)) {
                                        todaysWeather.tempDay = objTemp.getString(key);
                                    } else if("temp_min".equals(key)) {
                                        todaysWeather.tempMin = objTemp.getString(key);
                                    } else if("temp_max".equals(key)) {
                                        todaysWeather.tempMax = objTemp.getString(key);
                                    } else if("pressure".equals(key)) {
                                        todaysWeather.pressure = objTemp.getString(key);
                                    } else if("humidity".equals(key)) {
                                        todaysWeather.humidity = objTemp.getString(key);
                                    }
                                }
                            } else if("wind".equals(mainKey)) {
                                objTemp = jsonObj.getJSONObject(mainKey);
                                keys = objTemp.keys();
                                while(keys.hasNext()) {
                                    key = keys.next();
                                    if("speed".equals(key)) {
                                        todaysWeather.windSpeed = objTemp.getString(key);
                                    } else if("ged".equals(key)) {
                                        todaysWeather.windDegree = objTemp.getInt(key);
                                    }
                                }
                            } else if("clouds".equals(mainKey)) {
                                objTemp = jsonObj.getJSONObject(mainKey);
                                keys = objTemp.keys();
                                while(keys.hasNext()) {
                                    key = keys.next();
                                    if("all".equals(key)) {
                                        todaysWeather.clouds = objTemp.getString(key);
                                    }
                                }
                            } else if("dt".equals(mainKey)) {
                                todaysWeather.dt = jsonObj.getLong(mainKey);
                            } else if("sys".equals(mainKey)) {
                                objTemp = jsonObj.getJSONObject(mainKey);
                                keys = objTemp.keys();
                                while(keys.hasNext()) {
                                    key = keys.next();
                                    if("sunrise".equals(key)) {
                                        todaysWeather.sunrise = objTemp.getLong(key);
                                    } else if("sunset".equals(key)) {
                                        todaysWeather.sunset = objTemp.getLong(key);
                                    }
                                }
                            }
                        }


                        // 5일 예보
                        addr = String.format(forecastURL, citySplit[1], citySplit[2]);
                        url = new URL(addr);
                        reader = new BufferedReader(new InputStreamReader(url.openStream()));

                        JSON = "";
                        while((line = reader.readLine()) != null) {
                            JSON += line + "\n";
                        }
                        reader.close();

                        JSON = JSON.trim();

                        jsonObj = new JSONObject(JSON);
                        jsonArr = jsonObj.getJSONArray("list");
                        for(int i = 0; i < jsonArr.length(); i++) {
                            objTemp = jsonArr.getJSONObject(i);
                            Iterator<String> iter = objTemp.keys();
                            Weather weather = new Weather();
                            while(iter.hasNext()) {
                                key = iter.next();
                                if("dt".equals(key)) {
                                    weather.id = objTemp.getInt(key);
                                } else if("pressure".equals(key)) {
                                    weather.pressure = objTemp.getString(key);
                                } else if("humidity".equals(key)) {
                                    weather.humidity = objTemp.getString(key);
                                } else if("speed".equals(key)) {
                                    weather.windSpeed = objTemp.getString(key);
                                } else if("deg".equals(key)) {
                                    weather.windDegree = objTemp.getInt(key);
                                } else if("clouds".equals(key)) {
                                    weather.clouds = objTemp.getString(key);
                                } else if("rain".equals(key)) {
                                    weather.rain = objTemp.getString(key);
                                } else if("snow".equals(key)) {
                                    weather.snow = objTemp.getString(key);
                                }
                            }

                            JSONObject objTemp2 = objTemp.getJSONObject("temp");
                            iter = objTemp2.keys();
                            while(iter.hasNext()) {
                                key = iter.next();
                                if("day".equals(key)) {
                                    weather.tempDay = objTemp2.getString(key);
                                    if(weather.tempDay.indexOf(".") > 0)
                                        weather.tempDay = weather.tempDay.substring(0, weather.tempDay.indexOf("."));
                                } else if("min".equals(key)) {
                                    weather.tempMin = objTemp2.getString(key);
                                    if(weather.tempMin.indexOf(".") > 0)
                                        weather.tempMin = weather.tempMin.substring(0, weather.tempMin.indexOf("."));
                                } else if("max".equals(key)) {
                                    weather.tempMax = objTemp2.getString(key);
                                    if(weather.tempMax.indexOf(".") > 0)
                                        weather.tempMax = weather.tempMax.substring(0, weather.tempMax.indexOf("."));
                                } else if("night".equals(key)) {
                                    weather.tempNight = objTemp2.getString(key);
                                    if(weather.tempNight.indexOf(".") > 0)
                                        weather.tempNight = weather.tempNight.substring(0, weather.tempNight.indexOf("."));
                                } else if("eve".equals(key)) {
                                    weather.tempEve = objTemp2.getString(key);
                                    if(weather.tempEve.indexOf(".") > 0)
                                        weather.tempEve = weather.tempEve.substring(0, weather.tempEve.indexOf("."));
                                } else if("morn".equals(key)) {
                                    weather.tempMorn = objTemp2.getString(key);
                                    if(weather.tempMorn.indexOf(".") > 0)
                                        weather.tempMorn = weather.tempMorn.substring(0, weather.tempMorn.indexOf("."));
                                }
                            }

                            JSONArray arrTemp = objTemp.getJSONArray("weather");
                            if(arrTemp != null && arrTemp.length() > 0) {
                                objTemp2 = arrTemp.getJSONObject(0);
                                iter = objTemp2.keys();
                                while (iter.hasNext()) {
                                    key = iter.next();
                                    if ("id".equals(key)) {
                                        weather.id = objTemp2.getInt(key);
                                    }
                                }
                            }
                            arrWeathers.add(weather);
                        }

                    } else {
                        // ... 시무룩....
                    }
                    success = true;

                } catch(Exception e) {
                    e.printStackTrace();
                    success = false;
                }

                //TEST CODE
                /*
                Log.i("jrkim", "today");
                Log.i("jrkim", "id:" + todaysWeather.id);
                Log.i("jrkim", "temp:" + todaysWeather.tempDay + "(" + todaysWeather.tempMax + ", " + todaysWeather.tempMin+ ")");
                Log.i("jrkim", "pressure:" + todaysWeather.pressure);
                Log.i("jrkim", "cloud:" + todaysWeather.clouds);
                Log.i("jrkim", "humidity:" + todaysWeather.humidity);
                Log.i("jrkim", "rain:" + todaysWeather.rain);
                Log.i("jrkim", "snow:" + todaysWeather.snow);
                Log.i("jrkim", "wind:" + todaysWeather.windSpeed + "," + todaysWeather.windDegree);
                Log.i("jrkim", "sun rise/set:" + todaysWeather.sunrise + "/" + todaysWeather.sunset);

                for(int i = 0; i < arrWeathers.size(); i++) {
                    Weather weather = arrWeathers.get(i);
                    Log.i("jrkim", "day:" + (i + 1));
                    Log.i("jrkim", "id:" + weather.id);
                    Log.i("jrkim", "temp:"  + "(" + weather.tempMax + ", " + weather.tempMin+ ")" + weather.tempMorn + "~" + weather.tempDay + "~" + weather.tempEve + "~" + weather.tempNight);
                    Log.i("jrkim", "pressure:" + weather.pressure);
                    Log.i("jrkim", "cloud:" + weather.clouds);
                    Log.i("jrkim", "humidity:" + weather.humidity);
                    Log.i("jrkim", "rain:" + weather.rain);
                    Log.i("jrkim", "snow:" + weather.snow);
                    Log.i("jrkim", "wind:" + weather.windSpeed + "," + weather.windDegree);
                }
                */

                if(cardview != null) {
                    cardview.weatherCompleted();
                }
                weatherloading = false;
                if(success)
                    weatherinfo = true;
            }
        }.start();
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
