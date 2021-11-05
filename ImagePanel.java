import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.digitalpersona.uareu.*;
import com.digitalpersona.uareu.Fid.Fiv;

public class ImagePanel 
	extends JPanel
{
	private static final long serialVersionUID = 5;
	private BufferedImage m_image;
	private int w;
	private int h;
	
	public void showImage(Fid image){
		Fiv view = image.getViews()[0];
		m_image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		m_image.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
		w = view.getWidth();
		h = view.getHeight();
		repaint();
	} 
	
	public void paint(Graphics g) {
      if (isOpaque()){
         Graphics dc = g.create();
         dc.setColor(getBackground());
         dc.fillRect(0, 0, getWidth(), getHeight());
         dc.dispose();
      }
      g.drawImage(m_image, (400-w)/2, (500-h)/2, null);
	}

}
