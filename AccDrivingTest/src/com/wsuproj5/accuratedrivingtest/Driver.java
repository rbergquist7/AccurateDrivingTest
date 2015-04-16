package com.wsuproj5.accuratedrivingtest;

public class Driver {
    private String m_driversLicense = null;
    private String m_comments = null;
    private String m_LatLon = null;
    private String m_drive_route = null;
    private String m_Pass_Fail = "True";
    private String m_EvaluatorsName = null ;
    private int m_AvgMPH = 0;

	public  Driver() {}
 
    public String getdriversLicense()      	{return m_driversLicense;}
    public String getcomments()       		{return m_comments;}
    public String getLatLon()         		{return m_LatLon;}
    public String getPass_Fail() 			{return m_Pass_Fail;}
    public String getEvaluatorsName() 		{return m_EvaluatorsName;}
    public String getM_drive_route() 		{return m_drive_route;}
    public int getAvgMPH() 					{return m_AvgMPH;}
 
    public void setdriversLicense(String param)      		{m_driversLicense = param;}
    public void setcomments(String param)       			{m_comments = param;}
    public void setLatLon(String param)         			{m_LatLon= param;}
    public void setPass_Fail(String Pass_Fail) 				{m_Pass_Fail = Pass_Fail;}
    public void setEvaluatorsName(String EvaluatorsName) 	{m_EvaluatorsName = EvaluatorsName;}
    public void setAvgMPH(int avgMPH) 						{m_AvgMPH = avgMPH;}
    public void setM_drive_route(String drive_route) 		{m_drive_route = drive_route;}



}