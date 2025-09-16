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
 package fr.amapj.model.engine.metadata;

import java.util.ArrayList;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;


/**
 * Permet de gerer les meta data des Enum 
 *
 */
abstract public class MetaDataEnum
{
	private HelpInfo helpInfo = new HelpInfo();
	
	
	/**
	 * Permet de déclarer le contenu de l'aide pour cette combo box
	 */
	abstract public void fill();
	
	
	/**
	 * Ajout d'un texte 
	 */
	public void add(String texte)
	{
		add(null,texte,null);
	}
	
	public void addLink(String texte,String textLink,String link)
	{
		add(createLink(texte, textLink,link));
	}
	
	public void addLink(String textLink,String link)
	{
		add(createLink(textLink,link));
	}
	
	public void addBrLink(String textLink,String link)
	{
		add("<br/>"+createLink(textLink,link));
	}
	
	static public String createLink(String texte,String textLink,String link)
	{
		return texte+"<a target=\"_blank\" href=\"https://amapj.fr/"+link+"\">"+textLink+"</a><br/>";
	}
	
	static public String createLink(String textLink,String link)
	{
		String texte = "Vous trouverez ici une documentation complète sur : "; 
		return createLink(texte,textLink,link);
	}
	
	
	
	
	/**
	 * Texte pour chaque élément de l'énumération 
	 */
	public void add(Enum en,String lib)
	{
		add(en, lib, null);
	}
	
	/**
	 * Texte pour chaque élément de l'énumération 
	 */
	public void add(Enum en,String lib, String aide)
	{
		helpInfo.add(en, lib, aide);
	}
	
	public HelpInfo getHelpInfo()
	{
		return helpInfo;
	}

	
	static public class HelpInfo
	{
		List<HelpInfoEntry> libs = new ArrayList<>();
		
		public void add(Enum en,String lib,String aide)
		{
			HelpInfoEntry e = new HelpInfoEntry();
			e.en = en;
			e.lib = lib;
			e.aide = aide;
			libs.add(e);
		}
		
		public String getLib(Enum en)
		{
			String lib = null;
			for (HelpInfoEntry entry : libs) 
			{
				if (entry.en==en)
				{
					lib = entry.lib;
				}
			}
			
			if (lib==null)
			{
				lib = en.toString();
			}
			return lib;
		}
		
		public String getAide(Enum en)
		{
			for (HelpInfoEntry entry : libs) 
			{
				if (entry.en==en)
				{
					return entry.aide;
				}
			}
			return "";
		}
		
		
		public String getFullText(Enum[] enumsToExcludes)
		{
			String fullText = "";
			for (HelpInfoEntry entry : libs) 
			{
				if (entry.en==null)
				{
					fullText = fullText+entry.lib+"<br/>";
				}
				else
				{
					if (isPresent(entry.en,enumsToExcludes))
					{
						if (entry.aide!=null)
						{
							fullText = fullText+"<br/><b>"+entry.lib+"</b><br/>"+entry.aide+"<br/>";
						}
						else
						{
							fullText = fullText+"<br/><b>"+entry.lib+"</b><br/>";
						}	
					}
				}
				
			}
			return fullText;
		}

		private boolean isPresent(Enum en, Enum[] enumsToExcludes) 
		{
			if (enumsToExcludes==null)
			{
				return true;
			}
			for (Enum en1 : enumsToExcludes) 
			{
				if (en1==en)
				{
					return false;
				}
			}
			return true;
		}
	}
	
	static private class HelpInfoEntry
	{
		private Enum en;
		private String lib;
		private String aide;
	}
	
	
	/**
	 * Permet de construire les informations pour l'aide à partir de la classe d'un enum, contenant lui meme 
	 * une sous classe implementant AbstractEnumHelp
	 * 
	 * Retourne null si il n'y a pas une sous classe implementant AbstractEnumHelp
	 */
	static public HelpInfo getHelpInfo(Class en)
	{
		try
		{
			Class[] cls = en.getDeclaredClasses();
			for (Class cl : cls)
			{
				if (MetaDataEnum.class.isAssignableFrom(cl))
				{
					MetaDataEnum enumHelp = ( (MetaDataEnum) cl.newInstance());
					enumHelp.fill();
					return enumHelp.getHelpInfo();
				}
			}
		} 
		catch (SecurityException | InstantiationException | IllegalAccessException e)
		{
			throw new AmapjRuntimeException(e);
		}
		return null;
	}
	
	
}
