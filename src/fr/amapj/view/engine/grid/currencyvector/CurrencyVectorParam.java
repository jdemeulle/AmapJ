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
 package fr.amapj.view.engine.grid.currencyvector;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.common.CollectorUtils;
import fr.amapj.view.engine.grid.GridHeaderLine;



/**
 * Liste des parametres pour un PopupCurrencyVector
 *
 */
public class CurrencyVectorParam
{
	// Contient le montant de l'avoir initial
	public int avoirInitial;
		
	// Contient le montant Cible
	public int montantCible;	
	
	// Largeur de la colonne en pixel, exemple 110
	public int largeurCol;
	
	// Escpace entre les colonnes en pixel, exemple 3 
	public int espaceInterCol;
	
	// Présence d'une colonne additionnelle à droite de texte
	public boolean hasColComment;

	
	public List<GridHeaderLine> headerLines = new ArrayList<>();
	
	// Description de chaque ligne de saisie
	public List<CurrencyLine> lines = new ArrayList<>();
	
	// Si false : il y a seulement un bouton OK, les montants initiaux ne sont pas modifiés, et l'utilisateur ne peut pas Annuler
	// Si true : il y a un bouton Annuler et Sauvegarder
	public boolean hasSaveButton = true;
	
	// Si null, on utilise la valeur Sauvegarder, sinon on utilise la valeur indiquée 
	public String libSaveButton;
	
	// Message specifique a afficher en haut de popup
	public String messageSpecifique;
	
	// Message specifique a afficher en haut de popup, aprés le message spécifique 1
	public String messageSpecifique2;
	
	// Message specifique a afficher en haut de popup, aprés le message spécifique 2
	public String messageSpecifique3;
	

	static public class CurrencyLine
	{
		public int index;
		
		public String rightPart;
		
		//
		public CurrencyLineStatus status;

		//
		public Date datePaiement;
		
		// Contient le montant reel
		public int montant;
		
	}
	
	static public enum CurrencyLineStatus
	{		
		TIRETS , 
		
		READ_ONLY,
		
		EDITABLE , 
		
		ADJUST
	}
	
	/**
	 * Permet de verifier que les parametres saisis sont valides
	 */
	public void checkParam()
	{
		// Une seule ligne ADJUST est autorisée
		int nbAdjust = getNbAdjust();
		int nbEditable = getNbEditable();
		
		if (nbAdjust>1)
		{
			throw new AmapjRuntimeException("Il y a 2 ou plus lignes ADJUST : nbAdjust="+nbAdjust);			
		}
		if (nbEditable==0 && nbAdjust==1)
		{
			throw new AmapjRuntimeException("Cas incoherent : nbEditable==0 && nbAdjust==1");
		}
		
		
		// Si pas de bouton Save
		if (hasSaveButton==false)
		{
			if (nbEditable>0)
			{
				throw new AmapjRuntimeException("Il y a des lignes editables et l'utilisateur ne peut pas sauvegarder");
			}
		}
		
		// 
		for (int i = 0; i < lines.size(); i++) 
		{
			CurrencyLine line = lines.get(i);
			if (line.status==CurrencyLineStatus.TIRETS && line.montant!=0)
			{
				throw new AmapjRuntimeException("Il y a une ligne de type TIRETS avec un montant non null");
			}
			if (line.index!=i)
			{
				throw new AmapjRuntimeException("Il y a une erreur d'index");
			}
		}
		
		// Les montants proposés doivent être cohérents dés le départ en mode Adjust
		if (nbAdjust==1 && getMontantTotalPaiement()!=montantCible)
		{
			throw new AmapjRuntimeException("Les montants initiaux ne sont pas corrects");
		}
	}
	
	
	public boolean isReadOnly()
	{
		for (CurrencyLine line : lines) 
		{
			if (line.status==CurrencyLineStatus.EDITABLE)
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Indique si  le montant des chèques doit être égal au montant cible 
	 * Si il y a une case avec AJUST = true, alors on est dans ce cas 
	 */
	public boolean hasAdjust()
	{
		int nb =  getNbAdjust();
		if (nb>1)
		{
			throw new AmapjRuntimeException("Il y a 2 ou plus lignes ADJUST");			
		}
		return nb==1;	
	}
	

	//
	public int getNbLines()
	{
		return lines.size();
	}
	
	private int getNbAdjust()
	{
		return (int) lines.stream().filter(e->e.status==CurrencyLineStatus.ADJUST).count();
	}
	
	public int getNbEditable()
	{
		return (int) lines.stream().filter(e->e.status==CurrencyLineStatus.EDITABLE).count();
	}
	
	

	public CurrencyLine findAdjustLine() 
	{
		return lines.stream().filter(e->e.status==CurrencyLineStatus.ADJUST).collect(CollectorUtils.oneOrZero());
	}


	/**
	 * Réalise le calul de la cellule ADJUST pour que le montant des paiement soit egal au montant cible 
	 */
	public void performAdjust() 
	{
		CurrencyLine line = findAdjustLine();
		if (line==null)	
		{
			throw new AmapjRuntimeException("Pas de ligne Adjust");
		}
		line.montant = line.montant+montantCible-getMontantTotalPaiement();
	}

	
	public int getMontantTotalPaiement()
	{
		int mnt = avoirInitial;
		for (CurrencyLine line : lines) 
		{
			mnt = mnt+line.montant;
		}
		return mnt;
	}

	
}
