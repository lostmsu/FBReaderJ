package org.geometerplus.android.fbreader.tips;

import java.io.File;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.network.opds.OPDSEntry;
import org.geometerplus.fbreader.network.opds.OPDSFeedMetadata;
import org.geometerplus.fbreader.network.opds.OPDSFeedReader;
import org.geometerplus.fbreader.network.opds.OPDSXMLReader;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TipsService extends Service {
	public static final String TIPS_LOG = "tips";
	public static final String TIPS_STATE_KEY = "tips_state_key";
	
	private static final String TIPS_URL = "http://data.fbreader.org/tips/tips.xml"; // FIXME
	private static String TIPS_PATH;
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TIPS_LOG, "TipsService - onCreate");
		TIPS_PATH = Paths.networkCacheDirectory()+"/tips1.xml";
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.v(TIPS_LOG, "TipsService - onStart");

		try {
			File outFile = new File(TIPS_PATH);
			ZLNetworkManager.Instance().downloadToFile(TIPS_URL, outFile);
			Log.v(TIPS_LOG, "download done");
		} catch (ZLNetworkException e) {
			Log.v(TIPS_LOG, "exception: " + e.getMessage());
		}
		
		testParser();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.v(TIPS_LOG, "TipsService - onDestroy");
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v(TIPS_LOG, "TipsService - onBind");
		return null;
	}

	private void testParser(){
		ZLFile file = ZLFile.createFileByPath(TIPS_PATH);
		new OPDSXMLReader(new MyODPSFeedReader()).read(file);
	}

	
	// TODO test
	static String currenId = "fbreader-ru-hint-0000";
	static Tip myTip;
	
	private class MyODPSFeedReader implements OPDSFeedReader{
		@Override
		public void processFeedStart() {
		}
		
		@Override
		public void processFeedEnd() {
		}

		@Override
		public boolean processFeedMetadata(OPDSFeedMetadata feed, boolean beforeEntries) {
			//Log.v(TIPS_LOG, "processFeedMetadata >> " + feed.toString());
			return false;
		}
		
		@Override
		public boolean processFeedEntry(OPDSEntry entry) {
			Log.v(TIPS_LOG, "processFeedEntry >>" + entry.toString());
			myTip = new Tip(entry);
			if (myTip.getId().equals(nextId(currenId))){
				State.putToState(TIPS_STATE_KEY, myTip);
				
				final FBReaderApp fbReader = (FBReaderApp)FBReaderApp.Instance();
				fbReader.doAction(ActionCode.SHOW_TIP);
				
				return true;
			}
			return false;
		}
	}
	
	public class Tip {
		private OPDSEntry myEntry;
		
		Tip(OPDSEntry entry){
			myEntry = entry;
		}

		public String getId(){
			return myEntry.Id.Uri;
		}

		public String getTitle(){
			return myEntry.Title;
		}

		public String getSummary(){
			return myEntry.Summary;
		}
	}

	private static String nextId(String id){
		int val = Integer.parseInt(id.substring(id.length() - 4));
		val++;
		String end = Integer.toString(val);
		return id.substring(0, id.length() - end.length()) + end;
	}
	
}
