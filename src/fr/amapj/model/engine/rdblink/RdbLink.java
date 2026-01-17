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
 package fr.amapj.model.engine.rdblink;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.engine.transaction.TransactionHelper;



/**
 * Classe permettant de faire le lien avec la base de données 
 */
public class RdbLink 
{
	private EntityManager em;
	
	private Query q;
	
	public RdbLink(EntityManager em)
	{
		this.em = em;
	}
	
	public static RdbLink get() 
	{
		return TransactionHelper.get();
	}

    /**
     */
    public void persist(Object entity)
    {
    	checkInWriteTransaction();
    	em.persist(entity);
    }
  
    // Verification si on est bien dans une transaction en ecriture
    private void checkInWriteTransaction()
	{
		if (em.getTransaction().isActive()==false)
		{
			throw new AmapjRuntimeException("Tentative d'écriture dans une transaction en lecture");
		}
	}

	/**
     */    
    public void remove(Object entity)
    {
    	checkInWriteTransaction();
    	em.remove(entity);
    }
    
    
    /**
     * Supprime cet object, si entity est null, ne fait rien (pas d'erreur)  
     */    
    public void removeOrNull(Object entity)
    {
    	if (entity!=null)
    	{
    		checkInWriteTransaction();
    		em.remove(entity);
    	}
    }
    
    
    
    /**
     */
    public <T> T find(Class<T> entityClass, Object primaryKey)
    {
    	return em.find(entityClass, primaryKey);
    }
    
    /**
     * Retourne null si id est null, sinon retourne l'object referencé par cet identifiant 
     */
    public <T> T findOrNull(Class<T> entityClass, Long id)
    {
    	if (id==null)
    	{
    		return null;
    	}
    	return em.find(entityClass, id);
    }
    
   
    /**
     */
    public Query createQuery(String qlString)
    {
    	q = em.createQuery(qlString);
    	return q;
    }

    /**
     */
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery)
    {
    	TypedQuery<T> tq = em.createQuery(criteriaQuery);
    	q = tq;
    	return tq;
    }

   
   
    /**
     */
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass)
    {
    	TypedQuery<T> tq = em.createQuery(qlString,resultClass);
    	q = tq;
    	return tq;
    }

   

    /**
     */
    public Query createNativeQuery(String sqlString)
    {
    	q = em.createNativeQuery(sqlString);
    	return q;
    }

    
    /**
  
     */
    public CriteriaBuilder getCriteriaBuilder()
    {
    	return em.getCriteriaBuilder();
    }
    
    
    // FONCTIONS SPECIFIQUES AU POSITIONNEMENT DES PARAMETRES DU QUERY 
    
   public void setParameter(String name, Object value)
   {
	   q.setParameter(name, value);
   }
   
   
   // FONCTIONS SPECIFIQUES A LA RECUPERATION DES RESULTATS
   
   
   /**
    * Cette méthode peut être appelée une seule fois 
    */
   public RdbLinkResult result()
   {
	   Query query = q;
	   q = null;
	   return new RdbLinkResult(query);
   }  
 
	
	// CAS SPECIFIQUES 
	
	/**
	 * Doit être utilisé que dans des cas très rares
	 */
	public EntityManager getEm()
	{
		return em;
	}
	
}
