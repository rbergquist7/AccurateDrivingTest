package com.wsuproj5.accuratedrivingtest;

public class User {
	//object to be stored by fatfractal

		private String m_username = null;
		private String m_password = null;
		
		public	User() {}

		public String getUserName()      {return m_username;}
		public String getPassword()       {return m_password;}
		
		public void setusername(String param)      {m_username = param;}
		public void setpassword(String param)       {m_password = param;}
		
	
}
