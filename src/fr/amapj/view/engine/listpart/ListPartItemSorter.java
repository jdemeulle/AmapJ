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
 package fr.amapj.view.engine.listpart;

import java.text.Collator;

import com.vaadin.data.util.DefaultItemSorter;

public class ListPartItemSorter extends DefaultItemSorter 
{
	public ListPartItemSorter()
	{
		super(new ListPartPropertyValueComparator());
	}

	static public class ListPartPropertyValueComparator extends DefaultPropertyValueComparator 
	{
		@Override
		public int compare(Object o1, Object o2) 
		{
			if (o1 instanceof String && o2 instanceof String)
			{
				return Collator.getInstance().compare(o1, o2);
			}
			
			return super.compare(o1, o2);
		}
	}
}
