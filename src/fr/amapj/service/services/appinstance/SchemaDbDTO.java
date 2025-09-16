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
 package fr.amapj.service.services.appinstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Information sur le schema de la base
 *
 */
public class SchemaDbDTO 
{
	static public class OneSchemaDTO
	{
		public String code;
		
		public List<DbColumnDTO> cols = new ArrayList<>();
		
		public List<DbForeignKeyDTO> foreignKeys = new ArrayList<>();
		
		public List<DbUniqKeyDTO> uniqKeys = new ArrayList<>();

	}
	
	
	
	static public class DbColumnDTO
	{
		public String tableName;
		
		public String columnName;
		
		public String isNullable;
		
		public String dtdIdentifier;
		
		public long size;

		public DbColumnDTO(String tableName, String columnName, String isNullable, String dtdIdentifier,long size) 
		{
			this.tableName = tableName;
			this.columnName = columnName;
			this.isNullable = isNullable;
			this.dtdIdentifier = dtdIdentifier;
			this.size = size;
		}
				
	}
	
	static public class DbForeignKeyDTO
	{
		public String tableName;
		
		public String columnName;
		
		public String pkTableName;
		
		public String pkColumnName;
		
		public String fkName;

		public DbForeignKeyDTO(String tableName, String columnName, String pkTableName, String pkColumnName,String fkName) 
		{
			this.tableName = tableName;
			this.columnName = columnName;
			this.pkTableName = pkTableName;
			this.pkColumnName = pkColumnName;
			this.fkName = fkName;
		}			
	}
	
	static public class DbUniqKeyDTO
	{
		public String constraintName;
		
		public String tableName;
		
		public String columnName;
		
		public DbUniqKeyDTO(String constraintName,String tableName, String columnName) 
		{
			this.constraintName = constraintName;
			this.tableName = tableName;
			this.columnName = columnName;
		}
	}
	

	
	public List<OneSchemaDTO> instances = new ArrayList<>();
	
}
