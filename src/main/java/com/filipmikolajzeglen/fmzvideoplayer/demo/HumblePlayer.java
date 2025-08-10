package com.filipmikolajzeglen.fmzvideoplayer.demo;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import io.humble.video.AudioFormat;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaAudio;
import io.humble.video.MediaAudioResampler;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Rational;
import io.humble.video.awt.ImageFrame;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class HumblePlayer {

   public static void play(String filename) throws IOException, InterruptedException, LineUnavailableException {
      Demuxer demuxer = Demuxer.make();
      demuxer.open(filename, null, false, true, null, null);

      Decoder videoDecoder = null;
      int videoStreamId = -1;
      Decoder audioDecoder = null;
      int audioStreamId = -1;

      for (int i = 0; i < demuxer.getNumStreams(); i++) {
         DemuxerStream stream = demuxer.getStream(i);
         Decoder decoder = stream.getDecoder();
         if (decoder != null) {
            if (decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO && videoStreamId == -1) {
               videoStreamId = i;
               videoDecoder = decoder;
            } else if (decoder.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO && audioStreamId == -1) {
               audioStreamId = i;
               audioDecoder = decoder;
            }
         }
      }

      if (videoStreamId == -1 && audioStreamId == -1) {
         demuxer.close();
         throw new RuntimeException("Could not find video or audio stream in container: " + filename);
      }

      if (videoDecoder != null) {
         videoDecoder.open(null, null);
      }
      if (audioDecoder != null) {
         audioDecoder.open(null, null);
      }

      ImageFrame window = null;
      MediaPicture picture = null;
      MediaPictureConverter converter = null;
      if (videoDecoder != null) {
         picture = MediaPicture.make(videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());
         converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
         window = ImageFrame.make();
         if (window == null) {
            throw new RuntimeException("Cannot create window for video playback.");
         }
      }

      SourceDataLine line = null;
      MediaAudioResampler resampler = null;
      if (audioDecoder != null) {
         // Ustawiamy docelowy format audio na 16-bitowy PCM - jest on powszechnie wspierany.
         final AudioFormat.Type targetFormat = AudioFormat.Type.SAMPLE_FMT_S16;
         javax.sound.sampled.AudioFormat javaAudioFormat = new javax.sound.sampled.AudioFormat(
               audioDecoder.getSampleRate(),
               16, // 16-bitowa głębia
               audioDecoder.getChannels(),
               true,  // signed
               false  // little-endian
         );
         DataLine.Info info = new DataLine.Info(SourceDataLine.class, javaAudioFormat);
         line = (SourceDataLine) AudioSystem.getLine(info);
         line.open(javaAudioFormat);
         line.start();

         // Jeśli format w pliku różni się od docelowego, tworzymy resampler.
         if (audioDecoder.getSampleFormat() != targetFormat) {
            resampler = MediaAudioResampler.make(
                  audioDecoder.getChannelLayout(), audioDecoder.getSampleRate(), targetFormat,
                  audioDecoder.getChannelLayout(), audioDecoder.getSampleRate(), audioDecoder.getSampleFormat());
            resampler.open();
         }
      }

      final MediaPacket packet = MediaPacket.make();
      long startTime = System.nanoTime();

      while (demuxer.read(packet) >= 0) {
         if (packet.getStreamIndex() == videoStreamId && videoDecoder != null) {
            MediaPicture localPicture = MediaPicture.make(picture.getWidth(), picture.getHeight(), picture.getFormat());
            int offset = 0;
            int bytesRead;
            do {
               bytesRead = videoDecoder.decode(localPicture, packet, offset);
               if (localPicture.isComplete()) {
                  displayVideo(localPicture, converter, window, startTime, videoDecoder.getTimeBase());
               }
               offset += bytesRead;
            } while (offset < packet.getSize());
         } else if (packet.getStreamIndex() == audioStreamId && audioDecoder != null) {
            MediaAudio samples = MediaAudio.make(1024, audioDecoder.getSampleRate(), audioDecoder.getChannels(), audioDecoder.getChannelLayout(), audioDecoder.getSampleFormat());
            int offset = 0;
            int bytesRead;
            do {
               bytesRead = audioDecoder.decode(samples, packet, offset);
               if (samples.isComplete()) {
                  // Jeśli resampler istnieje, używamy go do konwersji próbek.
                  if (resampler != null) {
                     MediaAudio resampled = MediaAudio.make(
                           samples.getNumSamples(), resampler.getOutputSampleRate(), resampler.getOutputChannels(),
                           resampler.getOutputLayout(), resampler.getOutputFormat());
                     resampler.resample(resampled, samples);
                     byte[] rawSamples = resampled.getData(0).getByteArray(0, resampled.getData(0).getSize());
                     line.write(rawSamples, 0, rawSamples.length);
                  } else {
                     // W przeciwnym wypadku odtwarzamy oryginalne próbki.
                     byte[] rawSamples = samples.getData(0).getByteArray(0, samples.getData(0).getSize());
                     line.write(rawSamples, 0, rawSamples.length);
                  }
               }
               offset += bytesRead;
            } while (offset < packet.getSize());
         }
      }

      if (line != null) {
         line.drain();
         line.stop();
         line.close();
      }
      if (window != null) {
         window.dispose();
      }
      if (resampler != null) {
         resampler.delete();
      }
      demuxer.close();
   }

   private static void displayVideo(MediaPicture picture, MediaPictureConverter converter, ImageFrame window, long systemStartTime, Rational streamTimebase) throws InterruptedException {
      long streamTimestamp = picture.getTimeStamp();
      Rational systemTimeBase = Rational.make(1, 1000000000);
      streamTimestamp = systemTimeBase.rescale(streamTimestamp, streamTimebase);

      long systemTimestamp = System.nanoTime();
      long delay = (streamTimestamp - (systemTimestamp - systemStartTime));
      if (delay > 0) {
         Thread.sleep(delay / 1000000, (int) (delay % 1000000));
      }

      BufferedImage image = converter.toImage(null, picture);
      window.setImage(image);
   }

   public static void main(String[] args) {
      String videoPath = "F:\\FoxKids\\Batman Przyszłości\\S01E04-Golem.mkv";
      try {
         System.out.println("Odtwarzanie: " + videoPath);
         play(videoPath);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}