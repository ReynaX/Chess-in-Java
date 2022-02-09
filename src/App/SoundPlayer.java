package App;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;

public class SoundPlayer{
    public SoundPlayer(String resourceName)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        InputStream is = getClass().getResourceAsStream(resourceName);
        assert is != null;
        AudioInputStream sound = AudioSystem.getAudioInputStream(is);

        m_clip1 = AudioSystem.getClip();
        m_clip1.open(sound);
        m_clip2 = AudioSystem.getClip();
        m_clip2.open(sound);
    }

    public void play(){
        if(m_clip1.isRunning()){
            m_clip2.stop();
            m_clip2.setMicrosecondPosition(0);
            m_clip2.start();
        }else{
            m_clip1.stop();
            m_clip1.setMicrosecondPosition(0);
            m_clip1.start();
        }
    }

    private final Clip m_clip1;
    private final Clip m_clip2;
}
// Copied from stackoverflow (most of it) :-)