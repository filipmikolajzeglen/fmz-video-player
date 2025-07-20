package com.filipmikolajzeglen.fmzvideoplayer.video;

import java.util.Locale;
import java.util.function.Supplier;

import com.filipmikolajzeglen.fmzvideoplayer.FMZVideoPlayerConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Slider;

class SliderStyler
{
   static void addColorToSlider(Slider slider, Supplier<Double> valueSupplier)
   {
      Node node = slider.lookup(".track");
      if (node != null)
      {
         double percentage = calculatePercentage(slider, valueSupplier);
         String style = generateStyle(percentage);
         node.setStyle(style);
      }
   }

   static void addDynamicColorListener(Slider slider)
   {
      slider.skinProperty().addListener((obs, oldSkin, newSkin) -> {
         if (newSkin != null)
         {
            addColorToSlider(slider, slider::getValue);
         }
      });

      slider.valueProperty().addListener((obs, oldValue, newValue) -> addColorToSlider(slider, slider::getValue));
   }

   private static double calculatePercentage(Slider slider, Supplier<Double> valueSupplier)
   {
      return (valueSupplier.get() / slider.getMax()) * 100;
   }

   private static String generateStyle(double percentage)
   {
      return String.format(Locale.US, "-fx-background-color: linear-gradient(to right, %s %f%% , %s %f%%);",
            FMZVideoPlayerConfiguration.UI.PRIMARY_COLOR, percentage, FMZVideoPlayerConfiguration.UI.GRADIENT_COLOR,
            percentage);
   }
}