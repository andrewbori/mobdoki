/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.user.message;

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
public class MessageUpload extends HttpServlet {

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
        
        int to = Integer.parseInt(request.getParameter("to"));
        String subject = request.getParameter("subject");
        String idString = request.getParameter("id");
        String SSID = request.getParameter("ssid");
        
        BufferedReader reader = request.getReader();                // bejovo adatok
        String line = null;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {                    // Uezenet beolvasasa
            builder.append(line);
            builder.append("\n");
        }
        String text =  builder.toString();                              // Uzenet String-gé alakitasa
        reader.close();
        
        java.util.Date today = new java.util.Date();                // idobelyeg: pillanatnyi ido
        java.sql.Timestamp date = new java.sql.Timestamp(today.getTime());
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            if (db!=null) {
                
                PreparedStatement ps;
                if (idString!=null) {                             // Ha ez egy valaszlevel
                    int idAnswered = Integer.parseInt(idString);
                    
                    String sqlText = "UPDATE \"Message\" SET answered=true WHERE id=?";
                    ps = db.prepareStatement(sqlText);
                    ps.setInt(1,idAnswered);
                    ps.executeUpdate();
                }
                
                if (to!=0) {                                   // Ha van cimzett (orvos felhasznalonak)
                    ps = db.prepareStatement("INSERT INTO \"Message\" (sender,recipient,date,subject,text) " +
                                             "VALUES (?, ?, ?, ?, ?) " +
                                             "RETURNING id");
                    ps.setInt(1, Sessions.MySessions().getUserID(SSID));
                    ps.setInt(2, to);
                    ps.setTimestamp(3, date);
                    ps.setString(4, subject);
                    ps.setString(5, text);
                }
                else {
                    ps = db.prepareStatement("INSERT INTO \"Message\" (sender,date,subject,text) " +
                                             "VALUES (?, ?, ?, ?)" +
                                             "RETURNING id");
                    ps.setInt(1, Sessions.MySessions().getUserID(SSID));
                    ps.setTimestamp(2, date);
                    ps.setString(3, subject);
                    ps.setString(4, text);
                }
                
                ResultSet result = ps.executeQuery();
                if (result!=null && result.next()) {
                    int id = result.getInt(1);
                    json.put("id", id);
                    json.setOKMessage("Sikeres küldés.");
                } else json.setErrorMessage("Sikertelen küldés.");
               
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
