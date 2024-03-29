/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.medicalinfo;

import java.io.BufferedReader;
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
public class SetSickness extends HttpServlet {
   
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
        double seriousness = Double.parseDouble(request.getParameter("seriousness"));
        String SSID = request.getParameter("ssid");
        
        BufferedReader reader = request.getReader();                // bejovo adatok
        String line = null;
        StringBuilder builder = new StringBuilder();
        boolean firstline = true;
        while ((line = reader.readLine()) != null) {                    // Szoveg beolvasasa
            if (!firstline) builder.append("\n");
            builder.append(line);
            firstline=false;
        }
        String details =  builder.toString();                              // Uzenet String-gé alakitasa
        reader.close();
        
        try {
           if (!Sessions.MySessions().isDoctorAndValid(SSID)) {
               json.setUnauthorizedError();
               return;
           }

           Class.forName(Connect.driver); //load the driver
           Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT name FROM \"Sickness\" WHERE name LIKE ?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,sickness);
                ResultSet results = ps.executeQuery();              // megadott nevu betegseg mar van?
                if (results != null) {
                    if (results.next()) {                               // Ha igen: módosít
                        sqlText = "UPDATE \"Sickness\" SET seriousness=?, details=? WHERE name LIKE ?";
                        ps = db.prepareStatement(sqlText);
                        ps.setDouble(1,seriousness);
                        ps.setString(2,details);
                        ps.setString(3,sickness);
                        ps.executeUpdate();                             // Ha nem: módosítás
                        json.setOKMessage("Sikeres módosítás.");
                    } else {
                        sqlText = "INSERT INTO \"Sickness\"(name,seriousness,details) VALUES (?,?,?)";
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,sickness);
                        ps.setDouble(2,seriousness);
                        ps.setString(3,details);
                        ps.executeUpdate();                             // Ha nem: felvetel
                        json.setOKMessage("Sikeres felvétel!");
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
        return "NewSickness";
    }// </editor-fold>

}
