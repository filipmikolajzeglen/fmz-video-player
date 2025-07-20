package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.io.InputStream;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;

class VideoPlayerIcons
{
   private static final String SVG_PATH_START_TAG = "<path";
   private static final String SVG_PATH_D_ATTRIBUTE = "d=\"";
   private static final String SVG_PATH_END_QUOTE = "\"";
   private static final String EMPTY_SVG_CONTENT = "";

   static void setButtonSVG(Button button, SVGPath svgPath, String resourcePath)
   {
      svgPath.setContent(loadSvgContent(resourcePath));
      button.setGraphic(svgPath);
   }

   static void setLabelSVG(Label label, SVGPath svgPath, String resourcePath)
   {
      svgPath.setContent(loadSvgContent(resourcePath));
      label.setGraphic(svgPath);
   }

   static void setLabelVolumeSVG(Label label, SVGPath svgPath, String resourcePath, double translateX)
   {
      svgPath.setContent(loadSvgContent(resourcePath));
      svgPath.setTranslateX(translateX);
      label.setGraphic(svgPath);
   }

   private static String loadSvgContent(String resourcePath)
   {
      try (InputStream is = VideoPlayerIcons.class.getResourceAsStream(resourcePath))
      {
         if (is == null)
         {
            return EMPTY_SVG_CONTENT;
         }
         String svg = new String(is.readAllBytes());
         return extractPathD(svg);
      }
      catch (Exception e)
      {
         return EMPTY_SVG_CONTENT;
      }
   }

   private static String extractPathD(String svg)
   {
      int pathIndex = svg.indexOf(SVG_PATH_START_TAG);
      int dIndex = svg.indexOf(SVG_PATH_D_ATTRIBUTE, pathIndex);
      int dEnd = svg.indexOf(SVG_PATH_END_QUOTE, dIndex + 3);
      if (pathIndex < 0 || dIndex < 0 || dEnd <= dIndex)
      {
         return EMPTY_SVG_CONTENT;
      }
      return svg.substring(dIndex + 3, dEnd);
   }
}