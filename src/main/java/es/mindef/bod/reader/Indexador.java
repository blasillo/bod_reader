package es.mindef.bod.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.JOptionPane;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class Indexador {

    final private Tika tika = new Tika();

    private IndexWriter writer = null;

    protected String contenido;

    protected String metadatos;

    private int indexados = 0;

    private List<String> documentos = new ArrayList<String>();

    public Indexador()  {
        try {
            Path diretorio = FileSystems.getDefault().getPath(Configuracion._INDICE_);

            borrarIndices(diretorio.toFile());

            Directory d = new SimpleFSDirectory(diretorio);

            Analyzer analyzer = new StandardAnalyzer();

            final IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            this.writer = new IndexWriter(d, indexWriterConfig);

            long inicio = System.currentTimeMillis();
            
            indexarArchivosDiretorio ( new File (Configuracion._DOCUMENTOS_) );

            writer.commit();
            long fin = System.currentTimeMillis();

            JOptionPane.showMessageDialog(
                        null,
                        "Archivos indexados: " + indexados + "\n"
                                + "Tiempo de indexación: "
                                + String.valueOf((fin - inicio) / 1000) + "s"
                );
        }
        catch ( Exception e) {

        }
        finally {
          if ( writer != null) 
            try { writer.close(); } 
            catch (IOException e) {
                e.printStackTrace();
            }
    }

    }

    private void borrarIndices(File diretorio) {
        if (diretorio.exists()) {
            File archivos[] = diretorio.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    archivo.delete();
                }
            }
        }
    }

    void indexarArchivosDiretorio(File raiz) {
        FilenameFilter filtro = new FilenameFilter() {
            public boolean accept(File arquivo, String nome) {
                return nome.toLowerCase().endsWith(".pdf") || nome.toLowerCase().endsWith(".odt")
                        || nome.toLowerCase().endsWith(".doc") || nome.toLowerCase().endsWith(".docx")
                        || nome.toLowerCase().endsWith(".ppt") || nome.toLowerCase().endsWith(".pptx")
                        || nome.toLowerCase().endsWith(".xls") || nome.toLowerCase().endsWith(".xlsx")
                        || nome.toLowerCase().endsWith(".txt") || nome.toLowerCase().endsWith(".rtf")
                        || nome.toLowerCase().endsWith("");
            }
        };

        for (File archivo : raiz.listFiles(filtro)) {
            if (archivo.isFile()) {
                try {
                    String textoExtraido = Utiles.quitarAcentos( this.tika.parseToString(archivo) );
                    //System.out.println (  textoExtraido );
                    documentos.add( archivo.getAbsolutePath());

                    indexar(archivo, textoExtraido);
                    indexados++;
                } catch (Exception e) {
                    System.out.print(e);
                }
            } else {
                indexarArchivosDiretorio(archivo);
            }
        }

    }

    public List<String> getDocumentos (){ 
        return documentos;
    } 




    private void indexar(File arquivo, String textoExtraido, Metadata metadatos) {

    }

    
     
    

    private void indexar(File archivo, String textoExtraido) {
        SimpleDateFormat formateador = new SimpleDateFormat("ddMMyyyy");
        String ultimaModif= formateador.format(archivo.lastModified());

        System.out.println("Indexando documento: " + archivo.getName() );

        Document documento = new Document();

        documento.add(new TextField("UltimaModificacion", ultimaModif, Store.YES));
        documento.add(new TextField("Ruta", archivo.getAbsolutePath(), Store.YES));
        documento.add(new TextField("Nombre", archivo.getName(),Store.YES ));
        documento.add(new TextField("Texto", textoExtraido, Store.YES));

        try {
            getWriter().addDocument(documento);
        }
        catch (IOException e) {
            //logger.error(e);
            System.out.println (e);
        }
    }

    IndexWriter getWriter() {
        return writer;
    }

    



    public void indexarDocumento(final File file) throws Exception {
        final Document doc = new Document();
        //doc.add(new Field( "filename", file.getName(), Store.YES, Index.ANALYZED));
        doc.add ( new TextField( "archivo", file.getName(), Store.YES  ) );
        doc.add ( new TextField ("texto", tika.parseToString(file), Store.NO ));

        writer.addDocument (doc);
    }

    void parse(final String filename) {

        PDDocument pd;

        try {
            // Input PDF file
            final File input = new File(filename);

            final StringBuilder sb = new StringBuilder();
            pd = PDDocument.load(input);

            final PDDocumentInformation info = pd.getDocumentInformation();

            System.out.println("Title " + info.getTitle());
            System.out.println("Author " + info.getAuthor());
            System.out.println("Subject " + info.getSubject());
            System.out.println("Keywords " + info.getKeywords());
            System.out.println("Creator " + info.getCreator());
            System.out.println("Producer " + info.getProducer());
            System.out.println("Created date " + info.getCreationDate());
            System.out.println("Modified date " + info.getModificationDate());

            final PDFTextStripper stripper = new PDFTextStripper();

            sb.append(stripper.getText(pd));

            // System.out.println (sb.toString() );

            this.contenido = sb.toString();

            if (pd != null) {
                pd.close();
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}   