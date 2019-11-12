package dev.edmt.weatherapp.Common;

import android.support.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by reale on 05/10/2016.
 */

public class Common
{  
    @NonNull
    public static String apiRequest(String lat, String lng)
    {
	    final String API_KEY = "73e774e5e7212dee352ad8e5cf9e2236";
	    final String API_LINK = "http://api.openweathermap.org/data/2.5/weather";
    	
      StringBuilder sb = new StringBuilder(API_LINK);
      sb.append(String.format("?lat=%s&lon=%s&APPID=%s&units=metric",lat,lng,API_KEY));
      return sb.toString();
    }
    
    public static String unixTimeStampToDateTime(double unixTimeStamp)
    {
      DateFormat dateFormat = new SimpleDateFormat("HH:mm");
      Date date = new Date();
      date.setTime((long)unixTimeStamp*1000);
      return dateFormat.format(date);
    }
    
    public static String getImage(String icon)
    {
      return String.format("http://openweathermap.org/img/w/%s.png",icon);
    }
    
    public static String getDateNow()
    {
      DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");
      Date date = new Date();
      return dateFormat.format(date);
    }
}
