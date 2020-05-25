import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import javax.imageio.ImageIO;


/*
 *	@author Marlon Alexander Estupiñan Galindo
 *	@author Andres Gustavo Osorio Jimenez
 */


public class ReadSingleImage extends Applet implements MouseListener {

	private BufferedImage img;
	String name, folder;
	
	int h, w; 	// Altura y ancho de la imagen
	int[][] e;	// Energía de los pixeles de las imagenes
	
	Color contrast = Color.BLUE;  // Color usado como contraste 
	float epsilon  = 0.001f;	  // Factor de permisión para el metodo de FloodFill

	int max;
	
	boolean showEnergy = true;

	public void init() {
		try {
			name = "1.png";	// Nombre de la imagen con su tipo de dato
			folder = "images";  // Carpeta en donde se encuentra las imagenes
			URL url = new URL(getCodeBase(), folder + "/" + name);
			img = ImageIO.read(url);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		h = img.getHeight();
		w = img.getWidth();

		e = new int[w][h];

		max = 0;

		/*
		 * Calculo de la energía de los pixeles de la imagen usando dual gradient energy function
		 */
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				// horizontal

				Color a = new Color(j > 0 ? img.getRGB(i, j - 1) : img.getRGB(i, j));
				Color b = new Color(j < h - 1 ? img.getRGB(i, j + 1) : img.getRGB(i, j));

				int diffR = a.getRed() - b.getRed();
				int diffG = a.getGreen() - b.getGreen();
				int diffB = a.getBlue() - b.getBlue();

				e[i][j] += diffR * diffR + diffG * diffG + diffB * diffB;

				// vertical

				a = new Color(i > 0 ? img.getRGB(i - 1, j) : img.getRGB(i, j));
				b = new Color(i < w - 1 ? img.getRGB(i + 1, j) : img.getRGB(i, j));

				diffR = a.getRed() - b.getRed();
				diffG = a.getGreen() - b.getGreen();
				diffB = a.getBlue() - b.getBlue();

				e[i][j] += diffR * diffR + diffG * diffG + diffB * diffB;

				max = Math.max(max, e[i][j]);
			}
		}

		/*
		 *  Impresión de la energia de los pixeles en la escala de los grises
		 */
		
		if(showEnergy) {
			for (int i = 0; i < w; i++) {
				for (int j = 0; j < h; j++) {
					float xd = (float) e[i][j] / (float) max;
	//				xd = 1 - xd;
					Color newColor = new Color(xd, xd, xd);
					img.setRGB(i, j, newColor.getRGB());
				}
			}
		}

		addMouseListener(this);

	}

	public void mousePressed(MouseEvent e) {
		floodfill(e.getX(), e.getY());
		repaint();
	}
	
	
	

	int[] xd = { 0, 0, 1, -1 };
	int[] xy = { 1, -1, 0, 0 };

	void floodfill(int i, int j) {
		LinkedList<Integer> stack = new LinkedList<Integer>();

		stack.push(i * h + j);

		boolean[][] vis = new boolean[w][h];

		while (!stack.isEmpty()) {
			int val = stack.pop();

			int x = val / h;
			int y = val % h;

			vis[x][y] = true;
			img.setRGB(x, y, contrast.getRGB()); // Pinta el pixel con el color de contraste

			for (int k = 0; k < 4; k++) {
				int newPosX = x + xd[k];
				int newPosY = y + xy[k];
				
				if (newPosX < 0 || newPosX >= w || newPosY < 0 || newPosY >= h)
					continue;
				if (vis[newPosX][newPosY] != true) {
					if (Math.abs(e[newPosX][newPosY] - e[x][y]) < max * epsilon) {
						stack.push((newPosX) * h + y + xy[k]);
					}
				}
			}
		}
	}
	
	
	

	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, null);
		
		/*
		 * Guardar las imagenes
		 */
		try {
			ImageIO.write(img, name.substring(name.indexOf(".") + 1), new File("Outputs/" + "Contrast"+ name)); 
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}
