package argus.modules;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Screenshot {
	public static int FULLSIZE = 0;
	
	public static BufferedImage take() throws HeadlessException, AWTException {
		BufferedImage screen = new Robot().createScreenCapture(new Rectangle(
				Toolkit.getDefaultToolkit().getScreenSize()));
		return screen;
	}
	public static BufferedImage take(int width, int height) throws HeadlessException, AWTException {
		return resize(take(), width, height);
	}

	public static BufferedImage resize(BufferedImage original, int newWidth, int newHeight) {
		if (newWidth < newHeight) {
			int temp = newHeight;
			newHeight = newWidth;
			newWidth = temp;
		}
		BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
	    Graphics2D g = resized.createGraphics();
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.drawImage(original, 0, 0, newWidth, newHeight, 0, 0, original.getWidth(), original.getHeight(), null);
	    g.dispose();
		return resized;
	}

	public static class ImagePanel extends JFrame {
		private static final long serialVersionUID = 1L;

		public ImagePanel(BufferedImage bi) {
			this.setSize(bi.getWidth(), bi.getHeight());
			JLabel jLabel = new JLabel(new ImageIcon(bi));
			JPanel jPanel = new JPanel();
			jPanel.add(jLabel);
			this.add(jPanel);
		}
	}

	public static void main(String args[]) throws HeadlessException,
			AWTException {
		BufferedImage screen = Screenshot.take(800, 480);
		new ImagePanel(screen).setVisible(true);

	}
}