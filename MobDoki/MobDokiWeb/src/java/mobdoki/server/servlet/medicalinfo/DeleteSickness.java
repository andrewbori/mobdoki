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
import mobdoki.server.Sessions;

/**
 *
 * @author Andreas
 */
public class DeleteSickness extends HttpServlet {

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
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isDoctorAndValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }

            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT id FROM \"Sickness\" WHERE name LIKE ?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);
                ResultSet results = ps.executeQuery();              // megadott nevu betegseg mar van?
                if (results != null) {
                    if (results.next()) {                               // Ha igen: torles: diagnozis es betegseg
                        int sicknessID=results.getInt(1);
                        
                        sqlText = "DELETE FROM \"Diagnosis\" WHERE \"sicknessID\"=?";             // Diagnozis torles
                        ps = db.prepareStatement(sqlText);
                        ps.setInt(1,sicknessID);
                        ps.executeUpdate();
                        
                        sqlText = "DELETE FROM \"Curing\" WHERE \"sicknessID\"=?";               // Kuralas torles
                        ps = db.prepareStatement(sqlText);
                        ps.setInt(1,sicknessID);
                        ps.executeUpdate();
                        
                        sqlText = "DELETE FROM \"Sickness\" WHERE id=?";                // Betegseg torles
                        ps = db.prepareStatement(sqlText);
                        ps.setInt(1,sicknessID);
                        ps.executeUpdate();
                        
                        json.setOKMessage("Sikeres törlés!");
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
        return "Short description";
    }// </editor-fold>
}
