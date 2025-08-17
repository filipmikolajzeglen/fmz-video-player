package com.filipmikolajzeglen.fmzvideoplayer.video.effect;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javafx.scene.control.Labeled;
import javafx.scene.shape.SVGPath;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
public class VideoIconsEffect
{
   private static final String EMPTY_SVG_CONTENT = "";

   public static void setControlSVG(Labeled control, SVGPath svgPath, String resourcePath)
   {
      svgPath.setContent(loadSvgContent(resourcePath));
      control.setGraphic(svgPath);
   }

   public static String loadSvgContent(String resourcePath)
   {
      try (InputStream is = VideoIconsEffect.class.getResourceAsStream(resourcePath))
      {
         if (is == null)
         {
            log.warn("Cannot find SVG resource: {}", resourcePath);
            return EMPTY_SVG_CONTENT;
         }
         return extractPathDataFromStream(is);
      }
      catch (Exception e)
      {
         log.error("Error while loading or parsing SVG file: {}", resourcePath);
         log.error(e.getMessage());
         return EMPTY_SVG_CONTENT;
      }
   }

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

      log.warn("No <path> tag found in the SVG file.");
      return EMPTY_SVG_CONTENT;
   }
}