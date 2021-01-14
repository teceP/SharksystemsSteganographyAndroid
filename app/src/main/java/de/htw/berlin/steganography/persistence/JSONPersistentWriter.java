package de.htw.berlin.steganography.persistence;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class JSONPersistentWriter implements JSONPersistentHelper {
    Context context;
    private final String FILE_NAME ="JSONPersistentManagerFile77.json";

    public JSONPersistentWriter(Context context){
        this.context = context;
    }

    @Override
    public void writeToJsonFile(String s) throws IOException {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(s);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("JSONPersistentManager", "File write failed: " + e.toString());
        }
    }

    @Override
    public String readFromJsonFile() throws IOException {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("JSONPersistentWriter", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("JSONPersistentWriter", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
