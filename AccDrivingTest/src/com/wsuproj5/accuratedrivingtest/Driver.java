package com.wsuproj5.accuratedrivingtest;

public class Driver {
    private String m_driversLicense = null;
    private String m_comments = null;
    private byte[] m_imageData = null;
    private String m_LatLon = null;
 
    public  Driver() {}
 
    public String getdriversLicense()      {return m_driversLicense;}
    public String getcomments()       {return m_comments;}
    public byte[] getImageData()      {return m_imageData;}
    public String getLatLon()         {return m_LatLon;}
 
    public void setdriversLicense(String param)      {m_driversLicense = param;}
    public void setcomments(String param)       {m_comments = param;}
    public void setImageData(byte[] param)      {m_imageData = param;}
    public void setLatLon(String param)         {m_LatLon= param;}
}