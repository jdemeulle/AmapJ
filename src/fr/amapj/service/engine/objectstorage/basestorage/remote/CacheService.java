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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Map.Entry;

import org.ehcache.Cache;
import org.ehcache.PersistentCacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.core.statistics.CacheStatistics;
import org.ehcache.core.statistics.TierStatistics;

import fr.amapj.view.engine.ui.AppConfiguration;

public class CacheService 
{
	private PersistentCacheManager persistentCacheManager;
	
	private StatisticsService statisticsService;
	
	private Cache<String, byte[]> localCache;

	public CacheService(AppConfiguration app) 
	{
		statisticsService = new DefaultStatisticsService();
		   
		persistentCacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.using(statisticsService)
				.with(CacheManagerBuilder.persistence(new File(app.getLocalCacheDir()))) 
				.withCache("s3", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
							ResourcePoolsBuilder.newResourcePoolsBuilder().disk(Long.parseLong(app.getLocalCacheSize()), MemoryUnit.MB, true)) 
		  )
		  .build(true);
		
		localCache =   persistentCacheManager.getCache("s3", String.class, byte[].class); 
		
	}

	public void save(String key, byte[] content) 
	{
		localCache.put(key, content);
	}


	public byte[] get(String key) 
	{
		byte[] res = localCache.get(key);
		if (res==null)
		{
			return null;
		}
		return res;
	}

	public void remove(String key) 
	{
		localCache.remove(key);
	}
	
	public void close() 
	{
		persistentCacheManager.close();
	}

	public String getStatistic() 
	{
		CacheStatistics s = statisticsService.getCacheStatistics("s3");
		String res = 	"Cache hits 	= "+s.getCacheHits()+" ("+( (int) s.getCacheHitPercentage())+" %)<br/>"+
						"Cache miss 	= "+s.getCacheMisses()+" ("+( (int) s.getCacheMissPercentage())+" %)<br/>"+
						"Cache gets 	= "+s.getCacheGets()+"<br/>"+
						"Cache puts 	= "+s.getCachePuts()+"<br/>"+
						"Cache removals = "+s.getCacheRemovals()+"<br/>"+
						"Cache evictions= "+s.getCacheEvictions()+"<br/>"+
						"Cache expirations= "+s.getCacheExpirations()+"<br/>"; 
		
		for (Entry<String, TierStatistics> tiers : s.getTierStatistics().entrySet()) 
		{
			String tierName = tiers.getKey();
			TierStatistics ts = tiers.getValue();
			
			res= res+ 	"<br/>"+
						"Tier name 			= "+tierName+"<br/>"+
						"Nb Elements	 	= "+ts.getMappings()+"<br/>"+
						"AllocatedByteSize 	= "+ts.getAllocatedByteSize()+"<br/>"+
						"OccupiedByteSize	= "+ts.getOccupiedByteSize()+"<br/>";
		}
		return res;
	}

}
