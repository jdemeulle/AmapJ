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
 package fr.amapj.service.services.appinstance;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectionUtils;
import fr.amapj.common.DateUtils;
import fr.amapj.common.SQLUtils;
import fr.amapj.common.StringUtils;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.engine.tools.SpecificDbUtils;
import fr.amapj.model.engine.transaction.DataBaseInfo;
import fr.amapj.model.engine.transaction.DbRead;
import fr.amapj.model.engine.transaction.DbWrite;
import fr.amapj.model.engine.transaction.NewTransaction;
import fr.amapj.model.models.fichierbase.EtatUtilisateur;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.model.models.saas.AppInstance;
import fr.amapj.model.models.saas.StateOnStart;
import fr.amapj.service.services.appinstance.SchemaDbDTO.DbColumnDTO;
import fr.amapj.service.services.appinstance.SchemaDbDTO.DbForeignKeyDTO;
import fr.amapj.service.services.appinstance.SchemaDbDTO.DbUniqKeyDTO;
import fr.amapj.service.services.appinstance.SchemaDbDTO.OneSchemaDTO;
import fr.amapj.service.services.appinstance.SqlRequestDTO.DataBaseResponseDTO;
import fr.amapj.service.services.appinstance.SqlRequestDTO.ResponseDTO;
import fr.amapj.service.services.appinstance.SqlRequestDTO.SqlType;
import fr.amapj.service.services.logview.LogViewService;
import fr.amapj.service.services.logview.StatInstanceDTO;
import fr.amapj.service.services.mailer.MailerCounter;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;
import fr.amapj.service.services.session.SessionManager;
import fr.amapj.service.services.suiviacces.ConnectedUserDTO;
import fr.amapj.service.services.utilisateur.UtilisateurDTO;
import fr.amapj.service.services.utilisateur.UtilisateurService;
import fr.amapj.view.engine.ui.AppConfiguration;
import fr.amapj.view.views.appinstance.BackupImporter;

/**
 * Permet la gestion des instances de l'application
 * 
 */
public class AppInstanceService
{
	
	private final static Logger logger = LogManager.getLogger();

	// PARTIE REQUETAGE POUR AVOIR LA LISTE DES INSTANCES

	/**
	 * Permet de charger la liste de tous les instances
	 */
	@DbRead
	public List<AppInstanceDTO> getAllInstances(boolean withMaster)
	{
		RdbLink em = RdbLink.get();	
		
		List<AppInstanceDTO> res = new ArrayList<>();

		Query q = em.createQuery("select a from AppInstance a ORDER BY a.nomInstance");

		List<AppInstance> ps = q.getResultList();
		List<ConnectedUserDTO> connected = SessionManager.getAllConnectedUser();
		for (AppInstance p : ps)
		{
			AppInstanceDTO dto = createAppInstanceDto(connected,p);
			res.add(dto);
		}
		
		// On ajoute ensuite la base master si besoin 
		if (withMaster)
		{
			AppInstanceDTO master = AppConfiguration.getConf().getMasterConf();
			addInfo(master,connected);
			res.add(master);
		}

		return res;

	}

	

	public AppInstanceDTO createAppInstanceDto(List<ConnectedUserDTO> connected, AppInstance a)
	{
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.id = a.getId();
		dto.nomInstance = a.nomInstance;
		dto.dateCreation = a.dateCreation;
		dto.dbUserName = a.dbUserName;
		dto.dbPassword = a.dbPassword;	
		dto.stateOnStart = a.stateOnStart;
		addInfo(dto, connected);
		return dto;
	}
	
	
	private void addInfo(AppInstanceDTO dto, List<ConnectedUserDTO> connected)
	{
		dto.state = getState(dto.getNomInstance());
		dto.nbUtilisateurs = getNbUtilisateurs(connected,dto.getNomInstance());
		dto.nbMails = MailerCounter.getNbMails(dto.getNomInstance());
		
	}


	private AppState getState(String nomInstance)
	{
		DataBaseInfo dataBaseInfo = DbManager.get().findDataBaseFromName(nomInstance);
		if (dataBaseInfo == null)
		{
			return AppState.OFF;
		}
		return dataBaseInfo.getState();
	}
	
	private int getNbUtilisateurs(List<ConnectedUserDTO> connected, String nomInstance)
	{
		int res = 0;
		for (ConnectedUserDTO connectedUserDTO : connected)
		{
			if ((connectedUserDTO.isLogged==true) && (StringUtils.equals(connectedUserDTO.dbName,nomInstance)) )
			{
				res++;
			}
		}
		return res;
	}

	
	// CREATION D'UNE INSTANCE
	
	/**
	 * Permet de créer une instance 
	 * 
	 * Attention : ne pas mettre d'annotation dbWrite ou dbRead ici !!!
	 * 
	 */
	public void create(AppInstanceDTO appInstanceDTO)
	{
		// On crée la base
		// Attention : ne pas créer de transaction ici, car on va ecrire dans la base de données fille
		DbManager.get().createDataBase(appInstanceDTO);
		
		// On crée ensuite en base de données
		// On crée la transaction seulement ici dans le master
		NewTransaction.writeInMaster(em->
			{
				AppInstance a = new AppInstance();

				a.nomInstance = appInstanceDTO.nomInstance;
				a.dateCreation = DateUtils.getDate();
				a.dbUserName = appInstanceDTO.dbUserName;
				a.dbPassword = appInstanceDTO.dbPassword;
				a.stateOnStart = appInstanceDTO.stateOnStart;
						
				em.persist(a);
			});
	}
	
	
	
	// RESTAURATION D'UNE INSTANCE A PARTIR D'UNE SAUVEGARDE
	
	/**
	 * Permet de restaurer une instance à partir d'une sauvegarde 
	 * 
	 * Attention : ne pas mettre d'annotation dbWrite ou dbRead ici !!!
	 * @param backupImporter 
	 * 
	 */
	public void restoreFromBackup(AppInstanceDTO appInstanceDTO, BackupImporter backupImporter)
	{
		/*
		// On vérifie que la base n'existe pas déjà
		if (DbUtil.findDataBaseFromName(appInstanceDTO.nomInstance)!=null)
		{
			throw new RuntimeException("La base existe déjà");
		}
		
		// On restaure la base
		// Attention : ne pas créer de transaction ici, car on va ecrire dans la base de données fille
		AppInitializer.dbManager.restoreDataBase(appInstanceDTO,backupImporter);
				
		// On crée ensuite en base de données
		// On crée la transaction seulement ici dans le master
		NewTransaction.writeInMaster(em->
			{
			
				AppInstance a = new AppInstance();

				a.nomInstance = appInstanceDTO.nomInstance;
				a.dateCreation = DateUtils.getDate();
				a.dbms = appInstanceDTO.dbms;
				a.dbUserName = appInstanceDTO.dbUserName;
				a.dbPassword = appInstanceDTO.dbPassword;
				a.stateOnStart = appInstanceDTO.stateOnStart;
						
				em.persist(a);
			});*/
	}	


	// PARTIE SUPPRESSION

	/**
	 * Permet de supprimer un instance Ceci est fait dans une transaction en ecriture
	 */
	@DbWrite
	public void delete(final Long id)
	{
		RdbLink em = RdbLink.get();

		AppInstance a = em.find(AppInstance.class, id);

		em.remove(a);
	}


	
	
	// PARTIE SUDO
	public List<SudoUtilisateurDTO> getSudoUtilisateurDto(AppInstanceDTO dto)
	{
		return SpecificDbUtils.executeInSpecificDb(dto.nomInstance, ()->getSudoUtilisateurDto());
	}

	public List<SudoUtilisateurDTO> getSudoUtilisateurDto()
	{
		List<SudoUtilisateurDTO> res = new ArrayList<SudoUtilisateurDTO>();
		ParametresDTO param = new ParametresService().getParametres();
		
		List<UtilisateurDTO> utilisateurDTOs =  new UtilisateurService().getAllUtilisateurs(null);
		for (UtilisateurDTO utilisateur : utilisateurDTOs)
		{
			SudoUtilisateurDTO dto = new SudoUtilisateurDTO();
			dto.id = utilisateur.getId();
			dto.nom = utilisateur.getNom();
			dto.prenom = utilisateur.getPrenom();
			dto.roles = utilisateur.roles;
			dto.baseUrl = param.getUrl();
			dto.emailUrl = "?username="+utilisateur.getEmail();
			res.add(dto);
		}
		
		// Tri pour avoir les administrateurs en premier, puis les tresoriers , puis par ordre alphabetique
		CollectionUtils.sort(res, e->!e.roles.contains("ADMIN"),e->!e.roles.contains("TRESORIER"),e->e.nom,e->e.prenom);	
		
		return res;
	}



	/**
	 * 
	 * 
	 * @param selected
	 * @param appInstanceDTOs
	 * @param ddlRequest si true, alors les requetes SQL sont des inserts, des updates ou des commandes DDL, sinon ce sont des requetes SQL de type SELECT  
	 */
	public void executeSqlRequest(SqlRequestDTO selected,List<AppInstanceDTO>  appInstanceDTOs)
	{	
		
		boolean ddlRequest = (selected.sqlType == SqlType.UPDATE_OR_INSERT_OR_DDL);
		
		// Chaque requete est executée dans une transaction indépendante
		// On s'arrête dès qu'une requete échoue 
		
		selected.success = false;
		
		for (AppInstanceDTO appInstanceDTO : appInstanceDTOs)
		{
			logger.info("Execution des requetes sur la base "+appInstanceDTO.nomInstance);
			DataBaseResponseDTO dataBaseResponseDTO = new DataBaseResponseDTO();
			dataBaseResponseDTO.success = false;
			dataBaseResponseDTO.dbName = appInstanceDTO.nomInstance;
			selected.responses.add(dataBaseResponseDTO);
			
			int index = 1;
			for (String request : selected.verifiedRequests)
			{
				ResponseDTO res = executeOneSqlRequest(request,appInstanceDTO,index,ddlRequest);
				dataBaseResponseDTO.responses.add(res);
				if (res.success==false)
				{
					return ;
				}
				index++;
			}
			dataBaseResponseDTO.success = true;
		}
		
		selected.success = true;
	}


	private ResponseDTO executeOneSqlRequest(String request, AppInstanceDTO dto,int index, boolean ddlRequest) 
	{
		ResponseDTO res = new ResponseDTO();
		res.index = index;
		res.sqlRequest = request;
		
		try
		{
			if (ddlRequest)
			{
				res.nbModifiedLines = DbManager.get().executeUpdateSqlCommand(request, dto);
			}
			else
			{
				res.sqlResultSet = DbManager.get().executeQuerySqlCommand(request, dto);
			}
			
			res.sqlResponse = "OK";
			res.success = true;	
		} 
		catch (SQLException e)
		{
			res.sqlResponse = "Erreur : "+ e.getMessage();
			res.success = false;	
		}
		
		return res;
	}

	/**
	 * Permet de sauvegarder toutes ces instances
	 * 
	 * @param appInstanceDTOs
	 * @return
	 */
	public List<String> saveInstance(List<AppInstanceDTO> appInstanceDTOs)
	{
		List<String> res = new ArrayList<String>();
		
		String backupDir = AppConfiguration.getConf().getBackupDirectory();
		if (backupDir==null)
		{
			throw new RuntimeException("Le répertoire de stockage des sauvegardes n'est pas défini");
		}
		
		for (AppInstanceDTO appInstanceDTO : appInstanceDTOs)
		{
			String msg = saveInstance(appInstanceDTO,backupDir);
			res.add(msg);
		}
		return res;
	}

	private String saveInstance(AppInstanceDTO appInstanceDTO, String backupDir)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String fileName = backupDir+"/"+appInstanceDTO.nomInstance+"_"+df.format(DateUtils.getDate())+".tar.gz";
		
		String request = "BACKUP DATABASE TO '"+fileName+"' BLOCKING";
		
		
		ResponseDTO res =executeOneSqlRequest(request, appInstanceDTO,0,true);
		if (res.success==false)
		{
			return "Erreur pour "+appInstanceDTO.nomInstance+": "+res.sqlResponse;
		}
		
		
		File file= new File(fileName);
		if (file.canRead()==false)
		{
			return "Erreur pour "+appInstanceDTO.nomInstance+": le fichier n'est pas trouvé";
		}
		
		return "Succès pour "+appInstanceDTO.nomInstance;
	}

	
	/**
	 * Permet de recuperer les mails de tous les adminitrateurs sur toutes les bases 
	 * @return
	 */
	public String getAllMails()
	{
		StringBuffer str = new StringBuffer();
		SpecificDbUtils.executeInAllDb(()->appendMails(str),false);
		return str.toString();
	}
	
	@DbRead
	private Void appendMails(StringBuffer str)
	{
		RdbLink em = RdbLink.get();
		
		String dbName = DbManager.get().getCurrentDb().getDbName();
		
		//Query q = em.createQuery("select distinct(u) from Utilisateur u  where u.id in (select a.utilisateur.id from RoleAdmin a) OR u.id in (select t.utilisateur.id from RoleTresorier t)  order by u.nom,u.prenom");
		Query q = em.createQuery("select distinct(u) from Utilisateur u  where u.id in (select a.utilisateur.id from RoleAdmin a) and u.etatUtilisateur = :etat order by u.nom,u.prenom");
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		List<Utilisateur> us = q.getResultList();
		str.append(CollectionUtils.asStringFinalSep(us, ",",t->"\""+dbName+"\" <"+t.email+">"));
		
		return null;
	}
	
	
	
	/**
	 * Permet de recuperer des infos générales sur toutes les instances 
	 * 
	 * ATTENTION doit être appelé en dehors de toute transaction 
	 */
	public String getStatInfo()
	{
		AdminTresorierDataDTO data = new AdminTresorierDataDTO();
		data.extractionDate = new Date();
		
		// Recuperation des statistiques sur les acces
		String dbNameMaster = DbManager.get().getMasterDb().getDbName();
		
		List<StatInstanceDTO> statAccess = SpecificDbUtils.executeInSpecificDb(dbNameMaster, ()->new LogViewService().getStatInstance());
		
		SpecificDbUtils.executeInAllDb(()->appendStatInfo(data,statAccess),false);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		return gson.toJson(data);
	}
	
	@DbRead
	private Void appendStatInfo(AdminTresorierDataDTO data, List<StatInstanceDTO> statAccess)
	{
		RdbLink em = RdbLink.get();
		String dbName = DbManager.get().getCurrentDb().getDbName();
		
		AdminTresorierDataDTO.InstanceDTO stat = new AdminTresorierDataDTO.InstanceDTO();
		
		stat.code = dbName;
		stat.nom = new ParametresService().getParametres().nomAmap;
		stat.nbAccessLastMonth = statAccess.stream().filter(e->e.nomInstance.equals(dbName)).findFirst().map(e->e.detail[0].nbAccess).orElse(0);
		
		TypedQuery<Utilisateur> q = em.createQuery("select distinct(u) from Utilisateur u  where u.id in (select a.utilisateur.id from RoleAdmin a) and u.etatUtilisateur = :etat order by u.nom,u.prenom",Utilisateur.class);
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		stat.admins = q.getResultList().stream().map(e->new AdminTresorierDataDTO.ContactDTO(e.nom, e.prenom, e.email)).collect(Collectors.toList());
		
		q = em.createQuery("select distinct(u) from Utilisateur u  where u.id in (select a.utilisateur.id from RoleTresorier a) and u.etatUtilisateur = :etat order by u.nom,u.prenom",Utilisateur.class);
		q.setParameter("etat", EtatUtilisateur.ACTIF);
		stat.tresoriers = q.getResultList().stream().map(e->new AdminTresorierDataDTO.ContactDTO(e.nom, e.prenom, e.email)).collect(Collectors.toList());
				
		data.instances.add(stat);
		
		return null;
	}
	
	
	/**
	 * Permet de recuperer le schema sur toutes les bases 
	 * @return
	 */
	public String getSchemaAllBases(boolean computeSize)
	{
		SchemaDbDTO data = new SchemaDbDTO();
		SpecificDbUtils.executeInAllDb(()->appendSchema(data,computeSize),false);	
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(data);
	}
	
	@DbRead
	private Void appendSchema(SchemaDbDTO data, boolean computeSize)
	{
		RdbLink em = RdbLink.get();
		String dbName = DbManager.get().getCurrentDb().getDbName();
		
		OneSchemaDTO oneSchemaDTO = new OneSchemaDTO();
		oneSchemaDTO.code = dbName;
		data.instances.add(oneSchemaDTO);

		// Récupération de la liste des colonnes
		Query q = em.createNativeQuery("SELECT TABLE_NAME,COLUMN_NAME,IS_NULLABLE,DTD_IDENTIFIER FROM INFORMATION_SCHEMA.COLUMNS "
				+ "WHERE TABLE_SCHEMA='PUBLIC' ORDER BY TABLE_NAME, COLUMN_NAME");
		List<Object[]> ds = q.getResultList();
		for (Object[] d : ds) 
		{
			long size=0;
			if (computeSize)
			{
				size = computeSize(em,""+d[0],""+d[1],""+d[3]);
			}
			DbColumnDTO col = new DbColumnDTO(""+d[0],""+d[1],""+d[2],""+d[3],size);
			oneSchemaDTO.cols.add(col);
		}
		
		// Récupération de la liste des foreign keys
		q = em.createNativeQuery("select FKTABLE_NAME,FKCOLUMN_NAME,PKTABLE_NAME,PKCOLUMN_NAME,FK_NAME from INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE "
				+ " order by FKTABLE_NAME,FKCOLUMN_NAME,FK_NAME");
		ds = q.getResultList();
		for (Object[] d : ds) 
		{
			DbForeignKeyDTO col = new DbForeignKeyDTO(""+d[0],""+d[1],""+d[2],""+d[3],""+d[4]);
			oneSchemaDTO.foreignKeys.add(col);
		}
		
		// Récupération de la liste des clés uniques
		q = em.createNativeQuery("select CONSTRAINT_NAME,TABLE_NAME,column_name from information_schema.constraint_column_usage where CONSTRAINT_NAME "
				+ " in (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE CONSTRAINT_TYPE='UNIQUE' and TABLE_SCHEMA='PUBLIC') "
				+ " order by CONSTRAINT_NAME,COLUMN_NAME");
		ds = q.getResultList();
		for (Object[] d : ds) 
		{
			DbUniqKeyDTO col = new DbUniqKeyDTO(""+d[0],""+d[1],""+d[2]);
			oneSchemaDTO.uniqKeys.add(col);
		}
		 
		
		
		return null;
	}
	
	


	private long computeSize(RdbLink em, String tableName, String columnName, String dtdIdentifier)
	{
		if (dtdIdentifier.startsWith("VARCHAR"))
		{
			Query q = em.createNativeQuery("SELECT SUM(LENGTH("+columnName+")) FROM "+tableName);
			return SQLUtils.count(q);
		}
		else
		{
			int fieldSize = computeFieldSize(dtdIdentifier);
			Query q = em.createNativeQuery("SELECT COUNT("+columnName+") FROM "+tableName);
			return SQLUtils.count(q)*fieldSize;
		}
	}



	private int computeFieldSize(String dtdIdentifier)
	{
		if (dtdIdentifier.equals("BIGINT"))  return 8;
		if (dtdIdentifier.equals("INTEGER"))  return 4;
		if (dtdIdentifier.equals("BOOLEAN"))  return 1;
		if (dtdIdentifier.equals("DATE"))  return 6;
		if (dtdIdentifier.equals("TIMESTAMP"))  return 10;
		if (dtdIdentifier.equals("NUMERIC(38)"))  return 10;
		
		throw new AmapjRuntimeException(dtdIdentifier);
	}



	@DbWrite
	public void setDbStateOnStart(List<AppInstanceDTO> dtos, StateOnStart stateOnStart)
	{
		RdbLink em = RdbLink.get();
		
		for (AppInstanceDTO appInstanceDTO : dtos)
		{
			AppInstance app = em.find(AppInstance.class, appInstanceDTO.id);
			app.stateOnStart = stateOnStart;
		}
	}
	
}
