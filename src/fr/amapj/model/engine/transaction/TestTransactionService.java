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
 package fr.amapj.model.engine.transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.amapj.common.DateUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.fichierbase.Utilisateur;

/**
 * Permet de tester le bon fonctionnement des transactions
 */
public class TestTransactionService
{
	private final static Logger logger = LogManager.getLogger();

	public void noRdbLink()
	{
		// Une exception va être jeté, car il manque le tag @DbRead ou @DbWrite
		RdbLink em = RdbLink.get();
	}
	
	@DbRead
	public boolean withRdbLinkRead()
	{
		RdbLink em = RdbLink.get();
		return em.getEm().getTransaction().isActive();
	}
	
	@DbWrite
	public boolean withRdbLinkWrite()
	{
		RdbLink em = RdbLink.get();
		return em.getEm().getTransaction().isActive();
	}

	@DbWrite
	public Long insertOneUtilisateurSuccessful(int numero)
	{
		RdbLink em = RdbLink.get();
		return doInsert(em,numero);
	}
	
	@DbRead
	public void insertOneUtilisateurWrongAnnotation(int numero)
	{
		RdbLink em = RdbLink.get();
		doInsert(em,numero);
	}
	
		
	@DbWrite
	public void insertOneUtilisateurAndError(int numero)
	{
		RdbLink em = RdbLink.get();
		doInsert(em,numero);
		
		String str = null;
		
		// Une erreur va s'enclencher, suivi du roll back de la transaction 
		str.trim();
	}
	
	
	@DbWrite
	public void insertOneUtilisateurFlushAndError(int numero)
	{
		RdbLink em = RdbLink.get();
		doInsert(em,numero);
		
		em.getEm().flush();
		logger.info("Flush done");
		
		String str = null;
		
		// Une erreur va s'enclencher, suivi du roll back de la transaction 
		str.trim();
	}
	
	

	public void insertOneUtilisateurAndErrorOutsideTransaction(int numero)
	{
		// Dans une transaction, on insère l'utilisateur 
		insertOneUtilisateurSuccessful(numero);
		
		// Une erreur va s'enclencher, mais elle n'aura pas d'impact sur la transaction précédente qui est déjà commitée 
		String str = null;
		str.trim();
	}
	
	@DbWrite
	public void updateOneUtilisateur(Long id,String nom)
	{
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, id);
		u.nom = nom;
	}
	
	
	@DbRead
	public void updateOneUtilisateurWrongAnnotation(Long id,String nom)
	{
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, id);
		u.nom = nom;
	}
	
	
	@DbRead
	public int countUtilisateur()
	{
		RdbLink em = RdbLink.get();
		em.createQuery("select count(u) from Utilisateur u");
		return em.result().singleInt();
	}
	

	@DbRead
	public boolean checkNom(Long id,String nom)
	{
		RdbLink em = RdbLink.get();
		Utilisateur u = em.find(Utilisateur.class, id);
		return u.nom.equals(nom);
	}

	
	
	
	private Long doInsert(RdbLink em, int numero)
	{
		Utilisateur u = new Utilisateur();
		u.nom = "A"+numero;
		u.prenom = "a"+numero;
		u.email = "a"+numero+"@a.com";
		u.dateCreation = DateUtils.getDate();	
		em.persist(u);
		
		return u.id;
	}

	
	
	// Test d'imbrication d'annotation 
	
	/**
	 * On commence par une transaction en lecture qui continue par une transaction en ecriture 
	 */
	@DbRead
	public int chainAnnotation01(int numero)
	{
		// On fait d'bord une lecture 
		RdbLink em = RdbLink.get();
		em.createQuery("select count(u) from Utilisateur u");
		int nb = em.result().singleInt();
		
		// On cascade avec une écriture
		insertOneUtilisateurSuccessful(numero);
		
		return nb;
	}
	
	
	/**
	 * On commence par une transaction en ecriture qui appelle une transaction en lecture 
	 */
	@DbWrite
	public int chainAnnotation02(int numero)
	{
		// On fait une ecriture
		RdbLink em = RdbLink.get();
		doInsert(em,numero);
		
		// On appelle une transaction en lecture 
		return countUtilisateur();
	}
	
	/**
	 * On commence par une transaction en ecriture qui appelle une transaction en ecriture
	 * Retourne le nombre d'appel dans la deuxième transaction, c'est à dire 2 
	 */
	@DbWrite
	public int chainAnnotation03(int numero1,int numero2)
	{
		// On fait une ecriture
		RdbLink em = RdbLink.get();
		doInsert(em,numero1);
		
		// On appelle un service en écriture 
		return callFnAnnotation03(numero2);
		
	}

	@DbWrite
	private int  callFnAnnotation03(int numero2) 
	{
		// On fait une ecriture
		RdbLink em = RdbLink.get();
		doInsert(em,numero2);
		
		return TransactionHelper.getNbAppel();
	}
	
	
	
	/**
	 * On commence par une transaction en ecriture qui appelle une transaction en ecriture
	 * Une erreur a lieu dans la deuxième annotation 
	 * Retourne le nombre d'appel dans la deuxième transaction, c'est à dire 2 
	 */
	@DbWrite
	public void chainAnnotation04(int numero1,int numero2)
	{
		// On fait une ecriture
		RdbLink em = RdbLink.get();
		doInsert(em,numero1);
		
		// On appelle un service en écriture 
		callFnAnnotation04(numero2);
		
	}

	@DbWrite
	private void  callFnAnnotation04(int numero2) 
	{
		// On fait une ecriture
		RdbLink em = RdbLink.get();
		doInsert(em,numero2);
		
		String str = null;
		str.trim();
		
	}
	
	
}
