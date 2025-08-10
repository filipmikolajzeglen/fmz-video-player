package com.filipmikolajzeglen.fmzvideoplayer.demo;

import java.awt.image.BufferedImage;
import java.io.IOException;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Rational;
import io.humble.video.awt.ImageFrame;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

/**
 * Wersja finalna z poprawną synchronizacją.
 * Ignoruje błędne znaczniki czasu (PTS) z pliku i opiera synchronizację
 * na deklarowanej liczbie klatek na sekundę (frame rate), co zapewnia
 * płynne i poprawne odtwarzanie.
 */
public class DecodeAndPlayVideo {

   private static void playVideo(String filename) throws InterruptedException, IOException {
      Demuxer demuxer = Demuxer.make();
      demuxer.open(filename, null, false, true, null, null);

      int numStreams = demuxer.getNumStreams();
      int videoStreamId = -1;
      Decoder videoDecoder = null;
      Rational frameRate = null;

      for (int i = 0; i < numStreams; i++) {
         final DemuxerStream stream = demuxer.getStream(i);
         if (stream.getDecoder() != null && stream.getDecoder().getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
            videoStreamId = i;
            videoDecoder = stream.getDecoder();
            frameRate = stream.getFrameRate(); // Odczytujemy pewniejszą informację - liczbę klatek na sekundę
            break;
         }
      }
      if (videoStreamId == -1)
         throw new RuntimeException("Nie można znaleźć strumienia wideo w kontenerze: " + filename);

      videoDecoder.open(null, null);

      // Obliczamy czas trwania jednej klatki w nanosekundach
      final long frameDurationNanos = (long) (1_000_000_000.0 / frameRate.getDouble());
      long frameCount = 0; // Licznik wyświetlonych klatek

      final MediaPicture picture = MediaPicture.make(
            videoDecoder.getWidth(),
            videoDecoder.getHeight(),
            videoDecoder.getPixelFormat());

      final MediaPictureConverter converter =
            MediaPictureConverterFactory.createConverter(
                  MediaPictureConverterFactory.HUMBLE_BGR_24,
                  picture);
      BufferedImage image = null;

      final ImageFrame window = ImageFrame.make();
      long systemStartTime = System.nanoTime();

      final MediaPacket packet = MediaPacket.make();
      while (demuxer.read(packet) >= 0) {
         if (packet.getStreamIndex() == videoStreamId) {
            int offset = 0;
            int bytesRead = 0;
            do {
               bytesRead += videoDecoder.decode(picture, packet, offset);
               if (picture.isComplete()) {
                  image = displayVideoAtCorrectTime(picture, converter, image, window,
                        systemStartTime, frameCount, frameDurationNanos);
                  frameCount++; // Inkrementujemy licznik klatek
               }
               offset += bytesRead;
            } while (offset < packet.getSize());
         }
      }

      do {
         videoDecoder.decode(picture, null, 0);
         if (picture.isComplete()) {
            image = displayVideoAtCorrectTime(picture, converter, image, window,
                  systemStartTime, frameCount, frameDurationNanos);
            frameCount++;
         }
      } while (picture.isComplete());

      demuxer.close();
      window.dispose();
   }

   /**
    * Oblicza wymagane opóźnienie na podstawie licznika klatek i stałego czasu ich trwania.
    */
   private static BufferedImage displayVideoAtCorrectTime(final MediaPicture picture,
         final MediaPictureConverter converter, BufferedImage image,
         final ImageFrame window, long systemStartTime,
         long frameCount, long frameDurationNanos)
         throws InterruptedException {

      // Krok 1: Oblicz docelowy czas wyświetlenia klatki na podstawie jej numeru.
      long targetTimestampNanos = frameCount * frameDurationNanos;

      // Krok 2: Oblicz, ile czasu upłynęło na zegarze systemowym.
      long elapsedPlayerTimeNanos = System.nanoTime() - systemStartTime;

      // Krok 3: Oblicz opóźnienie.
      long delayNanos = targetTimestampNanos - elapsedPlayerTimeNanos;

      // Krok 4: Wstrzymaj wątek, jeśli to konieczne.
      if (delayNanos > 0) {
         Thread.sleep(delayNanos / 1000000, (int) (delayNanos % 1000000));
      }

      // Krok 5: Wyświetl obraz.
      image = converter.toImage(image, picture);
      window.setImage(image);
      return image;
   }

   public static void main(String[] args) throws InterruptedException, IOException {
      String videoPath = "F:\\FoxKids\\Batman Przyszłości\\S01E04-Golem.mkv";
      try {
         System.out.println("Rozpoczynanie odtwarzania wideo z pliku: " + videoPath);
         playVideo(videoPath);
         System.out.println("Odtwarzanie zakończone.");
      } catch (Exception e) {
         System.err.println("Wystąpił krytyczny błąd:");
         e.printStackTrace();
      }
   }
}