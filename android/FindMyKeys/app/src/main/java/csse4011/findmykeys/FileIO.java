package csse4011.findmykeys;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;



//import android.app.Activity;
//import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class FileIO {
	private String filename;
	private File sdCard;
	private File dir;
	
	public FileIO(String path, String filename){
		this.filename = filename;
		String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	    	Log.d("FileIO","Media writable");
	    }
		sdCard = Environment.getExternalStorageDirectory();
	    dir = new File ("/storage/sdcard1" +path);
	}

public void writeToFile(String s){
    File file = new File(dir,this.filename);
    try {
        FileOutputStream f = new FileOutputStream(file,true); //True = Append to file, false = Overwrite
        PrintStream p = new PrintStream(f);
        p.print(s);
        p.close();
        f.close();


    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }   
}
}