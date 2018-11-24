package pse;
import com.algorithmia.Algorithmia;
import com.algorithmia.AlgorithmiaClient;
import com.algorithmia.algo.AlgoResponse;
import com.algorithmia.algo.Algorithm;
import com.algorithmia.data.DataDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// import statements

/** Encapsulation of the OCR Mechanism
 *
 *  This class utilizes the Algorithmia API to perform OCR (optical character recognition) on filetypes that normally
 *  would not be supported through the original text document search.
 *
 *  The OCR technology in use is Tesseract and open source engine originally coded in C++ and adapted for use in java
 *
 *  PDF files are processed using PDFBox to convert them page-by-page into .bmp images for use in the Tesseract engine
 *  and ultimately processing by the main search engine
 *
 *  PDFBox and Algorithmia both found on Maven (ver. 2.0.1 and 1.0.10 respectively)
 *
 */
public class OCRManager {
    private AlgorithmiaClient client = Algorithmia.client("simPO1tCJsDuDuhZsDpMpAT0CdA1");

    /**
     * driving function of class
     * @param filename
     */
    public void manage(String filename){
    upload(filename);
    String input = "data://salali/test/" + filename;
    Algorithm algo = client.algo("ocr/RecognizeCharacters/0.3.0");
    try{
    AlgoResponse result = algo.pipe(input);
        //System.out.println(result);
         try{ // System.out.println(result.asString());
             write2File(result.asString(), filename);
             System.out.println("success");
             return;
         }catch (com.algorithmia.AlgorithmException re) {
        System.out.println(re.toString());
    }
        } catch (com.algorithmia.APIException e){
        System.out.println("api exception");
    }

        System.out.println( "failure" );
    }

    /**
     * utrilization of method overloading to manage PDF input
     * @param PDFList
     * @param filename
     */
    public void manage(List<String> PDFList, String filename){
        StringBuilder finalwrite = new StringBuilder();
        for(String s : PDFList){
            upload(s);
            Algorithm algo = client.algo("ocr/RecognizeCharacters/0.3.0");
            try{
                AlgoResponse result = algo.pipe("data://salali/test/" + s);
                //System.out.println(result);
                try{ // System.out.println(result.asString());
                    finalwrite.append(result.asString());
              
                }catch (com.algorithmia.AlgorithmException re) {
                    System.out.println(re.toString());
                }
            } catch (com.algorithmia.APIException e){
                System.out.println("api exception");
            }


        }
        write2File(finalwrite.toString(), filename);
    }

    /**
     * manages exporting document into Algorithmia database for ease of access
     * @param filename
     */
    private void upload(String filename){
        DataDirectory dir = client.dir("data://salali/test");
        try {
            String local_file =  filename;
            try{
            dir.putFile(new File(local_file));
            } catch (com.algorithmia.APIException re){
                System.out.println("api excception");
            }
        } catch (FileNotFoundException  e) {
            e.printStackTrace();
        }
    }

    /**
     * as implied, writes OCR text to text document to be sent back into main pse implementation
     * @param managed
     * @param filename
     */
    private void write2File(String managed, String filename) {
        int s = filename.length();
        if (s < 4) {
            System.out.println("failure ");
            return;
        }

        filename = filename.substring(0, s - 4) + ".txt";


        try {
            try {
                PrintWriter writer = new PrintWriter(filename, "UTF-8");
                writer.println(managed);
                writer.close();
            } catch (java.io.FileNotFoundException x) {
                x.printStackTrace();
            }
        } catch (java.io.UnsupportedEncodingException u) {
            u.printStackTrace();
        }
    }

    /**
     * manages incoming PDF file for use in the main function, as labelled, converts pdf to friendly file format of
     * choice and returns file paths
     *
     * @param filename
     * @return
     */
    public List<String> convertPDFtoImages(String filename){
        ArrayList<String> filePaths = new ArrayList<String>();
        try {
            PDDocument doc = PDDocument.load(new File(filename));
            //System.out.println(doc.getNumberOfPages());
            //System.out.println("we got here");
//            List<BufferedImage> images = getImagesFromPDF(doc);
//            //System.out.println("issues?");
//            int count = 0;
//            // System.out.println(images.size());
//            for(BufferedImage image : images){
//
//              //  System.out.println("we got here");
//                String outputfile = filename.substring(0, filename.length() -4) + count + ".jpg";
//                System.out.println("here?");
//              //  ImageIO.write(image, "jpg", new File(outputfile));
//                filePaths.add(filename + count + ".jpg");
//              //  System.out.println(filePaths.size());
//                count++; deprecated code
            PDFRenderer pdfRenderer = new PDFRenderer(doc);
            for (int page = 0; page < doc.getNumberOfPages(); ++page)
            {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // suffix in filename will be used as the file format
                ImageIOUtil.writeImage(bim, filename.substring(0, filename.length() - 4)
                        + "-" + (page+1) + ".png", 300);
                filePaths.add(filename.substring(0, filename.length() -4) + "-" + (page+1) + ".png");
            }
            doc.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

            return filePaths;
    }

//    /**
//     * recursively traces  PDF for all relevant images to write to document, order and sorting are irrelevant for
//     * purposes of this search engine and therefore ignored
//     *
//     * @param document
//     * @return
//     * @throws IOException
//     */
//    private List<BufferedImage> getImagesFromPDF(PDDocument document) throws IOException {
//       // System.out.println("works");
//        List<BufferedImage> images = new ArrayList<BufferedImage>();
//        for (PDPage page : document.getPages()) {
//            images.addAll(getImagesFromResources(page.getResources()));
//        }
//        //System.out.println(images.size());
//        return images;
//    }
//
//    private List<BufferedImage> getImagesFromResources(PDResources resources) throws IOException {
//        List<BufferedImage> images = new ArrayList<BufferedImage>();
//
//        for (COSName xObjectName : resources.getXObjectNames()) {
//            PDXObject xObject = resources.getXObject(xObjectName);
//
//            if (xObject instanceof PDFormXObject) {
//                images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
//            } else if (xObject instanceof PDImageXObject) {
//                images.add(((PDImageXObject) xObject).getImage());
//            }
//        }
//        //System.out.println(images.size());
//
//        return images;
//    } deprecated code not in use

}
