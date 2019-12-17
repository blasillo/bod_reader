package es.mindef.bod.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.QueryBuilder;

class Buscador {

    // private final static Logger logger = Logger.getLogger (Buscador.class);

    public Buscador(final String parametro) throws IOException {

        System.out.println("Buscando por: " + parametro);

        final Directory diretorio = new SimpleFSDirectory(FileSystems.getDefault().getPath(Configuracion._INDICE_));

        final IndexReader lector = DirectoryReader.open(diretorio);

        final IndexSearcher buscador = new IndexSearcher(lector);

        final Analyzer analizador = new StandardAnalyzer();

        final QueryBuilder builder = new QueryBuilder(analizador);
        final Query consulta = builder.createBooleanQuery("Texto", parametro);

        final long inicio = System.currentTimeMillis();
        final TopDocs resultado = buscador.search(consulta, 100);
        final long fin = System.currentTimeMillis();

        System.out.println("Tiempo de búsqueda: " + String.valueOf((fin - inicio) ) + " milisegundos");

        for (final ScoreDoc sd : resultado.scoreDocs) {
            final Document documento = buscador.doc(sd.doc);
            System.out.println("Documento:" + documento.get("Nombre"));
        }

        lector.close();

    }

    public Buscador(final List<String> parametros) throws IOException {

        System.out.println("Buscando ... ");

        final Directory diretorio = new SimpleFSDirectory(FileSystems.getDefault().getPath(Configuracion._INDICE_));

        final IndexReader lector = DirectoryReader.open(diretorio);

        final IndexSearcher buscador = new IndexSearcher(lector);

        final Analyzer analizador = new StandardAnalyzer();

        final Builder builder = new BooleanQuery.Builder();

        final long inicio = System.currentTimeMillis();
        


        for ( final String param: parametros) {
            builder.add(  new TermQuery(new Term("Texto", param )), Occur.SHOULD );
        }
      
     
        final Query consulta = builder.build();
        

        
        final TopDocs resultado = buscador.search(consulta, 100);


        final long fin = System.currentTimeMillis();
        
        System.out.println ( "Tiempo de búsqueda: " + String.valueOf((fin - inicio) / 1000) + "s");

        System.out.println ( "Encontrados:" + resultado.totalHits);

        for (final ScoreDoc sd : resultado.scoreDocs) {
            final Document documento = buscador.doc(sd.doc);
            System.out.println ( "Documento:" + documento.get("Nombre"));
        } 

        lector.close();
                    
       
    }
    
}