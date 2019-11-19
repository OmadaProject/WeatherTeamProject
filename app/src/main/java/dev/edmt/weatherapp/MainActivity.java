package dev.edmt.weatherapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dev.edmt.weatherapp.Common.Common;
import dev.edmt.weatherapp.Helper.Helper;
import dev.edmt.weatherapp.Model.OpenWeatherMap;

public class MainActivity extends AppCompatActivity
{
	TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtCelsius;
	ImageView imageView;
	String jsonString, date;
	int MY_PERMISSION = 0;
	
	private static final Type TYPE = new TypeToken<OpenWeatherMap>(){}.getType();
	private static final String TAG = MainActivity.class.getName();
	private static final String FILENAME = "weather_history.txt";
	
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
	  // Coordinates of target city (currently Thessaloniki)
	  final double Lat = 40.736851d;
	  final double Lng = 22.920227d;
	
	  new GetWeather().execute( Common.apiRequest( String.valueOf(Lat),String.valueOf(Lng) ) );
	  
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.activity_main);
	  
	  jsonString = null;
    txtCity = findViewById(R.id.txtCity);
    txtLastUpdate = findViewById(R.id.txtLastUpdate);
    txtDescription = findViewById(R.id.txtDescription);
    txtHumidity = findViewById(R.id.txtHumidity);
    txtTime = findViewById(R.id.txtTime);
    txtCelsius = findViewById(R.id.txtCelsius);
    imageView = findViewById(R.id.imageView);
    
    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
			PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
		    {
          Manifest.permission.INTERNET,
          Manifest.permission.ACCESS_COARSE_LOCATION,
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_NETWORK_STATE,
          Manifest.permission.SYSTEM_ALERT_WINDOW,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, MY_PERMISSION);
    }
  }
	
	public void getCurrentWeather(View v)
	{
		// Coordinates of target city (currently Thessaloniki)
		final double Lat = 40.736851d;
		final double Lng = 22.920227d;
		
		new GetWeather().execute( Common.apiRequest( String.valueOf(Lat),String.valueOf(Lng) ) );
	}
	
	public void save(View v)
	{
		Context context = getApplicationContext();
		String filePath = context.getFilesDir().getPath() + "/" + FILENAME;
		
//		NEED TO IMPLEMENT THIS
		boolean overwrite = checkIfSameDate(filePath);
		
		FileOutputStream fos = null;
		
		try
		{
			StringBuilder stringBuilder = new StringBuilder(jsonString);
			stringBuilder.append( System.getProperty("line.separator") );
			
			fos = new FileOutputStream(filePath, true);
			fos.write( stringBuilder.toString().getBytes() );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fos != null)
			{
				try
				{
					fos.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	boolean checkIfSameDate(String filePath)
	{
		boolean result = false;
		
		try
		{
			BufferedReader br = new BufferedReader( new FileReader(filePath) );
			String currentLine, lastLine = "";
			
			while ( (currentLine = br.readLine() ) != null)
				lastLine = currentLine;
			
			int index = lastLine.indexOf("dt") + 4;
			Long dt1 = Long.parseLong(lastLine.substring(index, index + 10), 10);
			
			index = jsonString.indexOf("dt") + 4;
			Long dt2 = Long.parseLong(jsonString.substring(index, index + 10), 10);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
			String date1 = simpleDateFormat.format( new Date(dt1 * 1000L) );
			String date2 = simpleDateFormat.format( new Date(dt2 * 1000L) );
			
			result = Objects.equals(date1, date2);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void load(View v)
	{
		FileInputStream fis = null;
		
		try
		{
			fis = openFileInput(FILENAME);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			StringBuilder sb = new StringBuilder();
			String text;
			
			while ( (text = br.readLine() ) != null)
			{
				sb.append(text).append("\n");
			}
			
			jsonString = sb.toString();
			setViews(jsonString);
			
			Log.i(TAG, "Loaded weather successfully");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
  private class GetWeather extends AsyncTask<String,Void,String>
  {
    ProgressDialog pd = new ProgressDialog(MainActivity.this);
    
    @Override
    protected void onPreExecute()
    {
      super.onPreExecute();
      pd.setTitle("Please wait...");
      pd.show();
    }
    
    @Override
    protected String doInBackground(String... params)
    {
    	String stream = null;
      String urlString = params[0];
      
//	    Log.i(TAG, urlString);
	    
	    Helper http = new Helper();
	    stream = http.getHTTPData(urlString);
	    
//	    Log.i(TAG, stream);
	    
	    return stream;
    }
    
    @Override
    protected void onPostExecute(String s)
    {
      super.onPostExecute(s);
      
      if(s.contains("Error: Not found city"))
      {
        pd.dismiss();
        return;
      }
      
      jsonString = s;
      
      pd.dismiss();
      
      setViews(s);
    }
  }
  
  public void setViews(String string)
  {
	  Gson gson = new Gson();
	  OpenWeatherMap openWeatherMap = gson.fromJson(string, TYPE);
	  
	  txtCity.setText( String.format( "%s,%s",openWeatherMap.getName(),openWeatherMap.getSys().getCountry() ) );
	  
	  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
	  date = simpleDateFormat.format( new Date(openWeatherMap.getDt() * 1000L) );
	  
	  txtLastUpdate.setText( String.format(date) );
	  txtDescription.setText(String.format("%s",openWeatherMap.getWeather().get(0).getDescription()));
	  txtHumidity.setText(String.format("%d%%",openWeatherMap.getMain().getHumidity()));
	  txtTime.setText(String.format("%s/%s",Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise()),Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
	  txtCelsius.setText(String.format("%.2f °C",openWeatherMap.getMain().getTemp()));
	  Picasso.with(MainActivity.this).load(Common.getImage(openWeatherMap.getWeather().get(0).getIcon())).into(imageView);
  }
}
