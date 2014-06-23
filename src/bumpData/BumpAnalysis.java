package bumpData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BumpAnalysis {
	private Connection conn;
	String init;
	String outDir;

	public static void main(String[] args){
		try {
			BumpAnalysis ba = new BumpAnalysis();
			Map<String, Integer> clobResultMap =  ba.analyzeClobs();

			for (Map.Entry<String, Integer> entry : clobResultMap.entrySet()) {
				if ( entry.getValue() > 1 ) {
					System.out.println("Entry: " + entry.getKey());
					System.out.println("Count: " + entry.getValue());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public BumpAnalysis() throws IOException {
		Properties props = new Properties();
		String url=null;
		String driverClassName=null;
		String user=null;
		String password=null;

		props.load(new FileInputStream("/home/bhuggins/sources/BumpMigration/src/thumbnails/config/thumbnail-grabber.properties"));
		url = props.getProperty("connection_url");
		driverClassName = props.getProperty("class");
		user = props.getProperty("username");
		password = props.getProperty("password");
		init =  props.getProperty("init");
		outDir = props.getProperty("output_dir");

		try {
			Class.forName(driverClassName).newInstance();
			conn = DriverManager.getConnection(url, user, password);
		}  
		catch (ClassNotFoundException ex) {System.err.println("ClassNotFoundException:  " + ex.getMessage()); System.exit(1);}
		catch (IllegalAccessException ex) {System.err.println("IllegalAccessException:  " + ex.getMessage()); System.exit(1);}
		catch (InstantiationException ex) {System.err.println("InstantiationException:  " + ex.getMessage()); System.exit(1);}
		catch (SQLException ex)  {System.err.println("SQLException:  " + ex.getMessage()); System.exit(1);}
	}

	public Map<String, Integer> analyzeClobs() {
		Statement st = null;
		ResultSet rs = null;
		Clob clob = null;
		Map<String, Integer> clobMap = new HashMap<String, Integer>();
		Map<Integer, String> resultSetMap = new HashMap<Integer, String>();
		String nullBumpData="";
		String xmlBumpData="";
		int rowCount = 0;
		try
		{
			st = conn.createStatement();
			st.executeQuery(init);
			String sql = "SELECT BUMPS.BUMP_ID, BUMPS.BUMP_DATA " + "FROM AS_BUMPBUILDER_OWNER.BUMPS";
			//String sql = "SELECT BUMPS.BUMP_ID, BUMPS.BUMP_DATA " + "FROM AS_BUMPBUILDER_OWNER.BUMPS WHERE ROWNUM < 50000";
			if ( st.execute( sql ) ) {
				rs = st.getResultSet();

				int bump_id=0;
				while (rs.next()) {
					++rowCount;
					bump_id = rs.getInt("BUMP_ID");
					clob = rs.getClob( "BUMP_DATA" );
					if (null != clob) {
						try {
							String temp = java.net.URLDecoder.decode( clob.toString(), "UTF-8");
							xmlBumpData = java.net.URLDecoder.decode( temp , "UTF-8");
						} catch (UnsupportedEncodingException e) {
							System.out.println("Can convert double url encoded bumps_data into string");
							e.printStackTrace();
						}
						resultSetMap.put(bump_id, xmlBumpData);
					}
					else
						nullBumpData += (bump_id + ", ");
				}

				for (Map.Entry<Integer, String> entry : resultSetMap.entrySet()) {
					String rsmValue = entry.getValue();
					if ( clobMap.containsKey(rsmValue) )
						clobMap.put(rsmValue, (clobMap.get(rsmValue) + 1) );
					else
						clobMap.put(rsmValue, 1);
				}

				if (rowCount == 0) {
					System.out.println("No records found");
				}
				else
					System.out.println("Processed " + rowCount + " records");
				rs.close();
			}
			st.close();
		} 
		catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
		finally {
			if (st != null) {
				try {
					st.close();
				}
				catch (Exception e) {
					// Intentionally ignore
				}
			}
		}
		if (nullBumpData.length() > 0) System.out.println( "Bumps with null bump_data - " + nullBumpData );
		return clobMap; 
	}

	protected void finalize() {
		if (conn != null) {
			try {
				this.conn.close();
			} 
			catch (SQLException ex) {
				System.err.println(ex.getMessage());
			}
		}
	}

	void listProperties() {
		Properties props = new Properties();

		try {
			props.load(new FileInputStream("/home/bhuggins/sources/BumpMigration/src/thumbnails/config/thumbnail-grabber.properties"));
			System.out.println( "Properties" );
			System.out.print( props.getProperty("init")+"|" );
			System.out.print( props.getProperty("connection_url")+"|" );
			System.out.print( props.getProperty("class")+"|");
			System.out.print( props.getProperty("username")+"|");
			System.out.print( props.getProperty("schema_owner") + "|");
			System.out.println( props.getProperty("output_dir"));
			System.out.println( "EOP" );
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
