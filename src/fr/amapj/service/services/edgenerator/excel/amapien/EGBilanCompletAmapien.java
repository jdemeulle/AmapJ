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

 package fr.amapj.service.services.edgenerator.excel.amapien;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.TypedQuery;

import fr.amapj.common.CollectionUtils;
import fr.amapj.common.FormatUtils;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.contrat.reel.Contrat;
import fr.amapj.model.models.fichierbase.Utilisateur;
import fr.amapj.service.engine.generator.excel.AbstractExcelGenerator;
import fr.amapj.service.engine.generator.excel.ExcelFormat;
import fr.amapj.service.engine.generator.excel.ExcelGeneratorTool;
import fr.amapj.service.services.access.AccessManagementService;
import fr.amapj.service.services.edgenerator.excel.cheque.EGSyntheseCheque;
import fr.amapj.service.services.edgenerator.excel.cheque.EGSyntheseCheque.Mode;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien;
import fr.amapj.service.services.edgenerator.excel.feuilledistribution.amapien.EGFeuilleDistributionAmapien.EGMode;
import fr.amapj.service.services.gestioncontrat.GestionContratService;
import fr.amapj.service.services.gestioncontrat.ModeleContratSummaryDTO;
import fr.amapj.service.services.gestioncontratsigne.ContratSigneDTO;
import fr.amapj.service.services.gestioncontratsigne.GestionContratSigneService;
import fr.amapj.service.services.gestioncotisation.GestionCotisationService;
import fr.amapj.service.services.gestioncotisation.PeriodeCotisationUtilisateurDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceDateDTO;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceService;
import fr.amapj.service.services.permanence.periode.PeriodePermanenceUtilisateurDTO;
import fr.amapj.service.services.parametres.ParametresDTO;
import fr.amapj.service.services.parametres.ParametresService;

/**
 * Permet de generer un fichier avec l'ensemble des informations connues sur un amapien
 * Cela peut servir en particulier pour le RGPD 
 */
public class EGBilanCompletAmapien extends AbstractExcelGenerator
{
	
	Long idUtilisateur;
	
	public EGBilanCompletAmapien(Long idUtilisateur)
	{
		this.idUtilisateur = idUtilisateur;
	}

	@Override
	public void fillExcelFile(RdbLink em,ExcelGeneratorTool et)
	{
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		TypedQuery<Contrat> q = em.createQuery("select c from Contrat c where c.utilisateur =:u order by c.dateCreation",Contrat.class);
		q.setParameter("u",u);
		List<Contrat> cs = q.getResultList();
		
		// Ajout d'une feuille avec la liste des informations generales de cet amapien
		addInfoGenerale(em,et,u,cs);

		// Ajout d'une feuille avec la liste des cotisations de cet amapien
		addInfoCotisations(em,et,u);

		// Ajout d'une feuille avec la liste de tous les contrats
		addListeContrats(em,u,et,cs);

		// Ajout d'une feuille avec tous les chéques de tous les contrats
		new EGSyntheseCheque(Mode.TOUS, idUtilisateur).addOneSheet(em, u, et, "Chèques",false);
		
		// Ajout d'une feuille avec les informations de permanence
		addInfoPermanence(em,et,u);
		
		// Ajout d'une feuille par contrat (livraison)
		addLivraison(em,et,cs);
		
	}

	
	
	/**
	 * Informations générales,  y compris les roles
	 * @param cs 
	 */
	private void addInfoGenerale(RdbLink em, ExcelGeneratorTool et, Utilisateur u, List<Contrat> cs) 
	{
		et.addSheet("Généralités", 2, 70);
		
		et.addRow("Informations générales pour l'utilisateur "+u.nom+" "+u.prenom,et.grasGaucheNonWrappe);
		et.addRow();
		
		addLine(et,"Nom",u.nom);
		addLine(et,"Prénom",u.prenom);
		addLine(et,"E mail",u.email);
		addLine(et,"Téléphone 1",u.numTel1);
		addLine(et,"Téléphone 2",u.numTel2);
		addLine(et,"Adresse",u.libAdr1);
		addLine(et,"Code postal",u.codePostal);
		addLine(et,"Ville",u.ville);
		
		
		et.addRow();
		
		addLine(et,"Date de création : ",u.dateCreation);
		addLine(et,"Nombre de contrats de cet utilisateur : ",""+cs.size());
		
		et.addRow();
		
		
		List<String> roles = new AccessManagementService().detailDesRoles(u.id); 
		for (String role : roles) 
		{
			addLine(et,role,"");
		}
	}

	private void addLine(ExcelGeneratorTool et, String col1, String col2) 
	{
		et.addRow();
		et.setCell(0, col1, et.grasGaucheNonWrappe);
		et.setCell(1, col2, et.nonGrasGaucheNonWrappe);
	}
	
	private void addLine(ExcelGeneratorTool et, String col1, Date col2) 
	{
		et.addRow();
		et.setCell(0, col1, et.grasGaucheNonWrappe);
		et.setCellDate(1, col2, et.nonGrasGaucheNonWrappe);
	}
	
	
	/**
	 * Informations sur les cotisations
	 */
	private void addInfoCotisations(RdbLink em, ExcelGeneratorTool et, Utilisateur u) 
	{
		List<PeriodeCotisationUtilisateurDTO> ps = new GestionCotisationService().getPeriodeCotisation(u.id);
		
		et.addSheet("Cotisations", 8, 20);
		
		et.addRow("Liste des cotisations pour l'utilisateur "+u.nom+" "+u.prenom,et.grasGaucheNonWrappe);
		et.addRow();
		
		if (ps.size()==0)
		{
			et.addRow("Cet utilisateur n'est pas associé à une période de cotisation",et.grasGaucheNonWrappe);
			return;
		}
		
		
		et.addRow();
		et.setCell(0, "Nom période", et.grasCentreBordure);
		et.setCell(1, "Date de début", et.grasCentreBordure);
		et.setCell(2, "Date de fin", et.grasCentreBordure);
		et.setCell(3, "Date de l'adhesion", et.grasCentreBordure);
		et.setCell(4, "Montant de l'adhesion", et.grasCentreBordure);
		et.setCell(5, "Réception paiement", et.grasCentreBordure);
		et.setCell(6, "État du paiement", et.grasCentreBordure);
		et.setCell(7, "Type du paiement", et.grasCentreBordure);
		
		for (PeriodeCotisationUtilisateurDTO p : ps) 
		{
			et.addRow();
			et.setCell(0, p.periodeNom, et.nonGrasGaucheBordure);
			et.setCellDate(1, p.periodeDateDebut, et.nonGrasCentreBordure);
			et.setCellDate(2, p.periodeDateFin, et.nonGrasCentreBordure);
			et.setCellDate(3, p.dateAdhesion, et.nonGrasCentreBordure);
			et.setCellPrix(4, p.montantAdhesion, et.prixCentreBordure);
			et.setCellDate(5, p.dateReceptionCheque, et.nonGrasCentreBordure);
			et.setCell(6, ""+p.etatPaiementAdhesion, et.nonGrasCentreBordure);
			et.setCell(7, ""+p.typePaiementAdhesion, et.nonGrasCentreBordure);
		}		
		
		
		
		
		
	}

	private void addListeContrats(RdbLink em, Utilisateur u, ExcelGeneratorTool et, List<Contrat> cs) 
	{
		et.addSheet("Liste des contrats", 7, 50);
		et.setColumnWidth(0, 15);
		et.setColumnWidth(1, 25);
		et.setColumnWidth(4, 20);
		et.setColumnWidth(5, 15);
		et.setColumnWidth(6, 15);
		
		
		et.addRow("Liste des contrats pour l'utilisateur "+u.nom+" "+u.prenom,et.grasGaucheNonWrappe);
		
		et.addRow();
		
		et.addRow();
		
		
		// Colonne 0  : 
		et.setCell(0, "Numéro", et.grasCentreBordure);
		
		// Colonne 1 
		et.setCell(1, "Date de création", et.grasCentreBordure);

		// Colonne 2 
		et.setCell(2, "Nom du contrat", et.grasGaucheNonWrappeBordure);
		
		// Colonne 3 
		et.setCell(3, "Nom du producteur", et.grasGaucheNonWrappeBordure);
		
		// Colonne 4
		et.setCell(4, "Montant", et.grasCentreBordure);
				
		// Colonne 5
		et.setCell(5, "Date de début", et.grasCentreBordure);

		// Colonne 6
		et.setCell(6, "Date de fin", et.grasCentreBordure);

		
		
		
		for (int i = 0; i < cs.size(); i++) 
		{
			Contrat contrat = cs.get(i);
			ContratSigneDTO dto = new GestionContratSigneService().createContratSigneInfo(em, contrat);
			ModeleContratSummaryDTO mcDto = new GestionContratService().createModeleContratInfo(em, contrat.modeleContrat);
			
			et.addRow();
			et.setCell(0, "C"+(i+1), et.nonGrasCentreBordure);
			et.setCellDateTime(1, contrat.dateCreation, et.nonGrasCentreBordure);
			et.setCell(2, contrat.modeleContrat.nom, et.nonGrasGaucheBordure);
			et.setCell(3, contrat.modeleContrat.producteur.nom, et.nonGrasGaucheBordure);
			et.setCellPrix(4, dto.mntCommande, et.prixCentreBordure);
			et.setCellDate(5, mcDto.dateDebut, et.nonGrasCentreBordure);
			et.setCellDate(6, mcDto.dateFin, et.nonGrasCentreBordure);
		}
		
	}
	
	
	private void addInfoPermanence(RdbLink em, ExcelGeneratorTool et, Utilisateur u) 
	{
		SimpleDateFormat df = FormatUtils.getStdDate();
		List<PeriodePermanenceDTO> ps = new PeriodePermanenceService().getAllPermanenceDTO(u.id);
		CollectionUtils.sort(ps, e->e.dateDebut);
		
		et.addSheet("Permanences", 2, 70);
		
		
		et.addRow("Liste des permanences pour l'utilisateur "+u.nom+" "+u.prenom,et.grasGaucheNonWrappe);
		
		et.addRow();
		
		for (PeriodePermanenceDTO p : ps) 
		{
			PeriodePermanenceUtilisateurDTO ppu = p.findPeriodePermanenceUtilisateurDTO(u.id);
			List<PeriodePermanenceDateDTO> dates = p.findPeriodePermanenceDateDTO(u.id);
			
			
			et.addRow();
			addLine(et, "Nom de la période de permanence", p.nom);
			addLine(et, "Date de début", p.dateDebut);
			addLine(et, "Date de fin", p.dateFin);
			addLine(et, "Nombre de participations demandées", ""+ppu.nbParticipation);
			addLine(et, "Nombre de dates où l'utilisateur s'est inscrit", ""+dates.size());
			for (int i = 0; i < dates.size(); i++) 
			{
				PeriodePermanenceDateDTO date = dates.get(i);
				String str = "Inscrit pour le "+df.format(date.datePerm) + " avec le(s) rôle(s) "+CollectionUtils.asString(date.getRoles(u.id), ",");
				addLine(et, "Date "+(i+1), str);
			}
			et.addRow();
		}
	}


	

	private void addLivraison(RdbLink em, ExcelGeneratorTool et, List<Contrat> cs) 
	{
		for (int i = 0; i < cs.size(); i++) 
		{
			Contrat contrat = cs.get(i);
			String nomPage = "C"+(i+1);
			new EGFeuilleDistributionAmapien(EGMode.STD, contrat.modeleContrat.id, contrat.id).addOnePage(em, et, nomPage);
		}
		
	}


	@Override
	public String getFileName(RdbLink em)
	{
		ParametresDTO param = new ParametresService().getParametres();

		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		return "bilan-complet-amapien-"+param.nomAmap+"-"+u.nom+" "+u.prenom;
	}
	

	@Override
	public String getNameToDisplay(RdbLink em)
	{
		Utilisateur u = em.find(Utilisateur.class, idUtilisateur);
		return "Le bilan complet pour l'amapien "+u.nom+" "+u.prenom;
	}
	
	@Override
	public ExcelFormat getFormat()
	{
		return ExcelFormat.XLS;
	}

}


