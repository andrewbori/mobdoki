/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet;

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
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;

/**
 *
 * @author Andreas
 */
public class Statistics extends HttpServlet {
   
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
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isDoctorAndValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {

                String sqlText = "SELECT COUNT(id) AS db FROM \"Sickness\"";  // Betegsegek szama
                PreparedStatement ps = db.prepareStatement(sqlText);
                ResultSet results = ps.executeQuery();
                if (results != null && results.next()) {                            // van talalat? akkor kiir
                    json.put("sickness",results.getInt(1));
                    results.close();
                } else json.put("sickness","?");

                sqlText = "SELECT COUNT(id) AS db FROM \"Symptom\"";          // Tunetek szama
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();
                if (results != null && results.next()) {                            // van talalat? akkor kiir
                    json.put("symptom",results.getInt(1));
                    results.close();
                } else json.put("symptom","?");
                
                sqlText = "SELECT COUNT(id) AS db FROM \"Hospital\"";          // Korhazak szama
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();
                if (results != null && results.next()) {                            // van talalat? akkor kiir
                    json.put("hospital",results.getInt(1));
                    results.close();
                } else json.put("hopsital","?");
                
                sqlText = "SELECT COUNT(u.id) AS db FROM \"User\" u, \"UserType\" ut " +       // Paciensek szama
                          "WHERE ut.name='patient' AND ut.id=u.\"usertypeID\"";
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();
                if (results != null && results.next()) {                            // van talalat? akkor kiir
                    json.put("patient",results.getInt(1));
                    results.close();
                } else json.put("patient","?");

                sqlText = "SELECT COUNT(id)/sicknessCnt as symptomAvg " +
                          "FROM \"Diagnosis\", (SELECT COUNT(id) as sicknessCnt FROM \"Sickness\") SicknessCnt " +
                          "GROUP BY sicknessCnt";                               // tunetek atlagos szama betegsegenkent
                ps = db.prepareStatement(sqlText);
                results = ps.executeQuery();
                if (results != null && results.next()) {                            // van talalat? akkor kiir
                    json.put("symptomAvarage",results.getInt(1));
                    results.close();
                } else json.put("symptomAvarage","?");
                db.close();
                json.setOK();
            } else json.setServerError();   // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
            json.setErrorMessage(e.getMessage());
        } finally {
            json.write(out);    // Osszeallitott JSON kiirasa az outputra
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
