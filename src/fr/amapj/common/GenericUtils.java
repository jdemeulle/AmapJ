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
 package fr.amapj.common;

import java.util.Date;


public class GenericUtils
{
	static public interface ToString<T>
	{
		public String toString(T t);
	}
	
	static public interface ToDate<T>
	{
		public Date toDate(T t);
	}
	
	static public interface ToBoolean<T>
	{
		public boolean toBoolean(T t);
	}
	
	static public interface ToLong<T>
	{
		public boolean toLong(T t);
	}
	
	
	
	static public interface GetField<T>
	{
		public Object getField(T t);
	}
	
	static public interface GetFieldTyped<T,V>
	{
		public V getField(T t);
	}
	
	
	static public interface SetField<T>
	{
		public void setField(T t,Object val);
	}
	
	
	static public interface VoidAction
	{
		public void action();
	}
	
	static public interface VoidActionException
	{
		public void action() throws Exception;
	}
	
	static public interface VoidActionThrows<T extends Exception>
	{
		public void action() throws T;
	}
	
	
	static public interface StringAction
	{
		public String action();
	}
	
	static public class Tuple2<X,Y>
	{
		public X e1;
		public Y e2;
	}
	
	
	static public class Ret<T>
	{
		private String msg;
		
		private T value;
		
		private boolean isOk;;
		
		static public <T> Ret<T> error(String msg)
		{
			Ret<T> r = new Ret<>();
			r.isOk = false;
			r.msg = msg;
			return r;
		}
		
		static public <T> Ret<T> ok(T value)
		{
			Ret<T> r = new Ret<>();
			r.isOk = true;
			r.value = value;
			return r;
		}
		
		public boolean isOK()
		{
			return isOk;
		}
		
		public T get()
		{
			if (isOk==false)
			{
				throw new AmapjRuntimeException();
			}
			return value;
		}
		
		public String msg()
		{
			if (isOk==true)
			{
				throw new AmapjRuntimeException();
			}
			return msg;
		}
	}

	
}
