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
 package fr.amapj.view.views.saisiecontrat.step1qte.abo;

import java.util.List;

import com.vaadin.shared.ui.label.ContentMode;

import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.integergrid.IntegerGridCell;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;
import fr.amapj.view.views.saisiecontrat.abo.ContratAbo;
import fr.amapj.view.views.saisiecontrat.abo.joker.SaisieJoker;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.ProduitHelpSupplier;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.CheckOnEndSaisieQte;

/**
 * Popup pour la saisie des quantites pour un contrat
 * de type "Panier", c'est à dire sans le choix des dates 
 *  
 */
public class PopupSaisieQteContratPanier extends PopupIntegerGrid
{	
	
	private ContratDTO contratDTO;
	
	private SaisieContratData data;
	
	// Largeur de la colonne description des produits 
	private int largeurColonne = 500; //TODO faire varier en fonction de la taille de l'écran
	
	// Informations condensée du contrat
	private ContratAbo abo;
	
	/**
	 * 
	 */
	public PopupSaisieQteContratPanier(SaisieContratData data)
	{
		super();
		
		this.data = data;
		this.contratDTO = data.contratDTO;
		this.abo = data.contratDTO.contratAbo;
		
		//
		popupTitle = "Mon contrat "+contratDTO.nom;
		
		// 
		param.readOnly = (data.modeSaisie==ModeSaisie.READ_ONLY);
		param.messageSpecifique = s(data.messageSpecifique);
		param.libButtonSave = "Continuer ...";
		
	}
	
	public void loadParam()
	{
		// Partie jokers
		if (contratDTO.jokerNbMax>0)
		{
			param.messageSpecifiqueBottom = computeJokerMessage();
		}
		
		//
		param.nbLig = contratDTO.contratColumns.size();
		param.nbCol = 1;
		computeCells();
		
		//
		param.buttonCopyFirstLine = false;

		// Largeur des colonnes
		param.largeurCol = 110;
				
		// Construction du header 1
		GridHeaderLine line1  =new GridHeaderLine();
		line1.addCell("Produit");
		line1.addCell("Qte");
		
		param.headerLines.add(line1);
				
		// Partie gauche de chaque ligne
		param.leftPartLineLargeur = largeurColonne; 
		param.leftPartLineStyle = "description-panier";
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			IntegerGridLine line = new IntegerGridLine();
			line.isVisible = true;
			line.leftPart = getText(col);
			line.leftPartHelpSupplier = new ProduitHelpSupplier(col);
			param.lines.add(line);
		}	
		
	
	}


	private void fillTableauPrix()
	{
		for (int j = 0; j < contratDTO.contratColumns.size(); j++)
		{
			ContratColDTO col = contratDTO.contratColumns.get(j);
			param.cell[j][0].prix = col.prix*abo.getNbLivraison(j);
		}
	}

	private void computeCells()
	{
		param.cell = new IntegerGridCell [contratDTO.contratColumns.size()][1];
		
		for (int j = 0; j < contratDTO.contratColumns.size(); j++)
		{
			IntegerGridCell c= new IntegerGridCell();
			int nbLivraison = abo.getNbLivraison(j);
			
			if (nbLivraison==0)
			{
				c.isStaticText = true;
				c.staticText = "XXXXXX";
			}
			else if (abo.isDisponible(j)==false && abo.getQte(j)==0)
			{
				c.isStaticText = true;
				c.staticText = "Non disponible";
			}
			else
			{
				c.isStaticText = false;
				c.qte = abo.getQte(j);
				c.prix = 0; // Sera fait dans fillTableauPrix
			}
			
			param.cell[j][0] = c;
		}
		
		fillTableauPrix();
		
	}
	
	



	private String getText(ContratColDTO col)
	{
		String str = getLine1(col)+"<br/>";
		
		str = str+"Prix unitaire : "+new CurrencyTextFieldConverter().convertToString(col.prix)+" €<br/>";
		
		int nbLivraison = abo.getNbLivraison(col.j);
		
		if (nbLivraison==0)
		{
			str = str+"Produit non disponible";
		}
		else
		{
			str = str+"<b>"+nbLivraison+" livraisons , prix total de "+new CurrencyTextFieldConverter().convertToString(nbLivraison*col.prix)+" €</b>";
		}
		
		return str;
	}


	private String getLine1(ContratColDTO col)
	{
		return "Abonnement pour 1 "+ col.nomProduit+","+col.condtionnementProduit;
	}



	@Override
	protected void handleContinuer()
	{
		data.validate();
		close();
	}

	@Override
	public boolean performSauvegarder()
	{
		updateFromSaisie();
	
		// On vérifie si on respecte les regles jokers
		String msg = abo.checkRegleJokerValide();
		if (msg!=null)
		{
			new MessagePopup("Impossible de continuer",ContentMode.HTML,ColorStyle.RED,msg).open();
			return false;
		}
		
		// On verifie les regles de fin de saisie des quantités 
		if (new CheckOnEndSaisieQte().check(data)==false)
		{
			return false;
		}
			
		//
		data.validate();
		return true;
	}
	
	/**
	 * Copie depuis la grille de saisie vers le modele
	 */
	private void updateFromSaisie() 
	{
		// On lit dans la grille de saisie
		for (int j = 0; j < contratDTO.contratColumns.size(); j++)
		{
			// On lit la quantité saisie
			int qteSaisie = param.cell[j][0].qte;
			
			//
			abo.updateQte(j, qteSaisie);
		}
	}

	/**
	 * Une ligne de la table contient dans le cas standard 3 lignes de texte
	 * Exemple : 
	 * 	 Abonnement pour 1 Pain de blé
	 * 	 Prix unitaire : 7.00 €
	 * 	 4 livraisons , prix total de 28.00 €
	 * 
	 *  Une ligne de texte fait 16 de hauteur, et il y a 10 de marges en haut et bas
	 *  Le mode readOnly n'a pas d'impact 
	 *  
	 *  Par contre, la première ligne ("Abonnement pour 1 Pain de blé" dans l'exemple) peut faire 
	 *  plus de 50 pixels, il faut donc compter le nombre de ligne max que peut prendre cette 
	 *  premiere ligne
	 *  
	 */
	@Override
	public int getLineHeight(boolean readOnly)
	{
		int nbLineMax = getNbLineMax();
		return (nbLineMax+1+1)*16+10+10;
	}
	
	/**
	 * Retourne le nombre de ligne max sur toutes les cellules pour la ligne 1 
	 */
	private int getNbLineMax()
	{
		int nbLine = 1;
		GridSizeCalculator cal = new GridSizeCalculator();
		
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			String cell = getLine1(col);		
			nbLine = Math.max(nbLine, cal.getHeight(cell,  largeurColonne-22, "Arial",16));
		}
		return nbLine;
	}

	@Override
	public int getHeaderHeight()
	{
		// On cacule la place consommée par les headers, boutons, ...
		// 284 : nombre de pixel mesurée pour les haeders, les boutons, ... en mode normal, 185 en mode compact
		return BaseUiTools.isCompactMode() ? 185 : 284;
	}
	
	
	protected void createButtonBar()
	{
		addButton("Détail des dates de livraison", e->handleDetailDate());
		
		if (contratDTO.jokerNbMax>0)
		{
			String caption = data.modeSaisie==ModeSaisie.READ_ONLY ? "Voir mes jokers" : "Choisir mes jokers"; 
			addButton(caption, e->handleJoker());
		}
				
		super.createButtonBar();
	}

	
	private void handleDetailDate() 
	{
		updateFromSaisie();
		new PopupDetailDate(contratDTO).open();
	}

	private void handleJoker()
	{
		SaisieJoker.doSaisieJoker(data,data.modeSaisie==ModeSaisie.READ_ONLY,()->onJokerClose());	
	}
	

	/**
	 * Permet le rafraichissement de l'ecran quand on revient des jokers 
	 */
	public void onJokerClose()
	{
		// On rafraichit tout d'abord le texte des jokers
		updateLabelMessageSpecifiqueBottom(computeJokerMessage());
		
		// On rafaichit ensuite les libellés 
		List<ContratColDTO> contratColumns = contratDTO.contratColumns;
		for (int i = 0; i < contratColumns.size(); i++)
		{
			ContratColDTO col = contratColumns.get(i);
			updateFirstCol(i, getText(col));
		}	
		
		// Il faut ensuite mettre à jour la matrice de prix
		fillTableauPrix();
		updatePrixTotal();
		
	}

	private String computeJokerMessage()
	{
		return new ContratAboManager().computeJokerMessage(contratDTO, abo.getNbJokersUsed());
	}
	
}
