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
import org.apache.catalina.Session;

/**
 *
 * @author Andreas
 */
public class MessageDelete extends HttpServlet {

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
        int imageID = Integer.parseInt(request.getParameter("imageID"));
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            if (db!=null) {
                
                // ----- Jogosult a felhasznalo a torlesre? ------
                String sqlText = "SELECT sender, coalesce(recipient,0) " +
                                 "FROM \"Message\" " + 
                                 "WHERE id=?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setInt(1, id);
                ResultSet results = ps.executeQuery();
                if (results!=null && results.next()) {
                    int senderID = results.getInt(1);
                    int recipientID = results.getInt(2);
                    int userID = Sessions.MySessions().getUserID(SSID);
                    
                    if(Sessions.MySessions().isDoctor(SSID)) {
                        if (userID!=senderID && userID!=recipientID && recipientID!=0) {
                            json.setUnauthorizedError();
                            return;
                        }
                    } else {
                        if (userID!=senderID && userID!=recipientID)  {
                            json.setUnauthorizedError();
                            return;
                        }
                    }
                }
                
                // ----- Uzenet torlese ------
                sqlText = "DELETE FROM \"Message\" WHERE id=?";
                ps = db.prepareStatement(sqlText);
                ps.setInt(1, id);
                ps.executeUpdate();

                // ----- Csatolt kep torlese -----
                sqlText = "DELETE FROM \"Image\" WHERE id=?";
                ps = db.prepareStatement(sqlText);
                ps.setInt(1, imageID);
                ps.executeUpdate();
                
                json.setOKMessage("Sikeres törlés.");
               
                ps.close();
                db.close();
            } else json.setServerError();   // adatbazis nem erheto el

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
