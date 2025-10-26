package sions.android.sionsbeat.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.util.Log;


public class CPUChecker extends Thread
{
	public CPUChecker(){
		super("CpuCheck");
	}
	
	private String cpuUsage;
	private boolean show;
	
	public void run(){
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		String line ;
		String cmd = "top -n 1";
		while(true){
			BufferedReader br = null;
			try{
				sleep(100);
				process = runtime.exec(cmd);
				br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = br.readLine()) != null) {
					if(line.indexOf( "sions.android.sionsbeat" ) != -1){
						String[] tok = line.trim().split("[ ]+");
						cpuUsage = tok[2];
					}
				}
				
				if(show){
					Log.d( "performance", "CPU "+cpuUsage );
				}
			}catch(Throwable e){
				Log.e("performance", "error", e);
			}finally{
				try{br.close();}catch(Throwable e){}
				try{process.destroy();}catch(Throwable e){}
			}
		}
	}

	public String getCpuUsage ()
	{
		return cpuUsage;
	}

	public void setCpuUsage ( String cpuUsage )
	{
		this.cpuUsage = cpuUsage;
	}

	public boolean isShow ()
	{
		return show;
	}

	public void setShow ( boolean show )
	{
		this.show = show;
	}
	
	
}
