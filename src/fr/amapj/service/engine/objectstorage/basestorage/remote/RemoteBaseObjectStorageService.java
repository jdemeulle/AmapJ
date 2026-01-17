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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.service.engine.objectstorage.basestorage.BaseStorageService;
import fr.amapj.view.engine.ui.AppConfiguration;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;


/**
 * Permet le stockage sur un stockage S3 distant 
 * 
 * Un cache local est mis en place avec EhCache pour limiter les accès distants
 *
 */
public class RemoteBaseObjectStorageService implements BaseStorageService
{
	private String s3StorageBucket = null;
	private String s3StorageKeyPrefix = null;
	
	private MinioClient minioClient;
	
	private CacheService cacheService;
	private S3UseStatistic s3Stats;
	
	private final static Logger logger = LogManager.getLogger();
		
	public RemoteBaseObjectStorageService(AppConfiguration app)
	{
		s3StorageBucket = app.getS3StorageBucket();
		s3StorageKeyPrefix = app.getS3StorageKeyPrefix();
		
		minioClient = MinioClient.builder()
			              .endpoint(app.getS3StorageEndPoint())
			              .credentials(app.getS3StorageLogin(), app.getS3StoragePassword())
			              .build();
		
		cacheService = new CacheService(app);
		s3Stats = new S3UseStatistic();
	}
	
	@Override
	public void saveContent(ObjectStorage initial,byte[] content)
	{	
		String key = createKey(initial.id);
		
		// Etape 1 - Sauvegarde dans le S3
		logger.info("Sauvegarde S3Storage - key ="+key);
        try
		{
        	ByteArrayInputStream bais = new ByteArrayInputStream(content);

        	// Ceci permet d'empecher l'écrasement d'un fichier existant 
        	// En effet, chaque envoi vers le S3 se fait avec une clé différente, on ne doit jamais écraser un fichier existant
        	// Voir https://stackoverflow.com/questions/12654828/amazon-s3-avoid-overwriting-objects-with-the-same-name
        	Map<String, String> headers = new HashMap<>();
        	headers.put("If-None-Match","*");

        	//
			minioClient.putObject(PutObjectArgs.builder().bucket(s3StorageBucket).extraHeaders(headers).object(key).stream(bais, bais.available(), -1).build());
			bais.close();
		} 
        catch (Exception e) 
        {
			throw new AmapjRuntimeException(e);
		}
        s3Stats.incrPut(content.length);
        
        // Etape 2 - Sauvegarde dans le cache 
        logger.info("Sauvegarde Cache - key ="+key);
        cacheService.save(key,content);
	}
	
	@Override
	public byte[] loadContent(ObjectStorage initial)  throws Exception
	{
		String key = createKey(initial.id);
		
		// Etape 1 - Recherche dans le cache local 
		logger.info("Lecture Cache - key ="+key);
		byte[] fromCache = cacheService.get(key);
		if (fromCache!=null)
		{
			return fromCache;
		}
		
		// Etape 2 - Recherche dans S3
		logger.info("Lecture S3Storage - key ="+key);
		InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(s3StorageBucket).object(key).build());
		byte[] bs = IOUtils.toByteArray(stream);
		s3Stats.incrGet(bs.length);
		
		//Etape 3 - Sauvegarde dans le cache local 
		logger.info("Sauvegarde Cache - key ="+key);
		cacheService.save(key, bs);
		
		//
		return bs;
	}

	/**
	 * Supprime le contenu de l'objet 
	 */
	@Override
	public void deleteContent(Long idObjectStorage)
	{
		String key = createKey(idObjectStorage);
		
		//Etape 1 - Suppression dans S3
		logger.info("Suppression S3Storage - key ="+key);
		try
		{
			minioClient.removeObject(RemoveObjectArgs.builder().bucket(s3StorageBucket).object(key).build());	
		} 
	    catch (Exception e) 
	    {
			throw new AmapjRuntimeException(e);
		}
		s3Stats.incrDelete();
		
		// Etape 2 - Suppression dans le cache local 
		logger.info("Suppression Cache - key ="+key);
		cacheService.remove(key);
	}
	
	@Override
	public void close()
	{
		cacheService.close();
	}
	
	private String createKey(Long id) 
	{
		return s3StorageKeyPrefix+"/"+DbManager.get().getCurrentDb().getDbName()+"/os/"+(id%100)+"/"+id;
	}
	
	
	// PARTIE TECHNIQUE 
	
	public String getStatistic()
	{
		return s3Stats.getStatistics()+cacheService.getStatistic();
	}
	

	/**
	 * Passage en V042 - A supprimer ensuite 
	 */
	public byte[] onlyForPatchLoadWebPageAsHtml(Long id) 
	{
		try
		{
			String key = s3StorageKeyPrefix+"/"+DbManager.get().getCurrentDb().getDbName()+"/webpages/"+id;
			
			InputStream stream = minioClient.getObject(GetObjectArgs.builder().bucket(s3StorageBucket).object(key).build());
			
			stream = new GZIPInputStream(stream);
			byte[] res = IOUtils.toByteArray(stream);
			stream.close();
			return res;
		}
		catch (Exception e) 
        {
			throw new AmapjRuntimeException(e);
		}
	}
	

	
}
