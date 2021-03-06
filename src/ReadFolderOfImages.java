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
 *	@author Marlon Alexander Estupi�an Galindo
 *	@author Andres Gustavo Osorio Jimenez
 */


public class ReadFolderOfImages extends Applet implements MouseListener {

	private BufferedImage img;
	String name, folder;

	int h, w; 	// Altura y ancho de la imagen
	int[][] e;	// Energ�a de los pixeles de las imagenes

	Color contrast = Color.BLUE;  // Color usado como contraste 
	float epsilon  = 0.2f;	  // Factor de permisi�n para el metodo de FloodFill

	int max;

	public void init() {
		folder = "Labels"; // Carpeta en donde se encuentra las imagenes
		File folderS = new File(folder + "/");
		File[] listOfFiles = folderS.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				System.out.println(file.getName());

				try {
					name = file.getName();

					URL url = new URL(getCodeBase(), folder + "/" + name);
					img = ImageIO.read(url);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					System.out.println("FAIOS");
				}

				h = img.getHeight();
				w = img.getWidth();

				e = new int[w][h];

				int max = 0;


				/*
				 * Calculo de la energ�a de los pixeles de la imagen usando dual gradient energy function
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
				 *  Impresi�n de la energia de los pixeles en la escala de los grises
				 */
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						float xd = (float) e[i][j] / (float) max;
						//xd = 1 - xd;
						Color newColor = new Color(xd, xd, xd);
						img.setRGB(i, j, newColor.getRGB());
					}
				}

				/*
				 * Guardar las imagenes con el mapa de energ�a
				 */
				try {
					ImageIO.write(img, name.substring(name.indexOf(".")+1), new File("Outputs/energy" + name));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				try {
					name = file.getName();
					
					URL url = new URL(getCodeBase(), folder + "/" + name);
					img = ImageIO.read(url);
				} catch (IOException e) {
					System.out.println(e.getMessage());
					System.out.println("FAIOS");
				}
				 floodfill(0, 0);
				/*
				 * Guardar las imagenes con el floodFill corrido
				 */
				try {
					ImageIO.write(img, name.substring(name.indexOf(".")+1), new File("Outputs/contrast" + name));
				} catch (IOException e1) {
					e1.printStackTrace();
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
				Color color = new Color(img.getRGB(newPosX, newPosY));
				if (vis[newPosX][newPosY] != true) {
					// Se a�ade una condici�n que fuerza al algoritmo a considerar colores alejados de los verdes como un mismo color
					if (Math.abs(e[newPosX][newPosY] - e[x][y]) < max * epsilon || 
							(color.getGreen() - color.getRed() < 30 && color.getGreen() - color.getBlue() < 30)) {
						stack.push((newPosX) * h + y + xy[k]);
					}
				}
			}
		}
	}


	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, null);

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
