package proj.Position;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;

public class LookHelper {
	public Boolean look(String filePath, String fileName) {
		Boolean result = false;
		File SDCardRoot = Environment.getExternalStorageDirectory();
		File lookFilePath = new File(SDCardRoot + filePath + fileName);
		
		return lookFilePath.exists();
	}
}


//File vPath = new File( vSDCard.getParent() + vSDCard.getName() + "/tom" );
//if( !vPath.exists() )
//   vPath.mkdirs();

//File SDCardRoot = Environment.getExternalStorageDirectory();
//File vPath = new File( SDCardRoot + "/tom" );
//  if( !vPath.exists() )
//      vPath.mkdirs();
//   
//   // ¼g¤JÀÉ®×
//   FileWriter vFile = null;
//try {
//	vFile = new FileWriter( SDCardRoot + "/tom/myTest.txt" );
//} catch (IOException e1) {
//	// TODO Auto-generated catch block
//	e1.printStackTrace();
//}
//   try {
//	vFile.write("Hello Android");
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}
//   try {
//	vFile.close();
//} catch (IOException e) {
//	// TODO Auto-generated catch block
//	e.printStackTrace();
//}