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
 package fr.amapj.model.engine.tools;

import java.util.ArrayList;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.MethodCallUtil;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.service.services.appinstance.AppState;



/**
 * Utilitaires pour les autres bases
 * 
 */
public class SpecificDbUtils 
{
	/**
	 * Permet d'executer une requete dans une base specifique
	 *
	 * Si une erreur apparait, alors l'exception est rejetée dans une RuntimeException 
	 * 
	 * ATTENTION : ceci doit être executé / appelé  depuis du code en dehors de toute transaction !! 
	 * 
	 * @param deamonName
	 * @param deamon
	 */
	static public <RESULT> RESULT executeInSpecificDb(String dbName,SpecificDbImpl<RESULT> deamon)
	{
		MethodCallUtil<RESULT, DataBaseInfo> ret = executeInSpecificDbNoException(dbName, deamon);
		if (ret.throwable!=null)
		{
			throw new AmapjRuntimeException("Une erreur est survenue dans la base <"+dbName+">",ret.throwable);
		}
		
		return ret.result;
	}
	
	static public <RESULT> RESULT executeInMaster(SpecificDbImpl<RESULT> deamon)
	{
		return executeInSpecificDb(DbManager.get().getMasterDb().dbName,deamon);
	}
	
	
	/**
	 * Permet d'executer une requete dans une base specifique
	 *
	 * Si une erreur apparait, alors l'exception est stockée dans le retour 
	 * 
	 * ATTENTION : ceci doit être executé / appelé  depuis du code en dehors de toute transaction !! 
	 * 
	 * @param deamonName
	 * @param deamon
	 */
	static public <RESULT> MethodCallUtil<RESULT, DataBaseInfo> executeInSpecificDbNoException(String dbName,SpecificDbImpl<RESULT> deamon)
	{
		MethodCallUtil<RESULT, DataBaseInfo> ret = new MethodCallUtil<RESULT, DataBaseInfo>();
		
		
		// Recherche de la base de données
		DataBaseInfo dataBaseInfo = DbManager.get().findDataBaseFromName(dbName);
		ret.context = dataBaseInfo;
		if (dataBaseInfo==null || dataBaseInfo.getState()!=AppState.ON)
		{
			ret.throwable = new AmapjRuntimeException("La base <"+dbName+"> est inconnue ou non active");
			return ret;
		}
		
		// Execution du code dans la base indiquée 
		RESULT res = null;
		Throwable throwable = null;
	
		DbManager.get().setDbForDeamonThread(dataBaseInfo);
		try
		{
			res = deamon.perform();
		}
		catch(Throwable t)
		{
			throwable = t;
			
		}
		DbManager.get().setDbForDeamonThread(null);
		
		
		//
		ret.result = res;
		ret.throwable = throwable;
		
		return ret;
	}
	
	
	
	/**
	 * Permet d'executer un morceau de code dans toutes les bases ACTIVES 
	 * 
	 * ATTENTION : ceci doit être executé / appelé  depuis du code en dehors de toute transaction !! 
	 * 
	 * Similaire à DeamonsUtils
	 * 
	 * @param continueOnFail  si false : si une erreur apparait, alors le traitement est stoppé et une exception est jetée 
	 * 						  si true : le traitement continue et l'exception est conservée dans le résultat 
	 */
	static public <RESULT> List<MethodCallUtil<RESULT, DataBaseInfo>> executeInAllDb(SpecificDbImpl<RESULT> deamon,boolean continueOnFail)
	{
		List<MethodCallUtil<RESULT, DataBaseInfo>> res =new ArrayList<MethodCallUtil<RESULT, DataBaseInfo>>();
		
		// On fait une copie de la liste des bases : on ne veut pas tenir compte des ajouts enventuels de base
		// avant la fin du déroulement complet du démon
		List<DataBaseInfo> dataBaseInfos = new ArrayList<DataBaseInfo>(DbManager.get().getAllDbs());
		for (DataBaseInfo dataBaseInfo : dataBaseInfos)
		{
			if (dataBaseInfo.getState()==AppState.ON)
			{
				MethodCallUtil<RESULT, DataBaseInfo> result = executeInSpecificDbNoException(dataBaseInfo.getDbName(),deamon);
				
				if ((continueOnFail==false) && (result.throwable!=null))
				{
					throw new AmapjRuntimeException("Une erreur est survenue dans la base -"+dataBaseInfo.getDbName()+"-",result.throwable);
				}
				
				res.add(result);
			}
		}
		return res;
	}
	
	
	
	
	
}
