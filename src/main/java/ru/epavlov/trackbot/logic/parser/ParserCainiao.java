package ru.epavlov.trackbot.logic.parser;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import ru.epavlov.trackbot.entity.Track;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/**
 * Created by Eugene on 12.05.2017.
 */
public class ParserCainiao implements Parser {

    private static final String url ="https://global.cainiao.com/detail.htm?mailNoList=";
    private static final String TAG = "["+ParserCainiao.class.getSimpleName()+"]: ";
    private static final Logger log = Logger.getLogger(ParserCainiao.class);
    private static final Pattern pattern = Pattern.compile(".*[0-9]{5,}.*");

    public Track getData(String track_id)  {
        //if (!checkTrack(track_id)) throw new TextException(TextException.WRONG_TEXT+": "+track_id);
        log.warn(TAG+url+track_id);
        String out="";
        try {
            URLConnection connection = new URL(url + track_id).openConnection();
            connection.setConnectTimeout(30000);
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String s;

            while ((s = br.readLine()) != null)
                if (s.contains("latestTrackingInfo") && s.contains("waybill_list_val_box")) {
                    out = s;
                    break;
                }
        } catch (Exception e) {
          //  MessageController.getInstance().notifyAdmins("Не удалось подключиться:\n"+url+track_id);
            return null;
        }
        if (out.equals("")) return null;// throw new TextException(TextException.WRONG_TRACK+": "+track_id);//  //не было данных
        try {
            Gson gson = new Gson();
            Track track = gson.fromJson(parseString(out), Track.class);
            track.setLastModify(System.currentTimeMillis());
            track.setId(track_id);
           // lastInfo.setTime();
            return track;
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(out);
            return null;
           // throw new TextException(TextException.WRONG_DATA);
        }
    }

    @Override
    public String getText(Track track, String desc) {
        String out=track.getId()+"\n";
        out+=desc==null||desc.equals("")?"":"\n"+desc+"\n\n";
        out+=(track.getStatus()==null ||  track.getStatus().equals(""))?"":"Статус: "+track.getStatus()+"\n";
        out+= track.getText()+"\n";
        out+= track.getTime()+"\n\n";
        out+="Проверен: "+ track.getLast_modify()+"\n";
        out+="Трекер: "+ getName();
        return  out;
    }

    @Override
    public String getName() {
        return "Global Cainiao";//"https://global.cainiao.com/";
    }



    public String getTrackerName() {
        return "Cainiao";
    }



    public boolean checkTrack(String text){
        return pattern.matcher(text).matches();
    }

    private String parseString(String out)  {
        try {
            out = out.split("latestTrackingInfo")[1];
            out = out.replaceAll("&quot;", "");
            out = out.replaceAll("</textarea>", "");
            out = "\"latestTrackingInfo" + out;
            String[] str = out.split("\\{");
            out = str[1].split("}")[0];
            out = out.replace("desc:", "{\"desc\":\"");
            out = out.replace(",timeZone:", "\"}");
            out = out.replace(",status:", "\",\"status\":\"");
            out = out.replace(",time:", "\",\"time\":\"");
            out = out.split("}")[0] + "}";
            return out;
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(out);
            return null;
            //   throw new TextException(TextException.WRONG_SITE_DATA);
        }
    }

    public static void main(String[] args)  {
        ParserCainiao parserCainiao =new ParserCainiao();
       // System.out.println(parserCainiao.getText(parserCainiao.getData("994574654029")));
    }
}
