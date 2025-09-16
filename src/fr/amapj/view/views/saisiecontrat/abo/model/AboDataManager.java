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
 package fr.amapj.view.views.saisiecontrat.abo.model;

import com.google.gson.Gson;

import fr.amapj.common.DateUtils;
import fr.amapj.common.GzipUtils;
import fr.amapj.service.services.mescontrats.ContratLigDTO.AboLigStatus;

/**
 * Données concernant les contrats de type abonnement et qui seront serialisés en base au format GSON
 * 
 * Attention : faire des modifs avec précaution ! 
 *
 */
public class AboDataManager
{
	public static String toString(AboData aboData)
	{
		return GzipUtils.compress(new Gson().toJson(aboData));
	}
	
	public static AboData fromString(String in)
	{
		in = GzipUtils.uncompress(in);
		return new Gson().fromJson(in, AboData.class);
	}
	
	public static void main(String[] args)
	{
		AboData aboData  = new AboData();
		
		AboDataLig lig = new AboDataLig();
		lig.date = DateUtils.getDateWithNoTime();
		lig.status = AboLigStatus.FORCED_TO_0;
		aboData.ligs.add(lig);
		
		lig = new AboDataLig();
		lig.date = DateUtils.addDays(DateUtils.getDateWithNoTime(),2);
		lig.status = AboLigStatus.JOKER;
		aboData.ligs.add(lig);
		
		String str1 = new Gson().toJson(aboData);
		System.out.println(str1);
		
		String str2 = toString(aboData);
		System.out.println(str2);
		
		AboData aboData2 = fromString(str2);
		
		System.out.println(aboData2.ligs.size());
		System.out.println(aboData2.ligs.get(0).date);
		System.out.println(aboData2.ligs.get(0).status);
		
	}

}
