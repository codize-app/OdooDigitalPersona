import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
import java.nio.file.Files;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.Paths;
import java.util.Arrays;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Fid.Fiv;

public class Capture 
	extends JPanel
	implements ActionListener
{
	private static final long serialVersionUID = 2;
	private static final String ACT_BACK = "back";

	private JDialog       m_dlgParent;
	private JTextField    textField;
	private CaptureThread m_capture;
	private Reader        m_reader;
	private ImagePanel    m_image;
	private boolean       m_bStreaming;

	private String odooUrl;
	
	private Capture(Reader reader, boolean bStreaming){
		m_reader = reader;
		m_bStreaming = bStreaming;
		
		m_capture = new CaptureThread(m_reader, m_bStreaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);

		final int vgap = 5;
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);

		textField = new JTextField();
		add(textField);

		m_image = new ImagePanel();
		Dimension dm = new Dimension(400, 500);
		m_image.setPreferredSize(dm);
		add(m_image);
		add(Box.createVerticalStrut(vgap));
		
		JButton btnBack = new JButton("Volver");
		btnBack.setContentAreaFilled(false);
		btnBack.setActionCommand(ACT_BACK);
		btnBack.addActionListener(this);
		add(btnBack);
		add(Box.createVerticalStrut(vgap));

		odooUrl = OdooDigitalPersona.odooUrl;
	}
	
	private void StartCaptureThread(){
		m_capture = new CaptureThread(m_reader, m_bStreaming, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT);
		m_capture.start(this);
	}

	private void StopCaptureThread(){
		if(null != m_capture) m_capture.cancel();
	}
	
	private void WaitForCaptureThread(){
		if(null != m_capture) m_capture.join(1000);
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equals(ACT_BACK)){
			//event from "back" button
			//cancel capture
			StopCaptureThread();
		}
		else if(e.getActionCommand().equals(CaptureThread.ACT_CAPTURE)){
			//event from capture thread
			CaptureThread.CaptureEvent evt = (CaptureThread.CaptureEvent)e;
			boolean bCanceled = false;
			
			if(null != evt.capture_result){
				boolean bGoodImage = false;
				if(null != evt.capture_result.image){
					if(m_bStreaming && (Reader.CaptureQuality.GOOD == evt.capture_result.quality || Reader.CaptureQuality.NO_FINGER == evt.capture_result.quality)) bGoodImage = true;
					if(!m_bStreaming && Reader.CaptureQuality.GOOD == evt.capture_result.quality) bGoodImage = true;
				}
				if (bGoodImage) {
					m_image.showImage(evt.capture_result.image);
					try{
						Fmd uue = UareUGlobal.GetEngine().CreateFmd(evt.capture_result.image, Fmd.Format.ANSI_378_2004);
						String s = Base64.getEncoder().encodeToString(uue.getData());
						System.out.println(s);

						try{
							URL url = new URL(odooUrl + "/dp/api/save_fingerprint");
							String urlParameters = "{\"params\":{\"binary\":\"" + s + "\", \"badge\":\"" + textField.getText() + "\"}}";
							byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
							int postDataLength = postData.length;
							try {
								System.out.println(url);
								HttpURLConnection conn = (HttpURLConnection)url.openConnection();
								conn.setDoOutput( true );
								conn.setInstanceFollowRedirects(false);
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
							}
						}catch(MalformedURLException ex){
							System.out.println(ex);
						}

						/*byte[] decode = Base64.getDecoder().decode(s);
						System.out.println(decode);
						Fmd buue = UareUGlobal.GetImporter().ImportFmd(decode, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
						System.out.println(buue.getData());
						int falsematch_rate = UareUGlobal.GetEngine().Compare(uue, 0, buue, 0);
						System.out.println(falsematch_rate);*/

					} catch(UareUException eua){
							System.out.println(eua);
					}
					/*try{
						String b64 = new String(Files.readAllBytes(Paths.get("demo.txt")));
						try{
							Fmd uue = UareUGlobal.GetEngine().CreateFmd(evt.capture_result.image, Fmd.Format.ANSI_378_2004);
							if(UareUGlobal.GetEngine().Compare(uue, 0, uue, 0) < 21474) {
                            	System.out.println("Son el Mismo");
                        	}
						}
						catch(UareUException eua){
							System.out.println(eua);
						}
					} catch(IOException io) {
						System.out.println(io);
					}*/
					//display image
					/*System.out.println(evt.capture_result.image);
					System.out.println(evt.capture_result.image.getCbeffId());
					System.out.println(evt.capture_result.image.getData());*/
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
				MessageBox.DpError("Capture",  evt.exception);
				bCanceled = true;
			}
			else if(null != evt.reader_status){
				MessageBox.BadStatus(evt.reader_status);
				bCanceled = true;
			}
			
			if(!bCanceled){
				if(!m_bStreaming){
					//restart capture thread
					WaitForCaptureThread();
					StartCaptureThread();
				}
			}
			else{
				//destroy dialog
				m_dlgParent.setVisible(false);
			}
		}
	}

	private void doModal(JDialog dlgParent){
		//open reader
		try{
			m_reader.Open(Reader.Priority.COOPERATIVE);
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Open()", e); }
		
		boolean bOk = true;
		if(m_bStreaming){
			//check if streaming supported
			Reader.Capabilities rc = m_reader.GetCapabilities();
			if(null != rc && !rc.can_stream){
				MessageBox.Warning("This reader does not support streaming");
				bOk = false;
			}
		}
		
		if(bOk){
			//start capture thread
			StartCaptureThread();
	
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
		}
		
		//close reader
		try{
			m_reader.Close();
		}
		catch(UareUException e){ MessageBox.DpError("Reader.Close()", e); }
	}
	
	public static void Run(Reader reader, boolean bStreaming){
    	JDialog dlg = new JDialog((JDialog)null, "Coloque su dedo en el Lector", true);
    	Capture capture = new Capture(reader, bStreaming);
    	capture.doModal(dlg);
	}
}
