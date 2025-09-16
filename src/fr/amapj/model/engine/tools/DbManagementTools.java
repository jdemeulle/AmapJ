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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import fr.amapj.common.DateUtils;
import fr.amapj.common.RandomUtils;
import fr.amapj.model.engine.db.DbManager;
import fr.amapj.model.engine.dbms.hsqlinternal.HsqlInternalDbms;
import fr.amapj.model.engine.dbms.hsqlinternal.HsqlInternalDbmsConf;
import fr.amapj.model.models.param.SmtpType;
import fr.amapj.model.models.saas.TypDbExemple;
import fr.amapj.service.services.appinstance.AppInstanceDTO;
import fr.amapj.view.engine.ui.AmapJLogManager;

/**
 * Outils pour le management des bass de données en dev et en prod
 *
 */
public class DbManagementTools 
{

	public static void main(String[] args) throws IOException
	{		
		new DbManagementTools().execute();
		
	}

	private Scanner sc;
	
	private void execute() throws IOException
	{
		sc = new Scanner(System.in);
		
		String choix = ask("Que voulez vous faire ?\n"
				+ "1 : Créer une base de données de DEV classique (mode hsql external)\n"
				+ "2 : Créer une base de donnes de DEV mode master(mode hsql internal)\n"
				+ "3 : Créer les bases pour la distribution\n"
				+ "4 : Créer une base de production\n");
		
		switch (choix.charAt(0))
		{
		case '1': devBaseClassique(); break;
		case '2': devBaseMaster(); break;
		case '3': distBase(); break;
		case '4': prodBase(); break;
		}
	}

	
	
	private void devBaseClassique() throws IOException
	{
		s("Création d'une base de données de dev classique");
		s("Il faut arreter la base de données et le serveur , et effacer tous les fichiers dans le repertoire db/data");
		ask("Appuyez sur entrée quand cela est fait");
		doDevBaseClassique();
		s("Tout est prêt , l'utilisateur MASTER est master@example.fr, mot de passe a");

	}
	

	private void doDevBaseClassique() throws IOException
	{
		DbManager dbManager = createHsqlInternalDbms();
		
		// Création de la base master
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.nomInstance="master";
		
		dto.nomAmap = "MASTER";
		dto.villeAmap = "MASTER";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 0;
		dto.url = "xx";
		dto.typDbExemple = TypDbExemple.BASE_MASTER;
		dto.user1Nom = "MASTER";
		dto.user1Prenom = "Master";
		dto.user1Email= "master@example.fr";
		dto.password = "a";
		dto.dbUserName = "SA";
		dto.dbPassword = "";
	
		
		dbManager.createDataBase(dto);
		
		// Création de la base amap1
		dto = new AppInstanceDTO();
		
		dto.nomInstance="amap1";
		
		dto.nomAmap = "AMAP1";
		dto.villeAmap = "VILLE AMAP1";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 200;
		dto.url = "http://amapj.fr/";
		
		// La fin des inscriptions est placée dans 10 jours
		Date d1 = DateUtils.addDays(DateUtils.getDate(), 10);
		Date d2 = DateUtils.firstMonday(d1);
		Date d3 = DateUtils.addDays(d2, 3);
		Date d4 = DateUtils.addDays(d2, 7*8);
		
		
		dto.dateDebut = d3;
		dto.dateFin = d4;
		dto.dateFinInscription = DateUtils.addDays(d3, -1);
		
		dto.typDbExemple = TypDbExemple.BASE_EXEMPLE;
		dto.password = "a";
		
		dbManager.createDataBase(dto);
		
		dbManager.stopDbms();
		
	}
	


	private void devBaseMaster() throws IOException
	{
		s("Création d'une base de données de dev MASTER");
		s("Il faut arreter la base de données et le serveur , et effacer tous les fichiers dans le repertoire db/data");
		ask("Appuyez sur entrée quand cela est fait");
		doDevBaseMaster();
		s("Il faut ensuite copier /build-tools/files-dev/master_mode/context.xml dans /amapj/WebContent/META-INF/context.xml");
		s("Tout est prêt , l'utilisateur MASTER est master@example.fr, mot de passe a");

	}

	
	/**
	 * Ce script permet de créer une base de données mode MASTER 
	 * 
	 * Base master : 
	 * Base amap1 : 
	 * 
	 */
	private void doDevBaseMaster() 
	{

		DbManager dbManager = createHsqlInternalDbms();
		
		// Création de la base master
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.nomInstance="master";
		
		dto.nomAmap = "MASTER";
		dto.villeAmap = "MASTER";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 0;
		dto.url = "xx";
		dto.typDbExemple = TypDbExemple.BASE_MASTER;
		dto.user1Nom = "MASTER";
		dto.user1Prenom = "Master";
		dto.user1Email= "master@example.fr";
		dto.password = "a";
	
		
		dbManager.createDataBase(dto);
		
		// Création de la base amap1
		dto = new AppInstanceDTO();
		
		dto.nomInstance="amap1";

		
		dto.nomAmap = "AMAP1";
		dto.villeAmap = "VILLE AMAP1";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 200;
		dto.url = "http://amapj.fr/";
		
		// La fin des inscriptions est placée dans 10 jours
		Date d1 = DateUtils.addDays(DateUtils.getDate(), 10);
		Date d2 = DateUtils.firstMonday(d1);
		Date d3 = DateUtils.addDays(d2, 3);
		Date d4 = DateUtils.addDays(d2, 7*8);
		
		
		dto.dateDebut = d3;
		dto.dateFin = d4;
		dto.dateFinInscription = DateUtils.addDays(d3, -1);
		
		dto.typDbExemple = TypDbExemple.BASE_EXEMPLE;
		dto.password = "a";
		
		dbManager.createDataBase(dto);
		
		dbManager.stopDbms();
		
	}
		
	private void distBase() throws IOException
	{
		s("Création d'une base de données pour la distribution");
		s("Il faut arreter la base de données et le serveur.");
		ask("Appuyez sur entrée quand cela est fait");
		doDistBase();
		s("Voir la suite des instructions dans /docs/tech-notes/admin/creation_version/creation_version.txt");

	}

	

	/**
	 * Ce script permet de créer les deux bases de données pour la distribution
	 * 
	 * Base m1 : correspond au master
	 * Base a1 : correspond à amap1
	 * 
	 */
	private void doDistBase() 
	{
		DbManager dbManager = createHsqlInternalDbms();
		
		// Création de la base m1
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.nomInstance="m1";

		
		dto.nomAmap = "MASTER";
		dto.villeAmap = "MASTER";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 0;
		dto.url = "xx";
		dto.typDbExemple = TypDbExemple.BASE_MASTER;
		
		dbManager.createDataBase(dto);
		
		// Création de la base a1
		dto = new AppInstanceDTO();
		
		dto.nomInstance="a1";
		
		dto.nomAmap = "AMAP1";
		dto.villeAmap = "VILLE AMAP1";
		dto.smtpType = SmtpType.GMAIL;
		dto.adrMailSrc = "";
		dto.nbMailMax = 200;
		dto.url = "http://amapj.fr/";
		
		// La base est valable 80 jours, donc la fin des inscriptions est placée dans 80 jours
		Date d1 = DateUtils.addDays(DateUtils.getDate(), 80);
		Date d2 = DateUtils.firstMonday(d1);
		Date d3 = DateUtils.addDays(d2, 3);
		Date d4 = DateUtils.addDays(d2, 7*12);
		
		
		dto.dateDebut = d3;
		dto.dateFin = d4;
		dto.dateFinInscription = d3;
		
		dto.typDbExemple = TypDbExemple.BASE_EXEMPLE;
		dto.password = "a";
		
		dbManager.createDataBase(dto);
		
		dbManager.stopDbms();
	
		SimpleDateFormat df = new SimpleDateFormat("dd MMMMM yyyy");
		System.out.println("===================================================");
		System.out.println("f[\"d1\"]=\""+df.format(d3)+"\";");
		System.out.println("f[\"d2\"]=\""+df.format(d3)+"\";");
		System.out.println("f[\"d3\"]=\""+df.format(d3)+"\";");
		System.out.println("f[\"d4\"]=\""+df.format(d4)+"\";");
		System.out.println("===================================================");
		
		
		
	}
	
	
	private void prodBase() throws IOException
	{
		s("Création d'une base de données pour la production");
		s("Il faut arreter la base de données et le serveur.");
		String name = ask("Merci de saisir le nom de la base ","s7");
		String password = doProdBase(name);
		System.out.println("===================================================");
		System.out.println("La base est créée , password = "+password);
		System.out.println("===================================================");
	}

	
	
	/**
	 * Ce script permet de créer une base de données MASTER pour la mise en PROD 

	 * 
	 */
	private String doProdBase(String name) 
	{
		String password = RandomUtils.generatePasswordMin(8);
		
		DbManager dbManager = createHsqlInternalDbms();
		
		// Création de la base master1
		AppInstanceDTO dto = new AppInstanceDTO();
		
		dto.nomInstance=name.toLowerCase()+"-master";
		
		
		dto.nomAmap = name.toUpperCase()+" MASTER";
		dto.villeAmap = name.toUpperCase()+" MASTER";
		dto.smtpType = SmtpType.POSTFIX_LOCAL;
		dto.adrMailSrc = name.toUpperCase()+"-master@m.amapj.fr";
		dto.nbMailMax = 100;
		dto.url = "https://"+name.toLowerCase()+".amapj.fr/p/"+name.toLowerCase()+"-master";
		
		dto.typDbExemple = TypDbExemple.BASE_MASTER;
		dto.user1Nom = "MASTER";
		dto.user1Prenom = "master";
		dto.user1Email= "postmaster@amapj.fr";
		dto.password = password;
		dto.dbUserName = "SA";
		dto.dbPassword = "";
	
		dbManager.createDataBase(dto);
		
		dbManager.stopDbms();
		
	
		return password;
	}
	
	
	// PARTIE TECHNIQUE DB  
	
	
	/**
	 * Permet une initialisation en mode base de données interne
	 */
	private DbManager createHsqlInternalDbms() 
	{
		AmapJLogManager.setLogDir("../../../logs/");
		
		HsqlInternalDbmsConf conf = new HsqlInternalDbmsConf("xx");
		conf.contentDirectory = "c:/prive/dev/amapj/git/amapj-dev/amapj/db/data";
		conf.port = 9001;
		
		DbManager.initialize(conf, null);
		DbManager.get().startDbms();
		
		return DbManager.get();
	}
	
	
	// PARTIE TECHNIQUE IHM  

	private void s(String s)
	{
		System.out.println(s);
		
	}

	private String ask(String message)
	{
		System.out.println(message);
		String str = sc.nextLine();
		return str;
	}
	
	private String ask(String message, String defaultValue)
	{
		String str = ask(message+"( exemple : "+defaultValue+" )");
		if (str.length()==0)
		{
			System.out.println(defaultValue);
			return defaultValue;
		}
		return str;
	}
		
}



