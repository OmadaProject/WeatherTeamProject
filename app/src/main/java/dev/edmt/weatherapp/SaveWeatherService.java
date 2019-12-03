package dev.edmt.weatherapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import dev.edmt.weatherapp.Common.Common;
import dev.edmt.weatherapp.Helper.Helper;

public class SaveWeatherService extends IntentService {

    private static final String[] CITY_NAMES = {MainActivity.THESSALONIKI_NAME, MainActivity.SERRES_NAME};

    public static final String TAG = SaveWeatherService.class.getSimpleName();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public SaveWeatherService(String name) {
        super(name);
    }

    public SaveWeatherService()
    {
        super("SaveWeatherService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        for (String cityName : CITY_NAMES) {
            String weatherJson = loadWeather(getApplicationContext(), cityName);
            if (weatherJson != null) {
                boolean succes = saveWeather(getApplicationContext(), cityName, weatherJson);
            }
        }

        stopSelf();
    }

    public static String getBasePath(Context context) {
        return context.getFilesDir().getPath() + File.separator;
    }


    private static @Nullable String loadWeather(@NonNull Context context, @NonNull String cityName) {
        double LAT;
        double LON;

        if (MainActivity.THESSALONIKI_NAME.equals(cityName)) {
            LAT = MainActivity.LAT_THESSALONIKI;
            LON = MainActivity.LON_THESSALONIKI;
        }
        else {
            LAT = MainActivity.LAT_SERRES;
            LON = MainActivity.LON_SERRES;
        }

        String urlString = Common.apiRequest(String.valueOf(LAT), String.valueOf(LON));
        Helper httpHelper = new Helper();
        String response = httpHelper.getHTTPData(urlString);


        if (response == null) {
            Log.w(TAG, "Could not get weather data");

            return null;
        }

        if (response.contains("Error: Not found city")) {
            return null;
        }

        return response;
    }

    private static boolean saveWeather(@NonNull Context context, @NonNull String cityName,
                                       @NonNull String weatherJson) {
        String filePath;

        if (MainActivity.THESSALONIKI_NAME.equals(cityName)) {
            filePath = getBasePath(context) + MainActivity.THESSALONIKI_FILENAME;
        }
        else {
            filePath = getBasePath(context) + MainActivity.SERRES_FILENAME;
        }

        FileOutputStream fileOutputStream = null;

        try
        {
            String content = weatherJson + System.getProperty("line.separator");
            File file = new File(filePath);
            boolean newFile = file.createNewFile(); // if file already exists will do nothing
            boolean append = true;
            StringBuffer stringBuffer;

            if(newFile)
                Log.i( TAG, "Created file to store weather information for " + cityName);

            if ( !newFile && ( stringBuffer = checkIfDateExists(filePath, weatherJson, cityName) ) != null )
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

        return false;
    }

    private static StringBuffer checkIfDateExists(@NonNull String filePath,
                                                  @NonNull String weatherJson, @NonNull String cityName) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            StringBuffer stringBuffer = new StringBuffer();
            String currentLine, lastLine = null;

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (lastLine != null) {
                    stringBuffer.append(lastLine);
                    stringBuffer.append('\n');
                }

                lastLine = currentLine;
            }

            bufferedReader.close();

            int index = lastLine.indexOf("dt") + 4;
            long dt1 = Long.parseLong(lastLine.substring(index, index + 10), 10);

            index = weatherJson.indexOf("dt") + 4;
            long dt2 = Long.parseLong(weatherJson.substring(index, index + 10), 10);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String date1 = simpleDateFormat.format(new Date(dt1 * 1000L));
            String date2 = simpleDateFormat.format(new Date(dt2 * 1000L));

            boolean equals = Objects.equals(date1, date2);

            if (equals) {
                Log.i(TAG, "Overwriting today's weather information for " + cityName);

                stringBuffer.append(weatherJson);
                stringBuffer.append('\n');

                return stringBuffer;
            } else
                Log.i(TAG, "Saving today's weather information for " + cityName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
