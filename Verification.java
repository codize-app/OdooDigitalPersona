import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.awt.image.BufferedImage;

import java.util.Base64;

import com.digitalpersona.uareu.*;

public class Verification 
	extends JPanel
	implements ActionListener
{
	private static final long serialVersionUID = 6;
	private static final String ACT_BACK = "back";

	private CaptureThread m_capture;
	private Reader  m_reader;
	private Fmd[]   m_fmds;
	private JDialog m_dlgParent;
	private JTextArea m_text;
	
	private final String m_strPrompt1 = "Verification started\n    put any finger on the reader\n\n";
	private final String m_strPrompt2 = "    put the same or any other finger on the reader\n\n";

	private String[] fids;
	private String odooUrl;

	private Verification(Reader reader){
		m_reader = reader;
		m_fmds = new Fmd[2]; //two FMDs to perform comparison

		final int vgap = 5;
		final int width = 380;
		
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		
		m_text = new JTextArea(22, 1);
		m_text.setEditable(false);
		JScrollPane paneReader = new JScrollPane(m_text);
		add(paneReader);
		Dimension dm = paneReader.getPreferredSize();
		dm.width = width;
		paneReader.setPreferredSize(dm);
		
		add(Box.createVerticalStrut(vgap));
		
		JButton btnBack = new JButton("Volver");
		btnBack.setContentAreaFilled(false);
		btnBack.setActionCommand(ACT_BACK);
		btnBack.addActionListener(this);
		add(btnBack);
		add(Box.createVerticalStrut(vgap));

		setOpaque(true);

		fids = OdooDigitalPersona.fids;
		odooUrl = OdooDigitalPersona.odooUrl;
	}

	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals(ACT_BACK)){
			//cancel capture
			StopCaptureThread();
		}
		else if(e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)){
			//process result
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent)e;
			if(ProcessCaptureResult(evt)){
				//restart capture thread
				WaitForCaptureThread();
				StartCaptureThread();
			}
			else{
				//destroy dialog
				m_dlgParent.setVisible(false);
			}
		}
	}
	
	private void StartCaptureThread(){
		m_capture = new CaptureThread(m_reader, false, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
		m_capture.start(this);
	}
	
	private void StopCaptureThread(){
		if(null != m_capture) m_capture.cancel();
	}
	
	private void WaitForCaptureThread(){
		if(null != m_capture) m_capture.join(1000);
	}
	
	private boolean ProcessCaptureResult(CaptureThread.CaptureEvent evt){
		boolean bCanceled = false;

		if(null != evt.capture_result){
			if(null != evt.capture_result.image && Reader.CaptureQuality.GOOD == evt.capture_result.quality){

				//extract features
				Engine engine = UareUGlobal.GetEngine();

				for (int i = 0; i < fids.length; i++) {
					String[] as = fids[i].split(",");
					System.out.println(as[0]);
					System.out.println(as[1]);

					try{
						if (as[0].length() > 3) {
							Fmd uue = UareUGlobal.GetEngine().CreateFmd(evt.capture_result.image, Fmd.Format.ANSI_378_2004);
							byte[] decode = Base64.getDecoder().decode(as[0]);
							Fmd buue = UareUGlobal.GetImporter().ImportFmd(decode, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);

							int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000;
							int falsematch_rate = UareUGlobal.GetEngine().Compare(uue, 0, buue, 0);
							
							if(falsematch_rate < target_falsematch_rate) {
								m_text.append("Concidencia con: " + as[1] + "\n");

								try{
									URL url = new URL (odooUrl + "/dp/api/hr_check");
									String urlParameters = "{\"params\":{\"badge\":\"" + as[1] + "\"}}";
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
										System.out.println(in.readLine());
									} catch(IOException io) {
										System.out.println(io);
										MessageBox.Warning("Algo salió mal: " + io);
									}
								}catch(MalformedURLException ex){
									System.out.println(ex);
								}

								break;
							}
						}
					}
					catch(UareUException e){ MessageBox.DpError("Engine.CreateFmd()", e); }
				}
			}
			else if(Reader.CaptureQuality.CANCELED == evt.capture_result.quality){
				//capture or streaming was canceled, just quit
				bCanceled = true;
			}
			else{
				//bad quality
				MessageBox.BadQuality(evt.capture_result.quality);
			}
		}
		else if(null != evt.exception){
			//exception during capture
			MessageBox.DpError("Capture", evt.exception);
			bCanceled = true;
		}
		else if(null != evt.reader_status){
			//reader failure
			MessageBox.BadStatus(evt.reader_status);
			bCanceled = true;
		}

		return !bCanceled;
	}

	private void doModal(JDialog dlgParent){
		//open reader
		try{
			m_reader.Open(Reader.Priority.COOPERATIVE);
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Open()", e); }
		
		//start capture thread
		StartCaptureThread();

		//put initial prompt on the screen
		m_text.append(m_strPrompt1);
		
		//bring up modal dialog
		m_dlgParent = dlgParent;
		m_dlgParent.setContentPane(this);
		m_dlgParent.pack();
		m_dlgParent.setLocationRelativeTo(null);
		m_dlgParent.toFront();
		m_dlgParent.setVisible(true);
		m_dlgParent.dispose();
		
		//cancel capture
		StopCaptureThread();
		
		//wait for capture thread to finish
		WaitForCaptureThread();
		
		//close reader
		try{
			m_reader.Close();
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Close()", e); }
	}
	
	public static void Run(Reader reader){
    	JDialog dlg = new JDialog((JDialog)null, "Verification", true);
    	Verification verification = new Verification(reader);
    	verification.doModal(dlg);
	}
}
