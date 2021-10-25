package com.szusta.meduva.util;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static int MINUTE_OFFSET = 30;

    public static Calendar roundToNextHalfHour(Calendar calendar) {
        Calendar toRoundCalendar = (Calendar) calendar.clone();
        int currMinutes = toRoundCalendar.get(Calendar.MINUTE);
        int mod = currMinutes % MINUTE_OFFSET;
        toRoundCalendar.add(Calendar.MINUTE, MINUTE_OFFSET - mod);
        toRoundCalendar.set(Calendar.SECOND, 0);
        toRoundCalendar.set(Calendar.MILLISECOND, 0);
        return toRoundCalendar;
    }

    public static boolean isNDaysBetween(Calendar now, Calendar someday, int days) {
        Calendar tempSomeday = (Calendar) someday.clone();
        tempSomeday.add(Calendar.DAY_OF_MONTH, -days);
        return now.before(tempSomeday);
    }

    public static Date getDayStart(Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar.getTime();
    }

    public static Date getDayEnd(Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        return calendar.getTime();
    }
}
