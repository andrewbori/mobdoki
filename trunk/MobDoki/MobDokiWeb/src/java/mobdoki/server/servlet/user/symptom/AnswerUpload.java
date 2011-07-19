/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.user.symptom;

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

/**
 *
 * @author mani
 */
public class AnswerUpload extends HttpServlet {

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
        
        String PictureName = request.getParameter("picturename");       // panaszkep neve

        BufferedReader reader = request.getReader();                    // bejovo adatok
        String line = null;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {                    // Valasz beolvasasa
            builder.append(line);
        }
        String upload = builder.toString();                            // Komment String-gé alakitasa

        reader.close();

        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            if (db != null) {
                PreparedStatement ps = db.prepareStatement("SELECT imgname FROM \"PictureComment\" WHERE imgname LIKE ?");
                ps.setString(1, PictureName);
                ResultSet results = ps.executeQuery();
                if (results != null) {                                                  // Ha letezik a megadott nevu komment
                    if (results.next()) {
                        ps = db.prepareStatement("UPDATE \"PictureComment\" SET answer = ? WHERE imgname LIKE ?");
                        ps.setString(1, upload);
                        ps.setString(2, PictureName);

                        if (ps.executeUpdate() > 0) {                                       // valasz feltoltese
                            json.setOKMessage("Sikeres feltöltés.");
                        } else throw new Exception();

                        ps = db.prepareStatement("UPDATE \"Picture\" SET answered = ? WHERE imgname LIKE ?");
                        ps.setBoolean(1, true);
                        ps.setString(2, PictureName);

                        ps.executeUpdate();                                                 // megvalaszolva

                    } else {
                        json.setErrorMessage("A megadott kép nem található.");
                    }
                }

                ps.close();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        } finally {
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
