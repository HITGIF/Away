/** Copyright (C) 2016 Gustav Wang */

package carbonylgroup.away.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class History {

    private final String dateStr;
    private final String shortDateStr;
    public  final Date date;
    public  final long time_took;

    public History(Date _date, long _time_took) {

        date = _date;
        time_took = _time_took;
        dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS", Locale.CHINA).format(date);
        shortDateStr = new SimpleDateFormat("MM-dd", Locale.CHINA).format(date);
    }

    public String getDateStr(boolean showAll){

        if(showAll)
            return dateStr;
        else
            return shortDateStr;
    }
}
