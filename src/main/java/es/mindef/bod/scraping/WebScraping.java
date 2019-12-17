package es.mindef.bod.scraping;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebScraping {


    public static String _BOD_URL_BASE_ = "http://intra.mdef.es";

    public static String _BOD_CONSULTA_ = "/intradef/contenido_INET_Active/bod/";
    
    //private String anno;
    //private String mes;
    //private String dia;
    //private String numero;
    
    



    public static void descargaBOD (String anno, String mes, String dia, String numero) {
        WebClient client = new WebClient();  

        client.getOptions().setCssEnabled(false);  
        client.getOptions().setJavaScriptEnabled(false);  

        try {  
            String searchUrl = _BOD_URL_BASE_ + _BOD_CONSULTA_ + anno + "/" + mes + "/" +
                            "BOD_" + anno + mes + dia + "_" + numero + ".pdf";

            System.out.println ("Intentando descargar : " + searchUrl );

            Page page = client.getPage(searchUrl);

            System.out.println ("Tipo:" + page.getWebResponse().getContentType() );

            
        }
        catch ( FailingHttpStatusCodeException fhscs ) {

            System.out.print("NO ENCONTRADO");
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }
}
