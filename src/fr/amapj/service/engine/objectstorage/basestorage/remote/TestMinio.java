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

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;


/**
 * Exemple d'utilisation de minio : 
 * 
 * https://github.com/minio/minio-java/blob/release/examples/GetObject.java 
 * 
 *
 */
public class TestMinio
{
	public static void main(String[] args) throws Exception
	{
		
		MinioClient m = MinioClient.builder()
	              .endpoint("http://127.0.0.1:9000/")
	              .credentials("s3login", "s3password")
	              .build();
		
		/*
		// Get information of an object.
        ObjectStat objectStat = m.statObject(StatObjectArgs.builder().bucket("abucket").object("amap1/").build());
        System.out.println(objectStat);
        */
		
		// Permet d'obtenir en une seule requete la liste de tous les objets dans le repertoire amap2
		// Ceci ce traduit par une seule requete HTTP 
	    Iterable<Result<Item>> results = m.listObjects(ListObjectsArgs.builder()
	                    .bucket("abucket")
	                    .prefix("amap2")
	                    .recursive(true)	               
	                    .build());

	        for (Result<Item> result : results) 
	        {
	          Item item = result.get();
	          System.out.println(/*item.lastModified() +*/ "\t" + item.size() + "\t" + item.objectName());
	        }
	}
}
