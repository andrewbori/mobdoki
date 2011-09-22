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
import java.util.Calendar;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;



/**
 *
 * @author Andreas
 */
public class Login extends HttpServlet {
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
           
        JSONObj json = new JSONObj();

        String username = request.getParameter("username");
        int password = Integer.parseInt(request.getParameter("password"));
        
        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            
            if (db!=null) {
                String sqlText = "SELECT u.id, u.\"usertypeID\", ut.name, u.lastmailcheck " +
                                 "FROM \"User\" u INNER JOIN \"UserType\" ut ON (u.\"usertypeID\"=ut.id)" + 
                                 "WHERE username=? AND password=?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,username);
                ps.setInt(2,password);
                ResultSet results = ps.executeQuery();
                if (results != null && results.next()) {            // felhasznalo tipusa es ID-ja
                    int userid = results.getInt(1);
                    int usertypeid = results.getInt(2);
                    String usertype = results.getString(3);
                    String lastmailcheck = results.getTimestamp(4).toString();
                    json.put("userid", userid);
                    json.put("usertypeid", usertypeid);
                    json.put("usertype", usertype);
                    json.put("lastmailcheck", lastmailcheck);
                    json.setOK();           // status: OK
                    
                    // Felhasznalo munkamenetenek letrehozasa
                    Calendar calendar = Calendar.getInstance();     // 30 perces munkamenet
                    calendar.add(Calendar.MINUTE, 30);
                    Date validTime = calendar.getTime();
                    HttpSession session = request.getSession();     // munkamenet azonositoja
                    while (Sessions.MySessions().addSession(session.getId(), userid, username, usertype, validTime)==false) {
                        session.invalidate();           // Ha van mar ilyen SSID, akkor uj kerese
                        session = request.getSession();
                    }
                    json.put("ssid", session.getId());
                    
                } else json.setErrorMessage("A megadott felhasználónév vagy jelszó helytelen.");
                
                results.close();
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

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
        return "Login";
    }
    // </editor-fold>

}
