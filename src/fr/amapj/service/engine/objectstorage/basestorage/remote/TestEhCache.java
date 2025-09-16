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
 package fr.amapj.service.engine.objectstorage.basestorage.remote;

import java.io.File;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;

import fr.amapj.view.engine.ui.AmapJLogManager;

public class TestEhCache 
{
	public static void main(String[] args) throws InterruptedException 
	{
		AmapJLogManager.setLogDir("../../../logs/");
		
		
		
		StatisticsService statisticsService = new DefaultStatisticsService();
	   

		PersistentCacheManager persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.using(statisticsService)
				.with(CacheManagerBuilder.persistence(new File("c://tmp/myData3"))) 
				.withCache("persistent-cache", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
							ResourcePoolsBuilder.newResourcePoolsBuilder().disk(2, MemoryUnit.GB, true)) 
		  )
		  .build(true);
		
		
		Cache<Long, byte[]> preConfigured =   persistentCacheManager.getCache("persistent-cache", Long.class, byte[].class); 
		
		preConfigured.put(1L, new byte[] { 1 ,2} ); 
		byte[] value = preConfigured.get(1L); 
		
		
		System.out.println("===========================================================                  value="+value[0]+ "   "+value[1]);

		
			
		CacheStatistics s = statisticsService.getCacheStatistics("persistent-cache");
		
		System.out.println("Cahce hit= "+s.getCacheHits());
		
		System.gc();
		
		Thread.sleep(20000000);
		
		persistentCacheManager.close();


	 
	}
}
