package ru.epavlov.trackbot.dao;

import org.apache.log4j.Logger;
import ru.epavlov.trackbot.logic.parser.MainParser;


/**
 * Created by Eugene on 23.06.2017.
 */
public class TrackDAO {
    public static final int ERROR_CODE = -1;
    private static TrackDAO instance;
    private static final String TAG = "[" + TrackDAO.class.getSimpleName() + "]: ";
    private static final Logger Log = Logger.getLogger(TrackDAO.class);

    public static TrackDAO getInstance() {
        if (instance == null) {
            synchronized (TrackDAO.class) {
                if (instance == null) {
                    instance = new TrackDAO();
                }
            }
        }
        return instance;
    }

    /**
     * возвращаем код парсера по треку
     *
     * @param track
     * @return
     */
    public int isTrack(String track) {
        for (Integer parserCode : MainParser.get().getParsers()) {
            if (MainParser.get().checkTrack(track, parserCode)) {
                return parserCode;
            }
        }
        return ERROR_CODE;
    }




}


