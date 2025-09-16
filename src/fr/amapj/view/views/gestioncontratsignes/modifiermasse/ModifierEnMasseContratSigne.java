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
 package fr.amapj.view.views.gestioncontratsignes.modifiermasse;

import fr.amapj.model.models.contrat.modele.GestionDocEngagement;
import fr.amapj.model.models.contrat.modele.GestionPaiement;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratDTO;
import fr.amapj.view.engine.popup.swicthpopup.SwitchPopup;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.date.PopupAjoutDateLivraison;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.date.PopupBarrerDateLivraison;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.date.PopupDeBarrerDateLivraison;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.date.PopupDeplacerDateLivraison;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.date.PopupSupprimerDateLivraison;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.docengagement.PopupModifGestionDocEngagementWithExistingContrat;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.docengagement.PopupSuppressAllDocEngagement;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.grille.PopupBarrerProduit;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.joker.PopupModifJoker;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement.PopupActiverSuiviPaiement;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement.PopupDesactiverSuiviPaiement;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement.PopupPaiementDate;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement.PopupPaiementRegleGestion;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.paiement.PopupTextePaiement;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.produit.PopupProduitAjout;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.produit.PopupProduitModifPrix;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.produit.PopupProduitOrdreContrat;
import fr.amapj.view.views.gestioncontratsignes.modifiermasse.produit.PopupProduitSuppression;

/**
 * Permet de choisir son action 
 */
public class ModifierEnMasseContratSigne
{
	
	/**
	 * 
	 */
	static public SwitchPopup createPopup(Long mcId)
	{
		SwitchPopup popup = new SwitchPopup("Modifications en masse sur les contrats signés",60);

		popup.addLine("Ajouter des dates de livraison", ()->new PopupAjoutDateLivraison(mcId));
		popup.addLine("Déplacer une date de livraison", ()->new PopupDeplacerDateLivraison(mcId));
		popup.addLine("Barrer une ou plusieurs dates de livraison", ()->new PopupBarrerDateLivraison(mcId));
		popup.addLine("Dé barrer une ou plusieurs dates de livraison", ()->new PopupDeBarrerDateLivraison(mcId));
		popup.addLine("Supprimer une ou plusieurs dates de livraison", ()->new PopupSupprimerDateLivraison(mcId)); 
		
		popup.addSeparator();
		
		popup.addLine("Ajouter des produits", ()->new PopupProduitAjout(mcId));
		popup.addLine("Supprimer des produits", ()->new PopupProduitSuppression(mcId));
		popup.addLine("Modifier les prix des produits", ()->new PopupProduitModifPrix(mcId));
		popup.addLine("Modifier l'ordre des produits dans le contrat", ()->new PopupProduitOrdreContrat(mcId));
		
		popup.addSeparator();
		
		popup.addLine("Barrer / Ne pas barrer des produits sur certaines dates", ()->new PopupBarrerProduit(mcId));
		
		popup.addSeparator();
		
		popup.addLine("Modifier les règles de gestion des jokers", ()->new PopupModifJoker(mcId));
		
		popup.addSeparator();
		
		ModeleContratDTO dto = new GestionContratService().loadModeleContrat(mcId);
		
		// Si ce contrat ne gére pas les paiements 
		if (dto.gestionPaiement==GestionPaiement.NON_GERE)
		{
			popup.addLine("Activer le suivi des paiements", ()->new PopupActiverSuiviPaiement(mcId));
			popup.addLine("Modifier le texte \"instructions pour le paiement\" ", ()->new PopupTextePaiement(mcId));
		}
		else
		{
			popup.addLine("Désactiver le suivi des paiements", ()->new PopupDesactiverSuiviPaiement(mcId));
			popup.addLine("Modifier les régles de gestion des paiements", ()->new PopupPaiementRegleGestion(mcId));
			popup.addLine("Supprimer / ajouter des dates de paiement", ()->new PopupPaiementDate(mcId));	
		}
		popup.addSeparator();
		
		// Les documents d'engagement
		popup.addLine("Modifier la gestion des documents d'engagement", ()->new PopupModifGestionDocEngagementWithExistingContrat(mcId));
		if (dto.gestionDocEngagement==GestionDocEngagement.SIGNATURE_EN_LIGNE)
		{
			popup.addLine("Supprimer tous les documents d'engagement déjà signés en ligne", ()->new PopupSuppressAllDocEngagement(mcId));
		}
		
		
		return popup;
	}

}
