package dev.edmt.weatherapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import dev.edmt.weatherapp.Common.Common;
import dev.edmt.weatherapp.Helper.Helper;
import dev.edmt.weatherapp.Model.OpenWeatherMap;

public class MainActivity extends AppCompatActivity
{
	TextView txtCity, txtLastUpdate, txtDescription, txtHumidity, txtTime, txtCelsius;
	Spinner spinner;
	EditText editText;
	ImageView imageView;
	RadioGroup radioGroup;
	RadioButton radioButton;
	String jsonString, filePath, basePath;
	List<String> jsons;
	
	private static final Type TYPE = new TypeToken<OpenWeatherMap>(){}.getType();
	private static final String TAG = MainActivity.class.getName();
	public static final String THESSALONIKI_FILENAME = "thessaloniki_history.txt";
	public static final String SERRES_FILENAME = "serres_history.txt";
	public static final String THESSALONIKI_NAME = "Thessaloniki";
	public static final String SERRES_NAME = "Serres";
	public static final double LAT_THESSALONIKI = 40.640266d;
	public static final double LON_THESSALONIKI = 22.939524d;
	public static final double LAT_SERRES = 41.08499d;
	public static final double LON_SERRES = 23.54757d;
	
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.activity_main);
	  
	  txtCity = findViewById(R.id.txtCity);
	  txtLastUpdate = findViewById(R.id.txtLastUpdate);
	  txtDescription = findViewById(R.id.txtDescription);
	  txtHumidity = findViewById(R.id.txtHumidity);
	  txtTime = findViewById(R.id.txtTime);
	  txtCelsius = findViewById(R.id.txtCelsius);
	  imageView = findViewById(R.id.imageView);
	  radioGroup = findViewById(R.id.city);
	  spinner = findViewById(R.id.dropdown);
	  editText = findViewById(R.id.editTextKeyword);
	  radioButton = null;
	  jsonString = null;
	  filePath = null;
	  basePath = SaveWeatherService.getBasePath(getApplicationContext());
	  jsons = new ArrayList<>();
	  
	  spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
	  {
		  @Override
		  public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
		  {
			  if(position > 0)
			    setViews( jsons.get(position - 1) );
		  }
		  
		  @Override
		  public void onNothingSelected(AdapterView<?> parentView)
		  {
			  Log.i(TAG, "onNothingSelected");
		  }
	  });

	  SaveWeatherService.schedule(getApplicationContext());
  }
	
	public void save(View v)
	{
		if(jsonString == null)
		{
			Log.w(TAG, "No weather information to be saved");
			return;
		}
		
		FileOutputStream fileOutputStream = null;
		
		try
		{
			String content = jsonString + System.getProperty("line.separator");
			File file = new File(filePath);
			boolean newFile = file.createNewFile(); // if file already exists will do nothing
			boolean append = true;
			StringBuffer stringBuffer;
			
			if(newFile)
				Log.i( TAG, "Created file to store weather information for " + radioButton.getText().toString() );
				
			if ( !newFile && ( stringBuffer = checkIfDateExists() ) != null )
			{
				content = stringBuffer.toString();
				append = false;
			}
			
			fileOutputStream = new FileOutputStream(filePath, append);
			fileOutputStream.write( content.getBytes() );
			fileOutputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fileOutputStream != null)
			{
				try
				{
					fileOutputStream.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public void filter(View v)
	{
		if(filePath == null)
		{
			Log.w(TAG, "Select a city to load weather history");
			return;
		}
		
		Log.i( TAG, "Searching \"" + editText.getText().toString() + "\" in weather history for " + radioButton.getText().toString() );
	}
	
	StringBuffer checkIfDateExists()
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new FileReader(filePath) );
			StringBuffer stringBuffer = new StringBuffer();
			String currentLine, lastLine = null;
			
			while ( (currentLine = bufferedReader.readLine() ) != null)
			{
				if(lastLine != null)
				{
					stringBuffer.append(lastLine);
					stringBuffer.append('\n');
				}
				
				lastLine = currentLine;
			}
			
			bufferedReader.close();
			
			int index = lastLine.indexOf("dt") + 4;
			long dt1 = Long.parseLong(lastLine.substring(index, index + 10), 10);
			
			index = jsonString.indexOf("dt") + 4;
			long dt2 = Long.parseLong(jsonString.substring(index, index + 10), 10);
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
			String date1 = simpleDateFormat.format( new Date(dt1 * 1000L) );
			String date2 = simpleDateFormat.format( new Date(dt2 * 1000L) );
			
			boolean equals = Objects.equals(date1, date2);
			
			if(equals)
			{
				Log.i( TAG, "Overwriting today's weather information for " + radioButton.getText().toString() );
				
				stringBuffer.append(jsonString);
				stringBuffer.append('\n');
				
				return stringBuffer;
			}
			else
				Log.i( TAG, "Saving today's weather information for " + radioButton.getText().toString() );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void onRadioButtonClicked(View v)
	{
		boolean isThessaloniki = v.getId() == R.id.thessaloniki;

		radioButton = findViewById( radioGroup.getCheckedRadioButtonId() );

		filePath = basePath + (isThessaloniki ? THESSALONIKI_FILENAME : SERRES_FILENAME);
		
		final double LAT = (isThessaloniki ? LAT_THESSALONIKI : LAT_SERRES);
		final double LON = (isThessaloniki ? LON_THESSALONIKI : LON_SERRES);
		new GetWeather().execute( Common.apiRequest( String.valueOf(LAT),String.valueOf(LON) ) );
		
		List<String> dates = new ArrayList<>( Arrays.asList("LOAD WEATHER") );
		
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new FileReader(filePath) );
			String currentLine;
			jsons.clear();
			
			while ( (currentLine = bufferedReader.readLine() ) != null)
			{
				jsons.add( currentLine + System.getProperty("line.separator") );
				int index = currentLine.indexOf("dt") + 4;
				long dt = Long.parseLong(currentLine.substring(index, index + 10), 10);
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
				dates.add( simpleDateFormat.format( new Date(dt * 1000L) ) );
			}
			
			bufferedReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dates);
		spinner.setAdapter(adapter);
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

		Helper http = new Helper();
		stream = http.getHTTPData(urlString);

		return stream;
    }
    
    @Override
    protected void onPostExecute(String s)
	{
		super.onPostExecute(s);

		if (s == null) {
			Log.w(TAG, "Could not get weather data");
			pd.dismiss();
			Toast.makeText(getApplicationContext(), R.string.check_network, Toast.LENGTH_SHORT).show();

			return;
		}

		if (s.contains("Error: Not found city")) {
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
	  
	  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
	  String date = simpleDateFormat.format( new Date(openWeatherMap.getDt() * 1000L) );
	  
	  txtCity.setText( String.format( "%s,%s",openWeatherMap.getName(),openWeatherMap.getSys().getCountry() ) );
	  txtLastUpdate.setText( String.format(date) );
	  txtDescription.setText(String.format("%s",openWeatherMap.getWeather().get(0).getDescription()));
	  txtHumidity.setText(String.format("%d%%",openWeatherMap.getMain().getHumidity()));
	  txtTime.setText(String.format("%s/%s",Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunrise()),Common.unixTimeStampToDateTime(openWeatherMap.getSys().getSunset())));
	  txtCelsius.setText(String.format("%.2f °C",openWeatherMap.getMain().getTemp()));
	  Picasso.with(MainActivity.this).load(Common.getImage(openWeatherMap.getWeather().get(0).getIcon())).into(imageView);
  }
}
