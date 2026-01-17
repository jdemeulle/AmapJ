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
 package fr.amapj.common;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import fr.amapj.model.engine.rdblink.RdbLink;



public class SQLUtils
{
	
	/**
	 * Permet de gérer le retour de la méthode 
	 * Query.getSingleResult()
	 * 
	 */
	public static int toInt(Object o)
	{
		if (o==null)
		{
			return 0;
		}
		if (o instanceof BigDecimal)
		{
			return ( (BigDecimal) o).intValue();	
		}
		
		if (o instanceof Long)
		{
			return ( (Long) o).intValue();	
		}

		if (o instanceof Integer)
		{
			return ( (Integer) o).intValue();	
		}
		
		throw new AmapjRuntimeException("Type inconnu:"+o.getClass());
		
	}
	
	
	/**
	 * Permet de gérer le retour de la méthode 
	 * Query.getSingleResult()
	 * 
	 */
	public static long toLong(Object o)
	{
		if (o==null)
		{
			return 0;
		}
		if (o instanceof BigDecimal)
		{
			return ( (BigDecimal) o).longValue();
		}
		
		if (o instanceof Long)
		{
			return ( (Long) o).longValue();
		}

		if (o instanceof Integer)
		{
			return ( (Integer) o).longValue();
		}
		
		throw new AmapjRuntimeException("Type inconnu:"+o.getClass());
		
	}
	
	
	/**
	 * Permet de réaliser un count
	 * 
	 */
	public static long count(Query q)
	{
		return toLong(q.getSingleResult());
	}
	
	
	/**
	 * Permet de supprimer tous les éléments retournés par cette requête
	 * 
	 * Retourne le nombre d'elements supprimés
	 */
	public static int deleteAll(RdbLink em,Query q)
	{
		List  mces = q.getResultList();
		
		for (Object mce : mces)
		{
			em.remove(mce);
		}
		
		return mces.size();
	}


	public static <T> T oneOrZero(TypedQuery<T> q)
	{
		List<T> ls = q.getResultList();
		if (ls.size()==0)
		{
			return null;
		}
		else if (ls.size()==1)
		{
			return ls.get(0);
		} 
		else
		{
			throw new AmapjRuntimeException("ls size = "+ls.size());
		}
	}
	
	
	public static <T> T one(TypedQuery<T> q)
	{
		List<T> ls = q.getResultList();
		if (ls.size()==0)
		{
			throw new AmapjRuntimeException("size = 0");
		}
		else if (ls.size()==1)
		{
			return ls.get(0);
		} 
		else
		{
			throw new AmapjRuntimeException("ls size = "+ls.size());
		}
	}
	
	
	
}
