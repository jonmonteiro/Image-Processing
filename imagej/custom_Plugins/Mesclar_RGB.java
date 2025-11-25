import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog; 
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Mesclar_RGB implements PlugIn {

    @Override
    public void run(String arg) {

        if (WindowManager.getImageCount() < 3) {
            IJ.error("Erro", "É necessário ter pelo menos 3 imagens abertas.");
            return;
        }

        String[] titulos = WindowManager.getImageTitles();

        // caixa de diálogo
        GenericDialog gd = new GenericDialog("Mesclar Canais RGB");

        gd.addChoice("Canal Vermelho (R):", titulos, titulos[0]);
        gd.addChoice("Canal Verde (G):", titulos, titulos[1]);
        gd.addChoice("Canal Azul (B):", titulos, titulos[2]);
        gd.showDialog();

        if (gd.wasCanceled()) return;

        // imagens baseadas nas escolhas
        ImagePlus imgR = WindowManager.getImage(gd.getNextChoice());
        ImagePlus imgG = WindowManager.getImage(gd.getNextChoice());
        ImagePlus imgB = WindowManager.getImage(gd.getNextChoice());

        if (validarImagens(imgR, imgG, imgB)) {
            construirImagemRGB(imgR, imgG, imgB);
        }
    }

    public boolean validarImagens(ImagePlus r, ImagePlus g, ImagePlus b) {
        
        if (r.getType() != ImagePlus.GRAY8 || g.getType() != ImagePlus.GRAY8 || b.getType() != ImagePlus.GRAY8) {
            IJ.error("Erro", "Todas as imagens selecionadas devem ser 8-bits (escala de cinza).");
            return false;
        }

        if (r.getWidth() != g.getWidth() || r.getWidth() != b.getWidth() ||
            r.getHeight() != g.getHeight() || r.getHeight() != b.getHeight()) {
            IJ.error("Erro", "As imagens devem ter as mesmas dimensões.");
            return false;
        }
        return true;
    }

    public void construirImagemRGB(ImagePlus r, ImagePlus g, ImagePlus b) {
        int largura = r.getWidth();
        int altura = r.getHeight();

        // processors para ler os pixels das imagens de entrada
        ImageProcessor ipR = r.getProcessor();
        ImageProcessor ipG = g.getProcessor();
        ImageProcessor ipB = b.getProcessor();

        // cria nova imagem configurada para ser RGB
        ImagePlus imgFinal = IJ.createImage("Resultado RGB", "RGB", largura, altura, 1);
        ImageProcessor ipFinal = imgFinal.getProcessor();

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                // lê a intensidade de cada pixel naquela posição da matriz (de 0-255)
                int valR = ipR.get(x, y);
                int valG = ipG.get(x, y);
                int valB = ipB.get(x, y);

                // empacota os 3 valores em um único inteiro
                // R vai para a esquerda (bits 16-23), G para o meio (bits 8-15), B fica na direita (bits 0-7)
                int pixelRGB = (valR << 16) | (valG << 8) | valB;

                ipFinal.set(x, y, pixelRGB);
            }
        }

        imgFinal.show();
    }
}