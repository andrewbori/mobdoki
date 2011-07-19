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
public class AddSymptom extends HttpServlet {
   
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
        String symptom = request.getParameter("symptom");

        try {
          Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT name FROM \"Sickness\" WHERE name LIKE ?"; // Betegseg letezik?
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);
                ResultSet results = ps.executeQuery();
                if (results != null) {
                    if (results.next()) {                                               // Ha igen akkor tovabb
                        sqlText = "SELECT name FROM \"Symptom\" WHERE name LIKE ?";     // Tunet letezik?
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,symptom);
                        results = ps.executeQuery();

                        if (!(results != null) || !(results.next())) {                      // Ha nem, akkor felvetel
                            sqlText = "INSERT INTO \"Symptom\"(name) VALUES (?)";
                            ps = db.prepareStatement(sqlText);
                            ps.setString(1,symptom);
                            ps.executeUpdate();
                        }

                        sqlText = "SELECT sickness FROM \"Diagnosis\" WHERE sickness LIKE ? AND symptom LIKE ?";     // Diagnozis letezik?
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,sickness);
                        ps.setString(2,symptom);
                        results = ps.executeQuery();
                        
                        if (!(results != null) || !(results.next())) {
                            sqlText = "INSERT INTO \"Diagnosis\" (sickness, symptom) VALUES (?,?)";              // Diagnozis felvetel
                            ps = db.prepareStatement(sqlText);
                            ps.setString(1,sickness);
                            ps.setString(2,symptom);
                            ps.executeUpdate();
                            json.setOKMessage("Sikeres hozzáadás!");
                        } else json.setErrorMessage("A diagnózis már megtalálható.");
                    } else {
                        json.setErrorMessage("A megadott betegség nem található.");
                    }
                    results.close();
                } else json.setDBError();
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
        return "Adds a symptom to the given sickness";
    }// </editor-fold>

}
