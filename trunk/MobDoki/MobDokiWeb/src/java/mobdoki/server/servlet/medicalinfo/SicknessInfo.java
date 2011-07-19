/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.medicalinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.geometric.PGpoint;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author Andreas
 */
public class SicknessInfo extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObj json = new JSONObj();
        
        String sickness = request.getParameter("sickness");
        try {

            if (sickness==null || sickness.equals("")) {            // hibas parameter eseten kivetel dobasa
                throw new Exception();
            }

            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {

                JSONArray symptom = new JSONArray();
                JSONArray hospital = new JSONArray();
                JSONArray coordinates = new JSONArray();
                
                String sqlText = "SELECT symptom FROM \"Diagnosis\" WHERE sickness LIKE ? ORDER BY symptom";     // Tunetek keresese
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);                           // parameter beallitasa az adott tunetre
                ResultSet results = ps.executeQuery();

                if (results != null) {
                    while (results.next()) {                        // menjunk vegig a talalatokon
                        symptom.put(results.getString(1));          // irjuk ki a megtalalalt tunetet
                    }
                    json.put("symptom",symptom);
                    results.close();
                }

                sqlText = "SELECT hospital, coordinates " +
                          "FROM \"Curing\",\"Hospital\" " +
                          "WHERE sickness LIKE ? AND hospital=name " +
                          "ORDER BY hospital";     // Kezelo korhazak keresese
                ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);                           // parameter beallitasa az adott tunetre
                results = ps.executeQuery();

                if (results != null) {
                    while (results.next()) {                        // menjunk vegig a talalatokon
                        hospital.put(results.getString(1));                 // irjuk ki a megtalalalt korhaz nevet
                        PGpoint point = (PGpoint)results.getObject(2);      // irjuk ki a megtalalalt korhaz koordinatait
                        coordinates.put((new JSONObject()).put("x", point.x).put("y", point.y));
                    }
                    json.put("hospital",hospital);                  // korhaznevek
                    json.put("coordinates",coordinates);            //  es korhazkoordinatak tombje a kimenetbe
                    results.close();
                }
                json.setOK();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        }
        finally {
            json.write(out);
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}