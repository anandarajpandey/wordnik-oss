package com.wordnik.system.mongodb;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.wordnik.util.RotatingFileWriter;

public class BaseMongoUtil {
	protected static String DATABASE_NAME = "test";
	protected static String DATABASE_USER_NAME = null;
	protected static String DATABASE_PASSWORD = null;
	protected static String DATABASE_HOST = "localhost";

	protected static String OUTPUT_DIRECTORY = null;
	protected static NumberFormat LONG_FORMAT = new DecimalFormat("###,###");
	protected static NumberFormat NUMBER_FORMAT = new DecimalFormat("###.##");
	protected static NumberFormat PERCENT_FORMAT = new DecimalFormat("#.##%");
	
	protected static boolean zip = false;
	protected static int fileSizeInMb = 100;
	protected static long WRITES = 0;
	protected static long REPORT_INTERVAL = 10000;
	
	private static DB db = null;
	protected DB getDb() throws Exception {
		if(db == null){
			synchronized(this){
				Mongo m = new Mongo(DATABASE_HOST);
				if(DATABASE_USER_NAME != null){
					db = m.getDB(DATABASE_NAME);
					db.authenticate(DATABASE_USER_NAME, DATABASE_PASSWORD.toCharArray());
				}
				else{
					db = m.getDB(DATABASE_NAME);
				}
			}
		}
		return db;
	}

	protected static Map<String, RotatingFileWriter> WRITERS = new HashMap<String, RotatingFileWriter>();
	protected void write(String collectionName, BasicDBObject dbo) throws IOException {
		RotatingFileWriter writer = WRITERS.get(collectionName);
		if(writer == null){
			writer = new RotatingFileWriter(collectionName, OUTPUT_DIRECTORY, "json", fileSizeInMb * 1048576L, zip);
			WRITERS.put(collectionName, writer);
		}
		writer.write(dbo.toString());
	}
	
	protected void closeWriter(String collectionName) throws IOException {
		RotatingFileWriter writer = WRITERS.get(collectionName);
		if(writer != null){
			writer.close();
		}
	}

	public static void usage(){
		System.out.println(" -d : database name");
		System.out.println(" -h : hostname");
		System.out.println(" -o : output directory");
		System.out.println(" [-u : username]");
		System.out.println(" [-p : password]");
	}

	protected static int parseArg(int pos, String[] args) {
		int i = pos;
		switch (args[i].charAt(1)) {
		case 'd':
			DATABASE_NAME = args[++i];
			break;
		case 'u':
			DATABASE_USER_NAME = args[++i];
			break;
		case 'p':
			DATABASE_PASSWORD = args[++i];
			break;
		case 'h':
			DATABASE_HOST = args[++i];
			break;
		case 'o':
			OUTPUT_DIRECTORY = args[++i];
			break;
		default:
			return 0;
		}
		return i - pos;
	}
}