package es.mindef.bod.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.text.Normalizer;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;


//import org.apache.lucene.queryParser.ParseException;
//import org.pdfbox.pdmodel.PDDocument;
//import org.pdfbox.util.PDFTextStripper;

public class PdfParse  {

    private static final String INDEX_DIR = "src/main/resources/index";
    private static final int DEFAULT_RESULT_SIZE = 100;

    /*

    void search () {

       File file = new File("C:\\Proyectos\\bod_reader\\bod_20191204_237-al.pdf");

       IndexItem pdfIndexItem = index(file);

       // creating an instance of the indexer class and indexing the items
       Indexer indexer = new Indexer(INDEX_DIR);
       indexer.index(pdfIndexItem);
       indexer.close();

        // creating an instance of the Searcher class to the query the index
        Searcher searcher = new Searcher(INDEX_DIR);
        int result = searcher.findByContent("Hello", DEFAULT_RESULT_SIZE);
        print(result);
        searcher.close();


    }

   

    public static IndexItem index(File file) throws IOException {
        PDDocument doc = PDDocument.load(file);
        String content = new PDFTextStripper().getText(doc);
        doc.close();
        return new IndexItem((long)file.getName().hashCode(), file.getName(), content);
    }
     */

    void parse () throws Exception {
   

      String filename = "C:\\Proyectos\\bod_reader\\bod_20191204_237-al.pdf";
      BodyContentHandler handler = new BodyContentHandler(-1);
      Metadata metadata = new Metadata();
      FileInputStream inputstream = new FileInputStream(new File(filename));


      System.out.println ("Detectado:" + detect (new File(filename) ));

      TikaConfig tikaConfig = TikaConfig.getDefaultConfig();

      ParseContext pcontext = new ParseContext();

      PDFParser pdfparser = new PDFParser(); 
      pdfparser.parse(inputstream, handler, metadata,pcontext);

      //String text2 = parseUsingComponents(filename, tikaConfig, metadata);

      String text = Normalizer.normalize(handler.toString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");



      System.out.println("Contenidos del PDF :\n" + text);

      System.out.println("Metadatos del PDF:");
      String[] metadataNames = metadata.names();
      
      for(String name : metadataNames) {
         System.out.println(name + " : " + metadata.get(name));
      }

    }
    public static String detect(File file) throws Exception {

        //
        if (!file.exists()) {
            throw new Exception("exception ! " + file.getAbsoluteFile() + " not existes.");
        }
        Tika t = new Tika();
        return t.detect(file);
    }
     



    public static String parseUsingComponents(String filename, TikaConfig tikaConfig,
                                              Metadata metadata) throws Exception {
        MimeTypes mimeRegistry = tikaConfig.getMimeRepository();

        System.out.println("Examinando: [" + filename + "]");

        //metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
        //System.out.println("Detectado tipo MIME : [" + mimeRegistry.detect(null, metadata) + "]");

        
        FileInputStream stream = new FileInputStream(new File(filename));
        //System.out.println("Detectado tipo MIME : [" + mimeRegistry.detect(stream, metadata) + "]");

        //LanguageDetector langDetector = LanguageDetector.getDefaultLanguageDetector(); // new
                                                                                       // OptimizeLangDetector().loadModels();
        //LanguageResult lang = langDetector.detect(FileUtils.readFileToString(new File(filename), UTF_8));

        //Parser parser = tikaConfig.getParser();
        //MediaType type = Detector.detect(stream, metadata);
        //metadata.set(Metadata.CONTENT_TYPE, type.toString());
        ContentHandler handler = new BodyContentHandler();
        PDFParser parser = new PDFParser(); 
    
        parser.parse(stream, handler, metadata, new ParseContext());

        return handler.toString(); 
    }



}