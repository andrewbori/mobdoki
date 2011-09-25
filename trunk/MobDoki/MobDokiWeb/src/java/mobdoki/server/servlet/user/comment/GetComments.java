/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.user.comment;

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
public class GetComments extends HttpServlet {

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
        String table = request.getParameter("table");
        String row = request.getParameter("name");
        
        int tableID;
        if (table.equals("Sickness")) tableID=1;
        else if (table.equals("Hospital")) tableID=2;
        else return;
        
        try {
           if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }

            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db != null) {
                String sqlText = "SELECT id FROM \"" + table + "\" WHERE name LIKE ?";     // Betegseg/korhaz azonositojanak lekerese
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,row);                           // parameter beallitasa az adott tunetre
                ResultSet rs = ps.executeQuery();
                
                int rowID;
                if (rs != null && rs.next()) {            // Ha van talalat
                    rowID=rs.getInt(1);
                    rs.close();
                } else {
                    json.setErrorMessage("A megadott bejegyzés nem található.");
                    return;
                }
                
                sqlText = "SELECT u.username,c.date,c.comment " +
                          "FROM \"Comment\" c INNER JOIN \"User\" u ON (c.\"userID\"=u.id) " +
                          "WHERE c.\"tableID\"=? AND c.\"rowID\"=? " +
                          "ORDER BY c.date DESC";
  
                ps = db.prepareStatement(sqlText);
                ps.setInt(1, tableID);
                ps.setInt(2, rowID);
                rs = ps.executeQuery();
                
                JSONArray senders = new JSONArray();
                JSONArray dates = new JSONArray();
                JSONArray comments = new JSONArray();
                
                if (rs!=null) {
                    while (rs.next()) {
                        String sender = rs.getString(1);
                        Timestamp date = rs.getTimestamp(2);
                        String comment = rs.getString(3);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        StringBuilder dateString = new StringBuilder( dateFormat.format( date ) );
                        
                        senders.put(sender);
                        dates.put(dateString.toString());
                        comments.put(comment);
                    }
                    
                    json.put("sender", senders);
                    json.put("date", dates);
                    json.put("comment", comments);

                    json.setOK();                   // Sikeres lekerdezes
                } else json.setErrorMessage("Nincs comment.");        // az uzenet nem talalhato
                
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
