package thumbnails;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ThumbnailGrabber {

	private Connection conn;
	String init;
	String outDir;

	public static void main(String[] args){
		try {
			ThumbnailGrabber tg = new ThumbnailGrabber();
			tg.listProperties();
			System.out.println( tg.process() );
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}
	public ThumbnailGrabber() throws IOException {
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
		}  catch (ClassNotFoundException ex) {System.err.println("ClassNotFoundException:  " + ex.getMessage()); System.exit(1);}
		catch (IllegalAccessException ex) {System.err.println("IllegalAccessException:  " + ex.getMessage()); System.exit(1);}
		catch (InstantiationException ex) {System.err.println("InstantiationException:  " + ex.getMessage()); System.exit(1);}
		catch (SQLException ex)  {System.err.println("SQLException:  " + ex.getMessage()); System.exit(1);}
	}

	private void writeToFile(int bump_id, InputStream is) {
		OutputStream outputStream = null;
		try {
			File thumbnailFile = new File(outDir + String.valueOf(bump_id) + ".jpg");
			thumbnailFile.createNewFile();
			outputStream = new FileOutputStream( thumbnailFile );

			int read = 0;
			byte[] bytes = new byte[1024];
	 
			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			};

			outputStream.close();
		}
		catch(IOException ioe) {
			System.out.println("Error writing file");
			ioe.printStackTrace();
		}
	}
	
	public int process() {
		Statement st = null;
		InputStream result = null;
		ResultSet rs = null;
		Blob blob = null;
		int rowCount = 0;
		try
		{
			st = conn.createStatement();
			st.executeQuery(init);
			String sql = "SELECT BUMPS.BUMP_ID, BUMPS.THUMBNAIL " + "FROM AS_BUMPBUILDER_OWNER.BUMPS WHERE BUMPS.BUMP_ID > 289625";
			//String sql = "SELECT BUMPS.BUMP_ID, BUMPS.THUMBNAIL " + "FROM AS_BUMPBUILDER_OWNER.BUMPS WHERE ROWNUM < 50000";
			if ( st.execute( sql ) ) {
                rs = st.getResultSet();
                int bump_id=0;
                while (rs.next()) {
                    ++rowCount;
                    bump_id = rs.getInt("BUMP_ID");
                    blob = rs.getBlob( "THUMBNAIL" );
                    if ( null != blob ) {
                    	result = blob.getBinaryStream();
                    	writeToFile(bump_id, result);
                    	result = null;
                    }
                    if (rowCount % 5000 == 0) System.out.println("Row count - " + rowCount);
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
		return rowCount; 
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