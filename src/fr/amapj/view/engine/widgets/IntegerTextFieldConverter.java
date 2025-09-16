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
 package fr.amapj.view.engine.widgets;

import java.util.Locale;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;

public class IntegerTextFieldConverter implements Converter<String,Integer>
{


	public IntegerTextFieldConverter()
	{
	}

	public Integer convertToModel(String str, Class<? extends Integer> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException
	{
		if (str!=null)
		{			
			// Suppression des espaces
			str = str.trim();
			
			if (str.length()==0)
			{
				return new Integer(0);
			}
			
			try
			{
				Integer res =  new Integer(str);
				return res;
			}
			catch (NumberFormatException e) 
			{
				throw new ConversionException("Valeur incorrecte : "+str);
			}
		}
		return null;
	}

	public String convertToPresentation(Integer value, Class<? extends String> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException
	{
		if (value != null)
		{
			return value.toString();
		}
		return null;
	}

	public Class<Integer> getModelType()
	{
		return Integer.class;
	}

	public Class<String> getPresentationType()
	{
		return String.class;
	}

	

}
