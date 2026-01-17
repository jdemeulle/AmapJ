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
 package fr.amapj.model.engine.dbms;

import java.sql.SQLException;
import java.util.List;

import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.service.services.appinstance.AppInstanceDTO;

public interface DBMS
{

	/**
	 * Réalise l'initialisation du DBMS , si nécessaire
	 */
	public void startDBMS();

	/**
	 * Permet le démarrage d'une base de données
	 * 
	 */
	public void startOneBase(DataBaseInfo dataBaseInfo);

	/**
	 * Permet l'arret d'une base de données
	 * 
	 */
	public void stopOneBase(DataBaseInfo dataBaseInfo);

	/**
	 * Permet l'arret du DBMS  
	 * @param dataBaseInfos 
	 */
	public void stopDBMS(List<DataBaseInfo> dataBaseInfos);
	
	
	/**
	 * Permet la création d'une nouvelle base de données
	 * 
	 * Cette méthode doit :
	 * - créer la base 
	 * - la démarrer 
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * 
	 */
	public void createOneBase(DataBaseInfo dataBaseInfo);
	
	
	/**
	 * Cette méthode permet la restauration d'une base de donnée à partir d'une sauvegarde
	 * 
	 * Cette méthode doit :
	 * - copier les fichiers 
	 * - démarrer la base  
	 * - l'enregistrer au niveau de DbUtil
	 * - la modifier (on efface les informations pour l'envoi des mails par sécurité) 
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * @param backupImporter 
	 * 
	 */
	// public void restoreOneBase(AppInstanceDTO dto, BackupImporter backupImporter);
	
	
	/**
	 * Cette méthode permet de remplacer une base de donnée par une autre 
	 * à partir du contenu des deux fichiers
	 * 
	 * Cette méthode doit :
	 * - arreter la base existante 
	 * - copier les fichiers 
	 * - redémarrer la base  
	 * - la modifier (on efface les informations pour l'envoi des mails par sécurité) 
	 * 
	 * En fin de cette méthode , la base est à l'état démarrée 
	 * 
	 */
	public void replaceOneBase(DataBaseInfo dataBaseInfo, byte[] fileProperties,byte[] fileScript);
	
	
	/**
	 * Permet l'execution d'une requete SQL d'update ou d'insert ou de modification du schéma sur la base indiquée
	 * Retourne le nombre de lignes modifiées
	 */
	public int executeUpdateSqlCommand(DataBaseInfo dataBaseInfo,String sqlCommand) throws SQLException;
	
	/**
	 * Permet l'execution d'une requete SQL de requete (SELECT ...)
	 * Retourne le resultat de la requete
	 */
	public List<List<String>> executeQuerySqlCommand(DataBaseInfo dataBaseInfo,String sqlCommand) throws SQLException;

	/**
	 * 
	 * @param dbName
	 * @param user
	 * @param password
	 * @return
	 */
	public String[] computeUrlUserPassword(String dbName, String user, String password);

	


}