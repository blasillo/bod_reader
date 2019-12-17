package es.mindef.bod.reader;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.graphics.text.LineText;
import org.icepdf.core.pobjects.graphics.text.PageText;
import org.icepdf.core.pobjects.graphics.text.WordText;
import org.icepdf.core.search.DocumentSearchController;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.icepdf.ri.common.utility.search.SearchPanel;

import es.mindef.bod.scraping.*;

@SpringBootApplication
public class ReaderApplication extends JFrame {

	JList listaDocumentos = new JList();

	final SwingController controller = new SwingController();

	//static String filename = "C:\\Proyectos\\bod_reader\\pdfs\\bod_20191204_237-al.pdf";

	ReaderApplication() {
		iniciarIU();
	}

	public void anadirDocumentos(List<String> lista) {
		DefaultListModel model = new DefaultListModel();

		for (String elemento : lista) {
			model.addElement(elemento);
		}
		listaDocumentos.setModel(model);

	}

	private void iniciarIU() {

		setTitle("Buscador BOD");
		setSize(1024, 1024);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		setJMenuBar(createMenuBar());

		ListSelectionListener listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent listSelectionEvent) {

				System.out.print("Primero: " + listSelectionEvent.getFirstIndex());
				System.out.print(", Ultimo: " + listSelectionEvent.getLastIndex());

				boolean adjust = listSelectionEvent.getValueIsAdjusting();

				//System.out.println(", Adjusting? " + adjust);

				if (!adjust) {
					JList list = (JList) listSelectionEvent.getSource();
					int selections[] = list.getSelectedIndices();
					List<String> selectionValues = list.getSelectedValuesList();

					for (int i = 0, n = selections.length; i < n; i++) {
						if (i == 0) {
							System.out.print("  Selections: ");

							controller.openDocument(selectionValues.get(i));

							controller.setUtilityPaneVisible(true);
							controller.showSearchPanel();

							DocumentSearchController searchController = controller.getDocumentSearchController();
							searchController.clearAllSearchHighlight();
							String[] terms = { "Reservista", "Tribunal", "Zertifikat" };

							for (String term : terms) {
								searchController.addSearchTerm(term, false, false);
							}

							Document document = controller.getDocument();
							SearchPanel spanel = new SearchPanel(controller);
							spanel.setDocument( document );
							controller.setSearchPanel (spanel);
							

							ArrayList<WordText> foundWords;

							for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {

								
								foundWords = searchController.searchPage(pageIndex);
							
								System.out.println("Página : " + pageIndex);

								if (foundWords != null) {
									for (WordText wordText : foundWords) {

										System.out.println("    Encontrado : " + wordText.toString());
										searchController.searchHighlightPage( pageIndex );
										List<LineText> lineItems =  searchController.searchHighlightPage(pageIndex,6);
										//
										spanel.addFoundEntry(wordText.toString(), pageIndex, lineItems, true);
									}
                                    
									controller.getDocumentViewController().getViewContainer().repaint();
                                    
								}
							}

						  
						}
						//System.out.print(selections[i] + "/" + selectionValues.get(i) + " ");
					}
					System.out.println();
				}
			}
		};

		listaDocumentos.addListSelectionListener( listSelectionListener );
	
		
		// add components
		add(listaDocumentos);

		// set component bounds (only needed by Absolute Positioning)
		listaDocumentos.setBounds(0, 0, 325, 1025);


		

		controller.setIsEmbeddedComponent(true);

		final SwingViewBuilder factory = new SwingViewBuilder(controller);
		final JPanel viewerComponentPanel = factory.buildViewerPanel();

		//viewerComponentPanel.setPreferredSize(new Dimension(1024, 1024));
		//viewerComponentPanel.setMaximumSize(new Dimension(1024, 1024));
		viewerComponentPanel.setMinimumSize (new Dimension(1024, 1024));
		viewerComponentPanel.setBounds(0, 0, 1024, 1024);
		viewerComponentPanel.setOpaque(true);

		ComponentKeyBinding.install(controller, viewerComponentPanel);
		

		final JPanel applicationPanel = new JPanel();

		


		applicationPanel.add(viewerComponentPanel);
		applicationPanel.invalidate();

		controller.getDocumentViewController().setAnnotationCallback(
				new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));

		//controller.openDocument(filename);

		


		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
						   listaDocumentos, applicationPanel);
						   
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerLocation(350);

		createLayout(splitPane);
		

	}

	private void createLayout(final JComponent... arg) {

		final Container pane = getContentPane();
		final GroupLayout gl = new GroupLayout(pane);

		pane.setLayout(gl);

		gl.setAutoCreateContainerGaps(true);

		gl.setHorizontalGroup(gl.createSequentialGroup().addComponent(arg[0]));

		gl.setVerticalGroup(gl.createSequentialGroup().addComponent(arg[0]));
	}

	public static void main(final String[] args) {
		// SpringApplication.run(ReaderApplication.class, args);
		final ConfigurableApplicationContext ctx = new SpringApplicationBuilder(ReaderApplication.class).headless(false)
				.run(args);

		final ReaderApplication app = ((BeanFactory) ctx).getBean(ReaderApplication.class);

		EventQueue.invokeLater(() -> {

			app.setVisible(true);
		});

		try {
			// PdfParse parser = new PdfParse();
			// parser.parse();
			System.out.println("Indexando documentos ...");
			final Indexador indexador = new Indexador();

			List<String> lista = indexador.getDocumentos();

			app.anadirDocumentos(lista);

			// parse.parse(filename);
			//		System.out.println("Buscando en documentos ...");
			// parse.indexDocument ( new File (filename) );
			// System.out.println ("Buscando ..." );
			//final Buscador buscador = new Buscador(Utiles.quitarAcentos("STANAG"));
			//final Buscador buscador = new Buscador(
            //    Arrays.asList(new String[]{  Utiles.quitarAcentos("Valcarce García")} )
			//);

			// descargar

			//System.out.println ( "Descargando documentos ... ");

			//WebScraping.descargaBOD ("2019","12","11","241");
			
            
			// parse.parse(filename);

		} catch (final Exception e) { 
			e.printStackTrace();
		}

		
	}


	protected JMenuBar  createMenuBar (){
		final JMenuBar menuBar = new JMenuBar();
		final JMenu menu = new JMenu("Archivo");

		menu.add(new JMenuItem("Preferencias ..."));
		menu.add(new JSeparator());

		final JMenuItem salirMenu = new JMenuItem("Salir");
		salirMenu.addActionListener((final ActionEvent event) -> {
			System.exit(0);
		});

		menu.add(salirMenu);

		menuBar.add(menu);

		return menuBar;
	}



	


}
