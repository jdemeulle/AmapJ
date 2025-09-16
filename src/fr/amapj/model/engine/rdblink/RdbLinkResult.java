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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

import javax.persistence.Query;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.SQLUtils;

/**
 *
 */
public class RdbLinkResult 
{
	private Query query;

	public RdbLinkResult(Query query) 
	{
		this.query = query;

	}

	public <DB> List<DB> list(Class<DB> resultClass) 
	{
		return query.getResultList();
	}

	public int size() 
	{
		return query.getResultList().size();
	}

	public <DB, DTO> List<DTO> listConverted(Class<DB> dbClass, Function<DB, DTO> toDTO) 
	{
		List<DB> toConvert = query.getResultList();
		List<DTO> DTOList = new ArrayList<>();
		for (DB db : toConvert) 
		{
			DTOList.add(toDTO.apply(db));
		}
		return DTOList;
	}
	
	public <DB> DB single(Class<DB> clazz) 
	{
		return (DB) query.getSingleResult();
	}
	
	public int singleInt()
	{
		return SQLUtils.toInt(query.getSingleResult());
	}
	
	public long singleLong()
	{
		return SQLUtils.toInt(query.getSingleResult());
	}


	public <DB> DB oneOrZero(Class<DB> clazz)
	{
		List<DB> ls = query.getResultList();
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
	

	public <DB> DB one(Class<DB> clazz)
	{
		List<DB> ls = query.getResultList();
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

	/**
	 * Si la liste resultat contient 0 élement : retourne null
	 * Si non :
	 * retourne chaque element de la liste séparé par un , en appliquant toString à chaque élement  
	 */
	public String asStringOrNull() 
	{
		List ls = query.getResultList();
		if (ls.size()==0)
		{
			return null;
		}
		
		return CollectionUtils.asString(ls, ",");
	}

	
}
