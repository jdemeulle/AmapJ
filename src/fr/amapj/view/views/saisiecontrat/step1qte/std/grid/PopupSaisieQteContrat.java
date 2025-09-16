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
 package fr.amapj.view.views.saisiecontrat.step1qte.std.grid;

import java.text.SimpleDateFormat;

import fr.amapj.common.AmapjRuntimeException;
import fr.amapj.model.models.contrat.modele.AffichageMontant;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.service.services.mescontrats.ContratColDTO;
import fr.amapj.service.services.mescontrats.ContratDTO;
import fr.amapj.service.services.mescontrats.ContratLigDTO;
import fr.amapj.view.engine.grid.GridHeaderLine;
import fr.amapj.view.engine.grid.GridHeaderLine.GridHeaderLineStyle;
import fr.amapj.view.engine.grid.GridSizeCalculator;
import fr.amapj.view.engine.grid.integergrid.IntegerGridLine;
import fr.amapj.view.engine.grid.integergrid.PopupIntegerGrid;
import fr.amapj.view.engine.grid.integergrid.lignecumul.LigneCumulParam;
import fr.amapj.view.engine.grid.integergrid.lignecumul.TypLigneCumul;
import fr.amapj.view.engine.popup.corepopup.CorePopup;
import fr.amapj.view.engine.popup.messagepopup.MessagePopup;
import fr.amapj.view.engine.tools.BaseUiTools;
import fr.amapj.view.engine.widgets.CurrencyTextFieldConverter;
import fr.amapj.view.views.saisiecontrat.SaisieContrat;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.ModeSaisie;
import fr.amapj.view.views.saisiecontrat.SaisieContrat.SaisieContratData;
import fr.amapj.view.views.saisiecontrat.step1qte.std.onedatetable.PopupSaisieQteTable;
import fr.amapj.view.views.saisiecontrat.step1qte.std.vueliste.PopupSaisieQteVueListe;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.ProduitHelpSupplier;
import fr.amapj.view.views.saisiecontrat.step1qte.utils.checkonend.CheckOnEndSaisieQte;

/**
 * Popup pour la saisie des quantites pour une carte prépayée 
 *  
 */
public class PopupSaisieQteContrat extends PopupIntegerGrid
{	
	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	
	private ContratDTO contratDTO;
	
	private SaisieContratData data;
	
	/**
	 * 
	 *  
	 */
	public PopupSaisieQteContrat(SaisieContratData data)
	{
		super();
		this.data = data;
		this.contratDTO = data.contratDTO;
		
		
		//
		popupTitle = "Mon contrat "+contratDTO.nom;
		
		// 
		param.readOnly = (data.modeSaisie==ModeSaisie.READ_ONLY);
		param.messageSpecifique = s(data.messageSpecifique);
		param.libButtonSave = "Continuer ...";
		
	}
	
	
	
	public void loadParam()
	{		
		//
		param.nbLig = contratDTO.contratLigs.size();
		param.nbCol = contratDTO.contratColumns.size();
		param.cell = contratDTO.extractCells();

		//
		param.allowedEmpty = false;
		
		// Largeur des colonnes
		param.largeurCol = 110;
		
				
		// Construction du header 1
		GridHeaderLine line1  =new GridHeaderLine();
		line1.addCell("Produit");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line1.addCell(col.nomProduit,new ProduitHelpSupplier(col));
		}
		GridSizeCalculator.autoSize(line1,param.largeurCol,"Arial",16);
	
		// Construction du header 2
		GridHeaderLine line2  =new GridHeaderLine();
		line2.style = GridHeaderLineStyle.PRIX;
		line2.addCell("prix unitaire");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line2.addCell(new CurrencyTextFieldConverter().convertToString(col.prix));
		}

		// Construction du header 3
		GridHeaderLine line3  =new GridHeaderLine();
		line3.addCell("Dates");
				
		for (ContratColDTO col : contratDTO.contratColumns)
		{
			line3.addCell(col.condtionnementProduit);
		}
		GridSizeCalculator.autoSize(line3,param.largeurCol,"Arial",16);
		
		param.headerLines.add(line1);
		param.headerLines.add(line2);
		param.headerLines.add(line3);
		
		
		// Partie gauche de chaque ligne
		param.leftPartLineLargeur = 110;
		param.leftPartLineStyle = "date-saisie";
		for (int i = 0; i < param.nbLig; i++)
		{
			ContratLigDTO lig = contratDTO.contratLigs.get(i);
			
			IntegerGridLine line = new IntegerGridLine();
			line.isVisible = computeVisible(lig);
			line.leftPart = df.format(lig.date);
			
			param.lines.add(line);
		}	
		
		//
		param.ligneCumulParam = computeLigneCumulParam();
		
		//
		param.switchButtonAction = ()->handleSwitchButton();
		
	}


	private void handleSwitchButton() 
	{
		// On recopie les données dans le contratDto
		for (int i = 0; i < param.nbLig; i++)
		{
			for (int j = 0; j < param.nbCol; j++)
			{
				contratDTO.cell[i][j].qte = param.cell[i][j].qte;
			}
		}
		
		// On calcule la nouvelle fenetre 
		CorePopup popup = computeSwitchPopup();
		
		// On insere la nouvelle fenetre dans le cascading popup 
		data.insertPopup(popup);
	}

	private CorePopup computeSwitchPopup() 
	{
		// On ouvre une nouvelle fenetre
		if (data.contratDTO.contratLigs.size()==1)
		{
			return new PopupSaisieQteTable(data);
		}
		else
		{
			return new PopupSaisieQteVueListe(data);
		}
	}

	private LigneCumulParam computeLigneCumulParam() 
	{
		// En mode readOnly, on affiche toujours le montant total
		if (param.readOnly)
		{
			return new LigneCumulParam();
		}
		
		switch (contratDTO.nature) 
		{
		case LIBRE:
		case ABONNEMENT: // Ce cas est possible, par exemple lors de la saisie d'un contrat abonnement par un réfrent 
			return new LigneCumulParam();

		case CARTE_PREPAYEE:
			if (contratDTO.paiement.gestionPaiement==GestionPaiement.NON_GERE && contratDTO.paiement.affichageMontant==AffichageMontant.MONTANT_PROCHAINE_LIVRAISON)
			{
				LigneCumulParam p = new LigneCumulParam();
				p.ligneCumul = TypLigneCumul.FIRST_LINE;
				p.libCustomLigneCumul = "Montant de la livraison du "+param.lines.stream().filter(e->e.isVisible).map(e->e.leftPart).findFirst().orElse("XX");
				return p;
			}
			else
			{
				LigneCumulParam p = new LigneCumulParam();
				p.ligneCumul = TypLigneCumul.STD;
				p.libCustomLigneCumul = "Montant des livraisons modifiables";
				return p;
			}
			
		default:
			throw new AmapjRuntimeException("Nature="+contratDTO.nature);
		}
	}



	private boolean computeVisible(ContratLigDTO lig)
	{
		if (param.readOnly)
		{
			return true;
		}
		return lig.isModifiable;
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
		// On recopie les données dans le contratDto
		for (int i = 0; i < param.nbLig; i++)
		{
			for (int j = 0; j < param.nbCol; j++)
			{
				contratDTO.cell[i][j].qte = param.cell[i][j].qte;
			}
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



	@Override
	public int getLineHeight(boolean readOnly)
	{
		// Une ligne fait 32 en mode edition , sinon 26
		return readOnly ? 26 : 32;
	}
	
	@Override
	public int getHeaderHeight()
	{
		// On cacule la place consommée par les headers, boutons, ...
		// 365 : nombre de pixel mesurée pour les haeders, les boutons, ... en mode normal, 270 en mode compact
		return BaseUiTools.isCompactMode() ? 270 : 365;

	}
}
