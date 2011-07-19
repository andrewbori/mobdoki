/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.user.health;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author mani
 */
public class PatientHealth extends HttpServlet {
   
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
        
        try {
            String username = request.getParameter("username");                             // Felhasznalonev
            int bp1 = (int)Double.parseDouble(request.getParameter("bp1"));                 // Vernyomas: systoles nyomas (felso)
            int bp2 = (int)Double.parseDouble(request.getParameter("bp2"));                 // Vernyomas: diastoles nyomas (also)
            int pulse = (int)Double.parseDouble(request.getParameter("pulse"));             // Vernyomas: diastoles nyomas (also)
            double temperature = Double.parseDouble(request.getParameter("temperature"));   // Testhomerseklet
            double weight = Double.parseDouble(request.getParameter("weight"));             // Testtomeg
            int mood = Integer.parseInt(request.getParameter("mood"));                      // Kozerzet

            java.util.Date today = new java.util.Date();                                    // idobelyeg: pillanatnyi ido
            java.sql.Timestamp sqlToday = new java.sql.Timestamp(today.getTime());
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            
            if (db != null) {

                PreparedStatement ps = db.prepareStatement("INSERT INTO \"PatientHealth\" " + 
                                                           "(username,date,bloodpressure1,bloodpressure2,pulse,weight,temperature,mood) " +
                                                           "VALUES (?,?,?,?,?,?,?,?)");
                ps.setString(1, username);
                ps.setTimestamp(2, sqlToday);
                ps.setInt(3, bp1);
                ps.setInt(4, bp2);
                ps.setInt(5, pulse);
                ps.setDouble(6, weight);
                ps.setDouble(7, temperature);
                ps.setInt(8, mood);

                if (ps.executeUpdate() > 0) {
                    json.setOKMessage("Sikeres feltöltés.");
                } else json.setDBError();

                ps.close();

                db.close();
            } else json.setServerError();   // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
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
