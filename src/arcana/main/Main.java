package arcana.main;

import javax.swing.SwingUtilities;
import arcana.ui.GameFrame;

public class Main {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(()->{
			new GameFrame();
		});
	}
}
