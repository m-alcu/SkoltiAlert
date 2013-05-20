package com.alcubier.skoltialert.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.alcubier.skoltialert.alerts.Alert;
import com.alcubier.skoltialert.mentions.Mention;
import com.alcubier.skoltialert.mentions.Utils;
import com.alcubier.skoltialert.tracks.Track;

/*

CREATE TABLE `track` (
  `IdTrack` double NOT NULL,
  `AliasTrack` varchar(20) NOT NULL,
  `IdClient` double NOT NULL,
  `SearchTrack` varchar(50) NOT NULL,
  `DateActive` date NOT NULL,
  `TopTrack` int NOT NULL,
  `BottomTrack` int NOT NULL,
  `Period` int NOT NULL,
  `LastId` bigint NOT NULL,
  PRIMARY KEY (`IdTrack`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

	double _id_track;
	String _alias_track;
	double _id_client;
	String _search_track;
	String _date_active;
	int    _top_track;
	int    _bottom_track;
	int    _period;
	long   _last_id;
	
	int    _id_mentions;
	String _date_mentions;
	String _hour_mentions;
	int    _duration;
	int    _count;
	long   _last_id;

*/

public class DatabaseHelper extends SQLiteOpenHelper {

	final String TAG = getClass().getName();
	
//	public static String dbName="skoltialertDB";
//=======================================================

	public static final String trackTable="track";
//-------------------------------------------------------	
	public static final String col_IdTrack="IdTrack";
	public static final String col_SearchTrack="SearchTrack";
	public static final String col_DateActive="DateActive";
	public static final String col_TopTrack="TopTrack";
	public static final String col_LastId="LastId";
    public static final String col_Count="Count";

	public static final String mentionsTable="mentions";
//-------------------------------------------------------	
	public static final String col_IdMentions="IdMentions";
//	public static final String col_IdTrack="IdTrack";
	public static final String col_DateMentions="DateMentions";
	public static final String col_Gifo="Gifo";
//	public static final String col_LastId="LastId";
	public static final String col_Text="Text";
	public static final String col_FromUser="FromUser";
	public static final String col_Iso="Iso";
	
	public static final String alertsTable="alerts";
	//-------------------------------------------------------	
	public static final String col_IdAlert="IdAlert";
//	public static final String col_IdTrack="IdTrack";
//	public static final String col_IdClient="IdClient";
//	public static final String col_SearchTrack="SearchTrack";
	public static final String col_DateAlertFrom="DateAlertFrom";
	public static final String col_DateAlertTo="DateAlertTo";
	public static final String col_IndAlert="Duration";
//	public static final String col_Duration="Duration";
//	public static final String col_Count="Count";
//	public static final String col_TopTrack="TopTrack";
//	public static final String col_BottomTrack="BottomTrack";
//	public static final String col_Period="Period";
	
	public DatabaseHelper(Context context) {

		super(context, Utils.getNameDatabase(context), null,23);
		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		db.execSQL("CREATE TABLE "+trackTable+" ("
				+col_IdTrack       +" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+col_SearchTrack   +" TEXT     , "
				+col_DateActive    +" DATE     , "
				+col_TopTrack      +" INTEGER  , "
				+col_LastId        +" BIGINT   , "
				+col_Count         +" INTEGER  );");	

		db.execSQL("CREATE TABLE "+mentionsTable+" ("
				+col_IdMentions    +" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+col_IdTrack       +" INTEGER  , "
				+col_LastId        +" BIGINT   , "
				+col_DateMentions  +" DATE     , "
				+col_FromUser      +" TEXT     , "
				+col_Gifo          +" TEXT     , "				
				+col_Text          +" TEXT     , "
				+col_Iso           +" TEXT    );");				

		db.execSQL("CREATE INDEX Indx1 ON "+mentionsTable+"("
				+col_IdTrack       +", "
				+col_LastId        +" );");
		
		db.execSQL("CREATE TABLE "+alertsTable+" ("
				+col_IdAlert       +" INTEGER PRIMARY KEY AUTOINCREMENT, "
				+col_IdTrack       +" INTEGER  , "
				+col_SearchTrack   +" TEXT     , "
				+col_DateAlertFrom +" DATE     , "
				+col_DateAlertTo   +" DATE     , "
				+col_IndAlert      +" INTEGER  , "				
				+col_Count         +" INTEGER  , "
				+col_TopTrack      +" INTEGER  );");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXISTS "+trackTable);
		db.execSQL("DROP INDEX IF EXISTS Indx1");
		db.execSQL("DROP TABLE IF EXISTS "+mentionsTable);
		db.execSQL("DROP TABLE IF EXISTS "+alertsTable);
		onCreate(db);
	}
	
	 public void AddTrack(Track track)
	{
		 
		SQLiteDatabase db= this.getWritableDatabase();
		ContentValues cv=new ContentValues();
		
//		cv.put(col_IdTrack, track.getIdTrack()); (autoincrement not needed)
		cv.put(col_SearchTrack, track.getSearchTrack());
		cv.put(col_DateActive, track.getDateActive());
		cv.put(col_TopTrack, track.getTopTrack());
		cv.put(col_LastId, track.getLastId());
		cv.put(col_Count, track.getCount());
		
		db.insert(trackTable, null, cv);
		db.close();
		
	}

	 public void AddMentions(Mention mentions)
	 {

		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();

		 cv.put(col_IdTrack, mentions.getIdTrack());
		 cv.put(col_DateMentions, mentions.getDateMentions());
		 cv.put(col_Gifo, mentions.getGifo());
		 cv.put(col_LastId, mentions.getLastId());
		 cv.put(col_Text, mentions.getText());
		 cv.put(col_FromUser, mentions.getFromUser());
		 cv.put(col_Iso, mentions.getIso());

		 db.insert(mentionsTable, null, cv);
		 db.close();

	 }

	 public void AddAlert(Alert alert)
	 {

		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();

		 cv.put(col_IdTrack, alert.getIdTrack());
		 cv.put(col_SearchTrack, alert.getSearchTrack());
		 cv.put(col_DateAlertFrom, alert.getDateAlertFrom());
		 cv.put(col_DateAlertTo, alert.getDateAlertTo());
		 cv.put(col_IndAlert, alert.getIndAlert());
		 cv.put(col_Count, alert.getCount());
		 cv.put(col_TopTrack, alert.getTopTrack());

		 db.insert(alertsTable, null, cv);
		 db.close();

	 }
 
	 
	 public int getTrackCount()
	 {
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+trackTable,null);
		int count= cur.getCount();
		cur.close();
		db.close();
		return count;
	 }
	 
	 public boolean getTrackDoNotExists(int idTrack)
	 {
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+trackTable+" WHERE "+col_IdTrack+"="+idTrack,null);
		int count= cur.getCount();
		cur.close();
		db.close();
		if (count == 1) {
			return false;
		} else {
			return true;
		}
	 }

	 public int getMentionsCount(int idTrack, String dateFrom, String dateTo)
	 {
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+mentionsTable+
                				" WHERE "+col_IdTrack+"="+idTrack+
				                  " AND "+col_DateMentions+">='"+dateFrom+
				                  "' AND "+col_DateMentions+"<'"+dateTo+"'",null);
				
		int count= cur.getCount();
		cur.close();
		return count;
	 }
	 
	 public Cursor getAllTracks()
	 {
		 SQLiteDatabase db=this.getWritableDatabase(); //puede ser modificada por el editor
		 Cursor cur= db.rawQuery("SELECT "+col_IdTrack+
				               " as _id, "+col_SearchTrack+
				                      ", "+col_DateActive+
				                      ", "+col_TopTrack+
				                      ", "+col_LastId+
				                      ", "+col_Count+
				                      " from "+trackTable, null);
		 return cur;
	 }
	 
	 public Date getFirstMention(int idTrack)
	 {
		 Date data = null;
		 SQLiteDatabase db=this.getReadableDatabase();
		 String consulta =    "SELECT "+col_IdMentions+
		 " as _id, "+col_IdTrack+
		 ", "+col_DateMentions+
		 ", "+col_Gifo+
		 ", "+col_LastId+				                      
		 ", "+col_Text+
		 ", "+col_FromUser+
		 ", "+col_Iso+
		 " from "+mentionsTable+
		 " WHERE "+col_IdTrack+"="+idTrack+
		 " ORDER BY "+col_DateMentions+" ASC LIMIT 1";
		 Log.i(TAG, "sql:"+consulta);
		 Cursor cur= db.rawQuery(consulta,null);
		 while (cur.moveToNext()) {
			 String salida = cur.getString(2);
			 Log.i(TAG,"antes String2Date"+salida);
			 data = Utils.String2Date(cur.getString(2), "yyyy-MM-dd HH:mm:ss");
			 Log.i(TAG,"despues String2Date"+data);
		 }
		 cur.close();
		 db.close();
		 return data;
	 }

	 public long getMaxLastId(int idTrack)
	 {
		 SQLiteDatabase db=this.getReadableDatabase();
		 long salida=0;
		 String consulta =    "SELECT "+col_LastId+
		 " from "+mentionsTable+
		 " WHERE "+col_IdTrack+"="+idTrack+
		 " ORDER BY "+col_LastId+" DESC LIMIT 1";
		 Cursor cur= db.rawQuery(consulta,null);
		 while (cur.moveToNext()) {
			 salida = cur.getLong(0);
		 }
		 cur.close();
		 db.close();
		 return salida;
	 }

	 
	 public Cursor getAlertMentions(int idTrack, String dateFrom, String dateTo)
	 {
		 SQLiteDatabase db=this.getReadableDatabase();
		 String Consulta =    "SELECT "+col_IdMentions+
		                	" as _id, "+col_IdTrack+
		                           ", "+col_DateMentions+
		                           ", "+col_Gifo+
		                           ", "+col_LastId+				                      
		                           ", "+col_Text+
		                           ", "+col_FromUser+
		                           ", "+col_Iso+
		                       " from "+mentionsTable+
		                       " WHERE "+col_IdTrack+"="+idTrack+
		                        " AND "+col_DateMentions+">='"+dateFrom+
		                       "' AND "+col_DateMentions+"<'"+dateTo+
		                       "' ORDER BY "+col_DateMentions+" DESC LIMIT 100";
			 
		 Cursor cur= db.rawQuery(Consulta,null);				                   
				                      
		 return cur;
	 }
	 
	 public ArrayList<Mention> ListAllMentions(int idTrack, long lastId, int limit)
	 {
		 ArrayList<Mention> mentions= new ArrayList<Mention>();
		 Mention currentMention;
		 SQLiteDatabase db=this.getReadableDatabase();
		 String Consulta = "SELECT "+col_IdMentions+
		 				 " as _id, "+col_IdTrack+
		 						", "+col_DateMentions+
		 						", "+col_Gifo+
		 						", "+col_LastId+				                      
		 						", "+col_Text+
		 						", "+col_FromUser+
		 						", "+col_Iso+
		 						" from "+mentionsTable+
		 						" WHERE "+col_IdTrack+"="+idTrack+
		 						" AND "+col_LastId+" < "+lastId+
		 						" ORDER BY "+col_LastId+" DESC LIMIT "+limit;
		 Cursor cur= db.rawQuery(Consulta,null);				                   
		 while (cur.moveToNext()) {
			 currentMention = new Mention();
			 currentMention.setIdMentions(cur.getInt(0));
			 currentMention.setIdTrack(cur.getInt(1));
			 currentMention.setDateMentions(cur.getString(2));
			 currentMention.setGifo(cur.getString(3));
			 currentMention.setLastId(cur.getLong(4));
			 currentMention.setText(cur.getString(5));
			 currentMention.setFromUser(cur.getString(6));
			 currentMention.setIso(cur.getString(7));
			 mentions.add(currentMention);
		 }
		 cur.close();
		 db.close();
		 return mentions;
	 }

	 public ArrayList<Mention> ListAlertMentions(int idTrack, String dateFrom, String dateTo, long lastId, int limit)
	 {
		 ArrayList<Mention> mentions= new ArrayList<Mention>();
		 Mention currentMention;
		 SQLiteDatabase db=this.getReadableDatabase();
		 String Consulta = "SELECT "+col_IdMentions+
		 				 " as _id, "+col_IdTrack+
		 						", "+col_DateMentions+
		 						", "+col_Gifo+
		 						", "+col_LastId+				                      
		 						", "+col_Text+
		 						", "+col_FromUser+
		 						", "+col_Iso+
		 						" from "+mentionsTable+
  		                        " WHERE "+col_IdTrack+"="+idTrack+
 		 					    " AND "+col_LastId+" < "+lastId+
		                        " AND "+col_DateMentions+">='"+dateFrom+
			                   "' AND "+col_DateMentions+"<'"+dateTo+
		 					   "' ORDER BY "+col_LastId+" DESC LIMIT "+limit;
		 Cursor cur= db.rawQuery(Consulta,null);				                   
		 while (cur.moveToNext()) {
			 currentMention = new Mention();
			 currentMention.setIdMentions(cur.getInt(0));
			 currentMention.setIdTrack(cur.getInt(1));
			 currentMention.setDateMentions(cur.getString(2));
			 currentMention.setGifo(cur.getString(3));
			 currentMention.setLastId(cur.getLong(4));
			 currentMention.setText(cur.getString(5));
			 currentMention.setFromUser(cur.getString(6));
			 currentMention.setIso(cur.getString(7));
			 mentions.add(currentMention);
		 }
		 cur.close();
		 db.close();
		 return mentions;
	 }

	 public int getAlertCount(int idTrack)
	 {
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+alertsTable+" WHERE "+col_IdTrack+"="+idTrack,null);
		int count= cur.getCount();
		cur.close();
		db.close();
		return count;
	 }

	 
	 public Cursor getAllAlerts(int idTrack)
	 {
		 SQLiteDatabase db=this.getWritableDatabase(); //puede ser modificada por el editor
		 Cursor cur= db.rawQuery("SELECT "+col_IdAlert+
				               " as _id, "+col_IdTrack+
				                      ", "+col_SearchTrack+
				                      ", "+col_DateAlertFrom+
				                      ", "+col_DateAlertTo+
				                      ", "+col_IndAlert+
				                      ", "+col_Count+				                      
				                      ", "+col_TopTrack+
				                      " from "+alertsTable+
				                      " WHERE "+col_IdTrack+"="+idTrack+
				                      " AND "+col_IndAlert+"=1 "+
				                      " ORDER BY "+col_IdAlert+" DESC", null);
		 return cur;
	 }

	 public ArrayList<Alert> ListAllAlerts(int idTrack, int top)
	 {
		 ArrayList<Alert> alerts= new ArrayList<Alert>();
		 Alert currentAlert;
		 SQLiteDatabase db=this.getReadableDatabase();
		 Cursor cur= db.rawQuery("SELECT "+col_IdAlert+
	               " as _id, "+col_IdTrack+
	               		  ", "+col_DateAlertFrom+
	                      ", "+col_DateAlertTo+
	                      ", "+col_IndAlert+
	                      ", "+col_Count+				                      
	                      ", "+col_TopTrack+
	                      " from "+alertsTable+
	                      " WHERE "+col_IdTrack+"="+idTrack+
 	                      " AND "+col_Count+" > "+top+
	                      " ORDER BY "+col_IdAlert+" ASC", null);
		 while (cur.moveToNext()) {
			 currentAlert = new Alert();
			 currentAlert.setIdAlert(cur.getInt(0));
			 currentAlert.setIdTrack(cur.getInt(1));
			 currentAlert.setDateAlertFrom(cur.getString(2));
			 currentAlert.setDateAlertTo(cur.getString(3));
			 currentAlert.setIndAlert(cur.getInt(4));
			 currentAlert.setCount(cur.getInt(5));
			 currentAlert.setTopTrack(cur.getInt(6));
			 alerts.add(currentAlert);
		 }
		 cur.close();
		 db.close();
		 return alerts;
	 }
	 
	 
	 public Date getLastAlert(int idTrack)
	 {
		 Date data = null;
		 SQLiteDatabase db=this.getReadableDatabase();
		 Cursor cur= db.rawQuery("SELECT "+col_IdAlert+
	               " as _id, "+col_IdTrack+
	                      ", "+col_SearchTrack+
	                      ", "+col_DateAlertFrom+
	                      ", "+col_DateAlertTo+
	                      ", "+col_IndAlert+
	                      ", "+col_Count+				                      
	                      ", "+col_TopTrack+
	                      " from "+alertsTable+
	                      " WHERE "+col_IdTrack+"="+idTrack+
	                      " ORDER BY "+col_IdAlert+" DESC LIMIT 1", null);
		 while (cur.moveToNext()) {
			 data = Utils.String2Date(cur.getString(4), "yyyy-MM-dd HH:mm:ss");
		 }
		 cur.close();
		 db.close();
		 return data;
	 }
	 public Vector<Double> VectorAllAlerts(int idTrack)
	 {
		 boolean firstDone = false;
		 Date data;
		 Long millis;
		 Double dMillis;
		 Double dFirstMillis;
		 Integer count;
		 Double dCount;
		 Vector<Double> vector = new Vector<Double>();
		 
		 dFirstMillis = (double) 0;
		 SQLiteDatabase db=this.getReadableDatabase();
		 Cursor cur= db.rawQuery("SELECT "+col_IdAlert+
	               " as _id, "+col_IdTrack+
	                      ", "+col_SearchTrack+
	                      ", "+col_DateAlertFrom+
	                      ", "+col_DateAlertTo+
	                      ", "+col_IndAlert+
	                      ", "+col_Count+				                      
	                      ", "+col_TopTrack+
	                      " from "+alertsTable+
	                      " WHERE "+col_IdTrack+"="+idTrack+
	                      " ORDER BY "+col_IdAlert+" ASC", null);
		 while (cur.moveToNext()) {
			 data = Utils.String2Date(cur.getString(4),"yyyy-MM-dd HH:mm:ss");
			 millis = data.getTime();
			 if (!firstDone) {
				 dMillis = millis.doubleValue();
				 dFirstMillis = dMillis;
				 vector.add(dMillis);
				 firstDone = true;
			 	} 
			 else {
				 dMillis = millis.doubleValue() - dFirstMillis;
				 vector.add(dMillis);
			 }
			 count = cur.getInt(6);
			 dCount = count.doubleValue();
			 vector.add(dCount);
		 }
		 cur.close();
		 db.close();
		 return vector;
	 }
	 
	 public List<Track> ListAllTracks()
	 {
		 List<Track> tracks= new ArrayList<Track>();
		 Track currentTrack;
		 SQLiteDatabase db=this.getReadableDatabase();
		 Cursor cur= db.rawQuery("SELECT "+col_IdTrack+
				                      ", "+col_SearchTrack+
				                      ", "+col_DateActive+
				                      ", "+col_TopTrack+
				                      ", "+col_LastId+
				                      ", "+col_Count+
				                      " from "+trackTable, null);
		 while (cur.moveToNext()) {
			 currentTrack = new Track();
			 currentTrack.setIdTrack(cur.getInt(0));
			 currentTrack.setSearchTrack(cur.getString(1));
			 currentTrack.setDateActive(cur.getString(2));
			 currentTrack.setTopTrack(cur.getInt(3));
			 currentTrack.setLastId(cur.getLong(4));
			 currentTrack.setCount(cur.getInt(5));
			 tracks.add(currentTrack);
		 }
		 cur.close();
		 db.close();
		 return tracks;
	 }
	 
	 public void UpdateTrack(Track track)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		 cv.put(col_SearchTrack, track.getSearchTrack());
		 cv.put(col_DateActive, track.getDateActive());
		 cv.put(col_TopTrack, track.getTopTrack());
		 cv.put(col_LastId, track.getLastId());
		 cv.put(col_Count, track.getCount());
		 db.update(trackTable, cv, col_IdTrack+"=?", new String []{String.valueOf(track.getIdTrack())});
		 db.close();
	 }
	 
	 public void UpdateTrackLastId(Track track)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		 cv.put(col_LastId, track.getLastId());
		 cv.put(col_DateActive, track.getDateActive());
		 db.update(trackTable, cv, col_IdTrack+"=?", new String []{String.valueOf(track.getIdTrack())});
		 db.close();
	 }

	 public void UpdateTrackCount(int IdTrack, int count)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		 cv.put(col_Count, count);
		 db.update(trackTable, cv, col_IdTrack+"=?", new String []{String.valueOf(IdTrack)});
		 db.close();
	 }

	 public void UpdateTrackDialog(Track track)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
		 ContentValues cv=new ContentValues();
		 cv.put(col_SearchTrack, track.getSearchTrack());
		 cv.put(col_TopTrack, track.getTopTrack());
		 db.update(trackTable, cv, col_IdTrack+"=?", new String []{String.valueOf(track.getIdTrack())});
		 db.close();
	 }

	 public void DeleteTrack(Track track)
	 {
		 SQLiteDatabase db=this.getWritableDatabase();
 		 db.delete(trackTable,col_IdTrack+"=?", new String [] {String.valueOf(track.getIdTrack())});
 		 db.close();
	 }
	 
//Cleanning Mentions BBDD.	 
	 
	 public long CountMentions() {

		  SQLiteDatabase db=this.getWritableDatabase();
          long total = DatabaseUtils.queryNumEntries(db,mentionsTable);
          db.close();
          return total;
      }
	  
	 public int CountMentionsOfTrack(int idTrack) {
	 
		SQLiteDatabase db=this.getReadableDatabase();
		Cursor cur= db.rawQuery("SELECT * FROM "+mentionsTable+" WHERE "+col_IdTrack+"="+idTrack,null);
		int count= cur.getCount();
		cur.close();
		db.close();
		return count;
	 }
	 
	  public void DeleteMention(int idMention, SQLiteDatabase db)
		 {
	 		 db.delete(mentionsTable,col_IdMentions+"=?", new String [] {String.valueOf(idMention)});
		 }

	  public void DeleteAlert(int idAlert, SQLiteDatabase db)
		 {
	 		 db.delete(alertsTable,col_IdAlert+"=?", new String [] {String.valueOf(idAlert)});
		 }	  
	  
	  public void DeleteOldMentionsByTrack( int limit, int idTrack)
		 {
		  
			SQLiteDatabase db=this.getWritableDatabase();
			String consulta;
			Cursor cur;
			String dateAlertTo = "";
//Borrado Mentions			
			consulta = "SELECT "+col_IdMentions+
									", "+col_DateMentions+
									" from "+mentionsTable+
									" WHERE "+col_IdTrack+"="+idTrack+
									" ORDER BY "+col_IdMentions+" ASC LIMIT "+limit;
			cur= db.rawQuery(consulta,null);				                   
			while (cur.moveToNext()) {
				DeleteMention(cur.getInt(0),db);
//				Log.i(TAG,"Borro idMention: "+cur.getInt(0));
				if (dateAlertTo.compareTo(cur.getString(1)) < 0) {
					dateAlertTo = cur.getString(1);
				}
			}
			cur.close();
//Borrado Alerts			
			Log.i(TAG,"Borro alert hasta: "+dateAlertTo);
			consulta = "SELECT "+col_IdAlert+
									" from "+alertsTable+
									" WHERE "+col_IdTrack+"="+idTrack+
									" AND "+col_DateAlertTo+"<'"+dateAlertTo+"'";
//			Log.i(TAG,"Borro alert sql: "+consulta);
			cur= db.rawQuery(consulta,null);
			while (cur.moveToNext()) {
				DeleteAlert(cur.getInt(0),db);
//				Log.i(TAG,"Borro idAlert: "+cur.getInt(0));
			}
			cur.close();
			
			db.close();
		 }
	  
	 public void CleanMentionsByTrack(Context context, int idTrack, boolean CleanAll) {

		 int difer;
		 int total;
		 int maxMentions = 1000;

		 if (!CleanAll) {
			 SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			 Integer MentionsPreference = Integer.parseInt(prefs.getString("list_last_mentions", "1000"));

			 switch(MentionsPreference) {
			 case 1000: maxMentions = 1000; break;
			 case 5000: maxMentions = 5000; break;
			 case 10000: maxMentions = 10000; break;
			 case 100000: maxMentions = 100000; break;
			 default: maxMentions = 10000; 
			 }
  			 Log.i(TAG,"Param borrar: "+maxMentions);
		 } else {
			 //delete all mentions
			 maxMentions = 0;
		 }
         
         total = CountMentionsOfTrack(idTrack);
         Log.i(TAG,"Item actual: "+idTrack);
         Log.i(TAG,"Numero mentions actual: "+total);
         if (total > maxMentions) {
        	 difer = total - maxMentions;
          	 Log.i(TAG,"Total a borrar: "+difer);
        	 DeleteOldMentionsByTrack(difer,idTrack);
         }
	 }

	  
	 public boolean UpdateTracks (Context context, String searchTrack) {

		 boolean hayAlarma = false;
		 boolean hayAlgunaAlarma = false;
		 List<Track> tracks=ListAllTracks();
		 Mention menciones;
		 Alert alerta;
		 int maxIdTrack = 0;
		 for (Track currentTrack : tracks) {
			 if ((currentTrack.getSearchTrack().equals(searchTrack)) || (searchTrack.equals(""))) {
				 menciones = new Mention();
				 menciones.GetMentions(this,currentTrack.getSearchTrack(), currentTrack);
//				 currentTrack.setLastId(menciones.getLastId());
				 alerta = new Alert();
				 hayAlarma = alerta.CheckAlert(this,currentTrack);
				 if (hayAlarma) {
					 hayAlgunaAlarma = true;
				 }
				 Log.i(TAG,"Actualizo track: "+alerta.getCount());
				 UpdateTrackCount(currentTrack.getIdTrack(),alerta.getCount());				 
			 }
			 if (currentTrack.getIdTrack() > maxIdTrack) {
				 maxIdTrack = currentTrack.getIdTrack();
			 }
			 
		 }
		 for( int i = 0 ; i < maxIdTrack ; i++ )  {
			 CleanMentionsByTrack(context,i,getTrackDoNotExists(i));
		 }
		 return hayAlgunaAlarma;
	 }
	 
}
