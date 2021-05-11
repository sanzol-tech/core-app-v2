/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: April 2021
 *
 */
package sanzol.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Captcha
{
	private final Logger LOG = LoggerFactory.getLogger(Captcha.class);

	private static final String[] CRAZY_FONTS =
		{ "AtomicClockRadio.ttf", "DotPreschool.ttf", "GhostCode.ttf", "Phantomime.ttf", "PWChristmasfont.ttf", "SubexKG.TTF", "WarTextInverse.ttf" };

	private int width = 208;
	private int height = 52;
	private boolean showGrid = true;
	private int gridSize = 17;
	private int rotationAmplitude = 10;
	private int scaleAmplitude = 20;

	private int lenght;
	private byte[] image;
	private String text;

	public byte[] getImage()
	{
		return image;
	}

	public String getText()
	{
		return text;
	}

	public Captcha withWidth(int width)
	{
		this.width = width;
		return this;
	}

	public Captcha withHeight(int height)
	{
		this.height = height;
		return this;
	}

	public Captcha withShowGrid(boolean showGrid)
	{
		this.showGrid = showGrid;
		return this;
	}

	public Captcha withGridSize(int gridSize)
	{
		this.gridSize = gridSize;
		return this;
	}

	public Captcha withRotationAmplitude(int rotationAmplitude)
	{
		this.rotationAmplitude = rotationAmplitude;
		return this;
	}

	public Captcha withScaleAmplitude(int scaleAmplitude)
	{
		this.scaleAmplitude = scaleAmplitude;
		return this;
	}

	public boolean isValid(String text)
	{
		if (text == null || text.isEmpty())
		{
			return false;
		}

		return text.equals(this.text);
	}

	public boolean isValidIgnoreCase(String text)
	{
		if (text == null || text.isEmpty())
		{
			return false;
		}

		return text.equalsIgnoreCase(this.text);
	}

	public static Captcha withLenght(int lenght)
	{
		Captcha captcha = new Captcha();
		captcha.lenght = lenght;
		return captcha;
	}

	public Captcha create() throws IOException
	{
		text = RandomStringUtils.randomAlphanumeric(lenght);
		BufferedImage bufferedImage = createCaptcha(text.toCharArray());

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", stream);
		image = stream.toByteArray();

		return this;
	}

	private BufferedImage createCaptcha(char[] text)
	{
		if (text == null || text.length == 0)
		{
			throw new IllegalArgumentException("No captcha text given");
		}

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setBackground(new Color(1f, 1f, 1f, .5f));

		clearCanvas(g2d);

		if (showGrid)
		{
			g2d.setColor(Color.LIGHT_GRAY);
			drawGrid(g2d);
		}

		int charMaxWidth = width / text.length;
		int xPos = 0;
		for (char ch : text)
		{
			g2d.setColor(Color.black);
			drawCharacter(g2d, ch, xPos, charMaxWidth);
			xPos += charMaxWidth;
		}

		g2d.dispose();
		return image;
	}

	private void clearCanvas(Graphics2D g2d)
	{
		g2d.clearRect(0, 0, width, height);
	}

	private void drawGrid(Graphics2D g2d)
	{
		Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
		g2d.setStroke(dashed);

		for (int y = 2; y < height; y += gridSize)
		{
			g2d.drawLine(0, y, width - 1, y);
		}

		for (int x = 2; x < width; x += gridSize)
		{
			g2d.drawLine(x, 0, x, height - 1);
		}
	}

	private void drawCharacter(Graphics2D g2d, char ch, int x, int boxWidth)
	{
		Random rnd = new Random();

		double degree = (rnd.nextDouble() * rotationAmplitude * 2) - rotationAmplitude;
		double scale = 1 - (rnd.nextDouble() * scaleAmplitude / 100);

		Graphics2D cg2d = (Graphics2D) g2d.create();

		Font font = getRandomFont(28);
		if (font != null)
		{
			cg2d.setFont(font);
		}

		cg2d.translate(x + (boxWidth / 2), height / 2);
		cg2d.rotate(degree * Math.PI / 90);
		cg2d.scale(scale, scale);

		FontMetrics fm = cg2d.getFontMetrics();
		int charWidth = fm.charWidth(ch);
		int charHeight = fm.getAscent() + fm.getDescent();

		cg2d.drawString(String.valueOf(ch), -(charWidth / 2), fm.getAscent() - (charHeight / 2));

		cg2d.dispose();
	}

	private Font getRandomFont(float size)
	{
		Random rnd = new Random();
		Integer i = (rnd.nextInt(CRAZY_FONTS.length));

		String fontPath = "/fonts/" + CRAZY_FONTS[i];
		try (InputStream is = Captcha.class.getResourceAsStream(fontPath))
		{
			Font font = Font.createFont(Font.TRUETYPE_FONT, is);
			return font.deriveFont(size);
		}
		catch (Exception e)
		{
			LOG.error("Failed load font {}", fontPath, e);
			return null;
		}
	}

}
