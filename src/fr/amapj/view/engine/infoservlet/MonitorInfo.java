/*
 *  Copyright 2013-2050 Emmanuel BRUN (contact@amapj.fr)
 * 
 *  This file is part of AmapJ.
 *  
 *  AmapJ is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  AmapJ is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with AmapJ.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 */
 package fr.amapj.view.engine.infoservlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.sun.management.UnixOperatingSystemMXBean;

import fr.amapj.common.StackUtils;
import fr.amapj.service.services.advanced.maintenance.MaintenanceService;

public class MonitorInfo
{
	// Charge du CPU entre 0 et 100 (totale de la machine)
	public int cpuLoad;

	// Pourcentage du disque disponible, compris entre 0 et 100 (totale de la
	// machine)
	public long diskFreeSpace;

	// Nombre de fichiers ouverts par l'application
	public long nbOpenFile;

	// Memoire (en Mo)
	public long memInit;
	public long memMax;
	public long memUsed;

	// Thread
	public int threadNb;
	public int threadPeak;
	
	// uptime en jours 
	public int upTimeDays;
	
	// Garbage collector
	public long totalGarbageCollections = 0;
	public long garbageCollectionTime = 0;
	
	// Résultats de top
	public List<String> top = new ArrayList<>();
	
	// Version du logiciel AmapJ
	public String version;
	
	
	@Override
	public String toString()
	{
		String str = "cpuLoad=" + cpuLoad + "<br/> diskFreeSpace=" + diskFreeSpace + "<br/> nbOpenFile=" + nbOpenFile + "<br/> memInit=" + memInit + "<br/> memMax="
				+ memMax + "<br/> memUsed=" + memUsed + "<br/> threadNb=" + threadNb + "<br/> threadPeak=" + threadPeak + "<br/> upTimeDays=" + upTimeDays+
				"<br/> totalGarbageCollections=" + totalGarbageCollections+"<br/> garbageCollectionTime=" + garbageCollectionTime+"<br/> ";
		
		str = str+"Top<br/><br/>";
		for (String s : top)
		{
			str = str+s+"<br/>";
		}
		
		return str;
	}
	
	

	static public MonitorInfo calculateMonitorInfo(boolean computeTop)
	{
		MonitorInfo info = new MonitorInfo();
	
		// Charge CPU
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		info.cpuLoad = (int) (os.getSystemLoadAverage() * 100);

		// Disque disponible
		File f = new File("/");
		if (f.getTotalSpace() != 0)
		{
			info.diskFreeSpace = (f.getFreeSpace() * 100L) / f.getTotalSpace();
		}

		// Nombre de fichiers ouverts
		if (os instanceof UnixOperatingSystemMXBean)
		{
			info.nbOpenFile = ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount();
		}

		// Memoire
		MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage mem = memBean.getHeapMemoryUsage();
		info.memInit = mem.getInit()/(1024*1024);
		info.memUsed = mem.getUsed()/(1024*1024);
		info.memMax = mem.getMax()/(1024*1024);
		
		// Nombre de threads
		ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
		info.threadNb = threadBean.getThreadCount();
		info.threadPeak = threadBean.getPeakThreadCount();
			
		// Uptime
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		info.upTimeDays = (int) (runtimeBean.getUptime()/(1000*3600*24));
		
		// Garbage collector 
		List<GarbageCollectorMXBean> garbageBean = ManagementFactory.getGarbageCollectorMXBeans();
		for(GarbageCollectorMXBean gc : garbageBean) 
		{	
	        long count = gc.getCollectionCount();

	        if(count >= 0) 
	        {
	            info.totalGarbageCollections += count;
	        }

	        long time = gc.getCollectionTime();

	        if(time >= 0) 
	        {
	            info.garbageCollectionTime += time;
	        }
		}
		
		// Top
		if (computeTop)
		{
			info.top = computeTopInfo();
		}
		
		// Version 
		info.version = new MaintenanceService().getVersion();
			

		return info;

	}
	
	static private List<String> computeTopInfo()
	{
		List<String> res = new ArrayList<>();

		try
		{
			ProcessBuilder builder = new ProcessBuilder("top", "-b", "-n", "1","-e","m","-o","%MEM");
			Process proc = builder.start();
	
			try (BufferedReader stdin = new BufferedReader(new InputStreamReader(proc.getInputStream())))
			{
				String line;
				while ((line = stdin.readLine()) != null)
				{
					res.add(line);
				}
			}
		}
		catch (IOException e) 
		{
			StackUtils.popStack(res, e);
		}
		
		return res;
	}

	public static String performMonitorInfo(boolean computeTopInfo)
	{
		MonitorInfo info = MonitorInfo.calculateMonitorInfo(computeTopInfo);
		return new Gson().toJson(info);
	}
	

	public static void main(String[] args)
	{
		System.out.println(performMonitorInfo(false));
	}
}
