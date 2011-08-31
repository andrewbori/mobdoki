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
public class MessageDownload extends HttpServlet {

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
        
        int id = Integer.parseInt(request.getParameter("id"));
        boolean inbox = Boolean.parseBoolean(request.getParameter("inbox"));
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }      
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db != null) {
                String sqlText = "SELECT m.sender,u1.username,m.recipient,m.date,m.subject,m.text " +
                                 "FROM \"Message\" m, \"User\" u1 " +
                                 "WHERE m.id=? AND m.sender=u1.id";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                
                if (rs!=null && rs.next()) {
                    int senderID = rs.getInt(1);
                    String sender = rs.getString(2);    // talalt uzenet
                    String recipient = rs.getString(3);
                    String dateString = rs.getTimestamp(4).toString();
                    String subject = rs.getString(5);
                    String text = rs.getString(6);
                             
                    json.put("sender", sender);         // valasz a kimenetre
                    json.put("senderID", senderID);
                    json.put("recipient", recipient);
                    json.put("date", dateString);
                    json.put("subject", subject);
                    json.put("text", text);
                    json.put("id", id);
                    json.setOK();                   // Sikeres lekerdezes
                    
                    if (inbox) {
                        sqlText = "UPDATE \"Message\" SET viewed=true " +
                                  "WHERE id=?";
                        ps = db.prepareStatement(sqlText);
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                } else json.setErrorMessage("Az üzenet nem található.");        // az uzenet nem talalhato

                rs.close();
                ps.close();
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
