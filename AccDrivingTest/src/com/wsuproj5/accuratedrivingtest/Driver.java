package com.wsuproj5.accuratedrivingtest;

public class Driver {
	
		//object to be stored by fatfractal

		private String m_driver_licence_number = null;
		private String comments_during_evaluation = null;
		private boolean pass = true;
		private int score = 0;
		
		public	Driver() {}

		public String getDriver_Licence_Numbere()      {return m_driver_licence_number;}
		public String getComments_during_evaluation() {return comments_during_evaluation;}	
		public boolean isPass() {return pass;}
		public int getScore() {return score;}
		
		
		public void setDriver_Licence_Numbere(String param)      {m_driver_licence_number = param;}
		public void setComments_during_evaluation(String comments_during_evaluation) {
			this.comments_during_evaluation = comments_during_evaluation;
		}
		public void setPass(boolean pass) {this.pass = pass;}
		public void setScore(int score) {this.score = score;}
		

}
