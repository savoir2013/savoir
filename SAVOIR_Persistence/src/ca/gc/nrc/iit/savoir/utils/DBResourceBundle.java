// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package ca.gc.nrc.iit.savoir.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Properties;

 

/**

*  DbResourceBundle is a concrete subclass of CommonResourceBundle that

*  manages resources for a locale using a set of strings from a database.

*

*  @author  Serguei Eremenko sergeremenko@yahoo.com

*  @version 1.0

*/

 

public class DBResourceBundle extends CustomResourceBundle {

   /**

   *  Sets the resource bundle base names as an array

   */

   public DBResourceBundle(String[] baseName){

      super(baseName);

      buildProperties();

   }

   /**

   *  Sets the resource bundle base names as an array from a string like:

   *  jdbc:oracle:thin:scott/tiger@mypc:1521:ORCL:table:ScottProp, etc

   */

   public DBResourceBundle(String baseName){

      super(baseName);

      buildProperties();

   }

   /**

   *  Sets the resource bundle base names as an array from a string stored

   *  into system properties: db.resource.bundle.name

   */

   public DBResourceBundle(){ this("db.resource.bundle.name");}

   /**

   *  @return Enumeration of the keys

   */

   public Enumeration getKeys(){

      return properties != null ? properties.propertyNames() : null;

   }

   /**

   *  Gets an object for the given key from this resource bundle and null if

   *  this resource bundle does not contain an object for the given key

   */

   protected Object handleGetObject(String key) {

      if (properties == null) return null;

      return properties.getProperty(key);

   }

   /** Fetches resources from a database */

   private void buildProperties(){

      Connection        con   = null;

      PreparedStatement ps    = null;

      ResultSet         rs    = null;

      String            token = ":table:";

     try{

         Class.forName("oracle.jdbc.driver.OracleDriver");

         for (int i=0;i<baseName.length;i++){

            int index = baseName[i].indexOf(token);

            if (index < 0) continue;

            String url   = baseName[i].substring(0,index).trim();

            index += token.length();

            String table = baseName[i].substring(index).trim();

            con = DriverManager.getConnection(url);

            ps = con.prepareStatement("select Key,Value from "+table);

            rs = ps.executeQuery();

            if (properties == null) properties= new Properties();

            else properties = new Properties(properties);

            while(rs.next()){

               properties.setProperty(rs.getString(1),rs.getString(2));

            }

            rs.close();

            ps.close();

            con.close();

         }

      }catch (Exception e){

         e.printStackTrace();

         throw new RuntimeException ("Can not build properties: "+e);

      }finally{

         try{ if (con != null) con.close(); con = null;}catch (Exception e){}

         try{ if (ps != null) ps.close(); ps = null;}catch (Exception e){}

         try{ if (rs != null) rs.close(); rs = null;}catch (Exception e){}

      }

   }

   /** Collection of resource strings */

   private Properties properties;

}
