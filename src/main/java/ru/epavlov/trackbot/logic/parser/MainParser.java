package ru.epavlov.trackbot.logic.parser;

import ru.epavlov.trackbot.entity.Track;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Eugene on 12.05.2017.
 */
public class MainParser {
    /**
     * 0 - Cainiao
     * 2 - Pochta
     * 3 - PochtaThread
     */
    private HashMap<Integer,Parser> parserMap;
    private static  MainParser instance;
  //  private ArrayList<ParserListener> listeners;
    private MainParser(){
        parserMap = new HashMap<>();
        parserMap.put(0, new ParserCainiao());
       // listeners = new ArrayList<>();
    }
    public static MainParser get(){
        if(instance == null){
            synchronized (MainParser.class) {
                if(instance == null){
                    instance = new MainParser();
                }
            }
        }
        return instance;
    }

    public boolean checkTrack(String text, int parser){
        try {
            return parserMap.get(parser).checkTrack(text);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Track parse(String trackId, int parser){
        try {
            Parser p = parserMap.get(parser);
            if (p!=null) return p.getData(trackId);
            return null;
            //else throw new TextException(TextException.WRONG_PARSER);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

//    public void addListener(ParserListener parserListener){
//        listeners.add(parserListener);
//    }
//    public void removeLitener(ParserListener parserListener){
//        listeners.remove(parserListener);
//    }
//    private void sendError(String error, String trackId){
//        listeners.forEach(listeners->{
//            listeners.onError(error,trackId);
//        });
//    }
//    private void sendData(Track data, int parser){
//        listeners.forEach(listeners->{
//            listeners.onData(data,parser);
//        });
//    }
    public interface ParserListener{
        void onData(Track lastInfo, int parser);
        void onError(String error, String trackId);
    }
    public String getText(Track track, String desc){
        return  parserMap.get(track.getParserCode()).getText(track, desc);
    }
    public String getParserName(int parserCode){
        return parserMap.get(parserCode).getName();
    }
    public Set<Integer> getParsers(){
         return  parserMap.keySet();
    }
}
