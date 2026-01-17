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
 package fr.amapj.service.engine.objectstorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.NewTransaction;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.model.models.objectstorage.ObjectStorageState;
import fr.amapj.service.engine.objectstorage.basestorage.BaseStorageService;


/**
 *  Voir /docs/tech-notes/dev/objectstorage/objectstorage.txt
 */
public class ObjectStorageService 
{
	private BaseStorageService baseStorageService;


	public ObjectStorageService(BaseStorageService baseStorageService)
	{
		this.baseStorageService = baseStorageService;
	}
	
	/**
	 * Retourne le contenu de cet objet 
	 * 
	 * Retourne null si initial est null 
	 */
	public byte[] get(ObjectStorage initial)
	{
		if (initial==null)
		{
			return null;
		}
		
		try
		{
			byte[] bs = baseStorageService.loadContent(initial);
			InputStream stream = new ByteArrayInputStream(bs);
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

	
	
	
	/** 
	 * Positionne le contenu de cet objet 
	 * 
	 * Si content est null, alors l'objet est supprimé 
	 */
	public ObjectStorage set(RdbLink em,ObjectStorage initial,byte[] content)
	{
		// On supprime l'élement précédent 
		if (initial!=null)
		{
			initial.state = ObjectStorageState.DELETED;
		}
		
		// Pas de nouvel objet à créer, on s'arrete ici 
		if (content==null)
		{
			return null; 
		}
		
		// On crée un nouvel ObjectStorage dans la transaction courante
		ObjectStorage finalObject = new ObjectStorage();
		em.persist(finalObject);

		// Compression du contenu
		ByteArrayOutputStream baos;
		try
		{
			baos = new ByteArrayOutputStream();
			GZIPOutputStream os = new GZIPOutputStream(baos);
			os.write(content);
			os.close();
		}
		catch (IOException e) 
		{
			throw new AmapjRuntimeException(e);
		}

		// Stockage réel du contenu compressé
		baseStorageService.saveContent(finalObject, baos.toByteArray());
       	
       	return finalObject;
	}
	
	
	/**
	 * Cette méthode doit être appelée uniquement par le démon 
	 * d'effacement des données 
	 */
	public void forDeamonDeleteObjectDefinitively(Long idObjectStorage)
	{
		NewTransaction.write(em->
		{ 
			// On efface d'abord le contenu, sans faire aucune requete sur la base, pour eviter de poser un lock sur la table 
			baseStorageService.deleteContent(idObjectStorage);
			
			// On efface ensuite l'objet en base de données 
			ObjectStorage os = em.find(ObjectStorage.class, idObjectStorage);
			em.remove(os);
		});	
	}
	
	/**
	 * Retourne le nombre total d'élements dans la table ObjectStorage
	 */
	@DbRead
	public int countAllObjectStorage()
	{
		RdbLink em = RdbLink.get();
		em.createQuery("select count(a) from ObjectStorage a");	
		return em.result().singleInt();
			
	}

	/**
	 * Permet la liberation des ressources
	 */
	public void close() 
	{
		baseStorageService.close();
	}
	
	
	public String getStatistics()
	{
		return baseStorageService.getStatistic();
	}
	
}
