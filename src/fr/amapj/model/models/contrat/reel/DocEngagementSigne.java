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
 package fr.amapj.model.models.contrat.reel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import fr.amapj.model.engine.Identifiable;
import fr.amapj.model.engine.rdblink.RdbLink;
import fr.amapj.model.models.objectstorage.ObjectStorage;
import fr.amapj.service.engine.objectstorage.ObjectStorageServiceProvider;


/**
 * Represente un document d'engagement pdf signé, relatif à un contrat
 * 
 *  A noter : le document est toujours signé par l'amapien en premier, puis par le producteur 
 *  Le cas signé par le producteur / non signé par l'amapien n'est pas possible  
 *
 */
@Entity
public class DocEngagementSigne  implements Identifiable
{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long id;
	
	// Un objet de type DocEngagementBin sérialisé, qui contient le document d'engagement signé au format pdf et les meta data associées
	@ManyToOne
	private ObjectStorage docEngagementBin;
	
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	public Date amapienDateSignature;
	
	@Size(min = 0, max = 512)
	@Column(length = 512)
	@NotNull
	public String amapienLibSignature;

	@Temporal(TemporalType.TIMESTAMP)
	public Date producteurDateSignature;
	
	@Size(min = 0, max = 512)
	@Column(length = 512)
	public String producteurLibSignature;
	
	// Date de la dernière génération de l'avenant, ou du pdf initial
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	public Date avenantDate;
	

	// 
	
	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}
	
	// Parti specifique à ObjectStorage docEngagementBin;
	
	// Permet d'accéder au contenu du DocEngagementBin 
	public byte[] getDocEngagementBin()
	{
		return ObjectStorageServiceProvider.find().get(docEngagementBin);
	}
	
	// Permet de positionner le contenu du DocEngagementBin
	public void setDocEngagementBin(RdbLink em,byte[] content)
	{
		docEngagementBin = ObjectStorageServiceProvider.find().set(em,docEngagementBin,content);
	}
	
}
