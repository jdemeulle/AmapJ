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
 package fr.amapj.view.engine.grid.integertable;

/**
 * Liste des parametres pour un PopupIntegerTable
 *
 */
public class IntegerTableParam
{
	//
	public int nbCell;
	
	// Contient les caracteristiques  de chaque cellule prix[numero_colonne]
	// Attention : ceci doit être un nouveau tableau, car il est modifié
	public IntegerTableCell[] cell;
		
	// Grille en lecture seule
	public boolean readOnly;
	
	// Message specifique a afficher en haut de popup
	public String messageSpecifique;
	
	// Libellé utilisé pour le prix total 
	public String libPrixTotal="";
	
	public boolean hasLignePrixTotal = true;
	
	// si true, il est autorisé de saisir une grille avec uniquement des 0
	// si false, il est impossible de saisir une grille avec uniquement des 0
	public boolean allowedEmpty = false;
	
	// Libellé pour le bouton Sauvegarder / Valider
	public String libButtonSave;
	
	// 
	public Runnable switchButtonAction;

	
	/**
	 * Cette méthode met à jour les quantités
	 * 
	 */
	public void updateQte(int x,int newQte)
	{	
		cell[x].qte = newQte;
	}
	
	/**
	 * Cette fonction doit être utilisée uniquement par PopupIntegerTable
	 */
	public int getMontantTotal()
	{
		int montantTotal = 0;
		for (int j = 0; j < nbCell; j++)
		{
			IntegerTableCell c = cell[j];
			montantTotal = montantTotal +c.qte*c.prix;
		}	
		return montantTotal;
	}
	
	/**
	 * Retourne true si la grille est vide (uniquement des 0)
	 */
	public boolean isEmpty()
	{
		for (int i = 0; i < nbCell; i++)
		{
			if (cell[i].qte!=0)
			{
				return false;
			}
		}
		return true;
	}
}
