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
 package fr.amapj.view.engine.grid.libbutton;

import java.util.ArrayList;
import java.util.List;



/**
 * Liste des parametres pour un PopupLibButton
 *
 */
public class LibButtonParam<T>
{
	//
	public int nbLig;
	
	public int nbCol;
	
	// Largeur de la colonne 1 en pixel, exemple 110
	public int largeurLib1;
	
	// Largeur de la colonne 2 en pixel, exemple 110
	public int largeurLib2;

	// Grille en lecture seule
	public boolean readOnly;
	
	// Message specifique a afficher en haut de popup
	public String messageSpecifique;
	
	// Libellé utilisé pour le prix total 
	public String libPrixTotal="";
	
	public boolean hasLignePrixTotal = true;
	
	// Message specifique a afficher en bas de popup
	public String messageSpecifiqueBottom;
	
	// Description des lignes
	public List<LibButtonLine<T>> lines = new ArrayList<>();
	
	// si true, il est autorisé de saisir une grille avec uniquement des 0
	// si false, il est impossible de saisir une grille avec uniquement des 0
	public boolean allowedEmpty = false;
	
	// Si non null, présence d'un bouton copier la première ligne qui appelle cette fonction   
	public Runnable copyFirstLineAction;
	
	// Libellé pour le bouton Sauvegarder / Valider
	public String libButtonSave;
	
	// Permet la mise en place du bouton "swicthMode" en haut à droite de l'écran 
	public Runnable switchButtonAction;
	
	
	/**
	 * 
	 */
	public int getMontantTotal()
	{
		int montantTotal = 0;
		for (int i = 0; i < nbLig; i++)
		{
			montantTotal = montantTotal +lines.get(i).montant;
		}	
		return montantTotal;
	}
	
	/**
	 * Retourne true si la grille est vide (uniquement des 0)
	 */
	public boolean isEmpty()
	{
		for (int i = 0; i < nbLig; i++)
		{
			if (lines.get(i).qteTotale!=0)
			{
				return false;
			}
		}
		return true;
	}
	
	public int getNbLineVisible()
	{
		int nb=0;
		for (int i = 0; i < nbLig; i++)
		{
			if (lines.get(i).isVisible)
			{
				nb++;
			}
		}
		return nb;
		
	}
	
	public int getIndexFirstLineVisible()
	{
		for (int i = 0; i < nbLig; i++)
		{
			if (lines.get(i).isVisible)
			{
				return i;
			}
		}
		return -1;
		
	}
}
