package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.filipmikolajzeglen.fmzvideoplayer.logger.Logger;
import javafx.scene.control.Labeled;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Klasa narzędziowa do ładowania danych ścieżek z plików SVG i przypisywania ich do kontrolek JavaFX. Używa
 * standardowego parsera XML do solidnej i bezpiecznej analizy plików SVG.
 */
public class VideoPlayerIcons
{

   private static final Logger LOGGER = new Logger();
   private static final String EMPTY_SVG_CONTENT = "";

   /**
    * Ustawia grafikę SVG dla dowolnej kontrolki typu Labeled (np. Button, Label).
    *
    * @param control      Kontrolka, której grafika ma zostać ustawiona.
    * @param svgPath      Obiekt SVGPath, który ma przechowywać ikonę.
    * @param resourcePath Ścieżka do pliku SVG w zasobach.
    */
   public static void setControlSVG(Labeled control, SVGPath svgPath, String resourcePath)
   {
      svgPath.setContent(loadSvgContent(resourcePath));
      control.setGraphic(svgPath);
   }

   /**
    * Ładuje zawartość atrybutu 'd' z pierwszej ścieżki (<path>) w pliku SVG.
    *
    * @param resourcePath Ścieżka do pliku SVG.
    * @return Zawartość atrybutu 'd' lub pusty string w przypadku błędu.
    */
   public static String loadSvgContent(String resourcePath)
   {
      try (InputStream is = VideoPlayerIcons.class.getResourceAsStream(resourcePath))
      {
         if (is == null)
         {
            LOGGER.warning("Nie można odnaleźć zasobu SVG: " + resourcePath);
            return EMPTY_SVG_CONTENT;
         }
         return extractPathDataFromStream(is);
      }
      catch (Exception e)
      {
         LOGGER.error("Błąd podczas ładowania lub parsowania pliku SVG: " + resourcePath);
         LOGGER.error(e.getMessage());
         return EMPTY_SVG_CONTENT;
      }
   }

   /**
    * Parsuje strumień danych SVG za pomocą parsera XML i wyodrębnia dane ścieżki.
    *
    * @param svgStream Strumień wejściowy z danymi pliku SVG.
    * @return Dane ścieżki z atrybutu 'd'.
    * @throws Exception w przypadku błędów parsowania.
    */
   private static String extractPathDataFromStream(InputStream svgStream) throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(svgStream);
      doc.getDocumentElement().normalize();

      NodeList pathNodes = doc.getElementsByTagName("path");
      if (pathNodes.getLength() > 0)
      {
         Element pathElement = (Element) pathNodes.item(0);
         return pathElement.getAttribute("d");
      }

      LOGGER.warning("Nie znaleziono tagu <path> w pliku SVG.");
      return EMPTY_SVG_CONTENT;
   }
}