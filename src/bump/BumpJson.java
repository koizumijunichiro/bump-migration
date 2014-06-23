package bump;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bumpData.BumpUtils;

class BumpJson {
	private int fileCount = 0;
	private int totalLineCount = 0;
	private String bumpFile;
	private StringBuilder jsonBumps;
	private String[] columnDescriptions;
	private static final int MAX_LINES=2500;

	BumpJson(String fileName){
		this.bumpFile = fileName;
	}

	void initializeJsonBumps() {
		jsonBumps = new StringBuilder("");
	}

	@SuppressWarnings("deprecation")
	/*
	 * 
	 * 
	 * bump_data - handled specially
	 * thumbnail_uri - to be loaded in a separate script
	 * 
	 * Oracle						mongodb
	 * ----------------------------------------------
	 * bumps_data					bump_data  
	 *								categories
	 * date_created					created_date
	 * filename						file_name
	 * shorturl
	 * thumbnail					thumbnail_uri
	 *
	 * 
	 * weighted_rating - unique to the node/mongodb solution
	 * 
	 * 
	 * 
	 * 
	 */
	String lineToJson(String line){
		Map<String, String> map = lineToMap(line);
		
		//translate bump_data
		String xmlBumpData="";
		String bump_data="\"\"";
		try {
			if ( map.containsKey("bumps_data")) {
				String temp = java.net.URLDecoder.decode( map.get("bumps_data") , "UTF-8");
				xmlBumpData = java.net.URLDecoder.decode( temp , "UTF-8");
			}
			else {
				throw ( new Exception("bumps_data is undefined for bump_id - " + map.get("bump_id")));
			}
		} catch (UnsupportedEncodingException e) {
			System.out.println("01 - Can convert double url encoded bumps_data into string");
			e.printStackTrace();
			return "}";
		} catch (Exception e) {
			bump_data="";
			System.out.println( "02.1 - Exception:  bumps_data is undefined for bump_id - " + map.get("bump_id") );
			System.out.println("02.2 - map:  " + map);
			System.out.println("02.3 - line:  " + line + "\n");
			return "}";
		}

		if (bump_data.equals(""))
			return "{}";
		else {
			//System.out.println("BumpJson 999.0 - xmlBumpData - :" + xmlBumpData );
			bump_data = BumpUtils.bumpDataToJson(xmlBumpData, line);
		}
		//System.out.println( map.get("bump_id") );
		
		StringBuilder json = new StringBuilder("{");
		json.append("\"approved\" : " + map.get("approved") + ",");											//01
		json.append("\"author_name\" : \"" + map.get("author_name") + "\",");								//02
		json.append("\"bump_data\" : " + bump_data + ",");													//03
		json.append("\"bump_id\" : " + map.get("bump_id") + ",");											//04
		//new field
		json.append("\"categories\" : [],");																//05
		// date_created Oracle / created_date monogodb
		json.append("\"created_date\" : \"" + map.get("date_created") + "\",");								//06
		// Oracle column deleted not used
		json.append("\"dislikes\" : " + map.get("dislikes") + ",");											//07
		
		if (map.containsKey("featured")) {
			String featured = (map.get("featured").equals("0"))?"false":"true";
			json.append("\"featured\" : " + featured + ",");												//08
		}
		// filename Oracle / file_name mongodb
		json.append("\"file_name\" : \"" + map.get("filename") + "\",");									//09
		json.append("\"flag_count\" : " + map.get("flag_count") + ",");										//10
		
		String is_sync_app="false";
		if ( map.containsKey("is_sync_app") ) is_sync_app=(map.get("is_sync_app").equals("0"))?"false":"true";
		json.append("\"is_sync_app\" : " + is_sync_app + ",");												//11
		
		String likes="";
		if ( map.containsKey("likes") ) likes=map.get("likes"); 
		json.append("\"likes\" : " + likes + ",");															//12
		
		String parent_id="-1";
		if ( map.containsKey("parent_id") ) parent_id = (( map.get("parent_id").equals("-1") )?"\"null\"":map.get("parent_id"));
		json.append("\"parent_id\" : " + parent_id + ",");													//13
		
		json.append("\"play_count\" : " + map.get("play_count") + ",");										//14
		
		String processed="false";
		if ( map.containsKey("processed") ) processed=(map.get("processed").equals("0"))?"false":"true";
		json.append("\"processed\" : " + processed + ",");													//15
		
		json.append("\"rating_count\" : " + map.get("rating_count") + ",");									//16
		json.append("\"rating_value\" : " + map.get("rating_value") + ",");									//17
		// Oracle column shorturl not needed
		
		String title="";
		if ( map.containsKey("title") ) {
			title = map.get("title");
			title = title.replace("\"", "\\\"");
		}
		json.append("\"title\" : \"" + title + "\",");														//18
		
		//thumbnail_uri to be updated w/ separate process
		json.append("\"thumbnail_uri\" : \"" + "https://s3.amazonaws.com/adultswim/4468a6b1-aee1-4c2a-a34c-86e1d02d188e.jpg" + "\",");//19
		// new field weighted_raiting
		json.append("\"weighted_rating\" :0");																//20
		json.append("}");
		return json.toString();
	}

	Map<String, String> lineToMap(String line){
		Map<String, String> bumpMap = new HashMap<String, String>();
		String[] lineArray = line.split("\\|", -1);
		for (int i=0; (i < columnDescriptions.length) && (i < lineArray.length); i++) {
			bumpMap.put(columnDescriptions[i], lineArray[i]);
		}
		//	System.out.println(bumpMap.toString());
		return bumpMap;
	}

	void writeJsonBumpFile(String line) {
		Map<String, String> bumpMap = lineToMap(line);


	}

	void writeToFile() {
		try {
			File file=new File("/home/bhuggins/misc/cartoon/bump-builder/bump-" + fileCount + ".json");
			file.createNewFile();

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			if (jsonBumps.charAt( jsonBumps.length()-1 ) == ',' ) jsonBumps.deleteCharAt( jsonBumps.length()-1 );
			//jsonBumps.append( "]" );
			bw.write( jsonBumps.toString() );
			bw.close();
			fileCount++;

			System.out.println("Created outputfile " + file.getName());
		}
		catch(IOException ioe) {
			System.out.println("03 - Error writing file");
			ioe.printStackTrace();
		}
	}

	String shorturlErrorHandler(BufferedReader br, String line) throws IOException {
		//trash the next 10 or 16 or x lines of error xml until you reach a line that begins with </html>
		String htmlLine="";
		while ( !htmlLine.startsWith("</html>") )
				htmlLine = br.readLine();
		//keep the last line of error xml and the final 3 pipe delimited columns
		line = line + htmlLine;
		System.out.println("11 - line: " + line );
		return line;
	}
	
	List<String> readBumps() {
		List<String> bumpList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			String currentLine;
			int lineCount=0;
			br = new BufferedReader(new FileReader(bumpFile));
			columnDescriptions = (br.readLine().split("\\|", -1));			
			initializeJsonBumps();

			while ((currentLine = br.readLine()) != null) {
//				if ( currentLine.contains("params%253D%25222%252529%2520Strap%2520on%2520a%2520ton") )
//					continue;
				if ( currentLine.length() < 67 ) // the line has been unexpectedly terminated w/ a newline
					currentLine += br.readLine();
				
				if (lineCount % MAX_LINES == 0 && lineCount > 0) {
					writeToFile();
					totalLineCount+=lineCount;
					lineCount=0;
					initializeJsonBumps();
				}
				
				if ( currentLine.endsWith("<html>") )   {// the column 'SHORTURL' contains a multi line xml error doc that must be discarded
					currentLine = shorturlErrorHandler( br, currentLine );
					System.out.println("09 - bad shorturl correction applied:  " + currentLine );
				}
				jsonBumps.append( lineToJson(currentLine) );
				if ( !(lineCount % MAX_LINES == (MAX_LINES-1)) ) 
					jsonBumps.append(",\n");
				lineCount++;
				//System.out.println(currentLine);
			}
			if (jsonBumps.length() > 1) {
				writeToFile();
			}


		} catch (IOException e) {
			System.out.println("04 - Error opening/reading " + bumpFile);
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				System.out.println("05 - Error closing file " + bumpFile);
				ex.printStackTrace();
			}
		}
		return bumpList;
	}



	public static void main(String[] args) {

		if ( !(args.length > 0) ) {
			System.out.println("Usage:\nBumpJson <path-to-bump-file>\n\n");
			System.exit(1);
		}
		new BumpJson( args[0] ).readBumps();
	}

}
