import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;

import com.digitalpersona.uareu.*;

public class OdooDigitalPersona 
	extends JPanel
	implements ActionListener
{
	private static final long serialVersionUID=1;
	
	private static final String ACT_SELECTION = "selection";
	private static final String ACT_CAPTURE = "capture";
	private static final String ACT_STREAMING = "streaming";
	private static final String ACT_VERIFICATION = "verification";
	private static final String ACT_IDENTIFICATION = "identification";
	private static final String ACT_ENROLLMENT = "enrollment";
	private static final String ACT_EXIT = "exit";
	
	private JDialog   m_dlgParent;
	private JTextArea m_textReader;
	
	private ReaderCollection m_collection;
	private Reader           m_reader;

	public static String[] fids;
	public static String odooUrl;
	
	private OdooDigitalPersona(){

		//Odoo POST
        System.out.println("Odoo Post");
		odooUrl = "http://localhost:8069";
		try{
			URL url = new URL (odooUrl + "/dp/api/get_connection");
			String urlParameters = "{}";
			byte[] postData = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int postDataLength = postData.length;
			try {
				System.out.println(url);
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setDoOutput( true );
				conn.setInstanceFollowRedirects( false );
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json"); 
				conn.setRequestProperty("charset", "utf-8");
				conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
				conn.setUseCaches(false);
				try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
					wr.write(postData);
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String s = in.readLine();
				String sub = s.substring(s.indexOf("\"fids\":") + 8, s.indexOf("}}"));
				fids = sub.replaceAll("\"", "").split(";");
			} catch(IOException io) {
				System.out.println(io);
				MessageBox.Warning("No es posible conectarse a Odoo: " + io);
			}
		}catch(MalformedURLException ex){
			System.out.println(ex);
		}
		// End Odoo POST

		final int vgap = 8;
		final int width = 300;
		
		BoxLayout layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		setLayout(layout);
		
		JLabel odooLogo = new JLabel();
		odooLogo.setIcon(new ImageIcon("img/odoo_logo.png"));
		add(odooLogo);
		add(Box.createVerticalStrut(vgap));
		JLabel lblReader = new JLabel("Lista de Lectores:");
		add(lblReader);
		add(Box.createVerticalStrut(vgap));
		Dimension dm = lblReader.getPreferredSize();
		dm.width = width;
		lblReader.setPreferredSize(dm);
		
		m_textReader = new JTextArea(3, 1);
		m_textReader.setEditable(false);
		JScrollPane paneReader = new JScrollPane(m_textReader);
		add(paneReader);
		add(Box.createVerticalStrut(vgap));
		
		JButton btnSelect = new JButton("Elegir nuevo Lector");
		btnSelect.setContentAreaFilled(false);
		btnSelect.setActionCommand(ACT_SELECTION);
		btnSelect.addActionListener(this);
		add(btnSelect);
		add(Box.createVerticalStrut(vgap));

		JButton btnCapture = new JButton("Capturar Huella");
		btnCapture.setContentAreaFilled(false);
		btnCapture.setActionCommand(ACT_CAPTURE);
		btnCapture.addActionListener(this);
		add(btnCapture);
		add(Box.createVerticalStrut(vgap));
		
		/*JButton btnStreaming = new JButton("Transmision");
		btnStreaming.setActionCommand(ACT_STREAMING);
		btnStreaming.addActionListener(this);
		add(btnStreaming);
		add(Box.createVerticalStrut(vgap));*/
		
		JButton btnVerification = new JButton("Verificar Huella");
		btnVerification.setContentAreaFilled(false);
		btnVerification.setActionCommand(ACT_VERIFICATION);
		btnVerification.addActionListener(this);
		add(btnVerification);
		add(Box.createVerticalStrut(vgap));
		
		/*JButton btnIdentification = new JButton("Identificacion");
		btnIdentification.setActionCommand(ACT_IDENTIFICATION);
		btnIdentification.addActionListener(this);
		add(btnIdentification);
		add(Box.createVerticalStrut(vgap));*/

		/*JButton btnEnrollment = new JButton("Inscripcion");
		btnEnrollment.setActionCommand(ACT_ENROLLMENT);
		btnEnrollment.addActionListener(this);
		add(btnEnrollment);
		add(Box.createVerticalStrut(vgap));*/

		add(Box.createVerticalStrut(vgap));
		JButton btnExit = new JButton("Salir");
		btnExit.setContentAreaFilled(false);
		btnExit.setActionCommand(ACT_EXIT);
		btnExit.addActionListener(this);
		add(btnExit);
		add(Box.createVerticalStrut(vgap));

		JLabel copy = new JLabel("powered by Exemax-Codize", SwingConstants.CENTER);
		add(copy);
		add(Box.createVerticalStrut(vgap));

		setOpaque(true);
	}

	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals(ACT_SELECTION)){
			m_reader = Selection.Select(m_collection);
			if(null != m_reader){
				m_textReader.setText(m_reader.GetDescription().name);
			}
			else{
				m_textReader.setText("");
			}
		}
		else if(e.getActionCommand().equals(ACT_CAPTURE)){
			if(null == m_reader){
				MessageBox.Warning("El Lector no ha sido Elegido");
			}
			else{
				Capture.Run(m_reader, false);
			}
		}
		else if(e.getActionCommand().equals(ACT_STREAMING)){
			if(null == m_reader){
				MessageBox.Warning("El Lector no ha sido Elegido");
			}
			else{
				Capture.Run(m_reader, true);
			}
		}
		else if(e.getActionCommand().equals(ACT_VERIFICATION)){
			if(null == m_reader){
				MessageBox.Warning("El Lector no ha sido Elegido");
			}
			else{
				Verification.Run(m_reader);
			}
		}
		else if(e.getActionCommand().equals(ACT_IDENTIFICATION)){
			if(null == m_reader){
				MessageBox.Warning("El Lector no ha sido Elegido");
			}
			else{
				Identification.Run(m_reader);
			}
		}
		else if(e.getActionCommand().equals(ACT_ENROLLMENT)){
			if(null == m_reader){
				MessageBox.Warning("El Lector no ha sido Elegido");
			}
			else{
				Enrollment.Run(m_reader);
			}
		}
		else if(e.getActionCommand().equals(ACT_EXIT)){
			m_dlgParent.setVisible(false);
		}
	}
	
	private void doModal(JDialog dlgParent){
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();
	}

	private static void createAndShowGUI() {
		OdooDigitalPersona paneContent = new OdooDigitalPersona();
		
		//initialize capture library by acquiring reader collection
		try{
			paneContent.m_collection = UareUGlobal.GetReaderCollection();
		}
		catch(UareUException e) {
			MessageBox.DpError("UareUGlobal.getReaderCollection()", e);
			return;
		}

		//run dialog
		JDialog dlg = new JDialog((JDialog)null, "Odoo Digital Persona App", true);
		paneContent.doModal(dlg);
		
		//release capture library by destroying reader collection
		try{
			UareUGlobal.DestroyReaderCollection();
		}
		catch(UareUException e) {
			MessageBox.DpError("UareUGlobal.destroyReaderCollection()", e);
		}
    }


	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

}
