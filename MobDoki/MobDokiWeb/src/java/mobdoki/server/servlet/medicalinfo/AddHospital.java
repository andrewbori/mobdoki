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
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author Andreas
 */
public class AddHospital extends HttpServlet {
   
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
        String hospital = request.getParameter("hospital");

        try {
           Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT name FROM \"Sickness\" WHERE name LIKE ?"; // Betegseg letezik?
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);
                ResultSet results = ps.executeQuery();
                if (results != null && results.next()) {                            // Ha igen akkor tovabb
                    sqlText = "SELECT name FROM \"Hospital\" WHERE name LIKE ?";    // Korhaz letezik?
                    ps = db.prepareStatement(sqlText);
                    ps.setString(1,hospital);
                    results = ps.executeQuery();

                    if (results != null && results.next()) {                      // Ha igen tovabb
                        sqlText = "SELECT sickness FROM \"Curing\" WHERE sickness LIKE ? AND hospital LIKE ?";     // Kezeles letezik?
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,sickness);
                        ps.setString(2,hospital);
                        results = ps.executeQuery();

                        if (!(results != null) || !(results.next())) {
                            sqlText = "INSERT INTO \"Curing\" (sickness, hospital) VALUES (?,?)";              // Ha nem kezeles felvetel
                            ps = db.prepareStatement(sqlText);
                            ps.setString(1,sickness);
                            ps.setString(2,hospital);
                            ps.executeUpdate();
                            json.setOKMessage("Sikeres hozzáadas!");
                        } else json.setErrorMessage("A kezelés már megtalálható!");
                    } else {
                        json.setErrorMessage("A megadott kórház nem található.");
                    }
                } else {
                     json.setErrorMessage("A megadott betegség nem található.");
                }
                results.close();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        } finally {
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
        return "Adds a Hospital to a Sickness";
    }// </editor-fold>

}
