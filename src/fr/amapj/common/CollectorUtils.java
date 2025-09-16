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

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorUtils
{
	/**
	 * Permet de construire un collector qui retoune :
	 * -> null si il y a aucun element
	 * -> l'élement si il y a un seul élement 
	 * -> jette une excecption sinon 
	 */
	public static <T> Collector<T, ?, T> oneOrZero()
	{
		return Collectors.collectingAndThen(Collectors.toList(), list -> 
		{
			if (list.size() == 0)
			{
				return null;
			}
			else if (list.size() == 1)
			{
				return list.get(0);	
			}
			else
			{
				throw new RuntimeException("Plusieurs elements :"+list.size());
			}
			
		});
	}
	
	
	/**
	 * Permet de construire un collector qui retoune :
	 * 
	 * -> l'élement si il y a un seul élement 
	 * -> jette une exception sinon dans tous les autres cas  
	 */
	public static <T> Collector<T, ?, T> one()
	{
		return Collectors.collectingAndThen(Collectors.toList(), list -> 
		{
			if (list.size() == 1)
			{
				return list.get(0);	
			}
			else
			{
				throw new RuntimeException("0 ou plusieurs elements :"+list.size());
			}
			
		});
	}
	
	
	/**
	 * si delim = ; 
	 * 
	 * Permet de transformer la liste "a" "b" "c" en a;b;c;
	 * 
	 * 
	 */
	public static <T> Collector<T, ?, T> join(String delim)
	{
		return (Collector<T, ?, T>) Collectors.joining(delim, "", delim);
	}
}
