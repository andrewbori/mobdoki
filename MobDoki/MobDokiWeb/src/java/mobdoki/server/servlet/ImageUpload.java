/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mobdoki.server.servlet;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
import javax.imageio.ImageIO;
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
public class ImageUpload extends HttpServlet {

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
        
        String SSID = request.getParameter("ssid");             // felhasznalo SSID-ja
        String table = request.getParameter("table");           // tabla neve
        String symptom = request.getParameter("symptom");
        String idString = request.getParameter("id");  // tabla soranak id-je

        if (!table.equals("Message") && !table.equals("Symptom") && !table.equals("User")) return;      // SQL injection megelozese!!!
        table = "\"" + table + "\"";                                                                    // tablanev idezojelek kozott
        
        // ---- Kep feldolgozasa ----
        InputStream fi = request.getInputStream();                      // bejovo adatok
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int ch;
        while ((ch = fi.read()) != -1) {                        // bejovo adatok beolvasasa
            baos.write(ch);
        }
        byte[] buffer = baos.toByteArray();

        File picture = new File(System.currentTimeMillis()+".jpg");
        OutputStream output = new FileOutputStream(picture);    // letoltott bajtok fajlba irasa
        output.write(buffer);
        output.close();
        FileInputStream input = new FileInputStream(picture);   // a fajl egy inputstream (az adatbazisba)

        // ----- Kep lekicsinyitese (medium) ------
        BufferedImage bufferedImage = ImageIO.read(picture);

        int width,height;                                                               // Magassag es szelesseg kiszamitasa
        if (bufferedImage.getHeight()>bufferedImage.getWidth()) {
            height = 320;
            width = height * bufferedImage.getWidth() / bufferedImage.getHeight();
        } else {
            width = 320;
            height = width * bufferedImage.getHeight() / bufferedImage.getWidth();
        }

        BufferedImage resizedImage = createResizedCopy(bufferedImage, width, height);          // Lekicsiniytett kep InputStream-me
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", os);
        buffer = os.toByteArray();
        InputStream input2 = new ByteArrayInputStream(buffer);
        
        // ----- Kep lekicsinyitese (small) ------
        if (bufferedImage.getHeight()>bufferedImage.getWidth()) {               // Magassag es szelesseg kiszamitasa
            height = 64;
            width = height * bufferedImage.getWidth() / bufferedImage.getHeight();
        } else {
            width = 64;
            height = width * bufferedImage.getHeight() / bufferedImage.getWidth();
        }

        bufferedImage = createResizedCopy(bufferedImage, width, height);          // Lekicsiniytett kep InputStream-me
        os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", os);
        byte[] buffer3 = os.toByteArray();
        InputStream input3 = new ByteArrayInputStream(buffer3);
        // ----------------------------
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {                     // Ervenyes a munkamenet?
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            
            if (db != null) {
                
                // ----- Uj kep feltoltese ------
                PreparedStatement ps = db.prepareStatement("INSERT INTO \"Image\" (image,medium,small) VALUES (?,?,?) RETURNING id");
                ps.setBinaryStream(1, input, (int) picture.length());
                ps.setBinaryStream(2, input2, (int) buffer.length);
                ps.setBinaryStream(3, input3, (int) buffer3.length);
                
                ResultSet result = ps.executeQuery();           // Kep feltoltese
                if (result!=null && result.next()) {
                    int imageID = result.getInt(1);             // Feltoltott kep azonositoja
                    int id;
                    
                    if (table.equals("\"Symptom\"")) {                                          // Tunet eseten azonosito lekerdezese
                        String sqlText = "SELECT id FROM \"Symptom\" WHERE name LIKE ?";
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1, symptom);
                        result = ps.executeQuery();
                        if (result != null && result.next()) {  // Ha a tunet letezik, akkor:
                                id = result.getInt(1);              // azonosito lekerdezese
                        } else throw new Exception();
                    } else id = Integer.parseInt(idString);                                 // Egyebkent parameterben van az azonosito
                    
                    int oldImageID = 0;
                    // ----- Regi kep lekerdezese ------
                    ps = db.prepareStatement("SELECT coalesce(\"imageID\",0) FROM " + table + " WHERE id=?");
                    ps.setInt(1, id);
                    result = ps.executeQuery();             // Regi kep lekerdezese
                    if (result!=null && result.next()) {
                        oldImageID = result.getInt(1);      // Regi kep azonositoja
                    }
                    
                    // ----- Kep frissitese ------
                    ps = db.prepareStatement("UPDATE " + table + " SET \"imageID\"=? WHERE id=?");
                    ps.setInt(1, imageID);
                    ps.setInt(2, id);
                    if (ps.executeUpdate() > 0) {                           // Ha sikeresen frissitett
                        json.setOKMessage("Sikeres feltöltés.");
                        
                        if (oldImageID!=0) {                                    // Ha van regi kep
                            // ----- Regi kep torlese -----
                            ps = db.prepareStatement("DELETE FROM \"Image\" WHERE id=?");
                            ps.setInt(1, oldImageID);
                            ps.executeUpdate(); 
                        }
                    
                    } else json.setErrorMessage("A képet nem sikerült elküldeni.");

                } else json.setErrorMessage("A képet nem sikerült elküldeni.");

                ps.close();
                input.close();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
            json.setErrorMessage(e.getMessage());
        } finally {
            json.write(out);
            picture.delete();
            out.close();
        }
    }
    
    BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight) {
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaledBI.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
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
