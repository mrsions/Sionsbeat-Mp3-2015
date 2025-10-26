package sions.android.sionsbeat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.androidquery.service.MarketService;
import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import sions.android.SQ;
import sions.android.sionsbeat.R;
import sions.android.sionsbeat.interpret.InterpretCollector;
import sions.android.sionsbeat.utils.CPUChecker;
import sions.android.sionsbeat.utils.ErrorController;
import sions.android.sionsbeat.utils.GameOptions;
import sions.android.sionsbeat.utils.GoogleGameService;
import sions.android.sionsbeat.utils.SoundFXPlayer;
import sions.android.sionsbeat.view.BackgroundDrawable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends BGMActivity implements Runnable {
	
	
	public static boolean STORE_NETWORK_STATE = true;

	// private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhufwSzNzG8q+MAbg4arAUonvwSwICBWPTEWkWTQp4ifJ2a5ZtDW8s5i5ZhxsD6stCiZH7gCOjqHu7pdNhkV9sv49rh0IHH+NQXu9xK3kIfUgDSgUxTu1UJ86rrgxDSaMxdoDosputsGtfURkFYCCVaykzEnLs4DUGN6LEY8CMBgQIL3r46rUUjR1NRhcIksNzpZhjqzdmBx9SWzP/Nnr2hhMe/dw0zeWok2AF7V7C0pyqZaHijTAJJJqKmOTbtN5My9qd5HwvSuoV9dMSssjNAiyRQzAuRuojSqM53wvLhHPx4SLCRB3IMH5y9Oh3qEGlN16q77ttjXulP09QFqbnQIDAQAB";
	// private static final byte[] SALT = {-46, 65, 30, -128, -103, -57, 74, -64, 51, 88, -95, -45, 77, -117, -36, -113, -11, 32, -64, 89};

	private ProgressBar mProgress;
	private TextView mText;
	private boolean enableNext;
	private boolean doNext;

	// private LicenseCheckerCallback mLicenseCheckerCallback;
	// private LicenseChecker mLicenseChecker;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		GameOptions.setupRootFile(this);
		if(GameOptions.getRootFile() == null)
		{
			Toast.makeText(this, R.string.not_found_storage, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		try{
			File tester = new File(GameOptions.get(this).getRootFile(), "tester");
			SQ.STORE_NETWORK_STATE = STORE_NETWORK_STATE = !tester.exists();
			if(!STORE_NETWORK_STATE){
				Toast.makeText(this, "개발자 모드입니다", Toast.LENGTH_LONG).show();
			}
			Log.d("test", "STORE_NETWORK_STATE "+STORE_NETWORK_STATE+" / "+tester+" / "+tester.exists());
		}catch(Throwable e){
			e.printStackTrace();
		}
		SQ.SQ(this);
		ErrorController.setupUncaughtException();
		
//		if(true){
//
//			Intent intent = new Intent(this, GameActivity.class);
//			intent.putExtra( GameActivity.INTENT_TAG, new File(GameOptions.getRootFile(), "album/Stop-Ghost-Kollective-132847-8.json").getAbsolutePath() );
//			startActivity( intent );
//			finish();
//
//			return;				
//		}

		BackgroundDrawable.setup(this);

		// mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		//
		// TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// String deviceId = tm.getDeviceId();
		//
		// mLicenseChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(), deviceId)), BASE64_PUBLIC_KEY);
		// mLicenseChecker.checkAccess(mLicenseCheckerCallback);

		// -- 멤버 변수 선언
		mProgress = (ProgressBar) findViewById(R.id.loading);
		mText = (TextView) findViewById(R.id.splash_text);

		// -- 쓰레드가 끝날 때 세팅이 완료된다.
		new Thread(this, "InitThread").start();

//		CPUChecker cpuc = new CPUChecker();
//		cpuc.setShow(true);
//		cpuc.start();

		// SHA 읽기
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");

				md.update(signature.toByteArray());
				md.digest();
				Log.i("PXR", Base64.encodeToString(md.digest(), Base64.DEFAULT));

				String st = "";
				byte[] sha1 = md.digest();
				for (byte b : sha1) {
					String hex = Integer.toHexString(b & 0xFF);
					if (hex.length() == 1) {
						st += "0" + hex + ":";
					} else {
						st += hex + ":";
					}
				}

				Log.i("PXR", st);
			}
		} catch (NameNotFoundException e) {
			Log.e("PXR", "", e);
		} catch (NoSuchAlgorithmException e) {
			Log.e("PXR", "", e);
		} finally {
			Log.i("PXR", "END");
		}
	}

	@Override
	protected void onStart () {
        super.onStart();
        if(SplashActivity.STORE_NETWORK_STATE){
        	try{
	        	EasyTracker.getInstance(this).activityStart(this);
	        	
	        	GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
	        	Tracker tracker = analytics.newTracker(getString(R.string.ga_trackingId));
	        	tracker.enableAdvertisingIdCollection(true);
        	}catch(Throwable e){
        		e.printStackTrace();
        	}
        }
	}
	
	@Override
	protected void onStop () {
	        super.onStop();
	        if(SplashActivity.STORE_NETWORK_STATE) EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onDestroy () {
		super.onDestroy();
		// mLicenseChecker.onDestroy();
	}

	/**
	 * 프로그램 초기화 인스톨, 네트워크 처리를 하기 위해 별도의 쓰레드에서 작업을 개시한다.
	 */
	@Override
	public void run () {
		SoundFXPlayer.get(this).play(R.raw.wellcome, GameOptions.get(this).getSettingInt(GameOptions.OPTION_SOUND_SYSTEM_VOICE)*0.01f);

		long startTime = System.currentTimeMillis();

		// -- 기초 폴더 생성
		File root = GameOptions.get(this).getRootFile();
		boolean rootExist = root.exists();
		if (!root.exists()) root.mkdirs();
		
		//-- 기존 파일 이전
		File beforeRootFile = GameOptions.getCompatRootFile(this);
		File[] beforeRootFiles = beforeRootFile.listFiles();
		if(beforeRootFile.exists() && !root.getAbsolutePath().equals(beforeRootFile.getAbsolutePath()) && beforeRootFiles!=null && beforeRootFiles.length>1)
		{
			boolean move = false;
			for(File ff:beforeRootFiles){
				if(ff.exists() && ff.isDirectory()){
					move = true;
				}
			}
			if(move){
				setLoading(0);
				setText(getString(R.string.splash_install_transport, 0));
				moveTo(beforeRootFile, root, 0, 100);
				rootExist = false;
			}
		}
		
		
		// -- 데이터 설치
		boolean install = false;
		try {

			// -- 설치 정보
			long installLength = GameOptions.get(this).getLong("installLength", 0L);
			int installVersion = GameOptions.get(this).getInt("installVersion", 0);

			// -- 비교 정보
			long length = getResources().openRawResourceFd(R.raw.sionsbeat).getLength();
			int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			
			Log.d("test", installLength+"!="+length+"/"+installVersion+"!="+versionCode+"/"+(!rootExist)+"/"+new File(root, "reinstall").exists());

			// -- 인스톨이 필요한지 결정
			install = !rootExist || length != installLength || installVersion != versionCode || new File(root, "reinstall").exists();
			// install = true; // test

			// -- Install
			if (install) {
				doInstall(root, length, versionCode);
				Thread.sleep(100);
			}
			// -- Networking Or joke
			else {
				// for(int i=0; i<100; i++){
				// Thread.sleep(2);
				// setLoading(i);
				// }
			}

			doExtract();

		}catch (Exception e) {
			if(e instanceof IOException && e.getMessage()!=null && e.getMessage().indexOf("ENOSPC") != -1){
				runOnUiThread(new Runnable() {
					public void run () {
						Toast.makeText(SplashActivity.this, R.string.no_space_left_on_device, Toast.LENGTH_LONG).show();
					}
				});
				return;
			}
			ErrorController.error(1, e);
			runOnUiThread(new Runnable() {
				public void run () {
					Toast.makeText(SplashActivity.this, R.string.splash_install_error, Toast.LENGTH_LONG).show();
				}
			});
			System.exit(0);
		}

		// -- 옵션 설정
		setupOptions();

		InterpretCollector.get(this);

		startTime = System.currentTimeMillis() - startTime;
		Log.d("test","Splash Time " + startTime + "ms");
		if (startTime < 2500) {
			try {
				Thread.sleep(2500 - startTime);
			} catch (Exception e) {}
		}

		// -- 최종
		this.enableNext = true;
		setText(getString(R.string.splash_touch));
		runOnUiThread(new Runnable() {
			public void run () {

				mProgress.setVisibility(View.INVISIBLE);
			}
		});

		// -- test
		// doNext();
	}

	/**
	 * 터치 이벤트가 발생한다.
	 */
	@Override
	public boolean onTouchEvent (MotionEvent event) {
		if (this.enableNext) {
			doNext();
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 프로그래스 바의 상태를 조정한다.
	 * 
	 * @param progress
	 */
	public void setLoading (final int progress) {
		runOnUiThread(new Runnable() {
			public void run () {
				try{
					mProgress.setProgress(progress);
				}catch(Exception e){}
			}
		});
	}

	/**
	 * 텍스트 뷰의 메세지를 수정한다.
	 * 
	 * @param text
	 */
	public void setText (final String text) {
		runOnUiThread(new Runnable() {
			public void run () {
				mText.setText(text);
			}
		});
	}

	/**
	 * 다음 화면 -> MusicSelectActivity로 이동한다.
	 */
	private void doNext () {
		if (!doNext) {
			doNext = true;

			Intent intent = new Intent(SplashActivity.this, MusicSelectActivity.class);
			startActivity(intent);
			finish();

		}
	}

	@Override
	public void finish () {
		super.finish();
		if (!doNext) {
			System.exit(0);
		}
	}

	/**
	 * 초기 옵션을 세팅한다.
	 */
	private void setupOptions () {

		GameOptions go = GameOptions.get(this);

		String marker = go.getString("marker", "GalaxyShutter");
		if (marker != null) {
			File file = go.getMarkerFile(marker);
			if (!file.exists() || !file.isDirectory()) {
				marker = null;
			}
		}
		if (marker == null) {
			File root = new File(go.getRootFile(), "marker");

			File[] markers = root.listFiles();
			for (File folder : markers) {
				if (folder.isDirectory()) {
					marker = folder.getAbsolutePath();
					marker = marker.substring(marker.lastIndexOf("/") + 1);
					break;
				}
			}

			if (marker != null) {
				go.put("marker", marker);
			} else {
				Toast.makeText(this, R.string.splash_abnormal_install, Toast.LENGTH_LONG);
				System.exit(0);
			}
		}

		if(go.getInt("absoluteOption",0) < 3){
//			go.put("absoluteOption", 3);
		}

	}

	/**
	 * 프로그램 데이터를 인스톨한다.
	 * 
	 * @param root
	 * @param length
	 * @param versionCode
	 * @throws Exception
	 */
	private void doInstall (File root, long length, int versionCode) throws Exception {

		setText(getString(R.string.splash_loading, 0));

		InputStream is = getResources().openRawResource(R.raw.sionsbeat);
		int total = is.available();

		ZipInputStream zis = null;
		ZipEntry ze = null;
		FileOutputStream fos = null;

		byte[] data = new byte[10240];
		int offset = 0;
		long printTime = System.currentTimeMillis();

		try {
			zis = new ZipInputStream(is);
			while (( ze = zis.getNextEntry() ) != null) {
				try {
					File f = new File(root, ze.getName());
					if (!f.isDirectory()) {
						f.getParentFile().mkdirs();

						fos = new FileOutputStream(f);
						while (( offset = zis.read(data) ) != -1) {
							fos.write(data, 0, offset);

							// -- 진행 상태를 화면에 표시합니다.
							if (System.currentTimeMillis() - printTime > 10) {
								printTime = System.currentTimeMillis();
								int percent = (int) ( ( (float) ( total - is.available() ) / total ) * 100 );

								setLoading(percent);
								setText(getString(R.string.splash_install, percent));
							}
						}
					}
				} finally {
					try {
						fos.close();
					} catch (Exception e) {}
				}
			}
		} finally {
			try {
				zis.close();
			} catch (Exception e) {}
			try {
				is.close();
			} catch (Exception e) {}
		}
		setLoading(100);
		setText(getString(R.string.splash_install, 100));

		GameOptions.get(this).put("installLength", length);
		GameOptions.get(this).put("installVersion", versionCode);

	}

	private void doExtract () throws UTFDataFormatException {

		extractFolders(new File(GameOptions.getRootFile(), "marker"));
		extractFolders(new File(GameOptions.getRootFile(), "music"));

	}

	private void extractFolders (File dir) throws UTFDataFormatException {
		for (File file : dir.listFiles()) {
			if (file.exists() && !file.isDirectory() && file.getName().toLowerCase().endsWith(".zip")) {
				extractZip(file);
			}
		}
	}

	private void extractZip (File file) throws UTFDataFormatException {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		ZipInputStream zis = null;
		ZipEntry ze = null;

		byte[] data = new byte[1024];
		int offset = 0;
		long printTime = System.currentTimeMillis();

		String rootName = file.getAbsolutePath();
		rootName = rootName.substring(0, rootName.lastIndexOf("."));

		String rootFileName = rootName.substring(rootName.lastIndexOf("/") + 1);

		File root = new File(rootName);
		root.mkdirs();

		try {
			fis = new FileInputStream(file);
			zis = new ZipInputStream(fis);

			int total = fis.available();

			while (( ze = zis.getNextEntry() ) != null) {
				try {
					File f = new File(root, ze.getName());
					if (!f.isDirectory()) {
						f.getParentFile().mkdirs();

						fos = new FileOutputStream(f);
						while (( offset = zis.read(data) ) != -1) {
							fos.write(data, 0, offset);

							// -- 진행 상태를 화면에 표시합니다.
							if (System.currentTimeMillis() - printTime > 10) {
								printTime = System.currentTimeMillis();
								int percent = ( total - fis.available() ) * 100 / total;

								setLoading(percent);
								setText(getString(R.string.splash_installZip, rootFileName, percent));
							}
						}
					}
				} finally {
					try {
						fos.close();
					} catch (Exception e) {}
				}
			}

			file.delete();

		} catch (UTFDataFormatException e){
			throw e;
		}catch (Exception e) {
			ErrorController.error(10, e);
		} finally {
			try {
				zis.close();
			} catch (Exception e) {}
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}

	private void moveTo(File src, File dst, float start, float end)
	{
		if(!src.exists());
		else if(src.isDirectory())
		{
			dst.mkdirs();
			File[] files = src.listFiles();
			File f;
			for(int i=0; i<files.length; i++)
			{
				f = files[i];
				float progressStart = ((end-start)*((float)i/files.length))+start;
				float progressEnd = ((end-start)*((i+1f)/files.length))+start;
				setLoading((int) progressStart);
				moveTo(f, new File(dst, f.getName()), progressStart, progressEnd);
			}
			try{ src.delete();
			}catch(Throwable ex){
				ErrorController.error(10, ex);
			}
		}
		else
		{
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try{
				fis = new FileInputStream(src);
				fos = new FileOutputStream(dst);
				
				byte[] data = new byte[1024];
				int size = 0;
				while((size=fis.read(data)) != -1){
					fos.write(data,0,size);
				}
			}catch(Throwable ex){
				ErrorController.error(10, ex);
			}finally{
				try{ fis.close(); }catch(Throwable ex){}
				try{ fos.close(); }catch(Throwable ex){}
			}
			try{ src.delete();
			}catch(Throwable ex){
				ErrorController.error(10, ex);
			}
		}
	}
	
	/********************************************************************
	 * 
	 * @Game API
	 * 
	 ********************************************************************/

	// private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
	//
	// @Override
	// public void allow (int reason) {
	// if (isFinishing()) return;
	//
	// Log.d("test","License 인증 성공 " + reason);
	// // 인증 성공
	// }
	//
	// @Override
	// public void dontAllow (int reason) {
	// if (isFinishing()) return;
	//
	// Log.d("test","License 인증 실패 " + reason);
	// // 인증 실패
	// }
	//
	// @Override
	// public void applicationError (int errorCode) {
	//
	// Log.d("test","License Error :" + errorCode);
	//
	// }
	//
	// }
}
