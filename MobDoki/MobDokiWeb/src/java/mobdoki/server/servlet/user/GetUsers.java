/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.user;

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
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;

/**
 *
 * @author mani
 */
public class GetUsers extends HttpServlet {
   
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
        String usertype = request.getParameter("usertype");
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isDoctorAndValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT u.id,u.username " +
                                 "FROM \"User\" u,\"UserType\" ut " +
                                 "WHERE ut.name=? AND u.\"usertypeID\"=ut.id";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1, usertype);
                ResultSet results = ps.executeQuery();
                if (results != null) {
                    JSONArray usernames = new JSONArray();          // felhasznalonevek tombje
                    JSONArray ids = new JSONArray();                // felhasznaloazonositok tombje
                    while (results.next()) {
                        ids.put(results.getInt(1));                     // felhasznaloazonosito a tombbe
                        usernames.put(results.getString(2));            // felhasznalonev a tombbe
                    }
                    json.put("ids", ids);                           // felhasznaloazonositok tombje a kimenetre
                    json.put("usernames", usernames);               // felhasznalonevek tombje a kimenetre
                    json.setOK();                                   // Sikeres vegrehajtas
                    results.close();
                } else json.setDBError();
                
                ps.close();
                db.close();
                
            } else json.setErrorMessage("Nem létezik ilyen tipusú felhasználó.");
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
