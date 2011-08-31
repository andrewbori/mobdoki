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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;
import org.json.JSONArray;

/**
 *
 * @author Andreas
 */
public class GetMessages extends HttpServlet {

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
        boolean inbox = Boolean.parseBoolean(request.getParameter("inbox"));
        
        try {
           if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db != null) {

                String sqlText;
                if (inbox) {
                    if (Sessions.MySessions().isDoctor(SSID)) {                 // orosnak a cimzett nelkuli levelek
                        sqlText = "SELECT m.id,m.subject,u.username,m.date,m.viewed,m.answered,coalesce(m.\"imageID\",0) " +
                                  "FROM \"Message\" m, \"User\" u " +
                                  "WHERE (m.recipient=? OR m.recipient IS Null) AND m.sender=u.id " +
                                  "ORDER BY m.date DESC";
                    } else {
                        sqlText = "SELECT m.id,m.subject,u.username,m.date,m.viewed,m.answered,coalesce(m.\"imageID\",0) " +
                                  "FROM \"Message\" m, \"User\" u " +
                                  "WHERE m.recipient=? AND m.sender=u.id " +
                                  "ORDER BY m.date DESC";
                    }
                }
                else  {
                    sqlText = "SELECT m.id,m.subject,coalesce(u.username,'[Minden orvos]'),m.date,m.viewed,m.answered,coalesce(m.\"imageID\",0) " +
                              "FROM \"Message\" m LEFT JOIN \"User\" u ON (m.recipient=u.id) " +
                              "WHERE m.sender=? " +
                              "ORDER BY m.date DESC";
                }
                
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setInt(1, Sessions.MySessions().getUserID(SSID));
                ResultSet rs = ps.executeQuery();
                
                JSONArray ids = new JSONArray();
                JSONArray subjects = new JSONArray();
                JSONArray senders = new JSONArray();
                JSONArray dates = new JSONArray();
                JSONArray vieweds = new JSONArray();
                JSONArray answereds = new JSONArray();
                JSONArray images = new JSONArray();
                
                if (rs!=null) {
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        String subject = rs.getString(2);
                        String sender = rs.getString(3);
                        Timestamp date = rs.getTimestamp(4);
                        boolean viewed = rs.getBoolean(5);
                        boolean answered = rs.getBoolean(6);
                        int image = rs.getInt(7);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        StringBuilder dateString = new StringBuilder( dateFormat.format( date ) );
                        
                        
                        ids.put(id);
                        subjects.put(subject);
                        senders.put(sender);
                        dates.put(dateString.toString());
                        vieweds.put(viewed);
                        answereds.put(answered);
                        images.put(image);
                    }
                    
                    json.put("id", ids);
                    json.put("subject", subjects);
                    json.put("sender", senders);
                    json.put("date", dates);
                    json.put("viewed", vieweds);
                    json.put("answered", answereds);
                    json.put("image", images);
                    json.setOK();                   // Sikeres lekerdezes
                } else json.setErrorMessage("Nincs üzenet.");        // az uzenet nem talalhato

                rs.close();
                ps.close();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            

        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
            json.setErrorMessage(e.getMessage());
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
