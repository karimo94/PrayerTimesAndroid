package com.karimo.prayertimes;
import java.util.Calendar;

/*import org.joda.time.DateTime;
import org.joda.time.chrono.IslamicChronology;*/

public class HijriCalendar
{
	static double gmod(double n,double  m) 
	{
        return ((n % m) + m) % m;
    }
 
    @SuppressWarnings("unused")
	static double[] kuwaiticalendar(boolean adjust) 
    {
        Calendar today = Calendar.getInstance();
        int adj=0;
        if(adjust)
        {
            adj=0;
        }
        else
        {
            adj=1;
        }
         
        if (adjust) 
        {
            int adjustmili = 1000 * 60 * 60 * 24 * adj;
            long todaymili = today.getTimeInMillis() + adjustmili;
            today.setTimeInMillis(todaymili);
        }
        double day = today.get(Calendar.DAY_OF_MONTH);
        double  month = today.get(Calendar.MONTH);
        double  year = today.get(Calendar.YEAR);
         
        double  m = month + 1;
        double  y = year;
        if (m < 3) 
        {
            y -= 1;
            m += 12;
        }
 
        double a = Math.floor(y / 100.);
        double b = 2 - a + Math.floor(a / 4.);
     
        if (y < 1583)
        {
            b = 0;
        }
        if (y == 1582) 
        {
            if (m > 10)
            {
                b = -10;
            }
            if (m == 10) 
            {
                b = 0;
                if (day > 4)
                {
                    b = -10;
                }
            }
        }
 
        double jd = Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + day
                + b - 1524;
         
        b = 0;
        if (jd > 2299160) 
        {
            a = Math.floor((jd - 1867216.25) / 36524.25);
            b = 1 + a - Math.floor(a / 4.);
        }
        double bb = jd + b + 1524;
        double cc = Math.floor((bb - 122.1) / 365.25);
        double dd = Math.floor(365.25 * cc);
        double ee = Math.floor((bb - dd) / 30.6001);
        day = (bb - dd) - Math.floor(30.6001 * ee);
        month = ee - 1;
        if (ee > 13) 
        {
            cc += 1;
            month = ee - 13;
        }
        year = cc - 4716;
 
        double wd = gmod(jd + 1, 7) + 1;
 
        double iyear = 10631. / 30.;
        double epochastro = 1948084;
        double epochcivil = 1948085;
 
        double shift1 = 8.01 / 60.;
 
        double z = jd - epochastro;
        double cyc = Math.floor(z / 10631.);
        z = z - 10631 * cyc;
        double j = Math.floor((z - shift1) / iyear);
        double iy = 30 * cyc + j;
        z = z - Math.floor(j * iyear + shift1);
        double im = Math.floor((z + 28.5001) / 29.5);
        if (im == 13)
        {
            im = 12;
        }
        double id = z - Math.floor(29.5001 * im - 29);
 
        double[]  myRes = new double[8];
 
        myRes[0] = day; // calculated day (CE)
        myRes[1] = month - 1; // calculated month (CE)
        myRes[2] = year; // calculated year (CE)
        myRes[3] = (jd - 1 == 0) ? jd : jd - 1; // julian day number
        myRes[4] = wd - 1; // weekday number
        myRes[5] = id; // islamic date
        myRes[6] = im - 1; // islamic month
        myRes[7] = iy; // islamic year
 
        return myRes;
    }
    static String writeIslamicDate() 
    {
        String[] wdNames = {"Ahad", "Ithnin", "Thulatha", "Arbaa", "Khemiss",
                "Jumuah", "Sabt"};
        String[] iMonthNames = {"Muharram", "Safar", "Rabi'ul Awwal",
                "Rabi'ul Akhir", "Jumadal Ula", "Jumadal Akhira", "Rajab",
                "Sha'ban", "Ramadan", "Shawwal", "Dhul Qa'ada", "Dhul Hijja"};
        // This Value is used to give the correct day +- 1 day
        boolean dayTest=true;
        double[] iDate = kuwaiticalendar(dayTest);
        String outputIslamicDate = wdNames[(int) iDate[4]] + ", " + (int)iDate[5] + " "
                + iMonthNames[(int) iDate[6]] + "\n" + (int)iDate[7] + " AH";
        
        // setup date object for midday on May Day 2004 (ISO year 2004)
        /*DateTime dtISO = new DateTime(2017, 4, 26, 12, 0, 0, 0);

        // find out what the same instant is using the Islamic Chronology
        DateTime dtIslamic = dtISO.withChronology(IslamicChronology.getInstance());
        System.out.println(dtIslamic.getDayOfMonth()); */
        return outputIslamicDate;
    }
}
