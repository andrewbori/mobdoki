/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;
import mobdoki.server.Sessions;

/**
 *
 * @author Andreas
 */
public class ImageDownload extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("image");
        OutputStream output = response.getOutputStream();
        String SSID = request.getParameter("ssid");             // felhasznalo SSID-ja
        String username = request.getParameter("username");
        String symptom = request.getParameter("symptom");
        String idString = request.getParameter("id");
        boolean large = Boolean.parseBoolean(request.getParameter("large"));
        int id=0;// = Integer.parseInt(request.getParameter("id"));

        try {
            if (!Sessions.MySessions().isValid(SSID)) {         // Ervenyes a munkamenet?
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            if (db != null) {
                if (username!=null) {
                    PreparedStatement ps = db.prepareStatement("SELECT i.id " +
                                                               "FROM \"User\" u INNER JOIN \"Image\" i ON (u.\"imageID\"=i.id) " +
                                                               "WHERE u.username LIKE ?");
                    ps.setString(1, username);
                    ResultSet rs = ps.executeQuery();
                    if (rs!=null && rs.next()) id = rs.getInt(1);               
                }
                else if (symptom!=null) {
                    PreparedStatement ps = db.prepareStatement("SELECT i.id " +
                                                               "FROM \"Symptom\" s INNER JOIN \"Image\" i ON (s.\"imageID\"=i.id) " +
                                                               "WHERE s.name LIKE ?");
                    ps.setString(1, symptom);
                    ResultSet rs = ps.executeQuery();
                    if (rs!=null && rs.next()) id = rs.getInt(1);  
                }
                else id = Integer.parseInt(idString);
                
                String sqlText;
                if (large) sqlText = "SELECT medium FROM \"Image\" WHERE id=?";
                else sqlText = "SELECT small FROM \"Image\" WHERE id=?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    byte[] imgBytes = rs.getBytes(1);

                    output.write(imgBytes);
                    output.close();
                }
                rs.close();
                ps.close();
            } else {
                throw new Exception();
            }
        } catch (Exception e) {

        } finally {
            output.close();
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
