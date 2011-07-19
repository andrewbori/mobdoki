/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet.medicalinfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class AddPictureToSymptom extends HttpServlet {

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
        
        String symptom = request.getParameter("symptom");           // A tunet neve
        InputStream fi = request.getInputStream();                  // bejovo adatok
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ch;
        while ((ch = fi.read()) != -1) {                            // bejovo adatok beolvasasa
            baos.write(ch);
        }
        byte[] map = baos.toByteArray();

        File picture = new File("myfile.jpg");
        OutputStream output = new FileOutputStream(picture);    // letoltott bajtok fajlba irasa
        output.write(map);
        output.close();
        FileInputStream input = new FileInputStream(picture);   // a fajl egy inputstream (az adatbazisba)

        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db != null) {
                String sqlText = "SELECT name FROM \"Symptom\" WHERE name LIKE ?";          // Megadott tunet keresese
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1, symptom);
                ResultSet results = ps.executeQuery();
                if (results != null) {
                    if (results.next()) {                                                   // Ha a tunet letezik, akkor:
                        sqlText = "UPDATE \"Symptom\" SET img = ?  WHERE name LIKE ?";          // Kep feltoltese az adatbazisba
                        ps = db.prepareStatement(sqlText);
                        ps.setBinaryStream(1, input, (int) picture.length());
                        ps.setString(2, symptom);

                        if (ps.executeUpdate() > 0) {
                            json.setOKMessage("Sikeres feltöltés.");
                        } else json.setErrorMessage("Sikertelen feltöltés.");

                        ps.close();
                        input.close();
                    } else {
                        json.setErrorMessage("A megadott tünet nem található.");
                    }
                    results.close();
                } else json.setDBError();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        } finally {
            json.write(out);
            picture.delete();
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
