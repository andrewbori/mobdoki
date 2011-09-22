/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.user.message;

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
public class HasNewMessage extends HttpServlet {

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
        String dateString = request.getParameter("date");
        
        java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(dateString);
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            
            if (db!=null) {
                String sqlText;
                if (Sessions.MySessions().isDoctor(SSID)) {  
                    sqlText = "SELECT MAX(date), COUNT(date) " +
                              "FROM \"Message\" " +
                              "WHERE (recipient=? OR recipient IS Null) AND date>? " +
                              "GROUP BY date";
                } else {
                    sqlText = "SELECT MAX(date), coalesce(COUNT(date),0) " +
                              "FROM \"Message\" " +
                              "WHERE \"recipient\"=? AND date>? " +
                              "GROUP BY date";
                }
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setInt(1, Sessions.MySessions().getUserID(SSID));
                ps.setTimestamp(2, timestamp);
                
                ResultSet rs = ps.executeQuery();
                
                if (rs!=null && rs.next()) {
                    json.put("date", rs.getTimestamp(1).toString());
                    json.put("count", rs.getInt(2));
                    
                    json.setOK();
                    
                    rs.close();
                } else {
                    json.put("count", 0);
                    json.setOK();
                }
                
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
        } finally {
            json.write(out);     // Osszeallitott JSON kiirasa az outputra
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
