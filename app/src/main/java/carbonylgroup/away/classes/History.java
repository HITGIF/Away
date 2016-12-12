/**
 * Copyright (C) 2016 Gustav Wang
 */

package carbonylgroup.away.classes;

import java.util.Date;

public class History {

    public final Date date;
    public final long time_took;

    public History(Date _date, long _time_took) {

        date = _date;
        time_took = _time_took;
    }

}