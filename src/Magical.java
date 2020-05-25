import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.PageAttributes.OriginType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Magical extends Canvas {
	
	ImageFrame original;
	ImageFrame energy;
	ImageFrame resalted;
	
	BufferedImage xdxd;
	BufferedImage floods;
	
	Color discriminante;
	boolean isDiscriminante;
	private int permisividad = 15000;
	Color contrast = Color.BLUE;  // Color usado como contraste 
	float epsilon  = 0.2f;	  // Factor de permisión para el metodo de FloodFill

	
	int h, w; 	// Altura y ancho de la imagen
	int[][] e;	// Energía de los pixeles de las imagenes


	int max;
	static JFrame f;
	JButton b;
	public Magical(JFrame f, JButton b) {
		this.f = f;
		this.b = b;
	}

    static BufferedImage img = null;


	public static void main(String[] args) {
        f=new JFrame();
        JButton b=new JButton("Seleccionar Imagen");  
        Magical m=new Magical(f,b);  
        b.setBounds(600, 675, 300, 50);
        f.setSize(1500,800);  
        f.getContentPane().setLayout(new BorderLayout());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(b);
        f.getContentPane().add(panel, BorderLayout.SOUTH);
        f.getContentPane().add(m, BorderLayout.CENTER);
        
        
        m.discriminante = new Color(16, 187, 32);
        m.isDiscriminante = true;
        ColorChooserButton colorButton = new ColorChooserButton(m.discriminante);
        JCheckBox cb = new JCheckBox("Usar color primario");    
        cb.setSelected(true);
        cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == 1) {
					colorButton.setEnabled(true);
					m.discriminante = colorButton.getSelectedColor();
					m.isDiscriminante = true;
				}
				else {
					colorButton.setEnabled(false);
					m.isDiscriminante = false;
				}
				
			}
		});
        
        colorButton.addColorChangedListener(new ColorChangedListener() {
            @Override
            public void colorChanged(Color newColor) {
                    m.discriminante = newColor;
            }
        });

        panel.add(cb);
        panel.add(colorButton);
        
        JButton reprint = new JButton("Repintar");
        
        reprint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				print(m);
			}
		});
        
        panel.add(reprint);
        b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		        JFileChooser fileChooser = new JFileChooser();
		        fileChooser.setCurrentDirectory(new File("bin\\Labels"));
		        int result = fileChooser.showOpenDialog(m);
		        if (result == JFileChooser.APPROVE_OPTION) {
		            File selectedFile = fileChooser.getSelectedFile();
		            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
		            if(!(selectedFile.getAbsolutePath().endsWith(".png") || selectedFile.getAbsolutePath().endsWith(".jpg") || selectedFile.getAbsolutePath().endsWith(".jpeg")))
		            	return;
		            
		    		try {
		    			img = ImageIO.read(selectedFile);
		    		} catch (IOException e) {e.printStackTrace();}
//		            Image i=t.getImage(image);
		    		
		    		print(m);
//		            f.add(b);
		        }
			}
		});
        	
        f.setVisible(true);  
	}
	
	
	public static void print(Magical m) {
		m.original = new ImageFrame(img, 400, 550);
		
        
		m.energyCalculate(img);
		
		m.energy = new ImageFrame(m.xdxd, 400, 550);
		
		m.floodCalculate(img);
		
		m.resalted = new ImageFrame(m.floods, 400, 550);
        f.add(m, BorderLayout.CENTER);
	}
	
	
    private void floodCalculate(BufferedImage img) {
    	floods = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
    	max = 0;
		h = img.getHeight();
		w = img.getWidth();
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				floods.setRGB(i, j, img.getRGB(i, j));
			}
		}
		
		
		floodfill(0, 0);
		
	}



	private void energyCalculate(BufferedImage img) {
		/*
		 * Calculo de la energía de los pixeles de la imagen usando dual gradient energy function
		 */
    	xdxd = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
    	max = 0;
		h = img.getHeight();
		w = img.getWidth();
		e = new int[w][h];
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
		
		System.out.println("MAX " + max);
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				float xd = (float) e[i][j] / (float) max;
				//xd = 1 - xd;
				Color newColor = new Color(xd, xd, xd);
				xdxd.setRGB(i, j, newColor.getRGB());
			}
		}
		
		
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
			floods.setRGB(x, y, contrast.getRGB()); // Pinta el pixel con el color de contraste

			for (int k = 0; k < 4; k++) {
				int newPosX = x + xd[k];
				int newPosY = y + xy[k];

				if (newPosX < 0 || newPosX >= w || newPosY < 0 || newPosY >= h)
					continue;
				Color color = new Color(floods.getRGB(newPosX, newPosY));
				if (vis[newPosX][newPosY] != true) {
					// Se añade una condición que fuerza al algoritmo a considerar colores alejados de los verdes como un mismo color
					if (Math.abs(e[newPosX][newPosY] - e[x][y]) < max * epsilon || 
							(isNear(color) || !isDiscriminante)) {
						stack.push((newPosX) * h + y + xy[k]);
					}
				}
			}
		}
	}



	private boolean isNear(Color color) {
		int error = 0;
		
		error += (color.getGreen() - discriminante.getGreen())*(color.getGreen() - discriminante.getGreen());
		error += (color.getRed() - discriminante.getRed())*(color.getRed() - discriminante.getRed());
		error += (color.getBlue() - discriminante.getBlue())*(color.getBlue() - discriminante.getBlue());
		
//		System.out.println("error: " + error);
		return error > permisividad ;
	}



	public void paint(Graphics g) {  
    	if(original == null) return;
        Toolkit t=Toolkit.getDefaultToolkit();  
        g.drawImage(original.img, 100 + original.offX,   50 + original.offY,this);
        g.drawImage(energy.img, 550 + energy.offX,   50 + energy.offY,this);
        g.drawImage(resalted.img, 1000 + resalted.offX,  50 + resalted.offY,this);

        g.drawRect(100, 50, 400, 550);
        g.drawRect(550, 50, 400, 550);
        g.drawRect(1000, 50, 400, 550);
        
        f.doLayout();
    }  
    
}

class ImageFrame extends Image{
	Image img;
	
	int offX, offY;
	
	public ImageFrame(BufferedImage buff, int w, int h) {
		
		int W = buff.getWidth();
		int H = buff.getHeight();
		double p1,p2, p;
		
		p1 = (double)w/W;
		p2 = (double)h/H;
		
		System.out.println("X = " + W);
		System.out.println("Y = " + H);
		
		if(H*p1 > h)
			p = p2;
		else
			p = p1;
		
		int realW = (int) (W*p);
		int realH = (int) (H*p);

		img = buff.getScaledInstance(realW, realH, ImageObserver.ALLBITS);
		
		System.out.println("X = " + realW);
		System.out.println("Y = " + realH);
		offX = Math.max(w-realW, 0);
		offY = Math.max(h-realH, 0);
		
		
		offX /= 2;
		offY /= 2;
	
		System.out.println("X = " + offX);
		System.out.println("Y = " + offY);
	}
	
	
	
	
	
	

	@Override
	public Graphics getGraphics() {
		return img.getGraphics();
	}

	@Override
	public int getHeight(ImageObserver observer) {
		return img.getHeight(observer);
	}

	@Override
	public Object getProperty(String name, ImageObserver observer) {
		return img.getProperty(name, observer);
	}

	@Override
	public ImageProducer getSource() {
		return img.getSource();
	}

	@Override
	public int getWidth(ImageObserver observer) {
		return img.getWidth(observer);
	}
	
}
interface ColorChangedListener {
    public void colorChanged(Color newColor);
}


class ColorChooserButton extends JButton {

    private Color current;

    public ColorChooserButton(Color c) {
        setSelectedColor(c); 
        setText("Color Primario");
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                Color newColor = JColorChooser.showDialog(null, "Choose a color", current);
                setSelectedColor(newColor);
            }
        });
    }

    public Color getSelectedColor() {
        return current;
    }

    public void setSelectedColor(Color newColor) {
        setSelectedColor(newColor, true);
    }

    public void setSelectedColor(Color newColor, boolean notify) {

        if (newColor == null) return;

        current = newColor;
        setIcon(createIcon(current, 16, 16));
        repaint();

        if (notify) {
            // Notify everybody that may be interested.
            for (ColorChangedListener l : listeners) {
                l.colorChanged(newColor);
            }
        }
    }



    private List<ColorChangedListener> listeners = new ArrayList<ColorChangedListener>();

    public void addColorChangedListener(ColorChangedListener toAdd) {
        listeners.add(toAdd);
    }

    public static  ImageIcon createIcon(Color main, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(main);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}