package projet.odsig.com.moveasy.autre;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.Matrix;

public class Utils {
	private static Utils instance = null;

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}

	public boolean isOnline(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			cm = null;
			netInfo = null;
			return true;
		}
		
		cm = null;
		netInfo = null;
		return false;
	}
	
	static public boolean existFile(File path) {
		boolean resultat = false;
		resultat = path.exists();
		return resultat;
	}
	
	static public boolean createDirectory(File path) {
		boolean resultat = true;
		resultat = path.mkdirs();
		return resultat;
	}
	
	static public void copyFileOrDir(String path, AssetManager assetManager) {
	    String assets[] = null;
	    try {
	        assets = assetManager.list(path);
	        if (assets.length == 0) {
	            copyFile(path, assetManager);
	        } else {
	            //String fullPath = "/data/data/com.vlr.test/" + path;
	        	String fullPath = Globals.wPathFiles + path;
	        	
	            File dir = new File(fullPath);
	            if (!dir.exists())
	                dir.mkdir();
	            for (int i = 0; i < assets.length; ++i) {
	                copyFileOrDir(path + "/" + assets[i], assetManager);
	            }
	        }
	    } catch (IOException ex) {
	        Log.e("tag", "I/O Exception", ex);
	    }
	}
	static public void copyFileOrDirDB(String path, AssetManager assetManager) {
	    String assets[] = null;
	    try {
	        assets = assetManager.list(path);
	        if (assets.length == 0) {
	        	//String fullPath = "/data/data/com.vlr.test/databases/";
	        	String fullPath = Globals.wPathFiles + "databases/";
	        	File dir = new File(fullPath);
	            if (!dir.exists()) {
	                dir.mkdir();
	            }
	            copyFileDB(path, assetManager);
	        } 
	    } catch (IOException ex) {
	        Log.e("tag", "I/O Exception", ex);
	    }
	}
	static public void copyFileDB(String filename, AssetManager assetManager) {
	    OutputStream out = null;
	    
	    //String newFileName = "/data/data/com.vlr.test/databases/" + filename + ".sqlite";
	    String newFileName = Globals.wPathFiles + "databases/" + filename + ".sqlite";
	    try {
            Log.e("Kev global", Globals.wPathFiles);
            Log.e("Kev newfilename", newFileName);
	    	out = new FileOutputStream(newFileName);
	    	ZipInputStream zis = new ZipInputStream(new BufferedInputStream(assetManager.open(filename + ".zip")));
			@SuppressWarnings("unused")
			ZipEntry ze;
		    while ((ze = zis.getNextEntry()) != null) {
		        byte[] buffer = new byte[1024];
		        int count;
		        while ((count = zis.read(buffer)) != -1) {
		        	out.write(buffer, 0, count);
		        }
		    }
		    zis.close();
		    out.close();
		    zis = null;
		    out = null;
		} catch (Exception e) {
	        Log.e("alain", e.getMessage());
		}
	}

	static public void copyFile(String filename, AssetManager assetManager) {
	    //AssetManager assetManager = this.getAssets();

	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = assetManager.open(filename);
	        String newFileName = Globals.wPathFiles + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	    } catch (Exception e) {
	        Log.e("alain", e.getMessage());
	    }
	}
	
	static public boolean deleteDirectory(File path) { 
        boolean resultat = true; 
        if (path.exists()) { 
            File[] files = path.listFiles(); 
            for(int i=0; i<files.length; i++) { 
                if(files[i].isDirectory()) { 
                        resultat &= deleteDirectory(files[i]); 
                } else { 
                	resultat &= files[i].delete(); 
                } 
            }
            resultat &= path.delete();
        } 
        return resultat; 
	}

	public final static Document XMLfromString(String xml){
		
		Document doc = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			InputSource is = new InputSource();
	        is.setCharacterStream(new StringReader(xml));
	        doc = db.parse(is); 
	        
		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}
		       
        return doc;
        
	}
	
	public final static Document XMLfromFile(InputStream wfile){
		
		Document doc = null;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
        	
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			//InputSource is = new InputSource();
	        //is.setCharacterStream(new StringReader(xml));
			//InputStream file = 
	        
	        doc = db.parse(wfile); 
	        
		} catch (ParserConfigurationException e) {
			System.out.println("XML parse error: " + e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println("Wrong XML file structure: " + e.getMessage());
            return null;
		} catch (IOException e) {
			System.out.println("I/O exeption: " + e.getMessage());
			return null;
		}
		       
        return doc;
        
	}
	
	/** Returns element value
	  * @param elem element (it is XML tag)
	  * @return Element value otherwise empty String
	  */
	 public final static String getElementValue( Node elem ) {
	     Node kid;
	     if( elem != null){
	         if (elem.hasChildNodes()){
	             for( kid = elem.getFirstChild(); kid != null; kid = kid.getNextSibling() ){
	                 if( kid.getNodeType() == Node.TEXT_NODE  ){
	                     return kid.getNodeValue();
	                 }
	             }
	         }
	     }
	     return "";
	 }
		 
	public static int numResults(Document doc){		
		Node results = doc.getDocumentElement();
		int res = -1;
		try{
			res = Integer.valueOf(results.getAttributes().getNamedItem("count").getNodeValue());
		} catch(Exception e ){
			res = -1;
		}		
		return res;
	}

	public static String getValue(Element item, String str) {		
		NodeList n = item.getElementsByTagName(str);
		return Utils.getElementValue(n.item(0));
	}
	
	public static String getAttr(Element item, String str) {		
		//NodeList n = item.getElementsByTagName(str);
		return item.getAttribute(str);
	}
	
	public static String casteFloat(Float nb) {
		//String s = Double.toString(nb) + "00";
		String s = Float.toString(nb) + "000";
        int dot = s.indexOf('.');
        //String resu = s.substring(0, dot) + ".00";
        String resu = s.substring(0, dot + 4);
		return resu;
	}
	
	public static boolean isParsableToInt(String i) {
		try {
			Integer.parseInt(i);
			return true;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
	}
	
	public static void setListViewHeightBasedOnChildren(ListView listView, TextView textView) {
    	ListAdapter listAdapter = listView.getAdapter();
    	ViewGroup.LayoutParams params = listView.getLayoutParams();
    	
        if (listAdapter == null) {
        	params.height = 0;
            listView.setLayoutParams(params);
            listView.requestLayout();
            textView.setLayoutParams(params);
            textView.requestLayout();
            return;
        } else {
        	if (listAdapter.getCount() == 0) {
        		params.height = 0;
                listView.setLayoutParams(params);
                listView.requestLayout();
                textView.setLayoutParams(params);
                textView.requestLayout();
                return;
        	}
        }

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 0));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
	
	public static void rotateImage(ImageView mImage, int angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate((float) angle, 95, 95);
		mImage.setImageMatrix(matrix);
	}

    public static String strPointToLatLon(String point) {
        String transformedDestGeom = point.replace("POINT(", "");
        String transformedLat = "", transformedLon = "";

        transformedDestGeom = transformedDestGeom.replace(")", "");

        for (int c = 0; c < transformedDestGeom.length(); c++) {
            if (Character.isSpaceChar(transformedDestGeom.charAt(c))) {
                transformedLon = transformedDestGeom.substring(0, c - 1);
                transformedLat = transformedDestGeom.substring(c + 1, transformedDestGeom.length());
            }
        }

        String latLon = transformedLat + ", " + transformedLon;

        return latLon;
    }
}
